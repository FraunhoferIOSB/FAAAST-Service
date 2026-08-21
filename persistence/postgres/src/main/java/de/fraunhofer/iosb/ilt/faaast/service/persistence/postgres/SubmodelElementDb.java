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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.BasicEventElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.DataElement;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.EmbeddedDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.Extension;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.RelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultBasicEventElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultBlob;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultCapability;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEntity;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultFile;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultRange;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;


/**
 * Read/write access to the submodel element tree: one {@code submodel_element} row per element at
 * any depth (with parent/root links, sibling position and materialized {@code idshort_path}), one row in the
 * model-type specific value table, plus payload and semantic reference rows.
 *
 */
final class SubmodelElementDb {

    private SubmodelElementDb() {}

    /**
     * Location of an element row within the tree.
     */
    record ElementInfo(long id, Long parentId, Long rootId, int position, int depth, String idShortPath, int modelType) {}

    // -------------------------------------------------------------------------------------------------
    // write
    // -------------------------------------------------------------------------------------------------

    /**
     * Inserts the given elements (and their subtrees) as children of the given parent, assigning positions starting at
     * {@code startPosition}.
     */
    static void insertTrees(Connection connection, long submodelDbId, Long parentId, Long rootId, boolean parentIsList, String parentPath, int depth, int startPosition,
                            List<SubmodelElement> elements)
            throws SQLException {
        if (elements == null) {
            return;
        }
        for (int i = 0; i < elements.size(); i++) {
            insertTree(connection, submodelDbId, parentId, rootId, parentIsList, parentPath, depth, startPosition + i, elements.get(i));
        }
    }


    /**
     * Inserts a single element and its subtree. Returns the database id of the inserted element row.
     */
    static long insertTree(Connection connection, long submodelDbId, Long parentId, Long rootId, boolean parentIsList, String parentPath, int depth, int position,
                           SubmodelElement element)
            throws SQLException {
        int modelType = EnumCodes.modelType(element);
        String idShortPath = childPath(parentPath, parentIsList, position, element.getIdShort());
        long elementId;
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT
                        + " (submodel_id, root_sme_id, parent_sme_id, position, id_short, category, model_type, idshort_path, depth)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, submodelDbId);
            stmt.setObject(2, rootId, Types.BIGINT);
            stmt.setObject(3, parentId, Types.BIGINT);
            stmt.setInt(4, position);
            stmt.setString(5, element.getIdShort());
            stmt.setString(6, element.getCategory());
            stmt.setInt(7, modelType);
            stmt.setString(8, idShortPath);
            stmt.setInt(9, depth);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                elementId = rs.getLong(1);
            }
        }
        insertElementPayload(connection, elementId, element);
        ReferenceDb.insertOwned(connection,
                DatabaseSchema.TABLE_SUBMODEL_ELEMENT_SEMANTIC_ID_REF,
                DatabaseSchema.TABLE_SUBMODEL_ELEMENT_SEMANTIC_ID_REF_KEY,
                DatabaseSchema.TABLE_SUBMODEL_ELEMENT_SEMANTIC_ID_REF_PAYLOAD,
                elementId, element.getSemanticId());
        ReferenceDb.insertList(connection,
                DatabaseSchema.TABLE_SUBMODEL_ELEMENT_SUPPLEMENTAL_REF, "submodel_element_id",
                DatabaseSchema.TABLE_SUBMODEL_ELEMENT_SUPPLEMENTAL_REF_KEY,
                DatabaseSchema.TABLE_SUBMODEL_ELEMENT_SUPPLEMENTAL_REF_PAYLOAD,
                elementId, element.getSupplementalSemanticIds());
        insertTypeSpecific(connection, elementId, element);

        Long childRootId = rootId != null ? rootId : elementId;
        if (element instanceof SubmodelElementCollection collection) {
            insertTrees(connection, submodelDbId, elementId, childRootId, false, idShortPath, depth + 1, 0, collection.getValue());
        }
        else if (element instanceof SubmodelElementList list) {
            insertTrees(connection, submodelDbId, elementId, childRootId, true, idShortPath, depth + 1, 0, list.getValue());
        }
        else if (element instanceof Entity entity) {
            insertTrees(connection, submodelDbId, elementId, childRootId, false, idShortPath, depth + 1, 0, entity.getStatements());
        }
        else if (element instanceof AnnotatedRelationshipElement annotated) {
            insertTrees(connection, submodelDbId, elementId, childRootId, false, idShortPath, depth + 1, 0,
                    annotated.getAnnotations() != null ? new ArrayList<SubmodelElement>(annotated.getAnnotations()) : null);
        }
        return elementId;
    }


    /**
     * Builds the materialized idShort path of a child element: {@code parent.child} for named containers,
     * {@code parent[position]} for SubmodelElementList children.
     */
    static String childPath(String parentPath, boolean parentIsList, int position, String idShort) {
        if (parentIsList) {
            return (parentPath == null ? "" : parentPath) + "[" + position + "]";
        }
        if (parentPath == null || parentPath.isEmpty()) {
            return idShort;
        }
        return parentPath + "." + idShort;
    }


    private static void insertElementPayload(Connection connection, long elementId, SubmodelElement element) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT_PAYLOAD
                        + " (submodel_element_id, description_payload, displayname_payload, embedded_data_specification_payload,"
                        + " supplemental_semantic_ids_payload, extensions_payload, qualifiers_payload)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setLong(1, elementId);
            stmt.setObject(2, DbJson.writeList(element.getDescription()), Types.OTHER);
            stmt.setObject(3, DbJson.writeList(element.getDisplayName()), Types.OTHER);
            stmt.setObject(4, DbJson.writeList(element.getEmbeddedDataSpecifications()), Types.OTHER);
            stmt.setObject(5, DbJson.writeList(element.getSupplementalSemanticIds()), Types.OTHER);
            stmt.setObject(6, DbJson.writeList(element.getExtensions()), Types.OTHER);
            stmt.setObject(7, DbJson.writeList(element.getQualifiers()), Types.OTHER);
            stmt.executeUpdate();
        }
    }


    private static void insertTypeSpecific(Connection connection, long elementId, SubmodelElement element) throws SQLException {
        if (element instanceof AnnotatedRelationshipElement annotated) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_ANNOTATED_RELATIONSHIP_ELEMENT + " (id, first, second) VALUES (?, ?, ?)")) {
                stmt.setLong(1, elementId);
                stmt.setObject(2, DbJson.write(annotated.getFirst()), Types.OTHER);
                stmt.setObject(3, DbJson.write(annotated.getSecond()), Types.OTHER);
                stmt.executeUpdate();
            }
        }
        else if (element instanceof RelationshipElement relationship) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_RELATIONSHIP_ELEMENT + " (id, first, second) VALUES (?, ?, ?)")) {
                stmt.setLong(1, elementId);
                stmt.setObject(2, DbJson.write(relationship.getFirst()), Types.OTHER);
                stmt.setObject(3, DbJson.write(relationship.getSecond()), Types.OTHER);
                stmt.executeUpdate();
            }
        }
        else if (element instanceof SubmodelElementList list) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT_LIST
                            + " (id, order_relevant, semantic_id_list_element, type_value_list_element, value_type_list_element) VALUES (?, ?, ?, ?, ?)")) {
                stmt.setLong(1, elementId);
                stmt.setObject(2, list.getOrderRelevant(), Types.BOOLEAN);
                stmt.setObject(3, DbJson.write(list.getSemanticIdListElement()), Types.OTHER);
                stmt.setInt(4, EnumCodes.of(list.getTypeValueListElement()));
                if (list.getValueTypeListElement() != null) {
                    stmt.setInt(5, EnumCodes.of(list.getValueTypeListElement()));
                }
                else {
                    stmt.setNull(5, Types.INTEGER);
                }
                stmt.executeUpdate();
            }
        }
        else if (element instanceof SubmodelElementCollection) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT_COLLECTION + " (id) VALUES (?)")) {
                stmt.setLong(1, elementId);
                stmt.executeUpdate();
            }
        }
        else if (element instanceof Property property) {
            insertProperty(connection, elementId, property);
        }
        else if (element instanceof MultiLanguageProperty mlp) {
            insertMultiLanguageProperty(connection, elementId, mlp);
        }
        else if (element instanceof Range range) {
            insertRange(connection, elementId, range);
        }
        else if (element instanceof ReferenceElement reference) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_REFERENCE_ELEMENT + " (id, value) VALUES (?, ?)")) {
                stmt.setLong(1, elementId);
                stmt.setObject(2, DbJson.write(reference.getValue()), Types.OTHER);
                stmt.executeUpdate();
            }
        }
        else if (element instanceof Blob blob) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_BLOB + " (id, content_type, value) VALUES (?, ?, ?)")) {
                stmt.setLong(1, elementId);
                stmt.setString(2, blob.getContentType());
                stmt.setBytes(3, blob.getValue());
                stmt.executeUpdate();
            }
        }
        else if (element instanceof File file) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_FILE + " (id, content_type, value) VALUES (?, ?, ?)")) {
                stmt.setLong(1, elementId);
                stmt.setString(2, file.getContentType());
                stmt.setString(3, file.getValue());
                stmt.executeUpdate();
            }
        }
        else if (element instanceof Entity entity) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_ENTITY + " (id, entity_type, global_asset_id, specific_asset_ids) VALUES (?, ?, ?, ?)")) {
                stmt.setLong(1, elementId);
                stmt.setInt(2, EnumCodes.of(entity.getEntityType()));
                stmt.setString(3, entity.getGlobalAssetId());
                stmt.setObject(4, DbJson.writeList(entity.getSpecificAssetIds()), Types.OTHER);
                stmt.executeUpdate();
            }
        }
        else if (element instanceof Operation operation) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_OPERATION + " (id, input_variables, output_variables, inoutput_variables) VALUES (?, ?, ?, ?)")) {
                stmt.setLong(1, elementId);
                stmt.setObject(2, DbJson.writeList(operation.getInputVariables()), Types.OTHER);
                stmt.setObject(3, DbJson.writeList(operation.getOutputVariables()), Types.OTHER);
                stmt.setObject(4, DbJson.writeList(operation.getInoutputVariables()), Types.OTHER);
                stmt.executeUpdate();
            }
        }
        else if (element instanceof BasicEventElement event) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_BASIC_EVENT
                            + " (id, observed, direction, state, message_topic, message_broker, last_update, min_interval, max_interval)"
                            + " VALUES (?, ?, ?, ?, ?, ?, ?::timestamptz, ?::interval, ?::interval)")) {
                stmt.setLong(1, elementId);
                stmt.setObject(2, DbJson.write(event.getObserved()), Types.OTHER);
                stmt.setInt(3, EnumCodes.of(event.getDirection()));
                stmt.setInt(4, EnumCodes.of(event.getState()));
                stmt.setString(5, event.getMessageTopic());
                stmt.setObject(6, DbJson.write(event.getMessageBroker()), Types.OTHER);
                stmt.setString(7, event.getLastUpdate());
                stmt.setString(8, event.getMinInterval());
                stmt.setString(9, event.getMaxInterval());
                stmt.executeUpdate();
            }
        }
        else {
            // Capability - no type-specific values
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_CAPABILITY + " (id) VALUES (?)")) {
                stmt.setLong(1, elementId);
                stmt.executeUpdate();
            }
        }
    }


    private static void insertProperty(Connection connection, long elementId, Property property) throws SQLException {
        TypedValue typed = TypedValue.of(property.getValueType(), property.getValue());
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO " + DatabaseSchema.TABLE_PROPERTY
                        + " (id, value_type, value_text, value_num, value_bool, value_time, value_date, value_datetime)"
                        + " VALUES (?, ?, ?, ?, ?, ?::time, ?::date, ?)")) {
            stmt.setLong(1, elementId);
            stmt.setInt(2, EnumCodes.of(property.getValueType()));
            stmt.setString(3, property.getValue());
            stmt.setBigDecimal(4, typed.numeric);
            stmt.setObject(5, typed.bool, Types.BOOLEAN);
            stmt.setObject(6, typed.time, Types.VARCHAR);
            stmt.setObject(7, typed.date, Types.VARCHAR);
            stmt.setObject(8, typed.dateTime);
            stmt.executeUpdate();
        }
        if (property.getValueId() != null) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_PROPERTY_PAYLOAD + " (property_element_id, value_id_payload) VALUES (?, ?)")) {
                stmt.setLong(1, elementId);
                stmt.setObject(2, DbJson.write(property.getValueId()), Types.OTHER);
                stmt.executeUpdate();
            }
        }
    }


    private static void insertMultiLanguageProperty(Connection connection, long elementId, MultiLanguageProperty mlp) throws SQLException {
        if (mlp.getValue() != null && !mlp.getValue().isEmpty()) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_MULTILANGUAGE_PROPERTY_VALUE + " (submodel_element_id, language, text) VALUES (?, ?, ?)")) {
                for (LangStringTextType langString: mlp.getValue()) {
                    stmt.setLong(1, elementId);
                    stmt.setString(2, langString.getLanguage());
                    stmt.setString(3, langString.getText());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }
        if (mlp.getValueId() != null) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO " + DatabaseSchema.TABLE_MULTILANGUAGE_PROPERTY_PAYLOAD + " (submodel_element_id, value_id_payload) VALUES (?, ?)")) {
                stmt.setLong(1, elementId);
                stmt.setObject(2, DbJson.write(mlp.getValueId()), Types.OTHER);
                stmt.executeUpdate();
            }
        }
    }


    private static void insertRange(Connection connection, long elementId, Range range) throws SQLException {
        TypedValue min = TypedValue.of(range.getValueType(), range.getMin());
        TypedValue max = TypedValue.of(range.getValueType(), range.getMax());
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO " + DatabaseSchema.TABLE_RANGE
                        + " (id, value_type, min_text, max_text, min_num, max_num, min_time, max_time, min_date, max_date, min_datetime, max_datetime)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?::time, ?::time, ?::date, ?::date, ?, ?)")) {
            stmt.setLong(1, elementId);
            stmt.setInt(2, EnumCodes.of(range.getValueType()));
            stmt.setString(3, range.getMin());
            stmt.setString(4, range.getMax());
            stmt.setBigDecimal(5, min.numeric);
            stmt.setBigDecimal(6, max.numeric);
            stmt.setObject(7, min.time, Types.VARCHAR);
            stmt.setObject(8, max.time, Types.VARCHAR);
            stmt.setObject(9, min.date, Types.VARCHAR);
            stmt.setObject(10, max.date, Types.VARCHAR);
            stmt.setObject(11, min.dateTime);
            stmt.setObject(12, max.dateTime);
            stmt.executeUpdate();
        }
    }

    /**
     * Best-effort conversion of a lexical value to the typed columns based on the XSD value type. Values that cannot
     * be converted are stored in the text column only.
     */
    private record TypedValue(BigDecimal numeric, Boolean bool, String time, String date, OffsetDateTime dateTime) {

        static TypedValue of(DataTypeDefXsd valueType, String value) {
            if (value == null || valueType == null) {
                return new TypedValue(null, null, null, null, null);
            }
            switch (valueType) {
                case BYTE, DECIMAL, DOUBLE, FLOAT, INT, INTEGER, LONG, NEGATIVE_INTEGER, NON_NEGATIVE_INTEGER,
                        NON_POSITIVE_INTEGER, POSITIVE_INTEGER, SHORT, UNSIGNED_BYTE, UNSIGNED_INT, UNSIGNED_LONG, UNSIGNED_SHORT:
                    try {
                        return new TypedValue(new BigDecimal(value), null, null, null, null);
                    }
                    catch (NumberFormatException e) {
                        return new TypedValue(null, null, null, null, null);
                    }
                case BOOLEAN:
                    if ("true".equals(value) || "1".equals(value)) {
                        return new TypedValue(null, Boolean.TRUE, null, null, null);
                    }
                    if ("false".equals(value) || "0".equals(value)) {
                        return new TypedValue(null, Boolean.FALSE, null, null, null);
                    }
                    return new TypedValue(null, null, null, null, null);
                case TIME:
                    try {
                        LocalTime.parse(value);
                        return new TypedValue(null, null, value, null, null);
                    }
                    catch (DateTimeParseException e) {
                        return new TypedValue(null, null, null, null, null);
                    }
                case DATE:
                    try {
                        LocalDate.parse(value);
                        return new TypedValue(null, null, null, value, null);
                    }
                    catch (DateTimeParseException e) {
                        return new TypedValue(null, null, null, null, null);
                    }
                case DATE_TIME:
                    try {
                        return new TypedValue(null, null, null, null, OffsetDateTime.parse(value));
                    }
                    catch (DateTimeParseException e) {
                        return new TypedValue(null, null, null, null, null);
                    }
                default:
                    return new TypedValue(null, null, null, null, null);
            }
        }
    }

    // -------------------------------------------------------------------------------------------------
    // read
    // -------------------------------------------------------------------------------------------------

    /**
     * Reads all root elements (with their full subtrees) of a submodel in document order.
     */
    static List<SubmodelElement> readAll(Connection connection, long submodelDbId) throws SQLException {
        return assemble(connection, submodelIdFilter(List.of(submodelDbId)), null, null)
                .getOrDefault(submodelDbId, List.of());
    }


    /**
     * Reads the element trees of multiple submodels at once, grouped by submodel database id. Submodels without
     * elements are not present in the result.
     */
    static Map<Long, List<SubmodelElement>> readAll(Connection connection, List<Long> submodelDbIds) throws SQLException {
        if (submodelDbIds.isEmpty()) {
            return Map.of();
        }
        return assemble(connection, submodelIdFilter(submodelDbIds), null, null);
    }


    /**
     * Reads the element addressed by the given idShort path including its subtree, or null if not present.
     */
    static SubmodelElement readSubtree(Connection connection, long submodelDbId, String idShortPath) throws SQLException {
        return firstRoot(assemble(connection, submodelIdFilter(List.of(submodelDbId)), idShortPath, null));
    }


    /**
     * Reads the element addressed by the given idShort path including its subtree, resolving the submodel by its
     * identifier within the same query. Returns null if the submodel or the element does not exist.
     */
    static SubmodelElement readSubtree(Connection connection, String submodelIdentifier, String idShortPath) throws SQLException {
        SubmodelFilter filter = new SubmodelFilter(
                "e.submodel_id = (SELECT id FROM " + DatabaseSchema.TABLE_SUBMODEL + " WHERE submodel_identifier = ?)",
                List.of(submodelIdentifier));
        return firstRoot(assemble(connection, filter, idShortPath, null));
    }


    private static SubmodelElement firstRoot(Map<Long, List<SubmodelElement>> assembled) {
        return assembled.values().stream()
                .flatMap(List::stream)
                .findFirst()
                .orElse(null);
    }


    /**
     * Returns the ids of a submodel's root elements in sibling position order, skipping {@code offset} roots and
     * returning at most {@code limit} ids (no limit if negative).
     */
    static List<Long> findRootIds(Connection connection, long submodelDbId, long offset, long limit) throws SQLException {
        String sql = "SELECT id FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT
                + " WHERE submodel_id = ? AND parent_sme_id IS NULL ORDER BY position"
                + (limit >= 0 ? " LIMIT ?" : "")
                + " OFFSET ?";
        List<Long> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, submodelDbId);
            if (limit >= 0) {
                stmt.setLong(2, limit);
                stmt.setLong(3, offset);
            }
            else {
                stmt.setLong(2, offset);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getLong(1));
                }
            }
        }
        return result;
    }


    /**
     * Reads the given root elements (with their full subtrees) of a submodel, ordered by sibling position.
     */
    static List<SubmodelElement> readTrees(Connection connection, long submodelDbId, List<Long> rootIds) throws SQLException {
        if (rootIds.isEmpty()) {
            return List.of();
        }
        return assemble(connection, submodelIdFilter(List.of(submodelDbId)), null, rootIds)
                .getOrDefault(submodelDbId, List.of());
    }


    /**
     * Finds the row of an element addressed by an idShort path, or null.
     */
    static ElementInfo findElement(Connection connection, long submodelDbId, String idShortPath) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, parent_sme_id, root_sme_id, position, depth, idshort_path, model_type FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT
                        + " WHERE submodel_id = ? AND idshort_path = ?")) {
            stmt.setLong(1, submodelDbId);
            stmt.setString(2, idShortPath);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new ElementInfo(
                        rs.getLong("id"),
                        rs.getObject("parent_sme_id", Long.class),
                        rs.getObject("root_sme_id", Long.class),
                        rs.getInt("position"),
                        rs.getInt("depth"),
                        rs.getString("idshort_path"),
                        rs.getInt("model_type"));
            }
        }
    }


    /**
     * Deletes an element row; children are removed by the cascade on the parent link.
     */
    static void deleteElement(Connection connection, long elementId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT + " WHERE id = ?")) {
            stmt.setLong(1, elementId);
            stmt.executeUpdate();
        }
    }


    /**
     * Escapes LIKE wildcards for use with {@code ESCAPE '\'}.
     */
    static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static final String SUBTREE_FILTER = " AND (e.idshort_path = ?"
            + " OR e.idshort_path LIKE ? ESCAPE '\\'"
            + " OR e.idshort_path LIKE ? ESCAPE '\\')";

    /**
     * SQL fragment (with bind parameters) restricting the element query to one or more submodels.
     */
    private record SubmodelFilter(String clause, List<Object> parameters) {}

    private static SubmodelFilter submodelIdFilter(List<Long> submodelDbIds) {
        return new SubmodelFilter("e.submodel_id = ANY(?)", List.of(submodelDbIds));
    }


    private static void bindParameters(Connection connection, PreparedStatement stmt, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            Object parameter = parameters.get(i);
            if (parameter instanceof List<?> ids) {
                stmt.setArray(i + 1, connection.createArrayOf("bigint", ids.toArray()));
            }
            else {
                stmt.setObject(i + 1, parameter);
            }
        }
    }


    /**
     * Reconstructs element trees, grouped by submodel database id. Optionally restricted to a subtree
     * ({@code subtreePath}) or to the given root elements ({@code rootIds}).
     *
     * <p>
     * Elements are built in two phases: a narrow query over the element/payload tables creates the skeleton tree,
     * then one batched query per model type actually present loads the type-specific values. This keeps the row width
     * small and avoids joining all type value tables when typical data only uses a few of them. Identical semantic id
     * payloads (common across elements) are parsed only once per call.
     */
    private static Map<Long, List<SubmodelElement>> assemble(Connection connection, SubmodelFilter submodelFilter, String subtreePath, List<Long> rootIds)
            throws SQLException {
        Map<Long, SubmodelElement> elementsById = new HashMap<>();
        Map<Long, List<SubmodelElement>> roots = new LinkedHashMap<>();
        Map<Integer, List<Long>> idsByModelType = new HashMap<>();
        ReferenceCache semanticIds = new ReferenceCache();
        List<Object> parameters = new ArrayList<>(submodelFilter.parameters());
        StringBuilder where = new StringBuilder(" WHERE ").append(submodelFilter.clause());
        if (subtreePath != null) {
            where.append(SUBTREE_FILTER);
            parameters.add(subtreePath);
            parameters.add(escapeLike(subtreePath) + ".%");
            parameters.add(escapeLike(subtreePath) + "[%");
        }
        if (rootIds != null) {
            where.append(" AND (e.id = ANY(?) OR e.root_sme_id = ANY(?))");
            parameters.add(rootIds);
            parameters.add(rootIds);
        }

        String sql = "SELECT e.id, e.submodel_id, e.parent_sme_id, e.model_type, e.id_short, e.category,"
                + " p.description_payload::text AS description, p.displayname_payload::text AS displayname,"
                + " p.embedded_data_specification_payload::text AS eds, p.supplemental_semantic_ids_payload::text AS supplemental,"
                + " p.extensions_payload::text AS extensions, p.qualifiers_payload::text AS qualifiers,"
                + " sem.parent_reference_payload::text AS semantic_id"
                + " FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT + " e"
                + " LEFT JOIN " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT_PAYLOAD + " p ON p.submodel_element_id = e.id"
                + " LEFT JOIN " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT_SEMANTIC_ID_REF_PAYLOAD + " sem ON sem.reference_id = e.id"
                + where
                + " ORDER BY e.depth, e.submodel_id, e.parent_sme_id NULLS FIRST, e.position";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            bindParameters(connection, stmt, parameters);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    int modelType = rs.getInt("model_type");
                    SubmodelElement element = newElement(modelType);
                    applyCommonFields(element, rs, semanticIds);
                    idsByModelType.computeIfAbsent(modelType, key -> new ArrayList<>()).add(id);
                    elementsById.put(id, element);
                    Long parentId = rs.getObject("parent_sme_id", Long.class);
                    SubmodelElement parent = parentId != null ? elementsById.get(parentId) : null;
                    if (parent == null) {
                        roots.computeIfAbsent(rs.getLong("submodel_id"), key -> new ArrayList<>()).add(element);
                    }
                    else {
                        attachChild(parent, element);
                    }
                }
            }
        }
        for (Map.Entry<Integer, List<Long>> entry: idsByModelType.entrySet()) {
            hydrate(connection, entry.getKey(), entry.getValue(), elementsById);
        }
        return roots;
    }


    private static void attachChild(SubmodelElement parent, SubmodelElement child) {
        if (parent instanceof SubmodelElementCollection collection) {
            collection.getValue().add(child);
        }
        else if (parent instanceof SubmodelElementList list) {
            list.getValue().add(child);
        }
        else if (parent instanceof Entity entity) {
            entity.getStatements().add(child);
        }
        else if (parent instanceof AnnotatedRelationshipElement annotated) {
            annotated.getAnnotations().add((DataElement) child);
        }
        else {
            throw new IllegalStateException("Element of type " + parent.getClass().getSimpleName() + " cannot have children");
        }
    }


    /**
     * Loads the language strings of the given MultiLanguageProperty elements and sets them on the already built
     * element instances.
     */
    private static void applyMlpValues(Connection connection, Map<Long, SubmodelElement> elementsById, List<Long> mlpIds) throws SQLException {
        Map<Long, List<LangStringTextType>> valuesByElement = new LinkedHashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT submodel_element_id, language, text FROM " + DatabaseSchema.TABLE_MULTILANGUAGE_PROPERTY_VALUE
                        + " WHERE submodel_element_id = ANY(?) ORDER BY id")) {
            stmt.setArray(1, connection.createArrayOf("bigint", mlpIds.toArray()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    valuesByElement.computeIfAbsent(rs.getLong(1), key -> new ArrayList<>())
                            .add(new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType.Builder()
                                    .language(rs.getString(2))
                                    .text(rs.getString(3))
                                    .build());
                }
            }
        }
        for (Map.Entry<Long, List<LangStringTextType>> entry: valuesByElement.entrySet()) {
            ((MultiLanguageProperty) elementsById.get(entry.getKey())).setValue(entry.getValue());
        }
    }


    /**
     * Creates an empty instance of the model type; type-specific values are filled in later by hydration.
     */
    private static SubmodelElement newElement(int modelType) throws SQLException {
        return switch (modelType) {
            case EnumCodes.MODEL_TYPE_PROPERTY -> new DefaultProperty();
            case EnumCodes.MODEL_TYPE_MULTI_LANGUAGE_PROPERTY -> new DefaultMultiLanguageProperty();
            case EnumCodes.MODEL_TYPE_RANGE -> new DefaultRange();
            case EnumCodes.MODEL_TYPE_REFERENCE_ELEMENT -> new DefaultReferenceElement();
            case EnumCodes.MODEL_TYPE_BLOB -> new DefaultBlob();
            case EnumCodes.MODEL_TYPE_FILE -> new DefaultFile();
            case EnumCodes.MODEL_TYPE_RELATIONSHIP_ELEMENT -> new DefaultRelationshipElement();
            case EnumCodes.MODEL_TYPE_ANNOTATED_RELATIONSHIP_ELEMENT -> new DefaultAnnotatedRelationshipElement();
            case EnumCodes.MODEL_TYPE_SUBMODEL_ELEMENT_COLLECTION -> new DefaultSubmodelElementCollection();
            case EnumCodes.MODEL_TYPE_SUBMODEL_ELEMENT_LIST -> new DefaultSubmodelElementList();
            case EnumCodes.MODEL_TYPE_ENTITY -> new DefaultEntity();
            case EnumCodes.MODEL_TYPE_OPERATION -> new DefaultOperation();
            case EnumCodes.MODEL_TYPE_BASIC_EVENT_ELEMENT -> new DefaultBasicEventElement();
            case EnumCodes.MODEL_TYPE_CAPABILITY -> new DefaultCapability();
            default -> throw new SQLException("Unsupported submodel element model type code: " + modelType);
        };
    }


    private static void applyCommonFields(SubmodelElement element, ResultSet rs, ReferenceCache semanticIds) throws SQLException {
        element.setIdShort(rs.getString("id_short"));
        element.setCategory(rs.getString("category"));
        String description = rs.getString("description");
        if (description != null) {
            element.setDescription(new ArrayList<>(DbJson.readList(description, LangStringTextType.class)));
        }
        String displayName = rs.getString("displayname");
        if (displayName != null) {
            element.setDisplayName(new ArrayList<>(DbJson.readList(displayName, LangStringNameType.class)));
        }
        String eds = rs.getString("eds");
        if (eds != null) {
            element.setEmbeddedDataSpecifications(new ArrayList<>(DbJson.readList(eds, EmbeddedDataSpecification.class)));
        }
        String supplemental = rs.getString("supplemental");
        if (supplemental != null) {
            element.setSupplementalSemanticIds(new ArrayList<>(DbJson.readList(supplemental, Reference.class)));
        }
        String extensions = rs.getString("extensions");
        if (extensions != null) {
            element.setExtensions(new ArrayList<>(DbJson.readList(extensions, Extension.class)));
        }
        String qualifiers = rs.getString("qualifiers");
        if (qualifiers != null) {
            element.setQualifiers(new ArrayList<>(DbJson.readList(qualifiers, Qualifier.class)));
        }
        element.setSemanticId(semanticIds.read(rs.getString("semantic_id")));
    }

    /**
     * Parses semantic id JSON payloads, caching parsed results by payload string: identical semantic ids (very common
     * across elements of a submodel) are parsed only once per read. Cache hits return a fresh copy so no two elements
     * ever share a Reference instance.
     */
    private static final class ReferenceCache {

        private final Map<String, Reference> cache = new HashMap<>();

        Reference read(String json) throws SQLException {
            if (json == null) {
                return null;
            }
            Reference cached = cache.get(json);
            if (cached == null) {
                Reference parsed = DbJson.read(json, Reference.class);
                if (parsed != null) {
                    cache.put(json, parsed);
                }
                return parsed;
            }
            return copyOf(cached);
        }


        private static Reference copyOf(Reference reference) {
            List<Key> keys = new ArrayList<>();
            if (reference.getKeys() != null) {
                for (Key key: reference.getKeys()) {
                    keys.add(new DefaultKey.Builder()
                            .type(key.getType())
                            .value(key.getValue())
                            .build());
                }
            }
            return new DefaultReference.Builder()
                    .type(reference.getType())
                    .referredSemanticId(reference.getReferredSemanticId() != null ? copyOf(reference.getReferredSemanticId()) : null)
                    .keys(keys)
                    .build();
        }
    }

    /**
     * Fills the type-specific values of one row of a type value table into the already built element.
     */
    @FunctionalInterface
    private interface RowHydrator {
        void apply(ResultSet rs, SubmodelElement element) throws SQLException;
    }

    private static void hydrateRows(Connection connection, String sql, String idColumn, List<Long> ids,
                                    Map<Long, SubmodelElement> elementsById, RowHydrator hydrator)
            throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setArray(1, connection.createArrayOf("bigint", ids.toArray()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    hydrator.apply(rs, elementsById.get(rs.getLong(idColumn)));
                }
            }
        }
    }


    /**
     * Loads the type-specific values of all elements of one model type with a single batched query.
     */
    private static void hydrate(Connection connection, int modelType, List<Long> ids, Map<Long, SubmodelElement> elementsById) throws SQLException {
        switch (modelType) {
            case EnumCodes.MODEL_TYPE_PROPERTY -> hydrateProperties(connection, ids, elementsById);
            case EnumCodes.MODEL_TYPE_MULTI_LANGUAGE_PROPERTY -> hydrateMultiLanguageProperties(connection, ids, elementsById);
            case EnumCodes.MODEL_TYPE_RANGE -> hydrateRanges(connection, ids, elementsById);
            case EnumCodes.MODEL_TYPE_REFERENCE_ELEMENT -> hydrateRows(connection,
                    "SELECT id, value::text AS value FROM " + DatabaseSchema.TABLE_REFERENCE_ELEMENT + " WHERE id = ANY(?)",
                    "id", ids, elementsById,
                    (rs, element) -> ((ReferenceElement) element).setValue(DbJson.read(rs.getString("value"), Reference.class)));
            case EnumCodes.MODEL_TYPE_BLOB -> hydrateRows(connection,
                    "SELECT id, content_type, value FROM " + DatabaseSchema.TABLE_BLOB + " WHERE id = ANY(?)",
                    "id", ids, elementsById,
                    (rs, element) -> {
                        Blob blob = (Blob) element;
                        blob.setContentType(rs.getString("content_type"));
                        blob.setValue(rs.getBytes("value"));
                    });
            case EnumCodes.MODEL_TYPE_FILE -> hydrateRows(connection,
                    "SELECT id, content_type, value FROM " + DatabaseSchema.TABLE_FILE + " WHERE id = ANY(?)",
                    "id", ids, elementsById,
                    (rs, element) -> {
                        File file = (File) element;
                        file.setContentType(rs.getString("content_type"));
                        file.setValue(rs.getString("value"));
                    });
            case EnumCodes.MODEL_TYPE_RELATIONSHIP_ELEMENT -> hydrateRelationships(connection, DatabaseSchema.TABLE_RELATIONSHIP_ELEMENT, ids, elementsById);
            case EnumCodes.MODEL_TYPE_ANNOTATED_RELATIONSHIP_ELEMENT -> hydrateRelationships(connection, DatabaseSchema.TABLE_ANNOTATED_RELATIONSHIP_ELEMENT, ids,
                    elementsById);
            case EnumCodes.MODEL_TYPE_SUBMODEL_ELEMENT_LIST -> hydrateLists(connection, ids, elementsById);
            case EnumCodes.MODEL_TYPE_ENTITY -> hydrateEntities(connection, ids, elementsById);
            case EnumCodes.MODEL_TYPE_OPERATION -> hydrateOperations(connection, ids, elementsById);
            case EnumCodes.MODEL_TYPE_BASIC_EVENT_ELEMENT -> hydrateBasicEvents(connection, ids, elementsById);
            default -> {
                // SubmodelElementCollection, Capability: no type-specific values
            }
        }
    }


    private static void hydrateProperties(Connection connection, List<Long> ids, Map<Long, SubmodelElement> elementsById) throws SQLException {
        hydrateRows(connection,
                "SELECT pe.id, pe.value_type,"
                        + " COALESCE(pe.value_text, pe.value_num::text,"
                        + "   CASE WHEN pe.value_bool THEN 'true' WHEN NOT pe.value_bool THEN 'false' END,"
                        + "   pe.value_time::text, pe.value_date::text, pe.value_datetime::text) AS value,"
                        + " ppl.value_id_payload::text AS value_id"
                        + " FROM " + DatabaseSchema.TABLE_PROPERTY + " pe"
                        + " LEFT JOIN " + DatabaseSchema.TABLE_PROPERTY_PAYLOAD + " ppl ON ppl.property_element_id = pe.id"
                        + " WHERE pe.id = ANY(?)",
                "id", ids, elementsById,
                (rs, element) -> {
                    Property property = (Property) element;
                    property.setValueType(EnumCodes.dataTypeDefXsd(rs.getObject("value_type", Integer.class)));
                    property.setValue(rs.getString("value"));
                    property.setValueId(DbJson.read(rs.getString("value_id"), Reference.class));
                });
    }


    private static void hydrateMultiLanguageProperties(Connection connection, List<Long> ids, Map<Long, SubmodelElement> elementsById) throws SQLException {
        hydrateRows(connection,
                "SELECT submodel_element_id, value_id_payload::text AS value_id"
                        + " FROM " + DatabaseSchema.TABLE_MULTILANGUAGE_PROPERTY_PAYLOAD + " WHERE submodel_element_id = ANY(?)",
                "submodel_element_id", ids, elementsById,
                (rs, element) -> ((MultiLanguageProperty) element).setValueId(DbJson.read(rs.getString("value_id"), Reference.class)));
        applyMlpValues(connection, elementsById, ids);
    }


    private static void hydrateRanges(Connection connection, List<Long> ids, Map<Long, SubmodelElement> elementsById) throws SQLException {
        hydrateRows(connection,
                "SELECT id, value_type,"
                        + " COALESCE(min_text, min_num::text, min_time::text, min_date::text, min_datetime::text) AS min_value,"
                        + " COALESCE(max_text, max_num::text, max_time::text, max_date::text, max_datetime::text) AS max_value"
                        + " FROM " + DatabaseSchema.TABLE_RANGE + " WHERE id = ANY(?)",
                "id", ids, elementsById,
                (rs, element) -> {
                    Range range = (Range) element;
                    range.setValueType(EnumCodes.dataTypeDefXsd(rs.getObject("value_type", Integer.class)));
                    range.setMin(rs.getString("min_value"));
                    range.setMax(rs.getString("max_value"));
                });
    }


    private static void hydrateRelationships(Connection connection, String table, List<Long> ids, Map<Long, SubmodelElement> elementsById) throws SQLException {
        hydrateRows(connection,
                "SELECT id, first::text AS first, second::text AS second FROM " + table + " WHERE id = ANY(?)",
                "id", ids, elementsById,
                (rs, element) -> {
                    RelationshipElement relationship = (RelationshipElement) element;
                    relationship.setFirst(DbJson.read(rs.getString("first"), Reference.class));
                    relationship.setSecond(DbJson.read(rs.getString("second"), Reference.class));
                });
    }


    private static void hydrateLists(Connection connection, List<Long> ids, Map<Long, SubmodelElement> elementsById) throws SQLException {
        hydrateRows(connection,
                "SELECT id, order_relevant, semantic_id_list_element::text AS semantic_id, type_value_list_element, value_type_list_element"
                        + " FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT_LIST + " WHERE id = ANY(?)",
                "id", ids, elementsById,
                (rs, element) -> {
                    SubmodelElementList list = (SubmodelElementList) element;
                    list.setOrderRelevant(rs.getObject("order_relevant", Boolean.class));
                    list.setSemanticIdListElement(DbJson.read(rs.getString("semantic_id"), Reference.class));
                    Integer typeValue = rs.getObject("type_value_list_element", Integer.class);
                    list.setTypeValueListElement(typeValue != null && typeValue != EnumCodes.NO_VALUE ? EnumCodes.aasSubmodelElements(typeValue) : null);
                    list.setValueTypeListElement(EnumCodes.dataTypeDefXsd(rs.getObject("value_type_list_element", Integer.class)));
                });
    }


    private static void hydrateEntities(Connection connection, List<Long> ids, Map<Long, SubmodelElement> elementsById) throws SQLException {
        hydrateRows(connection,
                "SELECT id, entity_type, global_asset_id, specific_asset_ids::text AS specific_asset_ids"
                        + " FROM " + DatabaseSchema.TABLE_ENTITY + " WHERE id = ANY(?)",
                "id", ids, elementsById,
                (rs, element) -> {
                    Entity entity = (Entity) element;
                    Integer entityType = rs.getObject("entity_type", Integer.class);
                    entity.setEntityType(EnumCodes.entityType(entityType != null && entityType != EnumCodes.NO_VALUE ? entityType : null));
                    entity.setGlobalAssetId(rs.getString("global_asset_id"));
                    String specificAssetIds = rs.getString("specific_asset_ids");
                    if (specificAssetIds != null) {
                        entity.setSpecificAssetIds(new ArrayList<>(DbJson.readList(specificAssetIds, SpecificAssetId.class)));
                    }
                });
    }


    private static void hydrateOperations(Connection connection, List<Long> ids, Map<Long, SubmodelElement> elementsById) throws SQLException {
        hydrateRows(connection,
                "SELECT id, input_variables::text AS in_vars, output_variables::text AS out_vars, inoutput_variables::text AS inout_vars"
                        + " FROM " + DatabaseSchema.TABLE_OPERATION + " WHERE id = ANY(?)",
                "id", ids, elementsById,
                (rs, element) -> {
                    Operation operation = (Operation) element;
                    String in = rs.getString("in_vars");
                    if (in != null) {
                        operation.setInputVariables(new ArrayList<>(DbJson.readList(in, OperationVariable.class)));
                    }
                    String out = rs.getString("out_vars");
                    if (out != null) {
                        operation.setOutputVariables(new ArrayList<>(DbJson.readList(out, OperationVariable.class)));
                    }
                    String inout = rs.getString("inout_vars");
                    if (inout != null) {
                        operation.setInoutputVariables(new ArrayList<>(DbJson.readList(inout, OperationVariable.class)));
                    }
                });
    }


    private static void hydrateBasicEvents(Connection connection, List<Long> ids, Map<Long, SubmodelElement> elementsById) throws SQLException {
        hydrateRows(connection,
                "SELECT id, observed::text AS observed, direction, state, message_topic, message_broker::text AS message_broker,"
                        + " last_update, min_interval::text AS min_interval, max_interval::text AS max_interval"
                        + " FROM " + DatabaseSchema.TABLE_BASIC_EVENT + " WHERE id = ANY(?)",
                "id", ids, elementsById,
                (rs, element) -> {
                    BasicEventElement event = (BasicEventElement) element;
                    event.setObserved(DbJson.read(rs.getString("observed"), Reference.class));
                    Integer direction = rs.getObject("direction", Integer.class);
                    event.setDirection(EnumCodes.direction(Objects.equals(direction, EnumCodes.NO_VALUE) ? null : direction));
                    Integer state = rs.getObject("state", Integer.class);
                    event.setState(EnumCodes.stateOfEvent(Objects.equals(state, EnumCodes.NO_VALUE) ? null : state));
                    event.setMessageTopic(rs.getString("message_topic"));
                    event.setMessageBroker(DbJson.read(rs.getString("message_broker"), Reference.class));
                    OffsetDateTime lastUpdate = rs.getObject("last_update", OffsetDateTime.class);
                    event.setLastUpdate(lastUpdate != null ? Instant.from(lastUpdate).toString() : null);
                    event.setMinInterval(rs.getString("min_interval"));
                    event.setMaxInterval(rs.getString("max_interval"));
                });
    }
}
