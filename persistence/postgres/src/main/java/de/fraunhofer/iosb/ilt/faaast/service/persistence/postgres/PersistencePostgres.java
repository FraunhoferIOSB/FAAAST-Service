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
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Query;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AssetAdministrationShellSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.ConceptDescriptionSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelElementSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.util.QueryModifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


public class PersistencePostgres implements Persistence<PersistencePostgresConfig> {

    private PersistencePostgresConfig config;
    private HikariDataSource dataSource;

    private final JsonSerializer jsonSerializer = new JsonSerializer();
    private final JsonDeserializer jsonDeserializer = new JsonDeserializer();

    private static final String TABLE_AAS = "aas";
    private static final String TABLE_SUBMODEL = "submodels";
    private static final String TABLE_CONCEPT = "concept_descriptions";
    private static final String TABLE_OP_RESULT = "operation_results";
    private static final String PATH_SPLIT_REGEX = "[.\\[\\]]+";

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

            initDb();

            if (config.getInitialModel() != null) {
                if (Boolean.TRUE.equals(config.getOverride())) {
                    deleteAll();
                    save(config.getInitialModel());
                }
                else if (isDatabaseEmpty()) {
                    save(config.getInitialModel());
                }
            }

        }
        catch (SQLException e) {
            throw new PersistenceException("Database connection failed", e);
        }
    }


    private void initDb() throws SQLException {
        try (Connection c = dataSource.getConnection(); Statement stmt = c.createStatement()) {
            String schema = " (id TEXT PRIMARY KEY, content JSONB NOT NULL, seq BIGSERIAL)";
            stmt.execute("CREATE TABLE IF NOT EXISTS " + TABLE_AAS + schema);
            stmt.execute("CREATE TABLE IF NOT EXISTS " + TABLE_SUBMODEL + schema);
            stmt.execute("CREATE TABLE IF NOT EXISTS " + TABLE_CONCEPT + schema);
            stmt.execute("CREATE TABLE IF NOT EXISTS " + TABLE_OP_RESULT + schema);
        }
    }


    private boolean isDatabaseEmpty() throws SQLException {
        try (Connection c = dataSource.getConnection();
                Statement stmt = c.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1 FROM " + TABLE_AAS + " LIMIT 1")) {
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


    public AssetAdministrationShell getAssetAdministrationShell(String id) throws ResourceNotFoundException {
        return loadEntity(TABLE_AAS, id, AssetAdministrationShell.class);
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
        List<AssetAdministrationShell> all = loadAllEntities(TABLE_AAS, AssetAdministrationShell.class);
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
        return preparePagedResult(stream, modifier, paging);
    }


    @Override
    public Page<AssetAdministrationShell> findAssetAdministrationShellsWithQuery(
                                                                                 AssetAdministrationShellSearchCriteria criteria,
                                                                                 QueryModifier modifier,
                                                                                 PagingInfo paging,
                                                                                 Query query)
            throws PersistenceException {

        throw new UnsupportedOperationException("Query in SQL not supported for now.");
    }


    @Override
    public void save(AssetAdministrationShell shell) {
        saveEntity(TABLE_AAS, shell.getId(), shell);
    }


    @Override
    public void deleteAssetAdministrationShell(String id) {
        deleteEntity(TABLE_AAS, id);
    }


    public Submodel getSubmodel(String id) throws ResourceNotFoundException {
        return loadEntity(TABLE_SUBMODEL, id, Submodel.class);
    }


    @Override
    public Submodel getSubmodel(String id, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
        return prepareResult(
                getSubmodel(id), modifier);
    }


    @Override
    public Page<Submodel> findSubmodels(SubmodelSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws PersistenceException {
        List<Submodel> all = loadAllEntities(TABLE_SUBMODEL, Submodel.class);
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
    public Page<Submodel> findSubmodelsWithQuery(
                                                 SubmodelSearchCriteria criteria,
                                                 QueryModifier modifier,
                                                 PagingInfo paging,
                                                 Query query)
            throws PersistenceException {

        throw new UnsupportedOperationException("Query in SQL not supported for now.");
    }


    @Override
    public void save(Submodel submodel) {
        saveEntity(TABLE_SUBMODEL, submodel.getId(), submodel);
    }


    @Override
    public void deleteSubmodel(String id) {
        deleteEntity(TABLE_SUBMODEL, id);
    }


    public List<Submodel> getSubmodels() {
        return loadAllEntities(TABLE_SUBMODEL, Submodel.class);
    }


    @Override
    public SubmodelElement getSubmodelElement(SubmodelElementIdentifier identifier, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(identifier, "identifier must not be null");
        return prepareResult(
                EnvironmentHelper.resolve(identifier.toReference(), getSubmodel(identifier.getSubmodelId()), SubmodelElement.class),
                modifier);
    }


    @Override
    public Page<SubmodelElement> findSubmodelElements(SubmodelElementSearchCriteria criteria, QueryModifier modifier, PagingInfo paging)
            throws ResourceNotFoundException, PersistenceException {
        List<Submodel> submodelsToCheck = new ArrayList<>();
        if (criteria.isParentSet() && criteria.getParent().getSubmodelId() != null) {
            try {
                submodelsToCheck.add(getSubmodel(criteria.getParent().getSubmodelId()));
            }
            catch (ResourceNotFoundException ignored) {}
        }
        else {
            submodelsToCheck.addAll(getSubmodels());
        }

        List<SubmodelElement> allMatches = new ArrayList<>();

        for (Submodel submodel: submodelsToCheck) {
            Collection<SubmodelElement> searchStart;
            if (criteria.isParentSet()) {
                String pathStr = criteria.getParent().getIdShortPath() != null ? criteria.getParent().getIdShortPath().toString() : "";
                if (pathStr.isEmpty()) {
                    searchStart = submodel.getSubmodelElements();
                }
                else {
                    try {
                        SubmodelElement parentEl = resolveElement(submodel, criteria.getParent());
                        if (parentEl instanceof SubmodelElementCollection) {
                            searchStart = ((SubmodelElementCollection) parentEl).getValue();
                        }
                        else if (parentEl instanceof SubmodelElementList) {
                            searchStart = ((SubmodelElementList) parentEl).getValue();
                        }
                        else {
                            searchStart = Collections.emptyList();
                        }
                    }
                    catch (ResourceNotFoundException e) {
                        continue;
                    }
                }
            }
            else {
                searchStart = submodel.getSubmodelElements();
            }

            if (searchStart != null) {
                collectMatchingElements(searchStart, criteria, allMatches);
            }
        }
        return preparePagedResult(allMatches.stream(), modifier, paging);
    }


    private void collectMatchingElements(Collection<SubmodelElement> elements, SubmodelElementSearchCriteria criteria, List<SubmodelElement> result) {
        if (elements == null)
            return;
        for (SubmodelElement el: elements) {
            boolean matches = true;
            if (criteria.isSemanticIdSet()) {
                if (!ReferenceHelper.equals(el.getSemanticId(), criteria.getSemanticId())) {
                    matches = false;
                }
            }
            if (matches) {
                result.add(el);
            }
            if (el instanceof SubmodelElementCollection) {
                collectMatchingElements(((SubmodelElementCollection) el).getValue(), criteria, result);
            }
            else if (el instanceof SubmodelElementList) {
                collectMatchingElements(((SubmodelElementList) el).getValue(), criteria, result);
            }
        }
    }


    @Override
    public void insert(SubmodelElementIdentifier parentIdentifier, SubmodelElement submodelElement)
            throws ResourceNotFoundException, ResourceNotAContainerElementException, ResourceAlreadyExistsException, PersistenceException {
        Submodel submodel = getSubmodel(parentIdentifier.getSubmodelId());
        String pathStr = parentIdentifier.getIdShortPath() != null ? parentIdentifier.getIdShortPath().toString() : "";

        if (pathStr.isEmpty()) {
            checkIdShortExists(submodel.getSubmodelElements(), submodelElement.getIdShort());
            submodel.getSubmodelElements().add(submodelElement);
        }
        else {
            SubmodelElement parent = resolveElement(submodel, parentIdentifier);
            if (parent instanceof SubmodelElementCollection coll) {
                checkIdShortExists(coll.getValue(), submodelElement.getIdShort());
                coll.getValue().add(submodelElement);
            }
            else if (parent instanceof SubmodelElementList list) {
                if (submodelElement.getIdShort() != null) {
                    checkIdShortExists(list.getValue(), submodelElement.getIdShort());
                }
                list.getValue().add(submodelElement);
            }
            else {
                throw new ResourceNotAContainerElementException(parentIdentifier.toString());
            }
        }
        save(submodel);
    }


    private SubmodelElement resolveElement(Submodel submodel, SubmodelElementIdentifier identifier) throws ResourceNotFoundException {
        List<String> path = getPathSegments(identifier);
        if (path.isEmpty())
            throw new ResourceNotFoundException("Identifier path is empty");

        SubmodelElement current = findInCollection(submodel.getSubmodelElements(), path.get(0));
        for (int i = 1; i < path.size(); i++) {
            String segment = path.get(i);
            if (current instanceof SubmodelElementCollection) {
                current = findInCollection(((SubmodelElementCollection) current).getValue(), segment);
            }
            else if (current instanceof SubmodelElementList) {
                try {
                    int index = Integer.parseInt(segment);
                    List<SubmodelElement> list = ((SubmodelElementList) current).getValue();
                    if (index < 0 || index >= list.size())
                        throw new ResourceNotFoundException("List index out of bounds");
                    current = list.get(index);
                }
                catch (NumberFormatException e) {
                    throw new ResourceNotFoundException("Invalid list index: " + segment);
                }
            }
            else {
                throw new ResourceNotFoundException("Element " + path.get(i - 1) + " is not a container");
            }
        }
        return current;
    }


    @Override
    public void update(SubmodelElementIdentifier identifier, SubmodelElement submodelElement) throws ResourceNotFoundException, PersistenceException {
        Submodel submodel = getSubmodel(identifier.getSubmodelId());
        ContainerContext ctx = resolveParentContainer(submodel, identifier);

        if (ctx.isList) {
            int index = Integer.parseInt(ctx.targetIdentifier);
            if (index < 0 || index >= ctx.list.size()) {
                throw new ResourceNotFoundException("List index out of bounds: " + index);
            }
            ctx.list.set(index, submodelElement);
        }
        else {
            int idx = -1;
            for (int i = 0; i < ctx.list.size(); i++) {
                if (Objects.equals(ctx.list.get(i).getIdShort(), ctx.targetIdentifier)) {
                    idx = i;
                    break;
                }
            }
            if (idx == -1)
                throw new ResourceNotFoundException("Element not found: " + ctx.targetIdentifier);
            ctx.list.set(idx, submodelElement);
        }
        save(submodel);
    }


    private void checkIdShortExists(Collection<SubmodelElement> collection, String idShort) throws ResourceAlreadyExistsException {
        if (idShort == null || collection == null)
            return;
        if (collection.stream().anyMatch(e -> Objects.equals(e.getIdShort(), idShort))) {
            throw new ResourceAlreadyExistsException("Element with idShort '" + idShort + "' already exists.");
        }
    }

    private static class ContainerContext {
        List<SubmodelElement> list;
        String targetIdentifier; // idShort OR index string
        boolean isList; // true if parent is a SubmodelElementList

        ContainerContext(List<SubmodelElement> list, String targetIdentifier, boolean isList) {
            this.list = list;
            this.targetIdentifier = targetIdentifier;
            this.isList = isList;
        }
    }

    private ContainerContext resolveParentContainer(Submodel submodel, SubmodelElementIdentifier identifier) throws ResourceNotFoundException {
        List<String> path = getPathSegments(identifier);
        if (path.isEmpty())
            throw new ResourceNotFoundException("Path is empty");

        String targetIdentifier = path.get(path.size() - 1);
        List<SubmodelElement> containerList;
        boolean isList = false;

        if (path.size() == 1) {
            containerList = submodel.getSubmodelElements();
        }
        else {
            // Navigate to the parent (n-2)
            SubmodelElement current = findInCollection(submodel.getSubmodelElements(), path.get(0));
            for (int i = 1; i < path.size() - 1; i++) {
                String segment = path.get(i);
                if (current instanceof SubmodelElementCollection) {
                    current = findInCollection(((SubmodelElementCollection) current).getValue(), segment);
                }
                else if (current instanceof SubmodelElementList) {
                    try {
                        int index = Integer.parseInt(segment);
                        current = ((SubmodelElementList) current).getValue().get(index);
                    }
                    catch (Exception e) {
                        throw new ResourceNotFoundException("Invalid path in list: " + segment);
                    }
                }
                else {
                    throw new ResourceNotFoundException("Element " + path.get(i - 1) + " is not a container");
                }
            }

            // Determine container type of parent
            if (current instanceof SubmodelElementCollection) {
                containerList = ((SubmodelElementCollection) current).getValue();
            }
            else if (current instanceof SubmodelElementList) {
                containerList = ((SubmodelElementList) current).getValue();
                isList = true;
            }
            else {
                throw new ResourceNotFoundException("Parent element is not a container");
            }
        }
        return new ContainerContext(containerList, targetIdentifier, isList);
    }


    private List<String> getPathSegments(SubmodelElementIdentifier identifier) {
        if (identifier.getIdShortPath() == null)
            return Collections.emptyList();
        String path = identifier.getIdShortPath().toString();
        if (path == null || path.isEmpty())
            return Collections.emptyList();

        // Updated to handle array notation in paths e.g. "list[0]"
        return Arrays.stream(path.split(PATH_SPLIT_REGEX))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }


    private SubmodelElement findInCollection(Collection<SubmodelElement> collection, String idShort) throws ResourceNotFoundException {
        if (collection == null)
            throw new ResourceNotFoundException("Collection is null");
        return collection.stream()
                .filter(e -> Objects.equals(e.getIdShort(), idShort))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Element not found: " + idShort));
    }


    @Override
    public void deleteSubmodelElement(SubmodelElementIdentifier identifier) throws ResourceNotFoundException, PersistenceException {
        Submodel submodel = getSubmodel(identifier.getSubmodelId());
        ContainerContext ctx = resolveParentContainer(submodel, identifier);

        if (ctx.isList) {
            int index = Integer.parseInt(ctx.targetIdentifier);
            if (index < 0 || index >= ctx.list.size()) {
                throw new ResourceNotFoundException("List index out of bounds: " + index);
            }
            ctx.list.remove(index);
        }
        else {
            boolean removed = ctx.list.removeIf(e -> Objects.equals(e.getIdShort(), ctx.targetIdentifier));
            if (!removed)
                throw new ResourceNotFoundException("Element not found: " + ctx.targetIdentifier);
        }
        save(submodel);
    }


    public ConceptDescription getConceptDescription(String id) throws ResourceNotFoundException {
        return loadEntity(TABLE_CONCEPT, id, ConceptDescription.class);
    }


    @Override
    public ConceptDescription getConceptDescription(String id, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
        return prepareResult(
                getConceptDescription(id),
                modifier);
    }


    @Override
    public Page<ConceptDescription> findConceptDescriptions(ConceptDescriptionSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws PersistenceException {
        List<ConceptDescription> all = loadAllEntities(TABLE_CONCEPT, ConceptDescription.class);
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
    public Page<ConceptDescription> findConceptDescriptionsWithQuery(
                                                                     ConceptDescriptionSearchCriteria criteria,
                                                                     QueryModifier modifier,
                                                                     PagingInfo paging,
                                                                     Query query)
            throws PersistenceException {

        throw new UnsupportedOperationException("Query in SQL not supported for now.");
    }


    @Override
    public void save(ConceptDescription conceptDescription) {
        saveEntity(TABLE_CONCEPT, conceptDescription.getId(), conceptDescription);
    }


    @Override
    public void deleteConceptDescription(String id) {
        deleteEntity(TABLE_CONCEPT, id);
    }


    @Override
    public OperationResult getOperationResult(OperationHandle handle) throws ResourceNotFoundException, PersistenceException {
        return loadEntity(TABLE_OP_RESULT, handle.getHandleId(), OperationResult.class);
    }


    @Override
    public void save(OperationHandle handle, OperationResult result) throws PersistenceException {
        saveEntity(TABLE_OP_RESULT, handle.getHandleId(), result);
    }


    public void save(Environment environment) {
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
        try (Connection c = dataSource.getConnection(); Statement stmt = c.createStatement()) {
            stmt.execute("TRUNCATE TABLE " + TABLE_AAS);
            stmt.execute("TRUNCATE TABLE " + TABLE_SUBMODEL);
            stmt.execute("TRUNCATE TABLE " + TABLE_CONCEPT);
            stmt.execute("TRUNCATE TABLE " + TABLE_OP_RESULT);
        }
        catch (SQLException e) {
            throw new PersistenceException("Failed to clear database", e);
        }
    }

    //helpers


    private <T> void saveEntity(String table, String id, T entity) {
        if (id == null)
            throw new IllegalArgumentException("Entity ID cannot be null");
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
            throw new RuntimeException("Failed to save to " + table + ": " + id, e);
        }
    }


    private <T> T loadEntity(String table, String id, Class<T> clazz) throws ResourceNotFoundException {
        String sql = "SELECT content FROM " + table + " WHERE id = ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement statement = c.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return jsonDeserializer.read(rs.getString("content"), clazz);
                }
                else {
                    throw new ResourceNotFoundException(clazz.getSimpleName() + " with id " + id + " not found");
                }
            }
        }
        catch (Exception e) {
            if (e instanceof ResourceNotFoundException)
                throw (ResourceNotFoundException) e;
            throw new RuntimeException("Database error loading " + id, e);
        }
    }


    private <T> List<T> loadAllEntities(String table, Class<T> clazz) {
        List<T> results = new ArrayList<>();
        String sql = "SELECT content FROM " + table + " ORDER BY seq ASC";
        try (Connection c = dataSource.getConnection(); PreparedStatement pstmt = c.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(jsonDeserializer.read(rs.getString("content"), clazz));
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Database error loading all from " + table, e);
        }
        return results;
    }


    private void deleteEntity(String table, String id) {
        String sql = "DELETE FROM " + table + " WHERE id = ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException("Database error deleting " + id, e);
        }
    }


    private static <T extends Referable> Page<T> preparePagedResult(Stream<T> input, QueryModifier modifier, PagingInfo paging) {
        Page<T> result = preparePagedResult(input, paging);
        result.setContent(QueryModifierHelper.applyQueryModifier(
                result.getContent().stream()
                        .map(DeepCopyHelper::deepCopy)
                        .collect(Collectors.toList()),
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
                        .collect(Collectors.toList()))
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


    private static String nextCursor(PagingInfo paging, int resultCount) {
        return nextCursor(paging, paging.hasLimit() && resultCount > paging.getLimit());
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


    private static <T extends Referable> T prepareResult(T result, QueryModifier modifier) {
        return QueryModifierHelper.applyQueryModifier(
                DeepCopyHelper.deepCopy(result),
                modifier);
    }
}
