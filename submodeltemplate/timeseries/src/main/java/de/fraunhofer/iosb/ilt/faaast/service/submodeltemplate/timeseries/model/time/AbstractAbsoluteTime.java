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

import java.time.ZonedDateTime;
import java.util.Objects;


/**
 * Abstract class implementing the AbsoluteTime interface. Contains the semanticID, datatype and methods to get the
 * start and end time.
 * Extending classes should set the startTimestampInEpochMillis and endTimestampInEpochMillis variables in the init
 * method, and should call the constructor of the abstract class in a parameterless constructor.
 */
public abstract class AbstractAbsoluteTime extends AbstractTime implements AbsoluteTime {

    protected boolean isInitialized = false;
    protected ZonedDateTime startTimestampInUtcTime;
    protected ZonedDateTime endTimestampInUtcTime;

    protected AbstractAbsoluteTime(String datatype) {
        super(datatype);
    }


    @Override
    public ZonedDateTime getStartAsUtcTime() throws MissingInitialisationException {
        if (this.isInitialized) {
            return this.startTimestampInUtcTime;
        }
        throw new MissingInitialisationException();
    }


    @Override
    public ZonedDateTime getEndAsUtcTime() throws MissingInitialisationException {
        if (this.isInitialized) {
            return this.endTimestampInUtcTime;
        }
        throw new MissingInitialisationException();
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
            AbstractAbsoluteTime other = (AbstractAbsoluteTime) obj;
            return super.equals(obj)
                    && Objects.equals(this.isInitialized, other.isInitialized)
                    && Objects.equals(this.startTimestampInUtcTime, other.startTimestampInUtcTime)
                    && Objects.equals(this.endTimestampInUtcTime, other.endTimestampInUtcTime);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.isInitialized, this.startTimestampInUtcTime, this.endTimestampInUtcTime);
    }

}
