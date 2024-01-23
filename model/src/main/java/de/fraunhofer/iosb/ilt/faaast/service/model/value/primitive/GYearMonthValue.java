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
package de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;


/**
 * A year value.
 */
public class GYearMonthValue extends AbstractDateTimeValue<OffsetDateTime> {

    private static final DateTimeFormatter FORMAT_BASE = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ofPattern("yyyy-MM"))
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .toFormatter();

    public GYearMonthValue() {
        super();
    }


    public GYearMonthValue(OffsetDateTime value) {
        super(value);
    }


    @Override
    public Datatype getDataType() {
        return Datatype.GYEAR_MONTH;
    }


    @Override
    protected DateTimeFormatter getFormatLocal() {
        return new DateTimeFormatterBuilder()
                .append(FORMAT_BASE)
                .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
                .toFormatter();
    }


    @Override
    protected DateTimeFormatter getFormatOffset() {
        return new DateTimeFormatterBuilder()
                .append(FORMAT_BASE)
                .appendZoneOrOffsetId()
                .toFormatter();
    }


    @Override
    protected OffsetDateTime parseLocal(String value, ZoneOffset offset) throws DateTimeParseException {
        return OffsetDateTime.parse(value, getFormatLocal());
    }


    @Override
    protected OffsetDateTime parseOffset(String value) throws DateTimeParseException {
        return OffsetDateTime.parse(value, getFormatOffset());
    }

}
