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
import java.util.Objects;


/**
 * Relative point in time in seconds as defined in TimeSeriesSubmodel. Time points refer to the start time of the time
 * series segment.
 */
public class RelativePointInTime extends AbstractRelativeTime {

    private static final String SEMANTIC_ID = "https://admin-shell.io/idta/TimeSeries/RelativePointInTime/1/1";
    private static final String DATA_TYPE = Datatype.FLOAT.getName();

    public RelativePointInTime() {
        super(SEMANTIC_ID, DATA_TYPE);
    }


    public RelativePointInTime(String value) {
        super(SEMANTIC_ID, DATA_TYPE);
        this.init(value);
    }


    @Override
    public boolean init(String value) {
        if (value == null) {
            return false;
        }
        FloatValue floatValue = (FloatValue) TypedValueFactory.createSafe(DATA_TYPE, value);
        if (floatValue != null) {
            this.startOffsetInEpochMillis = 1000 * floatValue.getValue().longValue();
            this.endOffsetInEpochMillis = this.startOffsetInEpochMillis;
            this.isInitialized = true;
        }
        else {
            this.isInitialized = false;
        }
        return this.isInitialized;
    }


    @Override
    public String getTimestampString() {
        return this.isInitialized ? Long.toString((this.startOffsetInEpochMillis / 1000)) : null;
    }


    @Override
    public boolean isIncrementalToPrevious() {
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
            RelativePointInTime other = (RelativePointInTime) obj;
            return Objects.equals(this.startOffsetInEpochMillis, other.startOffsetInEpochMillis)
                    && Objects.equals(this.endOffsetInEpochMillis, other.endOffsetInEpochMillis);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

}
