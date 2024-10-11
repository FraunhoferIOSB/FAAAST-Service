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
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
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
import com.mongodb.client.result.UpdateResult;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceAlreadyExistsException;
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
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
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

    private static final Pattern INDEX_REGEX = Pattern.compile("\\d+");

    private final JsonApiSerializer serializer = new JsonApiSerializer();
    private final JsonApiDeserializer deserializer = new JsonApiDeserializer();
    private PersistenceMongoConfig config;
    private MongoClient client;
    private MongoCollection<Document> aasCollection;
    private MongoCollection<Document> cdCollection;
    private MongoCollection<Document> submodelCollection;
    private MongoCollection<Document> operationCollection;

    @Override
    public void init(CoreConfig coreConfig, PersistenceMongoConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
    }


    @Override
    public void start() throws PersistenceException {
        client = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(config.getConnectionString()))
                        .build());
        MongoDatabase database = client.getDatabase(config.getDatabase());

        aasCollection = database.getCollection(AAS_COLLECTION_NAME);
        cdCollection = database.getCollection(CD_COLLECTION_NAME);
        submodelCollection = database.getCollection(SUBMODEL_COLLECTION_NAME);
        operationCollection = database.getCollection(OPERATION_COLLECTION_NAME);

        if (config.isOverride()) {
            aasCollection.drop();
            cdCollection.drop();
            submodelCollection.drop();
            operationCollection.drop();
        }

        if (!databaseHasSavedEnvironment(database)) {
            aasCollection.drop();
            cdCollection.drop();
            submodelCollection.drop();
            operationCollection.drop();
            try {
                saveEnvironment(config.loadInitialModel());
            }
            catch (DeserializationException | InvalidConfigurationException | IllegalStateException e) {
                throw new PersistenceException(e);
            }
        }
    }


    @Override
    public void stop() {
        client.close();
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


    private void saveEnvironment(Environment environment) throws PersistenceException {
        save(environment.getAssetAdministrationShells(), aasCollection);
        save(environment.getSubmodels(), submodelCollection);
        save(environment.getConceptDescriptions(), cdCollection);
    }


    private void save(List<? extends Identifiable> list, MongoCollection<Document> collection) throws PersistenceException {
        if (list.isEmpty())
            return;
        try {
            collection
                    .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                    .insertMany(list.stream()
                            .filter(Objects::nonNull)
                            .map(LambdaExceptionHelper.rethrowFunction(this::asDocument))
                            .toList());
        }
        catch (MongoException | IllegalArgumentException e) {
            throw new PersistenceException("Error saving data in MongoDB", e);
        }
    }


    private <T extends Identifiable> T fetch(MongoCollection<Document> collection, String id, Class<T> type) throws ResourceNotFoundException, PersistenceException {
        Bson filter = Filters.eq(ID_KEY, id);
        Document document = collection.find(filter).first();
        if (Objects.isNull(document)) {
            throw new ResourceNotFoundException(String.format(MSG_RESOURCE_NOT_FOUND_BY_ID, id));
        }
        return fromDocument(document, type);
    }


    private Document asDocument(Referable referable) throws PersistenceException {
        try {
            //TODO convert POJO directly to document if possible
            return Document.parse(serializer.write(referable));
        }
        catch (SerializationException e) {
            throw new PersistenceException(String.format("Error serializing referable to JSON (idShort: %s)", referable.getIdShort()), e);
        }
    }


    private <T> T fromDocument(Document document, Class<T> type) throws PersistenceException {
        try {
            //TODO convert bson directly to POJO if possible
            return deserializer.read(document.toJson(), type);
        }
        catch (DeserializationException e) {
            throw new PersistenceException(String.format("Error deserializing JSON from MongoDB (json: %s)", document.toJson()), e);
        }
    }


    private void deleteElementById(MongoCollection<Document> collection, String id) throws ResourceNotFoundException {
        Bson filter = Filters.eq(ID_KEY, id);
        DeleteResult result = collection.deleteOne(filter);
        if (result.getDeletedCount() == 0) {
            throw new ResourceNotFoundException(String.format(MSG_RESOURCE_NOT_FOUND_BY_ID, id));
        }
    }


    private void upsert(MongoCollection<Document> collection, Identifiable element) throws PersistenceException {
        collection.replaceOne(Filters.eq(ID_KEY, element.getId()),
                asDocument(element),
                new ReplaceOptions().upsert(true));
    }


    @Override
    public AssetAdministrationShell getAssetAdministrationShell(String id, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
        return prepareResult(
                fetch(aasCollection, id, AssetAdministrationShell.class),
                modifier);
    }


    @Override
    public Page<Reference> getSubmodelRefs(String aasId, PagingInfo paging) throws ResourceNotFoundException, PersistenceException {
        return preparePagedResult(
                getAssetAdministrationShell(aasId, QueryModifier.MINIMAL)
                        .getSubmodels()
                        .stream(),
                paging);
    }


    private static <T> Page<T> preparePagedResult(Stream<T> input, PagingInfo paging) throws PersistenceException {
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
    public Submodel getSubmodel(String id, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
        return prepareResult(
                fetch(submodelCollection, id, Submodel.class),
                modifier);
    }


    @Override
    public ConceptDescription getConceptDescription(String id, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
        return prepareResult(
                fetch(cdCollection, id, ConceptDescription.class),
                modifier);
    }


    @Override
    public SubmodelElement getSubmodelElement(SubmodelElementIdentifier identifier, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
        return prepareResult(
                fetch(identifier, SubmodelElement.class),
                modifier);
    }


    @Override
    public OperationResult getOperationResult(OperationHandle handle) throws ResourceNotFoundException, PersistenceException {
        try {
            Document handleDocument = Document.parse(serializer.write(handle));
            Bson filter = Filters.eq("handle", handleDocument);
            Document operationDocument = operationCollection.find(filter).first();
            if (Objects.isNull(operationDocument))
                throw new ResourceNotFoundException(handle.getHandleId());
            return deserializer.read(((Document) operationDocument.get("result")).toJson(), OperationResult.class);
        }
        catch (SerializationException | DeserializationException e) {
            throw new PersistenceException(e);
        }
    }


    @Override
    public Page<AssetAdministrationShell> findAssetAdministrationShells(AssetAdministrationShellSearchCriteria criteria, QueryModifier modifier, PagingInfo paging)
            throws PersistenceException {
        Ensure.requireNonNull(criteria, MSG_CRITERIA_NOT_NULL);
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(paging, MSG_PAGING_NOT_NULL);

        Bson filter = NO_FILTER;
        if (criteria.isIdShortSet())
            filter = Filters.and(filter, getIdShortFilter(criteria.getIdShort()));
        if (criteria.isAssetIdsSet())
            filter = Filters.and(filter, getAssetIdsFilter(criteria.getAssetIds()));
        return preparePagedResult(aasCollection, filter, paging, modifier, AssetAdministrationShell.class);
    }


    private <T extends Referable> Stream<T> asPojo(MongoIterable<Document> documents, Class<T> type) throws PersistenceException {
        return StreamSupport.stream(documents.spliterator(), false)
                .map(LambdaExceptionHelper.rethrowFunction(x -> fromDocument(x, type)));
    }


    private static <T> FindIterable<T> applyPaging(FindIterable<T> iterable, PagingInfo paging) {
        FindIterable<T> result = iterable;
        if (Objects.nonNull(paging.getCursor())) {
            result = result.skip(readCursor(paging.getCursor()));
        }
        if (paging.hasLimit()) {
            result = result.limit((int) paging.getLimit() + 1);
        }
        return result;
    }


    private <T extends Referable> Stream<T> applyPaging(Stream<T> input, PagingInfo paging) {
        Stream result = input;
        if (Objects.nonNull(paging.getCursor())) {
            result = result.skip(readCursor(paging.getCursor()));
        }
        if (paging.hasLimit()) {
            result = result.limit(paging.getLimit() + 1);
        }
        return result;
    }


    @Override
    public Page<Submodel> findSubmodels(SubmodelSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws PersistenceException {
        Ensure.requireNonNull(criteria, MSG_CRITERIA_NOT_NULL);
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(paging, MSG_PAGING_NOT_NULL);

        Bson filter = NO_FILTER;
        if (criteria.isIdShortSet())
            filter = Filters.and(filter, getIdShortFilter(criteria.getIdShort()));
        if (criteria.isSemanticIdSet())
            filter = Filters.and(filter, getSemanticIdFilter(criteria.getSemanticId()));
        return preparePagedResult(submodelCollection, filter, paging, modifier, Submodel.class);
    }


    @Override
    public Page<SubmodelElement> findSubmodelElements(SubmodelElementSearchCriteria criteria, QueryModifier modifier, PagingInfo paging)
            throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(criteria, MSG_CRITERIA_NOT_NULL);
        Ensure.requireNonNull(modifier, MSG_MODIFIER_NOT_NULL);
        Ensure.requireNonNull(paging, MSG_PAGING_NOT_NULL);
        final Collection<SubmodelElement> elements = new ArrayList<>();
        if (criteria.isParentSet()) {
            Referable parent = fetch(criteria.getParent(), Referable.class);

            PersistenceHelper.addSubmodelElementsFromParentToCollection(parent, elements);
        }
        Stream<SubmodelElement> result = elements.stream();
        if (criteria.isSemanticIdSet()) {
            // TODO refactor
            result = PersistenceHelper.filterBySemanticId(result, criteria.getSemanticId());
        }
        return preparePagedResult(result, modifier, paging);
    }


    @Override
    public Page<ConceptDescription> findConceptDescriptions(ConceptDescriptionSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws PersistenceException {
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

        return preparePagedResult(cdCollection, filter, paging, modifier, ConceptDescription.class);
    }


    @Override
    public void save(AssetAdministrationShell assetAdministrationShell) throws PersistenceException {
        upsert(aasCollection, assetAdministrationShell);
    }


    @Override
    public void save(ConceptDescription conceptDescription) throws PersistenceException {
        upsert(cdCollection, conceptDescription);
    }


    @Override
    public void save(Submodel submodel) throws PersistenceException {
        upsert(submodelCollection, submodel);
    }


    private static void ensureIdShortPresent(SubmodelElement submodelElement) {
        if (Objects.nonNull(submodelElement) && StringHelper.isBlank(submodelElement.getIdShort())) {
            throw new IllegalArgumentException("idShort most be non-empty");
        }
    }


    private void ensureDoesNotAlreadyExist(SubmodelElementIdentifier parentIdentifier, SubmodelElement submodelElement) throws ResourceAlreadyExistsException {
        Reference newElementReference = ReferenceBuilder.forParent(parentIdentifier.toReference(), submodelElement);
        if (submodelElementExists(newElementReference)) {
            throw new ResourceAlreadyExistsException(newElementReference);
        }
    }


    @Override
    public void insert(SubmodelElementIdentifier parentIdentifier, SubmodelElement submodelElement)
            throws ResourceNotFoundException, ResourceNotAContainerElementException, ResourceAlreadyExistsException, PersistenceException {
        Ensure.requireNonNull(parentIdentifier, "parent must be non-null");
        Ensure.requireNonNull(submodelElement, "submodelElement must be non-null");

        SubmodelElement parent = getSubmodelElement(parentIdentifier, QueryModifier.MINIMAL);
        if (!SubmodelElementCollection.class.isAssignableFrom(parent.getClass())
                && !SubmodelElementList.class.isAssignableFrom(parent.getClass())
                && !Entity.class.isAssignableFrom(parent.getClass())) {
            throw new ResourceNotAContainerElementException(String.format("illegal type for identifiable: %s. Must be one of: %s, %s, %s",
                    parent.getClass(),
                    Submodel.class,
                    SubmodelElementCollection.class,
                    SubmodelElementList.class));
        }
        if (SubmodelElementCollection.class.isAssignableFrom(parent.getClass())
                || Entity.class.isAssignableFrom(parent.getClass())) {
            ensureIdShortPresent(submodelElement);
            ensureDoesNotAlreadyExist(parentIdentifier, submodelElement);
        }

        MongoSubmodelElementPath filter = getFilter(parentIdentifier.getIdShortPath());
        filter.fieldname += String.format(".%s", VALUE_KEY);
        submodelCollection.updateOne(
                getFilterForSubmodel(parentIdentifier.getSubmodelId()),
                Updates.push(filter.fieldname, asDocument(submodelElement)),
                new UpdateOptions().arrayFilters(filter.arrayFilters));
    }


    @Override
    public void update(SubmodelElementIdentifier identifier, SubmodelElement submodelElement) throws ResourceNotFoundException, PersistenceException {
        UpdateResult result;
        SubmodelElementIdentifier parentIdentifier = SubmodelElementIdentifier.fromReference(ReferenceHelper.getParent(identifier.toReference()));
        if (parentIdentifier.getIdShortPath().isEmpty()) {
            MongoSubmodelElementPath filter = new MongoSubmodelElementPath();
            filter.arrayFilters.add(Filters.eq("i." + ID_SHORT_KEY, identifier.getIdShortPath().getElements().get(0)));
            result = submodelCollection.updateOne(
                    getFilterForSubmodel(identifier.getSubmodelId()),
                    Updates.set(SUBMODEL_ELEMENTS_KEY + ".$[i]", asDocument(submodelElement)),
                    new UpdateOptions().arrayFilters(filter.arrayFilters));
        }
        else {
            MongoSubmodelElementPath filter = getFilter(identifier.getIdShortPath());
            result = submodelCollection.updateOne(
                    getFilterForSubmodel(identifier.getSubmodelId()),
                    Updates.set(filter.fieldname, asDocument(submodelElement)),
                    new UpdateOptions().arrayFilters(filter.arrayFilters));
        }
        if (result.getModifiedCount() == 0)
            throw new ResourceNotFoundException(identifier.toReference());
    }


    private MongoSubmodelElementPath getFilter(IdShortPath path) {
        MongoSubmodelElementPath result = new MongoSubmodelElementPath();
        result.fieldname = SUBMODEL_ELEMENTS_KEY;
        for (int i = 0; i < path.getElements().size() - 1; i++) {
            String key = path.getElements().get(i);
            if (isIndex(key)) {
                key = key.substring(1, key.length() - 1);
                result.fieldname += String.format(".%s.%s", key, VALUE_KEY);
            }
            else {
                result.fieldname += String.format(".$[a%d].%s", i, VALUE_KEY);
                result.arrayFilters.add(Filters.eq(String.format("a%d.%s", i, ID_SHORT_KEY), key));
            }
        }
        String lastKey = path.getElements().get(path.getElements().size() - 1);
        if (isIndex(lastKey)) {
            result.fieldname += "." + lastKey;
        }
        else {
            result.fieldname += ".$[i]";
            result.arrayFilters.add(Filters.eq(String.format("i.%s", ID_SHORT_KEY), path.getElements().get(path.getElements().size() - 1)));
        }
        return result;
    }


    @Override
    public void save(OperationHandle handle, OperationResult result) {
        Document document = new Document();
        try {
            Document handleDocument = Document.parse(serializer.write(handle));
            Document resultDocument = Document.parse(serializer.write(result));
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
    public void deleteAssetAdministrationShell(String id) throws ResourceNotFoundException, PersistenceException {
        deleteElementById(aasCollection, id);
    }


    @Override
    public void deleteSubmodel(String id) throws ResourceNotFoundException, PersistenceException {
        deleteElementById(submodelCollection, id);
        Bson filter = Filters.eq("submodels.id", id);
        Bson update = Updates.pull("submodels", filter);
        aasCollection.updateMany(filter, update);
    }


    @Override
    public void deleteConceptDescription(String id) throws ResourceNotFoundException, PersistenceException {
        deleteElementById(cdCollection, id);
    }

    private static final int RANDOM_VALUE_LENGTH = 100;

    private static String generateRandomValue() {
        byte[] data = new byte[RANDOM_VALUE_LENGTH];
        new Random().nextBytes(data);
        return new String(data, StandardCharsets.UTF_8);
    }


    @Override
    public void deleteSubmodelElement(SubmodelElementIdentifier identifier) throws ResourceNotFoundException, PersistenceException {
        SubmodelElementIdentifier parentIdentifier = SubmodelElementIdentifier.fromReference(ReferenceHelper.getParent(identifier.toReference()));
        UpdateResult result;

        // deleting from submodel
        if (parentIdentifier.getIdShortPath().isEmpty()) {
            result = submodelCollection.updateOne(
                    getFilterForSubmodel(identifier.getSubmodelId()),
                    Updates.pull(SUBMODEL_ELEMENTS_KEY, Filters.eq(ID_SHORT_KEY, identifier.getIdShortPath().getElements().get(0))));
        }
        else {
            // delete from collection or list

            String lastKeyValue = identifier.getIdShortPath().getElements().get(identifier.getIdShortPath().getElements().size() - 1);
            MongoSubmodelElementPath filter = getFilter(parentIdentifier.getIdShortPath());
            filter.fieldname += "." + VALUE_KEY;
            // delete from list
            // As deletion by index is not possible, set value to random unique value and the delete all entries with this value.
            // This potentially deletes elements that are not intended to be deleted by probability converges to zero if random value is long enough.
            if (isIndex(lastKeyValue)) {
                String randomValue = generateRandomValue();
                submodelCollection.updateOne(
                        getFilterForSubmodel(identifier.getSubmodelId()),
                        Updates.set(filter.fieldname + "." + lastKeyValue, randomValue),
                        new UpdateOptions().arrayFilters(filter.arrayFilters));
                result = submodelCollection.updateOne(
                        getFilterForSubmodel(identifier.getSubmodelId()),
                        Updates.pull(filter.fieldname, randomValue),
                        new UpdateOptions().arrayFilters(filter.arrayFilters));
            }
            // delete from collection
            else {
                result = submodelCollection.updateOne(
                        getFilterForSubmodel(identifier.getSubmodelId()),
                        Updates.pull(filter.fieldname, Filters.eq(ID_SHORT_KEY, lastKeyValue)),
                        new UpdateOptions().arrayFilters(filter.arrayFilters));
            }
        }
        if (result.getModifiedCount() == 0)
            throw new ResourceNotFoundException(identifier.toReference());
    }


    private Bson getFilterForSubmodel(String submodelId) {
        return Filters.eq(ID_KEY, submodelId);
    }


    private Bson getIdShortFilter(String idShort) {
        return Filters.eq(ID_SHORT_KEY, idShort);
    }


    private Bson getSemanticIdFilter(Reference semanticId) throws PersistenceException {
        if (Objects.isNull(semanticId))
            return NO_FILTER;
        return Filters.eq("semanticId", getReferenceAsDocument(semanticId));
    }


    private Bson getIsCaseOfFilter(Reference isCaseOf) throws PersistenceException {
        if (Objects.isNull(isCaseOf))
            return NO_FILTER;
        return Filters.eq("isCaseOf", getReferenceAsDocument(isCaseOf)); // TODO better equals implementation
    }


    private Bson getDataSpecificationFilter(Reference dataSpecification) throws PersistenceException {
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


    private Document getReferenceAsDocument(Reference reference) throws PersistenceException {
        try {
            //Reference type has to match the one in the database exactly, ReferenceBuilder sets the wrong one
            reference.setType(ReferenceTypes.EXTERNAL_REFERENCE);
            String refJson = serializer.write(reference);
            return Document.parse(refJson);
        }
        catch (SerializationException e) {
            throw new PersistenceException(e);
        }
    }


    private static <T extends Referable> T prepareResult(T result, QueryModifier modifier) {
        if (result == null || modifier == null) {
            throw new IllegalArgumentException("Result or modifier cannot be null.");
        }
        return QueryModifierHelper.applyQueryModifier(result, modifier);
    }


    private <T extends Referable> T fetch(SubmodelElementIdentifier identifier, Class<T> returnType) throws ResourceNotFoundException, PersistenceException {
        Document result = loadDocument(identifier);
        if (Objects.isNull(result))
            throw new ResourceNotFoundException(identifier.toReference());
        try {
            return deserializer.read(result.toJson(), returnType);
        }
        catch (DeserializationException e) {
            throw new PersistenceException(e);
        }
    }


    private Document loadDocument(SubmodelElementIdentifier identifier) throws ResourceNotFoundException, PersistenceException {
        List<Bson> pipelineStages = new ArrayList<>();
        // Filter for the right submodel
        pipelineStages.add(Aggregates.match(Filters.eq(ID_KEY, identifier.getSubmodelId())));
        if (identifier.getIdShortPath().isEmpty())
            return submodelCollection.aggregate(pipelineStages).first();
        else {
            // Filter for the right submodel element in the "submodelElements" array of the right submodel
            pipelineStages.add(Aggregates.unwind("$" + SUBMODEL_ELEMENTS_KEY));
            pipelineStages.add(Aggregates.match(Filters.eq(SUBMODEL_ELEMENTS_KEY + "." + ID_SHORT_KEY, identifier.getIdShortPath().getElements().get(0))));

            String currentFieldName = SUBMODEL_ELEMENTS_KEY;
            for (int i = 1; i < identifier.getIdShortPath().getElements().size(); i++) {
                // Filter for the right submodel element in the "value" array of the parent submodel element
                currentFieldName += "." + VALUE_KEY;
                pipelineStages.add(Aggregates.unwind("$" + currentFieldName));
                if (isIndex(identifier.getIdShortPath().getElements().get(i))) {
                    pipelineStages.add(Aggregates.skip(Integer.parseInt(identifier.getIdShortPath().getElements().get(i))));
                    pipelineStages.add(Aggregates.limit(1));
                }
                else {
                    pipelineStages.add(Aggregates.match(Filters.eq(currentFieldName + "." + ID_SHORT_KEY, identifier.getIdShortPath().getElements().get(i))));
                }
            }

            try {
                Document nestedResult = submodelCollection.aggregate(pipelineStages).first().get(SUBMODEL_ELEMENTS_KEY, Document.class);
                for (int i = 1; i < identifier.getIdShortPath().getElements().size(); i++) {
                    nestedResult = nestedResult.get(VALUE_KEY, Document.class);
                }
                return nestedResult;
            }
            catch (Exception e) {
                throw new ResourceNotFoundException(identifier.toReference());
            }
        }
    }

    //    private Bson getFilter(SubmodelElementIdentifier identifier) {
    //        Reference reference = identifier.toReference();
    //        if (Objects.isNull(reference) || reference.getKeys().isEmpty()) {
    //            return Filters.empty();
    //        }
    //        Bson filter = Filters.eq(ID_KEY, reference.getKeys().get(0).getValue());
    //        if (reference.getKeys().size() > 1) {
    //            String currentPropertyKey = SUBMODEL_ELEMENTS_KEY;
    //            for (int i = 1; i < reference.getKeys().size(); i++) {
    //                String key = reference.getKeys().get(i).getValue();
    //                if (isIndex(key)) {
    //                    filter = Filters.and(filter, Filters.exists(currentPropertyKey + "." + VALUE_KEY + "." + key));
    //                }
    //                else {
    //                    filter = Filters.and(filter, Filters.eq(currentPropertyKey + "." + ID_SHORT_KEY, key));
    //                }
    //                currentPropertyKey += "." + VALUE_KEY;
    //            }
    //        }
    //        return filter;
    //    }


    private static boolean isIndex(String keyValue) {
        return INDEX_REGEX.matcher(keyValue).matches();
    }


    private <T extends Referable> Page<T> preparePagedResult(MongoCollection<Document> collection, Bson filter, PagingInfo paging, QueryModifier modifier, Class<T> type)
            throws PersistenceException {
        return preparePagedResult(
                asPojo(
                        applyPaging(collection.find(filter), paging),
                        type),
                modifier,
                paging);
    }


    private static <T extends Referable> Page<T> preparePagedResult(Stream<T> input, QueryModifier modifier, PagingInfo paging) throws PersistenceException {
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


    private static String nextCursor(PagingInfo paging, int resultCount) throws PersistenceException {
        return nextCursor(paging, paging.hasLimit() && resultCount > paging.getLimit());
    }


    private static String nextCursor(PagingInfo paging, boolean hasMoreData) throws PersistenceException {
        if (!hasMoreData) {
            return null;
        }
        if (!paging.hasLimit()) {
            throw new PersistenceException("unable to generate next cursor for paging - there should not be more data available if previous request did not have a limit set");
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

    private static class MongoSubmodelElementPath {
        String fieldname;
        List<Bson> arrayFilters = new ArrayList<>();
    }
}
