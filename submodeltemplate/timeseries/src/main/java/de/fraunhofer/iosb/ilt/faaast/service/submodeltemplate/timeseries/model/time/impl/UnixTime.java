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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.LongValue;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.AbstractAbsoluteTime;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.SupportedSemanticID;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;


/**
 * TimeType implementation for UNIX Timestamps (seconds since 01.01.1970).
 */
@SupportedSemanticID("https://admin-shell.io/idta/TimeSeries/UnixTime/1/1")
public class UnixTime extends AbstractAbsoluteTime {

    private static final String VALUE_TYPE = Datatype.LONG.getName();

    public UnixTime() {
        super(VALUE_TYPE);
    }


    public UnixTime(String value) {
        super(VALUE_TYPE);
        init(value);
    }


    @Override
    public boolean init(String value) {
        if (value == null) {
            return false;
        }
        LongValue longValue = (LongValue) TypedValueFactory.createSafe(VALUE_TYPE, value);
        if (longValue != null) {
            this.startTimestampInUtcTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(longValue.getValue()), ZoneOffset.UTC);
            this.endTimestampInUtcTime = startTimestampInUtcTime;
            this.isInitialized = true;
        }
        else {
            this.isInitialized = false;
        }
        return this.isInitialized;
    }


    @Override
    public String getTimestampString() {
        return this.isInitialized ? String.valueOf(this.startTimestampInUtcTime.toEpochSecond()) : null;
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