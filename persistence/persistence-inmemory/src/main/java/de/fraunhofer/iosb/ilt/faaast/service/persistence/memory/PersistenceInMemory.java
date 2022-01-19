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

import static de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.Util.applyQueryModifier;
import static de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.Util.empty;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.AASXPackage;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.PackageDescription;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.managers.aasx.PackagePersistenceManager;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.managers.identifiable.IdentifiablePersistenceManager;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.managers.referable.ReferablePersistenceManager;
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


/**
 * Implementation of Persistence interface for in memory storage To decouple the
 * returned AAS objects from the internal AAS Environment each object is copied
 * deeply in advance
 */
public class PersistenceInMemory implements Persistence<PersistenceInMemoryConfig> {

    private AssetAdministrationShellEnvironment aasEnvironment;
    private Map<String, OperationResult> operationResultMap = new ConcurrentHashMap<>();
    private Map<String, OperationHandle> operationHandleMap = new ConcurrentHashMap<>();
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
    }


    @Override
    public AssetAdministrationShellEnvironment getEnvironment() {
        return this.aasEnvironment;
    }


    @Override
    public <T extends Identifiable> T get(Identifier id, QueryModifier modifier) {
        if (id == null || modifier == null) {
            return null;
        }
        Identifiable identifiable = identifiablePersistenceManager.getIdentifiableById(id);
        applyQueryModifier(identifiable, modifier);

        return (T) identifiable;
    }


    @Override
    public SubmodelElement get(Reference reference, QueryModifier modifier) {
        if (reference == null || reference.getKeys() == null || modifier == null || this.aasEnvironment == null) {
            return null;
        }
        SubmodelElement submodelElement = referablePersistenceManager.getSubmodelElement(reference, modifier);
        applyQueryModifier(submodelElement, modifier);
        return submodelElement;
    }


    @Override
    public List<AssetAdministrationShell> get(String idShort, AssetIdentification assetId, QueryModifier modifier) {
        if (modifier == null || (!empty(idShort) && assetId != null)) {
            return null;
        }
        List<AssetAdministrationShell> shells = identifiablePersistenceManager.getAASs(idShort, assetId);
        if (shells == null) {
            return null;
        }
        applyQueryModifier(shells, modifier);
        return shells;
    }


    @Override
    public List<Submodel> get(String idShort, Reference semanticId, QueryModifier modifier) {
        if (modifier == null) {
            return null;
        }

        //TODO: allow this combination? Throw meaningful exception if not.
        if (!empty(idShort) && semanticId != null) {
            return null;
        }

        List<Submodel> submodels = identifiablePersistenceManager.getSubmodels(idShort, semanticId);
        if (submodels == null) {
            return null;
        }
        applyQueryModifier(submodels, modifier);
        return submodels;
    }


    @Override
    public List<SubmodelElement> getSubmodelElements(Reference reference, Reference semanticId, QueryModifier modifier) {
        if (reference == null || modifier == null) {
            return null;
        }

        List<SubmodelElement> submodelElements = referablePersistenceManager.getSubmodelElements(reference, semanticId);
        if (submodelElements == null) {
            return null;
        }
        applyQueryModifier(submodelElements, modifier);
        return submodelElements;
    }


    @Override
    public List<ConceptDescription> get(String idShort, Reference isCaseOf, Reference dataSpecification, QueryModifier modifier) {
        if (modifier == null) {
            return null;
        }

        List<ConceptDescription> conceptDescriptions = identifiablePersistenceManager.getConceptDescriptions(idShort, isCaseOf, dataSpecification);
        applyQueryModifier(conceptDescriptions, modifier);
        return conceptDescriptions;
    }


    @Override
    public SubmodelElement put(Reference parent, SubmodelElement submodelElement) {
        if (parent == null || submodelElement == null) {
            return null;
        }
        return referablePersistenceManager.putSubmodelElement(parent, submodelElement);
    }


    @Override
    public void remove(Identifier id) {
        if (id == null) {
            return;
        }
        identifiablePersistenceManager.remove(id);
    }


    @Override
    public void remove(Reference reference) {
        if (reference == null) {
            return;
        }
        referablePersistenceManager.remove(reference);
    }


    @Override
    public Identifiable put(Reference parent, Identifiable identifiable) {
        if (parent == null || identifiable == null) {
            return null;
        }
        return identifiablePersistenceManager.put(parent, identifiable);
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
    public void remove(String packageId) {
        throw new UnsupportedOperationException();
    }


    @Override
    public AASXPackage put(String packageId, Set aasIds, AASXPackage file, String fileName) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String save(Set aasIds, AASXPackage file, String fileName) {
        throw new UnsupportedOperationException();
    }


    @Override
    public OperationResult getOperationResult(String handleId) {
        if (!empty(handleId)) {
            return operationResultMap.getOrDefault(handleId, null);
        }
        return null;
    }


    @Override
    public OperationHandle putOperationContext(String handleId, String requestId, OperationResult operationResult) {
        if (empty(handleId)) {
            OperationHandle operationHandle = new OperationHandle.Builder()
                    .requestId(requestId)
                    .handleId(UUID.randomUUID().toString())
                    .build();
            operationHandleMap.put(operationHandle.getHandleId(), operationHandle);
            operationResultMap.putIfAbsent(handleId, operationResult);
            return operationHandle;
        }
        else if (!empty(handleId)) {
            operationResultMap.put(handleId, operationResult);
        }

        return null;
    }
}
