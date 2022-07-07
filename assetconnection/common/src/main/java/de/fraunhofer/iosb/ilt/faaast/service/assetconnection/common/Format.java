/*
 * Copyright 2022 Fraunhofer IOSB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import java.util.Map;

public interface Format {
    
    public String write(DataElementValue value) throws AssetConnectionException;
    public default DataElementValue read(String value, String query) throws AssetConnectionException {
        String key = "";
        return read(value, Map.of(key, query)).get(key);
    }
    
    public Map<String, DataElementValue> read(String value, Map<String, String> queries) throws AssetConnectionException;
}
