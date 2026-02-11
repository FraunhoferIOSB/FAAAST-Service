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
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Database schema definition for PostgreSQL persistence.
 */
public final class DatabaseSchema {

    /** Table name for Asset Administration Shells. */
    public static final String TABLE_AAS = "aas";

    /** Table name for Submodels. */
    public static final String TABLE_SUBMODEL = "submodels";

    /** Table name for Concept Descriptions. */
    public static final String TABLE_CONCEPT_DESCRIPTION = "concept_descriptions";

    /** Table name for Operation Results. */
    public static final String TABLE_OPERATION_RESULT = "operation_results";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
    public static final String CASCADE = " CASCADE";

    private DatabaseSchema() {}


    /**
     * Creates all database tables and indexes.
     *
     * @param connection the database connection
     * @throws SQLException if a database error occurs
     */
    public static void createTables(final Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm");
            stmt.execute(getAasTableCreate());
            stmt.execute(getSubmodelsTableCreate());
            stmt.execute(getConceptDescriptionTableCreate());
            stmt.execute(getOperationResultTableCreate());
            stmt.execute(getIndexesCreate());
        }
    }


    private static String getAasTableCreate() {
        return """
                CREATE TABLE IF NOT EXISTS %s (
                    id TEXT PRIMARY KEY,
                    id_short TEXT,
                    content JSONB NOT NULL,
                    seq BIGSERIAL
                )
                """.formatted(TABLE_AAS);
    }


    private static String getSubmodelsTableCreate() {
        return """
                CREATE TABLE IF NOT EXISTS %s (
                    id TEXT PRIMARY KEY,
                    id_short TEXT,
                    semantic_id TEXT,
                    content JSONB NOT NULL,
                    seq BIGSERIAL
                )
                """.formatted(TABLE_SUBMODEL);
    }


    private static String getConceptDescriptionTableCreate() {
        return """
                CREATE TABLE IF NOT EXISTS %s (
                    id TEXT PRIMARY KEY,
                    id_short TEXT,
                    content JSONB NOT NULL,
                    seq BIGSERIAL
                )
                """.formatted(TABLE_CONCEPT_DESCRIPTION);
    }


    private static String getOperationResultTableCreate() {
        return """
                CREATE TABLE IF NOT EXISTS %s (
                    id TEXT PRIMARY KEY,
                    content JSONB NOT NULL,
                    seq BIGSERIAL
                )
                """.formatted(TABLE_OPERATION_RESULT);
    }


    private static String getIndexesCreate() {
        return """
                CREATE INDEX IF NOT EXISTS idx_aas_id_short
                    ON %s(id_short);
                CREATE INDEX IF NOT EXISTS idx_submodel_id_short
                    ON %s(id_short);
                CREATE INDEX IF NOT EXISTS idx_submodel_semantic_id
                    ON %s USING GIST (semantic_id gist_trgm_ops);
                CREATE INDEX IF NOT EXISTS idx_concept_description_id_short
                    ON %s(id_short);
                """.formatted(
                TABLE_AAS,
                TABLE_SUBMODEL,
                TABLE_SUBMODEL,
                TABLE_CONCEPT_DESCRIPTION);
    }


    /**
     * Drops all database tables.
     *
     * @param connection the database connection
     * @throws SQLException if a database error occurs
     */
    public static void dropTables(final Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(DROP_TABLE + TABLE_OPERATION_RESULT
                    + CASCADE);
            stmt.execute(DROP_TABLE + TABLE_CONCEPT_DESCRIPTION
                    + CASCADE);
            stmt.execute(DROP_TABLE + TABLE_SUBMODEL + CASCADE);
            stmt.execute(DROP_TABLE + TABLE_AAS + CASCADE);
        }
    }
}
