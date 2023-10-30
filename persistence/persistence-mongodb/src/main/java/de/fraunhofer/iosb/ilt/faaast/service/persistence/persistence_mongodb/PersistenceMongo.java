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
import com.mongodb.client.result.UpdateResult;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.slf4j.LoggerFactory;


/**
 * Persistence implementation for a mongo database.
 */
public class PersistenceMongo implements Persistence<PersistenceMongoConfig> {
    private static final String ID_KEY = "id";
    private static final String ID_SHORT_KEY = "idShort";
    private static final String SUBMODEL_ELEMENTS_KEY = "submodelElements";
    private static final String VALUE_KEY = "value";

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
        Bson filter = Filters.eq(ID_KEY, id);
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
        Bson filter = Filters.eq(ID_KEY, id);
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
        collection.replaceOne(Filters.eq(ID_KEY, element.getId()),
                getDocument(element),
                new ReplaceOptions().upsert(true));
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
            OperationResult result = jsonApiDeserializer.read(operationCollection.find(filter).first().toJson(), OperationResult.class);
            return result;
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


    @Override
    public void  insert(SubmodelElementIdentifier parentIdentifier, SubmodelElement submodelElement)throws ResourceNotFoundException, ResourceNotAContainerElementException {
        Reference parentRef = parentIdentifier.toReference();
        Reference submodelElementRef = addSubmodelElementToReference(parentRef, submodelElement);

        if (!Objects.isNull(submodelCollection.find(getFilterForSubmodelOfReferenceKeys(getReferenceKeysWithoutIndices(submodelElementRef))).first())) {
            updateViaReference(submodelElementRef, submodelElement);
            return;
        }

        List<Key> keys = getReferenceKeysWithoutIndices(parentRef);
        UpdateResult result = null;
        Bson filter = getFilterForSubmodelOfReferenceKeys(keys);
        if (keys.size() == 1) {
            result = submodelCollection.updateOne(filter, Updates.push(SUBMODEL_ELEMENTS_KEY, getDocument(submodelElement)));
        }
        else {
            String fieldName = SUBMODEL_ELEMENTS_KEY;
            List<Bson> arrayFilters = new ArrayList<>();
            for (int i = 1; i < keys.size(); i++) {
                fieldName += String.format(".$[a%d].%s", i, VALUE_KEY);
                arrayFilters.add(Filters.eq(String.format("a%d.%s", i, ID_SHORT_KEY), keys.get(i).getValue()));
            }
            result = submodelCollection.updateOne(filter, Updates.push(fieldName, getDocument(submodelElement)),
                    new UpdateOptions().arrayFilters(arrayFilters));
        }
        if (result.getModifiedCount() == 0)
            throw new ResourceNotFoundException(parentRef);
    }

    private Reference addSubmodelElementToReference(Reference reference, SubmodelElement submodelElement) {
        Key submodelKey = new DefaultKey();
        submodelKey.setValue(submodelElement.getIdShort());
        List<Key> keyList = new ArrayList<>(reference.getKeys());
        keyList.add(submodelKey);
        Reference result = new DefaultReference();
        result.setKeys(keyList);
        return result;
    }


    @Override
    public void update(SubmodelElementIdentifier identifier, SubmodelElement submodelElement) throws ResourceNotFoundException {
        updateViaReference(identifier.toReference(), submodelElement);
    }

    private void updateViaReference(Reference reference, SubmodelElement submodelElement) throws ResourceNotFoundException {
        UpdateResult result = null;
        List<Key> keys = getReferenceKeysWithoutIndices(reference);
        Bson filter = getFilterForSubmodelOfReferenceKeys(keys);
        if (keys.size() == 2) {
            List<Bson> arrayFilters = new ArrayList<>();
            arrayFilters.add(Filters.eq("i." + ID_SHORT_KEY, keys.get(1).getValue()));
            result = submodelCollection.updateOne(filter, Updates.set(SUBMODEL_ELEMENTS_KEY + ".$[i]", getDocument(submodelElement)),
                    new UpdateOptions().arrayFilters(arrayFilters));
        }
        else {
            String fieldName = SUBMODEL_ELEMENTS_KEY;
            List<Bson> arrayFilters = new ArrayList<>();
            for (int i = 1; i < keys.size() - 1; i++) {
                fieldName += String.format(".$[a%d].%s", i, VALUE_KEY);
                arrayFilters.add(Filters.eq(String.format("a%d.%s", i, ID_SHORT_KEY), keys.get(i).getValue()));
            }
            fieldName += ".$[i]";
            arrayFilters.add(Filters.eq("i." + ID_SHORT_KEY, keys.get(keys.size()-1).getValue()));

            result = submodelCollection.updateOne(filter, Updates.set(fieldName, getDocument(submodelElement)),
                    new UpdateOptions().arrayFilters(arrayFilters));
        }
        if (result.getModifiedCount() == 0)
            throw new ResourceNotFoundException(reference);
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
        operationCollection.replaceOne(Filters.eq("handle", document.get("handle")),
                document,
                new ReplaceOptions().upsert(true));
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
        Bson filter = Filters.eq("submodels.id", id);
        Bson update = Updates.pull("submodels", filter);
        aasCollection.updateMany(filter, update);
    }


    @Override
    public void deleteConceptDescription(String id) throws ResourceNotFoundException {
        deleteElementById(cdCollection, id);
    }


    @Override
    public void deleteSubmodelElement(SubmodelElementIdentifier identifier) throws ResourceNotFoundException {
        Reference reference = identifier.toReference();
        UpdateResult result = null;
        List<Key> keys = getReferenceKeysWithoutIndices(reference);
        Bson filter = getFilterForSubmodelOfReferenceKeys(keys);
        Bson pullFilter =  Filters.eq(ID_SHORT_KEY, keys.get(keys.size()-1).getValue());

        if (keys.size() == 2) {
            result = submodelCollection.updateOne(filter,
                    Updates.pull(SUBMODEL_ELEMENTS_KEY, pullFilter));
        }
        else {
            String fieldName = SUBMODEL_ELEMENTS_KEY;
            List<Bson> arrayFilters = new ArrayList<>();
            for (int i = 1; i < keys.size() - 1; i++) {
                fieldName += String.format(".$[a%d].%s", i, VALUE_KEY);
                arrayFilters.add(Filters.eq(String.format("a%d.%s", i, ID_SHORT_KEY), keys.get(i).getValue()));
            }
            Document doc = submodelCollection.find(filter).first();
            result = submodelCollection.updateOne(filter, Updates.pull(fieldName, pullFilter),
                    new UpdateOptions().arrayFilters(arrayFilters));
        }
        if (result.getModifiedCount() == 0)
            throw new ResourceNotFoundException(reference);
    }


    private Bson getIdShortFilter(String idShort) {
        return Filters.eq(ID_SHORT_KEY, idShort);
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
        Document result = loadDocumentFromReference(reference);

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

    private Document loadDocumentFromReference(Reference reference) throws ResourceNotFoundException {
        List<Bson> pipelineStages = new ArrayList<>();
        List<Key> keys = reference.getKeys();

        // Filter for the right submodel
        pipelineStages.add(Aggregates.match(Filters.eq(ID_KEY, keys.get(0).getValue())));

        if (reference.getKeys().size() == 1)
            return submodelCollection.aggregate(pipelineStages).first();
        else {
            // Filter for the right submodel element in the "submodelElements" array of the right submodel
            pipelineStages.add(Aggregates.unwind("$" + SUBMODEL_ELEMENTS_KEY));
            pipelineStages.add(Aggregates.match(Filters.eq(SUBMODEL_ELEMENTS_KEY + "." + ID_SHORT_KEY, keys.get(1).getValue())));

            String currentFieldName = SUBMODEL_ELEMENTS_KEY;
            for (int i = 2; i < keys.size(); i++) {
                // Filter for the right submodel element in the "value" array of the parent submodel element
                currentFieldName += "." + VALUE_KEY;
                pipelineStages.add(Aggregates.unwind("$" + currentFieldName));
                if (isIndexKey(keys.get(i).getValue())) {
                    pipelineStages.add(Aggregates.skip(Math.max(Integer.parseInt(keys.get(i).getValue()) - 1, 0)));
                    pipelineStages.add(Aggregates.limit(1));
                } else {
                    pipelineStages.add(Aggregates.match(Filters.eq(currentFieldName + "." + ID_SHORT_KEY, keys.get(i).getValue())));
                }
            }

            try {
                Document nestedResult = submodelCollection.aggregate(pipelineStages).first().get(SUBMODEL_ELEMENTS_KEY, Document.class);
                for (int i = 2; i < keys.size(); i++) {
                    nestedResult = nestedResult.get(VALUE_KEY, Document.class);
                }
                return nestedResult;
            } catch (Exception e) {
                throw new ResourceNotFoundException(reference);
            }
        }
    }

    private Bson getFilterForSubmodelOfReferenceKeys(List<Key> keys) {
        Bson filter = Filters.eq(ID_KEY, keys.get(0).getValue());
        if (keys.size() > 1) {
            String currentPropertyKey = SUBMODEL_ELEMENTS_KEY;
            filter = Filters.and(filter, Filters.eq(currentPropertyKey + "." + ID_SHORT_KEY, keys.get(1).getValue()));
            for (int i = 2; i < keys.size(); i++) {
                currentPropertyKey += "." + VALUE_KEY;
                filter = Filters.and(filter, Filters.eq(currentPropertyKey + "." + ID_SHORT_KEY, keys.get(i).getValue()));
            }
        }

        return filter;
    }

    private List<Key> getReferenceKeysWithoutIndices(Reference reference) throws ResourceNotFoundException {
        List<Key> result = new ArrayList<>();
        List<Key> keys = reference.getKeys();
        for (Key oldkey: keys) {
            Key newKey = new DefaultKey();
            newKey.setType(oldkey.getType());
            if (isIndexKey(oldkey.getValue())) {
                newKey.setValue((String) loadDocumentFromReference(reference).get(ID_SHORT_KEY));
            } else {
                newKey.setValue(oldkey.getValue());
            }
            result.add(newKey);
        }
        return result;
    }

    private boolean isIndexKey(String keyValue) {
        // Alternate: keys.get(i-1).getType() == KeyTypes.SUBMODEL_ELEMENT_LIST
        return Pattern.compile("\\d+").matcher(keyValue).matches();
    }
}
