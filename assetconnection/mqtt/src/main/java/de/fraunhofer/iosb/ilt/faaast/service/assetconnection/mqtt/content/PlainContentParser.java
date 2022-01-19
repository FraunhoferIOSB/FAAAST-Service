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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.content;

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import io.adminshell.aas.v3.model.DataElement;
import io.adminshell.aas.v3.model.Property;


public class PlainContentParser implements ContentParser {

    @Override
    public DataElementValue parseValue(String raw, Class<? extends DataElement> elementType) {
        if (Property.class.isAssignableFrom(elementType)) {
            PropertyValue result = new PropertyValue();
            result.setValue(raw);
            return result;
        }
        throw new UnsupportedOperationException(String.format("error parsing value - unsupported element type (%s)", elementType.getSimpleName()));
    }


    @Override
    public DataElementValue parseValueWithQuery(String raw, Class<? extends DataElement> elementType, String query) {
        return parseValue(raw, elementType);
    }

}
