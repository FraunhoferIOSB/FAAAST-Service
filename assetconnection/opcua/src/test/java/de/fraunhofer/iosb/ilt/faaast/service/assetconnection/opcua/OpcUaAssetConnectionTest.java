/*
 * Copyright (c) 2021 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.DoubleValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.StringValue;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeContext;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import org.eclipse.milo.opcua.sdk.server.identity.AnonymousIdentityValidator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


public class OpcUaAssetConnectionTest {
    private static final long DEFAULT_TIMEOUT = 1000;
    private static EmbeddedOpcUaServer server;
    private static String serverUrl;
    private static int opcPort;
    private static int httpsPort;

    @BeforeClass
    public static void init() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Assert.assertNotNull(serverSocket);
            Assert.assertTrue(serverSocket.getLocalPort() > 0);
            opcPort = serverSocket.getLocalPort();
        }
        catch (IOException e) {
            Assert.fail("could not find free port");
        }
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Assert.assertNotNull(serverSocket);
            Assert.assertTrue(serverSocket.getLocalPort() > 0);
            httpsPort = serverSocket.getLocalPort();
        }
        catch (IOException e) {
            Assert.fail("could not find free port");
        }
        try {
            server = new EmbeddedOpcUaServer(
                    AnonymousIdentityValidator.INSTANCE, opcPort, httpsPort);
            server.startup().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        serverUrl = "opc.tcp://localhost:" + opcPort + "/milo";
    }

    @Test
    public void testSubscriptionProvider() throws AssetConnectionException, InterruptedException {
        OpcUaAssetConnectionConfig config = new OpcUaAssetConnectionConfig();
        config.setHost(serverUrl);
        OpcUaSubscriptionProviderConfig subProvider = new OpcUaSubscriptionProviderConfig();
        subProvider.setNodeId("ns=2;s=HelloWorld/Dynamic/Double");
        subProvider.setInterval(1000);

        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        ServiceContext serviceContext = mock(ServiceContext.class);
        TypeContext contextExample = new TypeContext();
        TypeInfo infoExample = new TypeInfo();
        infoExample.setValueType(PropertyValue.class);
        infoExample.setDatatype(Datatype.Double);
        contextExample.setRootInfo(infoExample);
        doReturn(contextExample).when(serviceContext).getTypeInfo(reference);

        config.getSubscriptionProviders().put(reference, subProvider);

        OpcUaAssetConnection connection = new OpcUaAssetConnection();
        connection.init(CoreConfig.builder().build(), config, serviceContext);

        final AtomicReference<DataElementValue> response = new AtomicReference<>();
        CountDownLatch condition = new CountDownLatch(1);
        connection.getSubscriptionProviders().get(reference).addNewDataListener(new NewDataListener() {
            @Override
            public void newDataReceived(DataElementValue data) {
                // we received data via OPC
                response.set(data);
                condition.countDown();
            }
        });
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        PropertyValue expected = new PropertyValue();
        expected.setValue(new DoubleValue(0.0));
        Assert.assertEquals(expected, response.get());
    }

    @Test
    public void testValueProvider() throws AssetConnectionException, InterruptedException {
        OpcUaAssetConnectionConfig config = new OpcUaAssetConnectionConfig();
        config.setHost(serverUrl);
        OpcUaValueProviderConfig valueProvider = new OpcUaValueProviderConfig();
        valueProvider.setNodeId("ns=2;s=HelloWorld/ScalarTypes/Double");

        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        ServiceContext serviceContext = mock(ServiceContext.class);
        TypeContext contextExample = new TypeContext();
        TypeInfo infoExample = new TypeInfo();
        infoExample.setValueType(PropertyValue.class);
        infoExample.setDatatype(Datatype.Double);
        contextExample.setRootInfo(infoExample);
        doReturn(contextExample).when(serviceContext).getTypeInfo(reference);

        config.getValueProviders().put(reference, valueProvider);

        OpcUaAssetConnection connection = new OpcUaAssetConnection();
        connection.init(CoreConfig.builder().build(), config, serviceContext);

        PropertyValue expected = new PropertyValue();
        expected.setValue(new DoubleValue(3.1));
        //write value
        connection.getValueProviders().get(reference).setValue(expected);

        //assert that new value is equal to written value
        Assert.assertEquals(expected, connection.getValueProviders().get(reference).getValue());
    }

    @Test
    public void testOperationProvider() throws AssetConnectionException, InterruptedException {
        OpcUaAssetConnectionConfig config = new OpcUaAssetConnectionConfig();
        config.setHost(serverUrl);
        OpcUaOperationProviderConfig opProvider = new OpcUaOperationProviderConfig();
        opProvider.setNodeId("ns=2;s=HelloWorld/sqrt(x)");

        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        ServiceContext serviceContext = mock(ServiceContext.class);

        config.getOperationProviders().put(reference, opProvider);

        OpcUaAssetConnection connection = new OpcUaAssetConnection();
        connection.init(CoreConfig.builder().build(), config, serviceContext);

        DefaultOperationVariable x = new DefaultOperationVariable();
        DefaultOperationVariable y = new DefaultOperationVariable();
        DefaultProperty xProp = new DefaultProperty();
        DefaultProperty yProp = new DefaultProperty();
        //todo: need type safe operation variable
        xProp.setValue(String.valueOf(4));
        //todo: is value type used for data types?
        xProp.setValueType("Double");
        x.setValue(xProp);
        y.setValue(yProp);

        OperationVariable[] output =
        connection.getOperationProviders().get(reference).invoke(new OperationVariable[]{x}, new OperationVariable[]{y});
        String expected = "2.0";
        DefaultProperty actualProperty = (DefaultProperty) output[0].getValue();
        String actual = actualProperty.getValue();
        Assert.assertEquals(expected, actual);
    }
}
