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

/**
 * Constants related to SMT TimeSeries.
 */
public class Constants {

    // SMT types & properties
    public static final String TIMESERIES_SUBMODEL_ID_SHORT = "TimeSeries";
    public static final String TIMESERIES_SUBMODEL_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/1/1/Submodel";
    public static final String SEGMENTS_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/Segments/1/1";
    public static final String TIMESERIES_SEGMENTS_ID_SHORT = "Segments";
    public static final String TIMESERIES_METADATA_ID_SHORT = "Metadata";
    public static final String METADATA_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/Metadata/1/";
    public static final String METADATA_RECORD_METADATA_ID_SHORT = "RecordMetadata";
    public static final String SEGMENT_RECORD_COUNT_ID_SHORT = "RecordCount";
    public static final String SEGMENT_START_TIME_ID_SHORT = "StartTime";
    public static final String SEGMENT_END_TIME_ID_SHORT = "EndTime";
    public static final String SEGMENT_SAMPLING_INTERVAL_ID_SHORT = "SamplingInterval";
    public static final String SEGMENT_SAMPLING_RATE_ID_SHORT = "SamplingRate";
    public static final String SEGMENT_KIND_ID_SHORT = "Kind";
    public static final String EXTERNAL_SEGMENT_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/Segments/ExternalSegment/1/1";
    public static final String LINKED_SEGMENT_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/Segments/LinkedSegment/1/1";
    public static final String LINKED_SEGMENT_ENDPOINT_ID_SHORT = "Endpoint";
    public static final String LINKED_SEGMENT_QUERY_ID_SHORT = "Query";
    public static final String INTERNAL_SEGMENT_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/Segments/InternalSegment/1/1";
    public static final String INTERNAL_SEGMENT_RECORDS_ID_SHORT = "Records";
    public static final String RECORD_TIME_ID_SHORT = "Time";
    public static final String RECORD_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/Record/1/1";
    // Operation: ReadRecords
    public static final String READ_RECORDS_ID_SHORT = "ReadRecords";
    public static final String READ_RECORDS_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/ReadRecords";
    public static final String READ_RECORDS_INPUT_TIMESPAN_ID_SHORT = "Timespan";
    public static final String READ_RECORDS_OUTPUT_RECORDS_ID_SHORT = "Records";
    public static final String READ_RECORDS_OUTPUT_RECORDS_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/TimeSeries/ReadRecords/Records";
    // Operation: ReadSegments
    public static final String READ_SEGMENTS_ID_SHORT = "ReadSegments";
    public static final String READ_SEGMENTS_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/ReadSegments";
    public static final String READ_SEGMENTS_INPUT_TIMESPAN_ID_SHORT = "Timespan";
    public static final String READ_SEGMENTS_OUTPUT_SEGMENTS_ID_SHORT = "Segments";
    public static final String READ_SEGMENTS_OUTPUT_SEGMENTS_SEMANTIC_ID = "https://admin-shell.io/idta/timeseries/TimeSeries /ReadSegments/Segments/1/1";
    // Time
    public static final String TIME_UTC = "https://admin-shell.io/idta/timeseries/UtcTime/1/1";
    // Time Unit
    public static final String TIMEUNIT_MILLISECOND = "millisecond";
    public static final String TIMEUNIT_SECOND = "second";
    public static final String TIMEUNIT_MINUTE = "minute";
    public static final String TIMEUNIT_HERTZ = "hertz";

    private Constants() {}
}
