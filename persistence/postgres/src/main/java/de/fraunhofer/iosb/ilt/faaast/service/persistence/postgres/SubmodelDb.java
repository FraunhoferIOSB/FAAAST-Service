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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.EmbeddedDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.Extension;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.ModellingKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;


/**
 * Read/write access to the submodel tables: {@code submodel}, {@code submodel_payload}, the
 * semantic id and supplemental semantic id reference tables and the submodel element tree.
 */
final class SubmodelDb {

    private SubmodelDb() {}


    /**
     * Returns the database id of a submodel, or null if not present.
     */
    static Long findDbId(Connection connection, String submodelIdentifier) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id FROM " + DatabaseSchema.TABLE_SUBMODEL + " WHERE submodel_identifier = ?")) {
            stmt.setString(1, submodelIdentifier);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }


    /**
     * Inserts a submodel with all dependent rows. If a row for the submodel identifier already exists, its contents
     * are replaced but the row (and thus the insertion order used for paging) is kept.
     *
     * @return the database id of the submodel row
     */
    static long save(Connection connection, Submodel submodel) throws SQLException {
        Long existing = findDbId(connection, submodel.getId());
        long dbId;
        if (existing != null) {
            dbId = existing;
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE " + DatabaseSchema.TABLE_SUBMODEL + " SET id_short = ?, category = ?, kind = ? WHERE id = ?")) {
                stmt.setString(1, submodel.getIdShort());
                stmt.setString(2, submodel.getCategory());
                setKind(stmt, 3, submodel.getKind());
                stmt.setLong(4, dbId);
                stmt.executeUpdate();
            }
            deleteContents(connection, dbId);
        }
        else {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_SUBMODEL + " (submodel_identifier, id_short, category, kind) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, submodel.getId());
                stmt.setString(2, submodel.getIdShort());
                stmt.setString(3, submodel.getCategory());
                setKind(stmt, 4, submodel.getKind());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    dbId = rs.getLong(1);
                }
            }
        }
        insertPayload(connection, dbId, submodel);
        ReferenceDb.insertOwned(connection,
                DatabaseSchema.TABLE_SUBMODEL_SEMANTIC_ID_REF,
                DatabaseSchema.TABLE_SUBMODEL_SEMANTIC_ID_REF_KEY,
                DatabaseSchema.TABLE_SUBMODEL_SEMANTIC_ID_REF_PAYLOAD,
                dbId, submodel.getSemanticId());
        ReferenceDb.insertList(connection,
                DatabaseSchema.TABLE_SUBMODEL_SUPPLEMENTAL_REF, "submodel_id",
                DatabaseSchema.TABLE_SUBMODEL_SUPPLEMENTAL_REF_KEY,
                DatabaseSchema.TABLE_SUBMODEL_SUPPLEMENTAL_REF_PAYLOAD,
                dbId, submodel.getSupplementalSemanticIds());
        SubmodelElementDb.insertTrees(connection, dbId, null, null, false, "", 0, 0, submodel.getSubmodelElements());
        return dbId;
    }


    private static void setKind(PreparedStatement stmt, int index, ModellingKind kind) throws SQLException {
        if (kind != null) {
            stmt.setInt(index, EnumCodes.of(kind));
        }
        else {
            stmt.setNull(index, Types.INTEGER);
        }
    }


    private static void insertPayload(Connection connection, long dbId, Submodel submodel) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO " + DatabaseSchema.TABLE_SUBMODEL_PAYLOAD
                        + " (submodel_id, description_payload, displayname_payload, administrative_information_payload,"
                        + " embedded_data_specification_payload, supplemental_semantic_ids_payload, extensions_payload, qualifiers_payload)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setLong(1, dbId);
            stmt.setObject(2, DbJson.writeList(submodel.getDescription()), Types.OTHER);
            stmt.setObject(3, DbJson.writeList(submodel.getDisplayName()), Types.OTHER);
            stmt.setObject(4, DbJson.write(submodel.getAdministration()), Types.OTHER);
            stmt.setObject(5, DbJson.writeList(submodel.getEmbeddedDataSpecifications()), Types.OTHER);
            stmt.setObject(6, DbJson.writeList(submodel.getSupplementalSemanticIds()), Types.OTHER);
            stmt.setObject(7, DbJson.writeList(submodel.getExtensions()), Types.OTHER);
            stmt.setObject(8, DbJson.writeList(submodel.getQualifiers()), Types.OTHER);
            stmt.executeUpdate();
        }
    }


    /**
     * Deletes all dependent rows of a submodel (payload, references, elements) but keeps the submodel row itself.
     */
    static void deleteContents(Connection connection, long dbId) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM " + DatabaseSchema.TABLE_SUBMODEL_PAYLOAD + " WHERE submodel_id = " + dbId);
            stmt.execute("DELETE FROM " + DatabaseSchema.TABLE_SUBMODEL_SEMANTIC_ID_REF + " WHERE id = " + dbId);
            stmt.execute("DELETE FROM " + DatabaseSchema.TABLE_SUBMODEL_SUPPLEMENTAL_REF + " WHERE submodel_id = " + dbId);
            stmt.execute("DELETE FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT + " WHERE submodel_id = " + dbId);
        }
    }


    /**
     * Reads a submodel (including all elements) by database id, or null if not present.
     */
    static Submodel read(Connection connection, long dbId) throws SQLException {
        List<Submodel> result = readMany(connection, List.of(dbId));
        return result.isEmpty() ? null : result.get(0);
    }


    /**
     * Reads multiple submodels (including all elements) by database id in the given order, using a constant number of
     * queries independent of the number of submodels. Ids without a matching row are skipped.
     */
    static List<Submodel> readMany(Connection connection, List<Long> dbIds) throws SQLException {
        if (dbIds.isEmpty()) {
            return List.of();
        }
        Map<Long, Submodel> byDbId = new LinkedHashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT s.id, s.submodel_identifier, s.id_short, s.category, s.kind,"
                        + " p.description_payload::text AS description, p.displayname_payload::text AS displayname,"
                        + " p.administrative_information_payload::text AS administration,"
                        + " p.embedded_data_specification_payload::text AS eds,"
                        + " p.supplemental_semantic_ids_payload::text AS supplemental,"
                        + " p.extensions_payload::text AS extensions, p.qualifiers_payload::text AS qualifiers,"
                        + " sem.parent_reference_payload::text AS semantic_id"
                        + " FROM " + DatabaseSchema.TABLE_SUBMODEL + " s"
                        + " LEFT JOIN " + DatabaseSchema.TABLE_SUBMODEL_PAYLOAD + " p ON p.submodel_id = s.id"
                        + " LEFT JOIN " + DatabaseSchema.TABLE_SUBMODEL_SEMANTIC_ID_REF_PAYLOAD + " sem ON sem.reference_id = s.id"
                        + " WHERE s.id = ANY(?)")) {
            stmt.setArray(1, connection.createArrayOf("bigint", dbIds.toArray()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    byDbId.put(rs.getLong("id"), buildSubmodel(rs));
                }
            }
        }
        Map<Long, List<SubmodelElement>> elements = SubmodelElementDb.readAll(connection, List.copyOf(byDbId.keySet()));
        for (Map.Entry<Long, Submodel> entry: byDbId.entrySet()) {
            entry.getValue().setSubmodelElements(new ArrayList<>(elements.getOrDefault(entry.getKey(), List.of())));
        }
        return dbIds.stream()
                .map(byDbId::get)
                .filter(Objects::nonNull)
                .toList();
    }


    private static Submodel buildSubmodel(ResultSet rs) throws SQLException {
        DefaultSubmodel submodel = new DefaultSubmodel();
        submodel.setId(rs.getString("submodel_identifier"));
        submodel.setIdShort(rs.getString("id_short"));
        submodel.setCategory(rs.getString("category"));
        submodel.setKind(EnumCodes.modellingKind(rs.getObject("kind", Integer.class)));
        String description = rs.getString("description");
        if (description != null) {
            submodel.setDescription(new ArrayList<>(DbJson.readList(description, LangStringTextType.class)));
        }
        String displayName = rs.getString("displayname");
        if (displayName != null) {
            submodel.setDisplayName(new ArrayList<>(DbJson.readList(displayName, LangStringNameType.class)));
        }
        submodel.setAdministration(DbJson.read(rs.getString("administration"), AdministrativeInformation.class));
        String eds = rs.getString("eds");
        if (eds != null) {
            submodel.setEmbeddedDataSpecifications(new ArrayList<>(DbJson.readList(eds, EmbeddedDataSpecification.class)));
        }
        String supplemental = rs.getString("supplemental");
        if (supplemental != null) {
            submodel.setSupplementalSemanticIds(new ArrayList<>(DbJson.readList(supplemental, Reference.class)));
        }
        String extensions = rs.getString("extensions");
        if (extensions != null) {
            submodel.setExtensions(new ArrayList<>(DbJson.readList(extensions, Extension.class)));
        }
        String qualifiers = rs.getString("qualifiers");
        if (qualifiers != null) {
            submodel.setQualifiers(new ArrayList<>(DbJson.readList(qualifiers, Qualifier.class)));
        }
        submodel.setSemanticId(DbJson.read(rs.getString("semantic_id"), Reference.class));
        return submodel;
    }
}
