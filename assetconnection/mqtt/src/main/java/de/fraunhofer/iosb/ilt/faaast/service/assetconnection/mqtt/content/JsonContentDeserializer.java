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
import de.fraunhofer.iosb.ilt.faaast.service.model.valuedata.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.valuedata.values.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.JsonDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;


public class JsonContentDeserializer implements ContentDeserializer {

    private final JsonDeserializer deserializer;

    public JsonContentDeserializer() {
        this.deserializer = new JsonDeserializer();
    }


    @Override
    public DataElementValue read(String raw, String query, TypeInfo typeInfo) throws AssetConnectionException {
        try {
            String value = raw;
            if (query != null) {
                try {
                    value = JsonPath.read(value, query).toString();

                    if (typeInfo != null
                            && ElementValueTypeInfo.class.isAssignableFrom(typeInfo.getClass())
                            && ((ElementValueTypeInfo) typeInfo).getDatatype() == Datatype.String) {
                        value = "\"" + value + "\"";
                    }
                }
                catch (RuntimeException ex) {
                    throw new AssetConnectionException(String.format("invalid JSON path expression '%s'", query), ex);
                }
            }
            return deserializer.readValue(value, typeInfo);
        }
        catch (DeserializationException ex) {
            throw new AssetConnectionException("parsing JSON value failed", ex);
        }
    }
}
