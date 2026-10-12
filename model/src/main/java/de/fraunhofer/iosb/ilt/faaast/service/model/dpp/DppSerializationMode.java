/*
 * Copyright 2026 Fraunhofer IOSB.
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

package de.fraunhofer.iosb.ilt.faaast.service.model.dpp;

import java.util.Arrays;


/**
 * Represents the different serialization modes for DPPs.
 */
public enum DppSerializationMode {
    COMPRESSED("compressed"),
    EXPANDED("full");

    private final String name;

    public static DppSerializationMode DEFAULT = DppSerializationMode.COMPRESSED;

    /**
     * Find DppSerializationMode corresponding to the given string.
     * 
     * @param s string to be parsed to DppSerializationMode.
     * @return DppSerializationMode corresponding to input.
     */
    public static DppSerializationMode parse(String s) {
        return Arrays.stream(DppSerializationMode.values())
                .filter(mode -> mode.getName().equals(s))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown serialization type"));
    }


    DppSerializationMode(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

}
