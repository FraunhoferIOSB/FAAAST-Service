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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.digitalpetri.modbus.server.ModbusTcpServer;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.model.ModbusDatatype;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.util.ModbusHelper;
import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.junit.Assert;
import org.junit.Test;


public class ModbusAssetConnectionTest {

    @Test
    public void testConnectNoTls() throws Exception {
        int port = PortHelper.findFreePort();
        ModbusTcpServer server = ModbusHelper.getServer(port, false);

        Reference reference = ReferenceHelper.parseReference("(Property)[ID_SHORT]Temperature");

        ModbusAssetConnectionConfig connectionConfig = ModbusAssetConnectionConfig.builder()
                .hostname(InetAddress.getLoopbackAddress().getHostName())
                .port(port)
                .connectTimeout(10000)
                .requestTimeout(10000)
                .tlsEnabled(false)
                .valueProvider(reference,
                        new ModbusValueProviderConfig.Builder()
                                .address(0)
                                .dataType(ModbusDatatype.HOLDING_REGISTER)
                                .quantity(1)
                                .build())

                .build();

        PropertyValue expected = PropertyValue.of(Datatype.INTEGER, "3");
        ServiceContext serviceContext = mockedContext(expected, reference);

        server.start();

        ModbusAssetConnection connection = connectionConfig.newInstance(CoreConfig.DEFAULT, serviceContext);
        awaitConnection(connection);
        connection.getValueProviders().get(reference).setValue(expected);
        DataElementValue actual = connection.getValueProviders().get(reference).getValue();
        connection.disconnect();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testConnectYesTls() throws Exception {
        int port = PortHelper.findFreePort();
        ModbusTcpServer server = ModbusHelper.getServer(port, true);

        Reference reference = ReferenceHelper.parseReference("(Property)[ID_SHORT]Temperature");

        ModbusAssetConnectionConfig connectionConfig = ModbusAssetConnectionConfig.builder()
                .hostname(InetAddress.getLoopbackAddress().getHostName())
                .port(port)
                .connectTimeout(10000)
                .requestTimeout(10000)
                .tlsEnabled(true)
                .keyCertificateConfig(CertificateConfig.builder()
                        .keyStorePath("src/test/resources/client.p12")
                        .keyStorePassword("123456")
                        .build())
                .trustCertificateConfig(CertificateConfig.builder()
                        .keyStorePath("src/test/resources/client-truststore.p12")
                        .keyStorePassword("123456")
                        .build())
                .valueProvider(reference,
                        new ModbusValueProviderConfig.Builder()
                                .address(0)
                                .dataType(ModbusDatatype.HOLDING_REGISTER)
                                .quantity(1)
                                .build())
                .build();

        PropertyValue expected = PropertyValue.of(Datatype.INTEGER, "3");
        ServiceContext serviceContext = mockedContext(expected, reference);

        server.start();

        ModbusAssetConnection connection = connectionConfig.newInstance(CoreConfig.DEFAULT, serviceContext);
        awaitConnection(connection);
        connection.getValueProviders().get(reference).setValue(expected);
        DataElementValue actual = connection.getValueProviders().get(reference).getValue();
        connection.disconnect();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testReconnect() {
        // TODO
        // create server, start server
        // set value at an address of the server
        // create and init assetconnection with a subscription
        // add listener to subscription
        // get the previously set value, assert it is correct
        // shutdown modbus server, wait until !connection.isConnected(), restart server, wait til connection.isConnected(),
        // update value at address
        // add listener, get value, validate again
    }


    @Test
    public void testOperationProvider() {
        // TODO if operations are implemented
    }


    @Test
    public void testSubscriptionProvider() {
        // TODO
        // Start server, assetConnection
        // valueProvider and subscriptionProvider
        // with valueprovider, change value; with subProvider, check if dataListeners get notified
    }


    @Test
    public void testValueProvider() {
        // TODO
    }


    private ServiceContext mockedContext(PropertyValue expected, Reference reference) throws PersistenceException, ResourceNotFoundException {
        ServiceContext serviceContext = mock(ServiceContext.class);
        doReturn(ElementValueTypeInfo.builder()
                .type(expected.getClass())
                .datatype(expected.getValue().getDataType())
                .build())
                .when(serviceContext)
                .getTypeInfo(reference);

        return serviceContext;
    }


    private void awaitConnection(AssetConnection connection) {
        await().atMost(90, TimeUnit.SECONDS)
                .with()
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> {
                    try {
                        connection.connect();
                    }
                    catch (AssetConnectionException e) {
                        throw new AssetConnectionException(e);
                        // do nothing
                    }
                    return connection.isConnected();
                });
    }
}
