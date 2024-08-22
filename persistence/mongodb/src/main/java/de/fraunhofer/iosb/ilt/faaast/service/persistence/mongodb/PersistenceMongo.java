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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.internal.client.model.FindOptions;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
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
    private static final String SERIALIZATION_ERROR = "Serialization of document with id %s failed!";
    private static final String DESERIALIZATION_ERROR = "Deserialization of document with id %s failed!";

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PersistenceMongo.class);
    private static final String MSG_RESOURCE_NOT_FOUND_BY_ID = "resource not found (id %s)";
    private static final String MSG_MODIFIER_NOT_NULL = "modifier must be non-null";
    private static final String MSG_CRITERIA_NOT_NULL = "criteria must be non-null";
    private static final String MSG_PAGING_NOT_NULL = "paging must be non-null";
    private static final Bson NO_FILTER = Filters.exists("_id");

    private static final String AAS_COLLECTION_NAME = "assetAdministrationShells";
    private static final String CD_COLLECTION_NAME = "contentDescriptions";
    private static final String SUBMODEL_COLLECTION_NAME = "submodels";
    private static final String OPERATION_COLLECTION_NAME = "operationResults";

    private PersistenceMongoConfig config;
    private MongoCollection<Document> aasCollection;
    private MongoCollection<Document> cdCollection;
    private MongoCollection<Document> submodelCollection;
    private MongoCollection<Document> operationCollection;
    private final JsonApiSerializer jsonApiSerializer = new JsonApiSerializer();
    private final JsonApiDeserializer jsonApiDeserializer = new JsonApiDeserializer();

    @Override
    public void init(CoreConfig coreConfig, PersistenceMongoConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;

        if (config.getEmbedded()) {
            Transitions transitions = Mongod.instance().transitions(Version.Main.PRODUCTION);
            TransitionWalker.ReachedState<RunningMongodProcess> runningProcess = transitions.walker()
                    .initState(StateID.of(RunningMongodProcess.class));
            config.setConnectionString("mongodb://" + runningProcess.current().getServerAddress().toString());
        }

        MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(config.getConnectionString()))
                        .build());
        MongoDatabase database = mongoClient.getDatabase(config.getDatabaseName());

        aasCollection = database.getCollection(AAS_COLLECTION_NAME);
        cdCollection = database.getCollection(CD_COLLECTION_NAME);
        submodelCollection = database.getCollection(SUBMODEL_COLLECTION_NAME);
        operationCollection = database.getCollection(OPERATION_COLLECTION_NAME);

        if (config.isOverride() || !databaseHasSavedEnvironment(database)) {
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


    private boolean databaseHasSavedEnvironment(MongoDatabase database) {
        List<String> collectionNames = new ArrayList<>();
        database.listCollectionNames().into(collectionNames);
        return collectionNames.contains(AAS_COLLECTION_NAME)
                || collectionNames.contains(SUBMODEL_COLLECTION_NAME)
                || collectionNames.contains(CD_COLLECTION_NAME)
                || collectionNames.contains(OPERATION_COLLECTION_NAME);
    }


    @Override
    public PersistenceMongoConfig asConfig() {
        return config;
    }


    private void saveEnvironment(Environment environment) {
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
        List<Document> documentList = list.stream()
                .map(this::getDocument)
                .collect(Collectors.toList());
        InsertManyResult result = collection.insertMany(documentList);
        return result.wasAcknowledged();
    }


    private <T extends Identifiable> T loadElementById(MongoCollection<Document> collection, String id, Class<T> type) throws ResourceNotFoundException {
        Bson filter = Filters.eq(ID_KEY, id);
        Document resultDocument = collection.find(filter).first();
        if (resultDocument == null)
            throw new ResourceNotFoundException(String.format(MSG_RESOURCE_NOT_FOUND_BY_ID, id));

        try {
            return jsonApiDeserializer.read(resultDocument.toJson(), type);
        }
        catch (DeserializationException e) {
            throw new IllegalStateException(e);
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
    public Page<Reference> getSubmodelRefs(String aasId, PagingInfo paging) throws ResourceNotFoundException {
        return preparePagedResult(
                getAssetAdministrationShell(aasId, QueryModifier.MINIMAL).getSubmodels().stream(), paging);
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
            Document operationDocument = operationCollection.find(filter).first();
            if (Objects.isNull(operationDocument))
                throw new ResourceNotFoundException(handle.getHandleId());
            return jsonApiDeserializer.read(((Document) operationDocument.get("result")).toJson(), OperationResult.class);
        }
        catch (SerializationException | DeserializationException e) {
            throw new IllegalStateException(e);
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

        FindOptions options = getPagingOptions(paging);
        return preparePagedResult(getResultStream(
                aasCollection.find(filter).skip(options.getSkip()).limit(options.getLimit()),
                AssetAdministrationShell.class), modifier, paging);
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

        FindOptions options = getPagingOptions(paging);
        return preparePagedResult(getResultStream(submodelCollection.find(filter)
                .skip(options.getSkip()).limit(options.getLimit()), Submodel.class), modifier, paging);
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

        result = applyPagingToStream(result, paging);
        return preparePagedResult(result, modifier, paging);
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

        FindOptions options = getPagingOptions(paging);
        return preparePagedResult(getResultStream(cdCollection.find(filter)
                .skip(options.getSkip()).limit(options.getLimit()), ConceptDescription.class), modifier, paging);
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
        Reference parentRef = parentIdentifier.toReference();
        Reference submodelElementRef = addSubmodelElementToReference(parentRef, submodelElement);

        if (!Objects.isNull(submodelCollection.find(getFilterForSubmodelOfReferenceKeys(getKeyValuesFromReference(submodelElementRef))).first())) {
            updateViaReference(submodelElementRef, submodelElement);
            return;
        }

        List<String> keys = getKeyValuesFromReference(parentRef);
        UpdateResult result;
        Bson filter = getFilterForSubmodelOfReferenceKeys(keys);
        if (keys.size() == 1) {
            result = submodelCollection.updateOne(filter, Updates.push(SUBMODEL_ELEMENTS_KEY, getDocument(submodelElement)));
        }
        else {
            List<Bson> arrayFilters = new ArrayList<>();
            String fieldName = getArrayFieldNameWithArrayFilters(keys, arrayFilters, keys.size());
            Object valueObject = loadDocumentFromReference(parentRef).get(VALUE_KEY);
            if (Objects.isNull(valueObject) || !List.class.isAssignableFrom(valueObject.getClass())) {
                throw new ResourceNotAContainerElementException(String.format("illegal type for parent. Must be one of: %s, %s, %s",
                        Submodel.class,
                        SubmodelElementCollection.class,
                        SubmodelElementList.class));
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
        List<String> keys = getKeyValuesFromReference(reference);
        Bson filter = getFilterForSubmodelOfReferenceKeys(keys);
        if (keys.size() == 2) {
            List<Bson> arrayFilters = new ArrayList<>();
            arrayFilters.add(Filters.eq("i." + ID_SHORT_KEY, keys.get(1)));
            result = submodelCollection.updateOne(filter, Updates.set(SUBMODEL_ELEMENTS_KEY + ".$[i]", getDocument(submodelElement)),
                    new UpdateOptions().arrayFilters(arrayFilters));
        }
        else {
            String fieldName = SUBMODEL_ELEMENTS_KEY;
            List<Bson> arrayFilters = new ArrayList<>();
            for (int i = 1; i < keys.size() - 1; i++) {
                fieldName += String.format(".$[a%d].%s", i, VALUE_KEY);
                arrayFilters.add(Filters.eq(String.format("a%d.%s", i, ID_SHORT_KEY), keys.get(i)));
            }
            fieldName += ".$[i]";
            arrayFilters.add(Filters.eq("i." + ID_SHORT_KEY, keys.get(keys.size() - 1)));

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
            LOGGER.error(String.format(SERIALIZATION_ERROR, handle.getHandleId()));
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
        List<String> keys = getKeyValuesFromReference(reference);
        Bson filter = getFilterForSubmodelOfReferenceKeys(keys);

        if (keys.size() == 2) {
            result = submodelCollection.updateOne(filter,
                    Updates.pull(SUBMODEL_ELEMENTS_KEY, Filters.eq(ID_SHORT_KEY, keys.get(keys.size() - 1))));
        }
        else {
            List<Bson> arrayFilters = new ArrayList<>();
            String fieldName = getArrayFieldNameWithArrayFilters(keys, arrayFilters, keys.size() - 1);

            String lastKeyValue = keys.get(keys.size() - 1);
            if (isKeyAnIndex(lastKeyValue)) {
                // no normal possibility for removing elements by index
                // fix by removing the value of the element to delete and then pulling all elements with null value
                submodelCollection.updateOne(filter, Updates.unset(fieldName + "." + lastKeyValue),
                        new UpdateOptions().arrayFilters(arrayFilters));
                result = submodelCollection.updateOne(filter, Updates.pull(fieldName, null),
                        new UpdateOptions().arrayFilters(arrayFilters));
            }
            else {
                result = submodelCollection.updateOne(filter, Updates.pull(fieldName, Filters.eq(ID_SHORT_KEY, keys.get(keys.size() - 1))),
                        new UpdateOptions().arrayFilters(arrayFilters));
            }

        }
        if (result.getModifiedCount() == 0)
            throw new ResourceNotFoundException(reference);
    }


    private String getArrayFieldNameWithArrayFilters(List<String> keys, List<Bson> arrayFilters, int count) {
        String fieldName = SUBMODEL_ELEMENTS_KEY;
        for (int i = 1; i < count; i++) {
            if (isKeyAnIndex(keys.get(i))) {
                fieldName += "." + keys.get(i) + "." + VALUE_KEY;
            }
            else {
                fieldName += String.format(".$[a%d].%s", i, VALUE_KEY);
                arrayFilters.add(Filters.eq(String.format("a%d.%s", i, ID_SHORT_KEY), keys.get(i)));
            }
        }
        return fieldName;
    }


    private Bson getIdShortFilter(String idShort) {
        return Filters.eq(ID_SHORT_KEY, idShort);
    }


    private Bson getSemanticIdFilter(Reference semanticId) {
        if (Objects.isNull(semanticId))
            return NO_FILTER;
        return Filters.eq("semanticId", getReferenceAsDocument(semanticId));
    }


    private Bson getIsCaseOfFilter(Reference isCaseOf) {
        if (Objects.isNull(isCaseOf))
            return NO_FILTER;
        return Filters.eq("isCaseOf", getReferenceAsDocument(isCaseOf)); // TODO better equals implementation
    }


    private Bson getDataSpecificationFilter(Reference dataSpecification) {
        if (Objects.isNull(dataSpecification))
            return NO_FILTER;
        return Filters.eq("embeddedDataSpecifications", getReferenceAsDocument(dataSpecification));
    }


    private Bson getAssetIdsFilter(List<AssetIdentification> assetIds) {
        if (assetIds == null)
            return NO_FILTER;

        List<String> globalAssetIdentificators = new ArrayList<>();
        List<SpecificAssetId> specificAssetIdentificators = new ArrayList<>();
        PersistenceHelper.splitAssetIdsIntoGlobalAndSpecificIds(assetIds, globalAssetIdentificators, specificAssetIdentificators);

        Bson filter = NO_FILTER;
        if (!globalAssetIdentificators.isEmpty()) {
            filter = Filters.and(filter, Filters.in("assetInformation.globalAssetId", globalAssetIdentificators.toArray()));
        }
        if (!specificAssetIdentificators.isEmpty()) {
            Bson specificAssetIdFilter = NO_FILTER;
            for (int i = 0; i < specificAssetIdentificators.size(); i++) {
                specificAssetIdFilter = Filters.or(
                        specificAssetIdFilter,
                        Filters.and(
                                Filters.eq("assetInformation.specificAssetIds.name", specificAssetIdentificators.get(i).getName()),
                                Filters.eq("assetInformation.specificAssetIds.value", specificAssetIdentificators.get(i).getValue())));
            }
            filter = Filters.and(filter, specificAssetIdFilter);
        }
        return filter;
    }


    private Document getReferenceAsDocument(Reference reference) {
        try {
            //Reference type has to match the one in the database exactly, ReferenceBuilder sets the wrong one
            reference.setType(ReferenceTypes.EXTERNAL_REFERENCE);
            String refJson = jsonApiSerializer.write(reference);
            return Document.parse(refJson);
        }
        catch (SerializationException e) {
            throw new IllegalStateException(e);
        }
    }


    private <T extends Identifiable> Stream<T> getResultStream(MongoIterable<Document> documents, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Document document: documents) {
            try {
                result.add(jsonApiDeserializer.read(document.toJson(), type));
            }
            catch (DeserializationException e) {
                LOGGER.error(String.format(DESERIALIZATION_ERROR, document.get("id")));
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
        try {
            return jsonApiDeserializer.read(result.toJson(), returnType);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Gets the right document to a reference from the database.
     * Behaviour should be similar to EnvironmentHelper.resolve().
     * Can handle indizes as keys.
     * Works by splitting arrays of submodelelements into different documents
     * and then selecting the right one.
     *
     * @param reference to be resolved.
     * @return the document that is the right submodelelement.
     * @throws ResourceNotFoundException if the referenced submodel does not exist in the database.
     */
    private Document loadDocumentFromReference(Reference reference) throws ResourceNotFoundException {
        List<Bson> pipelineStages = new ArrayList<>();
        List<String> keys = getKeyValuesFromReference(reference);

        // Filter for the right submodel
        pipelineStages.add(Aggregates.match(Filters.eq(ID_KEY, keys.get(0))));

        if (reference.getKeys().size() == 1)
            return submodelCollection.aggregate(pipelineStages).first();
        else {
            // Filter for the right submodel element in the "submodelElements" array of the right submodel
            pipelineStages.add(Aggregates.unwind("$" + SUBMODEL_ELEMENTS_KEY));
            pipelineStages.add(Aggregates.match(Filters.eq(SUBMODEL_ELEMENTS_KEY + "." + ID_SHORT_KEY, keys.get(1))));

            String currentFieldName = SUBMODEL_ELEMENTS_KEY;
            for (int i = 2; i < keys.size(); i++) {
                // Filter for the right submodel element in the "value" array of the parent submodel element
                currentFieldName += "." + VALUE_KEY;
                pipelineStages.add(Aggregates.unwind("$" + currentFieldName));
                if (isKeyAnIndex(keys.get(i))) {
                    pipelineStages.add(Aggregates.skip(Integer.parseInt(keys.get(i))));
                    pipelineStages.add(Aggregates.limit(1));
                }
                else {
                    pipelineStages.add(Aggregates.match(Filters.eq(currentFieldName + "." + ID_SHORT_KEY, keys.get(i))));
                }
            }

            try {
                Document nestedResult = submodelCollection.aggregate(pipelineStages).first().get(SUBMODEL_ELEMENTS_KEY, Document.class);
                for (int i = 2; i < keys.size(); i++) {
                    nestedResult = nestedResult.get(VALUE_KEY, Document.class);
                }
                return nestedResult;
            }
            catch (Exception e) {
                throw new ResourceNotFoundException(reference);
            }
        }
    }


    private Bson getFilterForSubmodelOfReferenceKeys(List<String> keys) {
        Bson filter = Filters.eq(ID_KEY, keys.get(0));
        if (keys.size() > 1) {
            String currentPropertyKey = SUBMODEL_ELEMENTS_KEY;
            for (int i = 1; i < keys.size(); i++) {
                if (isKeyAnIndex(keys.get(i))) {
                    filter = Filters.and(filter, Filters.exists(currentPropertyKey + "." + VALUE_KEY + "." + keys.get(i)));
                }
                else {
                    filter = Filters.and(filter, Filters.eq(currentPropertyKey + "." + ID_SHORT_KEY, keys.get(i)));
                }
                currentPropertyKey += "." + VALUE_KEY;
            }
        }

        return filter;
    }


    private List<String> getKeyValuesFromReference(Reference reference) throws ResourceNotFoundException {
        return reference.getKeys().stream()
                .map(key -> key.getValue())
                .collect(Collectors.toList());
    }


    private boolean isKeyAnIndex(String keyValue) {
        return Pattern.compile("\\d+").matcher(keyValue).matches();
    }


    FindOptions getPagingOptions(PagingInfo paging) {
        FindOptions options = new FindOptions();
        if (Objects.nonNull(paging.getCursor())) {
            options.skip(readCursor(paging.getCursor()));
        }
        if (paging.hasLimit()) {
            options.limit((int) paging.getLimit() + 1);
        }
        return options;
    }


    private <T extends Referable> Stream<T> applyPagingToStream(Stream<T> input, PagingInfo paging) {
        Stream result = input;
        if (Objects.nonNull(paging.getCursor())) {
            result = result.skip(readCursor(paging.getCursor()));
        }
        if (paging.hasLimit()) {
            result = result.limit(paging.getLimit() + 1);
        }
        return result;
    }


    private static <T extends Referable> Page<T> preparePagedResult(Stream<T> input, QueryModifier modifier, PagingInfo paging) {
        List<T> temp = input.collect(Collectors.toList());
        return Page.<T> builder()
                .result(QueryModifierHelper.applyQueryModifier(
                        temp.stream()
                                .limit(paging.hasLimit() ? paging.getLimit() : temp.size())
                                .map(DeepCopyHelper::deepCopy)
                                .collect(Collectors.toList()),
                        modifier))
                .metadata(PagingMetadata.builder()
                        .cursor(nextCursor(paging, temp.size()))
                        .build())
                .build();
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
            return writeCursor((int) paging.getLimit());
        }
        return writeCursor(readCursor(paging.getCursor()) + (int) paging.getLimit());
    }


    private static int readCursor(String cursor) {
        return Integer.parseInt(cursor);
    }


    private static String writeCursor(int index) {
        return Long.toString(index);
    }
}
