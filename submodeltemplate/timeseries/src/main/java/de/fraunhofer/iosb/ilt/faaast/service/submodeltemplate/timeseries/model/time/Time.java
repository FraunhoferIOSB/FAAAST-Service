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

/**
 * Base interface for times of TimeSeries data. Only its subclasses should be implemented-
 */
public interface Time {

    /**
     * Set the timestamp by parsing the value String and transforming it to a long value containing the equivalent epoch
     * milliseconds.
     *
     * @param value String containing the time stamp. Can be null.
     * @return true, if the initialization of the object was successfull, false if not.
     */
    public boolean init(String value);


    /**
     * Get the the {@link Datatype} used for the timestamp.
     *
     * @return {@link Datatype} name for the AAS property of the time.
     */
    public String getDataValueType();


    /**
     * Get the timestamp in the original format as String or null, if the object wasn't initialized.
     *
     * @return String representation of the timestamp.
     */
    public String getTimestampString();

}
