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

import com.google.common.reflect.TypeToken;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;


/**
 * Base class for implementing code to execute a given Request.
 *
 * @param <I> type of the request
 * @param <O> type of the corresponding response
 */
public abstract class RequestHandler<I extends Request<O>, O extends Response> {

    protected final Persistence persistence;
    protected final MessageBus messageBus;
    protected final AssetConnectionManager assetConnectionManager;

    public RequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        this.persistence = persistence;
        this.messageBus = messageBus;
        this.assetConnectionManager = assetConnectionManager;
    }


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
        Class<?> responseType = TypeToken.of(getClass()).resolveType(RequestHandler.class.getTypeParameters()[1]).getRawType();
        return (O) responseType.getConstructor().newInstance();
    }


    /**
     * Processes a request and returns the resulting response
     *
     * @param request the request
     * @return the response
     * @throws Exception if processing the request fails
     */
    public abstract O process(I request) throws Exception;


    /**
     * Check for each SubmodelElement if there is an AssetConnection.If yes read
     * the value from it and compare it to the current value.If they differ from
     * each other update the submodelelement with the value from the
     * AssetConnection.
     *
     * @param parent of the SubmodelElement List
     * @param submodelElements List of SubmodelElements which should be
     *            considered and updated
     * @throws ResourceNotFoundException if reference does not point to valid
     *             element
     * @throws AssetConnectionException if reading value from asset connection
     *             fails
     * @throws
     * de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException
     *             if mapping value read from asset connection fails
     * @throws
     * de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException if
     *             publishing fails
     */
    protected void syncWithAsset(Reference parent, Collection<SubmodelElement> submodelElements)
            throws ResourceNotFoundException, AssetConnectionException, ValueMappingException, MessageBusException {
        if (parent == null || submodelElements == null) {
            return;
        }
        for (SubmodelElement submodelElement: submodelElements) {
            Reference reference = AasUtils.toReference(parent, submodelElement);
            Optional<DataElementValue> newValue = assetConnectionManager.readValue(reference);
            if (newValue.isPresent()) {
                ElementValue oldValue = ElementValueMapper.toValue(submodelElement);
                if (!Objects.equals(oldValue, newValue)) {
                    submodelElement = persistence.put(null, reference, ElementValueMapper.setValue(submodelElement, newValue.get()));
                    messageBus.publish(ElementUpdateEventMessage.builder()
                            .element(AasUtils.toReference(parent, submodelElement))
                            .value(submodelElement)
                            .build());
                }
            }
            else if (SubmodelElementCollection.class.isAssignableFrom(submodelElement.getClass())) {
                syncWithAsset(reference, ((SubmodelElementCollection) submodelElement).getValues());
            }
        }
    }
}
