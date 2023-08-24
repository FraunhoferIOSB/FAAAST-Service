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

import java.util.Arrays;
import java.util.stream.Stream;


/**
 * Utility class helping with streams.
 */
public class StreamHelper {

    private StreamHelper() {}


    /**
     * Creates a stream of the given elements or an empty stream if agument is null or empty.
     *
     * @param <T> type of the elements
     * @param values values to convert to stream
     * @return a stream of the given elements
     */
    public static <T> Stream<T> toStream(T... values) {
        return Arrays.asList(values).stream();
    }
}
