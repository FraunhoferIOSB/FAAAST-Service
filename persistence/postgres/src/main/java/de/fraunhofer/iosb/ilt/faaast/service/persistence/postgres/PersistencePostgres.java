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
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.DeserializerWrapper;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AssetAdministrationShellSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.ConceptDescriptionSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelElementSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.util.QueryModifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.CollectionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.HasSemantics;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Persistence implementation for Postgres DB.
 */
public class PersistencePostgres implements Persistence<PersistencePostgresConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistencePostgres.class);

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

            DatabaseSchema.createTables(dataSource.getConnection());

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
        long offset = 0;
        if (paging.getCursor() != null) {
            offset = readCursor(paging.getCursor());
        }

        int totalCount = countEntities(DatabaseSchema.TABLE_AAS);
        int limit = paging.hasLimit() ? (int) paging.getLimit() : totalCount;

        List<AssetAdministrationShell> all;
        if (criteria != null && (criteria.getIdShort() != null || (criteria.getAssetIds() != null && !criteria.getAssetIds().isEmpty()))) {
            all = loadAllEntities(DatabaseSchema.TABLE_AAS, AssetAdministrationShell.class);
        }
        else {
            all = loadAllEntitiesPaginated(DatabaseSchema.TABLE_AAS, AssetAdministrationShell.class, offset, limit + 1);
        }

        Stream<AssetAdministrationShell> stream = all.stream();

        if (criteria != null) {
            if (criteria.getIdShort() != null) {
                stream = stream.filter(x -> Objects.equals(x.getIdShort(), criteria.getIdShort()));
            }
            if (criteria.getAssetIds() != null && !criteria.getAssetIds().isEmpty()) {
                stream = stream.filter(x -> x.getAssetInformation() != null &&
                        criteria.getAssetIds().stream().anyMatch(
                                c -> Objects.equals(x.getAssetInformation().getGlobalAssetId(), c.getValue())));
            }
        }
        return preparePagedResult(stream, paging);
    }


    @Override
    public void save(AssetAdministrationShell shell) {
        try {
            saveEntity(DatabaseSchema.TABLE_AAS, shell.getId(), shell);
        }
        catch (PersistenceException e) {
            LOGGER.error("Could not save aas with id {}", shell.getId());
        }
    }


    @Override
    public void deleteAssetAdministrationShell(String id) {
        try {
            deleteEntity(DatabaseSchema.TABLE_AAS, id);
        }
        catch (PersistenceException e) {
            LOGGER.error("Could not delete AAS with id {}", id);
        }
    }


    private Submodel getSubmodel(String id) throws ResourceNotFoundException, PersistenceException {
        return loadEntity(DatabaseSchema.TABLE_SUBMODEL, id, Submodel.class);
    }


    @Override
    public Submodel getSubmodel(String id, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
        return prepareResult(
                getSubmodel(id), modifier);
    }


    @Override
    public Page<Submodel> findSubmodels(SubmodelSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws PersistenceException {
        long offset = 0;
        if (paging.getCursor() != null) {
            offset = readCursor(paging.getCursor());
        }

        int totalCount = countEntities(DatabaseSchema.TABLE_SUBMODEL);
        int limit = paging.hasLimit() ? (int) paging.getLimit() : totalCount;

        List<Submodel> all;
        if (criteria != null && (criteria.getIdShort() != null || criteria.getSemanticId() != null)) {
            all = loadAllEntities(DatabaseSchema.TABLE_SUBMODEL, Submodel.class);
        }
        else {
            all = loadAllEntitiesPaginated(DatabaseSchema.TABLE_SUBMODEL, Submodel.class, offset, limit + 1);
        }

        Stream<Submodel> stream = all.stream();

        if (criteria != null) {
            if (criteria.getIdShort() != null) {
                stream = stream.filter(x -> Objects.equals(x.getIdShort(), criteria.getIdShort()));
            }
            if (criteria.getSemanticId() != null) {
                stream = stream.filter(x -> ReferenceHelper.equals(x.getSemanticId(), criteria.getSemanticId()));
            }
        }
        return preparePagedResult(stream, modifier, paging);
    }


    @Override
    public void save(Submodel submodel) {
        try {
            saveEntity(DatabaseSchema.TABLE_SUBMODEL, submodel.getId(), submodel);
        }
        catch (PersistenceException e) {
            LOGGER.error("Could not save submodel with id {}", submodel.getId());
        }
    }


    @Override
    public void deleteSubmodel(String id) {
        try {
            deleteEntity(DatabaseSchema.TABLE_SUBMODEL, id);
        }
        catch (PersistenceException e) {
            LOGGER.error("Could not delete submodel with id {}", id);
        }
    }


    @Override
    public SubmodelElement getSubmodelElement(SubmodelElementIdentifier identifier, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(identifier, MSG_ID_NOT_NULL);
        return prepareResult(
                EnvironmentHelper.resolve(identifier.toReference(), getSubmodel(identifier.getSubmodelId()), SubmodelElement.class),
                modifier);
    }


    @Override
    public Page<SubmodelElement> findSubmodelElements(SubmodelElementSearchCriteria criteria, QueryModifier modifier,
                                                      PagingInfo paging)
            throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(criteria, MSG_CRITERIA_NOT_NULL);
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(paging, MSG_PAGING_NOT_NULL);

        List<SubmodelElement> elements = new ArrayList<>();

        if (criteria.isParentSet()) {
            if (criteria.getParent().getSubmodelId() != null) {
                Submodel submodel = getSubmodel(criteria.getParent().getSubmodelId());
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
        }
        else {
            elements = getAllSubmodelElementsOptimized(paging);
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


    private List<SubmodelElement> getAllSubmodelElementsOptimized(PagingInfo paging) throws PersistenceException {
        List<SubmodelElement> elements = new ArrayList<>();

        if (paging.hasLimit()) {
            long offset = 0;
            if (paging.getCursor() != null) {
                offset = readCursor(paging.getCursor());
            }

            int limit = (int) paging.getLimit();
            int fetchSize = Math.min(limit * 2, 100);

            long currentOffset = offset;
            int totalLoaded = 0;

            while (totalLoaded < limit) {
                List<Submodel> submodels = loadAllEntitiesPaginated(DatabaseSchema.TABLE_SUBMODEL, Submodel.class, currentOffset, fetchSize);

                if (submodels.isEmpty()) {
                    break;
                }

                for (Submodel submodel: submodels) {
                    if (totalLoaded >= limit) {
                        break;
                    }
                    List<SubmodelElement> subElements = submodel.getSubmodelElements();
                    int elementsToAdd = Math.min(limit - totalLoaded, subElements.size());
                    elements.addAll(subElements.subList(0, elementsToAdd));
                    totalLoaded += elementsToAdd;
                }

                currentOffset += fetchSize;
                if (submodels.size() < fetchSize) {
                    break;
                }
            }
        }
        else {
            List<Submodel> submodels = loadAllEntities(DatabaseSchema.TABLE_SUBMODEL, Submodel.class);
            for (Submodel submodel: submodels) {
                elements.addAll(submodel.getSubmodelElements());
            }
        }

        return elements;
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
        Submodel submodel = getSubmodel(identifier.getSubmodelId());
        SubmodelElement oldElement = EnvironmentHelper.resolve(identifier.toReference(), submodel, SubmodelElement.class);
        Referable parent = EnvironmentHelper.resolve(ReferenceHelper.getParent(identifier.toReference()), submodel, Referable.class);

        if (SubmodelElementList.class.isAssignableFrom(parent.getClass())) {
            int index = Integer.parseInt(identifier.getIdShortPath().getElements().get(identifier.getIdShortPath().getElements().size() - 1).substring(1, 2));
            ((SubmodelElementList) parent).getValue().set(index, submodelElement);
            save(submodel);
            return;
        }

        Collection<SubmodelElement> container;
        if (Submodel.class.isAssignableFrom(parent.getClass())) {
            container = ((Submodel) parent).getSubmodelElements();
        }
        else if (SubmodelElementCollection.class.isAssignableFrom(parent.getClass())) {
            container = ((SubmodelElementCollection) parent).getValue();
        }
        else {
            throw new IllegalArgumentException(String.format(ILLEGAL_TYPE,
                    parent.getClass(),
                    Submodel.class,
                    SubmodelElementCollection.class,
                    SubmodelElementList.class));
        }
        CollectionHelper.put(container,
                container.stream()
                        .filter(x -> Objects.equals(x, oldElement))
                        .findFirst()
                        .orElse(null),
                submodelElement);
        save(submodel);
    }


    @Override
    public void deleteSubmodelElement(SubmodelElementIdentifier identifier) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(identifier, MSG_ID_NOT_NULL);
        Submodel submodel = getSubmodel(identifier.getSubmodelId());
        SubmodelElement element = EnvironmentHelper.resolve(identifier.toReference(), submodel, SubmodelElement.class);
        Referable parent = EnvironmentHelper.resolve(ReferenceHelper.getParent(identifier.toReference()), submodel, Referable.class);

        if (SubmodelElementList.class.isAssignableFrom(parent.getClass())) {
            ((SubmodelElementList) parent).getValue().remove(element);
        }
        else if (SubmodelElementCollection.class.isAssignableFrom(parent.getClass())) {
            ((SubmodelElementCollection) parent).getValue().remove(element);
        }
        else if (Submodel.class.isAssignableFrom(parent.getClass())) {
            ((Submodel) parent).getSubmodelElements().remove(element);
        }
        else {
            throw new IllegalArgumentException(String.format(ILLEGAL_TYPE,
                    parent.getClass(),
                    Submodel.class,
                    SubmodelElementCollection.class,
                    SubmodelElementList.class));
        }
        save(submodel);
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
        long offset = 0;
        if (paging.getCursor() != null) {
            offset = readCursor(paging.getCursor());
        }

        int totalCount = countEntities(DatabaseSchema.TABLE_CONCEPT_DESCRIPTION);
        int limit = paging.hasLimit() ? (int) paging.getLimit() : totalCount;

        List<ConceptDescription> all;
        if (criteria != null && (criteria.getIdShort() != null || criteria.getIsCaseOf() != null || criteria.getDataSpecification() != null)) {
            all = loadAllEntities(DatabaseSchema.TABLE_CONCEPT_DESCRIPTION, ConceptDescription.class);
        }
        else {
            all = loadAllEntitiesPaginated(DatabaseSchema.TABLE_CONCEPT_DESCRIPTION, ConceptDescription.class, offset, limit + 1);
        }

        Stream<ConceptDescription> stream = all.stream();

        if (criteria != null) {
            if (criteria.getIdShort() != null) {
                stream = stream.filter(x -> Objects.equals(x.getIdShort(), criteria.getIdShort()));
            }
            if (criteria.getIsCaseOf() != null) {
                stream = stream.filter(x -> x.getIsCaseOf() != null && x.getIsCaseOf().contains(criteria.getIsCaseOf()));
            }
            if (criteria.getDataSpecification() != null) {
                stream = stream.filter(x -> x.getEmbeddedDataSpecifications() != null &&
                        x.getEmbeddedDataSpecifications().stream().anyMatch(
                                d -> Objects.equals(d.getDataSpecification(), criteria.getDataSpecification())));
            }
        }
        return preparePagedResult(stream, modifier, paging);
    }


    @Override
    public void save(ConceptDescription conceptDescription) {
        try {
            saveEntity(DatabaseSchema.TABLE_CONCEPT_DESCRIPTION, conceptDescription.getId(), conceptDescription);
        }
        catch (PersistenceException e) {
            LOGGER.error("Could not save ConceptDescription with id {}", conceptDescription.getId());
        }
    }


    @Override
    public void deleteConceptDescription(String id) {
        try {
            deleteEntity(DatabaseSchema.TABLE_CONCEPT_DESCRIPTION, id);
        }
        catch (PersistenceException e) {
            LOGGER.error("Could not delete ConceptDescription with id {}", id);
        }
    }


    @Override
    public OperationResult getOperationResult(OperationHandle handle) throws ResourceNotFoundException, PersistenceException {
        return loadEntity(DatabaseSchema.TABLE_OPERATION_RESULT, handle.getHandleId(), OperationResult.class);
    }


    @Override
    public void save(OperationHandle handle, OperationResult result) throws PersistenceException {
        saveEntity(DatabaseSchema.TABLE_OPERATION_RESULT, handle.getHandleId(), result);
    }


    private void save(Environment environment) {
        if (environment == null)
            return;
        if (environment.getAssetAdministrationShells() != null)
            environment.getAssetAdministrationShells().forEach(this::save);
        if (environment.getSubmodels() != null)
            environment.getSubmodels().forEach(this::save);
        if (environment.getConceptDescriptions() != null)
            environment.getConceptDescriptions().forEach(this::save);
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


    private <T> void saveEntity(String table, String id, T entity) throws PersistenceException {
        Ensure.requireNonNull(id, MSG_ID_NOT_NULL);
        try {
            String json = jsonSerializer.write(entity);
            String sql = "INSERT INTO " + table + " (id, content) VALUES (?, ?) " +
                    "ON CONFLICT (id) DO UPDATE SET content = EXCLUDED.content";

            try (Connection c = dataSource.getConnection(); PreparedStatement preparedStatement = c.prepareStatement(sql)) {
                preparedStatement.setString(1, id);
                preparedStatement.setObject(2, json, Types.OTHER);
                preparedStatement.executeUpdate();
            }
        }
        catch (Exception e) {
            throw new PersistenceException("Failed to save to " + table + ": " + id, e);
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


    private <T> List<T> loadAllEntities(String table, Class<T> clazz) throws PersistenceException {
        List<T> results = new ArrayList<>();
        String sql = SELECT_CONTENT + table + " ORDER BY seq ASC";
        try (Connection c = dataSource.getConnection(); PreparedStatement pstmt = c.prepareStatement(sql)) {
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


    private <T> List<T> loadAllEntitiesPaginated(String table, Class<T> clazz, long offset, int limit) throws PersistenceException {
        List<T> results = new ArrayList<>();
        String sql = SELECT_CONTENT + table + " ORDER BY seq ASC LIMIT ? OFFSET ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            pstmt.setLong(2, offset);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(jsonDeserializer.read(rs.getString(CONTENT), clazz));
                }
            }
        }
        catch (Exception e) {
            throw new PersistenceException("Database error loading paginated from " + table, e);
        }
        return results;
    }


    private int countEntities(String table) throws PersistenceException {
        String sql = "SELECT COUNT(*) FROM " + table;
        try (Connection c = dataSource.getConnection(); PreparedStatement pstmt = c.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        catch (Exception e) {
            throw new PersistenceException("Database error counting from " + table, e);
        }
        return 0;
    }


    private void deleteEntity(String table, String id) throws PersistenceException {
        String sql = "DELETE FROM " + table + " WHERE id = ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
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
}
