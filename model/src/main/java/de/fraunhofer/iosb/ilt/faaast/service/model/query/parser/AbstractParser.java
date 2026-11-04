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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.parser;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.operand.StringToFieldIdentifierParser;

import java.util.List;


/**
 * Base class for parsers providing shared helper functionality.
 *
 * @param <I> the input type
 * @param <O> the output type
 */
public abstract class AbstractParser<I, O> implements Parser<I, O> {
    protected final StringToFieldIdentifierParser stringToFieldIdentifierParser = new StringToFieldIdentifierParser();

    /**
     * Returns whether the given list is neither null nor empty.
     *
     * @param list the list to check
     * @return true if the list is neither null nor empty, otherwise false
     */
    protected boolean notNullNorEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }

}
