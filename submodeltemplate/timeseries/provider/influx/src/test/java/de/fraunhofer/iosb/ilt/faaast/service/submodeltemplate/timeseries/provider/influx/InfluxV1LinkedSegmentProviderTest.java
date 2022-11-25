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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EndpointException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.ExecutionState;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.InvokeOperationSyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.AssetAdministrationShellElementWalker;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.DefaultAssetAdministrationShellElementVisitor;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.TimeSeriesSubmodelTemplateProcessor;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.TimeSeriesSubmodelTemplateProcessorConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.LinkedSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.TimeSeries;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import io.adminshell.aas.v3.model.impl.DefaultRange;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import io.apisense.embed.influx.InfluxServer;
import io.apisense.embed.influx.ServerAlreadyRunningException;
import io.apisense.embed.influx.configuration.InfluxConfigurationWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class InfluxV1LinkedSegmentProviderTest {

    private static final long OPERATION_TIMEOUT = 500000;
    private static InfluxServer influxServer;
    private static String influxEndpoint;
    private static final String INFLUX_BUCKET = "TestBucket";
    private static final String INFLUX_MEASUREMENT = "TestMeasurement";

    private static final LinkedSegment linkedSegment = LinkedSegment.builder()
            .semanticId(ReferenceHelper.globalReference(Constants.LINKED_SEGMENT_SEMANTIC_ID))
            .endpoint("http://localhost:8086")
            .query("SELECT * FROM " + INFLUX_MEASUREMENT)
            .build();

    private static final List<Record> records = List.of(
            TimeSeriesData.RECORD_01,
            TimeSeriesData.RECORD_02);

    private static final TimeSeries timeSeries = TimeSeries.builder()
            .identification(new DefaultIdentifier.Builder()
                    .idType(IdentifierType.IRI)
                    .identifier(IdentifierHelper.randomId("TimeSeries"))
                    .build())
            .metadata(TimeSeriesData.METADATA)
            .segment(linkedSegment)
            .build();

    private static final AssetAdministrationShellEnvironment environment = new DefaultAssetAdministrationShellEnvironment.Builder()
            .submodels(timeSeries)
            .build();

    // change all idShort of Record to null
    private static final AssetAdministrationShellElementWalker removeRecordIdShortWalker = AssetAdministrationShellElementWalker.builder()
            .visitor(new DefaultAssetAdministrationShellElementVisitor() {
                @Override
                public void visit(SubmodelElementCollection smc) {
                    if (smc != null && Objects.equals(ReferenceHelper.globalReference(Constants.RECORD_SEMANTIC_ID), smc.getSemanticId())) {
                        smc.setIdShort(null);
                    }
                }
            }).build();

    private Service service;

    @BeforeClass
    public static void setup() throws IOException, ServerAlreadyRunningException {
        int influxPort = 8086;//Network.getFreeServerPort();

        // configuration to start InfluxDB server with HTTP on port `freeHttpPort`
        // and default backup restore port
        InfluxConfigurationWriter influxConfig = new InfluxConfigurationWriter.Builder()
                .setHttp(influxPort) // by default auth is disabled
                .build();
        influxEndpoint = "http://localhost:" + influxPort;
        influxServer = new InfluxServer.Builder()
                .setInfluxConfiguration(influxConfig)
                .build();
        influxServer.start();

        InfluxDB influxDB = InfluxDBFactory.connect(influxEndpoint);
        influxDB.query(new Query("DROP DATABASE " + INFLUX_BUCKET));
        influxDB.query(new Query("CREATE DATABASE " + INFLUX_BUCKET));
        influxDB.setDatabase(INFLUX_BUCKET);
        String retentionPolicy = "forever";
        influxDB.query(new Query("CREATE RETENTION POLICY " + retentionPolicy + " ON " + INFLUX_BUCKET + " DURATION INF REPLICATION 1 DEFAULT"));
        influxDB.setRetentionPolicy(retentionPolicy);
        records.forEach(x -> influxDB.write(TimeSeriesData.toInfluxPoint(x, INFLUX_MEASUREMENT)));
    }


    private static InvokeOperationSyncRequest getReadRecordsOperationRequest(ZonedDateTime start, ZonedDateTime end) {
        return InvokeOperationSyncRequest.builder()
                .submodelId(timeSeries.getIdentification())
                .path(List.of(new DefaultKey.Builder()
                        .idType(KeyType.ID_SHORT)
                        .type(KeyElements.OPERATION)
                        .value(Constants.READ_RECORDS_ID_SHORT)
                        .build()))
                .timeout(OPERATION_TIMEOUT)
                .inputArgument(new DefaultOperationVariable.Builder()
                        .value(new DefaultRange.Builder()
                                .min(start != null ? start.toString() : null)
                                .max(end != null ? end.toString() : null)
                                .idShort(Constants.READ_RECORDS_INPUT_TIMESPAN_ID_SHORT)
                                .valueType(Datatype.DATE_TIME.getName())
                                .build())
                        .build())
                .build();
    }


    //    private static InvokeOperationSyncRequest getReadSegmentsOperationRequest(Long start, Long end) {
    //        return InvokeOperationSyncRequest.builder()
    //                .submodelId(timeSeries.getIdentification())
    //                .path(List.of(new DefaultKey.Builder()
    //                        .idType(KeyType.ID_SHORT)
    //                        .type(KeyElements.OPERATION)
    //                        .value(Constants.READ_SEGMENTS_ID_SHORT)
    //                        .build()))
    //                .timeout(OPERATION_TIMEOUT)
    //                .inputArgument(new DefaultOperationVariable.Builder()
    //                        .value(new DefaultRange.Builder()
    //                                .min(start != null ? Long.toString(start) : null)
    //                                .max(end != null ? Long.toString(end) : null)
    //                                .idShort(Constants.READ_SEGMENTS_INPUT_TIMESPAN_ID_SHORT)
    //                                .valueType(Datatype.LONG.getName())
    //                                .build())
    //                        .build())
    //                .build();
    //    }
    private Service startNewService(TimeSeriesSubmodelTemplateProcessorConfig config)
            throws ConfigurationInitializationException, ConfigurationException, AssetConnectionException, MessageBusException, EndpointException {
        Persistence persistence = mock(Persistence.class);
        when(persistence.get(isNull(), (Reference) isNull(), any()))
                .thenReturn(environment.getSubmodels());
        when(persistence.getEnvironment()).thenReturn(environment);
        MessageBus messageBus = mock(MessageBus.class);
        TimeSeriesSubmodelTemplateProcessor timeSeriesSubmodelTemplateProcessor = new TimeSeriesSubmodelTemplateProcessor();
        timeSeriesSubmodelTemplateProcessor.init(CoreConfig.DEFAULT, config, null);
        Service result = new Service(CoreConfig.DEFAULT, persistence, messageBus, null, null, List.of(timeSeriesSubmodelTemplateProcessor));
        result.start();
        return result;
    }


    @After
    public void cleanup() {
        if (service != null) {
            service.stop();
        }
    }


    @Before
    public void init() throws ConfigurationInitializationException, ConfigurationException, AssetConnectionException, MessageBusException, EndpointException, IOException,
            ServerAlreadyRunningException {
        service = startNewService(TimeSeriesSubmodelTemplateProcessorConfig.builder()
                .useSegmentTimestamps(true)
                .linkedSegmentProvider(InfluxV1LinkedSegmentProviderConfig.builder()
                        .endpoint(influxEndpoint)
                        .database(INFLUX_BUCKET)
                        .build())
                .build());
    }


    @Test
    public void testReadRecords() throws ConfigurationException, AssetConnectionException, MessageBusException, EndpointException {
        // fetch all records
        assertReturnedRecords(null, null, records);
    }


    private void assertReturnedRecords(ZonedDateTime start, ZonedDateTime end, Record... records) {
        assertReturnedRecords(start, end, Arrays.asList(records));
    }


    private void assertReturnedRecords(ZonedDateTime start, ZonedDateTime end, List<Record> records) {
        Response response = service.execute(getReadRecordsOperationRequest(start, end));
        SubmodelElement expected = new DefaultSubmodelElementCollection.Builder()
                .idShort(Constants.READ_RECORDS_OUTPUT_RECORDS_ID_SHORT)
                .semanticId(ReferenceHelper.globalReference(Constants.READ_RECORDS_OUTPUT_RECORDS_SEMANTIC_ID))
                .values(records.stream().map(x -> (SubmodelElementCollection) x).collect(Collectors.toList()))
                .build();
        assertTrue(response.getResult().getSuccess());
        assertEquals(StatusCode.SUCCESS, response.getStatusCode());
        assertThat(response, instanceOf(InvokeOperationSyncResponse.class));
        OperationResult operationResult = ((InvokeOperationSyncResponse) response).getPayload();
        assertEquals(ExecutionState.COMPLETED, operationResult.getExecutionState());
        assertThat(operationResult.getInoutputArguments(), anyOf(nullValue(), empty()));
        assertThat(operationResult.getOutputArguments(), hasSize(1));
        assertThat(operationResult.getOutputArguments().get(0).getValue(), instanceOf(SubmodelElementCollection.class));
        assertThat(((SubmodelElementCollection) operationResult.getOutputArguments().get(0).getValue()).getValues(), hasSize(records.size()));
        // cannot be equal because idShort is generated randomly
        // assertEquals(expected, operationResult.getOutputArguments().get(0).getValue());
        assertEqualsIgnoringRecordIdShort(expected, operationResult.getOutputArguments().get(0).getValue());
    }


    private static void assertEqualsIgnoringRecordIdShort(SubmodelElement expected, SubmodelElement actual) {
        SubmodelElement expectedCopy = DeepCopyHelper.deepCopy(expected, expected.getClass());
        SubmodelElement actualCopy = DeepCopyHelper.deepCopy(actual, expected.getClass());
        removeRecordIdShortWalker.walk(expectedCopy);
        removeRecordIdShortWalker.walk(actualCopy);
        assertEquals(expectedCopy, actualCopy);
    }

}
