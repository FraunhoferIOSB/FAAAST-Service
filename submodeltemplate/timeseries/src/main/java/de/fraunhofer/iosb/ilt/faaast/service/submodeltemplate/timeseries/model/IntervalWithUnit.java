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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * Wrapper for an interval and corresponding time unit.
 */
public class IntervalWithUnit {

    private Long interval;
    private TimeUnit unit;

    public IntervalWithUnit() {

    }


    public IntervalWithUnit(Long interval, TimeUnit unit) {
        this.interval = interval;
        this.unit = unit;
    }


    public Long getInterval() {
        return interval;
    }


    public void setInterval(Long interval) {
        this.interval = interval;
    }


    public TimeUnit getUnit() {
        return unit;
    }


    public void setUnit(TimeUnit unit) {
        this.unit = unit;
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
            IntervalWithUnit other = (IntervalWithUnit) obj;
            return super.equals(obj)
                    && Objects.equals(this.interval, other.interval)
                    && Objects.equals(this.unit, other.unit);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(interval, unit);
    }
}
