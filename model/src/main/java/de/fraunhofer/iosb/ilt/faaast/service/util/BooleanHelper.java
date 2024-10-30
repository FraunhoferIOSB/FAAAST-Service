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
package de.fraunhofer.iosb.ilt.faaast.service.util;

/**
 * Generic helper functions for working with booleans.
 */
public class BooleanHelper {

    private BooleanHelper() {}


    /**
     * Checks if a given string is a valid boolean, i.e., equals true or false ignoring case.
     *
     * @param value the string to parse
     * @return the corresponding boolean value
     * @throws IllegalArgumentException if the value is not a valid boolean
     */
    public static boolean parseStrictIgnoreCase(String value) {
        if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
            return true;
        }
        if (Boolean.FALSE.toString().equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException(String.format("'%s' is not a valid boolean value", value));
    }
}
