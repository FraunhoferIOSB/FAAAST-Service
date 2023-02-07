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
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.aasx.AASXPackage;
import de.fraunhofer.iosb.ilt.faaast.service.model.aasx.PackageDescription;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.ExecutionState;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.manager.IdentifiablePersistenceManager;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.manager.PackagePersistenceManager;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.manager.ReferablePersistenceManager;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.util.QueryModifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeExtractor;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.Operation;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;


/**
 * Implementation of {@link de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence} for in memory storage.
 *
 * <p>To decouple the AAS objects from the internal AAS Environment each returned object is copied deeply in advance.
 * Following types are not supported in the current version:
 * <ul>
 * <li>AASX packages
 * <li>Package Descriptors
 * <li>SubmodelElementStructs
 * </ul>
 */
public class PersistenceInMemory implements Persistence<PersistenceInMemoryConfig> {

    protected static final String MSG_MODIFIER_NOT_NULL = "modifier must be non-null";
    protected AssetAdministrationShellEnvironment model;
    protected CoreConfig coreConfig;
    protected PersistenceInMemoryConfig config;
    protected ServiceContext context;
    protected final IdentifiablePersistenceManager identifiablePersistenceManager;
    protected final Map<String, OperationHandle> operationHandleMap;
    protected final Map<String, OperationResult> operationResultMap;
    protected final PackagePersistenceManager packagePersistenceManager;
    protected final ReferablePersistenceManager referablePersistenceManager;

    public PersistenceInMemory() {
        operationResultMap = new ConcurrentHashMap<>();
        operationHandleMap = new ConcurrentHashMap<>();
        identifiablePersistenceManager = new IdentifiablePersistenceManager();
        referablePersistenceManager = new ReferablePersistenceManager();
        packagePersistenceManager = new PackagePersistenceManager();
    }


    @Override
    public void init(CoreConfig coreConfig, PersistenceInMemoryConfig config, ServiceContext context) throws ConfigurationInitializationException {
        Ensure.requireNonNull(coreConfig, "coreConfig must be non-null");
        Ensure.requireNonNull(config, "config must be non-null");
        Ensure.requireNonNull(context, "context must be non-null");
        this.coreConfig = coreConfig;
        this.config = config;
        this.context = context;
        try {
            this.model = config.loadInitialModel();
        }
        catch (InvalidConfigurationException | DeserializationException e) {
            throw new ConfigurationInitializationException("error initializing in-memory persistence", e);
        }
        identifiablePersistenceManager.setAasEnvironment(model);
        referablePersistenceManager.setAasEnvironment(model);
        packagePersistenceManager.setAasEnvironment(model);
    }


    @Override
    public PersistenceInMemoryConfig asConfig() {
        return config;
    }


    @Override
    public AssetAdministrationShellEnvironment getEnvironment() {
        return model;
    }


    @Override
    public <T extends Identifiable> T get(Identifier id, QueryModifier modifier, Class<T> type) throws ResourceNotFoundException {
        Ensure.requireNonNull(id, "id must be non-null");
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(type, "type must be non-null");
        Identifiable result = identifiablePersistenceManager.getIdentifiableById(id);
        if (result == null) {
            throw new ResourceNotFoundException(id, type);
        }
        if (!type.isAssignableFrom(result.getClass())) {
            throw new ResourceNotFoundException(String.format("Resource found but does not match expected type (id: %s, expected type: %s, actual type: %s)",
                    id,
                    type,
                    result.getClass()));
        }
        return QueryModifierHelper.applyQueryModifier(
                type.cast(result),
                modifier);
    }


    private void ensureInitialized() {
        Ensure.requireNonNull(model, "aasEnvironment not properly initialized (must be non-null)");
    }


    @Override
    public SubmodelElement get(Reference reference, QueryModifier modifier) throws ResourceNotFoundException {
        ensureInitialized();
        if (ReferenceHelper.isNullOrEmpty(reference)) {
            return null;
        }
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        ReferenceHelper.completeReferenceWithProperKeyElements(reference, model);
        return QueryModifierHelper.applyQueryModifier(
                referablePersistenceManager.getSubmodelElement(reference, modifier),
                modifier);
    }


    @Override
    public List<AssetAdministrationShell> get(String idShort, List<AssetIdentification> assetIds, QueryModifier modifier) {
        ensureInitialized();
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        return QueryModifierHelper.applyQueryModifier(
                identifiablePersistenceManager.getAASs(idShort, assetIds),
                modifier);
    }


    @Override
    public List<Submodel> get(String idShort, Reference semanticId, QueryModifier modifier) {
        ensureInitialized();
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        return QueryModifierHelper.applyQueryModifier(
                identifiablePersistenceManager.getSubmodels(idShort, semanticId),
                modifier);
    }


    @Override
    public List<ConceptDescription> get(String idShort, Reference isCaseOf, Reference dataSpecification, QueryModifier modifier) {
        ensureInitialized();
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        return QueryModifierHelper.applyQueryModifier(
                identifiablePersistenceManager.getConceptDescriptions(idShort, isCaseOf, dataSpecification),
                modifier);
    }


    @Override
    public AASXPackage get(String packageId) {
        throw new UnsupportedOperationException("not yet implemented");
    }


    @Override
    public List<PackageDescription> get(Identifier aasId) {
        throw new UnsupportedOperationException("not yet implemented");
    }


    @Override
    public OperationResult getOperationResult(String handleId) {
        if (StringUtils.isNoneBlank(handleId)) {
            return operationResultMap.getOrDefault(handleId, null);
        }
        return null;
    }


    @Override
    public List<SubmodelElement> getSubmodelElements(Reference reference, Reference semanticId, QueryModifier modifier) throws ResourceNotFoundException {
        ensureInitialized();
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        ReferenceHelper.completeReferenceWithProperKeyElements(reference, model);
        return QueryModifierHelper.applyQueryModifier(
                referablePersistenceManager.getSubmodelElements(reference, semanticId),
                modifier);
    }


    @Override
    public SubmodelElement put(Reference parent, Reference referenceToSubmodelElement, SubmodelElement submodelElement) throws ResourceNotFoundException {
        ensureInitialized();
        Ensure.requireNonNull(submodelElement, "submodelElement must be non-null");
        Ensure.require(parent != null || referenceToSubmodelElement != null, "either parent or referenceToSubmodelElement must be non-null");
        if (parent != null) {
            ReferenceHelper.completeReferenceWithProperKeyElements(parent, model);
        }
        if (referenceToSubmodelElement != null) {
            ReferenceHelper.completeReferenceWithProperKeyElements(referenceToSubmodelElement, model);
        }
        return referablePersistenceManager.putSubmodelElement(parent, referenceToSubmodelElement, submodelElement);
    }


    @Override
    public Identifiable put(Identifiable identifiable) {
        return identifiablePersistenceManager.put(identifiable);
    }


    @Override
    public String put(Set<Identifier> aasIds, AASXPackage file, String fileName) {
        throw new UnsupportedOperationException();
    }


    @Override
    public AASXPackage put(String packageId, Set aasIds, AASXPackage file, String fileName) {
        throw new UnsupportedOperationException();
    }


    @Override
    public OperationHandle putOperationContext(String handleId, String requestId, OperationResult operationResult) {
        OperationResult operationResultLocal = operationResult != null
                ? operationResult
                : new OperationResult.Builder()
                        .executionState(ExecutionState.INITIATED)
                        .requestId(requestId)
                        .build();
        String handleIdLocal = handleId;
        if (StringUtils.isBlank(handleId)) {
            handleIdLocal = UUID.randomUUID().toString();
            OperationHandle operationHandle = new OperationHandle.Builder()
                    .requestId(requestId)
                    .handleId(handleIdLocal)
                    .build();
            operationHandleMap.put(operationHandle.getHandleId(), operationHandle);
        }
        operationResultMap.put(handleIdLocal, operationResultLocal);
        return operationHandleMap.get(handleIdLocal);
    }


    @Override
    public void remove(Identifier id) throws ResourceNotFoundException {
        if (id != null) {
            identifiablePersistenceManager.remove(id);
        }
    }


    @Override
    public void remove(Reference reference) throws ResourceNotFoundException {
        if (reference != null) {
            ReferenceHelper.completeReferenceWithProperKeyElements(reference, model);
            referablePersistenceManager.remove(reference);
        }
    }


    @Override
    public void remove(String packageId) {
        throw new UnsupportedOperationException();
    }


    @Override
    public TypeInfo<?> getTypeInfo(Reference reference) {
        return TypeExtractor.extractTypeInfo(AasUtils.resolve(reference, getEnvironment()));
    }


    @Override
    public OperationVariable[] getOperationOutputVariables(Reference reference) {
        if (reference == null) {
            throw new IllegalArgumentException("reference must be non-null");
        }
        Referable referable = AasUtils.resolve(reference, getEnvironment());
        if (referable == null) {
            throw new IllegalArgumentException(String.format("reference could not be resolved (reference: %s)", AasUtils.asString(reference)));
        }
        if (!Operation.class.isAssignableFrom(referable.getClass())) {
            throw new IllegalArgumentException(String.format("reference points to invalid type (reference: %s, expected type: Operation, actual type: %s)",
                    AasUtils.asString(reference),
                    referable.getClass()));
        }
        return ((Operation) referable).getOutputVariables().toArray(new OperationVariable[0]);
    }
}
