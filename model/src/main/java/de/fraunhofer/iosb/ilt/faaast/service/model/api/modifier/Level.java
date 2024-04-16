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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.UnsupportedLevelModifierException;
import java.util.stream.Stream;


/**
 * Enum of different level options.
 */
public enum Level {
    DEEP,
    CORE;

    public static final Level DEFAULT = Level.DEEP;

    /**
     * Returns matching enum value from given string value.The names are matched case-insensitive, i.e. ignoring case.
     *
     * @param value the string value
     * @return matching enum value
     * @throws de.fraunhofer.iosb.ilt.faaast.service.model.exception.UnsupportedLevelModifierException if the value does not
     *             match any element
     */
    public static Level fromString(String value) throws UnsupportedLevelModifierException {
        return Stream.of(Level.values())
                .filter(x -> x.name().equalsIgnoreCase(value))
                .findAny()
                .orElseThrow(() -> new UnsupportedLevelModifierException(value));
    }


    /**
     * Returns matching enum value from given string value. The names are matched case-insensitive, i.e. ignoring case.
     * If the provided value does not match any enum value then {@link Level#DEFAULT} is returned.
     *
     * @param value the string value
     * @return matching enum value or default ({@link Level#DEFAULT}) if there is no match
     */
    public static Level fromStringOrDefault(String value) {
        return Stream.of(Level.values())
                .filter(x -> x.name().equalsIgnoreCase(value))
                .findAny()
                .orElse(DEFAULT);
    }
}
