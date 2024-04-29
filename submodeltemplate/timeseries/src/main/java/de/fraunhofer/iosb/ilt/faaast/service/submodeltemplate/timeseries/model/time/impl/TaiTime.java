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

import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DateTimeValue;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.AbstractAbsoluteTime;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.SupportedSemanticID;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.JulianFields;
import java.util.Objects;
import org.threeten.extra.scale.UtcRules;


/**
 * TimeType implementation of Time in TAI (International atomic time) as defined in TimeSeriesSubmodel. Timestamp
 * according to ISO 8601 on the
 * timescale international atomic time (TAI).
 */
@SupportedSemanticID("https://admin-shell.io/idta/TimeSeries/TaiTime/1/1")
public class TaiTime extends AbstractAbsoluteTime {

    private static final String VALUE_TYPE = Datatype.DATE_TIME.getName();
    private long taiOffset;

    public TaiTime() {
        super(VALUE_TYPE);
    }


    public TaiTime(String value) {
        super(VALUE_TYPE);
        init(value);
    }


    @Override
    public boolean init(String value) {
        if (value == null) {
            return false;
        }
        DateTimeValue typedValue = (DateTimeValue) TypedValueFactory.createSafe(VALUE_TYPE, value);
        if (typedValue == null) {
            this.isInitialized = false;
        }
        else {
            this.startTimestampInUtcTime = convertToUtc(typedValue);
            this.endTimestampInUtcTime = this.startTimestampInUtcTime;
            this.isInitialized = true;
        }
        return this.isInitialized;
    }


    private ZonedDateTime convertToUtc(DateTimeValue value) {
        ZonedDateTime time = value.getValue().toZonedDateTime();
        this.taiOffset = UtcRules.system().getTaiOffset(time.getLong(JulianFields.MODIFIED_JULIAN_DAY));
        time = time.minusSeconds(this.taiOffset);
        return time;
    }


    @Override
    public String getTimestampString() {
        return this.isInitialized
                ? startTimestampInUtcTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), VALUE_TYPE, taiOffset);
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
            return super.equals(obj)
                    && Objects.equals(this.taiOffset, other.taiOffset);
        }
    }

}
