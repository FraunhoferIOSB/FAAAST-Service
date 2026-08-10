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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;


public record DateTimeLiteral(ZonedDateTime value) implements Literal {

    public static DateTimeLiteral parse(String value) {
        return Optional.of(tryParseZonedDateTime(value))
                .orElse(tryParseInstant(value).map(i -> i.atZone(ZoneId.systemDefault())))
                .map(DateTimeLiteral::new)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Cannot parse %s to DateTime", value)));
    }


    private static Optional<ZonedDateTime> tryParseZonedDateTime(String value) {
        try {
            return Optional.of(ZonedDateTime.parse(value));
        }
        catch (DateTimeParseException dateTimeParseException) {
            return Optional.empty();
        }
    }


    private static Optional<Instant> tryParseInstant(String value) {
        try {
            return Optional.of(Instant.parse(value));
        }
        catch (DateTimeParseException dateTimeParseException) {
            return Optional.empty();
        }
    }


    @Override
    public boolean isDateTime() {
        return true;
    }


    @Override
    public DateTimeLiteral asDateTime() {
        return this;
    }
}
