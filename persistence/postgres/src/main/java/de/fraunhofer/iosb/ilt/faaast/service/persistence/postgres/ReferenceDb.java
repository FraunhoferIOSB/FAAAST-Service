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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Read/write access to the {@code <context>_reference} / {@code <context>_reference_key} /
 * {@code <context>_reference_payload} table triples. The key rows are the queryable
 * representation; the payload row stores the complete serialized Reference and is used to reconstruct the object
 * losslessly (including referredSemanticId).
 */
final class ReferenceDb {

    private ReferenceDb() {}


    /**
     * Writes a reference whose reference table uses the owner id as primary key (the {@code *_semantic_id_reference}
     * tables). Does nothing if the reference is null.
     */
    static void insertOwned(Connection connection, String refTable, String keyTable, String payloadTable, long ownerId, Reference reference) throws SQLException {
        if (reference == null) {
            return;
        }
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO " + refTable + " (id, type) VALUES (?, ?)")) {
            stmt.setLong(1, ownerId);
            stmt.setInt(2, EnumCodes.of(reference.getType()));
            stmt.executeUpdate();
        }
        insertKeys(connection, keyTable, ownerId, reference.getKeys());
        insertPayload(connection, payloadTable, ownerId, reference);
    }


    /**
     * Writes a list of references into a reference table with its own serial id and an owner column with position (the
     * {@code *_supplemental_semantic_id_reference} tables).
     */
    static void insertList(Connection connection, String refTable, String ownerColumn, String keyTable, String payloadTable, long ownerId, List<Reference> references)
            throws SQLException {
        if (references == null || references.isEmpty()) {
            return;
        }
        for (int position = 0; position < references.size(); position++) {
            Reference reference = references.get(position);
            long referenceId;
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + refTable + " (" + ownerColumn + ", position, type) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, ownerId);
                stmt.setInt(2, position);
                stmt.setInt(3, EnumCodes.of(reference.getType()));
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    referenceId = rs.getLong(1);
                }
            }
            insertKeys(connection, keyTable, referenceId, reference.getKeys());
            insertPayload(connection, payloadTable, referenceId, reference);
        }
    }


    private static void insertKeys(Connection connection, String keyTable, long referenceId, List<Key> keys) throws SQLException {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO " + keyTable + " (reference_id, position, type, value) VALUES (?, ?, ?, ?)")) {
            for (int position = 0; position < keys.size(); position++) {
                Key key = keys.get(position);
                stmt.setLong(1, referenceId);
                stmt.setInt(2, position);
                stmt.setInt(3, EnumCodes.of(key.getType()));
                stmt.setString(4, key.getValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }


    private static void insertPayload(Connection connection, String payloadTable, long referenceId, Reference reference) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO " + payloadTable + " (reference_id, parent_reference_payload) VALUES (?, ?)")) {
            stmt.setLong(1, referenceId);
            stmt.setObject(2, DbJson.write(reference), Types.OTHER);
            stmt.executeUpdate();
        }
    }


    /**
     * Reads back a single owned reference via its payload table. Returns null if not present.
     */
    static Reference readOwned(Connection connection, String payloadTable, long ownerId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT parent_reference_payload FROM " + payloadTable + " WHERE reference_id = ?")) {
            stmt.setLong(1, ownerId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? DbJson.read(rs.getString(1), Reference.class) : null;
            }
        }
    }


    /**
     * Reads back a reference list (ordered by position) via reference and payload tables.
     */
    static List<Reference> readList(Connection connection, String refTable, String ownerColumn, String payloadTable, long ownerId) throws SQLException {
        List<Reference> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT p.parent_reference_payload FROM " + refTable + " r"
                        + " JOIN " + payloadTable + " p ON p.reference_id = r.id"
                        + " WHERE r." + ownerColumn + " = ? ORDER BY r.position")) {
            stmt.setLong(1, ownerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(DbJson.read(rs.getString(1), Reference.class));
                }
            }
        }
        return result;
    }


    /**
     * The canonical aggregate form of a reference used for indexed equality matching in SQL: the reference keys as
     * {@code (typeCode)value} joined by {@code |}. Must match the SQL aggregate produced by
     * {@code string_agg('(' || type || ')' || value, '|' ORDER BY position)} over a reference key table.
     */
    static String keyAggregate(Reference reference) {
        if (reference == null || reference.getKeys() == null || reference.getKeys().isEmpty()) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (Key key: reference.getKeys()) {
            if (result.length() > 0) {
                result.append('|');
            }
            result.append('(').append(EnumCodes.of(key.getType())).append(')').append(key.getValue());
        }
        return result.toString();
    }
}
