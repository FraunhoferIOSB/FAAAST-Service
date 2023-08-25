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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.impl;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DateTimeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.AbstractAbsoluteTime;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.SupportedSemanticID;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


/**
 * TimeType implementation of UTC time as defined in TimeSeriesSubmodel. Timestamp according to ISO 8601 on the
 * timescale ccordinated universal time (UTC).
 */
@SupportedSemanticID("https://admin-shell.io/idta/TimeSeries/UtcTime/1/1")
public class UtcTime extends AbstractAbsoluteTime {

    private static final String VALUE_TYPE = Datatype.DATE_TIME.getName();

    public UtcTime() {
        super(VALUE_TYPE);
    }


    public UtcTime(String value) {
        super(VALUE_TYPE);
        init(value);
    }


    @Override
    public boolean init(String value) {
        if (value == null) {
            return false;
        }
        DateTimeValue dateTimeValue = (DateTimeValue) TypedValueFactory.createSafe(VALUE_TYPE, value);
        if (dateTimeValue != null) {
            this.startTimestampInEpochMillis = dateTimeValue.getValue().toInstant().toEpochMilli();
            this.endTimestampInEpochMillis = this.startTimestampInEpochMillis;
            this.isInitialized = true;
        }
        else {
            this.isInitialized = false;
        }
        return this.isInitialized;
    }


    @Override
    public String getTimestampString() {
        return this.isInitialized ? ZonedDateTime.ofInstant(Instant.ofEpochMilli(startTimestampInEpochMillis), ZoneOffset.UTC).format(DateTimeFormatter.ISO_ZONED_DATE_TIME) : null;
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
            return super.equals(obj);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), VALUE_TYPE);
    }

}
