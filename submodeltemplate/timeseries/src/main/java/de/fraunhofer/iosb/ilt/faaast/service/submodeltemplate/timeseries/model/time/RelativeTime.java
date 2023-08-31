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
import java.util.OptionalLong;


/**
 * Interface for relative time options of TimeSeries data. All time options are interpreted as a Duration. A point in
 * time is a duration of length 0.
 */
public interface RelativeTime extends Time {

    /**
     * Return the start time of the duration as a Unix Epoch milliseconds.
     *
     * @param utcStartTime point in time to which the RelativeTime is relative to in epoch milliseconds
     * @return {@link OptionalLong} containing the start time of the time duration transformed to Unix Epoch Milliseconds or
     *         an empty
     *         {@link OptionalLong} if the calculation was not successful or the object was not initialized.
     */
    public ZonedDateTime getStartAsUtcTime(ZonedDateTime utcStartTime) throws MissingInitialisationException;


    /**
     * Return the end time of the duration as Unix Epoch milliseconds.
     *
     * @param utcStartTime point in time to which the RelativeTime is relative to in epoch milliseconds
     * @return {@link OptionalLong} containing the end time of the time duration transformed to Unix Epoch Milliseconds or
     *         an empty
     *         {@link OptionalLong} if the calculation was not successfull or the object was not initialized.
     */
    public ZonedDateTime getEndAsEpochMillis(ZonedDateTime utcStartTime) throws MissingInitialisationException;


    /**
     * Check whether the time is incremental to a previous time value (i.e. previous records'). If false, all timepoints
     * of this class are relative to the same time point and should get the same startTIme during their start and end
     * time calculation.
     *
     * @return boolean whether the value is to be interpreted as incremental value.
     */
    public boolean isIncrementalToPrevious();

}
