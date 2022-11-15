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
package de.fraunhofer.iosb.ilt.faaast.service.model.value;

/**
 * Encapsulates functionality to parse an element value from any kind of user input.
 *
 * @param <T> type of input
 */
public interface ElementValueParser<T> {

    /**
     * Default parser that is the identity function, i.e. input already matches expected output type
     */
    public static ElementValueParser<ElementValue> DEFAULT = new ElementValueParser<ElementValue>() {
        @Override
        public <U extends ElementValue> U parse(ElementValue raw, Class<U> type) {
            return (U) raw;
        }
    };

    /**
     * Converts a raw value to an element value.
     *
     * @param <U> expected result type
     * @param raw input to convert
     * @param type expected result type
     * @return raw input converted to element value
     * @throws Exception if conversion fails
     */
    public <U extends ElementValue> U parse(T raw, Class<U> type) throws Exception;
}
