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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.JsonSerializer;


public class JsonContentSerializer implements ContentSerializer {

    private JsonSerializer serializer;

    public JsonContentSerializer() {
        this.serializer = new JsonSerializer();
    }


    @Override
    public String write(DataElementValue value, String query) throws AssetConnectionException {
        try {
            if (query != null) {
                throw new UnsupportedOperationException("writing JSON with JSON path is not supported");
            }
            return serializer.write(value);
        }
        catch (SerializationException ex) {
            throw new AssetConnectionException("serializing value to JSON failed", ex);
        }
    }
}
