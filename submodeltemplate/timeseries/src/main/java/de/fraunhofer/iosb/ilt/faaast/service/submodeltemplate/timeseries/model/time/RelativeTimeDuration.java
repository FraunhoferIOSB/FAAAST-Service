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
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Relative time duration in seconds as defined in TimeSeriesSubmodel. Time durations refer to the previous entry in the
 * time series segment.
 */
public class RelativeTimeDuration extends TimeType {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelativeTimeDuration.class);

    private final String SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/RelativeTimeDuration/1/1";
    private final String VALUE_TYPE = Datatype.FLOAT.getName();

    private boolean isIncremental = true;

    public RelativeTimeDuration(String value) {
        super(value);
    }


    @Override
    public String getTimeSemanticID() {
        return this.SEMANTIC_ID;
    }


    @Override
    public String getDataValueType() {
        return this.VALUE_TYPE;
    }


    @Override
    public ZonedDateTime getStartAsZonedDateTime(Optional<TimeType> startTime) {
        if (startTime.isPresent()) {
            return startTime.get().getStartAsZonedDateTime(Optional.empty());
        }
        return DEFAULT_START_TIME;
    }


    @Override
    public ZonedDateTime getEndAsZonedDateTime(Optional<TimeType> startTime) {
        long relativeEndPoint = 1;
        try {
            relativeEndPoint = (long) Float.parseFloat(timestamp);
        }
        catch (NumberFormatException e) {
            LOGGER.error(String.format("RelativeTimeDuration [%s] not parseable: %s", timestamp, e.getMessage()));
            LOGGER.error(String.format("Using duration of 1s instead."));
        }
        if (startTime.isPresent()) {
            return startTime.get().getStartAsZonedDateTime(Optional.empty()).plusSeconds(relativeEndPoint);
        }
        return DEFAULT_START_TIME.plusSeconds(relativeEndPoint);
    }


    @Override
    public boolean isParseable() {
        try {
            Float.parseFloat(timestamp);
            return true;
        }
        catch (NumberFormatException e) {
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
            RelativeTimeDuration other = (RelativeTimeDuration) obj;
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
