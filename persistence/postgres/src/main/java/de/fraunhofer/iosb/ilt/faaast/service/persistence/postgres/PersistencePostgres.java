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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.DeserializerWrapper;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AssetAdministrationShellSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.ConceptDescriptionSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelElementSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.util.PersistenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.util.QueryModifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.CollectionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.HasSemantics;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


/**
 * Persistence implementation for Postgres DB.
 */
public class PersistencePostgres implements Persistence<PersistencePostgresConfig> {

    private static final String MSG_ID_NOT_NULL = "id must be non-null";
    private static final String MSG_MODIFIER_NOT_NULL = "modifier must be non-null";
    private static final String MSG_CRITERIA_NOT_NULL = "criteria must be non-null";
    private static final String MSG_PAGING_NOT_NULL = "paging must be non-null";
    private static final String MSG_ELEMENT_NOT_NULL = "element must be non-null";
    private static final String ILLEGAL_TYPE = "illegal type for identifiable: %s. Must be one of: %s, %s, %s";
    private static final String SELECT_CONTENT = "SELECT content FROM ";
    private static final String CONTENT = "content";

    private PersistencePostgresConfig config;
    private HikariDataSource dataSource;

    private final JsonSerializer jsonSerializer = new JsonSerializer();
    private final DeserializerWrapper jsonDeserializer = new DeserializerWrapper();
    private final ObjectMapper varsMapper = new ObjectMapper();

    @Override
    public void init(CoreConfig coreConfig, PersistencePostgresConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
    }


    @Override
    public void start() throws PersistenceException {
        try {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getJdbcUrl());
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());
            hikariConfig.setMaximumPoolSize(10);
            this.dataSource = new HikariDataSource(hikariConfig);

            try (Connection connection = dataSource.getConnection()) {
                DatabaseSchema.createTables(connection);
            }

            if (config.loadInitialModel() != null) {
                if (config.getOverride()) {
                    deleteAll();
                    save(config.loadInitialModel());
                }
                else if (isDatabaseEmpty()) {
                    save(config.loadInitialModel());
                }
            }

        }
        catch (SQLException | InvalidConfigurationException | DeserializationException e) {
            throw new PersistenceException("Database connection failed", e);
        }
    }


    private boolean isDatabaseEmpty() throws SQLException {
        try (Connection c = dataSource.getConnection();
                Statement stmt = c.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1 FROM " + DatabaseSchema.TABLE_AAS + " LIMIT 1")) {
            return !rs.next();
        }
    }


    @Override
    public PersistencePostgresConfig asConfig() {
        return config;
    }


    @Override
    public void stop() {
        if (dataSource != null) {
            dataSource.close();
        }
    }


    private AssetAdministrationShell getAssetAdministrationShell(String id) throws ResourceNotFoundException, PersistenceException {
        return loadEntity(DatabaseSchema.TABLE_AAS, id, AssetAdministrationShell.class);
    }


    @Override
    public AssetAdministrationShell getAssetAdministrationShell(String id, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
        return prepareResult(
                getAssetAdministrationShell(id), modifier);
    }


    @Override
    public Page<Reference> getSubmodelRefs(String aasId, PagingInfo paging) throws ResourceNotFoundException, PersistenceException {
        AssetAdministrationShell shell = getAssetAdministrationShell(aasId);
        List<Reference> refs = shell.getSubmodels() != null ? shell.getSubmodels() : new ArrayList<>();
        return preparePagedResult(refs.stream(), paging);
    }


    @Override
    public Page<AssetAdministrationShell> findAssetAdministrationShells(AssetAdministrationShellSearchCriteria criteria, QueryModifier modifier, PagingInfo paging)
            throws PersistenceException {
        List<SqlCondition> conditions = new ArrayList<>();
        if (criteria != null) {
            if (criteria.getIdShort() != null) {
                conditions.add(idShortCondition(criteria.getIdShort()));
            }
            if (criteria.getAssetIds() != null && !criteria.getAssetIds().isEmpty()) {
                conditions.addAll(assetIdConditions(criteria.getAssetIds()));
            }
        }
        return findEntities(DatabaseSchema.TABLE_AAS, AssetAdministrationShell.class, conditions, modifier, paging);
    }


    private static SqlCondition idShortCondition(String idShort) {
        return new SqlCondition(DatabaseSchema.COLUMN_ID_SHORT + " = ?", List.of(idShort));
    }


    /**
     * Builds SQL conditions matching the asset id criteria: the shell must match any of the global asset ids (if set)
     * and any of the specific asset ids (if set). Specific asset ids are matched via JSONB containment, i.e. a stored
     * specificAssetId matches if it has at least the requested name and value.
     */
    private List<SqlCondition> assetIdConditions(List<AssetIdentification> assetIds) throws PersistenceException {
        List<String> globalAssetIds = new ArrayList<>();
        List<SpecificAssetId> specificAssetIds = new ArrayList<>();
        PersistenceHelper.splitAssetIdsIntoGlobalAndSpecificIds(assetIds, globalAssetIds, specificAssetIds);
        List<SqlCondition> result = new ArrayList<>();
        if (!globalAssetIds.isEmpty()) {
            result.add(new SqlCondition(
                    "content #>> '{assetInformation,globalAssetId}' IN (" + String.join(", ", Collections.nCopies(globalAssetIds.size(), "?")) + ")",
                    globalAssetIds));
        }
        if (!specificAssetIds.isEmpty()) {
            List<String> params = new ArrayList<>();
            try {
                for (SpecificAssetId specificAssetId: specificAssetIds) {
                    params.add("[" + jsonSerializer.write(specificAssetId) + "]");
                }
            }
            catch (Exception e) {
                throw new PersistenceException("Failed to serialize specificAssetId filter", e);
            }
            result.add(new SqlCondition(
                    "(" + String.join(" OR ", Collections.nCopies(params.size(), "content #> '{assetInformation,specificAssetIds}' @> ?::jsonb")) + ")",
                    params));
        }
        return result;
    }


    @Override
    public void save(AssetAdministrationShell shell) throws PersistenceException {
        saveEntity(DatabaseSchema.TABLE_AAS, shell.getId(), shell,
                new IndexColumn(DatabaseSchema.COLUMN_ID_SHORT, shell.getIdShort()));
    }


    @Override
    public void deleteAssetAdministrationShell(String id) throws ResourceNotFoundException, PersistenceException {
        deleteEntity(DatabaseSchema.TABLE_AAS, id);
    }


    private Submodel getSubmodel(String id) throws ResourceNotFoundException, PersistenceException {
        return loadEntity(DatabaseSchema.TABLE_SUBMODEL, id, Submodel.class);
    }


    @Override
    public Submodel getSubmodel(String id, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
        Submodel submodel = getSubmodel(id);
        if (modifier.getExtent() == Extent.WITH_BLOB_VALUE) {
            injectBlobValues(id, submodel);
        }
        return prepareResult(submodel, modifier);
    }


    @Override
    public Page<Submodel> findSubmodels(SubmodelSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws PersistenceException {
        List<SqlCondition> conditions = new ArrayList<>();
        if (criteria != null) {
            if (criteria.getIdShort() != null) {
                conditions.add(idShortCondition(criteria.getIdShort()));
            }
            if (criteria.getSemanticId() != null) {
                String semanticId = semanticIdAsString(criteria.getSemanticId());
                conditions.add(semanticId != null
                        ? new SqlCondition(DatabaseSchema.COLUMN_SEMANTIC_ID + " = ?", List.of(semanticId))
                        : new SqlCondition(DatabaseSchema.COLUMN_SEMANTIC_ID + " IS NULL", List.of()));
            }
        }
        Page<Submodel> result = findEntities(DatabaseSchema.TABLE_SUBMODEL, Submodel.class, conditions, modifier, paging);
        if (modifier.getExtent() == Extent.WITH_BLOB_VALUE) {
            for (Submodel submodel: result.getContent()) {
                injectBlobValues(submodel.getId(), submodel);
            }
        }
        return result;
    }


    /**
     * Canonical string form of a semanticId used for indexed equality matching in the database. Includes key types and
     * key values but ignores the reference type and referredSemanticId, approximating
     * {@link ReferenceHelper#equals(Reference, Reference)} (which additionally tolerates compatible key types and
     * compares referredSemanticId).
     */
    private static String semanticIdAsString(Reference semanticId) {
        return ReferenceHelper.toString(semanticId, false, false);
    }


    @Override
    public void save(Submodel submodel) throws PersistenceException {
        Submodel toStore = submodel;
        Map<String, byte[]> blobs = Map.of();
        if (BlobExternalization.containsExternalizableBlobValue(submodel)) {
            // work on a copy so the caller's object keeps its blob values
            toStore = DeepCopyHelper.deepCopy(submodel, Submodel.class);
            blobs = BlobExternalization.externalizeBlobValues(toStore);
        }
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try {
                upsertEntity(c, DatabaseSchema.TABLE_SUBMODEL, toStore.getId(), toStore,
                        new IndexColumn(DatabaseSchema.COLUMN_ID_SHORT, toStore.getIdShort()),
                        new IndexColumn(DatabaseSchema.COLUMN_SEMANTIC_ID, semanticIdAsString(toStore.getSemanticId())));
                insertBlobContents(c, toStore.getId(), blobs);
                c.commit();
            }
            catch (Exception e) {
                c.rollback();
                throw e;
            }
            finally {
                c.setAutoCommit(true);
            }
        }
        catch (Exception e) {
            throw new PersistenceException("Failed to save submodel: " + submodel.getId(), e);
        }
    }


    @Override
    public void deleteSubmodel(String id) throws ResourceNotFoundException, PersistenceException {
        deleteEntity(DatabaseSchema.TABLE_SUBMODEL, id);
    }


    @Override
    public SubmodelElement getSubmodelElement(SubmodelElementIdentifier identifier, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(identifier, MSG_ID_NOT_NULL);
        List<String> steps = identifier.getIdShortPath().getElements();
        if (steps.isEmpty()) {
            throw new ResourceNotFoundException(identifier.toReference());
        }
        String sql = "SELECT jsonb_path_query_first(content, ?::jsonpath, ?::jsonb) AS element FROM " + DatabaseSchema.TABLE_SUBMODEL + " WHERE id = ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement pstmt = c.prepareStatement(sql)) {
            Map<String, String> vars = new LinkedHashMap<>();
            pstmt.setString(1, buildElementJsonPath(steps, vars));
            pstmt.setString(2, varsMapper.writeValueAsString(vars));
            pstmt.setString(3, identifier.getSubmodelId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next() || rs.getString("element") == null) {
                    throw new ResourceNotFoundException(identifier.toReference());
                }
                SubmodelElement element = jsonDeserializer.read(rs.getString("element"), SubmodelElement.class);
                if (modifier.getExtent() == Extent.WITH_BLOB_VALUE) {
                    injectBlobValues(identifier.getSubmodelId(), element);
                }
                return prepareResult(element, modifier);
            }
        }
        catch (ResourceNotFoundException e) {
            throw e;
        }
        catch (Exception e) {
            throw new PersistenceException("Database error loading submodel element", e);
        }
    }


    /**
     * Builds a SQL/JSON path expression addressing the element identified by the given idShort path within a submodel
     * document. idShort steps become filter expressions referencing variables added to vars (so values are passed as
     * query parameters, not embedded in the path), index steps like [3] become numeric array accessors.
     */
    private static String buildElementJsonPath(List<String> steps, Map<String, String> vars) {
        StringBuilder path = new StringBuilder("$");
        String container = "submodelElements";
        for (int i = 0; i < steps.size(); i++) {
            String step = steps.get(i);
            if (isIndexSegment(step)) {
                path.append(".\"").append(container).append("\"[").append(step, 1, step.length() - 1).append("]");
            }
            else {
                String var = "k" + i;
                vars.put(var, step);
                path.append(".\"").append(container).append("\"[*] ? (@.\"idShort\" == $").append(var).append(")");
            }
            container = "value";
        }
        return path.toString();
    }


    private static boolean isIndexSegment(String step) {
        return step.matches("\\[\\d+\\]");
    }


    @Override
    public Page<SubmodelElement> findSubmodelElements(SubmodelElementSearchCriteria criteria, QueryModifier modifier,
                                                      PagingInfo paging)
            throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(criteria, MSG_CRITERIA_NOT_NULL);
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(paging, MSG_PAGING_NOT_NULL);

        if (!criteria.isParentSet()) {
            return findSubmodelElementsViaIndex(criteria, modifier, paging);
        }

        List<SubmodelElement> elements = new ArrayList<>();
        if (criteria.getParent().getSubmodelId() != null) {
            Submodel submodel = getSubmodel(criteria.getParent().getSubmodelId());
            if (modifier.getExtent() == Extent.WITH_BLOB_VALUE) {
                injectBlobValues(submodel.getId(), submodel);
            }
            Referable parent = EnvironmentHelper.resolve(criteria.getParent().toReference(), submodel,
                    Referable.class);
            if (Submodel.class.isAssignableFrom(parent.getClass())) {
                elements.addAll(((Submodel) parent).getSubmodelElements());
            }
            else if (SubmodelElementCollection.class.isAssignableFrom(parent.getClass())) {
                elements.addAll(((SubmodelElementCollection) parent).getValue());
            }
            else if (SubmodelElementList.class.isAssignableFrom(parent.getClass())) {
                elements.addAll(((SubmodelElementList) parent).getValue());
            }
        }

        Stream<SubmodelElement> result = elements.stream();
        if (criteria.isSemanticIdSet()) {
            result = filterBySemanticId(result, criteria.getSemanticId());
        }
        if (criteria.getValueOnly()) {
            result = filterByHasValueOnlySerialization(result);
        }
        return preparePagedResult(result, modifier, paging);
    }


    /**
     * Searches submodel elements across all submodels (at any nesting depth) using the submodel element index table.
     * The index is only used to locate matching elements - the elements themselves are extracted from the JSONB
     * submodel documents via the indexed jsonb path, so only matches are transferred and deserialized. Results are in
     * document order per submodel, submodels in insertion order. SemanticId matching uses the same canonical string
     * form as {@link #semanticIdAsString(Reference)} and includes supplemental semantic ids, mirroring the in-memory
     * implementation.
     */
    private Page<SubmodelElement> findSubmodelElementsViaIndex(SubmodelElementSearchCriteria criteria, QueryModifier modifier, PagingInfo paging)
            throws PersistenceException {
        StringBuilder sql = new StringBuilder("SELECT s.content #> e." + DatabaseSchema.COLUMN_DOC_PATH + " AS element, e.submodel_id FROM "
                + DatabaseSchema.TABLE_SUBMODEL_ELEMENT_INDEX + " e JOIN " + DatabaseSchema.TABLE_SUBMODEL + " s ON s.id = e.submodel_id");
        List<String> parameters = new ArrayList<>();
        if (criteria.isSemanticIdSet() && criteria.getSemanticId() != null) {
            String semanticId = semanticIdAsString(criteria.getSemanticId());
            if (semanticId != null) {
                sql.append(" WHERE (e.").append(DatabaseSchema.COLUMN_SEMANTIC_ID).append(" = ? OR ? = ANY(e.supplemental_semantic_ids))");
                parameters.add(semanticId);
                parameters.add(semanticId);
            }
            else {
                sql.append(" WHERE e.").append(DatabaseSchema.COLUMN_SEMANTIC_ID).append(" IS NULL");
            }
        }
        sql.append(" ORDER BY s.seq, e.ord_path");
        List<SubmodelElement> elements = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement pstmt = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setString(i + 1, parameters.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SubmodelElement element = jsonDeserializer.read(rs.getString("element"), SubmodelElement.class);
                    if (modifier.getExtent() == Extent.WITH_BLOB_VALUE) {
                        injectBlobValues(rs.getString("submodel_id"), element);
                    }
                    elements.add(element);
                }
            }
        }
        catch (Exception e) {
            throw new PersistenceException("Database error searching submodel elements", e);
        }
        Stream<SubmodelElement> result = elements.stream();
        if (criteria.getValueOnly()) {
            result = filterByHasValueOnlySerialization(result);
        }
        return preparePagedResult(result, modifier, paging);
    }


    private static <T> Stream<T> filterByHasValueOnlySerialization(Stream<T> stream) {
        return stream.filter(ElementValueHelper::isValueOnlySupported);
    }


    private static <T extends HasSemantics> Stream<T> filterBySemanticId(Stream<T> stream, Reference semanticId) {
        if (Objects.isNull(semanticId)) {
            return stream;
        }
        return stream.filter(x -> ReferenceHelper.equals(x.getSemanticId(), semanticId)
                || Optional.ofNullable(x.getSupplementalSemanticIds())
                        .orElse(List.of()).stream()
                        .anyMatch(y -> ReferenceHelper.equals(y, semanticId)));
    }


    @Override
    public void insert(SubmodelElementIdentifier parentIdentifier, SubmodelElement submodelElement)
            throws ResourceNotFoundException, ResourceNotAContainerElementException, ResourceAlreadyExistsException, PersistenceException {
        Ensure.requireNonNull(parentIdentifier, MSG_ID_NOT_NULL);
        Ensure.requireNonNull(submodelElement, MSG_ELEMENT_NOT_NULL);

        Submodel submodel = getSubmodel(parentIdentifier.getSubmodelId());
        Referable parent = EnvironmentHelper.resolve(parentIdentifier.toReference(), submodel, Referable.class);

        Collection<SubmodelElement> container;
        boolean acceptEmptyIdShort = false;
        if (Submodel.class.isAssignableFrom(parent.getClass())) {
            container = ((Submodel) parent).getSubmodelElements();
        }
        else if (SubmodelElementCollection.class.isAssignableFrom(parent.getClass())) {
            container = ((SubmodelElementCollection) parent).getValue();
        }
        else if (SubmodelElementList.class.isAssignableFrom(parent.getClass())) {
            container = ((SubmodelElementList) parent).getValue();
            acceptEmptyIdShort = true;
        }
        else {
            throw new IllegalArgumentException(String.format(ILLEGAL_TYPE,
                    parent.getClass(),
                    Submodel.class,
                    SubmodelElementCollection.class,
                    SubmodelElementList.class));
        }
        if (!acceptEmptyIdShort && StringHelper.isBlank(submodelElement.getIdShort())) {
            throw new IllegalArgumentException("idShort most be non-empty");
        }
        CollectionHelper.put(container,
                container.stream()
                        .filter(StringHelper.isBlank(submodelElement.getIdShort())
                                ? x -> false
                                : x -> !StringHelper.isBlank(x.getIdShort())
                                        && x.getIdShort().equalsIgnoreCase(submodelElement.getIdShort()))
                        .findFirst()
                        .orElse(null),
                submodelElement);
        save(submodel);
    }


    @Override
    public void update(SubmodelElementIdentifier identifier, SubmodelElement submodelElement) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(identifier, MSG_ID_NOT_NULL);
        Ensure.requireNonNull(submodelElement, MSG_ELEMENT_NOT_NULL);
        List<String> steps = identifier.getIdShortPath().getElements();
        if (steps.isEmpty()) {
            throw new ResourceNotFoundException(identifier.toReference());
        }
        SubmodelElement toStore = submodelElement;
        Map<String, byte[]> blobs = Map.of();
        if (BlobExternalization.containsExternalizableBlobValue(submodelElement)) {
            // work on a copy so the caller's object keeps its blob values
            toStore = DeepCopyHelper.deepCopy(submodelElement, SubmodelElement.class);
            blobs = BlobExternalization.externalizeBlobValues(toStore);
        }
        // single atomic update statement: the row lock makes concurrent updates to different elements of
        // the same submodel serialize instead of overwriting each other
        String sql = "UPDATE " + DatabaseSchema.TABLE_SUBMODEL
                + " SET content = jsonb_set(content, " + DatabaseSchema.FUNCTION_RESOLVE_PATH + "(content, ?), ?::jsonb)"
                + " WHERE id = ? AND " + DatabaseSchema.FUNCTION_RESOLVE_PATH + "(content, ?) IS NOT NULL";
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement pstmt = c.prepareStatement(sql)) {
                Array stepsArray = c.createArrayOf("text", steps.toArray(new String[0]));
                pstmt.setArray(1, stepsArray);
                pstmt.setString(2, jsonSerializer.write(toStore));
                pstmt.setString(3, identifier.getSubmodelId());
                pstmt.setArray(4, stepsArray);
                if (pstmt.executeUpdate() == 0) {
                    throw new ResourceNotFoundException(identifier.toReference());
                }
                insertBlobContents(c, identifier.getSubmodelId(), blobs);
                c.commit();
            }
            catch (Exception e) {
                c.rollback();
                throw e;
            }
            finally {
                c.setAutoCommit(true);
            }
        }
        catch (ResourceNotFoundException e) {
            throw e;
        }
        catch (Exception e) {
            throw new PersistenceException("Database error updating submodel element", e);
        }
    }


    @Override
    public void deleteSubmodelElement(SubmodelElementIdentifier identifier) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(identifier, MSG_ID_NOT_NULL);
        List<String> steps = identifier.getIdShortPath().getElements();
        if (steps.isEmpty()) {
            throw new ResourceNotFoundException(identifier.toReference());
        }
        String sql = "UPDATE " + DatabaseSchema.TABLE_SUBMODEL
                + " SET content = content #- " + DatabaseSchema.FUNCTION_RESOLVE_PATH + "(content, ?)"
                + " WHERE id = ? AND " + DatabaseSchema.FUNCTION_RESOLVE_PATH + "(content, ?) IS NOT NULL";
        try (Connection c = dataSource.getConnection(); PreparedStatement pstmt = c.prepareStatement(sql)) {
            Array stepsArray = c.createArrayOf("text", steps.toArray(new String[0]));
            pstmt.setArray(1, stepsArray);
            pstmt.setString(2, identifier.getSubmodelId());
            pstmt.setArray(3, stepsArray);
            if (pstmt.executeUpdate() == 0) {
                throw new ResourceNotFoundException(identifier.toReference());
            }
        }
        catch (ResourceNotFoundException e) {
            throw e;
        }
        catch (SQLException e) {
            throw new PersistenceException("Database error deleting submodel element", e);
        }
    }


    private ConceptDescription getConceptDescription(String id) throws ResourceNotFoundException, PersistenceException {
        return loadEntity(DatabaseSchema.TABLE_CONCEPT_DESCRIPTION, id, ConceptDescription.class);
    }


    @Override
    public ConceptDescription getConceptDescription(String id, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
        return prepareResult(
                getConceptDescription(id),
                modifier);
    }


    @Override
    public Page<ConceptDescription> findConceptDescriptions(ConceptDescriptionSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws PersistenceException {
        List<SqlCondition> conditions = new ArrayList<>();
        if (criteria != null && criteria.getIdShort() != null) {
            conditions.add(idShortCondition(criteria.getIdShort()));
        }
        if (criteria != null && (criteria.getIsCaseOf() != null || criteria.getDataSpecification() != null)) {
            // reference comparisons that cannot be expressed as indexed SQL equality - filter in
            // memory, but still apply the SQL-able conditions in the query
            Stream<ConceptDescription> stream = loadAllEntities(DatabaseSchema.TABLE_CONCEPT_DESCRIPTION, ConceptDescription.class, conditions).stream();
            if (criteria.getIsCaseOf() != null) {
                stream = stream.filter(x -> x.getIsCaseOf() != null && x.getIsCaseOf().contains(criteria.getIsCaseOf()));
            }
            if (criteria.getDataSpecification() != null) {
                stream = stream.filter(x -> x.getEmbeddedDataSpecifications() != null &&
                        x.getEmbeddedDataSpecifications().stream().anyMatch(
                                d -> Objects.equals(d.getDataSpecification(), criteria.getDataSpecification())));
            }
            return preparePagedResult(stream, modifier, paging);
        }
        return findEntities(DatabaseSchema.TABLE_CONCEPT_DESCRIPTION, ConceptDescription.class, conditions, modifier, paging);
    }


    @Override
    public void save(ConceptDescription conceptDescription) throws PersistenceException {
        saveEntity(DatabaseSchema.TABLE_CONCEPT_DESCRIPTION, conceptDescription.getId(), conceptDescription,
                new IndexColumn(DatabaseSchema.COLUMN_ID_SHORT, conceptDescription.getIdShort()));
    }


    @Override
    public void deleteConceptDescription(String id) throws ResourceNotFoundException, PersistenceException {
        deleteEntity(DatabaseSchema.TABLE_CONCEPT_DESCRIPTION, id);
    }


    @Override
    public OperationResult getOperationResult(OperationHandle handle) throws ResourceNotFoundException, PersistenceException {
        return loadEntity(DatabaseSchema.TABLE_OPERATION_RESULT, handle.getHandleId(), OperationResult.class);
    }


    @Override
    public void save(OperationHandle handle, OperationResult result) throws PersistenceException {
        saveEntity(DatabaseSchema.TABLE_OPERATION_RESULT, handle.getHandleId(), result);
    }


    private void save(Environment environment) throws PersistenceException {
        if (environment == null) {
            return;
        }
        if (environment.getAssetAdministrationShells() != null) {
            for (AssetAdministrationShell shell: environment.getAssetAdministrationShells()) {
                save(shell);
            }
        }
        if (environment.getSubmodels() != null) {
            for (Submodel submodel: environment.getSubmodels()) {
                save(submodel);
            }
        }
        if (environment.getConceptDescriptions() != null) {
            for (ConceptDescription conceptDescription: environment.getConceptDescriptions()) {
                save(conceptDescription);
            }
        }
    }


    @Override
    public void deleteAll() throws PersistenceException {
        try (Connection c = dataSource.getConnection()) {
            DatabaseSchema.dropTables(c);
            DatabaseSchema.createTables(c);
        }
        catch (SQLException e) {
            throw new PersistenceException("Failed to clear database", e);
        }
    }


    private <T> void saveEntity(String table, String id, T entity, IndexColumn... indexColumns) throws PersistenceException {
        try {
            try (Connection c = dataSource.getConnection()) {
                upsertEntity(c, table, id, entity, indexColumns);
            }
        }
        catch (Exception e) {
            throw new PersistenceException("Failed to save to " + table + ": " + id, e);
        }
    }


    private <T> void upsertEntity(Connection connection, String table, String id, T entity, IndexColumn... indexColumns) throws Exception {
        Ensure.requireNonNull(id, MSG_ID_NOT_NULL);
        String json = jsonSerializer.write(entity);
        StringBuilder columns = new StringBuilder("id, content");
        StringBuilder placeholders = new StringBuilder("?, ?");
        StringBuilder updates = new StringBuilder("content = EXCLUDED.content");
        for (IndexColumn column: indexColumns) {
            columns.append(", ").append(column.name());
            placeholders.append(", ?");
            updates.append(", ").append(column.name()).append(" = EXCLUDED.").append(column.name());
        }
        String sql = "INSERT INTO " + table + " (" + columns + ") VALUES (" + placeholders + ") " +
                "ON CONFLICT (id) DO UPDATE SET " + updates;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, id);
            preparedStatement.setObject(2, json, Types.OTHER);
            int parameterIndex = 3;
            for (IndexColumn column: indexColumns) {
                preparedStatement.setString(parameterIndex++, column.value());
            }
            preparedStatement.executeUpdate();
        }
    }


    /**
     * Inserts externalized blob content rows. Must be called in the same transaction as the document write and after
     * it, so the foreign key on the submodel is satisfied and the index trigger's orphan cleanup (which runs on the
     * document write) does not see the new rows.
     */
    private void insertBlobContents(Connection connection, String submodelId, Map<String, byte[]> blobs) throws SQLException {
        if (blobs.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO " + DatabaseSchema.TABLE_BLOB_STORE + " (submodel_id, blob_id, content) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Map.Entry<String, byte[]> blob: blobs.entrySet()) {
                pstmt.setString(1, submodelId);
                pstmt.setString(2, blob.getKey());
                pstmt.setBytes(3, blob.getValue());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }


    /**
     * Resolves externalization placeholders in the element tree back to the stored blob content. Placeholders without
     * a matching blob store row are left untouched.
     */
    private void injectBlobValues(String submodelId, Referable root) throws PersistenceException {
        Map<String, List<Blob>> placeholders = BlobExternalization.findBlobPlaceholders(root);
        if (placeholders.isEmpty()) {
            return;
        }
        String sql = "SELECT blob_id, content FROM " + DatabaseSchema.TABLE_BLOB_STORE + " WHERE submodel_id = ? AND blob_id = ANY(?)";
        try (Connection c = dataSource.getConnection(); PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setString(1, submodelId);
            pstmt.setArray(2, c.createArrayOf("text", placeholders.keySet().toArray(new String[0])));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    byte[] content = rs.getBytes("content");
                    placeholders.getOrDefault(rs.getString("blob_id"), List.of())
                            .forEach(blob -> blob.setValue(content));
                }
            }
        }
        catch (SQLException e) {
            throw new PersistenceException("Database error loading blob content for submodel " + submodelId, e);
        }
    }


    private <T> T loadEntity(String table, String id, Class<T> clazz) throws ResourceNotFoundException, PersistenceException {
        String sql = SELECT_CONTENT + table + " WHERE id = ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement statement = c.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return jsonDeserializer.read(rs.getString(CONTENT), clazz);
                }
                else {
                    throw new ResourceNotFoundException(clazz.getSimpleName() + " with id " + id + " not found");
                }
            }
        }
        catch (ResourceNotFoundException e) {
            throw e;
        }
        catch (Exception e) {
            throw new PersistenceException("Database error loading " + id, e);
        }
    }


    private <T> List<T> loadAllEntities(String table, Class<T> clazz, List<SqlCondition> conditions) throws PersistenceException {
        List<T> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_CONTENT).append(table);
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(conditions.stream().map(SqlCondition::clause).collect(Collectors.joining(" AND ")));
        }
        sql.append(" ORDER BY seq ASC");
        try (Connection c = dataSource.getConnection(); PreparedStatement pstmt = c.prepareStatement(sql.toString())) {
            int parameterIndex = 1;
            for (SqlCondition condition: conditions) {
                for (String parameter: condition.parameters()) {
                    pstmt.setString(parameterIndex++, parameter);
                }
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(jsonDeserializer.read(rs.getString(CONTENT), clazz));
                }
            }
        }
        catch (Exception e) {
            throw new PersistenceException("Database error loading all from " + table, e);
        }
        return results;
    }


    /**
     * Queries one page of entities with all filtering, ordering and pagination done by the database. Pagination uses
     * the cursor as keyset on the seq column ({@code seq > cursor}) instead of an offset, so pages are stable under
     * concurrent inserts and deletes and the database never scans skipped rows. One row more than the limit is fetched
     * to determine whether more data is available.
     */
    private <T extends Referable> Page<T> findEntities(String table, Class<T> clazz, List<SqlCondition> conditions, QueryModifier modifier, PagingInfo paging)
            throws PersistenceException {
        long previousSeq = paging.getCursor() != null ? readCursor(paging.getCursor()) : 0;
        StringBuilder sql = new StringBuilder("SELECT content, seq FROM ").append(table).append(" WHERE seq > ?");
        for (SqlCondition condition: conditions) {
            sql.append(" AND ").append(condition.clause());
        }
        sql.append(" ORDER BY seq ASC");
        if (paging.hasLimit()) {
            sql.append(" LIMIT ?");
        }
        List<T> content = new ArrayList<>();
        long lastSeq = previousSeq;
        boolean hasMore = false;
        try (Connection c = dataSource.getConnection(); PreparedStatement pstmt = c.prepareStatement(sql.toString())) {
            int parameterIndex = 1;
            pstmt.setLong(parameterIndex++, previousSeq);
            for (SqlCondition condition: conditions) {
                for (String parameter: condition.parameters()) {
                    pstmt.setString(parameterIndex++, parameter);
                }
            }
            if (paging.hasLimit()) {
                pstmt.setLong(parameterIndex, paging.getLimit() + 1);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    if (paging.hasLimit() && content.size() >= paging.getLimit()) {
                        hasMore = true;
                        break;
                    }
                    content.add(jsonDeserializer.read(rs.getString(CONTENT), clazz));
                    lastSeq = rs.getLong("seq");
                }
            }
        }
        catch (Exception e) {
            throw new PersistenceException("Database error querying " + table, e);
        }
        return Page.<T> builder()
                .result(QueryModifierHelper.applyQueryModifier(content, modifier))
                .metadata(PagingMetadata.builder()
                        .cursor(hasMore ? writeCursor(lastSeq) : null)
                        .build())
                .build();
    }


    private void deleteEntity(String table, String id) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(id, MSG_ID_NOT_NULL);
        String sql = "DELETE FROM " + table + " WHERE id = ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setString(1, id);
            if (pstmt.executeUpdate() == 0) {
                throw new ResourceNotFoundException(String.format("resource not found (id %s)", id));
            }
        }
        catch (SQLException e) {
            throw new PersistenceException("Database error deleting " + id, e);
        }
    }


    private static <T extends Referable> Page<T> preparePagedResult(Stream<T> input, QueryModifier modifier, PagingInfo paging) {
        Page<T> result = preparePagedResult(input, paging);
        result.setContent(QueryModifierHelper.applyQueryModifier(
                result.getContent().stream()
                        .map(DeepCopyHelper::deepCopy)
                        .toList(),
                modifier));
        return result;
    }


    private static <T> Page<T> preparePagedResult(Stream<T> input, PagingInfo paging) {
        Stream<T> result = input;
        if (Objects.nonNull(paging.getCursor())) {
            result = result.skip(readCursor(paging.getCursor()));
        }
        if (paging.hasLimit()) {
            result = result.limit(paging.getLimit() + 1);
        }
        List<T> temp = result.toList();
        return Page.<T> builder()
                .result(temp.stream()
                        .limit(paging.hasLimit() ? paging.getLimit() : temp.size())
                        .toList())
                .metadata(PagingMetadata.builder()
                        .cursor(nextCursor(paging, temp.size()))
                        .build())
                .build();
    }


    private static long readCursor(String cursor) {
        return Long.parseLong(cursor);
    }


    private static String writeCursor(long index) {
        return Long.toString(index);
    }


    private static String nextCursor(PagingInfo paging, boolean hasMoreData) {
        if (!hasMoreData) {
            return null;
        }
        if (!paging.hasLimit()) {
            throw new IllegalStateException("unable to generate next cursor for paging - there should not be more data available if previous request did not have a limit set");
        }
        if (Objects.isNull(paging.getCursor())) {
            return writeCursor(paging.getLimit());
        }
        return writeCursor(readCursor(paging.getCursor()) + paging.getLimit());
    }


    private static String nextCursor(PagingInfo paging, int resultCount) {
        return nextCursor(paging, paging.hasLimit() && resultCount > paging.getLimit());
    }


    private static <T extends Referable> T prepareResult(T result, QueryModifier modifier) {
        return QueryModifierHelper.applyQueryModifier(
                DeepCopyHelper.deepCopy(result),
                modifier);
    }

    /**
     * A queryable column holding a value extracted from the entity on save, kept in sync with the JSONB content.
     */
    private record IndexColumn(String name, String value) {}

    /**
     * A SQL filter fragment with positional parameters, e.g. {@code id_short = ?}. All parameters are bound as
     * strings.
     */
    private record SqlCondition(String clause, List<String> parameters) {}
}
