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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
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
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultRange;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TimeSeriesSubmodelTemplateProcessorTest {

    private static final long OPERATION_TIMEOUT = 5000;

    private static final Record record01 = new Record(1660000000, "0", "0.1");
    private static final Record record02 = new Record(1660001000, "0", "0.2");
    // intentionally setting wrong timestamps on segment-level for internalSegment0
    private static final InternalSegment internalSegment0 = new InternalSegment(1668000000L, 1668001000L, record01, record02);

    private static final Record record11 = new Record(1668000000, "11", "1.1");
    private static final Record record12 = new Record(1668001000, "12", "1.2");
    private static final InternalSegment internalSegment1 = new InternalSegment(1668000000L, 1668001000L, record11, record12);

    private static final Record record21 = new Record(1668000000, "21", "2.1");
    private static final Record record22 = new Record(1668001000, "22", "2.2");
    private static final InternalSegment internalSegment2 = new InternalSegment(record21, record22);

    private static final TimeSeries timeSeries = new TimeSeries(
            List.of(new Field("Foo", Datatype.INT),
                    new Field("Bar", Datatype.DOUBLE)),
            internalSegment0,
            internalSegment1,
            internalSegment2);

    private static Submodel submodel = timeSeriesToAas(timeSeries);

    private static AssetAdministrationShellEnvironment environment = new DefaultAssetAdministrationShellEnvironment.Builder()
            .submodels(submodel)
            .build();

    private Service service;

    private static InvokeOperationSyncRequest getReadRecordsOperationRequest(Long start, Long end) {
        return InvokeOperationSyncRequest.builder()
                .submodelId(submodel.getIdentification())
                .path(List.of(new DefaultKey.Builder()
                        .idType(KeyType.ID_SHORT)
                        .type(KeyElements.OPERATION)
                        .value(Constants.READ_RECORDS_ID_SHORT)
                        .build()))
                .timeout(OPERATION_TIMEOUT)
                .inputArgument(new DefaultOperationVariable.Builder()
                        .value(new DefaultRange.Builder()
                                .min(start != null ? Long.toString(start) : null)
                                .max(end != null ? Long.toString(end) : null)
                                .idShort(Constants.READ_RECORDS_INPUT_TIMESPAN_ID_SHORT)
                                .valueType(Datatype.LONG.getName())
                                .build())
                        .build())
                .build();
    }


    private static InvokeOperationSyncRequest getReadSegmentsOperationRequest(Long start, Long end) {
        return InvokeOperationSyncRequest.builder()
                .submodelId(submodel.getIdentification())
                .path(List.of(new DefaultKey.Builder()
                        .idType(KeyType.ID_SHORT)
                        .type(KeyElements.OPERATION)
                        .value(Constants.READ_SEGMENTS_ID_SHORT)
                        .build()))
                .timeout(OPERATION_TIMEOUT)
                .inputArgument(new DefaultOperationVariable.Builder()
                        .value(new DefaultRange.Builder()
                                .min(start != null ? Long.toString(start) : null)
                                .max(end != null ? Long.toString(end) : null)
                                .idShort(Constants.READ_SEGMENTS_INPUT_TIMESPAN_ID_SHORT)
                                .valueType(Datatype.LONG.getName())
                                .build())
                        .build())
                .build();
    }


    private static long getTimeOfNewestRecord() {
        return timeSeries.internalSegments.stream()
                .flatMap(x -> x.records.stream())
                .mapToLong(x -> x.timestamp)
                .max()
                .getAsLong();
    }


    private static long getTimeOfOldestRecord() {
        return timeSeries.internalSegments.stream()
                .flatMap(x -> x.records.stream())
                .mapToLong(x -> x.timestamp)
                .min()
                .getAsLong();
    }


    private static SubmodelElementCollection internalSegmentToAas(InternalSegment segment) {
        DefaultSubmodelElementCollection result = new DefaultSubmodelElementCollection.Builder()
                .idShort(segment.idShort)
                .semanticId(ReferenceHelper.globalReference(Constants.INTERNAL_SEGMENT_SEMANTIC_ID))
                .value(new DefaultSubmodelElementCollection.Builder()
                        .idShort("Records")
                        .values(segment.records.stream()
                                .map(record -> recordToAas(record))
                                .collect(Collectors.toList()))
                        .build())
                .build();
        if (segment.start.isPresent()) {
            result.getValues().add(new DefaultProperty.Builder()
                    .idShort(Constants.SEGMENT_START_TIME_ID_SHORT)
                    .value(Long.toString(segment.start.get()))
                    .valueType(Datatype.LONG.getName())
                    .build());
        }
        if (segment.end.isPresent()) {
            result.getValues().add(new DefaultProperty.Builder()
                    .idShort(Constants.SEGMENT_END_TIME_ID_SHORT)
                    .value(Long.toString(segment.end.get()))
                    .valueType(Datatype.LONG.getName())
                    .build());
        }
        return result;
    }


    private static String randomIdShort(String prefix) {
        return String.format("%s_%s", prefix, UUID.randomUUID().toString().replace("-", ""));
    }


    private static SubmodelElementCollection recordToAas(Record record) {
        return new DefaultSubmodelElementCollection.Builder()
                .idShort(record.idShort)
                .semanticId(ReferenceHelper.globalReference(Constants.RECORD_SEMANTIC_ID))
                .values(record.values.stream()
                        .map(value -> new DefaultProperty.Builder()
                                .idShort(timeSeries.properties.get(record.values.indexOf(value)).name)
                                .valueType(timeSeries.properties.get(record.values.indexOf(value)).datatype.getName())
                                .value(value)
                                .build())
                        .collect(Collectors.toList()))
                .value(new DefaultProperty.Builder()
                        .idShort(Constants.RECORD_TIME_ID_SHORT)
                        .valueType(Datatype.LONG.getName())
                        .value(Long.toString(record.timestamp))
                        .build())
                .build();
    }


    private static Submodel timeSeriesToAas(TimeSeries timeSeries) {
        return new DefaultSubmodel.Builder()
                .idShort("TimeSeries")
                .semanticId(ReferenceHelper.globalReference(Constants.TIMESERIES_SUBMODEL_SEMANTIC_ID))
                .identification(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.IRI)
                        .identifier("urn:aas:id:example:submodel:1")
                        .build())
                .submodelElement(new DefaultSubmodelElementCollection.Builder()
                        .idShort("Metadata")
                        .value(new DefaultSubmodelElementCollection.Builder()
                                .idShort("Record")
                                .values(timeSeries.properties.stream()
                                        .map(x -> new DefaultProperty.Builder()
                                                .idShort(x.name)
                                                .valueType(x.datatype.getName())
                                                .build())
                                        .collect(Collectors.toList()))
                                .build())
                        .build())
                .submodelElement(new DefaultSubmodelElementCollection.Builder()
                        .idShort("Segments")
                        .values(timeSeries.internalSegments.stream()
                                .map(segment -> internalSegmentToAas(segment))
                                .collect(Collectors.toList()))
                        .build())
                .build();
    }


    @After
    public void cleanup() {
        if (service != null) {
            service.stop();
        }
    }


    @Before
    public void init() throws ConfigurationInitializationException, ConfigurationException, AssetConnectionException, MessageBusException, EndpointException {
        service = startNewService(TimeSeriesSubmodelTemplateProcessorConfig.builder()
                .useSegmentTimestamps(true)
                .build());
    }


    @Test
    public void testReadRecords() throws ConfigurationException, AssetConnectionException, MessageBusException, EndpointException {
        // fetch all records
        assertReturnedRecords(null, null, record01, record02, record11, record12, record21, record22);
        // fetch exactly all records
        assertReturnedRecords(getTimeOfOldestRecord(), getTimeOfNewestRecord(), record01, record02, record11, record12, record21, record22);
        // fetch nothing
        assertReturnedRecords(getTimeOfOldestRecord() - 100, getTimeOfOldestRecord() - 1);
        assertReturnedRecords(getTimeOfNewestRecord() + 1, getTimeOfNewestRecord() + 100);
        // fetch partial records
        assertReturnedRecords(record11.timestamp, record11.timestamp, record11, record21);
    }


    @Test
    public void testReadSegments() throws ConfigurationException, AssetConnectionException, MessageBusException, EndpointException {
        Service serviceNotUsingSegmentTimestamps = startNewService(TimeSeriesSubmodelTemplateProcessorConfig.builder()
                .useSegmentTimestamps(false)
                .build());
        // fetch all records
        assertReturnedSegments(null, null, internalSegment0, internalSegment1, internalSegment2);
        assertReturnedSegments(serviceNotUsingSegmentTimestamps, null, null, internalSegment0, internalSegment1, internalSegment2);
        // fetch exactly all records
        assertReturnedSegments(getTimeOfOldestRecord(), getTimeOfNewestRecord(), internalSegment0, internalSegment1, internalSegment2);
        assertReturnedSegments(serviceNotUsingSegmentTimestamps, getTimeOfOldestRecord(), getTimeOfNewestRecord(), internalSegment0, internalSegment1, internalSegment2);
        // fetch nothing
        assertReturnedSegments(getTimeOfOldestRecord() - 100, getTimeOfOldestRecord() - 1);
        assertReturnedSegments(serviceNotUsingSegmentTimestamps, getTimeOfOldestRecord() - 100, getTimeOfOldestRecord() - 1);
        assertReturnedSegments(getTimeOfNewestRecord() + 1, getTimeOfNewestRecord() + 100);
        assertReturnedSegments(serviceNotUsingSegmentTimestamps, getTimeOfNewestRecord() + 1, getTimeOfNewestRecord() + 100);
        // fetch partial records
        assertReturnedSegments(record11.timestamp, record11.timestamp, internalSegment0, internalSegment1, internalSegment2);
        assertReturnedSegments(serviceNotUsingSegmentTimestamps, record11.timestamp, record11.timestamp, internalSegment1, internalSegment2);
        serviceNotUsingSegmentTimestamps.stop();
    }


    private void assertReturnedRecords(Long start, Long end, Record... records) {
        Response response = service.execute(getReadRecordsOperationRequest(start, end));
        SubmodelElement expected = new DefaultSubmodelElementCollection.Builder()
                .idShort(Constants.READ_RECORDS_OUTPUT_RECORDS_ID_SHORT)
                .semanticId(ReferenceHelper.globalReference(Constants.READ_RECORDS_OUTPUT_RECORDS_SEMANTIC_ID))
                .values(Stream.of(records).map(x -> recordToAas(x)).collect(Collectors.toList()))
                .build();
        assertTrue(response.getResult().getSuccess());
        assertEquals(StatusCode.SUCCESS, response.getStatusCode());
        assertThat(response, instanceOf(InvokeOperationSyncResponse.class));
        OperationResult operationResult = ((InvokeOperationSyncResponse) response).getPayload();
        assertEquals(ExecutionState.COMPLETED, operationResult.getExecutionState());
        assertThat(operationResult.getInoutputArguments(), anyOf(nullValue(), empty()));
        assertThat(operationResult.getOutputArguments(), hasSize(1));
        assertThat(operationResult.getOutputArguments().get(0).getValue(), instanceOf(SubmodelElementCollection.class));
        assertThat(((SubmodelElementCollection) operationResult.getOutputArguments().get(0).getValue()).getValues(), hasSize(records.length));
        assertEquals(expected, operationResult.getOutputArguments().get(0).getValue());
    }


    private void assertReturnedSegments(Service service, Long start, Long end, InternalSegment... segments) {
        Response response = service.execute(getReadSegmentsOperationRequest(start, end));
        SubmodelElement expected = new DefaultSubmodelElementCollection.Builder()
                .idShort(Constants.READ_SEGMENTS_OUTPUT_SEGMENTS_ID_SHORT)
                .semanticId(ReferenceHelper.globalReference(Constants.READ_SEGMENTS_OUTPUT_SEGMENTS_SEMANTIC_ID))
                .values(Stream.of(segments).map(x -> internalSegmentToAas(x)).collect(Collectors.toList()))
                .build();
        assertTrue(response.getResult().getSuccess());
        assertEquals(StatusCode.SUCCESS, response.getStatusCode());
        assertThat(response, instanceOf(InvokeOperationSyncResponse.class));
        OperationResult operationResult = ((InvokeOperationSyncResponse) response).getPayload();
        assertEquals(ExecutionState.COMPLETED, operationResult.getExecutionState());
        assertThat(operationResult.getInoutputArguments(), anyOf(nullValue(), empty()));
        assertThat(operationResult.getOutputArguments(), hasSize(1));
        assertThat(operationResult.getOutputArguments().get(0).getValue(), instanceOf(SubmodelElementCollection.class));
        assertThat(((SubmodelElementCollection) operationResult.getOutputArguments().get(0).getValue()).getValues(), hasSize(segments.length));
        assertEquals(expected, operationResult.getOutputArguments().get(0).getValue());
    }


    private void assertReturnedSegments(Long start, Long end, InternalSegment... segments) {
        assertReturnedSegments(service, start, end, segments);
    }


    private Service startNewService(TimeSeriesSubmodelTemplateProcessorConfig config)
            throws ConfigurationInitializationException, ConfigurationException, AssetConnectionException, MessageBusException, EndpointException {
        Persistence persistence = mock(Persistence.class);
        when(persistence.get(isNull(), (Reference) isNull(), any()))
                .thenReturn(environment.getSubmodels());
        MessageBus messageBus = mock(MessageBus.class);
        TimeSeriesSubmodelTemplateProcessor timeSeriesSubmodelTemplateProcessor = new TimeSeriesSubmodelTemplateProcessor();
        timeSeriesSubmodelTemplateProcessor.init(CoreConfig.DEFAULT, config, null);
        Service result = new Service(CoreConfig.DEFAULT, persistence, messageBus, null, null, List.of(timeSeriesSubmodelTemplateProcessor));
        result.start();
        return result;
    }

    private static class Field {

        private final String name;
        private final Datatype datatype;

        public Field(String name, Datatype datatype) {
            this.name = name;
            this.datatype = datatype;
        }
    }

    private static class InternalSegment {

        private final String idShort;
        private final List<Record> records;
        private final Optional<Long> start;
        private final Optional<Long> end;

        public InternalSegment(Record... records) {
            this.idShort = randomIdShort("InternalSegment");
            this.records = Arrays.asList(records);
            this.start = Optional.empty();
            this.end = Optional.empty();
        }


        public InternalSegment(Long start, Long end, Record... records) {
            this.idShort = randomIdShort("InternalSegment");
            this.start = Optional.ofNullable(start);
            this.end = Optional.ofNullable(end);
            this.records = Arrays.asList(records);
        }
    }

    private static class Record {

        private final String idShort;
        private final long timestamp;
        private final List<String> values;

        public Record(long timestamp, String... values) {
            this.idShort = randomIdShort("Record");
            this.timestamp = timestamp;
            this.values = Arrays.asList(values);
        }


        public Record(int timestamp, String... values) {
            this((long) timestamp, values);
        }
    }

    private static class TimeSeries {

        private final List<Field> properties;
        private final List<InternalSegment> internalSegments;

        public TimeSeries(List<Field> properties, InternalSegment... internalSegments) {
            this.properties = properties;
            this.internalSegments = Arrays.asList(internalSegments);
        }
    }
}
