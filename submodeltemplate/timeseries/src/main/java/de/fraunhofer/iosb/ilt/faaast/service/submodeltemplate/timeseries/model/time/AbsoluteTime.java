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

import java.util.OptionalLong;


/**
 * Interface for absolute times of TimeSeries data. All time options are interpreted as a Duration. A point in time is a
 * duration of length 0.
 */
public interface AbsoluteTime extends Time {

    /**
     * Return the start time of the duration as Unix Epoch Milliseconds or an empty {@link OptionalLong} if the time
     * object was not initialized.
     *
     * @return The start time of the time duration transformed to Epoch Milliseconds.
     */
    public OptionalLong getStartAsEpochMillis();


    /**
     * Return the end time of the duration as Unix Epoch Milliseconds or an empty {@link OptionalLong} if the time
     * object was not initialized.
     *
     * @return The end time of the time duration transformed to Epoch Milliseconds.
     */
    public OptionalLong getEndAsEpochMillis();

}