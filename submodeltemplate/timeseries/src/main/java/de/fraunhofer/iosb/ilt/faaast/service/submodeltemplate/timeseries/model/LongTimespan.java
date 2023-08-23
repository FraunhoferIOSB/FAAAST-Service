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

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import java.time.ZonedDateTime;
import java.util.OptionalLong;


/**
 * Class representing a timestamp with optional start and end points. If start resp. end is not set that means that the
 * interval is open in that direction.
 */
public class LongTimespan {

    private final OptionalLong start;
    private final OptionalLong end;
    public static final LongTimespan EMPTY = new LongTimespan(OptionalLong.empty(), OptionalLong.empty());

    /**
     * Creates an new instance of given start and end.
     *
     * @param start the start
     * @param end the end
     * @return new instance
     */
    public static LongTimespan of(Long start, Long end) {
        return new LongTimespan(start, end);
    }


    /**
     * Creates an new instance of given start and end.
     *
     * @param start the start
     * @param end the end
     * @return new instance
     */
    public static LongTimespan fromString(String start, String end) {
        return new LongTimespan(tryParse(start), tryParse(end));
    }


    /**
     * Creates an new instance of given DateTime timespan.
     *
     * @param timespan ZonedDateTime based timespan to be converted to unix timespamps in long values
     * @return new instance
     */
    public static LongTimespan fromTimespan(Timespan timespan) {
        return new LongTimespan((timespan.getStart().isPresent() ? OptionalLong.of(timespan.getStart().get().toInstant().toEpochMilli()) : OptionalLong.empty()),
                timespan.getStart().isPresent() ? OptionalLong.of(timespan.getEnd().get().toInstant().toEpochMilli()) : OptionalLong.empty());
    }


    public LongTimespan(Long start, Long end) {
        this.start = start != null ? OptionalLong.of(start.longValue()) : OptionalLong.empty();
        this.end = end != null ? OptionalLong.of(end.longValue()) : OptionalLong.empty();
    }


    public LongTimespan(OptionalLong start, OptionalLong end) {
        this.start = start;
        this.end = end;
    }


    /**
     * Checks if a given point in time is inside the timespan.
     *
     * @param point the point to check
     * @return true if given point is inside the timespan, false otherwise
     */
    public boolean includes(Long point) {
        return !(point < absoluteStart() || point > absoluteEnd());
    }


    /**
     * Checks if overlaps with other timespan.
     *
     * @param other other timespan
     * @return true if overlaps, false otherwise
     */
    public boolean overlaps(LongTimespan other) {
        return includes(other.absoluteStart()) || includes(other.absoluteEnd()) || other.includes(absoluteStart()) || other.includes(absoluteEnd());
    }


    public OptionalLong getStart() {
        return start;
    }


    public OptionalLong getEnd() {
        return end;
    }


    private Long absoluteStart() {
        return start.orElse(Long.MIN_VALUE);
    }


    private Long absoluteEnd() {
        return end.orElse(Long.MAX_VALUE);
    }


    private static OptionalLong tryParse(String value) {
        if (value == null) {
            return OptionalLong.empty();
        }

        try {
            return OptionalLong.of((Long) TypedValueFactory.create(Datatype.LONG, value).getValue());
        }
        catch (ValueFormatException ex) {}
        try {
            return OptionalLong.of(((ZonedDateTime) TypedValueFactory.create(Datatype.DATE_TIME, value).getValue()).toInstant().toEpochMilli());
        }
        catch (ValueFormatException e) {
            return OptionalLong.empty();
        }
    }

}
