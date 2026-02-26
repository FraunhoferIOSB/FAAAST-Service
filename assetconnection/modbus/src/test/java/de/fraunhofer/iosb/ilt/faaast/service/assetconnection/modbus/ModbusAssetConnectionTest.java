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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.digitalpetri.modbus.client.ModbusTcpClient;
import com.digitalpetri.modbus.pdu.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.pdu.WriteSingleRegisterRequest;
import com.digitalpetri.modbus.server.ModbusTcpServer;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.model.ModbusDatatype;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.testutil.ModbusTestHelper;
import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.BooleanValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.HexBinaryValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.IntValue;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.junit.Assert;
import org.junit.Test;


public class ModbusAssetConnectionTest {

    @Test
    public void testConnectNoTls() throws Exception {
        int port = PortHelper.findFreePort();
        ModbusTcpServer server = ModbusTestHelper.getServer(port, false);

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
        assertEquals(expected, actual);

        server.stop();
    }


    @Test
    public void testConnectWithTls() throws Exception {
        int port = PortHelper.findFreePort();
        ModbusTcpServer server = ModbusTestHelper.getServer(port, true);

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
        assertEquals(expected, actual);

        server.stop();
    }


    @Test
    public void testReconnect() throws Exception {
        int pollingRate = 1000;
        int modbusAddress = 42;
        int unitId = 1;
        int value = 42;
        int newValue = 43;

        Reference reference = ReferenceHelper.parseReference("(Property)[ID_SHORT]Temperature");
        ServiceContext serviceContext = mockedContext(PropertyValue.of(Datatype.INT, String.valueOf(value)), reference);

        // create server, start server
        int port = PortHelper.findFreePort();
        ModbusTcpServer server = ModbusTestHelper.getServer(port, false);
        server.start();

        // set value at an address of the server
        writeToServer(port, unitId, modbusAddress, value);
        // create and init assetconnection with a subscription
        var connConfig = ModbusAssetConnectionConfig.builder()
                .hostname(InetAddress.getLoopbackAddress().getHostName())
                .port(port)
                .connectTimeout(12345)
                .requestTimeout(12345)
                .tlsEnabled(false)
                .subscriptionProvider(reference,
                        new ModbusSubscriptionProviderConfig.Builder()
                                .address(modbusAddress)
                                .dataType(ModbusDatatype.HOLDING_REGISTER)
                                .quantity(1)
                                .pollingRate(pollingRate)
                                .unitId(unitId)
                                .build())
                .valueProvider(reference,
                        new ModbusValueProviderConfig.Builder()
                                .address(modbusAddress)
                                .dataType(ModbusDatatype.HOLDING_REGISTER)
                                .quantity(1)
                                .unitId(unitId)
                                .build())
                .build();

        var assetConnection = connConfig.newInstance(CoreConfig.DEFAULT, serviceContext);
        awaitConnection(assetConnection);

        // add listener to subscription
        // Assert that the next value received from the subscription is 43

        final AtomicReference<DataElementValue> updatedResponse = new AtomicReference<>();
        CountDownLatch conditionUpdated = new CountDownLatch(1);
        NewDataListener updatedListener = (DataElementValue data) -> {
            updatedResponse.set(data);
            conditionUpdated.countDown();
        };

        assetConnection.getSubscriptionProviders().get(reference).addNewDataListener(updatedListener);

        // get the previously set value, assert it is correct
        assertEquals(42, (int) ((IntValue) ((PropertyValue) assetConnection.getValueProviders().get(reference).getValue()).getValue()).getValue());

        // shutdown modbus server, wait until !connection.isConnected(), restart server, wait til connection.isConnected(),
        server.stop();
        await().atMost(Duration.ofSeconds(30))
                .until(() -> !assetConnection.isConnected());

        server.start();
        await().atMost(Duration.ofSeconds(30))
                .until(assetConnection::isConnected);

        // update value at address
        writeToServer(port, unitId, modbusAddress, newValue);

        // add listener, get value, validate again
        assertTrue(conditionUpdated.await(pollingRate + 15000, TimeUnit.MILLISECONDS));
        assertEquals(newValue, (int) ((PropertyValue) updatedResponse.get()).getValue().getValue());

        server.stop();
    }


    @Test
    public void testSubscriptionProvider() throws Exception {
        // Start server, assetConnection
        int pollingRate = 1000;
        int modbusAddress = 42;
        int unitId = 1;
        int value = 42;
        int newValue = 43;

        Reference reference = ReferenceHelper.parseReference("(Property)[ID_SHORT]Temperature");
        ServiceContext serviceContext = mockedContext(PropertyValue.of(Datatype.INT, String.valueOf(value)), reference);

        // create server, start server
        int port = PortHelper.findFreePort();
        ModbusTcpServer server = ModbusTestHelper.getServer(port, false);
        server.start();

        // valueProvider and subscriptionProvider
        var connConfig = ModbusAssetConnectionConfig.builder()
                .hostname(InetAddress.getLoopbackAddress().getHostName())
                .port(port)
                .connectTimeout(12345)
                .requestTimeout(12345)
                .tlsEnabled(false)
                .subscriptionProvider(reference,
                        new ModbusSubscriptionProviderConfig.Builder()
                                .address(modbusAddress)
                                .dataType(ModbusDatatype.HOLDING_REGISTER)
                                .quantity(1)
                                .pollingRate(pollingRate)
                                .unitId(unitId)
                                .build())
                .valueProvider(reference,
                        new ModbusValueProviderConfig.Builder()
                                .address(modbusAddress)
                                .dataType(ModbusDatatype.HOLDING_REGISTER)
                                .quantity(1)
                                .unitId(unitId)
                                .build())
                .build();

        var assetConnection = connConfig.newInstance(CoreConfig.DEFAULT, serviceContext);
        awaitConnection(assetConnection);

        // with value provider, change value; with subProvider, check if dataListeners get notified
        final AtomicReference<Boolean> notified = new AtomicReference<>(false);
        CountDownLatch conditionUpdated = new CountDownLatch(1);
        assetConnection.getSubscriptionProviders().get(reference).addNewDataListener(data -> {
            notified.set(true);
            conditionUpdated.countDown();
        });

        assetConnection.getValueProviders().get(reference).setValue(new PropertyValue(new IntValue(newValue)));

        assertTrue(conditionUpdated.await(pollingRate + 100, TimeUnit.MILLISECONDS));
        assertTrue(notified.get());
        server.stop();
    }


    @Test
    public void testValueProvider() throws Exception {
        int port = PortHelper.findFreePort();
        ModbusTcpServer server = ModbusTestHelper.getServer(port, false);
        server.start();

        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.INTEGER, "787878787878787878787878787878"), 2, 7);
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.UNSIGNED_BYTE, "78"), 17);
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.INT, "78"), 1);
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.STRING, "testtest"), 49, "testtest".getBytes(StandardCharsets.UTF_8).length / 2);
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.BOOLEAN, new BooleanValue(false).asString()), 9);
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.BYTE, "-78"), 10);
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.SHORT, "-189"), 11);
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.INT, "78"), 12);
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.LONG, "7878787878787878787"), 13, 4);
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.UNSIGNED_SHORT, "78"), 18);
        // UnsignedInt has >4 bytes;
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.UNSIGNED_INT, "2222278"), 19, 4);
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.UNSIGNED_LONG, "78"), 24, 5);

        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.POSITIVE_INTEGER, "78"), 30);
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.NON_NEGATIVE_INTEGER, "78"), 33);
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.NON_NEGATIVE_INTEGER, "0"), 1234);
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.NEGATIVE_INTEGER, "-78"), 1234);
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.NON_POSITIVE_INTEGER, "-78"), 1234);
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.NON_POSITIVE_INTEGER, "0"), 1234);
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.HEX_BINARY, new HexBinaryValue(new byte[] {
                0x0,
                0xF
        }).asString()), 1234);
        assertWriteReadRegister(port, 1, PropertyValue.of(Datatype.BASE64_BINARY, "0xE"), 1234);

        server.stop();
    }


    private void assertWriteReadRegister(int port, int unitId, PropertyValue expected, int address) throws Exception {
        assertWriteReadRegister(port, unitId, expected, address, 1);
    }


    // From opcua test
    private void assertWriteReadRegister(int port, int unitId, PropertyValue expected, int address, int quantity) throws Exception {
        Reference reference = ReferenceHelper.parseReference("(Property)[ID_SHORT]Temperature");
        ServiceContext serviceContext = mock(ServiceContext.class);
        doReturn(ElementValueTypeInfo.builder()
                .type(expected.getClass())
                .datatype(expected.getValue().getDataType())
                .build())
                .when(serviceContext)
                .getTypeInfo(reference);

        ModbusAssetConnectionConfig config = ModbusAssetConnectionConfig.builder()
                .hostname(InetAddress.getLoopbackAddress().getHostName())
                .port(port)
                .requestTimeout(10001)
                .connectTimeout(10001)
                .valueProvider(reference,
                        new ModbusValueProviderConfig.Builder()
                                .unitId(unitId)
                                .address(address)
                                .quantity(quantity)
                                .dataType(ModbusDatatype.HOLDING_REGISTER)
                                .build())
                .build();
        ModbusAssetConnection connection = config.newInstance(CoreConfig.DEFAULT, serviceContext);
        awaitConnection(connection);
        connection.getValueProviders().get(reference).setValue(expected);
        PropertyValue actual = (PropertyValue) connection.getValueProviders().get(reference).getValue();
        connection.disconnect();
        if (expected.getValue().getValue() instanceof byte[]) {
            Assert.assertArrayEquals((byte[]) expected.getValue().getValue(), (byte[]) actual.getValue().getValue());
        }
        else {
            Assert.assertEquals(expected.getValue().getValue(), actual.getValue().getValue());
        }
        // Clean up
        if (quantity > 1) {
            writeToServer(port, unitId, address, new byte[quantity * 2]);
        }
        else {
            writeToServer(port, unitId, address, 0);
        }
    }


    private void writeToServer(int port, int unitId, int address, int value) throws Exception {
        ModbusTcpClient client = ModbusTestHelper.getClient(port);
        client.connect();

        client.writeSingleRegister(unitId, new WriteSingleRegisterRequest(address, value));
    }


    private void writeToServer(int port, int unitId, int address, byte[] value) throws Exception {
        ModbusTcpClient client = ModbusTestHelper.getClient(port);
        client.connect();
        client.writeMultipleRegisters(unitId, new WriteMultipleRegistersRequest(address, value.length / 2, value));
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
