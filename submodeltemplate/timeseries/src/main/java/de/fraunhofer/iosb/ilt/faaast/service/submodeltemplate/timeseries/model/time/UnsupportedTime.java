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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TimeType implementation for semantic IDs not yet supported.
 */
public class UnsupportedTime extends TimeType {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnsupportedTime.class);

    private String valueType = Datatype.DATE_TIME.getName();
    private String semanticID;
    private boolean isIncremental;

    public UnsupportedTime() {
        super(null);
    }


    public UnsupportedTime(String timestamp, String semanticID, Optional<String> valueType) {
        super(timestamp);
        this.semanticID = semanticID;

        if (valueType.isPresent()) {
            this.valueType = valueType.get();
        }
    }


    @Override
    public String getTimeSemanticID() {
        return this.semanticID;
    }


    @Override
    public String getOriginalTimestamp() {
        return this.timestamp;
    }


    @Override
    public String getDataValueType() {
        return this.valueType;
    }


    @Override
    public ZonedDateTime getStartAsZonedDateTime(Optional<TimeType> startTime) {
        try {
            return ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        }
        catch (DateTimeParseException e) {
            LOGGER.warn(String.format("Unsupported time format: [%s]. Parsing to ZonedDateTime not successfull", this.semanticID));
        }

        if (startTime.isEmpty()) {
            return DEFAULT_START_TIME;
        }
        else if (startTime.get() instanceof UnsupportedTime) {
            return startTime.get().getStartAsZonedDateTime(Optional.empty());
        }
        else {
            return startTime.get().getStartAsZonedDateTime(startTime);
        }
    }


    @Override
    public ZonedDateTime getEndAsZonedDateTime(Optional<TimeType> startTime) {
        try {
            return ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        }
        catch (DateTimeParseException e) {
            LOGGER.warn(String.format("Unsupported time format: [%s]. Parsing to ZonedDateTime not successfull", this.semanticID));
        }

        if (startTime.isEmpty()) {
            return DEFAULT_END_TIME;
        }
        else if (startTime.get() instanceof UnsupportedTime) {
            return startTime.get().getStartAsZonedDateTime(Optional.empty());
        }
        else {
            return startTime.get().getStartAsZonedDateTime(startTime);
        }
    }


    @Override
    public boolean isParseable() {
        return false;
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
            UnsupportedTime other = (UnsupportedTime) obj;
            return Objects.equals(this.semanticID, other.semanticID)
                    && Objects.equals(this.valueType, other.valueType)
                    && Objects.equals(this.timestamp, other.timestamp);
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
        this.semanticID = semanticID;
        this.valueType = datavalueType;
    }

}
