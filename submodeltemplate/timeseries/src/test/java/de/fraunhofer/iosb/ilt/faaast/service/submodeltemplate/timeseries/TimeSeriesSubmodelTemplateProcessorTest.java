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
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.ExternalSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.InternalSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.LinkedSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.TimeSeries;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.TimeFactory;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.DummyExternalSegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.DummyExternalSegmentProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.DummyInternalSegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.DummyInternalSegmentProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.DummyLinkedSegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.DummyLinkedSegmentProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.File;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.impl.DefaultFile;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import io.adminshell.aas.v3.model.impl.DefaultRange;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TimeSeriesSubmodelTemplateProcessorTest {

    private static final long OPERATION_TIMEOUT = 500000;

    public static final InternalSegment INTERNAL_SEGMENT = InternalSegment.builder()
            .dontCalculateProperties()
            .start(TimeFactory.getTimeFrom(TimeSeriesData.RECORD_00, null, null, null).getStart().get())
            .end(TimeFactory.getTimeFrom(TimeSeriesData.RECORD_05, null, null, null).getEnd().get())
            .record(TimeSeriesData.RECORD_00)
            .record(TimeSeriesData.RECORD_01)
            .record(TimeSeriesData.RECORD_02)
            .record(TimeSeriesData.RECORD_03)
            .record(TimeSeriesData.RECORD_04)
            .record(TimeSeriesData.RECORD_05)
            .build();

    public static final InternalSegment INTERNAL_SEGMENT_WITHOUT_TIMES = InternalSegment.builder()
            .dontCalculateProperties()
            .record(TimeSeriesData.RECORD_06)
            .record(TimeSeriesData.RECORD_07)
            .build();

    public static final InternalSegment INTERNAL_SEGMENT_WITH_WRONG_TIMES = InternalSegment.builder()
            .dontCalculateProperties()
            .start(TimeFactory.getTimeFrom(TimeSeriesData.RECORD_00, null, null, null).getStart().get())
            .end(TimeFactory.getTimeFrom(TimeSeriesData.RECORD_05, null, null, null).getEnd().get())
            .record(TimeSeriesData.RECORD_08)
            .record(TimeSeriesData.RECORD_09)
            .build();

    public static final InternalSegment INTERNAL_SEGMENT_WITH_OTHER_TIMESTAMPS = InternalSegment.builder()
            .dontCalculateProperties()
            .start(TimeFactory.getTimeFrom(TimeSeriesData.RECORD_05, null, null, null).getStart().get())
            .end(TimeFactory.getTimeFrom(TimeSeriesData.RECORD_05, null, null, null).getEnd().get())
            .record(TimeSeriesData.RECORD_10)
            .record(TimeSeriesData.RECORD_11)
            .build();

    public static final TimeSeries TIME_SERIES = TimeSeries.builder()
            .identification(new DefaultIdentifier.Builder()
                    .idType(IdentifierType.IRI)
                    .identifier(IdentifierHelper.randomId("TimeSeries"))
                    .build())
            .metadata(TimeSeriesData.METADATA)
            .segment(INTERNAL_SEGMENT)
            .segment(INTERNAL_SEGMENT_WITHOUT_TIMES)
            .segment(INTERNAL_SEGMENT_WITH_WRONG_TIMES)
            .segment(INTERNAL_SEGMENT_WITH_OTHER_TIMESTAMPS)
            .build();
    public static final AssetAdministrationShellEnvironment ENVIRONMENT = new DefaultAssetAdministrationShellEnvironment.Builder()
            .submodels(TIME_SERIES)
            .build();

    private Service serviceUsingSegmentTimestamps;
    private Service serviceNotUsingSegmentTimestamps;

    private static InvokeOperationSyncRequest getReadRecordsOperationRequest(TimeSeries timeSeries, ZonedDateTime start, ZonedDateTime end) {
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


    private static InvokeOperationSyncRequest getReadSegmentsOperationRequest(TimeSeries timeSeries, ZonedDateTime start, ZonedDateTime end) {
        return InvokeOperationSyncRequest.builder()
                .submodelId(timeSeries.getIdentification())
                .path(List.of(new DefaultKey.Builder()
                        .idType(KeyType.ID_SHORT)
                        .type(KeyElements.OPERATION)
                        .value(Constants.READ_SEGMENTS_ID_SHORT)
                        .build()))
                .timeout(OPERATION_TIMEOUT)
                .inputArgument(new DefaultOperationVariable.Builder()
                        .value(new DefaultRange.Builder()
                                .min(start != null ? start.toString() : null)
                                .max(end != null ? end.toString() : null)
                                .idShort(Constants.READ_SEGMENTS_INPUT_TIMESPAN_ID_SHORT)
                                .valueType(Datatype.DATE_TIME.getName())
                                .build())
                        .build())
                .build();
    }


    @After
    public void cleanup() {
        serviceUsingSegmentTimestamps.stop();
        serviceNotUsingSegmentTimestamps.stop();
    }


    @Before
    public void init() throws ConfigurationInitializationException, ConfigurationException, AssetConnectionException, MessageBusException, EndpointException {
        serviceUsingSegmentTimestamps = startNewService(
                ENVIRONMENT,
                TimeSeriesSubmodelTemplateProcessorConfig.builder()
                        .useSegmentTimestamps(true)
                        .build());
        serviceNotUsingSegmentTimestamps = startNewService(
                ENVIRONMENT,
                TimeSeriesSubmodelTemplateProcessorConfig.builder()
                        .useSegmentTimestamps(false)
                        .build());
    }


    @Test
    public void testDummyInternalSegmentProvider()
            throws ConfigurationException, ConfigurationInitializationException, AssetConnectionException, EndpointException, MessageBusException {
        TimeSeries timeSeries = TimeSeries.builder()
                .identification(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.IRI)
                        .identifier(IdentifierHelper.randomId("TimeSeries"))
                        .build())
                .metadata(TimeSeriesData.METADATA)
                .segment(InternalSegment.builder()
                        .dontCalculateProperties()
                        .build())
                .build();
        Service service = startNewService(
                new DefaultAssetAdministrationShellEnvironment.Builder()
                        .submodels(timeSeries)
                        .build(),
                TimeSeriesSubmodelTemplateProcessorConfig.builder()
                        .useSegmentTimestamps(false)
                        .internalSegmentProvider(new DummyInternalSegmentProviderConfig())
                        .build());
        assertReturnedRecords(service, timeSeries,
                null,
                null,
                DummyInternalSegmentProvider.RECORDS);
        service.stop();
    }


    @Test
    public void testDummyLinkedSegmentProvider()
            throws ConfigurationException, ConfigurationInitializationException, AssetConnectionException, EndpointException, MessageBusException {
        String endpoint = "http://example.org";
        TimeSeries timeSeries = TimeSeries.builder()
                .identification(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.IRI)
                        .identifier(IdentifierHelper.randomId("TimeSeries"))
                        .build())
                .metadata(TimeSeriesData.METADATA)
                .segment(LinkedSegment.builder()
                        .endpoint(endpoint)
                        .build())
                .build();
        Service service = startNewService(
                new DefaultAssetAdministrationShellEnvironment.Builder()
                        .submodels(timeSeries)
                        .build(),
                TimeSeriesSubmodelTemplateProcessorConfig.builder()
                        .useSegmentTimestamps(false)
                        .linkedSegmentProvider(DummyLinkedSegmentProviderConfig.builder()
                                .endpoint(endpoint)
                                .build())
                        .build());
        assertReturnedRecords(service, timeSeries,
                null,
                null,
                DummyLinkedSegmentProvider.RECORDS);
        service.stop();
    }


    @Test
    public void testDummyExternalSegmentProvider()
            throws ConfigurationException, ConfigurationInitializationException, AssetConnectionException, EndpointException, MessageBusException {
        File defaultFile = new DefaultFile();
        String segmentShortID = IdentifierHelper.randomId("ExternalSegment");
        TimeSeries timeSeries = TimeSeries.builder()
                .identification(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.IRI)
                        .identifier(IdentifierHelper.randomId("TimeSeries"))
                        .build())
                .metadata(TimeSeriesData.METADATA)
                .segment(ExternalSegment.builder()
                        .data(defaultFile)
                        .idShort(segmentShortID)
                        .build())
                .build();
        Service service = startNewService(
                new DefaultAssetAdministrationShellEnvironment.Builder()
                        .submodels(timeSeries)
                        .build(),
                TimeSeriesSubmodelTemplateProcessorConfig.builder()
                        .useSegmentTimestamps(false)
                        .externalSegmentProvider(DummyExternalSegmentProviderConfig.builder().segmentShortID(segmentShortID)
                                .build())
                        .build());
        assertReturnedRecords(service, timeSeries,
                null,
                null,
                DummyExternalSegmentProvider.RECORDS);
        service.stop();
    }


    @Test
    public void testReadRecords() throws ConfigurationException, AssetConnectionException, MessageBusException, EndpointException {
        // fetch all records
        assertReturnedRecords(serviceUsingSegmentTimestamps, TIME_SERIES,
                null,
                null,
                TimeSeriesData.RECORDS);
        // fetch exactly all records
        assertReturnedRecords(serviceUsingSegmentTimestamps, TIME_SERIES,
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_00, null, null, null).getStart().get(),
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_09, null, null, null).getEnd().get(),
                TimeSeriesData.RECORDS);
        // fetch nothing
        assertReturnedRecords(serviceUsingSegmentTimestamps, TIME_SERIES,
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_00, null, null, null).getStart().get().minusHours(1),
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_00, null, null, null).getStart().get().minusMinutes(1));

        assertReturnedRecords(serviceUsingSegmentTimestamps, TIME_SERIES,
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_09, null, null, null).getEnd().get().plusMinutes(1),
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_09, null, null, null).getStart().get().plusHours(1));
        // fetch records from only one segment        
        assertReturnedRecords(serviceUsingSegmentTimestamps, TIME_SERIES,
                TimeFactory.getTimeFrom(INTERNAL_SEGMENT.getRecords().get(0), null, null, null).getStart().get(),
                TimeFactory.getTimeFrom(INTERNAL_SEGMENT.getRecords().get(INTERNAL_SEGMENT.getRecords().size() - 1), null, null, null).getEnd().get(),
                INTERNAL_SEGMENT.getRecords());
        // fetch partialy records from multiple segment
        assertReturnedRecords(serviceUsingSegmentTimestamps, TIME_SERIES,
                TimeFactory.getTimeFrom(INTERNAL_SEGMENT.getRecords().get(INTERNAL_SEGMENT.getRecords().size() - 1), null, null, null).getStart().get(),
                TimeFactory.getTimeFrom(INTERNAL_SEGMENT_WITHOUT_TIMES.getRecords().get(0), null, null, null).getEnd().get(),
                List.of(TimeSeriesData.RECORD_05, TimeSeriesData.RECORD_06, TimeSeriesData.RECORD_10));
    }


    @Test
    public void testReadSegments() throws ConfigurationException, AssetConnectionException, MessageBusException, EndpointException {
        // fetch all records
        assertReturnedSegments(serviceUsingSegmentTimestamps, TIME_SERIES,
                null,
                null,
                INTERNAL_SEGMENT,
                INTERNAL_SEGMENT_WITHOUT_TIMES,
                INTERNAL_SEGMENT_WITH_WRONG_TIMES,
                INTERNAL_SEGMENT_WITH_OTHER_TIMESTAMPS);
        assertReturnedSegments(serviceNotUsingSegmentTimestamps, TIME_SERIES,
                null, null,
                INTERNAL_SEGMENT,
                INTERNAL_SEGMENT_WITHOUT_TIMES,
                INTERNAL_SEGMENT_WITH_WRONG_TIMES,
                INTERNAL_SEGMENT_WITH_OTHER_TIMESTAMPS);
        // fetch exactly all records
        assertReturnedSegments(serviceUsingSegmentTimestamps, TIME_SERIES,
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_00, null, null, null).getStart().get(),
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_09, null, null, null).getEnd().get(),
                INTERNAL_SEGMENT,
                INTERNAL_SEGMENT_WITHOUT_TIMES,
                INTERNAL_SEGMENT_WITH_WRONG_TIMES,
                INTERNAL_SEGMENT_WITH_OTHER_TIMESTAMPS);
        assertReturnedSegments(serviceNotUsingSegmentTimestamps, TIME_SERIES,
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_00, null, null, null).getStart().get(),
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_09, null, null, null).getEnd().get(),
                INTERNAL_SEGMENT,
                INTERNAL_SEGMENT_WITHOUT_TIMES,
                INTERNAL_SEGMENT_WITH_WRONG_TIMES,
                INTERNAL_SEGMENT_WITH_OTHER_TIMESTAMPS);
        // fetch nothing
        assertReturnedSegments(serviceUsingSegmentTimestamps, TIME_SERIES,
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_00, null, null, null).getStart().get().minusHours(1),
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_00, null, null, null).getStart().get().minusMinutes(1));
        assertReturnedSegments(serviceNotUsingSegmentTimestamps, TIME_SERIES,
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_00, null, null, null).getStart().get().minusHours(1),
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_00, null, null, null).getStart().get().minusMinutes(1));

        assertReturnedSegments(serviceUsingSegmentTimestamps, TIME_SERIES,
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_09, null, null, null).getEnd().get().plusMinutes(1),
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_09, null, null, null).getEnd().get().plusHours(1));
        assertReturnedSegments(serviceNotUsingSegmentTimestamps, TIME_SERIES,
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_09, null, null, null).getEnd().get().plusMinutes(1),
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_09, null, null, null).getEnd().get().plusHours(1));
        // fetch partial 
        assertReturnedSegments(serviceUsingSegmentTimestamps, TIME_SERIES,
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_00, null, null, null).getStart().get(),
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_05, null, null, null).getEnd().get(),
                INTERNAL_SEGMENT,
                INTERNAL_SEGMENT_WITH_WRONG_TIMES,
                INTERNAL_SEGMENT_WITH_OTHER_TIMESTAMPS);
        assertReturnedSegments(serviceNotUsingSegmentTimestamps, TIME_SERIES,
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_00, null, null, null).getStart().get(),
                TimeFactory.getTimeFrom(TimeSeriesData.RECORD_05, null, null, null).getEnd().get(),
                INTERNAL_SEGMENT);
    }


    private void assertReturnedRecords(Service service, TimeSeries timeSeries, ZonedDateTime start, ZonedDateTime end, Record... records) {
        assertReturnedRecords(service, timeSeries, start, end, Arrays.asList(records));
    }


    private void assertReturnedRecords(Service service, TimeSeries timeSeries, ZonedDateTime start, ZonedDateTime end, List<Record> records) {
        Response response = service.execute(getReadRecordsOperationRequest(timeSeries, start, end));
        SubmodelElement expected = new DefaultSubmodelElementCollection.Builder()
                .idShort(Constants.READ_RECORDS_OUTPUT_RECORDS_ID_SHORT)
                .semanticId(ReferenceHelper.globalReference(Constants.READ_RECORDS_OUTPUT_RECORDS_SEMANTIC_ID))
                .values(records.stream().map(SubmodelElement.class::cast).collect(Collectors.toList()))
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
        assertEquals(expected, operationResult.getOutputArguments().get(0).getValue());
    }


    private void assertReturnedSegments(Service service, TimeSeries timeSeries, ZonedDateTime start, ZonedDateTime end, InternalSegment... segments) {
        Response response = service.execute(getReadSegmentsOperationRequest(timeSeries, start, end));
        SubmodelElement expected = new DefaultSubmodelElementCollection.Builder()
                .idShort(Constants.READ_SEGMENTS_OUTPUT_SEGMENTS_ID_SHORT)
                .semanticId(ReferenceHelper.globalReference(Constants.READ_SEGMENTS_OUTPUT_SEGMENTS_SEMANTIC_ID))
                .values(Arrays.asList(segments))
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


    private Service startNewService(AssetAdministrationShellEnvironment environment, TimeSeriesSubmodelTemplateProcessorConfig config)
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
}
