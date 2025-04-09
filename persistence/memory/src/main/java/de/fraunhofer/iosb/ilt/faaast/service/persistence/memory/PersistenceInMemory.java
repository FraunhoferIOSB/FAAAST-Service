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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.memory;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.AssetAdministrationShellElementWalker;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.DefaultAssetAdministrationShellElementVisitor;
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
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.HasSemantics;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;


/**
 * Implementation of {@link de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence} for in memory storage.
 *
 * <p>Following types are not supported in the current version:
 * <ul>
 * <li>AASX packages
 * <li>Package Descriptors
 * </ul>
 */
public class PersistenceInMemory implements Persistence<PersistenceInMemoryConfig> {

    private static final String MSG_RESOURCE_NOT_FOUND_BY_ID = "resource not found (id %s)";
    private static final String MSG_ID_NOT_NULL = "id must be non-null";
    private static final String MSG_MODIFIER_NOT_NULL = "modifier must be non-null";
    private static final String MSG_CRITERIA_NOT_NULL = "criteria must be non-null";
    private static final String MSG_PAGING_NOT_NULL = "paging must be non-null";

    private Environment environment;
    private PersistenceInMemoryConfig config;
    private Map<OperationHandle, OperationResult> operationStates;

    public PersistenceInMemory() {
        operationStates = new ConcurrentHashMap<>();
    }


    public Environment getEnvironment() {
        return environment;
    }


    public Map<OperationHandle, OperationResult> getOperationStates() {
        return operationStates;
    }


    @Override
    public void start() throws PersistenceException {
        //intentionally left empty
    }


    @Override
    public void stop() {
        //intentionally left empty
    }


    public void setOperationStates(Map<OperationHandle, OperationResult> operationStates) {
        this.operationStates = operationStates;
    }


    @Override
    public void deleteAssetAdministrationShell(String id) throws ResourceNotFoundException {
        Ensure.requireNonNull(id, MSG_ID_NOT_NULL);
        if (!environment.getAssetAdministrationShells().removeIf(x -> Objects.equals(x.getId(), id))) {
            throw new ResourceNotFoundException(String.format(MSG_RESOURCE_NOT_FOUND_BY_ID, id));
        }
    }


    @Override
    public void deleteConceptDescription(String id) throws ResourceNotFoundException {
        Ensure.requireNonNull(id, MSG_ID_NOT_NULL);
        if (!environment.getConceptDescriptions().removeIf(x -> Objects.equals(x.getId(), id))) {
            throw new ResourceNotFoundException(String.format(MSG_RESOURCE_NOT_FOUND_BY_ID, id));
        }
    }


    @Override
    public void deleteSubmodel(String id) throws ResourceNotFoundException {
        Ensure.requireNonNull(id, MSG_ID_NOT_NULL);
        if (!environment.getSubmodels().removeIf(x -> Objects.equals(x.getId(), id))) {
            throw new ResourceNotFoundException(String.format(MSG_RESOURCE_NOT_FOUND_BY_ID, id));
        }
        // TODO check if submodelRef inside AAS should really be deleted or this has to be done manually
        Reference submodelRef = ReferenceBuilder.forSubmodel(id);
        environment.getAssetAdministrationShells().forEach(x -> x.getSubmodels().remove(submodelRef));
    }


    @Override
    public void deleteSubmodelElement(SubmodelElementIdentifier identifier) throws ResourceNotFoundException {
        Ensure.requireNonNull(identifier, "path must be non-null");
        final Reference reference = identifier.toReference();
        final SubmodelElement element = EnvironmentHelper.resolve(reference, environment, SubmodelElement.class);
        Referable parent = EnvironmentHelper.resolve(ReferenceHelper.getParent(reference), environment);
        final AtomicBoolean deleted = new AtomicBoolean(false);
        AssetAdministrationShellElementWalker.builder()
                .visitor(new DefaultAssetAdministrationShellElementVisitor() {
                    @Override
                    public void visit(SubmodelElementCollection submodelElementCollection) {
                        deleted.compareAndSet(false, submodelElementCollection.getValue().remove(element));
                    }


                    @Override
                    public void visit(SubmodelElementList submodelElementList) {
                        deleted.compareAndSet(false, submodelElementList.getValue().remove(element));
                    }


                    @Override
                    public void visit(Submodel submodel) {
                        deleted.compareAndSet(false, submodel.getSubmodelElements().remove(element));
                    }
                })
                .build()
                .walk(parent);
        if (!deleted.get()) {
            throw new ResourceNotFoundException(reference);
        }
    }


    @Override
    public void deleteAll() throws PersistenceException {
        operationStates.clear();
        environment = new DefaultEnvironment();
    }


    @Override
    public Page<AssetAdministrationShell> findAssetAdministrationShells(AssetAdministrationShellSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) {
        Ensure.requireNonNull(criteria, MSG_CRITERIA_NOT_NULL);
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(paging, MSG_PAGING_NOT_NULL);

        Stream<AssetAdministrationShell> result = environment.getAssetAdministrationShells().stream();
        if (criteria.isIdShortSet()) {
            result = filterByIdShort(result, criteria.getIdShort());
        }
        if (criteria.isAssetIdsSet()) {
            result = filterByAssetIds(result, criteria.getAssetIds());
        }
        return preparePagedResult(result, modifier, paging);
    }


    @Override
    public Page<ConceptDescription> findConceptDescriptions(ConceptDescriptionSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) {
        Ensure.requireNonNull(criteria, MSG_CRITERIA_NOT_NULL);
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(paging, MSG_PAGING_NOT_NULL);
        Stream<ConceptDescription> result = environment.getConceptDescriptions().stream();
        if (criteria.isIdShortSet()) {
            result = filterByIdShort(result, criteria.getIdShort());
        }
        if (criteria.isIsCaseOfSet()) {
            result = filterByIsCaseOf(result, criteria.getIsCaseOf());
        }
        if (criteria.isDataSpecificationSet()) {
            result = filterByDataSpecification(result, criteria.getDataSpecification());
        }
        return preparePagedResult(result, modifier, paging);
    }


    @Override
    public Page<SubmodelElement> findSubmodelElements(SubmodelElementSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws ResourceNotFoundException {
        Ensure.requireNonNull(criteria, MSG_CRITERIA_NOT_NULL);
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(paging, MSG_PAGING_NOT_NULL);
        final Collection<SubmodelElement> elements = new ArrayList<>();
        if (criteria.isParentSet()) {
            Referable parent = EnvironmentHelper.resolve(criteria.getParent().toReference(), environment);
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
        else {
            AssetAdministrationShellElementWalker.builder()
                    .visitor(new DefaultAssetAdministrationShellElementVisitor() {
                        @Override
                        public void visit(SubmodelElement submodelElement) {
                            elements.add(submodelElement);
                        }
                    })
                    .build()
                    .walk(environment);
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


    @Override
    public Page<Submodel> findSubmodels(SubmodelSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) {
        Ensure.requireNonNull(criteria, MSG_CRITERIA_NOT_NULL);
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(paging, MSG_PAGING_NOT_NULL);
        Stream<Submodel> result = environment.getSubmodels().stream();
        if (criteria.isIdShortSet()) {
            result = filterByIdShort(result, criteria.getIdShort());
        }
        if (criteria.isSemanticIdSet()) {
            result = filterBySemanticId(result, criteria.getSemanticId());
        }
        return preparePagedResult(result, modifier, paging);
    }


    @Override
    public AssetAdministrationShell getAssetAdministrationShell(String id, QueryModifier modifier) throws ResourceNotFoundException {
        return prepareResult(
                filterById(environment.getAssetAdministrationShells().stream(), id)
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException(String.format(MSG_RESOURCE_NOT_FOUND_BY_ID, id))),
                modifier);
    }


    @Override
    public ConceptDescription getConceptDescription(String id, QueryModifier modifier) throws ResourceNotFoundException {
        return prepareResult(
                filterById(environment.getConceptDescriptions().stream(), id)
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException(String.format(MSG_RESOURCE_NOT_FOUND_BY_ID, id))),
                modifier);
    }


    @Override
    public OperationResult getOperationResult(OperationHandle handle) throws ResourceNotFoundException {
        Ensure.requireNonNull(handle, "handle must be non-null");
        return Ensure.requireNonNull(
                operationStates.get(handle),
                new ResourceNotFoundException(String.format("Operation handle does not exist (hanldeId: %s)", handle.getHandleId())));
    }


    @Override
    public Submodel getSubmodel(String id, QueryModifier modifier) throws ResourceNotFoundException {
        return prepareResult(
                filterById(environment.getSubmodels().stream(), id)
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException(String.format(MSG_RESOURCE_NOT_FOUND_BY_ID, id))),
                modifier);
    }


    @Override
    public SubmodelElement getSubmodelElement(SubmodelElementIdentifier identifier, QueryModifier modifier) throws ResourceNotFoundException {
        return prepareResult(
                EnvironmentHelper.resolve(identifier.toReference(), environment, SubmodelElement.class),
                modifier);
    }


    @Override
    public Page<Reference> getSubmodelRefs(String aasId, PagingInfo paging) throws ResourceNotFoundException {
        return preparePagedResult(
                getAssetAdministrationShell(aasId, QueryModifier.MINIMAL).getSubmodels().stream(),
                paging);
    }


    @Override
    public void init(CoreConfig coreConfig, PersistenceInMemoryConfig config, ServiceContext context) throws ConfigurationInitializationException {
        Ensure.requireNonNull(config, "config must be non-null");
        Ensure.requireNonNull(context, "context must be non-null");
        this.config = config;
        try {
            this.environment = config.loadInitialModel();
        }
        catch (InvalidConfigurationException | DeserializationException e) {
            throw new ConfigurationInitializationException("error initializing in-memory persistence", e);
        }
    }


    @Override
    public PersistenceInMemoryConfig asConfig() {
        return config;
    }


    @Override
    public void insert(SubmodelElementIdentifier parentIdentifier, SubmodelElement submodelElement) throws ResourceNotFoundException, ResourceNotAContainerElementException {
        Ensure.requireNonNull(parentIdentifier, "parent must be non-null");
        Ensure.requireNonNull(submodelElement, "submodelElement must be non-null");
        Referable parent = EnvironmentHelper.resolve(parentIdentifier.toReference(), environment);

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
            throw new IllegalArgumentException(String.format("illegal type for identifiable: %s. Must be one of: %s, %s, %s",
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
    }


    @Override
    public void update(SubmodelElementIdentifier identifier, SubmodelElement submodelElement) throws ResourceNotFoundException {
        Ensure.requireNonNull(identifier, "identifier must be non-null");
        Ensure.requireNonNull(submodelElement, "submodelElement must be non-null");
        SubmodelElement oldElement = getSubmodelElement(identifier, QueryModifier.DEFAULT);
        Referable parent = EnvironmentHelper.resolve(ReferenceHelper.getParent(identifier.toReference()), environment);

        if (SubmodelElementList.class.isAssignableFrom(parent.getClass())) {
            int index = Integer.parseInt(identifier.getIdShortPath().getElements().get(identifier.getIdShortPath().getElements().size() - 1).substring(1, 2));
            ((SubmodelElementList) parent).getValue().set(index, submodelElement);
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
            throw new IllegalArgumentException(String.format("illegal type for identifiable: %s. Must be one of: %s, %s, %s",
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
    }


    @Override
    public void save(AssetAdministrationShell assetAdministrationShell) {
        saveOrUpdateById(environment.getAssetAdministrationShells(), assetAdministrationShell);
    }


    @Override
    public void save(ConceptDescription conceptDescription) {
        saveOrUpdateById(environment.getConceptDescriptions(), conceptDescription);
    }


    @Override
    public void save(Submodel submodel) {
        saveOrUpdateById(environment.getSubmodels(), submodel);
    }


    @Override
    public void save(OperationHandle handle, OperationResult result) {
        operationStates.put(handle, result);
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


    private static <T extends Referable> Stream<T> filterByIdShort(Stream<T> stream, String idShort) {
        return stream.filter(x -> Objects.equals(x.getIdShort(), idShort));
    }


    private static <T extends Identifiable> Stream<T> filterById(Stream<T> stream, String id) {
        return stream.filter(x -> Objects.equals(x.getId(), id));
    }


    private static Stream<AssetAdministrationShell> filterByAssetIds(Stream<AssetAdministrationShell> stream, List<AssetIdentification> assetIds) {
        if (Objects.isNull(assetIds)) {
            return stream;
        }
        Stream<AssetAdministrationShell> result = stream;
        List<String> globalAssetIdentificators = assetIds.stream()
                .filter(x -> GlobalAssetIdentification.class.isAssignableFrom(x.getClass()))
                .map(GlobalAssetIdentification.class::cast)
                .map(x -> x.getValue())
                .collect(Collectors.toList());
        List<SpecificAssetId> specificAssetIdentificators = assetIds.stream()
                .filter(x -> SpecificAssetIdentification.class.isAssignableFrom(x.getClass()))
                .map(x -> new DefaultSpecificAssetId.Builder()
                        .name(((SpecificAssetIdentification) x).getKey())
                        .value(((SpecificAssetIdentification) x).getValue())
                        .build())
                .collect(Collectors.toList());

        if (!globalAssetIdentificators.isEmpty()) {
            result = result.filter(x -> globalAssetIdentificators.contains(x.getAssetInformation().getGlobalAssetId()));
        }
        if (!specificAssetIdentificators.isEmpty()) {
            result = result.filter(x -> specificAssetIdentificators.stream().anyMatch(y -> x.getAssetInformation().getSpecificAssetIds().contains(y)));
        }
        return result;
    }


    private static Stream<ConceptDescription> filterByIsCaseOf(Stream<ConceptDescription> stream, Reference isCaseOf) {
        if (Objects.isNull(isCaseOf)) {
            return stream;
        }
        return stream.filter(x -> x.getIsCaseOf().stream().anyMatch(y -> ReferenceHelper.equals(y, isCaseOf)));
    }


    private static Stream<ConceptDescription> filterByDataSpecification(Stream<ConceptDescription> stream, Reference dataSpecification) {
        if (Objects.isNull(dataSpecification)) {
            return stream;
        }
        return stream.filter(x -> Objects.nonNull(x.getEmbeddedDataSpecifications())
                && x.getEmbeddedDataSpecifications().stream()
                        .anyMatch(y -> ReferenceHelper.equals(y.getDataSpecification(), dataSpecification)));
    }


    private static <T extends Referable> T prepareResult(T result, QueryModifier modifier) {
        return QueryModifierHelper.applyQueryModifier(
                DeepCopyHelper.deepCopy(result),
                modifier);
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


    private static <T> Page<T> preparePagedResult(Stream<T> input, PagingInfo paging) {
        Stream<T> result = input;
        if (Objects.nonNull(paging.getCursor())) {
            result = result.skip(readCursor(paging.getCursor()));
        }
        if (paging.hasLimit()) {
            result = result.limit(paging.getLimit() + 1);
        }
        List<T> temp = result.collect(Collectors.toList());
        return Page.<T> builder()
                .result(temp.stream()
                        .limit(paging.hasLimit() ? paging.getLimit() : temp.size())
                        .collect(Collectors.toList()))
                .metadata(PagingMetadata.builder()
                        .cursor(nextCursor(paging, temp.size()))
                        .build())
                .build();
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


    private static <T extends Identifiable> void saveOrUpdateById(Collection<T> container, T element) {
        CollectionHelper.put(container,
                container.stream()
                        .filter(x -> Objects.nonNull(x.getId()) && x.getId().equalsIgnoreCase(element.getId()))
                        .findFirst()
                        .orElse(null),
                element);
    }
}
