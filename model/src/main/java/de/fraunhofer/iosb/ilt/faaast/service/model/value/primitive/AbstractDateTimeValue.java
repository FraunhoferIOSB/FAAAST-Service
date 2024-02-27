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

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import org.apache.commons.lang3.StringUtils;


/**
 * Abstract base class for date and/or time-realted values.
 *
 * @param <T> The actual type
 */
public abstract class AbstractDateTimeValue<T extends Temporal> extends TypedValue<T> {

    private boolean isLocal = false;

    protected AbstractDateTimeValue() {
        super();
    }


    protected AbstractDateTimeValue(T value) {
        super(value);
    }


    /**
     * Gets the {@link DateTimeFormatter} to format local values, i.e. without offset.
     *
     * @return the {@link DateTimeFormatter}
     */
    protected DateTimeFormatter getFormatLocal() {
        return new DateTimeFormatterBuilder()
                .append(getFormatBase())
                .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
                .toFormatter();
    }


    /**
     * Gets the {@link DateTimeFormatter} that can be used as base for both local and with offset.
     *
     * @return the {@link DateTimeFormatter}
     */
    protected DateTimeFormatter getFormatBase() {
        return new DateTimeFormatterBuilder().toFormatter();
    }


    /**
     * Gets the {@link DateTimeFormatter} to format values with offset.
     *
     * @return the {@link DateTimeFormatter}
     */
    protected DateTimeFormatter getFormatOffset() {
        return new DateTimeFormatterBuilder()
                .append(getFormatBase())
                .appendZoneOrOffsetId()
                .toFormatter();
    }


    /**
     * Parses the value as local value, i.e. without offset.
     *
     * @param value the value to parse
     * @return the parsed value
     * @throws DateTimeParseException if parsing fails
     */
    protected abstract T parseLocal(String value) throws DateTimeParseException;


    /**
     * Parses the value as value with offset, i.e. without offset.
     *
     * @param value the value to parse
     * @return the parsed value
     * @throws DateTimeParseException if parsing fails
     */
    protected abstract T parseOffset(String value) throws DateTimeParseException;


    /**
     * Checks if a given string represents a local element, i.e. without offset.
     *
     * @param value the value to check
     * @return true if it is local, false otherwise
     */
    protected boolean isLocal(String value) {
        try {
            getFormatLocal().parse(value);
            return true;
        }
        catch (DateTimeParseException e) {
            return false;
        }
    }


    private T parse(String value) {
        return isLocal(value)
                ? parseLocal(value)
                : parseOffset(value);
    }


    @Override
    public void fromString(String value) throws ValueFormatException {
        if (StringUtils.isAllBlank(value)) {
            this.setValue(null);
            return;
        }
        try {
            setValue(parse(value));
            isLocal = isLocal(value);
        }
        catch (IllegalArgumentException e) {
            throw new ValueFormatException(
                    String.format("unable to parse value (value: %s, type: %s)",
                            value, getDataType()),
                    e);
        }
    }


    @Override
    public String asString() {
        return new DateTimeFormatterBuilder().append(isLocal
                ? getFormatLocal()
                : getFormatOffset())
                .toFormatter().format(value);
    }


    @Override
    public Datatype getDataType() {
        return Datatype.DATE_TIME;
    }

}
