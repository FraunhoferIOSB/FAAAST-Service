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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries;

import static de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.AbstractTimeSeriesOperationProvider.tryParseLong;

import io.adminshell.aas.v3.model.Property;
import java.util.Optional;


/**
 * Class representeing a timestamp with optional start and end points. If start resp. end is not set that means that the
 * interval is open in that direction.
 */
public class Timespan {

    private final Optional<Long> start;
    private final Optional<Long> end;

    public Timespan(long start, long end) {
        this.start = Optional.of(start);
        this.end = Optional.of(end);
    }


    public Timespan(Property start, Property end) {
        this.start = start != null ? tryParseLong(start.getValue()) : Optional.empty();
        this.end = end != null ? tryParseLong(end.getValue()) : Optional.empty();
    }


    /**
     * Checks if a given point in time is inside the timespan.
     *
     * @param point the point to check
     * @return true if given point is inside the timespan, false otherwise
     */
    public boolean includes(long point) {
        return absoluteStart() <= point && absoluteEnd() >= point;
    }


    /**
     * Checks if overlaps with other timespan.
     *
     * @param other other timespan
     * @return true if overlaps, false otherwise
     */
    public boolean overlaps(Timespan other) {
        return includes(other.absoluteStart()) || includes(other.absoluteEnd()) || other.includes(absoluteStart()) || other.includes(absoluteEnd());
    }


    public Optional<Long> getStart() {
        return start;
    }


    public Optional<Long> getEnd() {
        return end;
    }


    private long absoluteStart() {
        return start.orElse(Long.MIN_VALUE);
    }


    private long absoluteEnd() {
        return end.orElse(Long.MAX_VALUE);
    }

}
