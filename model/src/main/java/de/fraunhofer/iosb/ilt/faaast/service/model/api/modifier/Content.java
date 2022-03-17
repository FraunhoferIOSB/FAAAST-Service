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

import java.util.stream.Stream;


/**
 * Enum of different content options
 */
public enum Content {
    NORMAL,
    VALUE,
    TRIMMED, // defined in swagger but not specification
    REFERENCE,
    PATH;

    public static final Content DEFAULT = Content.NORMAL;

    public static Content fromString(String value) {
        return Stream.of(Content.values())
                .filter(x -> x.name().equalsIgnoreCase(value))
                .findAny()
                .orElse(Content.NORMAL);
    }
}
