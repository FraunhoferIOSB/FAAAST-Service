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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
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
import de.fraunhofer.iosb.ilt.faaast.service.persistence.TransactionalAction;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.util.PersistenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.util.QueryModifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Persistence implementation for Postgres DB (schema management: {@link DatabaseSchema}).
 * Identifiables are decomposed into normalized tables; submodel elements are stored as one row per
 * element with a materialized idShort path, so single elements can be read, replaced and deleted without touching the
 * rest of the submodel.
 */
public class PersistencePostgres implements Persistence<PersistencePostgresConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistencePostgres.class);
    private static final String MSG_ID_NOT_NULL = "id must be non-null";
    private static final String MSG_MODIFIER_NOT_NULL = "modifier must be non-null";
    private static final String MSG_CRITERIA_NOT_NULL = "criteria must be non-null";
    private static final String MSG_PAGING_NOT_NULL = "paging must be non-null";
    private static final String MSG_ELEMENT_NOT_NULL = "element must be non-null";
    private static final String ILLEGAL_TYPE = "illegal type for identifiable: %s. Must be one of: %s, %s, %s";

    private PersistencePostgresConfig config;
    private HikariDataSource dataSource;

    private Connection bound;

    /** cleared once the transaction has ended. */
    private boolean valid = true;

    @Override
    public void init(CoreConfig coreConfig, PersistencePostgresConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
    }


    /**
     * Creates the transaction persistence for the given connection.
     */
    private PersistencePostgres boundTo(Connection connection) {
        PersistencePostgres result = new PersistencePostgres();
        result.config = this.config;
        result.dataSource = this.dataSource;
        result.bound = connection;
        return result;
    }


    /**
     * Obtains the connection to use for a single operation in transaction.
     */
    private Connection acquire() throws SQLException {
        if (bound == null) {
            return dataSource.getConnection();
        }
        if (!valid) {
            throw new IllegalStateException("transaction-scoped Persistence used outside the transaction it belongs to");
        }
        return nonClosing(bound);
    }


    private static Connection nonClosing(Connection connection) {
        return (Connection) Proxy.newProxyInstance(
                PersistencePostgres.class.getClassLoader(),
                new Class<?>[] {
                        Connection.class
                },
                (proxy, method, args) -> {
                    if ("close".equals(method.getName()) && (args == null || args.length == 0)) {
                        return null;
                    }
                    try {
                        return method.invoke(connection, args);
                    }
                    catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }


    @Override
    public void start() throws PersistenceException {
        ensureNotBound();
        try {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getJdbcUrl());
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());
            hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
            hikariConfig.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
            // intervals (BasicEventElement min/max interval) are read back in ISO-8601 form
            hikariConfig.setConnectionInitSql("SET intervalstyle = 'iso_8601'");
            this.dataSource = new HikariDataSource(hikariConfig);

            try (Connection connection = acquire()) {
                DatabaseSchema.createSchema(connection);
            }

            if (config.loadInitialModel() != null) {
                // the whole initial load in one transaction
                if (config.getOverride()) {
                    runInTransaction(tx -> {
                        tx.deleteAll();
                        save(tx, config.loadInitialModel());
                    });
                }
                else if (isDatabaseEmpty()) {
                    runInTransaction(tx -> save(tx, config.loadInitialModel()));
                }
            }
        }
        catch (Exception e) {
            throw new PersistenceException("Database connection failed", e);
        }
    }


    private boolean isDatabaseEmpty() throws SQLException {
        try (Connection c = acquire();
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
        ensureNotBound();
        if (dataSource != null) {
            dataSource.close();
        }
    }


    private void ensureNotBound() {
        if (bound != null) {
            throw new UnsupportedOperationException("operations are not available on a transaction Persistence");
        }
    }

    // -------------------------------------------------------------------------------------------------
    // Asset Administration Shells
    // -------------------------------------------------------------------------------------------------


    private AssetAdministrationShell getAssetAdministrationShell(String id) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(id, MSG_ID_NOT_NULL);
        try (Connection c = acquire()) {
            Long dbId = AasDb.findDbId(c, id);
            if (dbId == null) {
                throw new ResourceNotFoundException("AssetAdministrationShell with id " + id + " not found");
            }
            return AasDb.read(c, dbId);
        }
        catch (SQLException e) {
            throw new PersistenceException("Database error loading " + id, e);
        }
    }


    @Override
    public AssetAdministrationShell getAssetAdministrationShell(String id, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
        return prepareResult(getAssetAdministrationShell(id), modifier);
    }


    @Override
    public Page<Reference> getSubmodelRefs(String aasId, PagingInfo paging) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(aasId, MSG_ID_NOT_NULL);
        try (Connection c = acquire()) {
            Long dbId = AasDb.findDbId(c, aasId);
            if (dbId == null) {
                throw new ResourceNotFoundException("AssetAdministrationShell with id " + aasId + " not found");
            }
            return preparePagedResult(AasDb.readSubmodelReferences(c, dbId).stream(), paging);
        }
        catch (SQLException e) {
            throw new PersistenceException("Database error loading submodel references of " + aasId, e);
        }
    }


    @Override
    public Page<AssetAdministrationShell> findAssetAdministrationShells(AssetAdministrationShellSearchCriteria criteria, QueryModifier modifier, PagingInfo paging)
            throws PersistenceException {
        List<SqlCondition> conditions = new ArrayList<>();
        if (criteria != null) {
            if (criteria.getIdShort() != null) {
                conditions.add(new SqlCondition("a.id_short = ?", List.of(criteria.getIdShort())));
            }
            if (criteria.getAssetIds() != null && !criteria.getAssetIds().isEmpty()) {
                conditions.addAll(assetIdConditions(criteria.getAssetIds()));
            }
        }
        String sql = "SELECT a.id FROM " + DatabaseSchema.TABLE_AAS + " a"
                + " LEFT JOIN " + DatabaseSchema.TABLE_ASSET_INFORMATION + " ai ON ai.asset_information_id = a.id"
                + " WHERE a.id > ?";
        return findIdentifiables(sql, "a.id", conditions, modifier, paging, (c, ids) -> readEach(c, ids, AasDb::read));
    }


    /**
     * Builds SQL conditions matching the asset id criteria: the shell must match any of the global asset ids (if set)
     * and any of the specific asset ids (if set). A stored specificAssetId matches if it has the requested name and
     * value.
     */
    private static List<SqlCondition> assetIdConditions(List<AssetIdentification> assetIds) {
        List<String> globalAssetIds = new ArrayList<>();
        List<SpecificAssetId> specificAssetIds = new ArrayList<>();
        PersistenceHelper.splitAssetIdsIntoGlobalAndSpecificIds(assetIds, globalAssetIds, specificAssetIds);
        List<SqlCondition> result = new ArrayList<>();
        if (!globalAssetIds.isEmpty()) {
            result.add(new SqlCondition(
                    "ai.global_asset_id IN (" + "?, ".repeat(globalAssetIds.size() - 1) + "?)",
                    globalAssetIds));
        }
        if (!specificAssetIds.isEmpty()) {
            StringBuilder clause = new StringBuilder("(");
            List<String> parameters = new ArrayList<>();
            for (int i = 0; i < specificAssetIds.size(); i++) {
                if (i > 0) {
                    clause.append(" OR ");
                }
                clause.append("EXISTS (SELECT 1 FROM ").append(DatabaseSchema.TABLE_SPECIFIC_ASSET_ID)
                        .append(" s WHERE s.asset_information_id = a.id AND s.name = ? AND s.value = ?)");
                parameters.add(specificAssetIds.get(i).getName());
                parameters.add(specificAssetIds.get(i).getValue());
            }
            clause.append(")");
            result.add(new SqlCondition(clause.toString(), parameters));
        }
        return result;
    }


    @Override
    public void save(AssetAdministrationShell shell) throws PersistenceException {
        Ensure.requireNonNull(shell.getId(), MSG_ID_NOT_NULL);
        try {
            inOwnTransaction(c -> AasDb.save(c, shell), "Failed to save AssetAdministrationShell: " + shell.getId());
        }
        catch (ResourceNotFoundException e) {
            throw new PersistenceException("Failed to save AssetAdministrationShell: " + shell.getId(), e);
        }
    }


    @Override
    public void deleteAssetAdministrationShell(String id) throws ResourceNotFoundException, PersistenceException {
        deleteIdentifiable(DatabaseSchema.TABLE_AAS, "aas_id", id);
    }

    // -------------------------------------------------------------------------------------------------
    // Submodels
    // -------------------------------------------------------------------------------------------------


    private Submodel getSubmodel(String id) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(id, MSG_ID_NOT_NULL);
        try (Connection c = acquire()) {
            Long dbId = SubmodelDb.findDbId(c, id);
            if (dbId == null) {
                throw new ResourceNotFoundException("Submodel with id " + id + " not found");
            }
            return SubmodelDb.read(c, dbId);
        }
        catch (SQLException e) {
            throw new PersistenceException("Database error loading " + id, e);
        }
    }


    @Override
    public Submodel getSubmodel(String id, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
        return prepareResult(getSubmodel(id), modifier);
    }


    @Override
    public Page<Submodel> findSubmodels(SubmodelSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws PersistenceException {
        List<SqlCondition> conditions = new ArrayList<>();
        if (criteria != null) {
            if (criteria.getIdShort() != null) {
                conditions.add(new SqlCondition("s.id_short = ?", List.of(criteria.getIdShort())));
            }
            if (criteria.getSemanticId() != null) {
                String aggregate = ReferenceDb.keyAggregate(criteria.getSemanticId());
                if (aggregate != null) {
                    conditions.add(new SqlCondition(
                            "(SELECT string_agg('(' || k.type || ')' || k.value, '|' ORDER BY k.position)"
                                    + " FROM " + DatabaseSchema.TABLE_SUBMODEL_SEMANTIC_ID_REF_KEY + " k WHERE k.reference_id = s.id) = ?",
                            List.of(aggregate)));
                }
                else {
                    conditions.add(new SqlCondition(
                            "NOT EXISTS (SELECT 1 FROM " + DatabaseSchema.TABLE_SUBMODEL_SEMANTIC_ID_REF + " r WHERE r.id = s.id)",
                            List.of()));
                }
            }
        }
        String sql = "SELECT s.id FROM " + DatabaseSchema.TABLE_SUBMODEL + " s WHERE s.id > ?";
        return findIdentifiables(sql, "s.id", conditions, modifier, paging, SubmodelDb::readMany);
    }


    @Override
    public void save(Submodel submodel) throws PersistenceException {
        Ensure.requireNonNull(submodel.getId(), MSG_ID_NOT_NULL);
        try {
            inOwnTransaction(c -> SubmodelDb.save(c, submodel), "Failed to save submodel: " + submodel.getId());
        }
        catch (ResourceNotFoundException e) {
            throw new PersistenceException("Failed to save submodel: " + submodel.getId(), e);
        }
    }


    @Override
    public void deleteSubmodel(String id) throws ResourceNotFoundException, PersistenceException {
        deleteIdentifiable(DatabaseSchema.TABLE_SUBMODEL, "submodel_identifier", id);
    }

    // -------------------------------------------------------------------------------------------------
    // Submodel elements
    // -------------------------------------------------------------------------------------------------


    @Override
    public SubmodelElement getSubmodelElement(SubmodelElementIdentifier identifier, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(identifier, MSG_ID_NOT_NULL);
        List<String> steps = identifier.getIdShortPath().getElements();
        if (steps.isEmpty()) {
            throw new ResourceNotFoundException(identifier.toReference());
        }
        try (Connection c = acquire()) {
            SubmodelElement element = SubmodelElementDb.readSubtree(c, identifier.getSubmodelId(), toDbPath(steps));
            if (element == null) {
                throw new ResourceNotFoundException(identifier.toReference());
            }
            return prepareResult(element, modifier);
        }
        catch (SQLException e) {
            throw new PersistenceException("Database error loading submodel element", e);
        }
    }


    /**
     * Converts an API idShort path (steps like {@code a}, {@code [3]}) into the materialized path format of the
     * database, e.g. {@code a.b[3].c}.
     */
    private static String toDbPath(List<String> steps) {
        StringBuilder result = new StringBuilder();
        for (String step: steps) {
            if (isIndexSegment(step)) {
                result.append(step);
            }
            else {
                if (!result.isEmpty()) {
                    result.append('.');
                }
                result.append(step);
            }
        }
        return result.toString();
    }


    private static boolean isIndexSegment(String step) {
        return step.matches("\\[\\d+\\]");
    }


    @Override
    public Page<SubmodelElement> findSubmodelElements(SubmodelElementSearchCriteria criteria, QueryModifier modifier, PagingInfo paging)
            throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(criteria, MSG_CRITERIA_NOT_NULL);
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(paging, MSG_PAGING_NOT_NULL);

        if (!criteria.isParentSet()) {
            return findSubmodelElementsAcrossSubmodels(criteria, modifier, paging);
        }

        List<SubmodelElement> elements = new ArrayList<>();
        if (criteria.getParent().getSubmodelId() != null) {
            List<String> steps = criteria.getParent().getIdShortPath() != null
                    ? criteria.getParent().getIdShortPath().getElements()
                    : List.of();
            try (Connection c = acquire()) {
                if (steps.isEmpty()) {
                    Long submodelDbId = SubmodelDb.findDbId(c, criteria.getParent().getSubmodelId());
                    if (submodelDbId == null) {
                        throw new ResourceNotFoundException(criteria.getParent().toReference());
                    }
                    if (!criteria.isSemanticIdSet() && !criteria.getValueOnly()) {
                        // no post-fetch filters, so paging can be done by the database: only the
                        // requested root elements (and their subtrees) are read at all
                        return pageRootElements(c, submodelDbId, modifier, paging);
                    }
                    elements.addAll(SubmodelElementDb.readAll(c, submodelDbId));
                }
                else {
                    SubmodelElement parent = SubmodelElementDb.readSubtree(c, criteria.getParent().getSubmodelId(), toDbPath(steps));
                    if (parent == null) {
                        throw new ResourceNotFoundException(criteria.getParent().toReference());
                    }
                    if (parent instanceof SubmodelElementCollection collection) {
                        elements.addAll(collection.getValue());
                    }
                    else if (parent instanceof SubmodelElementList list) {
                        elements.addAll(list.getValue());
                    }
                }
            }
            catch (SQLException e) {
                throw new PersistenceException("Database error searching submodel elements", e);
            }
        }
        Stream<SubmodelElement> result = elements.stream();
        if (criteria.isSemanticIdSet()) {
            result = filterBySemanticId(result, criteria.getSemanticId());
        }
        if (criteria.getValueOnly()) {
            result = result.filter(ElementValueHelper::isValueOnlySupported);
        }
        return preparePagedResult(result, modifier, paging);
    }


    /**
     * Pages the root elements of a submodel directly in SQL: only the ids of the requested page of root elements are
     * selected (ordered by sibling position, cursor = number of roots skipped), then just those subtrees are
     * reconstructed. Avoids materializing the whole submodel per page request.
     */
    private static Page<SubmodelElement> pageRootElements(Connection c, long submodelDbId, QueryModifier modifier, PagingInfo paging) throws SQLException {
        long offset = paging.getCursor() != null ? readCursor(paging.getCursor()) : 0;
        if (!paging.hasLimit() && offset == 0) {
            return Page.<SubmodelElement> builder()
                    .result(QueryModifierHelper.applyQueryModifier(SubmodelElementDb.readAll(c, submodelDbId), modifier))
                    .metadata(PagingMetadata.builder().build())
                    .build();
        }
        List<Long> rootIds = SubmodelElementDb.findRootIds(c, submodelDbId, offset, paging.hasLimit() ? paging.getLimit() + 1 : -1);
        boolean hasMore = paging.hasLimit() && rootIds.size() > paging.getLimit();
        if (hasMore) {
            rootIds = rootIds.subList(0, (int) paging.getLimit());
        }
        List<SubmodelElement> content = SubmodelElementDb.readTrees(c, submodelDbId, rootIds);
        return Page.<SubmodelElement> builder()
                .result(QueryModifierHelper.applyQueryModifier(content, modifier))
                .metadata(PagingMetadata.builder()
                        .cursor(hasMore ? writeCursor(offset + paging.getLimit()) : null)
                        .build())
                .build();
    }


    /**
     * Searches submodel elements across all submodels (at any nesting depth). Candidate submodels are located via the
     * indexed semantic id reference key tables; matching elements are then selected in document order using the same
     * semantics as the in-memory implementation ({@link ReferenceHelper#equals(Reference, Reference)}, including
     * supplemental semantic ids).
     */
    private Page<SubmodelElement> findSubmodelElementsAcrossSubmodels(SubmodelElementSearchCriteria criteria, QueryModifier modifier, PagingInfo paging)
            throws PersistenceException {
        List<SubmodelElement> matches = new ArrayList<>();
        try (Connection c = acquire()) {
            for (long submodelDbId: findCandidateSubmodels(c, criteria)) {
                for (SubmodelElement root: SubmodelElementDb.readAll(c, submodelDbId)) {
                    collectMatchesDocumentOrder(root, criteria, matches);
                }
            }
        }
        catch (SQLException e) {
            throw new PersistenceException("Database error searching submodel elements", e);
        }
        Stream<SubmodelElement> result = matches.stream();
        if (criteria.getValueOnly()) {
            result = result.filter(ElementValueHelper::isValueOnlySupported);
        }
        return preparePagedResult(result, modifier, paging);
    }


    private List<Long> findCandidateSubmodels(Connection c, SubmodelElementSearchCriteria criteria) throws SQLException {
        String sql;
        List<String> parameters = new ArrayList<>();
        if (criteria.isSemanticIdSet() && criteria.getSemanticId() != null) {
            String aggregate = ReferenceDb.keyAggregate(criteria.getSemanticId());
            sql = "SELECT DISTINCT e.submodel_id FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT + " e"
                    + " WHERE (SELECT string_agg('(' || k.type || ')' || k.value, '|' ORDER BY k.position)"
                    + "   FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT_SEMANTIC_ID_REF_KEY + " k WHERE k.reference_id = e.id) = ?"
                    + " OR EXISTS (SELECT 1 FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT_SUPPLEMENTAL_REF + " sr"
                    + "   WHERE sr.submodel_element_id = e.id"
                    + "   AND (SELECT string_agg('(' || k.type || ')' || k.value, '|' ORDER BY k.position)"
                    + "     FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT_SUPPLEMENTAL_REF_KEY + " k WHERE k.reference_id = sr.id) = ?)"
                    + " ORDER BY e.submodel_id";
            parameters.add(ReferenceDb.keyAggregate(criteria.getSemanticId()));
            parameters.add(aggregate);
        }
        else if (criteria.isSemanticIdSet()) {
            sql = "SELECT DISTINCT e.submodel_id FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT + " e"
                    + " WHERE NOT EXISTS (SELECT 1 FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT_SEMANTIC_ID_REF + " r WHERE r.id = e.id)"
                    + " ORDER BY e.submodel_id";
        }
        else {
            sql = "SELECT id FROM " + DatabaseSchema.TABLE_SUBMODEL + " ORDER BY id";
        }
        List<Long> result = new ArrayList<>();
        try (PreparedStatement stmt = c.prepareStatement(sql)) {
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setString(i + 1, parameters.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getLong(1));
                }
            }
        }
        return result;
    }


    private static void collectMatchesDocumentOrder(SubmodelElement element, SubmodelElementSearchCriteria criteria, List<SubmodelElement> matches) {
        if (!criteria.isSemanticIdSet() || matchesSemanticId(element, criteria.getSemanticId())) {
            matches.add(element);
        }
        List<? extends SubmodelElement> children = List.of();
        if (element instanceof SubmodelElementCollection collection) {
            children = collection.getValue();
        }
        else if (element instanceof SubmodelElementList list) {
            children = list.getValue();
        }
        else if (element instanceof Entity entity) {
            children = entity.getStatements();
        }
        else if (element instanceof org.eclipse.digitaltwin.aas4j.v3.model.AnnotatedRelationshipElement annotated) {
            children = annotated.getAnnotations();
        }
        for (SubmodelElement child: children) {
            collectMatchesDocumentOrder(child, criteria, matches);
        }
    }


    private static boolean matchesSemanticId(HasSemantics element, Reference semanticId) {
        if (semanticId == null) {
            return element.getSemanticId() == null;
        }
        return ReferenceHelper.equals(element.getSemanticId(), semanticId)
                || Optional.ofNullable(element.getSupplementalSemanticIds())
                        .orElse(List.of()).stream()
                        .anyMatch(x -> ReferenceHelper.equals(x, semanticId));
    }


    private static <T extends HasSemantics> Stream<T> filterBySemanticId(Stream<T> stream, Reference semanticId) {
        if (Objects.isNull(semanticId)) {
            return stream;
        }
        return stream.filter(x -> matchesSemanticId(x, semanticId));
    }


    @Override
    public void insert(SubmodelElementIdentifier parentIdentifier, SubmodelElement submodelElement)
            throws ResourceNotFoundException, ResourceNotAContainerElementException, ResourceAlreadyExistsException, PersistenceException {
        Ensure.requireNonNull(parentIdentifier, MSG_ID_NOT_NULL);
        Ensure.requireNonNull(submodelElement, MSG_ELEMENT_NOT_NULL);
        List<String> parentSteps = parentIdentifier.getIdShortPath().getElements();
        inOwnTransaction(c -> {
            Long submodelDbId = SubmodelDb.findDbId(c, parentIdentifier.getSubmodelId());
            if (submodelDbId == null) {
                throw new ResourceNotFoundException(parentIdentifier.toReference());
            }
            Long parentId = null;
            Long rootId = null;
            boolean parentIsList = false;
            String parentPath = "";
            int depth = 0;
            if (!parentSteps.isEmpty()) {
                SubmodelElementDb.ElementInfo parent = SubmodelElementDb.findElement(c, submodelDbId, toDbPath(parentSteps));
                if (parent == null) {
                    throw new ResourceNotFoundException(parentIdentifier.toReference());
                }
                if (parent.modelType() != EnumCodes.MODEL_TYPE_SUBMODEL_ELEMENT_COLLECTION
                        && parent.modelType() != EnumCodes.MODEL_TYPE_SUBMODEL_ELEMENT_LIST) {
                    throw new IllegalArgumentException(String.format(ILLEGAL_TYPE,
                            "model type code " + parent.modelType(),
                            Submodel.class,
                            SubmodelElementCollection.class,
                            SubmodelElementList.class));
                }
                parentId = parent.id();
                rootId = parent.rootId() != null ? parent.rootId() : parent.id();
                parentIsList = parent.modelType() == EnumCodes.MODEL_TYPE_SUBMODEL_ELEMENT_LIST;
                parentPath = parent.idShortPath();
                depth = parent.depth() + 1;
            }
            if (!parentIsList && StringHelper.isBlank(submodelElement.getIdShort())) {
                throw new IllegalArgumentException("idShort most be non-empty");
            }
            // an existing child with the same idShort is replaced in place, otherwise the element is appended
            SubmodelElementDb.ElementInfo existing = StringHelper.isBlank(submodelElement.getIdShort())
                    ? null
                    : findChildByIdShort(c, submodelDbId, parentId, submodelElement.getIdShort());
            int position;
            if (existing != null) {
                SubmodelElementDb.deleteElement(c, existing.id());
                position = existing.position();
            }
            else {
                position = nextPosition(c, submodelDbId, parentId);
            }
            SubmodelElementDb.insertTree(c, submodelDbId, parentId, rootId, parentIsList, parentPath, depth, position, submodelElement);
            return null;
        }, "Database error inserting submodel element");
    }


    private static SubmodelElementDb.ElementInfo findChildByIdShort(Connection c, long submodelDbId, Long parentId, String idShort) throws SQLException {
        try (PreparedStatement stmt = c.prepareStatement(
                "SELECT id, parent_sme_id, root_sme_id, position, depth, idshort_path, model_type FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT
                        + " WHERE submodel_id = ? AND parent_sme_id IS NOT DISTINCT FROM ? AND lower(id_short) = lower(?)")) {
            stmt.setLong(1, submodelDbId);
            stmt.setObject(2, parentId, Types.BIGINT);
            stmt.setString(3, idShort);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new SubmodelElementDb.ElementInfo(
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


    private static int nextPosition(Connection c, long submodelDbId, Long parentId) throws SQLException {
        try (PreparedStatement stmt = c.prepareStatement(
                "SELECT COALESCE(MAX(position) + 1, 0) FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT
                        + " WHERE submodel_id = ? AND parent_sme_id IS NOT DISTINCT FROM ?")) {
            stmt.setLong(1, submodelDbId);
            stmt.setObject(2, parentId, Types.BIGINT);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }


    @Override
    public void update(SubmodelElementIdentifier identifier, SubmodelElement submodelElement) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(identifier, MSG_ID_NOT_NULL);
        Ensure.requireNonNull(submodelElement, MSG_ELEMENT_NOT_NULL);
        List<String> steps = identifier.getIdShortPath().getElements();
        if (steps.isEmpty()) {
            throw new ResourceNotFoundException(identifier.toReference());
        }
        inOwnTransaction(c -> {
            Long submodelDbId = SubmodelDb.findDbId(c, identifier.getSubmodelId());
            if (submodelDbId == null) {
                throw new ResourceNotFoundException(identifier.toReference());
            }
            SubmodelElementDb.ElementInfo existing = SubmodelElementDb.findElement(c, submodelDbId, toDbPath(steps));
            if (existing == null) {
                throw new ResourceNotFoundException(identifier.toReference());
            }
            ParentContext parent = parentContext(c, existing);
            SubmodelElementDb.deleteElement(c, existing.id());
            SubmodelElementDb.insertTree(c, submodelDbId, existing.parentId(), existing.rootId(),
                    parent.isList(), parent.path(), existing.depth(), existing.position(), submodelElement);
            return null;
        }, "Database error updating submodel element");
    }


    @Override
    public void deleteSubmodelElement(SubmodelElementIdentifier identifier) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(identifier, MSG_ID_NOT_NULL);
        List<String> steps = identifier.getIdShortPath().getElements();
        if (steps.isEmpty()) {
            throw new ResourceNotFoundException(identifier.toReference());
        }
        inOwnTransaction(c -> {
            Long submodelDbId = SubmodelDb.findDbId(c, identifier.getSubmodelId());
            if (submodelDbId == null) {
                throw new ResourceNotFoundException(identifier.toReference());
            }
            SubmodelElementDb.ElementInfo existing = SubmodelElementDb.findElement(c, submodelDbId, toDbPath(steps));
            if (existing == null) {
                throw new ResourceNotFoundException(identifier.toReference());
            }
            ParentContext parent = parentContext(c, existing);
            SubmodelElementDb.deleteElement(c, existing.id());
            if (parent.isList()) {
                shiftListSiblingsAfterDelete(c, submodelDbId, existing, parent.path());
            }
            return null;
        }, "Database error deleting submodel element");
    }

    /**
     * Position/path context of the parent container of an element (the submodel itself for root elements).
     */
    private record ParentContext(boolean isList, String path) {}

    private static ParentContext parentContext(Connection c, SubmodelElementDb.ElementInfo element) throws SQLException {
        if (element.parentId() == null) {
            return new ParentContext(false, "");
        }
        try (PreparedStatement stmt = c.prepareStatement(
                "SELECT model_type, idshort_path FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT + " WHERE id = ?")) {
            stmt.setLong(1, element.parentId());
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return new ParentContext(rs.getInt("model_type") == EnumCodes.MODEL_TYPE_SUBMODEL_ELEMENT_LIST, rs.getString("idshort_path"));
            }
        }
    }


    /**
     * After deleting an element from a SubmodelElementList, moves the following siblings one position down and
     * rewrites their materialized paths (and those of their descendants), so list indices stay consecutive. Siblings
     * are processed in ascending order, each moving into the slot just vacated.
     */
    private static void shiftListSiblingsAfterDelete(Connection c, long submodelDbId, SubmodelElementDb.ElementInfo deleted, String parentPath) throws SQLException {
        record Sibling(long id, int position, String path) {}
        List<Sibling> siblings = new ArrayList<>();
        try (PreparedStatement stmt = c.prepareStatement(
                "SELECT id, position, idshort_path FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT
                        + " WHERE submodel_id = ? AND parent_sme_id = ? AND position > ? ORDER BY position")) {
            stmt.setLong(1, submodelDbId);
            stmt.setLong(2, deleted.parentId());
            stmt.setInt(3, deleted.position());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    siblings.add(new Sibling(rs.getLong(1), rs.getInt(2), rs.getString(3)));
                }
            }
        }
        for (Sibling sibling: siblings) {
            int newPosition = sibling.position() - 1;
            String newPath = parentPath + "[" + newPosition + "]";
            try (PreparedStatement stmt = c.prepareStatement(
                    "UPDATE " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT + " SET position = ? WHERE id = ?")) {
                stmt.setInt(1, newPosition);
                stmt.setLong(2, sibling.id());
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = c.prepareStatement(
                    "UPDATE " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT
                            + " SET idshort_path = ? || SUBSTRING(idshort_path FROM ?)"
                            + " WHERE submodel_id = ? AND (idshort_path = ? OR idshort_path LIKE ? ESCAPE '\\' OR idshort_path LIKE ? ESCAPE '\\')")) {
                stmt.setString(1, newPath);
                stmt.setInt(2, sibling.path().length() + 1);
                stmt.setLong(3, submodelDbId);
                stmt.setString(4, sibling.path());
                stmt.setString(5, SubmodelElementDb.escapeLike(sibling.path()) + ".%");
                stmt.setString(6, SubmodelElementDb.escapeLike(sibling.path()) + "[%");
                stmt.executeUpdate();
            }
        }
    }

    // -------------------------------------------------------------------------------------------------
    // Concept descriptions
    // -------------------------------------------------------------------------------------------------


    private ConceptDescription getConceptDescription(String id) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(id, MSG_ID_NOT_NULL);
        try (Connection c = acquire();
                PreparedStatement stmt = c.prepareStatement(
                        "SELECT data FROM " + DatabaseSchema.TABLE_CONCEPT_DESCRIPTION + " WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new ResourceNotFoundException("ConceptDescription with id " + id + " not found");
                }
                return DbJson.read(rs.getString(1), ConceptDescription.class);
            }
        }
        catch (SQLException e) {
            throw new PersistenceException("Database error loading " + id, e);
        }
    }


    @Override
    public ConceptDescription getConceptDescription(String id, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
        return prepareResult(getConceptDescription(id), modifier);
    }


    @Override
    public Page<ConceptDescription> findConceptDescriptions(ConceptDescriptionSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws PersistenceException {
        List<SqlCondition> conditions = new ArrayList<>();
        if (criteria != null && criteria.getIdShort() != null) {
            conditions.add(new SqlCondition("cd.id_short = ?", List.of(criteria.getIdShort())));
        }
        if (criteria != null && (criteria.getIsCaseOf() != null || criteria.getDataSpecification() != null)) {
            // reference comparisons that cannot be expressed as indexed SQL equality - filter in
            // memory, but still apply the SQL-able conditions in the query
            Stream<ConceptDescription> stream = loadAllConceptDescriptions(conditions).stream();
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
        String sql = "SELECT cd.seq FROM " + DatabaseSchema.TABLE_CONCEPT_DESCRIPTION + " cd WHERE cd.seq > ?";
        return findIdentifiables(sql, "cd.seq", conditions, modifier, paging, (c, ids) -> readEach(c, ids, PersistencePostgres::readConceptDescriptionBySeq));
    }


    private static ConceptDescription readConceptDescriptionBySeq(Connection c, long seq) throws SQLException {
        try (PreparedStatement stmt = c.prepareStatement(
                "SELECT data FROM " + DatabaseSchema.TABLE_CONCEPT_DESCRIPTION + " WHERE seq = ?")) {
            stmt.setLong(1, seq);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? DbJson.read(rs.getString(1), ConceptDescription.class) : null;
            }
        }
    }


    private List<ConceptDescription> loadAllConceptDescriptions(List<SqlCondition> conditions) throws PersistenceException {
        StringBuilder sql = new StringBuilder("SELECT data FROM " + DatabaseSchema.TABLE_CONCEPT_DESCRIPTION + " cd");
        for (int i = 0; i < conditions.size(); i++) {
            sql.append(i == 0 ? " WHERE " : " AND ").append(conditions.get(i).clause());
        }
        sql.append(" ORDER BY cd.seq ASC");
        List<ConceptDescription> result = new ArrayList<>();
        try (Connection c = acquire(); PreparedStatement stmt = c.prepareStatement(sql.toString())) {
            int parameterIndex = 1;
            for (SqlCondition condition: conditions) {
                for (String parameter: condition.parameters()) {
                    stmt.setString(parameterIndex++, parameter);
                }
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(DbJson.read(rs.getString(1), ConceptDescription.class));
                }
            }
        }
        catch (SQLException e) {
            throw new PersistenceException("Database error loading concept descriptions", e);
        }
        return result;
    }


    @Override
    public void save(ConceptDescription conceptDescription) throws PersistenceException {
        Ensure.requireNonNull(conceptDescription.getId(), MSG_ID_NOT_NULL);
        try (Connection c = acquire();
                PreparedStatement stmt = c.prepareStatement(
                        "INSERT INTO " + DatabaseSchema.TABLE_CONCEPT_DESCRIPTION + " (id, id_short, data) VALUES (?, ?, ?)"
                                + " ON CONFLICT (id) DO UPDATE SET id_short = EXCLUDED.id_short, data = EXCLUDED.data")) {
            stmt.setString(1, conceptDescription.getId());
            stmt.setString(2, conceptDescription.getIdShort());
            stmt.setObject(3, DbJson.write(conceptDescription), Types.OTHER);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new PersistenceException("Failed to save ConceptDescription: " + conceptDescription.getId(), e);
        }
    }


    @Override
    public void deleteConceptDescription(String id) throws ResourceNotFoundException, PersistenceException {
        deleteIdentifiable(DatabaseSchema.TABLE_CONCEPT_DESCRIPTION, "id", id);
    }

    // -------------------------------------------------------------------------------------------------
    // Operation results
    // -------------------------------------------------------------------------------------------------


    @Override
    public OperationResult getOperationResult(OperationHandle handle) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(handle, MSG_ID_NOT_NULL);
        try (Connection c = acquire();
                PreparedStatement stmt = c.prepareStatement(
                        "SELECT content FROM " + DatabaseSchema.TABLE_OPERATION_RESULT + " WHERE id = ?")) {
            stmt.setString(1, handle.getHandleId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new ResourceNotFoundException("OperationResult with handle " + handle.getHandleId() + " not found");
                }
                return DbJson.read(rs.getString(1), OperationResult.class);
            }
        }
        catch (SQLException e) {
            throw new PersistenceException("Database error loading operation result", e);
        }
    }


    @Override
    public void save(OperationHandle handle, OperationResult result) throws PersistenceException {
        Ensure.requireNonNull(handle, MSG_ID_NOT_NULL);
        try (Connection c = acquire();
                PreparedStatement stmt = c.prepareStatement(
                        "INSERT INTO " + DatabaseSchema.TABLE_OPERATION_RESULT + " (id, content) VALUES (?, ?)"
                                + " ON CONFLICT (id) DO UPDATE SET content = EXCLUDED.content")) {
            stmt.setString(1, handle.getHandleId());
            stmt.setObject(2, DbJson.write(result), Types.OTHER);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new PersistenceException("Failed to save operation result", e);
        }
    }

    // -------------------------------------------------------------------------------------------------
    // misc
    // -------------------------------------------------------------------------------------------------


    private static void save(Persistence<?> target, Environment environment) throws PersistenceException {
        if (environment == null) {
            return;
        }
        if (environment.getAssetAdministrationShells() != null) {
            for (AssetAdministrationShell shell: environment.getAssetAdministrationShells()) {
                target.save(shell);
            }
        }
        if (environment.getSubmodels() != null) {
            for (Submodel submodel: environment.getSubmodels()) {
                target.save(submodel);
            }
        }
        if (environment.getConceptDescriptions() != null) {
            for (ConceptDescription conceptDescription: environment.getConceptDescriptions()) {
                target.save(conceptDescription);
            }
        }
    }


    @Override
    public void deleteAll() throws PersistenceException {
        try (Connection c = acquire()) {
            DatabaseSchema.clearData(c);
        }
        catch (SQLException e) {
            throw new PersistenceException("Failed to clear database", e);
        }
    }

    // -------------------------------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------------------------------

    @FunctionalInterface
    private interface ConnectionAction<T> {
        T run(Connection connection) throws Exception;
    }

    @FunctionalInterface
    private interface IdentifiableReader<T> {
        T read(Connection connection, long dbId) throws SQLException;
    }

    @FunctionalInterface
    private interface IdentifiableBatchReader<T> {
        List<T> read(Connection connection, List<Long> dbIds) throws SQLException;
    }

    private static <T> List<T> readEach(Connection connection, List<Long> dbIds, IdentifiableReader<T> reader) throws SQLException {
        List<T> result = new ArrayList<>();
        for (Long dbId: dbIds) {
            T entity = reader.read(connection, dbId);
            if (entity != null) {
                result.add(entity);
            }
        }
        return result;
    }


    private <T> T inOwnTransaction(ConnectionAction<T> action, String errorMessage)
            throws PersistenceException, ResourceNotFoundException {
        if (bound != null) {
            try {
                return action.run(acquire());
            }
            catch (ResourceNotFoundException | IllegalArgumentException | IllegalStateException e) {
                throw e;
            }
            catch (Exception e) {
                throw new PersistenceException(errorMessage, e);
            }
        }
        try {
            return runTransaction(action, errorMessage);
        }
        catch (Exception e) {
            throw new PersistenceException(errorMessage, e);
        }
    }


    @Override
    public boolean supportsTransactions() {
        return true;
    }


    @Override
    public <R> R inTransaction(TransactionalAction<R> action) throws Exception {
        Ensure.requireNonNull(action, "action must be non-null");
        if (bound != null) {
            // already inside a transaction --> join it rather than opening a second one
            return action.execute(this);
        }
        return runTransaction(c -> {
            PersistencePostgres view = boundTo(c);
            try {
                return action.execute(view);
            }
            finally {
                view.valid = false;
            }
        }, "transaction failed");
    }


    /**
     * Opens a transaction on a pooled connection, commits on success and rolls back on any failure.
     *
     */
    private <T> T runTransaction(ConnectionAction<T> action, String errorMessage) throws Exception {
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try {
                T result = action.run(c);
                c.commit();
                return result;
            }
            catch (Exception e) {
                try {
                    c.rollback();
                }
                catch (SQLException rollbackFailure) {
                    e.addSuppressed(rollbackFailure);
                }
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                throw e;
            }
            finally {
                try {
                    c.setAutoCommit(true);
                }
                catch (SQLException resetFailure) {
                    LOGGER.debug("failed to reset autoCommit before returning connection to pool", resetFailure);
                }
            }
        }
        catch (SQLException e) {
            throw new PersistenceException(errorMessage, e);
        }
    }


    private void deleteIdentifiable(String table, String idColumn, String id) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(id, MSG_ID_NOT_NULL);
        try (Connection c = acquire();
                PreparedStatement stmt = c.prepareStatement("DELETE FROM " + table + " WHERE " + idColumn + " = ?")) {
            stmt.setString(1, id);
            if (stmt.executeUpdate() == 0) {
                throw new ResourceNotFoundException(String.format("resource not found (id %s)", id));
            }
        }
        catch (SQLException e) {
            throw new PersistenceException("Database error deleting " + id, e);
        }
    }


    /**
     * Queries one page of identifiables with filtering, ordering and pagination done by the database. Pagination uses
     * the cursor as keyset on the serial id/seq column ({@code > cursor}) instead of an offset, so pages are stable
     * under concurrent inserts and deletes. One row more than the limit is fetched to determine whether more data is
     * available.
     */
    private <T extends Referable> Page<T> findIdentifiables(String baseSql, String orderColumn, List<SqlCondition> conditions,
                                                            QueryModifier modifier, PagingInfo paging, IdentifiableBatchReader<T> reader)
            throws PersistenceException {
        long previousSeq = paging.getCursor() != null ? readCursor(paging.getCursor()) : 0;
        StringBuilder sql = new StringBuilder(baseSql);
        for (SqlCondition condition: conditions) {
            sql.append(" AND ").append(condition.clause());
        }
        sql.append(" ORDER BY ").append(orderColumn).append(" ASC");
        if (paging.hasLimit()) {
            sql.append(" LIMIT ?");
        }
        List<T> content;
        long lastSeq = previousSeq;
        boolean hasMore = false;
        try (Connection c = acquire()) {
            List<Long> ids = new ArrayList<>();
            try (PreparedStatement stmt = c.prepareStatement(sql.toString())) {
                int parameterIndex = 1;
                stmt.setLong(parameterIndex++, previousSeq);
                for (SqlCondition condition: conditions) {
                    for (String parameter: condition.parameters()) {
                        stmt.setString(parameterIndex++, parameter);
                    }
                }
                if (paging.hasLimit()) {
                    stmt.setLong(parameterIndex, paging.getLimit() + 1);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ids.add(rs.getLong(1));
                    }
                }
            }
            hasMore = paging.hasLimit() && ids.size() > paging.getLimit();
            if (hasMore) {
                ids = ids.subList(0, (int) paging.getLimit());
            }
            content = ids.isEmpty() ? List.of() : reader.read(c, ids);
            if (!ids.isEmpty()) {
                lastSeq = ids.get(ids.size() - 1);
            }
        }
        catch (SQLException e) {
            throw new PersistenceException("Database error querying page", e);
        }
        return Page.<T> builder()
                .result(QueryModifierHelper.applyQueryModifier(content, modifier))
                .metadata(PagingMetadata.builder()
                        .cursor(hasMore ? writeCursor(lastSeq) : null)
                        .build())
                .build();
    }


    private static <T extends Referable> Page<T> preparePagedResult(Stream<T> input, QueryModifier modifier, PagingInfo paging) {
        // no defensive deep copy here: unlike the in-memory persistence, all elements are freshly
        // built from the database, so the modifier cannot corrupt shared state
        Page<T> result = preparePagedResult(input, paging);
        result.setContent(QueryModifierHelper.applyQueryModifier(result.getContent(), modifier));
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
        return QueryModifierHelper.applyQueryModifier(result, modifier);
    }

    /**
     * A SQL filter fragment with positional parameters, e.g. {@code id_short = ?}. All parameters are bound as
     * strings.
     */
    private record SqlCondition(String clause, List<String> parameters) {}
}
