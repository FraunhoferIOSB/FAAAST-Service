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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider;

import de.fraunhofer.iosb.ilt.faaast.service.config.Configurable;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Metadata;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Segment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Timespan;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.TimeType;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * Interface for datasource-specific implementation for segment providers.
 *
 * @param <T> type of support segment
 * @param <C> type of matching configuration
 */
public interface SegmentProvider<T extends Segment, C extends SegmentProviderConfig> extends Configurable<C> {

    /**
     * Reads all records of a segment from underlying datasource.
     *
     * @param metadata the metadata of the time series
     * @param segment the segment to read from
     * @return list of all records of the segment
     * @throws de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.SegmentProviderException if
     *             fetching the data fails
     */
    public List<Record> getRecords(Metadata metadata, T segment) throws SegmentProviderException;


    /**
     * Reads records of a segment from underlying datasource filtered by time. If start or end is null, this means there
     * is no restriction on start/end, e.g.if only start is defined, all records with a time >= start should be
     * returned.
     *
     * @param metadata the metadata of the time series
     * @param segment the segment to read from
     * @param timespan the timespan to search
     * @return all records of the segment within given time interval
     * @throws de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.SegmentProviderException if
     *             fetching the data fails
     */
    public default List<Record> getRecords(Metadata metadata, T segment, Timespan timespan) throws SegmentProviderException {
        //TODO ensure getRecord sorted by time
        List<Record> records = getRecords(metadata, segment);
        List<Record> filteredRecord = new ArrayList<>();

        ZonedDateTime previousEndTime = segment.getStart();
        for (Record currentRecord: records) {
            TimeType recordTime = currentRecord.getSingleTime();
            ZonedDateTime startTime = recordTime.isIncrementalToPrevious() ? previousEndTime : segment.getStart();
            ZonedDateTime currentEnd = recordTime.getEndAsZonedDateTime(startTime);
            previousEndTime = currentEnd;

            if (timespan.overlaps(new Timespan(recordTime.getStartAsZonedDateTime(startTime), currentEnd))) {
                filteredRecord.add(currentRecord);
            }
        }
        return filteredRecord;

        //        return getRecords(metadata, segment).stream()
        //                //                .filter(x -> (timespan.includes(x.getSingleTime().getStartAsZonedDateTime(segment.getStart())))
        //                //                        || (timespan.includes(x.getSingleTime().getEndAsZonedDateTime(segment.getStart()))))
        //                .collect(Collectors.toList());
    }
}
