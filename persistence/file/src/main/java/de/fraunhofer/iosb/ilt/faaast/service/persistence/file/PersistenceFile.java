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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.file;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.EnvironmentSerializationManager;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.aasx.AASXPackage;
import de.fraunhofer.iosb.ilt.faaast.service.model.aasx.PackageDescription;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemory;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellEnvironment;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifier;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of {@link de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence} for a file storage.
 *
 * <p>Following types are not supported in the current version:
 * <ul>
 * <li>AASX packages
 * <li>Package Descriptors
 * <li>SubmodelElementStructs
 * </ul>
 */
public class PersistenceFile implements Persistence<PersistenceFileConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceFile.class);
    private PersistenceFileConfig config;
    private PersistenceInMemory persistence;

    @Override
    public PersistenceFileConfig asConfig() {
        return config;
    }


    @Override
    public <T extends Identifiable> T get(Identifier id, QueryModifier modifier, Class<T> type) throws ResourceNotFoundException {
        return persistence.get(id, modifier, type);
    }


    @Override
    public SubmodelElement get(Reference reference, QueryModifier modifier) throws ResourceNotFoundException {
        return persistence.get(reference, modifier);
    }


    @Override
    public List<AssetAdministrationShell> get(String idShort, List<AssetIdentification> assetIds, QueryModifier modifier) {
        return persistence.get(idShort, assetIds, modifier);
    }


    @Override
    public List<Submodel> get(String idShort, Reference semanticId, QueryModifier modifier) {
        return persistence.get(idShort, semanticId, modifier);
    }


    @Override
    public List<ConceptDescription> get(String idShort, Reference isCaseOf, Reference dataSpecification, QueryModifier modifier) {
        return persistence.get(idShort, isCaseOf, dataSpecification, modifier);
    }


    @Override
    public AASXPackage get(String packageId) {
        return persistence.get(packageId);
    }


    @Override
    public List<PackageDescription> get(Identifier aasId) {
        return persistence.get(aasId);
    }


    @Override
    public AssetAdministrationShellEnvironment getEnvironment() {
        return persistence.getEnvironment();
    }


    @Override
    public OperationVariable[] getOperationOutputVariables(Reference reference) {
        return persistence.getOperationOutputVariables(reference);
    }


    @Override
    public OperationResult getOperationResult(String handleId) {
        return persistence.getOperationResult(handleId);
    }


    @Override
    public List<SubmodelElement> getSubmodelElements(Reference reference, Reference semanticId, QueryModifier modifier) throws ResourceNotFoundException {
        return persistence.getSubmodelElements(reference, semanticId, modifier);
    }


    @Override
    public TypeInfo<?> getTypeInfo(Reference reference) {
        return persistence.getTypeInfo(reference);
    }


    @Override
    public void init(CoreConfig coreConfig, PersistenceFileConfig config, ServiceContext context) throws ConfigurationInitializationException {
        this.config = config;
        try {
            config.init();
            AssetAdministrationShellEnvironment aasEnvironment = config.loadInitialModel();
            persistence = PersistenceInMemoryConfig.builder()
                    .initialModel(aasEnvironment)
                    .build()
                    .newInstance(coreConfig, context);
            save();
        }
        catch (ConfigurationException | DeserializationException e) {
            throw new ConfigurationInitializationException("initializing file persistence failed", e);
        }
    }


    @Override
    public AASXPackage put(String packageId, Set<Identifier> aasIds, AASXPackage file, String fileName) {
        AASXPackage result = persistence.put(packageId, aasIds, file, fileName);
        save();
        return result;
    }


    @Override
    public String put(Set<Identifier> aasIds, AASXPackage file, String fileName) {
        String result = persistence.put(aasIds, file, fileName);
        save();
        return result;
    }


    @Override
    public OperationHandle putOperationContext(String handleId, String requestId, OperationResult operationResult) {
        OperationHandle result = persistence.putOperationContext(handleId, requestId, operationResult);
        save();
        return result;
    }


    private void save() {
        try {
            EnvironmentSerializationManager
                    .serializerFor(config.getDataformat())
                    .write(new File(String.valueOf(config.getFilePath())), persistence.getEnvironment());
        }
        catch (IOException | SerializationException e) {
            LOGGER.error(String.format("Could not save environment to file %s", config.getFilePath()), e);
        }
    }


    @Override
    public Identifiable put(Identifiable identifiable) {
        Identifiable element = persistence.put(identifiable);
        save();
        return element;
    }


    @Override
    public SubmodelElement put(Reference parent, Reference referenceToSubmodelElement, SubmodelElement submodelElement) throws ResourceNotFoundException {
        SubmodelElement element = persistence.put(parent, referenceToSubmodelElement, submodelElement);
        save();
        return element;
    }


    @Override
    public void remove(Identifier id) throws ResourceNotFoundException {
        persistence.remove(id);
        save();
    }


    @Override
    public void remove(Reference reference) throws ResourceNotFoundException {
        persistence.remove(reference);
        save();
    }


    @Override
    public void remove(String packageId) {
        persistence.remove(packageId);
        save();
    }
}
