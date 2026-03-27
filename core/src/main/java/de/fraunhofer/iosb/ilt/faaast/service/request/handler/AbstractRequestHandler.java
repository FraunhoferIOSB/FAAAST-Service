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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.google.common.reflect.TypeToken;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementCollectionValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementListValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.DataElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


/**
 * Base class for implementing code to execute a given Request.
 *
 * @param <I> type of the request
 * @param <O> type of the corresponding response
 */
public abstract class AbstractRequestHandler<I extends Request<O>, O extends Response> {

    /**
     * Creates a empty response object.
     *
     * @return new empty response object
     * @throws NoSuchMethodException if response type does not implement a
     *             parameterless constructor
     * @throws InstantiationException if response type is abstract
     * @throws InvocationTargetException if parameterless constructor of
     *             response type throws an exception
     * @throws IllegalAccessException if parameterless constructor of response
     *             type is inaccessible
     */
    public O newResponse() throws NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
        return (O) ConstructorUtils.invokeConstructor(
                TypeToken.of(getClass())
                        .resolveType(AbstractRequestHandler.class.getTypeParameters()[1])
                        .getRawType());
    }


    /**
     * Processes a request and returns the resulting response.
     *
     * @param request the request
     * @param context the execution context
     * @return the response
     * @throws Exception if processing the request fails
     */
    public abstract O process(I request, RequestExecutionContext context) throws Exception;


    /**
     * Checks for each SubmodelElement if there is an AssetConnection
     * ValueProvider. If yes read the value from it and compare it to the
     * current value.If they differ from each other update the submodelelement
     * with the value from the AssetConnection.
     *
     * @param parent of the SubmodelElement List
     * @param submodelElements List of SubmodelElements which should be
     *            considered and updated
     * @param publishOnMessageBus if ValueChangeEventMessages should be sent on
     *            message bus
     * @param context the execution context
     * @param parentIsList True if parent is a SubmodelElementList, false if
     *            not.
     * @throws ResourceNotFoundException if reference does not point to valid
     *             element
     * @throws ResourceNotAContainerElementException if reference does not point
     *             to valid element
     * @throws AssetConnectionException if reading value from asset connection
     *             fails
     * @throws ValueMappingException if mapping value read from asset connection
     *             fails
     * @throws MessageBusException if publishing fails
     * @throws PersistenceException if storage error occurs
     */
    protected void syncWithAsset(Reference parent, Collection<SubmodelElement> submodelElements, boolean publishOnMessageBus, RequestExecutionContext context,
                                 boolean parentIsList)
            throws ResourceNotFoundException, ResourceNotAContainerElementException, AssetConnectionException, ValueMappingException, MessageBusException, PersistenceException {
        syncWithAsset(parent, submodelElements, publishOnMessageBus, context, parentIsList, SubmodelElement.class);
    }


    private <T extends SubmodelElement> void syncWithAsset(Reference parent, Collection<T> submodelElements, boolean publishOnMessageBus, RequestExecutionContext context,
                                                           boolean parentIsList, Class<T> collectionClass)
            throws ResourceNotFoundException, ResourceNotAContainerElementException, AssetConnectionException, ValueMappingException, MessageBusException, PersistenceException {
        if (parent == null || submodelElements == null) {
            return;
        }
        Map<T, ElementValue> updatedSubmodelElements = new HashMap<>();
        Map<T, Reference> updatedSubmodelElementRefs = new HashMap<>();
        int index = 0;
        for (T submodelElement: submodelElements) {
            Reference reference = parentIsList
                    ? ReferenceBuilder.with(parent).index(index).build()
                    : ReferenceBuilder.with(parent).element(submodelElement).build();
            Optional<DataElementValue> newValue = context.getAssetConnectionManager().readValue(reference);
            if (newValue.isPresent()) {
                ElementValue oldValue = ElementValueMapper.toValue(submodelElement);
                if (!Objects.equals(oldValue, newValue.get())) {
                    updatedSubmodelElements.put(submodelElement, newValue.get());
                    updatedSubmodelElementRefs.put(submodelElement, reference);
                }
            }
            else if (isSubmodelElementContainer(submodelElement)) {
                syncAssetSubmodelElementContainer(reference, submodelElement, publishOnMessageBus, context);
            }
            index++;
        }
        for (var update: updatedSubmodelElements.entrySet()) {
            Reference reference = updatedSubmodelElementRefs.get(update.getKey());
            T oldElement = update.getKey();
            T newElement = DeepCopyHelper.deepCopy(oldElement, collectionClass);
            ElementValueMapper.setValue(newElement, update.getValue());
            context.getPersistence().update(reference, newElement);
            submodelElements.remove(oldElement);
            submodelElements.add(newElement);
            ElementValue oldValue = ElementValueMapper.toValue(oldElement);
            ElementValue newValue = ElementValueMapper.toValue(newElement);
            if (publishOnMessageBus && !Objects.equals(oldValue, newValue)) {
                context.getMessageBus().publish(ValueChangeEventMessage.builder()
                        .element(reference)
                        .oldValue(oldValue)
                        .newValue(newValue)
                        .build());
            }
        }
    }


    /**
     * Checks for each SubmodelElement if there is an AssetConnection
     * ValueProvider, it compares the new value with the current value. If it
     * has changed, it writes the new value to the AssetConnection.
     *
     * @param <T> The actual class derived from SubmodelElement.
     * @param parent of the SubmodelElement List.
     * @param oldSubmodelElements The old list of SubmodelElements.
     * @param newSubmodelElements The new list of SubmodelElements.
     * @param publishOnMessageBus if ValueChangeEventMessages should be sent on
     *            message bus.
     * @param context the execution context.
     * @param parentIsList True if parent is a SubmodelElementList, false if
     *            not.
     * @throws ValueMappingException if mapping value read from asset connection
     *             fails.
     * @throws MessageBusException if publishing fails.
     * @throws AssetConnectionException if writing the value to the asset
     *             connection fails.
     */
    protected <T extends SubmodelElement> void syncWriteAsset(Reference parent, List<T> oldSubmodelElements, List<T> newSubmodelElements, boolean publishOnMessageBus,
                                                              RequestExecutionContext context, boolean parentIsList)
            throws ValueMappingException, AssetConnectionException, MessageBusException {

        if (parent == null || newSubmodelElements == null) {
            return;
        }

        int index = 0;
        for (T newSubmodelElement: newSubmodelElements) {
            Reference reference = parentIsList
                    ? ReferenceBuilder.with(parent).index(index).build()
                    : ReferenceBuilder.with(parent).element(newSubmodelElement).build();
            T oldSubmodelElement = null;
            if (oldSubmodelElements != null) {
                Optional<T> elem = oldSubmodelElements.stream().filter(x -> Objects.equals(x.getIdShort(), newSubmodelElement.getIdShort())).findFirst();
                oldSubmodelElement = elem.isPresent() ? elem.get() : null;
            }
            syncWriteAssetSubmodelElement(reference, oldSubmodelElement, newSubmodelElement, publishOnMessageBus, context);

            index++;
        }
    }


    /**
     * Creates an updated element based on a JSON merge patch.
     *
     * @param <T> the type of the element to update
     * @param patch the JSON merge patch containing the changes to apply
     * @param targetBean the original element to apply the update to
     * @param type the type information
     * @return the updated element
     * @throws
     * de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException
     *             if applying the merge patch fails
     */
    protected <T> T applyMergePatch(JsonMergePatch patch, T targetBean, Class<T> type) throws InvalidRequestException {
        try {
            JsonNode json = new JsonSerializer().toNode(targetBean);
            JsonNode updatedJson = patch.apply(json);
            return new JsonDeserializer().read(updatedJson, type);
        }
        catch (JsonPatchException | IllegalArgumentException | DeserializationException e) {
            throw new InvalidRequestException("Error applying JSON merge patch", e);
        }
    }


    /**
     * Parses a {@code SpecificAssetId} as {@code AssetIdentification}.
     *
     * @param specificAssetId the input to parse
     * @return the parsed output
     */
    protected AssetIdentification parseSpecificAssetId(SpecificAssetId specificAssetId) {
        if (Objects.isNull(specificAssetId)) {
            return null;
        }
        if (specificAssetId.getName().equalsIgnoreCase(FaaastConstants.KEY_GLOBAL_ASSET_ID)) {
            return new GlobalAssetIdentification.Builder()
                    .value(specificAssetId.getValue())
                    .build();
        }
        return new SpecificAssetIdentification.Builder()
                .value(specificAssetId.getValue())
                .key(specificAssetId.getName())
                .build();
    }


    /**
     * Parses a list of {@code SpecificAssetId} as {@code AssetIdentification}.
     *
     * @param specificAssetIds the input to parse
     * @return the parsed output
     */
    protected List<AssetIdentification> parseSpecificAssetIds(List<SpecificAssetId> specificAssetIds) {
        if (Objects.isNull(specificAssetIds)) {
            return List.of();
        }
        return specificAssetIds.stream()
                .map(this::parseSpecificAssetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    /**
     * Synchronizes SubmodelElement Containers with the AssetConnection.
     *
     * @param reference of the SubmodelElement container
     * @param submodelElement the desired submodelElement
     * @param publishOnMessageBus if ValueChangeEventMessages should be sent on
     *            message bus
     * @param context the execution context
     * @throws ResourceNotFoundException if reference does not point to valid
     *             element
     * @throws ResourceNotAContainerElementException if reference does not point
     *             to valid element
     * @throws AssetConnectionException if reading value from asset connection
     *             fails
     * @throws ValueMappingException if mapping value read from asset connection
     *             fails
     * @throws MessageBusException if publishing fails
     * @throws PersistenceException if storage error occurs
     */
    protected void syncAssetSubmodelElementContainer(Reference reference, SubmodelElement submodelElement, boolean publishOnMessageBus, RequestExecutionContext context)
            throws ResourceNotFoundException, ResourceNotAContainerElementException, AssetConnectionException, ValueMappingException, MessageBusException, PersistenceException {
        if (submodelElement instanceof SubmodelElementCollection collection) {
            syncWithAsset(reference, collection.getValue(), publishOnMessageBus, context, false, SubmodelElement.class);
        }
        else if (submodelElement instanceof SubmodelElementList list) {
            syncWithAsset(reference, list.getValue(), publishOnMessageBus, context, true, SubmodelElement.class);
        }
        else if (submodelElement instanceof Entity entity) {
            syncWithAsset(reference, entity.getStatements(), publishOnMessageBus, context, false, SubmodelElement.class);
        }
        else if (submodelElement instanceof AnnotatedRelationshipElement relElement) {
            syncWithAsset(reference, relElement.getAnnotations(), publishOnMessageBus, context, false, DataElement.class);
        }
    }


    /**
     * Synchronizes (write) SubmodelElement Containers with the AssetConnection
     * ValueProvider.
     *
     * @param reference of the SubmodelElement container
     * @param oldSubmodelElement The old submodelElement.
     * @param newSubmodelElement The new submodelElement.
     * @param publishOnMessageBus if ValueChangeEventMessages should be sent on
     *            message bus
     * @param context the execution context
     * @throws ValueMappingException if mapping value read from asset connection
     *             fails
     * @throws MessageBusException if publishing fails
     * @throws AssetConnectionException if writing the value to the asset
     *             connection fails.
     */
    protected void syncWriteAssetSubmodelElementContainer(Reference reference, SubmodelElement oldSubmodelElement, SubmodelElement newSubmodelElement, boolean publishOnMessageBus,
                                                          RequestExecutionContext context)
            throws ValueMappingException, AssetConnectionException, MessageBusException {
        if (((oldSubmodelElement == null) || oldSubmodelElement instanceof SubmodelElementCollection) && (newSubmodelElement instanceof SubmodelElementCollection newCollection)) {
            List<SubmodelElement> oldElements = oldSubmodelElement != null ? ((SubmodelElementCollection) oldSubmodelElement).getValue() : null;
            syncWriteAsset(reference, oldElements, newCollection.getValue(), publishOnMessageBus, context, false);
        }
        else if (((oldSubmodelElement == null) || oldSubmodelElement instanceof Entity) && (newSubmodelElement instanceof Entity newEntity)) {
            List<SubmodelElement> oldElements = oldSubmodelElement != null ? ((Entity) oldSubmodelElement).getStatements() : null;
            syncWriteAsset(reference, oldElements, newEntity.getStatements(), publishOnMessageBus, context, false);
        }
        else if (((oldSubmodelElement == null) || oldSubmodelElement instanceof SubmodelElementList) && (newSubmodelElement instanceof SubmodelElementList newList)) {
            List<SubmodelElement> oldElements = oldSubmodelElement != null ? ((SubmodelElementList) oldSubmodelElement).getValue() : null;
            syncWriteAsset(reference, oldElements, newList.getValue(), publishOnMessageBus, context, true);
        }
        else if (((oldSubmodelElement == null) || oldSubmodelElement instanceof AnnotatedRelationshipElement)
                && (newSubmodelElement instanceof AnnotatedRelationshipElement newRelElement)) {
            List<DataElement> oldElements = oldSubmodelElement != null ? ((AnnotatedRelationshipElement) oldSubmodelElement).getAnnotations() : null;
            syncWriteAsset(reference, oldElements, newRelElement.getAnnotations(), publishOnMessageBus, context, false);
        }
    }


    /**
     * Checks whether the given submodelElement is a container (e.g. a
     * Collection).
     *
     * @param submodelElement The desired submodelElement.
     * @return True if it's a container, false if not.
     */
    protected static boolean isSubmodelElementContainer(SubmodelElement submodelElement) {
        if (submodelElement == null) {
            return false;
        }
        return SubmodelElementCollection.class.isAssignableFrom(submodelElement.getClass())
                || SubmodelElementList.class.isAssignableFrom(submodelElement.getClass())
                || Entity.class.isAssignableFrom(submodelElement.getClass())
                || AnnotatedRelationshipElement.class.isAssignableFrom(submodelElement.getClass());
    }


    /**
     * Checks for the given SubmodelElement if there is an AssetConnection
     * ValueProvider, it compares the new value with the current value. If it
     * has changed, it writes the new value to the AssetConnection. If the
     * SubmodelElement is acontainer, this is done recursively.
     *
     * @param reference of the SubmodelElement List
     * @param oldSubmodelElement The old SubmodelElement.
     * @param newSubmodelElement The new SubmodelElement,
     * @param publishOnMessageBus if ValueChangeEventMessages should be sent on
     *            message bus
     * @param context the execution context
     * @throws ValueMappingException if mapping value read from asset connection
     *             fails
     * @throws AssetConnectionException if writing the value to the asset
     *             connection fails.
     * @throws MessageBusException if publishing fails
     */
    protected void syncWriteAssetSubmodelElement(Reference reference, SubmodelElement oldSubmodelElement, SubmodelElement newSubmodelElement, boolean publishOnMessageBus,
                                                 RequestExecutionContext context)
            throws ValueMappingException, AssetConnectionException, MessageBusException {
        if ((oldSubmodelElement == null) || (Objects.equals(oldSubmodelElement.getClass(), newSubmodelElement.getClass()))
                && ElementValueHelper.isSerializableAsValue(newSubmodelElement.getClass())) {
            if (isSubmodelElementContainer(newSubmodelElement)) {
                syncWriteAssetSubmodelElementContainer(reference, oldSubmodelElement, newSubmodelElement, publishOnMessageBus, context);
            }
            else {
                writeToAsset(reference, oldSubmodelElement, newSubmodelElement, publishOnMessageBus, context);
            }
        }
    }


    /**
     * Checks for the given SubmodelElement if there is an AssetConnection
     * ValueProvider, it compares the new value with the old value. If it
     * has changed, it writes the new value to the AssetConnection. If the
     * SubmodelElement is acontainer, this is done recursively.
     *
     * @param reference of the SubmodelElement.
     * @param submodelElement The desired SubmodelElement.
     * @param oldValue The old value.
     * @param newValue The new value.
     * @param publishOnMessageBus if ValueChangeEventMessages should be sent on
     *            message bus
     * @param context the execution context
     * @throws AssetConnectionException if writing the value to the asset
     *             connection fails.
     * @throws MessageBusException if publishing fails
     */
    protected void syncWriteAssetSubmodelElementValue(Reference reference, SubmodelElement submodelElement, ElementValue oldValue, ElementValue newValue,
                                                      boolean publishOnMessageBus,
                                                      RequestExecutionContext context)
            throws AssetConnectionException, MessageBusException {
        if ((oldValue == null) || Objects.equals(oldValue.getClass(), newValue.getClass())) {
            if (isSubmodelElementContainer(submodelElement)) {
                syncWriteAssetSubmodelElementContainerValue(reference, submodelElement, oldValue, newValue, publishOnMessageBus, context);
            }
            else {
                writeToAsset(reference, oldValue, newValue, publishOnMessageBus, context);
            }
        }
    }


    /**
     * Checks for the given SubmodelElement if there is an AssetConnection
     * ValueProvider, it compares the new value with the current value. If it
     * has changed, it writes the new value to the AssetConnection. If the
     * SubmodelElement is acontainer, this is done recursively.
     * 
     * @param parent The parent SubmodelElement.
     * @param parentElement The reference of the parent SubmodelElement.
     * @param oldValue The old value.
     * @param newValue Tzhe new value.
     * @param publishOnMessageBus if ValueChangeEventMessages should be sent on
     *            message bus
     * @param context the execution context
     * @throws MessageBusException if publishing fails
     * @throws AssetConnectionException if writing the value to the asset
     *             connection fails.
     */
    protected void syncWriteAssetSubmodelElementContainerValue(Reference parent, SubmodelElement parentElement, ElementValue oldValue, ElementValue newValue,
                                                               boolean publishOnMessageBus,
                                                               RequestExecutionContext context)
            throws AssetConnectionException, MessageBusException {

        if ((parentElement instanceof SubmodelElementCollection collection)
                && ((oldValue == null) || oldValue instanceof SubmodelElementCollectionValue)
                && (newValue instanceof SubmodelElementCollectionValue newCollValue)) {
            syncWriteCollectionValue(parent, collection, oldValue != null ? (SubmodelElementCollectionValue) oldValue : null, newCollValue, publishOnMessageBus, context);
        }
        else if ((parentElement instanceof SubmodelElementList list)
                && ((oldValue == null) || oldValue instanceof SubmodelElementListValue)
                && (newValue instanceof SubmodelElementListValue newListValue)) {
            syncWriteListValue(parent, list, oldValue != null ? (SubmodelElementListValue) oldValue : null, newListValue, publishOnMessageBus, context);
        }
        else if ((parentElement instanceof Entity entity)
                && ((oldValue == null) || oldValue instanceof EntityValue)
                && (newValue instanceof EntityValue newEntityValue)) {
            syncWriteEntityValue(parent, entity, oldValue != null ? (EntityValue) oldValue : null, newEntityValue, publishOnMessageBus, context);
        }
        else if ((parentElement instanceof AnnotatedRelationshipElement entRelElement)
                && ((oldValue == null) || oldValue instanceof AnnotatedRelationshipElementValue)
                && (newValue instanceof AnnotatedRelationshipElementValue newEntRelElementValue)) {
            syncWriteAnnotatedRelationshipValue(parent, entRelElement, oldValue != null ? (AnnotatedRelationshipElementValue) oldValue : null, newEntRelElementValue,
                    publishOnMessageBus, context);
        }
    }


    private void syncWriteAnnotatedRelationshipValue(Reference parent, AnnotatedRelationshipElement entRelElement, AnnotatedRelationshipElementValue oldEntRelElementValue,
                                                     AnnotatedRelationshipElementValue newEntRelElementValue, boolean publishOnMessageBus, RequestExecutionContext context)
            throws AssetConnectionException, MessageBusException {
        for (var child: entRelElement.getAnnotations()) {
            if (newEntRelElementValue.getAnnotations().containsKey(child.getIdShort())) {
                ElementValue oldValue;
                if ((oldEntRelElementValue != null) && oldEntRelElementValue.getAnnotations().containsKey(child.getIdShort())) {
                    oldValue = oldEntRelElementValue.getAnnotations().get(child.getIdShort());
                }
                else {
                    oldValue = null;
                }
                Reference reference = ReferenceBuilder.with(parent).element(child).build();
                syncWriteAssetSubmodelElementValue(reference, child, oldValue,
                        newEntRelElementValue.getAnnotations().get(child.getIdShort()),
                        publishOnMessageBus, context);
            }
        }
    }


    private void syncWriteEntityValue(Reference parent, Entity oldEntity, EntityValue oldEntityValue, EntityValue newEntityValue, boolean publishOnMessageBus,
                                      RequestExecutionContext context)
            throws MessageBusException, AssetConnectionException {
        for (var child: oldEntity.getStatements()) {
            if (newEntityValue.getStatements().containsKey(child.getIdShort())) {
                ElementValue oldValue;
                if ((oldEntityValue != null) && oldEntityValue.getStatements().containsKey(child.getIdShort())) {
                    oldValue = oldEntityValue.getStatements().get(child.getIdShort());
                }
                else {
                    oldValue = null;
                }
                Reference reference = ReferenceBuilder.with(parent).element(child).build();
                syncWriteAssetSubmodelElementValue(reference, child, oldValue,
                        newEntityValue.getStatements().get(child.getIdShort()),
                        publishOnMessageBus, context);
            }
        }
    }


    private void syncWriteListValue(Reference parent, SubmodelElementList list, SubmodelElementListValue oldListValue, SubmodelElementListValue newListValue,
                                    boolean publishOnMessageBus, RequestExecutionContext context)
            throws MessageBusException, AssetConnectionException {
        for (int index = 0; index < list.getValue().size(); index++) {
            if (newListValue.getValues().size() > index) {
                ElementValue oldValue;
                if ((oldListValue != null) && (oldListValue.getValues().size() > index)) {
                    oldValue = oldListValue.getValues().get(index);
                }
                else {
                    oldValue = null;
                }
                Reference reference = ReferenceBuilder.with(parent).index(index).build();
                syncWriteAssetSubmodelElementValue(reference, list.getValue().get(index), oldValue, newListValue.getValues().get(index),
                        publishOnMessageBus, context);
            }
        }
    }


    private void syncWriteCollectionValue(Reference parent, SubmodelElementCollection collection, SubmodelElementCollectionValue oldCollValue,
                                          SubmodelElementCollectionValue newCollValue, boolean publishOnMessageBus, RequestExecutionContext context)
            throws AssetConnectionException, MessageBusException {
        for (var child: collection.getValue()) {
            if (newCollValue.getValues().containsKey(child.getIdShort())) {
                ElementValue oldValue;
                if ((oldCollValue != null) && oldCollValue.getValues().containsKey(child.getIdShort())) {
                    oldValue = oldCollValue.getValues().get(child.getIdShort());
                }
                else {
                    oldValue = null;
                }
                Reference reference = ReferenceBuilder.with(parent).element(child).build();
                syncWriteAssetSubmodelElementValue(reference, child, oldValue, newCollValue.getValues().get(child.getIdShort()),
                        publishOnMessageBus, context);
            }
        }
    }


    private void writeToAsset(Reference reference, SubmodelElement oldSubmodelElement, SubmodelElement newSubmodelElement, boolean publishOnMessageBus,
                              RequestExecutionContext context)
            throws ValueMappingException, AssetConnectionException, MessageBusException {
        ElementValue oldValue = oldSubmodelElement != null ? ElementValueMapper.toValue(oldSubmodelElement) : null;
        ElementValue newValue = ElementValueMapper.toValue(newSubmodelElement);
        writeToAsset(reference, oldValue, newValue, publishOnMessageBus, context);
    }


    private void writeToAsset(Reference reference, ElementValue oldValue, ElementValue newValue, boolean publishOnMessageBus, RequestExecutionContext context)
            throws AssetConnectionException, MessageBusException {

        if ((context.getAssetConnectionManager().hasValueProvider(reference))
                && ((oldValue == null) || (!Objects.equals(oldValue, newValue)))) {
            context.getAssetConnectionManager().setValue(reference, newValue);
            if (publishOnMessageBus) {
                context.getMessageBus().publish(ValueChangeEventMessage.builder()
                        .element(reference)
                        .oldValue(oldValue)
                        .newValue(newValue)
                        .build());
            }
        }

    }
}
