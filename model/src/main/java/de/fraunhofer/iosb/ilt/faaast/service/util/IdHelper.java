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

import java.util.UUID;


/**
 * Helper class for working with ids.
 */
public class IdHelper {

    private IdHelper() {}


    /**
     * Generates a random id based on UUID without '-'.
     *
     * @return a random id
     */
    public static String randomId() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    /**
     * Generates a random id with based on UUID without '-' with the pattern '[prefix]_[random id]'.
     *
     * @param prefix the prefix to use
     * @return a random id
     */
    public static String randomId(String prefix) {
        return String.format("%s_%s", prefix, randomId());
    }
}
