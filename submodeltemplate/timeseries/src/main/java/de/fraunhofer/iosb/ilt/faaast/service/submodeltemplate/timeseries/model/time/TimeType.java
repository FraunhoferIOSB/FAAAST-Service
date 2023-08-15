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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


/**
 * Class for time options of TimeSeries data. All time options are interpreted as a Duration. A point in time is a
 * duration of length 0.
 */
public abstract class TimeType {

    protected String timestamp;
    public static final ZonedDateTime DEFAULT_START_TIME = ZonedDateTime.parse("0000-01-01T00:00:00Z", DateTimeFormatter.ISO_ZONED_DATE_TIME);
    public static final ZonedDateTime DEFAULT_END_TIME = ZonedDateTime.parse("9999-12-31T00:00:00Z", DateTimeFormatter.ISO_ZONED_DATE_TIME);

    protected TimeType(String timestamp) {
        this.timestamp = timestamp;
    }


    /**
     * Return the start time of the duration as a {@link ZonedDateTime}. Alternatively might return
     * {@link DEFAULT_START_TIME} if the value of the TimeType not parseable.
     *
     * @param startTime TimeType representing the start time point, if the TimeType is relative.
     * @return The start time of the time duration transformed to {@link ZonedDateTime}.
     */
    public abstract ZonedDateTime getStartAsZonedDateTime(Optional<TimeType> startTime);


    /**
     * Return the end time of the duration as a {@link ZonedDateTime}. Alternatively might return
     * {@link DEFAULT_END_TIME} if the value of the TimeType not parseable.
     *
     * @param startTime TimeType representing the start time point, if the TimeType is relative.
     * @return The end time of the time duration transformed to {@link ZonedDateTime}.
     */
    public abstract ZonedDateTime getEndAsZonedDateTime(Optional<TimeType> startTime);


    /**
     * Check whether the original time stamp is in the correct format.
     *
     * @return true, if the value can be parsed.
     */
    public abstract boolean isParseable();


    /**
     * Set whether the time is incremental and therefore dependent on a previous time value (i.e. previous records)
     *
     * @param isIncremental boolean whether the value is to be interpreted as incremental value.
     */
    public abstract void setIsIncrementalToPrevious(boolean isIncremental);


    /**
     * Check whether the time is incremental and therefore dependent on a previous time value (i.e. previous records)
     *
     * @return boolean whether the value is to be interpreted as incremental value.
     */
    public abstract boolean isIncrementalToPrevious();


    /**
     * Return the start time of the duration as a {@link ZonedDateTime}. Wraps the given start time into a
     * {@linkUtcTime}. Might return {@link DEFAULT_START_TIME} if the value of the TimeType not parseable.
     *
     * @param startTime ZonedDateTime representing the start time point, if the TimeType is relative.
     * @return The start time of the time duration transformed to {@link ZonedDateTime}.
     */
    public ZonedDateTime getStartAsZonedDateTime(ZonedDateTime startTime) {
        if (startTime != null) {
            TimeType startTimeWrapped = new UtcTime(startTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            return getStartAsZonedDateTime(Optional.of(startTimeWrapped));
        }
        else {
            return getStartAsZonedDateTime(Optional.empty());
        }
    }


    /**
     * Return the end time of the duration as a {@link ZonedDateTime}. Wraps the given start time into a
     * {@linkUtcTime}. Might return {@link DEFAULT_END_TIME} if the value of the TimeType not parseable.
     *
     * @param startTime ZonedDateTime representing the start time point, if the TimeType is relative.
     * @return The end time of the time duration transformed to {@link ZonedDateTime}.
     */
    public ZonedDateTime getEndAsZonedDateTime(ZonedDateTime startTime) {
        if (startTime != null) {
            TimeType startTimeWrapped = new UtcTime(startTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            return getEndAsZonedDateTime(Optional.of(startTimeWrapped));
        }
        else {
            return getEndAsZonedDateTime(Optional.empty());
        }
    }


    /**
     * Get the semantic ID represented by this class.
     *
     * @return String containing the semantic ID represented by this class.
     */
    public abstract String getTimeSemanticID();


    /**
     * Get the name of the {@link Datatype} used for the timestamp.
     *
     * @return {@link Datatype} name for the AAS property of the time.
     */
    public abstract String getDataValueType();


    /**
     * Set the non final variables of the TimeType.
     *
     * @param value String containing the time stamp.
     * @param semanticID String containing the semantic ID of the time type.
     * @param datavalueType String containing the {@link Datatype} name used for this TimeType.
     */
    public abstract void init(String value, String semanticID, String datavalueType);


    /**
     * Get the original input timestamp for the TimeType.
     *
     * @return the original input String.
     */
    public String getOriginalTimestamp() {
        return this.timestamp;
    }

}
