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
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.aasx.AASXPackage;
import de.fraunhofer.iosb.ilt.faaast.service.model.aasx.PackageDescription;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.ExecutionState;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.manager.IdentifiablePersistenceManager;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.manager.PackagePersistenceManager;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.manager.ReferablePersistenceManager;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.util.QueryModifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.Identifier;
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
 * Implementation of
 * {@link de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence}
 * for in memory storage.
 * <p>
 * To decouple the AAS objects from the internal AAS Environment each returned object is copied
 * deeply in advance.
 * Following types are not supported in the current version:
 * <p>
 * <ul>
 * <li>AASX packages
 * <li>Package Descriptors
 * <li>SubmodelElementStructs
 * </ul>
 * <p>
 */
public class PersistenceInMemory implements Persistence<PersistenceInMemoryConfig> {

    private AssetAdministrationShellEnvironment aasEnvironment;
    private final Map<String, OperationResult> operationResultMap = new ConcurrentHashMap<>();
    private final Map<String, OperationHandle> operationHandleMap = new ConcurrentHashMap<>();
    private final IdentifiablePersistenceManager identifiablePersistenceManager = new IdentifiablePersistenceManager();
    private final ReferablePersistenceManager referablePersistenceManager = new ReferablePersistenceManager();
    private final PackagePersistenceManager packagePersistenceManager = new PackagePersistenceManager();

    @Override
    public void init(CoreConfig coreConfig, PersistenceInMemoryConfig config, ServiceContext context) {

    }


    @Override
    public PersistenceInMemoryConfig asConfig() {
        return null;
    }


    @Override
    public void setEnvironment(AssetAdministrationShellEnvironment environment) {
        this.aasEnvironment = environment;
        this.identifiablePersistenceManager.setAasEnvironment(environment);
        this.referablePersistenceManager.setAasEnvironment(environment);
        this.packagePersistenceManager.setAasEnvironment(environment);
    }


    @Override
    public AssetAdministrationShellEnvironment getEnvironment() {
        return this.aasEnvironment;
    }


    @Override
    public <T extends Identifiable> T get(Identifier id, QueryModifier modifier) throws ResourceNotFoundException {
        if (id == null || modifier == null) {
            return null;
        }
        Identifiable identifiable = identifiablePersistenceManager.getIdentifiableById(id);
        QueryModifierHelper.applyQueryModifier(identifiable, modifier);

        return (T) identifiable;
    }


    @Override
    public SubmodelElement get(Reference reference, QueryModifier modifier) throws ResourceNotFoundException {
        if (reference == null || reference.getKeys() == null || modifier == null || this.aasEnvironment == null) {
            return null;
        }
        ReferenceHelper.completeReferenceWithProperKeyElements(reference, this.aasEnvironment);
        SubmodelElement submodelElement = referablePersistenceManager.getSubmodelElement(reference, modifier);
        QueryModifierHelper.applyQueryModifier(submodelElement, modifier);
        return submodelElement;
    }


    @Override
    public List<AssetAdministrationShell> get(String idShort, List<AssetIdentification> assetIds, QueryModifier modifier) {
        if (modifier == null || (StringUtils.isNoneBlank(idShort) && assetIds != null)) {
            return null;
        }
        List<AssetAdministrationShell> shells = identifiablePersistenceManager.getAASs(idShort, assetIds);
        if (shells == null) {
            return null;
        }
        QueryModifierHelper.applyQueryModifier(shells, modifier);
        return shells;
    }


    @Override
    public List<Submodel> get(String idShort, Reference semanticId, QueryModifier modifier) {
        if (modifier == null) {
            return null;
        }

        //TODO: allow this combination? Throw meaningful exception if not.
        if (StringUtils.isNoneBlank(idShort) && semanticId != null) {
            return null;
        }

        List<Submodel> submodels = identifiablePersistenceManager.getSubmodels(idShort, semanticId);
        if (submodels == null) {
            return null;
        }
        QueryModifierHelper.applyQueryModifier(submodels, modifier);
        return submodels;
    }


    @Override
    public List<SubmodelElement> getSubmodelElements(Reference reference, Reference semanticId, QueryModifier modifier) throws ResourceNotFoundException {
        if (reference == null || modifier == null) {
            return null;
        }
        ReferenceHelper.completeReferenceWithProperKeyElements(reference, this.aasEnvironment);

        List<SubmodelElement> submodelElements = referablePersistenceManager.getSubmodelElements(reference, semanticId);
        if (submodelElements == null) {
            return null;
        }
        QueryModifierHelper.applyQueryModifier(submodelElements, modifier);
        return submodelElements;
    }


    @Override
    public List<ConceptDescription> get(String idShort, Reference isCaseOf, Reference dataSpecification, QueryModifier modifier) {
        if (modifier == null) {
            return null;
        }

        List<ConceptDescription> conceptDescriptions = identifiablePersistenceManager.getConceptDescriptions(idShort, isCaseOf, dataSpecification);
        QueryModifierHelper.applyQueryModifier(conceptDescriptions, modifier);
        return conceptDescriptions;
    }


    @Override
    public SubmodelElement put(Reference parent, Reference referenceToSubmodelElement, SubmodelElement submodelElement) throws ResourceNotFoundException {
        if ((parent == null && referenceToSubmodelElement == null) || submodelElement == null) {
            return null;
        }
        if (parent != null) {
            ReferenceHelper.completeReferenceWithProperKeyElements(parent, this.aasEnvironment);
        }
        if (referenceToSubmodelElement != null) {
            ReferenceHelper.completeReferenceWithProperKeyElements(referenceToSubmodelElement, this.aasEnvironment);
        }
        return referablePersistenceManager.putSubmodelElement(parent, referenceToSubmodelElement, submodelElement);
    }


    @Override
    public void remove(Identifier id) throws ResourceNotFoundException {
        if (id == null) {
            return;
        }
        identifiablePersistenceManager.remove(id);
    }


    @Override
    public void remove(Reference reference) throws ResourceNotFoundException {
        if (reference == null) {
            return;
        }
        ReferenceHelper.completeReferenceWithProperKeyElements(reference, this.aasEnvironment);
        referablePersistenceManager.remove(reference);
    }


    @Override
    public Identifiable put(Identifiable identifiable) {
        if (identifiable == null) {
            return null;
        }
        return identifiablePersistenceManager.put(identifiable);
    }


    @Override
    public AASXPackage get(String packageId) {
        throw new UnsupportedOperationException();
    }


    @Override
    public List<PackageDescription> get(Identifier aasId) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String put(Set<Identifier> aasIds, AASXPackage file, String fileName) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void remove(String packageId) {
        throw new UnsupportedOperationException();
    }


    @Override
    public AASXPackage put(String packageId, Set aasIds, AASXPackage file, String fileName) {
        throw new UnsupportedOperationException();
    }


    @Override
    public OperationResult getOperationResult(String handleId) {
        if (StringUtils.isNoneBlank(handleId)) {
            return operationResultMap.getOrDefault(handleId, null);
        }
        return null;
    }


    @Override
    public OperationHandle putOperationContext(String handleId, String requestId, OperationResult operationResult) {
        if (operationResult == null) {
            operationResult = new OperationResult.Builder()
                    .executionState(ExecutionState.INITIATED)
                    .requestId(requestId)
                    .build();
        }

        if (StringUtils.isBlank(handleId)) {
            OperationHandle operationHandle = new OperationHandle.Builder()
                    .requestId(requestId)
                    .handleId(UUID.randomUUID().toString())
                    .build();
            operationHandleMap.put(operationHandle.getHandleId(), operationHandle);
            operationResultMap.putIfAbsent(operationHandle.getHandleId(), operationResult);
            return operationHandle;
        }
        else {
            operationResultMap.put(handleId, operationResult);
        }

        return null;
    }
}
