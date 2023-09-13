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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import jakarta.json.JsonMergePatch;
import jakarta.json.JsonValue;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;


/**
 * Base class for implementing code to execute a given Request.
 *
 * @param <I> type of the request
 * @param <O> type of the corresponding response
 */
public abstract class AbstractRequestHandler<I extends Request<O>, O extends Response> {

    protected final RequestExecutionContext context;

    protected AbstractRequestHandler(RequestExecutionContext context) {
        this.context = context;
    }


    /**
     * Creates a empty response object.
     *
     * @return new empty response object
     * @throws NoSuchMethodException if response type does not implement a parameterless constructor
     * @throws InstantiationException if response type is abstract
     * @throws InvocationTargetException if parameterless constructor of response type throws an exception
     * @throws IllegalAccessException if parameterless constructor of response type is inaccessible
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
     * @return the response
     * @throws Exception if processing the request fails
     */
    public abstract O process(I request) throws Exception;


    /**
     * Check for each SubmodelElement if there is an AssetConnection. If yes read the value from it and compare it to
     * the current value.If they differ from each other update the submodelelement with the value from the
     * AssetConnection.
     *
     * @param parent of the SubmodelElement List
     * @param submodelElements List of SubmodelElements which should be considered and updated
     * @throws ResourceNotFoundException if reference does not point to valid element
     * @throws ResourceNotAContainerElementException if reference does not point to valid element
     * @throws AssetConnectionException if reading value from asset connection fails
     * @throws de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException if mapping value read from
     *             asset connection fails
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException if publishing fails
     */
    protected void syncWithAsset(Reference parent, Collection<SubmodelElement> submodelElements)
            throws ResourceNotFoundException, ResourceNotAContainerElementException, AssetConnectionException, ValueMappingException, MessageBusException {
        if (parent == null || submodelElements == null) {
            return;
        }
        Map<SubmodelElement, ElementValue> updatedSubmodelElements = new HashMap<>();
        for (SubmodelElement submodelElement: submodelElements) {
            Reference reference = AasUtils.toReference(parent, submodelElement);
            Optional<DataElementValue> newValue = context.getAssetConnectionManager().readValue(reference);
            if (newValue.isPresent()) {
                ElementValue oldValue = ElementValueMapper.toValue(submodelElement);
                if (!Objects.equals(oldValue, newValue.get())) {
                    updatedSubmodelElements.put(submodelElement, newValue.get());
                }
            }
            else if (SubmodelElementCollection.class.isAssignableFrom(submodelElement.getClass())) {
                syncWithAsset(reference, ((SubmodelElementCollection) submodelElement).getValue());
            }
        }

        for (var update: updatedSubmodelElements.entrySet()) {
            Reference reference = AasUtils.toReference(parent, update.getKey());
            SubmodelElement oldElement = update.getKey();
            SubmodelElement newElement = DeepCopyHelper.deepCopy(oldElement, SubmodelElement.class);
            ElementValueMapper.setValue(newElement, update.getValue());
            context.getPersistence().update(reference, newElement);
            submodelElements.remove(oldElement);
            submodelElements.add(newElement);
            context.getMessageBus().publish(ValueChangeEventMessage.builder()
                    .element(reference)
                    .oldValue(ElementValueMapper.toValue(oldElement))
                    .newValue(ElementValueMapper.toValue(newElement))
                    .build());
        }
    }


    /**
     * Creates an updated element based on a JSON merge patch.
     *
     * @param <T> the type of the element to update
     * @param changes the JSON merge patch containing the changes to apply
     * @param targetBean the original element to apply the update to
     * @param type the type information
     * @return the updated element
     */
    protected <T> T mergePatch(JsonMergePatch changes, T targetBean, Class<T> type) {
        ObjectMapper mapper = new ObjectMapper();
        JsonValue target = mapper.convertValue(targetBean, JsonValue.class);
        JsonValue result = changes.apply(target);
        return mapper.convertValue(result, type);
    }
}
