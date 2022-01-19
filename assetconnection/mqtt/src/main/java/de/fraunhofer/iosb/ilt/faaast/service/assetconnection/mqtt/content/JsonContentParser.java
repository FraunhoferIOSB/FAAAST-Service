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

import com.jayway.jsonpath.JsonPath;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.JsonDeserializer;
import io.adminshell.aas.v3.model.DataElement;


public class JsonContentParser extends JsonDeserializer implements ContentParser {

    @Override
    public DataElementValue parseValue(String raw, Class<? extends DataElement> elementType) throws AssetConnectionException {
        try {
            // TODO implement type mapping in  core (de.fraunhofer.iosb.ilt.faaast.service.util.DataElementValueMapper)
            // and mal elementType to DataElementValue
            return this.read(raw, DataElementValue.class);
        }
        catch (DeserializationException ex) {
            throw new AssetConnectionException("parsing JSON value failed", ex);
        }
    }

    @Override
    public DataElementValue parseValueWithQuery(String raw, Class<? extends DataElement> elementType, String query) throws AssetConnectionException {
        try {
            // TODO implement type mapping in  core (de.fraunhofer.iosb.ilt.faaast.service.util.DataElementValueMapper)
            // and mal elementType to DataElementValue
            if(!query.isBlank()) {
                return this.read(JsonPath.read(raw, query).toString(), DataElementValue.class);
            } else {
                return this.read(raw, DataElementValue.class);
            }
        }
        catch (DeserializationException ex) {
            throw new AssetConnectionException("parsing JSON value failed", ex);
        }
    }
}
