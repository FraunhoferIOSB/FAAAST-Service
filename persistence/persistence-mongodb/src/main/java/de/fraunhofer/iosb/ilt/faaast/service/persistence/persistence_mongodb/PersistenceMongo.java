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
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AssetAdministrationShellSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.ConceptDescriptionSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelElementSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelSearchCriteria;
import java.util.List;

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


/**
 * lol.
 */
public class PersistenceMongo implements Persistence<PersistenceMongoConfig> {

    private final String ID_KEY = "model_id";
    private final String ENVIRONMENT_KEY = "environment";

    private PersistenceMongoConfig config;
    private CoreConfig coreConfig;
    private ServiceContext serviceContext;
    private MongoCollection<Document> environmentCollection;
    private ObjectMapper mapper;

    @Override
    public void init(CoreConfig coreConfig, PersistenceMongoConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
        this.coreConfig = coreConfig;
        this.serviceContext = serviceContext;

        mapper = new ObjectMapper();

        MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
                        .build());
        MongoDatabase database = mongoClient.getDatabase(config.getDatabaseName());
        environmentCollection = database.getCollection(config.getCollectionName());

        Document modelDoc = environmentCollection.find(Filters.eq(ID_KEY, config.getModelId())).first();
        if (modelDoc == null) {
            try {
                Document envDoc = Document.parse(mapper.writeValueAsString());
                modelDoc = new Document()
                        .append(ID_KEY, config.getModelId())
                        .append(ENVIRONMENT_KEY, envDoc);
                 environmentCollection.insertOne(modelDoc);
            } catch (Exception e) {
                throw new ConfigurationInitializationException(e);
            }
        }
        getMemoryPersistence();
    }

    @Override
    public PersistenceMongoConfig asConfig() {
        return config;
    }

    private PersistenceInMemory getMemoryPersistence() {
        Document envDoc = environmentCollection.find(Filters.eq(ID_KEY, config.getModelId())).first();
        Environment aasEnvironment = (Environment) envDoc.get(ENVIRONMENT_KEY);
        try {
            return PersistenceInMemoryConfig.builder()
                    .initialModel(aasEnvironment)
                    .build()
                    .newInstance(coreConfig, serviceContext);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveEnvironment(Environment aasEnvironment) {
        Bson filter = Filters.eq(ID_KEY, config.getModelId());
        Bson updateOperation = Updates.set(ENVIRONMENT_KEY, aasEnvironment);
        environmentCollection.updateOne(filter, updateOperation);
    }


    @Override
    public AssetAdministrationShell getAssetAdministrationShell(String id, QueryModifier modifier) throws ResourceNotFoundException {
        return getMemoryPersistence().getAssetAdministrationShell(id, modifier);
    }


    @Override
    public Submodel getSubmodel(String id, QueryModifier modifier) throws ResourceNotFoundException {
        return null;
    }


    @Override
    public ConceptDescription getConceptDescription(String id, QueryModifier modifier) throws ResourceNotFoundException {
        return null;
    }


    @Override
    public SubmodelElement getSubmodelElement(SubmodelElementIdentifier identifier, QueryModifier modifier) throws ResourceNotFoundException {
        return null;
    }


    @Override
    public List<SubmodelElement> getSubmodelElements(SubmodelElementIdentifier identifier, QueryModifier modifier)
            throws ResourceNotFoundException, ResourceNotAContainerElementException {
        return Persistence.super.getSubmodelElements(identifier, modifier);
    }


    @Override
    public OperationResult getOperationResult(OperationHandle handle) throws ResourceNotFoundException {
        return null;
    }


    @Override
    public List<AssetAdministrationShell> findAssetAdministrationShells(AssetAdministrationShellSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) {
        return null;
    }


    @Override
    public List<Submodel> findSubmodels(SubmodelSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) {
        return null;
    }


    @Override
    public List<SubmodelElement> findSubmodelElements(SubmodelElementSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws ResourceNotFoundException {
        return null;
    }


    @Override
    public List<ConceptDescription> findConceptDescriptions(ConceptDescriptionSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) {
        return null;
    }


    @Override
    public void save(AssetAdministrationShell assetAdministrationShell) {
        PersistenceInMemory persistence = getMemoryPersistence();
        persistence.save(assetAdministrationShell);
        saveEnvironment(persistence.getEnvironment());
    }


    @Override
    public void save(ConceptDescription conceptDescription) {

    }


    @Override
    public void save(Submodel submodel) {

    }


    @Override
    public void save(SubmodelElementIdentifier identifier, SubmodelElement submodelElement) throws ResourceNotFoundException, ResourceNotAContainerElementException {

    }


    @Override
    public void save(OperationHandle handle, OperationResult result) {

    }


    @Override
    public void deleteAssetAdministrationShell(String id) throws ResourceNotFoundException {

    }


    @Override
    public void deleteSubmodel(String id) throws ResourceNotFoundException {

    }


    @Override
    public void deleteConceptDescription(String id) throws ResourceNotFoundException {

    }


    @Override
    public void deleteSubmodelElement(SubmodelElementIdentifier identifier) throws ResourceNotFoundException {

    }


    @Override
    public void deleteAssetAdministrationShell(AssetAdministrationShell assetAdministrationShell) throws ResourceNotFoundException {
        Persistence.super.deleteAssetAdministrationShell(assetAdministrationShell);
    }


    @Override
    public void deleteSubmodel(Submodel submodel) throws ResourceNotFoundException {
        Persistence.super.deleteSubmodel(submodel);
    }


    @Override
    public void deleteConceptDescription(ConceptDescription conceptDescription) throws ResourceNotFoundException {
        Persistence.super.deleteConceptDescription(conceptDescription);
    }


    @Override
    public void deleteSubmodelElement(Reference reference) throws ResourceNotFoundException {
        Persistence.super.deleteSubmodelElement(reference);
    }


    @Override
    public void save(Reference parent, SubmodelElement submodelElement) throws ResourceNotFoundException, ResourceNotAContainerElementException {
        Persistence.super.save(parent, submodelElement);
    }


    @Override
    public <T extends SubmodelElement> T getSubmodelElement(SubmodelElementIdentifier identifier, QueryModifier modifier, Class<T> type) throws ResourceNotFoundException {
        return Persistence.super.getSubmodelElement(identifier, modifier, type);
    }


    @Override
    public SubmodelElement getSubmodelElement(Reference reference, QueryModifier modifier) throws ResourceNotFoundException {
        return Persistence.super.getSubmodelElement(reference, modifier);
    }


    @Override
    public <T extends SubmodelElement> T getSubmodelElement(Reference reference, QueryModifier modifier, Class<T> type) throws ResourceNotFoundException {
        return Persistence.super.getSubmodelElement(reference, modifier, type);
    }


    @Override
    public List<SubmodelElement> getSubmodelElements(Reference reference, QueryModifier modifier) throws ResourceNotFoundException, ResourceNotAContainerElementException {
        return Persistence.super.getSubmodelElements(reference, modifier);
    }


    @Override
    public List<AssetAdministrationShell> getAllAssetAdministrationShells(QueryModifier modifier, PagingInfo paging) {
        return Persistence.super.getAllAssetAdministrationShells(modifier, paging);
    }


    @Override
    public List<Submodel> getAllSubmodels(QueryModifier modifier, PagingInfo paging) {
        return Persistence.super.getAllSubmodels(modifier, paging);
    }


    @Override
    public List<ConceptDescription> getAllConceptDescriptions(QueryModifier modifier, PagingInfo paging) {
        return Persistence.super.getAllConceptDescriptions(modifier, paging);
    }


    @Override
    public List<SubmodelElement> getAllSubmodelElements(QueryModifier modifier, PagingInfo paging) throws ResourceNotFoundException {
        return Persistence.super.getAllSubmodelElements(modifier, paging);
    }
}
