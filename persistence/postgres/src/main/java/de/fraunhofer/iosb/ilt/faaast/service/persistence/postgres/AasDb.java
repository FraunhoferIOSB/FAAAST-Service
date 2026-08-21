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
import org.eclipse.digitaltwin.aas4j.v3.model.AdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.EmbeddedDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.Extension;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Resource;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultResource;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;


/**
 * Read/write access to the Asset Administration Shell tables: {@code aas}, {@code aas_payload},
 * {@code asset_information} (with specific asset ids and thumbnail) and the submodel reference tables.
 */
final class AasDb {

    private AasDb() {}


    /**
     * Returns the database id of a shell, or null if not present.
     */
    static Long findDbId(Connection connection, String aasId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id FROM " + DatabaseSchema.TABLE_AAS + " WHERE aas_id = ?")) {
            stmt.setString(1, aasId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }


    /**
     * Inserts a shell with all dependent rows. If a row for the shell id already exists, its contents are replaced but
     * the row (and thus the insertion order used for paging) is kept.
     *
     * @return the database id of the aas row
     */
    static long save(Connection connection, AssetAdministrationShell shell) throws SQLException {
        Long existing = findDbId(connection, shell.getId());
        long dbId;
        if (existing != null) {
            dbId = existing;
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE " + DatabaseSchema.TABLE_AAS + " SET id_short = ?, category = ? WHERE id = ?")) {
                stmt.setString(1, shell.getIdShort());
                stmt.setString(2, shell.getCategory());
                stmt.setLong(3, dbId);
                stmt.executeUpdate();
            }
            deleteContents(connection, dbId);
        }
        else {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_AAS + " (aas_id, id_short, category, model_type) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, shell.getId());
                stmt.setString(2, shell.getIdShort());
                stmt.setString(3, shell.getCategory());
                stmt.setInt(4, EnumCodes.MODEL_TYPE_AAS);
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    dbId = rs.getLong(1);
                }
            }
        }
        insertPayload(connection, dbId, shell);
        insertAssetInformation(connection, dbId, shell.getAssetInformation());
        insertSubmodelReferences(connection, dbId, shell.getSubmodels());
        return dbId;
    }


    private static void deleteContents(Connection connection, long dbId) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM " + DatabaseSchema.TABLE_AAS_PAYLOAD + " WHERE aas_id = " + dbId);
            stmt.execute("DELETE FROM " + DatabaseSchema.TABLE_ASSET_INFORMATION + " WHERE asset_information_id = " + dbId);
            stmt.execute("DELETE FROM " + DatabaseSchema.TABLE_AAS_SUBMODEL_REFERENCE + " WHERE aas_id = " + dbId);
        }
    }


    private static void insertPayload(Connection connection, long dbId, AssetAdministrationShell shell) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO " + DatabaseSchema.TABLE_AAS_PAYLOAD
                        + " (aas_id, description_payload, displayname_payload, administrative_information_payload,"
                        + " embedded_data_specification_payload, extensions_payload, derived_from_payload)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setLong(1, dbId);
            stmt.setObject(2, DbJson.writeList(shell.getDescription()), Types.OTHER);
            stmt.setObject(3, DbJson.writeList(shell.getDisplayName()), Types.OTHER);
            stmt.setObject(4, DbJson.write(shell.getAdministration()), Types.OTHER);
            stmt.setObject(5, DbJson.writeList(shell.getEmbeddedDataSpecifications()), Types.OTHER);
            stmt.setObject(6, DbJson.writeList(shell.getExtensions()), Types.OTHER);
            stmt.setObject(7, DbJson.write(shell.getDerivedFrom()), Types.OTHER);
            stmt.executeUpdate();
        }
    }


    private static void insertAssetInformation(Connection connection, long dbId, AssetInformation assetInformation) throws SQLException {
        if (assetInformation == null) {
            return;
        }
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO " + DatabaseSchema.TABLE_ASSET_INFORMATION
                        + " (asset_information_id, asset_kind, global_asset_id, asset_type, model_type) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setLong(1, dbId);
            if (assetInformation.getAssetKind() != null) {
                stmt.setInt(2, EnumCodes.of(assetInformation.getAssetKind()));
            }
            else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, assetInformation.getGlobalAssetId());
            stmt.setString(4, assetInformation.getAssetType());
            stmt.setInt(5, EnumCodes.MODEL_TYPE_ASSET_INFORMATION);
            stmt.executeUpdate();
        }
        if (assetInformation.getDefaultThumbnail() != null) {
            Resource thumbnail = assetInformation.getDefaultThumbnail();
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_THUMBNAIL + " (id, content_type, value) VALUES (?, ?, ?)")) {
                stmt.setLong(1, dbId);
                stmt.setString(2, thumbnail.getContentType());
                stmt.setString(3, thumbnail.getPath());
                stmt.executeUpdate();
            }
        }
        List<SpecificAssetId> specificAssetIds = assetInformation.getSpecificAssetIds();
        if (specificAssetIds != null) {
            for (int position = 0; position < specificAssetIds.size(); position++) {
                insertSpecificAssetId(connection, dbId, position, specificAssetIds.get(position));
            }
        }
    }


    private static void insertSpecificAssetId(Connection connection, long assetInformationId, int position, SpecificAssetId specificAssetId) throws SQLException {
        long rowId;
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO " + DatabaseSchema.TABLE_SPECIFIC_ASSET_ID + " (position, asset_information_id, name, value) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, position);
            stmt.setLong(2, assetInformationId);
            stmt.setString(3, specificAssetId.getName());
            stmt.setString(4, specificAssetId.getValue());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                rowId = rs.getLong(1);
            }
        }
        if (specificAssetId.getSemanticId() != null) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_SPECIFIC_ASSET_ID_PAYLOAD + " (specific_asset_id, semantic_id_payload) VALUES (?, ?)")) {
                stmt.setLong(1, rowId);
                stmt.setObject(2, DbJson.write(specificAssetId.getSemanticId()), Types.OTHER);
                stmt.executeUpdate();
            }
        }
        ReferenceDb.insertOwned(connection,
                DatabaseSchema.TABLE_SPECIFIC_ASSET_ID_EXTERNAL_SUBJECT_REF,
                DatabaseSchema.TABLE_SPECIFIC_ASSET_ID_EXTERNAL_SUBJECT_REF_KEY,
                DatabaseSchema.TABLE_SPECIFIC_ASSET_ID_EXTERNAL_SUBJECT_REF_PAYLOAD,
                rowId, specificAssetId.getExternalSubjectId());
        ReferenceDb.insertList(connection,
                DatabaseSchema.TABLE_SPECIFIC_ASSET_ID_SUPPLEMENTAL_REF, "specific_asset_id_id",
                DatabaseSchema.TABLE_SPECIFIC_ASSET_ID_SUPPLEMENTAL_REF_KEY,
                DatabaseSchema.TABLE_SPECIFIC_ASSET_ID_SUPPLEMENTAL_REF_PAYLOAD,
                rowId, specificAssetId.getSupplementalSemanticIds());
    }


    private static void insertSubmodelReferences(Connection connection, long dbId, List<Reference> references) throws SQLException {
        if (references == null || references.isEmpty()) {
            return;
        }
        for (int position = 0; position < references.size(); position++) {
            Reference reference = references.get(position);
            long referenceId;
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_AAS_SUBMODEL_REFERENCE + " (aas_id, position, type) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, dbId);
                stmt.setInt(2, position);
                stmt.setInt(3, EnumCodes.of(reference.getType()));
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    referenceId = rs.getLong(1);
                }
            }
            if (reference.getKeys() != null && !reference.getKeys().isEmpty()) {
                try (PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO " + DatabaseSchema.TABLE_AAS_SUBMODEL_REFERENCE_KEY + " (reference_id, position, type, value) VALUES (?, ?, ?, ?)")) {
                    for (int keyPosition = 0; keyPosition < reference.getKeys().size(); keyPosition++) {
                        stmt.setLong(1, referenceId);
                        stmt.setInt(2, keyPosition);
                        stmt.setInt(3, EnumCodes.of(reference.getKeys().get(keyPosition).getType()));
                        stmt.setString(4, reference.getKeys().get(keyPosition).getValue());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            }
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_AAS_SUBMODEL_REFERENCE_PAYLOAD + " (reference_id, parent_reference_payload) VALUES (?, ?)")) {
                stmt.setLong(1, referenceId);
                stmt.setObject(2, DbJson.write(reference), Types.OTHER);
                stmt.executeUpdate();
            }
        }
    }


    /**
     * Reads a shell by database id, or null if not present.
     */
    static AssetAdministrationShell read(Connection connection, long dbId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT a.id, a.aas_id, a.id_short, a.category,"
                        + " p.description_payload::text AS description, p.displayname_payload::text AS displayname,"
                        + " p.administrative_information_payload::text AS administration,"
                        + " p.embedded_data_specification_payload::text AS eds,"
                        + " p.extensions_payload::text AS extensions, p.derived_from_payload::text AS derived_from"
                        + " FROM " + DatabaseSchema.TABLE_AAS + " a"
                        + " LEFT JOIN " + DatabaseSchema.TABLE_AAS_PAYLOAD + " p ON p.aas_id = a.id"
                        + " WHERE a.id = ?")) {
            stmt.setLong(1, dbId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return buildShell(connection, rs);
            }
        }
    }


    private static AssetAdministrationShell buildShell(Connection connection, ResultSet rs) throws SQLException {
        DefaultAssetAdministrationShell shell = new DefaultAssetAdministrationShell();
        long dbId = rs.getLong("id");
        shell.setId(rs.getString("aas_id"));
        shell.setIdShort(rs.getString("id_short"));
        shell.setCategory(rs.getString("category"));
        String description = rs.getString("description");
        if (description != null) {
            shell.setDescription(new ArrayList<>(DbJson.readList(description, LangStringTextType.class)));
        }
        String displayName = rs.getString("displayname");
        if (displayName != null) {
            shell.setDisplayName(new ArrayList<>(DbJson.readList(displayName, LangStringNameType.class)));
        }
        shell.setAdministration(DbJson.read(rs.getString("administration"), AdministrativeInformation.class));
        String eds = rs.getString("eds");
        if (eds != null) {
            shell.setEmbeddedDataSpecifications(new ArrayList<>(DbJson.readList(eds, EmbeddedDataSpecification.class)));
        }
        String extensions = rs.getString("extensions");
        if (extensions != null) {
            shell.setExtensions(new ArrayList<>(DbJson.readList(extensions, Extension.class)));
        }
        shell.setDerivedFrom(DbJson.read(rs.getString("derived_from"), Reference.class));
        shell.setAssetInformation(readAssetInformation(connection, dbId));
        shell.setSubmodels(new ArrayList<>(readSubmodelReferences(connection, dbId)));
        return shell;
    }


    private static AssetInformation readAssetInformation(Connection connection, long dbId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT ai.asset_kind, ai.global_asset_id, ai.asset_type, t.content_type AS thumbnail_content_type, t.value AS thumbnail_path"
                        + " FROM " + DatabaseSchema.TABLE_ASSET_INFORMATION + " ai"
                        + " LEFT JOIN " + DatabaseSchema.TABLE_THUMBNAIL + " t ON t.id = ai.asset_information_id"
                        + " WHERE ai.asset_information_id = ?")) {
            stmt.setLong(1, dbId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                DefaultAssetInformation assetInformation = new DefaultAssetInformation();
                assetInformation.setAssetKind(EnumCodes.assetKind(rs.getObject("asset_kind", Integer.class)));
                assetInformation.setGlobalAssetId(rs.getString("global_asset_id"));
                assetInformation.setAssetType(rs.getString("asset_type"));
                String thumbnailPath = rs.getString("thumbnail_path");
                String thumbnailContentType = rs.getString("thumbnail_content_type");
                if (thumbnailPath != null || thumbnailContentType != null) {
                    DefaultResource thumbnail = new DefaultResource();
                    thumbnail.setPath(thumbnailPath);
                    thumbnail.setContentType(thumbnailContentType);
                    assetInformation.setDefaultThumbnail(thumbnail);
                }
                assetInformation.setSpecificAssetIds(new ArrayList<>(readSpecificAssetIds(connection, dbId)));
                return assetInformation;
            }
        }
    }


    private static List<SpecificAssetId> readSpecificAssetIds(Connection connection, long dbId) throws SQLException {
        List<SpecificAssetId> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT s.id, s.name, s.value, p.semantic_id_payload::text AS semantic_id, ext.parent_reference_payload::text AS external_subject_id"
                        + " FROM " + DatabaseSchema.TABLE_SPECIFIC_ASSET_ID + " s"
                        + " LEFT JOIN " + DatabaseSchema.TABLE_SPECIFIC_ASSET_ID_PAYLOAD + " p ON p.specific_asset_id = s.id"
                        + " LEFT JOIN " + DatabaseSchema.TABLE_SPECIFIC_ASSET_ID_EXTERNAL_SUBJECT_REF_PAYLOAD + " ext ON ext.reference_id = s.id"
                        + " WHERE s.asset_information_id = ? ORDER BY s.position")) {
            stmt.setLong(1, dbId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DefaultSpecificAssetId specificAssetId = new DefaultSpecificAssetId();
                    specificAssetId.setName(rs.getString("name"));
                    specificAssetId.setValue(rs.getString("value"));
                    specificAssetId.setSemanticId(DbJson.read(rs.getString("semantic_id"), Reference.class));
                    specificAssetId.setExternalSubjectId(DbJson.read(rs.getString("external_subject_id"), Reference.class));
                    List<Reference> supplemental = ReferenceDb.readList(connection,
                            DatabaseSchema.TABLE_SPECIFIC_ASSET_ID_SUPPLEMENTAL_REF, "specific_asset_id_id",
                            DatabaseSchema.TABLE_SPECIFIC_ASSET_ID_SUPPLEMENTAL_REF_PAYLOAD, rs.getLong("id"));
                    if (!supplemental.isEmpty()) {
                        specificAssetId.setSupplementalSemanticIds(new ArrayList<>(supplemental));
                    }
                    result.add(specificAssetId);
                }
            }
        }
        return result;
    }


    /**
     * Reads the submodel references of a shell in stored order.
     */
    static List<Reference> readSubmodelReferences(Connection connection, long dbId) throws SQLException {
        List<Reference> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT p.parent_reference_payload::text FROM " + DatabaseSchema.TABLE_AAS_SUBMODEL_REFERENCE + " r"
                        + " JOIN " + DatabaseSchema.TABLE_AAS_SUBMODEL_REFERENCE_PAYLOAD + " p ON p.reference_id = r.id"
                        + " WHERE r.aas_id = ? ORDER BY r.position")) {
            stmt.setLong(1, dbId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(DbJson.read(rs.getString(1), Reference.class));
                }
            }
        }
        return result;
    }
}
