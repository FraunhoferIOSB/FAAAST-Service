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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.format;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.ElementInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import java.util.Map;


/**
 * Interface for data formats to be used for data de-/encoding across different asset connection independently of
 * underlying transport protocol.
 */
public interface Format {

    public String getMimeType();


    /**
     * Serializes a given data value as string according to the format.
     *
     * @param value the value to format
     * @return the serialized value
     * @throws AssetConnectionException if serialization fails
     */
    public String write(DataElementValue value) throws AssetConnectionException;


    /**
     * Deserializes a value from String given its type information.
     *
     * @param value the string to deserialize
     * @param elementInfo additional information for deserialization
     * @return the deserialized value
     * @throws AssetConnectionException if deserialization fails
     */
    public default DataElementValue read(String value, ElementInfo elementInfo) throws AssetConnectionException {
        String key = "query";
        return read(value, Map.of(key, elementInfo)).get(key);
    }


    /**
     * Deserializes a value from String.
     *
     * @param value the string to deserialize
     * @param query a query that specifies which part of the value input actually represents the value
     * @param typeInfo additional type information
     * @return the deserialized value
     * @throws AssetConnectionException if deserialization fails
     */
    public default DataElementValue read(String value, String query, TypeInfo<?> typeInfo) throws AssetConnectionException {
        return read(value, ElementInfo.of(query, typeInfo));
    }


    /**
     * Deserializes a set of values from String.
     *
     * @param value input string
     * @param elements details about elements to deserialize
     * @return the deserialized values
     * @throws AssetConnectionException if deserialization fails
     */
    public Map<String, DataElementValue> read(String value, Map<String, ElementInfo> elements) throws AssetConnectionException;
}
