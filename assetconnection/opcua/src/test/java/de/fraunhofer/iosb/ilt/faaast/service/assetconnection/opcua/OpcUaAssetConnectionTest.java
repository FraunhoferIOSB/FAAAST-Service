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

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.ArgumentMapping;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.util.OpcUaHelper;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DateTimeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.server.identity.AnonymousIdentityValidator;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.junit.Assert;
import org.junit.Test;


public class OpcUaAssetConnectionTest extends AbstractOpcUaBasedTest {

    @Test
    public void testSubscriptionProviderWithScalarValues()
            throws AssetConnectionException, InterruptedException, ValueFormatException, ExecutionException, UaException, ConfigurationInitializationException, Exception {
        assertSubscribe("ns=2;s=HelloWorld/ScalarTypes/Double", PropertyValue.of(Datatype.DOUBLE, "0.1"), null);
    }


    @Test
    public void testSubscriptionProviderWithArrayValues()
            throws AssetConnectionException, InterruptedException, ValueFormatException, ExecutionException, UaException, ConfigurationInitializationException, Exception {
        assertSubscribe("ns=2;s=HelloWorld/MatrixTypes/DoubleArray", PropertyValue.of(Datatype.DOUBLE, "5.3"), "[3][2]");
    }


    private void setOpcUaValue(OpcUaAssetConnectionConfig config, String nodeId, Object value) throws Exception {
        OpcUaClient client = OpcUaHelper.connect(config);
        client.connect().get();
        StatusCode statusCode = OpcUaHelper.writeValue(client, nodeId, value);
        Assert.assertTrue(statusCode.isGood());
        client.disconnect().get();
    }


    @Test
    public void testReconnect() throws Exception {
        int assetTcpPort = findFreePort();
        int assetHttpsPort = findFreePort();
        double initialValue = 1.0;
        double updatedValue = 2.2;
        String assetEndpoint = "opc.tcp://localhost:" + assetTcpPort + "/milo";
        String nodeId = "ns=2;s=HelloWorld/ScalarTypes/Double";
        PropertyValue expectedInitial = PropertyValue.of(Datatype.DOUBLE, Double.toString(initialValue));
        PropertyValue expectedUpdated = PropertyValue.of(Datatype.DOUBLE, Double.toString(updatedValue));
        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        ServiceContext serviceContext = mock(ServiceContext.class);
        TypeInfo infoExample = ElementValueTypeInfo.builder()
                .type(PropertyValue.class)
                .datatype(Datatype.DOUBLE)
                .build();
        doReturn(infoExample).when(serviceContext).getTypeInfo(reference);
        OpcUaAssetConnectionConfig config = OpcUaAssetConnectionConfig.builder()
                .host(assetEndpoint)
                .subscriptionProvider(reference, OpcUaSubscriptionProviderConfig.builder()
                        .nodeId(nodeId)
                        .build())
                .build();
        // start asset
        EmbeddedOpcUaServer asset = new EmbeddedOpcUaServer(AnonymousIdentityValidator.INSTANCE, assetTcpPort, assetHttpsPort);
        asset.startup().get();
        // set asset value to initial value
        setOpcUaValue(config, nodeId, initialValue);
        // start asset connection & wait for initial value
        OpcUaAssetConnection connection = config.newInstance(CoreConfig.DEFAULT, serviceContext);
        connection.connect();
        final AtomicReference<DataElementValue> initialResponse = new AtomicReference<>();
        CountDownLatch conditionOriginalValue = new CountDownLatch(1);
        NewDataListener initialListener = (DataElementValue data) -> {
            initialResponse.set(data);
            conditionOriginalValue.countDown();
        };
        connection.getSubscriptionProviders().get(reference).addNewDataListener(initialListener);
        Assert.assertTrue(String.format("test failed because there was no response within defined time (%d %s)", getWaitTime(), TimeUnit.MILLISECONDS),
                conditionOriginalValue.await(getWaitTime(), TimeUnit.MILLISECONDS));
        Assert.assertEquals(expectedInitial, initialResponse.get());
        connection.getSubscriptionProviders().get(reference).removeNewDataListener(initialListener);
        // stop asset
        asset.shutdown().get();
        await().atMost(30, TimeUnit.SECONDS)
                .until(() -> !connection.isConnected());
        // restart asset
        asset = new EmbeddedOpcUaServer(AnonymousIdentityValidator.INSTANCE, assetTcpPort, assetHttpsPort);
        asset.startup().get();
        await().atMost(30, TimeUnit.SECONDS)
                .until(() -> connection.isConnected());
        // set value on asset to updated value
        setOpcUaValue(config, nodeId, updatedValue);
        // wait for updated value from asset connection
        final AtomicReference<DataElementValue> updatedResponse = new AtomicReference<>();
        CountDownLatch conditionUpdated = new CountDownLatch(1);
        NewDataListener updatedListener = (DataElementValue data) -> {
            updatedResponse.set(data);
            conditionUpdated.countDown();
        };
        connection.getSubscriptionProviders().get(reference).addNewDataListener(updatedListener);
        Assert.assertTrue(String.format("test failed because there was no response within defined time (%d %s)", getWaitTime(), TimeUnit.MILLISECONDS),
                conditionUpdated.await(getWaitTime(), TimeUnit.MILLISECONDS));
        Assert.assertEquals(expectedUpdated, updatedResponse.get());
        asset.shutdown().get();
    }


    private void awaitConnection(AssetConnection connection) {
        await().atMost(30, TimeUnit.SECONDS)
                .with()
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> {
                    try {
                        connection.connect();
                    }
                    catch (AssetConnectionException e) {
                        // do nothing
                    }
                    return connection.isConnected();
                });
    }


    private void assertSubscribe(String nodeId, PropertyValue expected, String elementIndex)
            throws AssetConnectionException, InterruptedException, ExecutionException, UaException, ConfigurationInitializationException, Exception {
        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        long interval = 1000;
        ServiceContext serviceContext = mock(ServiceContext.class);
        TypeInfo infoExample = ElementValueTypeInfo.builder()
                .type(PropertyValue.class)
                .datatype(expected.getValue().getDataType())
                .build();
        doReturn(infoExample).when(serviceContext).getTypeInfo(reference);

        OpcUaAssetConnectionConfig config = OpcUaAssetConnectionConfig.builder()
                .host(serverUrl)
                .subscriptionProvider(reference, OpcUaSubscriptionProviderConfig.builder()
                        .nodeId(nodeId)
                        .interval(interval)
                        .arrayIndex(elementIndex)
                        .build())
                .valueProvider(reference,
                        OpcUaValueProviderConfig.builder()
                                .nodeId(nodeId)
                                .arrayIndex(elementIndex)
                                .build())
                .build();
        OpcUaAssetConnection connection = config.newInstance(
                CoreConfig.builder()
                        .build(),
                serviceContext);
        awaitConnection(connection);
        // first value should always be the current value
        OpcUaClient client = OpcUaHelper.connect(config);
        DataValue originalValue = OpcUaHelper.readValue(client, nodeId);
        client.disconnect().get();
        final AtomicReference<DataElementValue> originalValueResponse = new AtomicReference<>();
        CountDownLatch conditionOriginalValue = new CountDownLatch(1);
        connection.getSubscriptionProviders().get(reference).addNewDataListener((DataElementValue data) -> {
            originalValueResponse.set(data);
            conditionOriginalValue.countDown();
        });
        Assert.assertTrue(String.format("test failed because there was no response within defined time (%d %s)", getWaitTime(), TimeUnit.MILLISECONDS),
                conditionOriginalValue.await(getWaitTime(), TimeUnit.MILLISECONDS));
        if ((elementIndex == null) || elementIndex.equals("")) {
            Assert.assertEquals(
                    PropertyValue.of(expected.getValue().getDataType(), originalValue.getValue().getValue().toString()),
                    originalValueResponse.get());
        }
        // second value should be new value
        final AtomicReference<DataElementValue> newValueResponse = new AtomicReference<>();
        CountDownLatch conditionNewValue = new CountDownLatch(1);
        connection.getSubscriptionProviders().get(reference).addNewDataListener((DataElementValue data) -> {
            newValueResponse.set(data);
            conditionNewValue.countDown();
        });
        connection.getValueProviders().get(reference).setValue(expected);
        Assert.assertTrue(String.format("test failed because there was no response within defined time (%d %s)", getWaitTime(), TimeUnit.MILLISECONDS),
                conditionNewValue.await(getWaitTime(), TimeUnit.MILLISECONDS));
        Assert.assertEquals(expected, newValueResponse.get());
    }


    private void assertWriteReadValue(String nodeId, PropertyValue expected, String arrayIndex)
            throws AssetConnectionException, InterruptedException, ConfigurationInitializationException, ConfigurationException {
        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        ServiceContext serviceContext = mock(ServiceContext.class);
        doReturn(ElementValueTypeInfo.builder()
                .type(expected.getClass())
                .datatype(expected.getValue().getDataType())
                .build())
                        .when(serviceContext)
                        .getTypeInfo(reference);
        OpcUaAssetConnectionConfig config = OpcUaAssetConnectionConfig.builder()
                .valueProvider(reference,
                        OpcUaValueProviderConfig.builder()
                                .nodeId(nodeId)
                                .arrayIndex(arrayIndex)
                                .build())
                .host(serverUrl)
                .build();
        OpcUaAssetConnection connection = config.newInstance(CoreConfig.DEFAULT, serviceContext);
        awaitConnection(connection);
        connection.getValueProviders().get(reference).setValue(expected);
        DataElementValue actual = connection.getValueProviders().get(reference).getValue();
        connection.disconnect();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testValueProviderWithScalarValues()
            throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException, ConfigurationException {
        assertWriteReadValue("ns=2;s=HelloWorld/ScalarTypes/Double", PropertyValue.of(Datatype.DOUBLE, "3.3"), null);
        assertWriteReadValue("ns=2;s=HelloWorld/ScalarTypes/String", PropertyValue.of(Datatype.STRING, "hello world!"), null);
        assertWriteReadValue("ns=2;s=HelloWorld/ScalarTypes/Integer", PropertyValue.of(Datatype.INTEGER, "42"), null);
        assertWriteReadValue("ns=2;s=HelloWorld/ScalarTypes/Boolean", PropertyValue.of(Datatype.BOOLEAN, "true"), null);
        assertWriteReadValue("ns=2;s=HelloWorld/ScalarTypes/DateTime",
                PropertyValue.of(Datatype.DATE_TIME, ZonedDateTime.of(2022, 11, 28, 14, 12, 35, 0, ZoneId.of(DateTimeValue.DEFAULT_TIMEZONE)).toString()), null);
    }


    @Test
    public void testValueProviderWithArrayValues()
            throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException, ConfigurationException {
        assertWriteReadValue("ns=2;s=HelloWorld/ArrayTypes/Int32Array", PropertyValue.of(Datatype.INT, "78"), "[2]");
        assertWriteReadValue("ns=2;s=HelloWorld/ArrayTypes/FloatArray", PropertyValue.of(Datatype.FLOAT, "24.5"), "[1]");
        assertWriteReadValue("ns=2;s=HelloWorld/ArrayTypes/StringArray", PropertyValue.of(Datatype.STRING, "new test value"), "[3]");
        assertWriteReadValue("ns=2;s=HelloWorld/MatrixTypes/DoubleArray", PropertyValue.of(Datatype.DOUBLE, "789.5"), "[2][4]");
        assertWriteReadValue("ns=2;s=HelloWorld/MatrixTypes/BooleanArray", PropertyValue.of(Datatype.BOOLEAN, "true"), "[1][0]");
    }


    private void assertInvokeOperation(
                                       String nodeId,
                                       boolean sync,
                                       Map<String, PropertyValue> input,
                                       Map<String, PropertyValue> inoutput,
                                       Map<String, PropertyValue> expectedInoutput,
                                       Map<String, PropertyValue> expectedOutput)
            throws AssetConnectionException, InterruptedException, ConfigurationInitializationException, ConfigurationException {
        assertInvokeOperation(nodeId, sync, input, inoutput, expectedInoutput, expectedOutput, null, null);
    }


    private void assertInvokeOperation(String nodeId,
                                       boolean sync,
                                       Map<String, PropertyValue> input,
                                       Map<String, PropertyValue> inoutput,
                                       Map<String, PropertyValue> expectedInoutput,
                                       Map<String, PropertyValue> expectedOutput,
                                       List<ArgumentMapping> inputMapping,
                                       List<ArgumentMapping> outputMapping)
            throws AssetConnectionException, InterruptedException, ConfigurationInitializationException, ConfigurationException {
        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        OpcUaAssetConnectionConfig config = OpcUaAssetConnectionConfig.builder()
                .host(serverUrl)
                .operationProvider(reference,
                        OpcUaOperationProviderConfig.builder()
                                .nodeId(nodeId)
                                .inputArgumentMappings(inputMapping)
                                .outputArgumentMappings(outputMapping)
                                .build())
                .build();
        OperationVariable[] inputVariables = input == null
                ? new OperationVariable[0]
                : input.entrySet().stream().map(x -> {
                    Property property = new DefaultProperty.Builder()
                            .idShort(x.getKey())
                            .build();
                    ElementValueMapper.setValue(property, x.getValue());
                    return new DefaultOperationVariable.Builder()
                            .value(property)
                            .build();
                }).toArray(OperationVariable[]::new);
        OperationVariable[] inoutputVariables = inoutput == null
                ? new OperationVariable[0]
                : inoutput.entrySet().stream().map(x -> {
                    Property property = new DefaultProperty.Builder()
                            .idShort(x.getKey())
                            .build();
                    ElementValueMapper.setValue(property, x.getValue());
                    return new DefaultOperationVariable.Builder()
                            .value(property)
                            .build();
                }).toArray(OperationVariable[]::new);
        OperationVariable[] expectedInOut = expectedInoutput == null
                ? new OperationVariable[0]
                : expectedInoutput.entrySet().stream().map(x -> {
                    Property property = new DefaultProperty.Builder()
                            .idShort(x.getKey())
                            .build();
                    ElementValueMapper.setValue(property, x.getValue());
                    return new DefaultOperationVariable.Builder()
                            .value(property)
                            .build();
                }).toArray(OperationVariable[]::new);
        OperationVariable[] expectedOut = expectedOutput == null
                ? new OperationVariable[0]
                : expectedOutput.entrySet().stream().map(x -> {
                    Property property = new DefaultProperty.Builder()
                            .idShort(x.getKey())
                            .build();
                    ElementValueMapper.setValue(property, x.getValue());
                    return new DefaultOperationVariable.Builder()
                            .value(property)
                            .build();
                }).toArray(OperationVariable[]::new);
        ServiceContext serviceContext = mock(ServiceContext.class);
        doReturn(expectedOut)
                .when(serviceContext)
                .getOperationOutputVariables(reference);
        OpcUaAssetConnection connection = config.newInstance(CoreConfig.DEFAULT, serviceContext);
        awaitConnection(connection);
        OperationVariable[] actual;
        if (sync) {
            actual = connection.getOperationProviders().get(reference).invoke(inputVariables, inoutputVariables);
        }
        else {
            final AtomicReference<OperationVariable[]> operationResult = new AtomicReference<>();
            final AtomicReference<OperationVariable[]> operationInout = new AtomicReference<>();
            CountDownLatch condition = new CountDownLatch(1);
            connection.getOperationProviders().get(reference).invokeAsync(inputVariables, inoutputVariables, (res, inout) -> {
                operationResult.set(res);
                operationInout.set(inout);
                condition.countDown();
            });
            condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            actual = operationResult.get();
            inoutputVariables = operationInout.get();
        }
        connection.disconnect();
        Assert.assertArrayEquals(expectedOut, actual);
        Assert.assertArrayEquals(expectedInOut, inoutputVariables);
    }


    @Test
    public void testOperationProvider() throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException, ConfigurationException {
        String nodeIdSqrt = "ns=2;s=HelloWorld/sqrt(x)";
        assertInvokeOperation(nodeIdSqrt,
                true,
                Map.of("x", PropertyValue.of(Datatype.DOUBLE, "9.0")),
                null,
                null,
                Map.of("x_sqrt", PropertyValue.of(Datatype.DOUBLE, "3.0")));
        assertInvokeOperation(nodeIdSqrt,
                false,
                Map.of("x", PropertyValue.of(Datatype.DOUBLE, "9.0")),
                null,
                null,
                Map.of("x_sqrt", PropertyValue.of(Datatype.DOUBLE, "3.0")));
        assertInvokeOperation(nodeIdSqrt,
                true,
                null,
                Map.of("x", PropertyValue.of(Datatype.DOUBLE, "4.0"),
                        "x_sqrt", PropertyValue.of(Datatype.DOUBLE, "4.0")),
                Map.of("x", PropertyValue.of(Datatype.DOUBLE, "4.0"),
                        "x_sqrt", PropertyValue.of(Datatype.DOUBLE, "2.0")),
                Map.of("x_sqrt", PropertyValue.of(Datatype.DOUBLE, "2.0")));
        assertInvokeOperation(nodeIdSqrt,
                false,
                null,
                Map.of("x", PropertyValue.of(Datatype.DOUBLE, "4.0"),
                        "x_sqrt", PropertyValue.of(Datatype.DOUBLE, "4.0")),
                Map.of("x", PropertyValue.of(Datatype.DOUBLE, "4.0"),
                        "x_sqrt", PropertyValue.of(Datatype.DOUBLE, "2.0")),
                Map.of("x_sqrt", PropertyValue.of(Datatype.DOUBLE, "2.0")));
    }


    @Test
    public void testOperationProviderMapping()
            throws AssetConnectionException, InterruptedException, ValueFormatException, ConfigurationInitializationException, ConfigurationException {
        String nodeIdSqrt = "ns=2;s=HelloWorld/sqrt(x)";
        assertInvokeOperation(nodeIdSqrt,
                true,
                Map.of("x_aas", PropertyValue.of(Datatype.DOUBLE, "4.0")),
                null,
                null,
                Map.of("x_sqrt", PropertyValue.of(Datatype.DOUBLE, "2.0")),
                List.of(ArgumentMapping.builder()
                        .idShort("x_aas")
                        .argumentName("x")
                        .build()),
                null);
        assertInvokeOperation(nodeIdSqrt,
                false,
                Map.of("x", PropertyValue.of(Datatype.DOUBLE, "4.0")),
                null,
                null,
                Map.of("x_sqrt_aas", PropertyValue.of(Datatype.DOUBLE, "2.0")),
                null,
                List.of(ArgumentMapping.builder()
                        .idShort("x_sqrt_aas")
                        .argumentName("x_sqrt")
                        .build()));
        assertInvokeOperation(nodeIdSqrt,
                true,
                null,
                Map.of("x_aas", PropertyValue.of(Datatype.DOUBLE, "4.0"),
                        "x_sqrt_aas", PropertyValue.of(Datatype.DOUBLE, "4.0")),
                Map.of("x_aas", PropertyValue.of(Datatype.DOUBLE, "4.0"),
                        "x_sqrt_aas", PropertyValue.of(Datatype.DOUBLE, "2.0")),
                Map.of("x_sqrt_aas", PropertyValue.of(Datatype.DOUBLE, "2.0")),
                List.of(ArgumentMapping.builder()
                        .idShort("x_aas")
                        .argumentName("x")
                        .build()),
                List.of(ArgumentMapping.builder()
                        .idShort("x_sqrt_aas")
                        .argumentName("x_sqrt")
                        .build()));
        assertInvokeOperation(nodeIdSqrt,
                false,
                null,
                Map.of("x_aas", PropertyValue.of(Datatype.DOUBLE, "4.0"),
                        "x_sqrt_aas", PropertyValue.of(Datatype.DOUBLE, "4.0")),
                Map.of("x_aas", PropertyValue.of(Datatype.DOUBLE, "4.0"),
                        "x_sqrt_aas", PropertyValue.of(Datatype.DOUBLE, "2.0")),
                Map.of("x_sqrt_aas", PropertyValue.of(Datatype.DOUBLE, "2.0")),
                List.of(ArgumentMapping.builder()
                        .idShort("x_aas")
                        .argumentName("x")
                        .build()),
                List.of(ArgumentMapping.builder()
                        .idShort("x_sqrt_aas")
                        .argumentName("x_sqrt")
                        .build()));
    }

}
