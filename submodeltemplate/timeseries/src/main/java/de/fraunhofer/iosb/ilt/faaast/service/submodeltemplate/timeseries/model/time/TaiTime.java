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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.JulianFields;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.scale.UtcRules;


/**
 * TimeType implementation of Time in TAI (International atomic time) as defined in TimeSeriesSubmodel. Timestamp
 * according to ISO 8601 on the
 * timescale international atomic time (TAI).
 */
public class TaiTime extends TimeType {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaiTime.class);

    private final String SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/TaiTime/1/1";
    private final String VALUE_TYPE = Datatype.DATE_TIME.getName();
    public boolean isIncremental = false;

    public TaiTime(String value) {
        super(value);
    }


    @Override
    public String getTimeSemanticID() {
        return this.SEMANTIC_ID;
    }


    @Override
    public String getOriginalTimestamp() {
        return this.timestamp;
    }


    @Override
    public String getDataValueType() {
        return this.VALUE_TYPE;
    }


    @Override
    public ZonedDateTime getStartAsZonedDateTime(Optional<TimeType> startTime) {
        try {
            LocalDateTime time = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            int offset = UtcRules.system().getTaiOffset(time.getLong(JulianFields.MODIFIED_JULIAN_DAY));
            time = time.minusSeconds(offset); //TODO: check if plus or minus
            return time.atZone(ZoneOffset.UTC);
        }
        catch (DateTimeParseException e) {
            LOGGER.error(String.format("TaiTime: could not parse [%s]", timestamp));
            return DEFAULT_START_TIME;
        }
    }


    @Override
    public ZonedDateTime getEndAsZonedDateTime(Optional<TimeType> startTime) {
        return getStartAsZonedDateTime(startTime);
    }


    @Override
    public boolean isParseable() {
        try {
            ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
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
            TaiTime other = (TaiTime) obj;
            return Objects.equals(this.SEMANTIC_ID, other.SEMANTIC_ID)
                    && Objects.equals(this.VALUE_TYPE, other.VALUE_TYPE)
                    && Objects.equals(this.timestamp, other.timestamp)
                    && Objects.equals(this.isIncremental, other.isIncremental);
        }
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
