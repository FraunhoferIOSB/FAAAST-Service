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

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
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
import de.fraunhofer.iosb.ilt.faaast.service.persistence.util.PagingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.util.QueryModifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.LoggerFactory;


/**
 * Persistence implementation for a mongo database.
 */
public class PersistenceMongo implements Persistence<PersistenceMongoConfig> {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PersistenceMongo.class);
    private static final String MSG_RESOURCE_NOT_FOUND_BY_ID = "resource not found (id %s)";
    private static final String MSG_MODIFIER_NOT_NULL = "modifier must be non-null";
    private static final String MSG_CRITERIA_NOT_NULL = "criteria must be non-null";
    private static final String MSG_PAGING_NOT_NULL = "paging must be non-null";
    private static final Bson NO_FILTER = Filters.exists("_id");

    private static final String AAS_COLLECTION_NAME = "aasCol";
    private static final String CD_COLLECTION_NAME = "cdCol";
    private static final String SUBMODEL_COLLECTION_NAME = "submodelCol";

    private PersistenceMongoConfig config;
    private MongoCollection<Document> aasCollection, cdCollection, submodelCollection;
    private JsonApiSerializer jsonApiSerializer = new JsonApiSerializer();
    private JsonApiDeserializer jsonApiDeserializer = new JsonApiDeserializer();

    @Override
    public void init(CoreConfig coreConfig, PersistenceMongoConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;

        MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(config.getConnectionString()))
                        .build());
        MongoDatabase database = mongoClient.getDatabase(config.getDatabaseName());
        aasCollection = database.getCollection(AAS_COLLECTION_NAME);
        cdCollection = database.getCollection(CD_COLLECTION_NAME);
        submodelCollection = database.getCollection(SUBMODEL_COLLECTION_NAME);

        if (!config.isUseExisting()) {
            aasCollection.drop();
            cdCollection.drop();
            submodelCollection.drop();

            try {
                saveEnvironment(config.loadInitialModel());
                deleteSubmodel("https://acplt.org/Test_Submodel");
            }
            catch (DeserializationException | InvalidConfigurationException e) {
                throw new ConfigurationInitializationException(e);
            }
            catch (ResourceNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public PersistenceMongoConfig asConfig() {
        return config;
    }


    public void saveEnvironment(Environment environment) {
        if (!saveListInCollection(environment.getAssetAdministrationShells(), aasCollection)) {
            throw new IllegalStateException("Failed to save AAS");
        }
        if (!saveListInCollection(environment.getSubmodels(), submodelCollection)) {
            throw new IllegalStateException("Failed to save Submodels");
        }

        if (!saveListInCollection(environment.getConceptDescriptions(), cdCollection)) {
            throw new IllegalStateException("Failed to save CD");
        }
    }


    private boolean saveListInCollection(List<? extends Identifiable> list, MongoCollection<Document> collection) {
        if (list.isEmpty())
            return true;
        List<Document> docList = list.stream()
                .map(this::getDocument)
                .collect(Collectors.toList());
        InsertManyResult result = collection.insertMany(docList);
        return result.wasAcknowledged();
    }


    private <T extends Identifiable> T loadElementById(MongoCollection<Document> collection, String id, Class<T> type) throws ResourceNotFoundException {
        Bson filter = Filters.eq("id", id);
        Document resultDoc = collection.find(filter).first();
        if (resultDoc == null)
            throw new ResourceNotFoundException(String.format(MSG_RESOURCE_NOT_FOUND_BY_ID, id));

        try {
            return jsonApiDeserializer.read(resultDoc.toJson(), type);
        }
        catch (DeserializationException e) {
            throw new RuntimeException(e); // TODO
        }
    }


    private void deleteElementById(MongoCollection<Document> collection, String id) throws ResourceNotFoundException {
        Bson filter = Filters.eq("id", id);
        DeleteResult result = collection.deleteOne(filter);
        if (result.getDeletedCount() == 0) {
            throw new ResourceNotFoundException(String.format(MSG_RESOURCE_NOT_FOUND_BY_ID, id));
        }
    }


    private Document getDocument(Identifiable identifiable) {
        try {
            String json = jsonApiSerializer.write(identifiable);
            return Document.parse(json);
        }
        catch (SerializationException e) {
            throw new RuntimeException("Error serializing identifiable to JSON", e);
        }
    }


    private void saveOrUpdateById(MongoCollection<Document> collection, Identifiable element) {
        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(true);
        Bson filter = Filters.eq("id", element.getId());
        collection.updateOne(filter, getDocument(element), updateOptions);
    }


    @Override
    public AssetAdministrationShell getAssetAdministrationShell(String id, QueryModifier modifier) throws ResourceNotFoundException {
        return prepareResult(loadElementById(aasCollection, id, AssetAdministrationShell.class), modifier);
    }


    @Override
    public Submodel getSubmodel(String id, QueryModifier modifier) throws ResourceNotFoundException {
        return prepareResult(loadElementById(submodelCollection, id, Submodel.class), modifier);
    }


    @Override
    public ConceptDescription getConceptDescription(String id, QueryModifier modifier) throws ResourceNotFoundException {
        return prepareResult(loadElementById(cdCollection, id, ConceptDescription.class), modifier);
    }


    @Override
    public SubmodelElement getSubmodelElement(SubmodelElementIdentifier identifier, QueryModifier modifier) throws ResourceNotFoundException {
        return null;
    }


    @Override
    public Page<SubmodelElement> getSubmodelElements(SubmodelElementIdentifier identifier, QueryModifier modifier)
            throws ResourceNotFoundException, ResourceNotAContainerElementException {
        return Persistence.super.getSubmodelElements(identifier, modifier);
    }


    @Override
    public OperationResult getOperationResult(OperationHandle handle) throws ResourceNotFoundException {
        return null;
    }


    @Override
    public Page<AssetAdministrationShell> findAssetAdministrationShells(AssetAdministrationShellSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) {
        Ensure.requireNonNull(criteria, MSG_CRITERIA_NOT_NULL);
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(paging, MSG_PAGING_NOT_NULL);
        return null;
    }


    @Override
    public Page<Submodel> findSubmodels(SubmodelSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) {
        Ensure.requireNonNull(criteria, MSG_CRITERIA_NOT_NULL);
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(paging, MSG_PAGING_NOT_NULL);

        Bson filter = NO_FILTER;
        if (criteria.isIdShortSet())
            filter = Filters.and(filter, getIdShortFilter(criteria.getIdShort()));
        if (criteria.isSemanticIdSet())
            filter = Filters.and(filter, getSemanticIdFilter(criteria.getSemanticId()));

        return PagingHelper.preparePagedResult(getResultStream(submodelCollection.find(filter), Submodel.class), modifier, paging);
    }


    @Override
    public Page<SubmodelElement> findSubmodelElements(SubmodelElementSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws ResourceNotFoundException {
        Ensure.requireNonNull(criteria, MSG_CRITERIA_NOT_NULL);
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(paging, MSG_PAGING_NOT_NULL);
        return null;
    }


    @Override
    public Page<ConceptDescription> findConceptDescriptions(ConceptDescriptionSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) {
        Ensure.requireNonNull(criteria, MSG_CRITERIA_NOT_NULL);
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(paging, MSG_PAGING_NOT_NULL);

        Bson filter = NO_FILTER;
        if (criteria.isIdShortSet())
            filter = Filters.and(filter, getIdShortFilter(criteria.getIdShort()));
        if (criteria.isIsCaseOfSet())
            filter = Filters.and(filter, getIsCaseOfFilter(criteria.getIsCaseOf()));
        if (criteria.isDataSpecificationSet())
            filter = Filters.and(filter, getDataSpecificationFilter(criteria.getDataSpecification()));

        return PagingHelper.preparePagedResult(getResultStream(cdCollection.find(filter), ConceptDescription.class), modifier, paging);
    }


    @Override
    public void save(AssetAdministrationShell assetAdministrationShell) {
        saveOrUpdateById(aasCollection, assetAdministrationShell);
    }


    @Override
    public void save(ConceptDescription conceptDescription) {
        saveOrUpdateById(cdCollection, conceptDescription);
    }


    @Override
    public void save(Submodel submodel) {
        saveOrUpdateById(submodelCollection, submodel);
    }


    @Override
    public void insert(SubmodelElementIdentifier parentIdentifier, SubmodelElement submodelElement) throws ResourceNotFoundException, ResourceNotAContainerElementException {

    }


    @Override
    public void update(SubmodelElementIdentifier identifier, SubmodelElement submodelElement) throws ResourceNotFoundException {

    }


    @Override
    public void save(OperationHandle handle, OperationResult result) {

    }


    @Override
    public void deleteAssetAdministrationShell(String id) throws ResourceNotFoundException {
        deleteElementById(aasCollection, id);
    }


    @Override
    public void deleteSubmodel(String id) throws ResourceNotFoundException {
        deleteElementById(submodelCollection, id);
        Reference submodelRef = ReferenceBuilder.forSubmodel(id);
        Document referenceDocument = getReferenceDocument(submodelRef);
        Bson filter = Filters.eq("submodels", referenceDocument);
        Document update = new Document("$pull", new Document("submodels", referenceDocument));
        aasCollection.updateMany(filter, update);
    }


    @Override
    public void deleteConceptDescription(String id) throws ResourceNotFoundException {
        deleteElementById(cdCollection, id);
    }


    @Override
    public void deleteSubmodelElement(SubmodelElementIdentifier identifier) throws ResourceNotFoundException {

    }


    @Override
    public void insert(Reference parent, SubmodelElement submodelElement) throws ResourceNotFoundException, ResourceNotAContainerElementException {
        Persistence.super.insert(parent, submodelElement);
    }


    @Override
    public void update(Reference reference, SubmodelElement submodelElement) throws ResourceNotFoundException {
        Persistence.super.update(reference, submodelElement);
    }


    @Override
    public <T extends SubmodelElement> T getSubmodelElement(SubmodelElementIdentifier identifier, QueryModifier modifier, Class<T> type) throws ResourceNotFoundException {
        return Persistence.super.getSubmodelElement(identifier, modifier, type);
    }


    @Override
    public <T extends SubmodelElement> T getSubmodelElement(Reference reference, QueryModifier modifier, Class<T> type) throws ResourceNotFoundException {
        return Persistence.super.getSubmodelElement(reference, modifier, type);
    }


    @Override
    public Page<SubmodelElement> getSubmodelElements(Reference reference, QueryModifier modifier) throws ResourceNotFoundException, ResourceNotAContainerElementException {
        return Persistence.super.getSubmodelElements(reference, modifier);
    }


    @Override
    public Page<AssetAdministrationShell> getAllAssetAdministrationShells(QueryModifier modifier, PagingInfo paging) {
        return Persistence.super.getAllAssetAdministrationShells(modifier, paging);
    }


    @Override
    public Page<Submodel> getAllSubmodels(QueryModifier modifier, PagingInfo paging) {
        return Persistence.super.getAllSubmodels(modifier, paging);
    }


    @Override
    public Page<ConceptDescription> getAllConceptDescriptions(QueryModifier modifier, PagingInfo paging) {
        return Persistence.super.getAllConceptDescriptions(modifier, paging);
    }


    @Override
    public Page<SubmodelElement> getAllSubmodelElements(QueryModifier modifier, PagingInfo paging) throws ResourceNotFoundException {
        return Persistence.super.getAllSubmodelElements(modifier, paging);
    }


    private Bson getIdShortFilter(String idShort) {
        return Filters.eq("idShort", idShort);
    }


    private Bson getSemanticIdFilter(Reference semanticId) {
        if (Objects.isNull(semanticId))
            return NO_FILTER;
        return Filters.eq("semanticId", getReferenceDocument(semanticId));
    }


    private Bson getIsCaseOfFilter(Reference isCaseOf) {
        if (Objects.isNull(isCaseOf))
            return NO_FILTER;
        return Filters.eq("isCaseOf", getReferenceDocument(isCaseOf)); // TODO better equals implementation
    }


    private Bson getDataSpecificationFilter(Reference dataSpecification) {
        if (Objects.isNull(dataSpecification))
            return NO_FILTER;
        return Filters.eq("embeddedDataSpecifications", getReferenceDocument(dataSpecification));
    }


    private Document getReferenceDocument(Reference reference) {
        try {
            //Ref type has to match the one in the database exactly, ReferenceBuilder sets the wrong one
            reference.setType(ReferenceTypes.EXTERNAL_REFERENCE);
            String refJson = jsonApiSerializer.write(reference);
            return Document.parse(refJson);
        }
        catch (SerializationException e) {
            throw new RuntimeException(e); // TODO
        }
    }


    private <T extends Identifiable> Stream<T> getResultStream(MongoIterable<Document> documents, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Document document: documents) {
            try {
                result.add(jsonApiDeserializer.read(document.toJson(), type));
            }
            catch (DeserializationException e) {
                throw new RuntimeException(e); // TODO
            }
        }
        return result.stream();
    }


    private static <T extends Referable> T prepareResult(T result, QueryModifier modifier) {
        return QueryModifierHelper.applyQueryModifier(
                DeepCopyHelper.deepCopy(result),
                modifier);
    }
}
