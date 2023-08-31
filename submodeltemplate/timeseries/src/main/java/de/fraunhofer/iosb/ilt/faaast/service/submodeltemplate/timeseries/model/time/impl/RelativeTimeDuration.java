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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.FloatValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.AbstractRelativeTime;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.SupportedSemanticID;
import java.util.Objects;


/**
 * Relative time duration in seconds as defined in TimeSeriesSubmodel. Time durations are relative to the previous entry
 * in the time series segment.
 */
@SupportedSemanticID("https://admin-shell.io/idta/TimeSeries/RelativeTimeDuration/1/1")
public class RelativeTimeDuration extends AbstractRelativeTime {

    private static final String VALUE_TYPE = Datatype.FLOAT.getName();

    public RelativeTimeDuration() {
        super(VALUE_TYPE);
    }


    public RelativeTimeDuration(String value) {
        super(VALUE_TYPE);
        init(value);
    }


    @Override
    public boolean init(String value) {
        if (value == null) {
            return false;
        }
        FloatValue floatValue = (FloatValue) TypedValueFactory.createSafe(VALUE_TYPE, value);
        if (floatValue != null) {
            this.startOffsetInNanoseconds = 0;
            this.endOffsetInNanoseconds = 1000000000 * floatValue.getValue().longValue();
            this.isInitialized = true;
        }
        else {
            this.isInitialized = false;
        }
        return this.isInitialized;
    }


    @Override
    public String getTimestampString() {
        return this.isInitialized ? Long.toString((endOffsetInNanoseconds / 1000000000)) : null;
    }


    @Override
    public boolean isIncrementalToPrevious() {
        return true;
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
