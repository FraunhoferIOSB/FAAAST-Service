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
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.LongTimespan;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Metadata;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Segment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.AbsoluteTime;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.RelativeTime;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.Time;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.util.ZonedDateTimeHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;


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
     * @return all records of the segment within given time interval sorted by time
     * @throws de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.SegmentProviderException if
     *             fetching the data fails
     */
    public default List<Record> getRecords(Metadata metadata, T segment, LongTimespan timespan) throws SegmentProviderException {
        //                List<Record> records = getRecords(metadata, segment);
        //                List<Record> filteredRecord = new ArrayList<>();
        //                
        //                Long segmentStart = segment.getStart() != null ? segment.getStart().toInstant().toEpochMilli() : 0L;
        //                Long previousEndTime = segmentStart;
        //                for (Record currentRecord: records) {
        //                    Time recordTime = currentRecord.getSingleTime();
        //                    Long currentStartTime;
        //                    Long currentEnd;
        //                    if (recordTime instanceof RelativeTime) {
        //                        currentStartTime = ((RelativeTime)recordTime).isIncrementalToPrevious() ? previousEndTime : segmentStart;
        //                        currentEnd = ((RelativeTime)recordTime).getEndAsEpochMillis(currentStartTime).orElse(Long.MAX_VALUE);                        
        //                        previousEndTime = currentEnd;
        //                    }
        //                    else {
        //                        currentStartTime = ((AbsoluteTime)recordTime).getStartAsEpochMillis().orElse(Long.MIN_VALUE);
        //                        currentEnd = ((AbsoluteTime)recordTime).getEndAsEpochMillis().orElse(Long.MAX_VALUE);                        
        //                        previousEndTime = currentEnd;
        //                    }
        //                    if (timespan.overlaps(new LongTimespan(currentStartTime, currentEnd))) {
        //                        filteredRecord.add(currentRecord);
        //                    } else if(timespan.getEnd().orElse(Long.MAX_VALUE) < currentStartTime) {
        //                        break;
        //                    }
        //                }
        //                return filteredRecord;

        // Assumes: Records ordered by time, all records contain all time fields described in the metadata.
        Optional<Entry<String, Time>> absolute = metadata.getRecordMetadataTime().entrySet().stream().filter(entr -> entr.getValue() instanceof AbsoluteTime).findFirst();
        if (absolute.isPresent()) {
            String absoluteName = absolute.get().getKey();

            return getRecords(metadata, segment).stream().sequential()
                    .dropWhile(rec -> !getLongTimespan(((AbsoluteTime) (rec.getTimes().get(absoluteName)))).overlaps(timespan))
                    .takeWhile(rec -> getLongTimespan(((AbsoluteTime) (rec.getTimes().get(absoluteName)))).overlaps(timespan))
                    .collect(Collectors.toList());
        }

        Optional<Entry<String, Time>> relative = metadata.getRecordMetadataTime().entrySet().stream().filter(entr -> entr.getValue() instanceof RelativeTime).findFirst();
        if (relative.isPresent() && segment.getStart() != null) {
            String relativeName = relative.get().getKey();
            Long segmentStart = ZonedDateTimeHelper.convertZonedDateTimeToEpochMillis(segment.getStart());

            if (!((RelativeTime) relative.get().getValue()).isIncrementalToPrevious()) {
                return getRecords(metadata, segment).stream().sequential()
                        .dropWhile(rec -> !getLongTimespan(((RelativeTime) (rec.getTimes().get(relativeName))), segmentStart).overlaps(timespan))
                        .takeWhile(rec -> getLongTimespan(((RelativeTime) (rec.getTimes().get(relativeName))), segmentStart).overlaps(timespan))
                        .collect(Collectors.toList());
            }
            else {
                List<Record> filteredRecord = new ArrayList<>();
                List<Record> records = getRecords(metadata, segment);

                Long previousEndTime = segmentStart;
                for (Record currentRecord: records) {
                    RelativeTime recordTime = (RelativeTime) currentRecord.getTimes().get(relativeName);
                    long currentStartTime = recordTime.isIncrementalToPrevious() ? previousEndTime : segmentStart;
                    long currentEnd = recordTime.getEndAsEpochMillis(currentStartTime).getAsLong();
                    previousEndTime = currentEnd;

                    if (timespan.overlaps(new LongTimespan(recordTime.getStartAsEpochMillis(currentStartTime).orElse(currentEnd), currentEnd))) {
                        filteredRecord.add(currentRecord);
                    }
                }
                return filteredRecord;
            }

        }
        return new ArrayList<>();
    }


    private LongTimespan getLongTimespan(AbsoluteTime time) {
        return LongTimespan.of(time.getStartAsEpochMillis().orElse(Long.MIN_VALUE), time.getEndAsEpochMillis().orElse(Long.MAX_VALUE));
    }


    private LongTimespan getLongTimespan(RelativeTime time, long startTime) {
        return LongTimespan.of(time.getStartAsEpochMillis(startTime).orElse(Long.MIN_VALUE), time.getEndAsEpochMillis(startTime).orElse(Long.MAX_VALUE));
    }
}
