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
import com.mongodb.client.*;
import com.mongodb.client.model.*;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AssetAdministrationShellSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.ConceptDescriptionSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelElementSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.util.PersistenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.util.QueryModifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
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
    private static final String OPERATION_COLLECTION_NAME = "opCol";

    private PersistenceMongoConfig config;
    private MongoCollection<Document> aasCollection, cdCollection, submodelCollection, operationCollection;
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
        operationCollection = database.getCollection(OPERATION_COLLECTION_NAME);

        if (!config.isUseExisting()) {
            aasCollection.drop();
            cdCollection.drop();
            submodelCollection.drop();
            operationCollection.drop();

            try {
                saveEnvironment(config.loadInitialModel());
            }
            catch (DeserializationException | InvalidConfigurationException e) {
                throw new ConfigurationInitializationException(e);
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


    private Document getDocument(Referable referable) {
        try {
            String json = jsonApiSerializer.write(referable);
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
        return prepareResult(
                resolveReference(identifier.toReference(), SubmodelElement.class),
                modifier);
    }


    @Override
    public OperationResult getOperationResult(OperationHandle handle) throws ResourceNotFoundException {
        try {
            Document handleDocument = Document.parse(jsonApiSerializer.write(handle));
            Bson filter = Filters.eq("handle", handleDocument);
            return jsonApiDeserializer.read(operationCollection.find(filter).first().toJson(), OperationResult.class);
        }
        catch (SerializationException | DeserializationException e) {
            throw new RuntimeException(e); // TODO
        }

    }


    @Override
    public Page<AssetAdministrationShell> findAssetAdministrationShells(AssetAdministrationShellSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) {
        Ensure.requireNonNull(criteria, MSG_CRITERIA_NOT_NULL);
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(paging, MSG_PAGING_NOT_NULL);

        Bson filter = NO_FILTER;
        if (criteria.isIdShortSet())
            filter = Filters.and(filter, getIdShortFilter(criteria.getIdShort()));
        if (criteria.isAssetIdsSet())
            filter = Filters.and(filter, getAssetIdsFilter(criteria.getAssetIds()));
        return PersistenceHelper.preparePagedResult(getResultStream(aasCollection.find(filter), AssetAdministrationShell.class), modifier, paging);
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

        return PersistenceHelper.preparePagedResult(getResultStream(submodelCollection.find(filter), Submodel.class), modifier, paging);
    }


    @Override
    public Page<SubmodelElement> findSubmodelElements(SubmodelElementSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws ResourceNotFoundException {
        Ensure.requireNonNull(criteria, MSG_CRITERIA_NOT_NULL);
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(paging, MSG_PAGING_NOT_NULL);
        final Collection<SubmodelElement> elements = new ArrayList<>();
        if (criteria.isParentSet()) {
            Referable parent = resolveReference(criteria.getParent().toReference(), Referable.class);

            PersistenceHelper.addSubmodelElementsFromParentToCollection(parent, elements);
        }
        Stream<SubmodelElement> result = elements.stream();
        if (criteria.isSemanticIdSet()) {
            result = PersistenceHelper.filterBySemanticId(result, criteria.getSemanticId());
        }
        return PersistenceHelper.preparePagedResult(result, modifier, paging);
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

        return PersistenceHelper.preparePagedResult(getResultStream(cdCollection.find(filter), ConceptDescription.class), modifier, paging);
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


    // TODO ersetzen bei gleicher idShort
    @Override
    public void insert(SubmodelElementIdentifier parentIdentifier, SubmodelElement submodelElement) throws ResourceNotFoundException, ResourceNotAContainerElementException {
        Reference parentRef = parentIdentifier.toReference();
        Bson filter = getFilterForSubmodelOfReference(parentRef);
        if (parentRef.getKeys().size() == 1) {
            submodelCollection.updateOne(filter, Updates.push("submodelElements", getDocument(submodelElement)));
        }
        else {
            String fieldName = "submodelElements";
            List<Bson> arrayFilters = new ArrayList<>();
            for (int i = 1; i < parentRef.getKeys().size(); i++) {
                fieldName += String.format(".$[a%d].value", i);
                arrayFilters.add(Filters.eq(String.format("a%d.idShort", i), parentRef.getKeys().get(i).getValue()));
            }
            submodelCollection.updateOne(filter, Updates.push(fieldName, getDocument(submodelElement)),
                    new UpdateOptions().arrayFilters(arrayFilters));
        }
    }


    @Override
    public void update(SubmodelElementIdentifier identifier, SubmodelElement submodelElement) throws ResourceNotFoundException {
        Reference reference = identifier.toReference();
        Bson filter = getFilterForSubmodelOfReference(reference);
        if (reference.getKeys().size() == 2) {
            List<Bson> arrayFilters = new ArrayList<>();
            arrayFilters.add(Filters.eq("i.idShort", reference.getKeys().get(1)));
            submodelCollection.updateOne(filter, Updates.set("submodelElements.$[i]", getDocument(submodelElement)), new UpdateOptions().arrayFilters(arrayFilters));
        }
        else {
            // TODO for nested elements
            /*String fieldName = "submodelElements";
            List<Bson> arrayFilters = new ArrayList<>();
            for (int i = 1; i < parentRef.getKeys().size(); i++) {
                fieldName += String.format(".$[a%d].value", i);
                arrayFilters.add(Filters.eq(String.format("a%d.idShort", i), parentRef.getKeys().get(i).getValue()));
            }
            submodelCollection.updateOne(filter, Updates.push(fieldName, getDocument(submodelElement)),
                    new UpdateOptions().arrayFilters(arrayFilters));*/
        }
    }


    @Override
    public void save(OperationHandle handle, OperationResult result) {
        Document document = new Document();
        try {
            Document handleDocument = Document.parse(jsonApiSerializer.write(handle));
            Document resultDocument = Document.parse(jsonApiSerializer.write(result));
            document.append("handle", handleDocument).append("result", resultDocument);
        }
        catch (SerializationException e) {
            throw new RuntimeException(e); // TODO
        }
        operationCollection.insertOne(document);
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


    private Bson getAssetIdsFilter(List<AssetIdentification> assetIds) {
        if (assetIds == null)
            return NO_FILTER;

        List<String> globalAssetIdentificators = new ArrayList<>();
        List<SpecificAssetID> specificAssetIdentificators = new ArrayList<>();
        PersistenceHelper.splitAssetIdsIntoGlobalAndSpecificIds(assetIds, globalAssetIdentificators, specificAssetIdentificators);

        Bson filter = NO_FILTER;
        if (!globalAssetIdentificators.isEmpty()) {
            filter = Filters.and(filter, Filters.in("assetInformation.globalAssetId", globalAssetIdentificators.toArray()));
        }
        if (!specificAssetIdentificators.isEmpty()) {
            filter = Filters.and(filter, Filters.in("assetInformation.specificAssetIds", specificAssetIdentificators.toArray()));
        }
        return filter;
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


    private <T extends Referable> T resolveReference(Reference reference, Class<T> returnType) throws ResourceNotFoundException {
        Document result = null;
        List<Bson> pipelineStages = new ArrayList<>();

        // Filter for the right submodel
        pipelineStages.add(Aggregates.match(Filters.eq("id", reference.getKeys().get(0).getValue())));

        if (reference.getKeys().size() == 1)
            result = submodelCollection.aggregate(pipelineStages).first();
        else {
            // Filter for the right submodel element in the "submodelElements" array of the right submodel
            pipelineStages.add(Aggregates.unwind("$submodelElements"));
            pipelineStages.add(Aggregates.match(Filters.eq("submodelElements.idShort", reference.getKeys().get(1).getValue())));

            String currentFieldName = "submodelElements";
            for (int i = 2; i < reference.getKeys().size(); i++) {
                // Filter for the right submodel element in the "value" array of the parent submodel element
                currentFieldName += ".value";
                pipelineStages.add(Aggregates.unwind("$" + currentFieldName));
                pipelineStages.add(Aggregates.match(Filters.eq(currentFieldName + ".idShort", reference.getKeys().get(2).getValue())));
            }

            try {
                Document nestedResult = submodelCollection.aggregate(pipelineStages).first().get("submodelElements", Document.class);
                for (int i = 2; i < reference.getKeys().size(); i++) {
                    nestedResult = nestedResult.get("value", Document.class);
                }
                result = nestedResult;
            }
            catch (Exception e) {
                throw new ResourceNotFoundException(reference);
            }
        }

        if (Objects.isNull(result))
            throw new ResourceNotFoundException(reference);
        Referable parent = null;
        try {
            return jsonApiDeserializer.read(result.toJson(), returnType);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Bson getFilterForSubmodelOfReference(Reference reference) {
        Bson filter = Filters.eq("id", reference.getKeys().get(0).getValue());
        if (reference.getKeys().size() > 1) {
            String currentPropertyKey = "submodelElements";
            filter = Filters.and(filter, Filters.eq(currentPropertyKey + ".idShort", reference.getKeys().get(1).getValue()));
            for (int i = 2; i < reference.getKeys().size(); i++) {
                currentPropertyKey += ".value";
                filter = Filters.and(filter, Filters.eq(currentPropertyKey + ".idShort", reference.getKeys().get(i).getValue()));
            }
        }

        return filter;
    }
}
