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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.postgres;

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.DeserializerWrapper;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;


/**
 * (De-)serialization of AAS model objects to/from the JSONB payload columns. Errors are surfaced
 * as {@link SQLException} so they integrate with the surrounding JDBC code.
 */
final class DbJson {

    private static final JsonSerializer SERIALIZER = new JsonSerializer();
    private static final DeserializerWrapper DESERIALIZER = new DeserializerWrapper();

    private DbJson() {}


    static String write(Object value) throws SQLException {
        if (value == null) {
            return null;
        }
        try {
            return SERIALIZER.write(value);
        }
        catch (SerializationException e) {
            throw new SQLException("Failed to serialize AAS model payload", e);
        }
    }


    static String writeList(Collection<?> value) throws SQLException {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return SERIALIZER.writeList(value);
        }
        catch (SerializationException e) {
            throw new SQLException("Failed to serialize AAS model payload list", e);
        }
    }


    static <T> T read(String json, Class<T> type) throws SQLException {
        // some payload columns holding a single object default to '[]' meaning "not set"
        if (json == null || json.isBlank() || "[]".equals(json.trim())) {
            return null;
        }
        try {
            return DESERIALIZER.read(json, type);
        }
        catch (DeserializationException e) {
            throw new SQLException("Failed to deserialize AAS model payload", e);
        }
    }


    static <T> List<T> readList(String json, Class<T> type) throws SQLException {
        if (json == null) {
            return List.of();
        }
        try {
            return DESERIALIZER.readList(json, type);
        }
        catch (DeserializationException e) {
            throw new SQLException("Failed to deserialize AAS model payload list", e);
        }
    }
}
