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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection;

/**
 * Enum listing different types of argument validations. {@code NONE} indicates no validation is performed,
 * {@code REQUIRE_PRESENT} indicates that all arguments are required to be explicitely passed/present, and
 * {@code REQUIRE_PRESENT_OR_DEFAULT} that all arguments must either be present or have a default value set on the
 * argument definition.
 */
public enum ArgumentValidationMode {
    NONE,
    REQUIRE_PRESENT,
    REQUIRE_PRESENT_OR_DEFAULT;

    public static final ArgumentValidationMode DEFAULT = REQUIRE_PRESENT_OR_DEFAULT;
}
