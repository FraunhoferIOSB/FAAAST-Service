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

    /**
     * Table name for the submodel element index. Holds one row per submodel element (at any depth), extracted from the
     * JSONB submodel documents. The documents remain the source of truth; this table is a queryable index kept in sync
     * by a database trigger on every write to the submodels table.
     */
    public static final String TABLE_SUBMODEL_ELEMENT_INDEX = "submodel_element_index";

    /**
     * Table name for externalized Blob element values. Blob content is stored here (raw, not base64) instead of
     * inside the submodel JSONB documents; the document keeps a small placeholder in the Blob's value. Rows are
     * removed by the index trigger when their placeholder disappears from the document, and by FK cascade when the
     * submodel is deleted.
     */
    public static final String TABLE_BLOB_STORE = "blob_store";

    /** Column name for the idShort of an entity. */
    public static final String COLUMN_ID_SHORT = "id_short";

    /** Column name for the semanticId of an entity. */
    public static final String COLUMN_SEMANTIC_ID = "semantic_id";

    /** Column name for the jsonb path of an indexed element within its submodel document. */
    public static final String COLUMN_DOC_PATH = "doc_path";

    /** Name of the SQL function resolving an idShort path to a numeric jsonb path. */
    public static final String FUNCTION_RESOLVE_PATH = "faaast_resolve_path";

    /** Name of the SQL function computing the canonical string form of a Reference. */
    public static final String FUNCTION_REFERENCE_STRING = "faaast_reference_string";

    /** Name of the SQL function coercing a jsonb value to an array. */
    public static final String FUNCTION_JSONB_ARRAY = "faaast_jsonb_array";

    /** Name of the SQL function safely casting text to numeric. */
    public static final String FUNCTION_TRY_NUMERIC = "faaast_try_numeric";

    /** Name of the SQL function safely casting text to timestamptz. */
    public static final String FUNCTION_TRY_TIMESTAMP = "faaast_try_timestamp";

    /** Name of the trigger function (re)building the submodel element index for a submodel. */
    public static final String FUNCTION_INDEX_SUBMODEL_ELEMENTS = "faaast_index_submodel_elements";

    /** Name of the trigger on the submodels table maintaining the submodel element index. */
    public static final String TRIGGER_INDEX_SUBMODEL_ELEMENTS = "trg_faaast_index_submodel_elements";

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
            stmt.execute(getAasTableCreate());
            stmt.execute(getSubmodelsTableCreate());
            stmt.execute(getConceptDescriptionTableCreate());
            stmt.execute(getOperationResultTableCreate());
            stmt.execute(getSubmodelElementIndexTableCreate());
            stmt.execute(getBlobStoreTableCreate());
            stmt.execute(getIndexesCreate());
            stmt.execute(getResolvePathFunctionCreate());
            stmt.execute(getJsonbArrayFunctionCreate());
            stmt.execute(getReferenceStringFunctionCreate());
            stmt.execute(getTryNumericFunctionCreate());
            stmt.execute(getTryTimestampFunctionCreate());
            stmt.execute(getIndexSubmodelElementsFunctionCreate());
            stmt.execute(getIndexSubmodelElementsTriggerCreate());
        }
    }


    /**
     * Function translating an idShort path (e.g. {@code {a, b, [3]}}) into the numeric jsonb path of that element
     * within a submodel document (e.g. {@code {submodelElements, 0, value, 1, value, 3}}), as required by
     * {@code jsonb_set} and {@code #-}. Returns NULL if any step cannot be resolved, so callers can use it both to
     * address the element and to guard against missing paths in the same statement.
     */
    private static String getResolvePathFunctionCreate() {
        return """
                CREATE OR REPLACE FUNCTION %s(doc jsonb, steps text[]) RETURNS text[] AS $func$
                DECLARE
                    result text[] := ARRAY[]::text[];
                    current jsonb := doc;
                    container text := 'submodelElements';
                    step text;
                    pos int;
                    arr jsonb;
                BEGIN
                    FOREACH step IN ARRAY steps LOOP
                        arr := current -> container;
                        IF arr IS NULL OR jsonb_typeof(arr) <> 'array' THEN
                            RETURN NULL;
                        END IF;
                        IF step ~ '^\\[[0-9]+\\]$' THEN
                            pos := substring(step from 2 for length(step) - 2)::int;
                            IF pos >= jsonb_array_length(arr) THEN
                                RETURN NULL;
                            END IF;
                        ELSE
                            SELECT (o.ord - 1)::int INTO pos
                            FROM jsonb_array_elements(arr) WITH ORDINALITY AS o(elem, ord)
                            WHERE o.elem ->> 'idShort' = step
                            LIMIT 1;
                            IF pos IS NULL THEN
                                RETURN NULL;
                            END IF;
                        END IF;
                        result := result || container || pos::text;
                        current := arr -> pos;
                        container := 'value';
                    END LOOP;
                    RETURN result;
                END;
                $func$ LANGUAGE plpgsql IMMUTABLE
                """.formatted(FUNCTION_RESOLVE_PATH);
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


    /**
     * The submodel element index holds one row per element at any nesting depth. {@code doc_path} is the jsonb path of
     * the element within the submodel document (usable with {@code #>}), {@code ord_path} the positions only (usable
     * for document-order sorting), {@code id_short_path} the API-facing idShort path. {@code semantic_id} uses the
     * canonical string form produced by {@link #FUNCTION_REFERENCE_STRING}. The typed value columns are populated for
     * Property elements according to their valueType and enable indexed, type-correct comparisons (e.g. for the AAS
     * query language).
     */
    private static String getSubmodelElementIndexTableCreate() {
        return """
                CREATE TABLE IF NOT EXISTS %s (
                    submodel_id TEXT NOT NULL REFERENCES %s(id) ON DELETE CASCADE,
                    doc_path TEXT[] NOT NULL,
                    ord_path INT[] NOT NULL,
                    id_short_path TEXT NOT NULL,
                    id_short TEXT,
                    semantic_id TEXT,
                    supplemental_semantic_ids TEXT[],
                    model_type TEXT,
                    value_type TEXT,
                    value_text TEXT,
                    value_num NUMERIC,
                    value_bool BOOLEAN,
                    value_datetime TIMESTAMPTZ,
                    PRIMARY KEY (submodel_id, doc_path)
                )
                """.formatted(TABLE_SUBMODEL_ELEMENT_INDEX, TABLE_SUBMODEL);
    }


    /**
     * The blob store holds the content of externalized Blob element values, keyed by the placeholder string exactly
     * as it appears in the JSONB document (and thus in the value_text column of the submodel element index, which the
     * index trigger uses for orphan cleanup).
     */
    private static String getBlobStoreTableCreate() {
        return """
                CREATE TABLE IF NOT EXISTS %s (
                    submodel_id TEXT NOT NULL REFERENCES %s(id) ON DELETE CASCADE,
                    blob_id TEXT NOT NULL,
                    content BYTEA NOT NULL,
                    PRIMARY KEY (submodel_id, blob_id)
                )
                """.formatted(TABLE_BLOB_STORE, TABLE_SUBMODEL);
    }


    /**
     * Coerces a jsonb value to an array, so that {@code jsonb_array_elements} can be applied without error to values
     * that may be missing or of a different type.
     */
    private static String getJsonbArrayFunctionCreate() {
        return """
                CREATE OR REPLACE FUNCTION %s(v jsonb) RETURNS jsonb AS $func$
                    SELECT CASE WHEN jsonb_typeof(v) = 'array' THEN v ELSE '[]'::jsonb END
                $func$ LANGUAGE sql IMMUTABLE
                """.formatted(FUNCTION_JSONB_ARRAY);
    }


    /**
     * Canonical string form of a Reference given as jsonb, e.g. {@code (GlobalReference)urn:example}. Must produce the
     * same format as {@code ReferenceHelper.toString(reference, false, false)} on the Java side so values written by
     * Java code and values extracted by the index trigger compare equal. Returns NULL for missing or key-less
     * references.
     */
    private static String getReferenceStringFunctionCreate() {
        return """
                CREATE OR REPLACE FUNCTION %s(ref jsonb) RETURNS text AS $func$
                    SELECT string_agg('(' || (k.elem ->> 'type') || ')' || (k.elem ->> 'value'), ', ' ORDER BY k.ord)
                    FROM jsonb_array_elements(%s(ref -> 'keys')) WITH ORDINALITY AS k(elem, ord)
                $func$ LANGUAGE sql IMMUTABLE
                """.formatted(FUNCTION_REFERENCE_STRING, FUNCTION_JSONB_ARRAY);
    }


    private static String getTryNumericFunctionCreate() {
        return """
                CREATE OR REPLACE FUNCTION %s(v text) RETURNS numeric AS $func$
                BEGIN
                    RETURN v::numeric;
                EXCEPTION WHEN OTHERS THEN
                    RETURN NULL;
                END;
                $func$ LANGUAGE plpgsql IMMUTABLE
                """.formatted(FUNCTION_TRY_NUMERIC);
    }


    private static String getTryTimestampFunctionCreate() {
        return """
                CREATE OR REPLACE FUNCTION %s(v text) RETURNS timestamptz AS $func$
                BEGIN
                    RETURN v::timestamptz;
                EXCEPTION WHEN OTHERS THEN
                    RETURN NULL;
                END;
                $func$ LANGUAGE plpgsql IMMUTABLE
                """.formatted(FUNCTION_TRY_TIMESTAMP);
    }


    /**
     * Trigger function rebuilding the submodel element index rows for a submodel from its JSONB document. Because it
     * runs on every insert or update of the document row, the index stays consistent with all write paths (full saves
     * as well as partial jsonb_set/#- element updates) within the same transaction. The recursive walk descends into
     * the container arrays of SubmodelElementCollection/-List (value), Entity (statements) and
     * AnnotatedRelationshipElement (annotations); children of a SubmodelElementList are addressed by index ([n]) in
     * the idShort path, all others by idShort.
     */
    private static String getIndexSubmodelElementsFunctionCreate() {
        return """
                CREATE OR REPLACE FUNCTION %1$s() RETURNS trigger AS $func$
                BEGIN
                    DELETE FROM %2$s WHERE submodel_id = NEW.id;
                    INSERT INTO %2$s (
                        submodel_id, doc_path, ord_path, id_short_path, id_short, semantic_id,
                        supplemental_semantic_ids, model_type, value_type, value_text, value_num, value_bool, value_datetime)
                    WITH RECURSIVE element(elem, doc_path, ord_path, id_short_path) AS (
                        SELECT t.elem,
                               ARRAY['submodelElements', (t.ord - 1)::text],
                               ARRAY[(t.ord - 1)::int],
                               COALESCE(t.elem ->> 'idShort', '[' || (t.ord - 1)::text || ']')
                        FROM jsonb_array_elements(%3$s(NEW.content -> 'submodelElements')) WITH ORDINALITY AS t(elem, ord)
                        UNION ALL
                        SELECT c.elem,
                               e.doc_path || ARRAY[c.container, (c.ord - 1)::text],
                               e.ord_path || (c.ord - 1)::int,
                               e.id_short_path || CASE
                                   WHEN e.elem ->> 'modelType' = 'SubmodelElementList'
                                       THEN '[' || (c.ord - 1)::text || ']'
                                   ELSE '.' || COALESCE(c.elem ->> 'idShort', '[' || (c.ord - 1)::text || ']')
                               END
                        FROM element e
                        CROSS JOIN LATERAL (
                            SELECT child.elem, child.ord, ct.container
                            FROM (VALUES
                                    ('SubmodelElementCollection', 'value'),
                                    ('SubmodelElementList', 'value'),
                                    ('Entity', 'statements'),
                                    ('AnnotatedRelationshipElement', 'annotations')
                                 ) AS ct(model_type, container)
                            CROSS JOIN LATERAL jsonb_array_elements(%3$s(e.elem -> ct.container)) WITH ORDINALITY AS child(elem, ord)
                            WHERE ct.model_type = e.elem ->> 'modelType'
                        ) c
                    )
                    SELECT NEW.id,
                           e.doc_path,
                           e.ord_path,
                           e.id_short_path,
                           e.elem ->> 'idShort',
                           %4$s(e.elem -> 'semanticId'),
                           (SELECT array_agg(%4$s(s.elem))
                            FROM jsonb_array_elements(%3$s(e.elem -> 'supplementalSemanticIds')) AS s(elem)),
                           e.elem ->> 'modelType',
                           e.elem ->> 'valueType',
                           v.text_value,
                           CASE WHEN e.elem ->> 'valueType' IN (
                                    'xs:byte', 'xs:decimal', 'xs:double', 'xs:float', 'xs:int', 'xs:integer', 'xs:long',
                                    'xs:negativeInteger', 'xs:nonNegativeInteger', 'xs:nonPositiveInteger',
                                    'xs:positiveInteger', 'xs:short', 'xs:unsignedByte', 'xs:unsignedInt',
                                    'xs:unsignedLong', 'xs:unsignedShort')
                                THEN %5$s(v.text_value) END,
                           CASE WHEN e.elem ->> 'valueType' = 'xs:boolean' AND lower(v.text_value) IN ('true', 'false', '1', '0')
                                THEN lower(v.text_value) IN ('true', '1') END,
                           CASE WHEN e.elem ->> 'valueType' IN ('xs:date', 'xs:dateTime')
                                THEN %6$s(v.text_value) END
                    FROM element e
                    CROSS JOIN LATERAL (
                        SELECT CASE WHEN jsonb_typeof(e.elem -> 'value') = 'string' THEN e.elem ->> 'value' END AS text_value
                    ) v;
                    DELETE FROM %7$s b
                    WHERE b.submodel_id = NEW.id
                      AND NOT EXISTS (
                          SELECT 1 FROM %2$s i
                          WHERE i.submodel_id = NEW.id
                            AND i.model_type = 'Blob'
                            AND i.value_text = b.blob_id);
                    RETURN NULL;
                END;
                $func$ LANGUAGE plpgsql
                """.formatted(
                FUNCTION_INDEX_SUBMODEL_ELEMENTS,
                TABLE_SUBMODEL_ELEMENT_INDEX,
                FUNCTION_JSONB_ARRAY,
                FUNCTION_REFERENCE_STRING,
                FUNCTION_TRY_NUMERIC,
                FUNCTION_TRY_TIMESTAMP,
                TABLE_BLOB_STORE);
    }


    private static String getIndexSubmodelElementsTriggerCreate() {
        return """
                DROP TRIGGER IF EXISTS %1$s ON %2$s;
                CREATE TRIGGER %1$s
                    AFTER INSERT OR UPDATE ON %2$s
                    FOR EACH ROW EXECUTE FUNCTION %3$s();
                """.formatted(
                TRIGGER_INDEX_SUBMODEL_ELEMENTS,
                TABLE_SUBMODEL,
                FUNCTION_INDEX_SUBMODEL_ELEMENTS);
    }


    private static String getIndexesCreate() {
        return """
                CREATE INDEX IF NOT EXISTS idx_aas_id_short
                    ON %1$s(id_short);
                CREATE INDEX IF NOT EXISTS idx_aas_seq
                    ON %1$s(seq);
                CREATE INDEX IF NOT EXISTS idx_aas_global_asset_id
                    ON %1$s((content #>> '{assetInformation,globalAssetId}'));
                CREATE INDEX IF NOT EXISTS idx_aas_specific_asset_ids
                    ON %1$s USING GIN ((content #> '{assetInformation,specificAssetIds}') jsonb_path_ops);
                CREATE INDEX IF NOT EXISTS idx_submodel_id_short
                    ON %2$s(id_short);
                CREATE INDEX IF NOT EXISTS idx_submodel_semantic_id
                    ON %2$s(semantic_id);
                CREATE INDEX IF NOT EXISTS idx_submodel_seq
                    ON %2$s(seq);
                CREATE INDEX IF NOT EXISTS idx_concept_description_id_short
                    ON %3$s(id_short);
                CREATE INDEX IF NOT EXISTS idx_concept_description_seq
                    ON %3$s(seq);
                CREATE INDEX IF NOT EXISTS idx_sme_semantic_id
                    ON %4$s(semantic_id) WHERE semantic_id IS NOT NULL;
                CREATE INDEX IF NOT EXISTS idx_sme_supplemental_semantic_ids
                    ON %4$s USING GIN (supplemental_semantic_ids);
                CREATE INDEX IF NOT EXISTS idx_sme_id_short
                    ON %4$s(id_short) WHERE id_short IS NOT NULL;
                CREATE INDEX IF NOT EXISTS idx_sme_value_text
                    ON %4$s(value_text) WHERE value_text IS NOT NULL;
                CREATE INDEX IF NOT EXISTS idx_sme_value_num
                    ON %4$s(value_num) WHERE value_num IS NOT NULL;
                CREATE INDEX IF NOT EXISTS idx_sme_value_datetime
                    ON %4$s(value_datetime) WHERE value_datetime IS NOT NULL;
                """.formatted(
                TABLE_AAS,
                TABLE_SUBMODEL,
                TABLE_CONCEPT_DESCRIPTION,
                TABLE_SUBMODEL_ELEMENT_INDEX);
    }


    /**
     * Drops all database tables.
     *
     * @param connection the database connection
     * @throws SQLException if a database error occurs
     */
    public static void dropTables(final Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(DROP_TABLE + TABLE_BLOB_STORE + CASCADE);
            stmt.execute(DROP_TABLE + TABLE_SUBMODEL_ELEMENT_INDEX + CASCADE);
            stmt.execute(DROP_TABLE + TABLE_OPERATION_RESULT
                    + CASCADE);
            stmt.execute(DROP_TABLE + TABLE_CONCEPT_DESCRIPTION
                    + CASCADE);
            stmt.execute(DROP_TABLE + TABLE_SUBMODEL + CASCADE);
            stmt.execute(DROP_TABLE + TABLE_AAS + CASCADE);
            stmt.execute("DROP FUNCTION IF EXISTS " + FUNCTION_RESOLVE_PATH + "(jsonb, text[])");
            stmt.execute("DROP FUNCTION IF EXISTS " + FUNCTION_INDEX_SUBMODEL_ELEMENTS + "()");
            stmt.execute("DROP FUNCTION IF EXISTS " + FUNCTION_REFERENCE_STRING + "(jsonb)");
            stmt.execute("DROP FUNCTION IF EXISTS " + FUNCTION_JSONB_ARRAY + "(jsonb)");
            stmt.execute("DROP FUNCTION IF EXISTS " + FUNCTION_TRY_NUMERIC + "(text)");
            stmt.execute("DROP FUNCTION IF EXISTS " + FUNCTION_TRY_TIMESTAMP + "(text)");
        }
    }
}
