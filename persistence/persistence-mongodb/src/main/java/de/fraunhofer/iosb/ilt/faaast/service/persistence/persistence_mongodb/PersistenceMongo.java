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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.persistence_mongodb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AssetAdministrationShellSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.ConceptDescriptionSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelElementSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemory;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.LoggerFactory;


/**
 * Persistence implementation for a mongo database.
 */
public class PersistenceMongo implements Persistence<PersistenceMongoConfig> {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PersistenceMongo.class);
    private final String ID_KEY = "model_id";
    private final String ENVIRONMENT_KEY = "environment";

    private PersistenceMongoConfig config;
    private MongoCollection<Document> environmentCollection;
    private PersistenceInMemory persistenceInMemory;
    private ObjectMapper mapper;

    @Override
    public void init(CoreConfig coreConfig, PersistenceMongoConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;

        mapper = new ObjectMapper();

        MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(config.getConnectionString()))
                        .build());
        MongoDatabase database = mongoClient.getDatabase(config.getDatabaseName());
        environmentCollection = database.getCollection(config.getCollectionName());

        Document environmentDocument = environmentCollection.find(Filters.eq(ID_KEY, config.getModelId())).first();
        Environment aasEnvironment = null;
        if (environmentDocument == null) {
            try {
                aasEnvironment = config.loadInitialModel();
                insertEnvironment(config.getModelId(), aasEnvironment);
            }
            catch (ConfigurationException | JsonProcessingException | DeserializationException e) {
                throw new ConfigurationInitializationException(e);
            }
        }
        else {
            try {
                aasEnvironment = loadEnvironment(config.getModelId());
            }
            catch (DeserializationException e) {
                throw new ConfigurationInitializationException(e);
            }
        }
        try {
            persistenceInMemory = PersistenceInMemoryConfig.builder()
                    .initialModel(aasEnvironment)
                    .build()
                    .newInstance(coreConfig, serviceContext);
        }
        catch (ConfigurationException e) {
            throw new ConfigurationInitializationException(e);
        }
    }


    @Override
    public PersistenceMongoConfig asConfig() {
        return config;
    }


    private Environment loadEnvironment(String modelId) throws DeserializationException {
        Document environmentDocument = environmentCollection.find(Filters.eq(ID_KEY, modelId)).first();
        String envJsonString = ((Document) environmentDocument.get(ENVIRONMENT_KEY)).toJson();
        JsonApiDeserializer deserializer = new JsonApiDeserializer();
        return deserializer.read(envJsonString, Environment.class);
    }


    private void insertEnvironment(String modelId, Environment aasEnvironment) throws JsonProcessingException {
        Document environmentDocument = getEnvironmentDocument(aasEnvironment);
        environmentCollection.insertOne(environmentDocument);
    }


    private void saveEnvironment(String modelId, Environment aasEnvironment) {
        Document environmentDocument = null;
        try {
            environmentDocument = getEnvironmentDocument(aasEnvironment);
        }
        catch (JsonProcessingException e) {
            LOGGER.error("Serialising aas environment failed!");
        }

        Bson filter = Filters.eq(ID_KEY, modelId);
        Bson updateOperation = Updates.set(ENVIRONMENT_KEY, environmentDocument.get(ENVIRONMENT_KEY));
        environmentCollection.updateOne(filter, updateOperation);
    }


    private Document getEnvironmentDocument(Environment aasEnvironment) throws JsonProcessingException {
        String environmentJsonString = mapper.writeValueAsString(aasEnvironment);
        return new Document()
                .append(ID_KEY, config.getModelId())
                .append(ENVIRONMENT_KEY, Document.parse(environmentJsonString));
    }


    @Override
    public AssetAdministrationShell getAssetAdministrationShell(String id, QueryModifier modifier) throws ResourceNotFoundException {
        return persistenceInMemory.getAssetAdministrationShell(id, modifier);
    }


    @Override
    public Submodel getSubmodel(String id, QueryModifier modifier) throws ResourceNotFoundException {
        return persistenceInMemory.getSubmodel(id, modifier);
    }


    @Override
    public ConceptDescription getConceptDescription(String id, QueryModifier modifier) throws ResourceNotFoundException {
        return persistenceInMemory.getConceptDescription(id, modifier);
    }


    @Override
    public SubmodelElement getSubmodelElement(SubmodelElementIdentifier identifier, QueryModifier modifier) throws ResourceNotFoundException {
        return persistenceInMemory.getSubmodelElement(identifier, modifier);
    }


    @Override
    public Page<SubmodelElement> getSubmodelElements(SubmodelElementIdentifier identifier, QueryModifier modifier)
            throws ResourceNotFoundException, ResourceNotAContainerElementException {
        return persistenceInMemory.getSubmodelElements(identifier, modifier);
    }


    @Override
    public OperationResult getOperationResult(OperationHandle handle) throws ResourceNotFoundException {
        return persistenceInMemory.getOperationResult(handle);
    }


    @Override
    public Page<AssetAdministrationShell> findAssetAdministrationShells(AssetAdministrationShellSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) {
        return persistenceInMemory.findAssetAdministrationShells(criteria, modifier, paging);
    }


    @Override
    public Page<Submodel> findSubmodels(SubmodelSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) {
        return persistenceInMemory.findSubmodels(criteria, modifier, paging);
    }


    @Override
    public Page<SubmodelElement> findSubmodelElements(SubmodelElementSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws ResourceNotFoundException {
        return persistenceInMemory.findSubmodelElements(criteria, modifier, paging);
    }


    @Override
    public Page<ConceptDescription> findConceptDescriptions(ConceptDescriptionSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) {
        return persistenceInMemory.findConceptDescriptions(criteria, modifier, paging);
    }


    @Override
    public void save(AssetAdministrationShell assetAdministrationShell) {
        persistenceInMemory.save(assetAdministrationShell);
        saveEnvironment(config.getModelId(), persistenceInMemory.getEnvironment());
    }


    @Override
    public void save(ConceptDescription conceptDescription) {
        persistenceInMemory.save(conceptDescription);
        saveEnvironment(config.getModelId(), persistenceInMemory.getEnvironment());
    }


    @Override
    public void save(Submodel submodel) {
        persistenceInMemory.save(submodel);
        saveEnvironment(config.getModelId(), persistenceInMemory.getEnvironment());
    }


    @Override
    public void insert(SubmodelElementIdentifier parentIdentifier, SubmodelElement submodelElement) throws ResourceNotFoundException, ResourceNotAContainerElementException {
        persistenceInMemory.insert(parentIdentifier, submodelElement);
        saveEnvironment(config.getModelId(), persistenceInMemory.getEnvironment());
    }


    @Override
    public void update(SubmodelElementIdentifier identifier, SubmodelElement submodelElement) throws ResourceNotFoundException {
        persistenceInMemory.update(identifier, submodelElement);
        saveEnvironment(config.getModelId(), persistenceInMemory.getEnvironment());
    }


    @Override
    public void save(OperationHandle handle, OperationResult result) {
        persistenceInMemory.save(handle, result);
        saveEnvironment(config.getModelId(), persistenceInMemory.getEnvironment());
    }


    @Override
    public void deleteAssetAdministrationShell(String id) throws ResourceNotFoundException {
        persistenceInMemory.deleteAssetAdministrationShell(id);
        saveEnvironment(config.getModelId(), persistenceInMemory.getEnvironment());
    }


    @Override
    public void deleteSubmodel(String id) throws ResourceNotFoundException {
        persistenceInMemory.deleteSubmodel(id);
        saveEnvironment(config.getModelId(), persistenceInMemory.getEnvironment());
    }


    @Override
    public void deleteConceptDescription(String id) throws ResourceNotFoundException {
        persistenceInMemory.deleteConceptDescription(id);
        saveEnvironment(config.getModelId(), persistenceInMemory.getEnvironment());
    }


    @Override
    public void deleteSubmodelElement(SubmodelElementIdentifier identifier) throws ResourceNotFoundException {
        persistenceInMemory.deleteSubmodelElement(identifier);
        saveEnvironment(config.getModelId(), persistenceInMemory.getEnvironment());
    }


    @Override
    public void deleteAssetAdministrationShell(AssetAdministrationShell assetAdministrationShell) throws ResourceNotFoundException {
        persistenceInMemory.deleteAssetAdministrationShell(assetAdministrationShell);
        saveEnvironment(config.getModelId(), persistenceInMemory.getEnvironment());
    }


    @Override
    public void deleteSubmodel(Submodel submodel) throws ResourceNotFoundException {
        persistenceInMemory.deleteSubmodel(submodel);
        saveEnvironment(config.getModelId(), persistenceInMemory.getEnvironment());
    }


    @Override
    public void deleteConceptDescription(ConceptDescription conceptDescription) throws ResourceNotFoundException {
        persistenceInMemory.deleteConceptDescription(conceptDescription);
        saveEnvironment(config.getModelId(), persistenceInMemory.getEnvironment());
    }


    @Override
    public void deleteSubmodelElement(Reference reference) throws ResourceNotFoundException {
        persistenceInMemory.deleteSubmodelElement(reference);
        saveEnvironment(config.getModelId(), persistenceInMemory.getEnvironment());
    }


    @Override
    public void insert(Reference parent, SubmodelElement submodelElement) throws ResourceNotFoundException, ResourceNotAContainerElementException {
        persistenceInMemory.insert(parent, submodelElement);
        saveEnvironment(config.getModelId(), persistenceInMemory.getEnvironment());
    }


    @Override
    public void update(Reference reference, SubmodelElement submodelElement) throws ResourceNotFoundException {
        persistenceInMemory.update(reference, submodelElement);
        saveEnvironment(config.getModelId(), persistenceInMemory.getEnvironment());
    }


    @Override
    public <T extends SubmodelElement> T getSubmodelElement(SubmodelElementIdentifier identifier, QueryModifier modifier, Class<T> type) throws ResourceNotFoundException {
        return Persistence.super.getSubmodelElement(identifier, modifier, type);
    }


    @Override
    public SubmodelElement getSubmodelElement(Reference reference, QueryModifier modifier) throws ResourceNotFoundException {
        return persistenceInMemory.getSubmodelElement(reference, modifier);
    }


    @Override
    public <T extends SubmodelElement> T getSubmodelElement(Reference reference, QueryModifier modifier, Class<T> type) throws ResourceNotFoundException {
        return persistenceInMemory.getSubmodelElement(reference, modifier, type);
    }


    @Override
    public Page<SubmodelElement> getSubmodelElements(Reference reference, QueryModifier modifier) throws ResourceNotFoundException, ResourceNotAContainerElementException {
        return persistenceInMemory.getSubmodelElements(reference, modifier);
    }


    @Override
    public Page<AssetAdministrationShell> getAllAssetAdministrationShells(QueryModifier modifier, PagingInfo paging) {
        return persistenceInMemory.getAllAssetAdministrationShells(modifier, paging);
    }


    @Override
    public Page<Submodel> getAllSubmodels(QueryModifier modifier, PagingInfo paging) {
        return persistenceInMemory.getAllSubmodels(modifier, paging);
    }


    @Override
    public Page<ConceptDescription> getAllConceptDescriptions(QueryModifier modifier, PagingInfo paging) {
        return persistenceInMemory.getAllConceptDescriptions(modifier, paging);
    }


    @Override
    public Page<SubmodelElement> getAllSubmodelElements(QueryModifier modifier, PagingInfo paging) throws ResourceNotFoundException {
        return persistenceInMemory.getAllSubmodelElements(modifier, paging);
    }
}
