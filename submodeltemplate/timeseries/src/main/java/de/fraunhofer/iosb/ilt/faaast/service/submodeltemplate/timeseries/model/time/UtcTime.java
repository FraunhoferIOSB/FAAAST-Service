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

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TimeType implementation of UTC time as defined in TimeSeriesSubmodel. Timestamp according to ISO 8601 on the
 * timescale ccordinated universal time (UTC).
 */
public class UtcTime extends TimeType {

    private static final Logger LOGGER = LoggerFactory.getLogger(UtcTime.class);

    private static final String SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/UtcTime/1/1";
    private static final String VALUE_TYPE = Datatype.DATE_TIME.getName();
    private boolean isIncremental = false;

    public UtcTime(String value) {
        super(value);
    }


    @Override
    public String getTimeSemanticID() {
        return SEMANTIC_ID;
    }


    @Override
    public String getDataValueType() {
        return VALUE_TYPE;
    }


    @Override
    public ZonedDateTime getStartAsZonedDateTime(Optional<TimeType> startTime) {
        try {
            return ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        }
        catch (DateTimeParseException e) {
            LOGGER.error(String.format("UtcTime: could not parse [%s]", timestamp));
            return ZonedDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneOffset.UTC);
        }
    }


    @Override
    public ZonedDateTime getEndAsZonedDateTime(Optional<TimeType> startTime) {
        return getStartAsZonedDateTime(startTime);
    }


    @Override
    public boolean isParseable() {
        try {
            ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_ZONED_DATE_TIME);
            return true;
        }
        catch (DateTimeParseException e) {
            return false;
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (obj == null) {
            return false;
        }
        else if (this.getClass() != obj.getClass()) {
            return false;
        }
        else {
            UtcTime other = (UtcTime) obj;
            return Objects.equals(this.timestamp, other.timestamp)
                    && Objects.equals(this.isIncrementalToPrevious(), other.isIncremental);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }


    @Override
    public void setIsIncrementalToPrevious(boolean isIncremental) {
        this.isIncremental = isIncremental;
    }


    @Override
    public boolean isIncrementalToPrevious() {
        return this.isIncremental;
    }


    @Override
    public void init(String value, String semanticID, String datavalueType) {
        this.timestamp = value;
    }

}
