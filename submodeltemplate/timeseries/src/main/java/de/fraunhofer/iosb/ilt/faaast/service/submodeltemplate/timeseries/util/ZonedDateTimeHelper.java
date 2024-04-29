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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.util;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValueFactory;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;


/**
 * Collection of utility methods for {@link java.time.ZonedDateTime}.
 */
public class ZonedDateTimeHelper {

    /**
     * Parses a given string to {@link java.time.ZonedDateTime} if possible.
     *
     * @param value the value to parse
     * @return {@link java.util.Optional} of {@link java.time.ZonedDateTime} containing the result
     */
    public static Optional<ZonedDateTime> tryParse(String value) {
        try {
            Object typedValue = TypedValueFactory.create(Datatype.DATE_TIME, value).getValue();
            if (Objects.isNull(typedValue) || !OffsetDateTime.class.isAssignableFrom(typedValue.getClass())) {
                return Optional.empty();
            }
            return Optional.ofNullable(((OffsetDateTime) typedValue).toZonedDateTime());
        }
        catch (ValueFormatException ex) {
            return Optional.empty();
        }
    }


    /**
     * Convert epoch milliseconds to UTC ZonedDateTime.
     *
     * @param epochMillis timestamp expressed in milliseconds since 01.01.1970.
     * @return Equivalent ZonedDateTime value.
     */
    public static ZonedDateTime convertEpochMillisToZonedDateTime(Long epochMillis) {
        if (epochMillis != null) {
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
        }
        else {
            return null;
        }
    }


    /**
     * Convert ZonedDateTime to epoch milliseconds.
     *
     * @param dateTime ZonedDateTime value
     * @return Equivalent timestamp expressed in milliseconds since 01.01.1970
     */
    public static Long convertZonedDateTimeToEpochMillis(ZonedDateTime dateTime) {
        if (dateTime != null) {
            return dateTime.toInstant().toEpochMilli();
        }
        else {
            return null;
        }
    }


    private ZonedDateTimeHelper() {

    }
}
