/*
 * Copyright (C) 2026 the Eclipse BaSyx Authors and Fraunhofer IESE
 * Copyright (c) 2026 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 *
 * Derived from the Eclipse BaSyx Go components database schema
 * (https://github.com/eclipse-basyx/basyx-go-components), modified for FA³ST.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * SPDX-License-Identifier: MIT
 */

-- FA³ST PostgreSQL persistence schema, version 1.0.0.
--
-- Conventions:
--   * Identifiables (aas, submodel, concept_description) have one row in a main table with the
--     queryable scalar attributes; nested structures that are only read back as a whole are stored
--     as JSONB in a companion <table>_payload table.
--   * References are stored as a triple of tables: <context>_reference (owner + reference type),
--     <context>_reference_key (one row per key, queryable) and <context>_reference_payload (the
--     complete serialized Reference for lossless reconstruction).
--   * Enum values are stored as integer codes, see EnumCodes.java.
--   * Every table has db_created_at/db_updated_at bookkeeping columns; db_updated_at is maintained
--     by the trigger installed at the end of this script.

-- ------------------------------------------
-- Extensions
-- ------------------------------------------
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ------------------------------------------
-- Asset Administration Shells
-- ------------------------------------------

CREATE TABLE IF NOT EXISTS aas (
  id BIGSERIAL PRIMARY KEY,
  aas_id varchar(2048) UNIQUE NOT NULL,
  id_short varchar(128),
  category varchar(128),
  model_type int NOT NULL DEFAULT 3,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aas_payload (
  aas_id BIGINT PRIMARY KEY REFERENCES aas(id) ON DELETE CASCADE,
  description_payload JSONB,
  displayname_payload JSONB,
  administrative_information_payload JSONB,
  embedded_data_specification_payload JSONB,
  extensions_payload JSONB,
  derived_from_payload JSONB,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS asset_information (
  asset_information_id BIGINT PRIMARY KEY REFERENCES aas(id) ON DELETE CASCADE,
  asset_kind int,
  global_asset_id varchar(2048),
  asset_type varchar(2048),
  model_type int NOT NULL DEFAULT 4,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aas_submodel_reference (
  id BIGSERIAL PRIMARY KEY,
  aas_id BIGINT NOT NULL REFERENCES aas(id) ON DELETE CASCADE,
  position INTEGER NOT NULL,
  type int NOT NULL,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aas_submodel_reference_key (
  id           BIGSERIAL PRIMARY KEY,
  reference_id BIGINT NOT NULL REFERENCES aas_submodel_reference(id) ON DELETE CASCADE,
  position     INTEGER NOT NULL,
  type         int NOT NULL,
  value        TEXT NOT NULL,
  UNIQUE(reference_id, position),
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS aas_submodel_reference_payload (
  id           BIGSERIAL PRIMARY KEY,
  reference_id BIGINT NOT NULL REFERENCES aas_submodel_reference(id) ON DELETE CASCADE,
  parent_reference_payload JSONB NOT NULL,
  UNIQUE(reference_id),
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS thumbnail_file_element (
  id           BIGINT PRIMARY KEY REFERENCES asset_information(asset_information_id) ON DELETE CASCADE,
  content_type TEXT,
  file_name    TEXT,
  value        TEXT,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS specific_asset_id (
  id BIGSERIAL PRIMARY KEY,
  position INTEGER NOT NULL,
  asset_information_id BIGINT NOT NULL REFERENCES asset_information(asset_information_id) ON DELETE CASCADE,
  name VARCHAR(64) NOT NULL,
  value VARCHAR(2048) NOT NULL,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS specific_asset_id_payload (
  specific_asset_id BIGINT PRIMARY KEY REFERENCES specific_asset_id(id) ON DELETE CASCADE,
  semantic_id_payload JSONB DEFAULT '[]',
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS specific_asset_id_external_subject_id_reference (
  id   BIGINT PRIMARY KEY REFERENCES specific_asset_id(id) ON DELETE CASCADE,
  type int NOT NULL,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS specific_asset_id_external_subject_id_reference_key (
  id           BIGSERIAL PRIMARY KEY,
  reference_id BIGINT NOT NULL REFERENCES specific_asset_id_external_subject_id_reference(id) ON DELETE CASCADE,
  position     INTEGER NOT NULL,
  type         int NOT NULL,
  value        TEXT NOT NULL,
  UNIQUE(reference_id, position),
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS specific_asset_id_external_subject_id_reference_payload (
  id           BIGSERIAL PRIMARY KEY,
  reference_id BIGINT NOT NULL REFERENCES specific_asset_id_external_subject_id_reference(id) ON DELETE CASCADE,
  parent_reference_payload JSONB NOT NULL,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS specific_asset_id_supplemental_semantic_id_reference (
  id BIGSERIAL PRIMARY KEY,
  specific_asset_id_id BIGINT NOT NULL REFERENCES specific_asset_id(id) ON DELETE CASCADE,
  position INTEGER NOT NULL,
  type int NOT NULL,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS specific_asset_id_supplemental_semantic_id_reference_key (
  id           BIGSERIAL PRIMARY KEY,
  reference_id BIGINT NOT NULL REFERENCES specific_asset_id_supplemental_semantic_id_reference(id) ON DELETE CASCADE,
  position     INTEGER NOT NULL,
  type         int NOT NULL,
  value        TEXT NOT NULL,
  UNIQUE(reference_id, position),
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS specific_asset_id_supplemental_semantic_id_reference_payload (
  id           BIGSERIAL PRIMARY KEY,
  reference_id BIGINT NOT NULL REFERENCES specific_asset_id_supplemental_semantic_id_reference(id) ON DELETE CASCADE,
  parent_reference_payload JSONB NOT NULL,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ------------------------------------------
-- Submodels
-- ------------------------------------------

CREATE TABLE IF NOT EXISTS submodel (
  id          BIGSERIAL PRIMARY KEY,
  submodel_identifier varchar(2048) UNIQUE NOT NULL,              -- Identifiable.id
  id_short    varchar(128),
  category    varchar(128),
  kind        int,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS submodel_payload (
  submodel_id BIGINT PRIMARY KEY REFERENCES submodel(id) ON DELETE CASCADE,
  description_payload JSONB,
  displayname_payload JSONB,
  administrative_information_payload JSONB,
  embedded_data_specification_payload JSONB,
  supplemental_semantic_ids_payload JSONB,
  extensions_payload JSONB,
  qualifiers_payload JSONB,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS submodel_semantic_id_reference (
  id   BIGINT PRIMARY KEY REFERENCES submodel(id) ON DELETE CASCADE,
  type int NOT NULL,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS submodel_semantic_id_reference_key (
  id           BIGSERIAL PRIMARY KEY,
  reference_id BIGINT NOT NULL REFERENCES submodel_semantic_id_reference(id) ON DELETE CASCADE,
  position     INTEGER NOT NULL,
  type         int NOT NULL,
  value        TEXT NOT NULL,
  UNIQUE(reference_id, position),
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS submodel_semantic_id_reference_payload (
  id           BIGSERIAL PRIMARY KEY,
  reference_id BIGINT NOT NULL REFERENCES submodel_semantic_id_reference(id) ON DELETE CASCADE,
  parent_reference_payload JSONB NOT NULL,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS submodel_supplemental_semantic_id_reference (
  id BIGSERIAL PRIMARY KEY,
  submodel_id BIGINT NOT NULL REFERENCES submodel(id) ON DELETE CASCADE,
  position INTEGER NOT NULL,
  type INTEGER NOT NULL,
  UNIQUE(submodel_id, position),
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS submodel_supplemental_semantic_id_reference_key (
  id BIGSERIAL PRIMARY KEY,
  reference_id BIGINT NOT NULL REFERENCES submodel_supplemental_semantic_id_reference(id) ON DELETE CASCADE,
  position INTEGER NOT NULL,
  type INTEGER NOT NULL,
  value TEXT NOT NULL,
  UNIQUE(reference_id, position),
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS submodel_supplemental_semantic_id_reference_payload (
  id BIGSERIAL PRIMARY KEY,
  reference_id BIGINT NOT NULL REFERENCES submodel_supplemental_semantic_id_reference(id) ON DELETE CASCADE,
  parent_reference_payload JSONB NOT NULL,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ------------------------------------------
-- Submodel elements
-- ------------------------------------------

CREATE TABLE IF NOT EXISTS submodel_element (
  id             BIGSERIAL PRIMARY KEY,
  submodel_id    BIGINT NOT NULL REFERENCES submodel(id) ON DELETE CASCADE,
  root_sme_id    BIGINT REFERENCES submodel_element(id) ON DELETE CASCADE,
  parent_sme_id  BIGINT REFERENCES submodel_element(id) ON DELETE CASCADE,
  position       INTEGER,                                   -- for ordering in lists
  id_short       varchar(128),
  category       varchar(128),
  model_type     int NOT NULL,
  idshort_path   TEXT NOT NULL,                            -- e.g. sm_abc.sensors[2].temperature
  depth	BIGINT,
  CONSTRAINT uq_sibling_idshort UNIQUE (submodel_id, parent_sme_id, idshort_path),
  CONSTRAINT uq_sibling_pos     UNIQUE (submodel_id, parent_sme_id, position),
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS submodel_element_payload (
  submodel_element_id BIGINT PRIMARY KEY REFERENCES submodel_element(id) ON DELETE CASCADE,
  description_payload JSONB DEFAULT '[]',
  displayname_payload JSONB DEFAULT '[]',
  embedded_data_specification_payload JSONB DEFAULT '[]',
  supplemental_semantic_ids_payload JSONB DEFAULT '[]',
  extensions_payload JSONB DEFAULT '[]',
  qualifiers_payload JSONB,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS submodel_element_semantic_id_reference (
  id   BIGINT PRIMARY KEY REFERENCES submodel_element(id) ON DELETE CASCADE,
  type int NOT NULL,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS submodel_element_semantic_id_reference_key (
  id           BIGSERIAL PRIMARY KEY,
  reference_id BIGINT NOT NULL REFERENCES submodel_element_semantic_id_reference(id) ON DELETE CASCADE,
  position     INTEGER NOT NULL,
  type         int NOT NULL,
  value        TEXT NOT NULL,
  UNIQUE(reference_id, position),
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS submodel_element_semantic_id_reference_payload (
  id           BIGSERIAL PRIMARY KEY,
  reference_id BIGINT NOT NULL REFERENCES submodel_element_semantic_id_reference(id) ON DELETE CASCADE,
  parent_reference_payload JSONB NOT NULL,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS submodel_element_supplemental_semantic_id_reference (
  id BIGSERIAL PRIMARY KEY,
  submodel_element_id BIGINT NOT NULL REFERENCES submodel_element(id) ON DELETE CASCADE,
  position INTEGER NOT NULL,
  type INTEGER NOT NULL,
  UNIQUE(submodel_element_id, position),
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS submodel_element_supplemental_semantic_id_reference_key (
  id BIGSERIAL PRIMARY KEY,
  reference_id BIGINT NOT NULL REFERENCES submodel_element_supplemental_semantic_id_reference(id) ON DELETE CASCADE,
  position INTEGER NOT NULL,
  type INTEGER NOT NULL,
  value TEXT NOT NULL,
  UNIQUE(reference_id, position),
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS submodel_element_supplemental_semantic_id_reference_payload (
  id BIGSERIAL PRIMARY KEY,
  reference_id BIGINT NOT NULL REFERENCES submodel_element_supplemental_semantic_id_reference(id) ON DELETE CASCADE,
  parent_reference_payload JSONB NOT NULL,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS property_element (
  id            BIGINT PRIMARY KEY REFERENCES submodel_element(id) ON DELETE CASCADE,
  value_type    int NOT NULL,
  value_text    TEXT,
  value_num     NUMERIC,
  value_bool    BOOLEAN,
  value_time    TIME,
  value_date    DATE,
  value_datetime TIMESTAMPTZ,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS property_element_payload (
  property_element_id BIGINT PRIMARY KEY REFERENCES property_element(id) ON DELETE CASCADE,
  value_id_payload JSONB DEFAULT '[]',
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS multilanguage_property_value (
  id                  BIGSERIAL PRIMARY KEY,
  submodel_element_id BIGINT NOT NULL REFERENCES submodel_element(id) ON DELETE CASCADE,
  language            TEXT NOT NULL,
  text                TEXT NOT NULL,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS multilanguage_property_payload (
  submodel_element_id BIGINT PRIMARY KEY REFERENCES submodel_element(id) ON DELETE CASCADE,
  value_id_payload    JSONB DEFAULT '[]',
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS blob_element (
  id           BIGINT PRIMARY KEY REFERENCES submodel_element(id) ON DELETE CASCADE,
  content_type TEXT,
  value        BYTEA,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS file_element (
  id           BIGINT PRIMARY KEY REFERENCES submodel_element(id) ON DELETE CASCADE,
  content_type TEXT,
  file_name    TEXT,
  value        TEXT,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS range_element (
  id            BIGINT PRIMARY KEY REFERENCES submodel_element(id) ON DELETE CASCADE,
  value_type    int NOT NULL,
  min_text      TEXT,  max_text      TEXT,
  min_num       NUMERIC, max_num     NUMERIC,
  min_time      TIME,   max_time     TIME,
  min_date      DATE,   max_date     DATE,
  min_datetime  TIMESTAMPTZ, max_datetime TIMESTAMPTZ,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS reference_element (
  id        BIGINT PRIMARY KEY REFERENCES submodel_element(id) ON DELETE CASCADE,
  value JSONB,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS relationship_element (
  id         BIGINT PRIMARY KEY REFERENCES submodel_element(id) ON DELETE CASCADE,
  first JSONB,
  second JSONB,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS annotated_relationship_element (
  id         BIGINT PRIMARY KEY REFERENCES submodel_element(id) ON DELETE CASCADE,
  first JSONB,
  second JSONB,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS submodel_element_collection (
  id BIGINT PRIMARY KEY REFERENCES submodel_element(id) ON DELETE CASCADE,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS submodel_element_list (
  id                         BIGINT PRIMARY KEY REFERENCES submodel_element(id) ON DELETE CASCADE,
  order_relevant             BOOLEAN,
  semantic_id_list_element   JSONB,
  type_value_list_element    int NOT NULL,
  value_type_list_element    int,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS entity_element (
  id              BIGINT PRIMARY KEY REFERENCES submodel_element(id) ON DELETE CASCADE,
  entity_type     int NOT NULL,
  global_asset_id TEXT,
  specific_asset_ids JSONB DEFAULT '[]',
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS operation_element (
  id BIGINT PRIMARY KEY REFERENCES submodel_element(id) ON DELETE CASCADE,
  input_variables JSONB DEFAULT '[]',
  output_variables JSONB DEFAULT '[]',
  inoutput_variables JSONB DEFAULT '[]',
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS basic_event_element (
  id                BIGINT PRIMARY KEY REFERENCES submodel_element(id) ON DELETE CASCADE,
  observed          JSONB,
  direction         int NOT NULL,
  state             int NOT NULL,
  message_topic     TEXT,
  message_broker    JSONB,
  last_update       TIMESTAMPTZ,
  min_interval      INTERVAL,
  max_interval      INTERVAL,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS capability_element (
  id BIGINT PRIMARY KEY REFERENCES submodel_element(id) ON DELETE CASCADE,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ------------------------------------------
-- Concept descriptions
-- ------------------------------------------

CREATE TABLE IF NOT EXISTS concept_description (
  id TEXT PRIMARY KEY,
  id_short TEXT,
  data JSONB,
  seq BIGSERIAL,                                            -- preserves insertion order for paging
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ------------------------------------------
-- Operation results
-- ------------------------------------------

CREATE TABLE IF NOT EXISTS operation_result (
  id TEXT PRIMARY KEY,
  content JSONB NOT NULL,
  db_created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  db_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ------------------------------------------
-- Timestamp triggers
-- ------------------------------------------

CREATE OR REPLACE FUNCTION set_db_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.db_updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
DECLARE
  v_schema_name TEXT := current_schema();
  v_table_name TEXT;
  v_trigger_name TEXT;
BEGIN
  FOR v_table_name IN
    SELECT table_name
    FROM information_schema.columns
    WHERE table_schema = v_schema_name
      AND column_name = 'db_updated_at'
    ORDER BY table_name
  LOOP
    v_trigger_name := left(format('%s_set_db_updated_at', v_table_name), 63);

    IF NOT EXISTS (
      SELECT 1
      FROM pg_trigger t
      JOIN pg_class c ON c.oid = t.tgrelid
      JOIN pg_namespace n ON n.oid = c.relnamespace
      WHERE t.tgname = v_trigger_name
        AND n.nspname = v_schema_name
        AND c.relname = v_table_name
        AND NOT t.tgisinternal
    ) THEN
      EXECUTE format(
        'CREATE TRIGGER %I BEFORE UPDATE ON %I.%I FOR EACH ROW EXECUTE FUNCTION set_db_updated_at()',
        v_trigger_name,
        v_schema_name,
        v_table_name
      );
    END IF;
  END LOOP;
END $$;

-- ------------------------------------------
-- Indexes
-- ------------------------------------------

CREATE INDEX IF NOT EXISTS ix_aas_identifier ON aas(aas_id);
CREATE INDEX IF NOT EXISTS ix_aas_idshort ON aas(id_short);

CREATE INDEX IF NOT EXISTS ix_asset_information_asset_kind ON asset_information(asset_kind);
CREATE INDEX IF NOT EXISTS ix_asset_information_asset_type ON asset_information(asset_type);
CREATE INDEX IF NOT EXISTS ix_asset_information_global_asset_id ON asset_information(global_asset_id);

CREATE INDEX IF NOT EXISTS ix_sm_identifier  ON submodel(submodel_identifier);
CREATE INDEX IF NOT EXISTS ix_sm_idshort     ON submodel(id_short);

CREATE INDEX IF NOT EXISTS ix_sme_path_gin   ON submodel_element USING GIN (idshort_path gin_trgm_ops);
CREATE INDEX IF NOT EXISTS ix_sme_sub_path   ON submodel_element(submodel_id, idshort_path);
CREATE INDEX IF NOT EXISTS ix_sme_parent_pos ON submodel_element(parent_sme_id, position);
CREATE INDEX IF NOT EXISTS ix_sme_sub_type   ON submodel_element(submodel_id, model_type);
CREATE INDEX IF NOT EXISTS ix_sme_sub_parent ON submodel_element(submodel_id, parent_sme_id);
CREATE INDEX IF NOT EXISTS ix_sme_sub_root   ON submodel_element(submodel_id, root_sme_id);
CREATE INDEX IF NOT EXISTS ix_sme_parent_fk  ON submodel_element(parent_sme_id);
CREATE INDEX IF NOT EXISTS ix_sme_root_fk    ON submodel_element(root_sme_id);
CREATE INDEX IF NOT EXISTS ix_sme_sub_depth  ON submodel_element(submodel_id, depth);
CREATE INDEX IF NOT EXISTS ix_sme_roots_order
  ON submodel_element (submodel_id,
                       (CASE WHEN position IS NULL THEN 1 ELSE 0 END),
                       position,
                       idshort_path,
                       id)
  WHERE parent_sme_id IS NULL;
CREATE INDEX IF NOT EXISTS ix_sme_roots_page
  ON submodel_element(submodel_id, idshort_path, id)
  WHERE parent_sme_id IS NULL;

CREATE INDEX IF NOT EXISTS ix_mlp_lang      ON multilanguage_property_value(submodel_element_id, language);
CREATE INDEX IF NOT EXISTS ix_mlp_text_trgm ON multilanguage_property_value USING GIN (text gin_trgm_ops);
CREATE INDEX IF NOT EXISTS ix_file_value_trgm ON file_element USING GIN (value gin_trgm_ops);
CREATE INDEX IF NOT EXISTS ix_bee_lastupd ON basic_event_element(last_update);

CREATE INDEX IF NOT EXISTS ix_specasset_aas ON specific_asset_id(asset_information_id);
CREATE INDEX IF NOT EXISTS ix_specasset_name_value_aas ON specific_asset_id(name, value, asset_information_id);
CREATE INDEX IF NOT EXISTS ix_specasset_name ON specific_asset_id(name);
CREATE INDEX IF NOT EXISTS ix_specasset_name_value ON specific_asset_id(name, value);
CREATE INDEX IF NOT EXISTS ix_specasset_value_trgm ON specific_asset_id USING GIN (value gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_submodel_semantic_id_ref_type ON submodel_semantic_id_reference(type);
CREATE INDEX IF NOT EXISTS ix_submodel_semantic_id_refkey_refid ON submodel_semantic_id_reference_key(reference_id);
CREATE INDEX IF NOT EXISTS ix_submodel_semantic_id_refkey_refval ON submodel_semantic_id_reference_key(reference_id, value);
CREATE INDEX IF NOT EXISTS ix_submodel_semantic_id_refkey_type_val ON submodel_semantic_id_reference_key(type, value);
CREATE INDEX IF NOT EXISTS ix_submodel_semantic_id_refkey_val_trgm ON submodel_semantic_id_reference_key USING GIN (value gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_submodel_supp_sem_owner_id ON submodel_supplemental_semantic_id_reference(submodel_id);
CREATE INDEX IF NOT EXISTS ix_submodel_supp_sem_refkey_refid ON submodel_supplemental_semantic_id_reference_key(reference_id);
CREATE INDEX IF NOT EXISTS ix_submodel_supp_sem_refkey_refval ON submodel_supplemental_semantic_id_reference_key(reference_id, value);
CREATE INDEX IF NOT EXISTS ix_submodel_supp_sem_refkey_type_val ON submodel_supplemental_semantic_id_reference_key(type, value);
CREATE INDEX IF NOT EXISTS ix_submodel_supp_sem_refkey_val_trgm ON submodel_supplemental_semantic_id_reference_key USING GIN (value gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_submodel_element_semantic_id_ref_type ON submodel_element_semantic_id_reference(type);
CREATE INDEX IF NOT EXISTS ix_submodel_element_semantic_id_refkey_refid ON submodel_element_semantic_id_reference_key(reference_id);
CREATE INDEX IF NOT EXISTS ix_submodel_element_semantic_id_refkey_refval ON submodel_element_semantic_id_reference_key(reference_id, value);
CREATE INDEX IF NOT EXISTS ix_submodel_element_semantic_id_refkey_type_val ON submodel_element_semantic_id_reference_key(type, value);
CREATE INDEX IF NOT EXISTS ix_submodel_element_semantic_id_refkey_val_trgm ON submodel_element_semantic_id_reference_key USING GIN (value gin_trgm_ops);
CREATE INDEX IF NOT EXISTS ix_submodel_element_semantic_id_refpayload_refid ON submodel_element_semantic_id_reference_payload(reference_id);

CREATE INDEX IF NOT EXISTS ix_sme_supp_sem_owner_id ON submodel_element_supplemental_semantic_id_reference(submodel_element_id);
CREATE INDEX IF NOT EXISTS ix_sme_supp_sem_refkey_refid ON submodel_element_supplemental_semantic_id_reference_key(reference_id);
CREATE INDEX IF NOT EXISTS ix_sme_supp_sem_refkey_refval ON submodel_element_supplemental_semantic_id_reference_key(reference_id, value);
CREATE INDEX IF NOT EXISTS ix_sme_supp_sem_refkey_type_val ON submodel_element_supplemental_semantic_id_reference_key(type, value);
CREATE INDEX IF NOT EXISTS ix_sme_supp_sem_refkey_val_trgm ON submodel_element_supplemental_semantic_id_reference_key USING GIN (value gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_specasset_external_subject_id_ref_type ON specific_asset_id_external_subject_id_reference(type);
CREATE INDEX IF NOT EXISTS ix_specasset_external_subject_id_refkey_refid ON specific_asset_id_external_subject_id_reference_key(reference_id);
CREATE INDEX IF NOT EXISTS ix_specasset_external_subject_id_refkey_refval ON specific_asset_id_external_subject_id_reference_key(reference_id, value);
CREATE INDEX IF NOT EXISTS ix_specasset_external_subject_id_refkey_type_val ON specific_asset_id_external_subject_id_reference_key(type, value);
CREATE INDEX IF NOT EXISTS ix_specasset_external_subject_id_refkey_val_trgm ON specific_asset_id_external_subject_id_reference_key USING GIN (value gin_trgm_ops);
CREATE INDEX IF NOT EXISTS ix_specasset_external_subject_id_refpayload_refid ON specific_asset_id_external_subject_id_reference_payload(reference_id);

CREATE INDEX IF NOT EXISTS ix_specasset_supp_semantic_owner_id ON specific_asset_id_supplemental_semantic_id_reference(specific_asset_id_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_specasset_supp_semantic_owner_position ON specific_asset_id_supplemental_semantic_id_reference(specific_asset_id_id, position);
CREATE INDEX IF NOT EXISTS ix_specasset_supp_semantic_refkey_refid ON specific_asset_id_supplemental_semantic_id_reference_key(reference_id);
CREATE INDEX IF NOT EXISTS ix_specasset_supp_semantic_refkey_refval ON specific_asset_id_supplemental_semantic_id_reference_key(reference_id, value);
CREATE INDEX IF NOT EXISTS ix_specasset_supp_semantic_refkey_type_val ON specific_asset_id_supplemental_semantic_id_reference_key(type, value);
CREATE INDEX IF NOT EXISTS ix_specasset_supp_semantic_refkey_val_trgm ON specific_asset_id_supplemental_semantic_id_reference_key USING GIN (value gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_cd_seq ON concept_description(seq);
