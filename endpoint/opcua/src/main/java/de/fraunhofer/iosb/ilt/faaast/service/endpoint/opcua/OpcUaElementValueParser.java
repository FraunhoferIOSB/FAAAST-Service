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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValueParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tino Bischoff
 */
@SuppressWarnings("rawtypes")
public class OpcUaElementValueParser implements ElementValueParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaElementValueParser.class);

    /**
     * Parse the given raw value.
     * Here the raw value is already an ElementValue
     * 
     * @param raw The desire raw value
     * @param type The type of the old value.
     * @return The corresponding ElementValue
     */
    @Override
    public ElementValue parse(Object raw, Class type) {
        if (raw == null) {
            throw new IllegalArgumentException("raw == null");
        }

        ElementValue retval = null;

        if (raw instanceof ElementValue) {
            retval = (ElementValue) raw;
        }
        else {
            LOGGER.warn("parse: invalid raw value");
            throw new IllegalArgumentException("raw not an ElementValue");
        }

        return retval;
    }

}
