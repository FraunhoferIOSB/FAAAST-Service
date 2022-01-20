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

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Reference;
import org.eclipse.milo.opcua.sdk.server.identity.AnonymousIdentityValidator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


public class OpcUaAssetConnectionTest {
    private static final long DEFAULT_TIMEOUT = 1000;
    private static EmbeddedOpcUaServer server;
    private static String serverUrl;

    @BeforeClass
    public static void init() {
        try {
            server = new EmbeddedOpcUaServer(
                    AnonymousIdentityValidator.INSTANCE);
            server.startup().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        serverUrl = "opc.tcp://localhost:" + 8081 + "/milo";
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
        doReturn(Property.class).when(serviceContext).getElementType(reference);
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
        expected.setValue("0.0");
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
        doReturn(Property.class).when(serviceContext).getElementType(reference);
        config.getValueProviders().put(reference, valueProvider);

        OpcUaAssetConnection connection = new OpcUaAssetConnection();
        connection.init(CoreConfig.builder().build(), config, serviceContext);

        PropertyValue expected = new PropertyValue();
        expected.setValue("3.1");
        //write value
        connection.getValueProviders().get(reference).setValue(expected);

        PropertyValue actual;
        actual = (PropertyValue) connection.getValueProviders().get(reference).getValue();
        System.out.println("Value should be 3.1 but is: "+actual.getValue());

        //the values are read at the time the provider is registered, so we have to init again to see the changed value
        connection.init(CoreConfig.builder().build(), config, serviceContext);
        actual = (PropertyValue) connection.getValueProviders().get(reference).getValue();
        System.out.println("Value should be 3.1: "+actual.getValue());
        Assert.assertEquals(expected, connection.getValueProviders().get(reference).getValue());
    }
}
