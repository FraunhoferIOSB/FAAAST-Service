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

import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.util.ZonedDateTimeHelper;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;


/**
 * Class representing a timestamp with optional start and end points. If start resp. end is not set that means that the
 * interval is open in that direction.
 */
public class Timespan {

    private final Optional<ZonedDateTime> start;
    private final Optional<ZonedDateTime> end;
    public static final Timespan EMPTY = new Timespan(Optional.empty(), Optional.empty());

    /**
     * Creates an new instance of given start and end.
     *
     * @param start the start
     * @param end the end
     * @return new instance
     */
    public static Timespan of(ZonedDateTime start, ZonedDateTime end) {
        return new Timespan(start, end);
    }


    /**
     * Creates an new instance of given start and end.
     *
     * @param start the start
     * @param end the end
     * @return new instance
     */
    public static Timespan fromString(String start, String end) {
        return new Timespan(ZonedDateTimeHelper.tryParse(start), ZonedDateTimeHelper.tryParse(end));
    }


    public Timespan(ZonedDateTime start, ZonedDateTime end) {
        this.start = Optional.of(start);
        this.end = Optional.of(end);
    }


    public Timespan(Optional<ZonedDateTime> start, Optional<ZonedDateTime> end) {
        this.start = start;
        this.end = end;
    }


    /**
     * Checks if a given point in time is inside the timespan.
     *
     * @param point the point to check
     * @return true if given point is inside the timespan, false otherwise
     */
    public boolean includes(ZonedDateTime point) {
        return !(point.isBefore(absoluteStart()) || point.isAfter(absoluteEnd()));
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


    public Optional<ZonedDateTime> getStart() {
        return start;
    }


    public Optional<ZonedDateTime> getEnd() {
        return end;
    }


    private ZonedDateTime absoluteStart() {
        return start.orElse(Instant.ofEpochMilli(Long.MIN_VALUE).atZone(ZoneOffset.UTC));
    }


    private ZonedDateTime absoluteEnd() {
        return end.orElse(Instant.ofEpochMilli(Long.MAX_VALUE).atZone(ZoneOffset.UTC));
    }

}
