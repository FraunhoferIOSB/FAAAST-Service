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

import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.Reference;
import java.util.List;


/**
 * Constants related to SMT TimeSeries.
 */
public class Constants {

    // SMT types & properties
    public static final String TIMESERIES_SUBMODEL_ID_SHORT = "TimeSeries";
    public static final String TIMESERIES_SUBMODEL_SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/1/1";
    public static final String SEGMENTS_SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/Segments/1/1";
    public static final String TIMESERIES_SEGMENTS_ID_SHORT = "Segments";
    public static final String TIMESERIES_METADATA_ID_SHORT = "Metadata";
    public static final String METADATA_SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/Metadata/1/1";
    public static final String METADATA_RECORD_METADATA_ID_SHORT = "RecordMetadata";
    public static final String SEGMENT_RECORD_COUNT_ID_SHORT = "RecordCount";
    public static final String SEGMENT_START_TIME_ID_SHORT = "StartTime";
    public static final String SEGMENT_START_TIME_SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/Segment/StartTime/1/1";
    public static final String SEGMENT_END_TIME_ID_SHORT = "EndTime";
    public static final String SEGMENT_END_TIME_SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/Segment/EndTime/1/1";
    public static final String SEGMENT_SAMPLING_INTERVAL_ID_SHORT = "SamplingInterval";
    public static final String SEGMENT_SAMPLING_RATE_ID_SHORT = "SamplingRate";
    public static final String SEGMENT_DURATION_ID_SHORT = "Duration";
    public static final String SEGMENT_DURATION_SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/Segment/Duration/1/1 ";
    public static final String SEGMENT_STATE_ID_SHORT = "State";
    public static final String SEGMENT_STATE_SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/Segment/State/1/1";
    public static final String SEGMENT_STATE_COMPLETED_SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/Segment/State/Completed/1/1";
    public static final String SEGMENT_STATE_IN_PROGRESS_SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/Segment/State/InProgress/1/1";
    public static final String SEGMENT_LAST_UPDATE_ID_SHORT = "LastUpdate";
    public static final String EXTERNAL_SEGMENT_SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/Segments/ExternalSegment/1/1";
    public static final String FILE_SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/File/1/1";
    public static final String BLOB_SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/Blob/1/1";
    public static final String LINKED_SEGMENT_SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/Segments/LinkedSegment/1/1";
    public static final String LINKED_SEGMENT_ENDPOINT_ID_SHORT = "Endpoint";
    public static final String LINKED_SEGMENT_QUERY_ID_SHORT = "Query";
    public static final String INTERNAL_SEGMENT_SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/Segments/InternalSegment/1/1";
    public static final String INTERNAL_SEGMENT_RECORDS_ID_SHORT = "Records";
    public static final String RECORD_TIME_ID_SHORT = "Time00";
    public static final String RECORD_SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/Record/1/1";
    // Operation: ReadRecords
    public static final String READ_RECORDS_ID_SHORT = "ReadRecords";
    public static final String READ_RECORDS_SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/ReadRecords/1/1";
    public static final String READ_RECORDS_INPUT_TIMESPAN_ID_SHORT = "Timespan";
    public static final String READ_RECORDS_OUTPUT_RECORDS_ID_SHORT = "Records";
    public static final String READ_RECORDS_OUTPUT_RECORDS_SEMANTIC_ID = " https://admin-shell.io/idta/TimeSeries/Records/1/1";
    // Operation: ReadSegments
    public static final String READ_SEGMENTS_ID_SHORT = "ReadSegments";
    public static final String READ_SEGMENTS_SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/ReadSegments/1/1";
    public static final String READ_SEGMENTS_INPUT_TIMESPAN_ID_SHORT = "Timespan";
    public static final String READ_SEGMENTS_OUTPUT_SEGMENTS_ID_SHORT = "Segments";
    public static final String READ_SEGMENTS_OUTPUT_SEGMENTS_SEMANTIC_ID = " https://admin-shell.io/idta/TimeSeries/Segments/1/1";
    // Time
    public static final String TIME_UTC = "https://admin-shell.io/idta/TimeSeries/UtcTime/1/1";
    public static final String TIME_TAI = "https://admin-shell.io/idta/TimeSeries/TaiTime/1/1";
    public static final String TIME_RELATIVE_POINT_IN_TIME = "https://admin-shell.io/idta/TimeSeries/RelativePointInTime/1/1";
    public static final String TIME_RELATIVE_DURATION = "https://admin-shell.io/idta/TimeSeries/RelativeTimeDuration/1/1";
    public static final String TIME_UNIX = "https://admin-shell.io/idta/TimeSeries/UnixTime/1/1"; //TODO: find semantic id for unix timestamp
    // Time Unit
    public static final String TIMEUNIT_MILLISECOND = "millisecond";
    public static final String TIMEUNIT_SECOND = "second";
    public static final String TIMEUNIT_MINUTE = "minute";
    public static final String TIMEUNIT_HERTZ = "hertz";

    public static final List<Reference> SEGMENTS_SEMANTIC_IDS = List.of(
            ReferenceHelper.globalReference(INTERNAL_SEGMENT_SEMANTIC_ID),
            ReferenceHelper.globalReference(LINKED_SEGMENT_SEMANTIC_ID),
            ReferenceHelper.globalReference(EXTERNAL_SEGMENT_SEMANTIC_ID));

    private Constants() {}
}
