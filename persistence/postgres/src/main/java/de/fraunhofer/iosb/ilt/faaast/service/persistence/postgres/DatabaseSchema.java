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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


/**
 * Database schema management for the PostgreSQL persistence.
 *
 * <p>
 * The schema is defined by the classpath resource {@code faaast/schema/base.sql}
 */
public final class DatabaseSchema {

    /** Table name for Asset Administration Shells. */
    public static final String TABLE_AAS = "aas";
    public static final String TABLE_AAS_PAYLOAD = "aas_payload";
    public static final String TABLE_ASSET_INFORMATION = "asset_information";
    public static final String TABLE_AAS_SUBMODEL_REFERENCE = "aas_submodel_reference";
    public static final String TABLE_AAS_SUBMODEL_REFERENCE_KEY = "aas_submodel_reference_key";
    public static final String TABLE_AAS_SUBMODEL_REFERENCE_PAYLOAD = "aas_submodel_reference_payload";
    public static final String TABLE_THUMBNAIL = "thumbnail_file_element";
    public static final String TABLE_SPECIFIC_ASSET_ID = "specific_asset_id";
    public static final String TABLE_SPECIFIC_ASSET_ID_PAYLOAD = "specific_asset_id_payload";
    public static final String TABLE_SPECIFIC_ASSET_ID_EXTERNAL_SUBJECT_REF = "specific_asset_id_external_subject_id_reference";
    public static final String TABLE_SPECIFIC_ASSET_ID_EXTERNAL_SUBJECT_REF_KEY = "specific_asset_id_external_subject_id_reference_key";
    public static final String TABLE_SPECIFIC_ASSET_ID_EXTERNAL_SUBJECT_REF_PAYLOAD = "specific_asset_id_external_subject_id_reference_payload";
    public static final String TABLE_SPECIFIC_ASSET_ID_SUPPLEMENTAL_REF = "specific_asset_id_supplemental_semantic_id_reference";
    public static final String TABLE_SPECIFIC_ASSET_ID_SUPPLEMENTAL_REF_KEY = "specific_asset_id_supplemental_semantic_id_reference_key";
    public static final String TABLE_SPECIFIC_ASSET_ID_SUPPLEMENTAL_REF_PAYLOAD = "specific_asset_id_supplemental_semantic_id_reference_payload";

    /** Table name for Submodels. */
    public static final String TABLE_SUBMODEL = "submodel";
    public static final String TABLE_SUBMODEL_PAYLOAD = "submodel_payload";
    public static final String TABLE_SUBMODEL_SEMANTIC_ID_REF = "submodel_semantic_id_reference";
    public static final String TABLE_SUBMODEL_SEMANTIC_ID_REF_KEY = "submodel_semantic_id_reference_key";
    public static final String TABLE_SUBMODEL_SEMANTIC_ID_REF_PAYLOAD = "submodel_semantic_id_reference_payload";
    public static final String TABLE_SUBMODEL_SUPPLEMENTAL_REF = "submodel_supplemental_semantic_id_reference";
    public static final String TABLE_SUBMODEL_SUPPLEMENTAL_REF_KEY = "submodel_supplemental_semantic_id_reference_key";
    public static final String TABLE_SUBMODEL_SUPPLEMENTAL_REF_PAYLOAD = "submodel_supplemental_semantic_id_reference_payload";

    /** Table name for submodel elements (one row per element at any depth). */
    public static final String TABLE_SUBMODEL_ELEMENT = "submodel_element";
    public static final String TABLE_SUBMODEL_ELEMENT_PAYLOAD = "submodel_element_payload";
    public static final String TABLE_SUBMODEL_ELEMENT_SEMANTIC_ID_REF = "submodel_element_semantic_id_reference";
    public static final String TABLE_SUBMODEL_ELEMENT_SEMANTIC_ID_REF_KEY = "submodel_element_semantic_id_reference_key";
    public static final String TABLE_SUBMODEL_ELEMENT_SEMANTIC_ID_REF_PAYLOAD = "submodel_element_semantic_id_reference_payload";
    public static final String TABLE_SUBMODEL_ELEMENT_SUPPLEMENTAL_REF = "submodel_element_supplemental_semantic_id_reference";
    public static final String TABLE_SUBMODEL_ELEMENT_SUPPLEMENTAL_REF_KEY = "submodel_element_supplemental_semantic_id_reference_key";
    public static final String TABLE_SUBMODEL_ELEMENT_SUPPLEMENTAL_REF_PAYLOAD = "submodel_element_supplemental_semantic_id_reference_payload";

    public static final String TABLE_PROPERTY = "property_element";
    public static final String TABLE_PROPERTY_PAYLOAD = "property_element_payload";
    public static final String TABLE_MULTILANGUAGE_PROPERTY_VALUE = "multilanguage_property_value";
    public static final String TABLE_MULTILANGUAGE_PROPERTY_PAYLOAD = "multilanguage_property_payload";
    public static final String TABLE_BLOB = "blob_element";
    public static final String TABLE_FILE = "file_element";
    public static final String TABLE_RANGE = "range_element";
    public static final String TABLE_REFERENCE_ELEMENT = "reference_element";
    public static final String TABLE_RELATIONSHIP_ELEMENT = "relationship_element";
    public static final String TABLE_ANNOTATED_RELATIONSHIP_ELEMENT = "annotated_relationship_element";
    public static final String TABLE_SUBMODEL_ELEMENT_COLLECTION = "submodel_element_collection";
    public static final String TABLE_SUBMODEL_ELEMENT_LIST = "submodel_element_list";
    public static final String TABLE_ENTITY = "entity_element";
    public static final String TABLE_OPERATION = "operation_element";
    public static final String TABLE_BASIC_EVENT = "basic_event_element";
    public static final String TABLE_CAPABILITY = "capability_element";

    /** Table name for Concept Descriptions. */
    public static final String TABLE_CONCEPT_DESCRIPTION = "concept_description";

    /** Table name for Operation Results. */
    public static final String TABLE_OPERATION_RESULT = "operation_result";

    /** Schema version bookkeeping table. */
    public static final String TABLE_SYSTEM = "faaast_schema_version";

    private static final String RESOURCE_BASE = "/faaast/schema/base.sql";
    private static final List<String> PATCH_VERSIONS = List.of();

    private DatabaseSchema() {}


    /**
     * Creates or upgrades the database schema.
     *
     * @param connection the database connection
     * @throws SQLException if a database error occurs
     */
    public static void createSchema(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS %s (
                        identifier BIGSERIAL PRIMARY KEY,
                        schema_version VARCHAR NOT NULL,
                        applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                    )
                    """.formatted(TABLE_SYSTEM));
            String currentVersion = readSchemaVersion(stmt);
            if (currentVersion == null) {
                stmt.execute(loadScript(RESOURCE_BASE));
                stmt.execute("INSERT INTO " + TABLE_SYSTEM + " (schema_version) VALUES ('v1.0.0')");
                currentVersion = "1.0.0";
            }
        }
    }


    private static String readSchemaVersion(Statement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery("SELECT schema_version FROM " + TABLE_SYSTEM + " ORDER BY identifier DESC LIMIT 1")) {
            if (rs.next()) {
                String version = rs.getString(1);
                return version != null && version.startsWith("v") ? version.substring(1) : version;
            }
            return null;
        }
    }


    private static int compareVersions(String a, String b) {
        String[] partsA = a.split("\\.");
        String[] partsB = b.split("\\.");
        for (int i = 0; i < Math.max(partsA.length, partsB.length); i++) {
            int valueA = i < partsA.length ? Integer.parseInt(partsA[i]) : 0;
            int valueB = i < partsB.length ? Integer.parseInt(partsB[i]) : 0;
            if (valueA != valueB) {
                return Integer.compare(valueA, valueB);
            }
        }
        return 0;
    }


    private static String loadScript(String resource) throws SQLException {
        try (InputStream in = DatabaseSchema.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new SQLException("Missing schema resource: " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new SQLException("Failed to read schema resource: " + resource, e);
        }
    }


    /**
     * Removes all data written by this persistence implementation (but keeps the schema). Tables that only reference
     * the truncated tables are emptied by the cascade.
     *
     * @param connection the database connection
     * @throws SQLException if a database error occurs
     */
    public static void clearData(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("TRUNCATE TABLE "
                    + String.join(", ",
                            TABLE_AAS,
                            TABLE_SUBMODEL,
                            TABLE_CONCEPT_DESCRIPTION,
                            TABLE_OPERATION_RESULT)
                    + " CASCADE");
        }
    }
}
