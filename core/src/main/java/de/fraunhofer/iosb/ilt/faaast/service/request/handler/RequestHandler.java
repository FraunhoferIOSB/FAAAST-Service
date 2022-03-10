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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationFinishEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationInvokeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


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
     * @throws Exception if any executing fails
     */
    public abstract O process(I request) throws Exception;


    /**
     * Publish a ElementCreateEventMessage to the message bus
     *
     * @param reference of the element
     * @param referable the instance
     */
    public void publishElementCreateEventMessage(Reference reference, Referable referable) {
        try {
            ElementCreateEventMessage eventMessage = new ElementCreateEventMessage();
            eventMessage.setElement(reference);
            eventMessage.setValue(referable);
            messageBus.publish(eventMessage);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    ;


    /**
     * Publish a ElementReadEventMessage to the message bus
     *
     * @param reference of the element
     * @param referable the instance
     */
    public void publishElementReadEventMessage(Reference reference, Referable referable) {
        ElementReadEventMessage eventMessage = new ElementReadEventMessage();
        eventMessage.setElement(reference);
        eventMessage.setValue(referable);
        try {
            this.messageBus.publish(eventMessage);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    ;


    /**
     * Publish a ElementUpdateEventMessage to the message bus
     *
     * @param reference of the element
     * @param referable the instance
     */
    public void publishElementUpdateEventMessage(Reference reference, Referable referable) {
        ElementUpdateEventMessage eventMessage = new ElementUpdateEventMessage();
        eventMessage.setElement(reference);
        eventMessage.setValue(referable);
        try {
            this.messageBus.publish(eventMessage);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    ;


    /**
     * Publish a ElementDeleteEventMessage to the message bus
     *
     * @param reference of the element
     * @param referable the instance
     */
    public void publishElementDeleteEventMessage(Reference reference, Referable referable) {
        ElementDeleteEventMessage eventMessage = new ElementDeleteEventMessage();
        eventMessage.setElement(reference);
        eventMessage.setValue(referable);
        try {
            this.messageBus.publish(eventMessage);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    ;


    /**
     * Publish a ValueChangeEventMessage to the message bus
     *
     * @param reference of the element
     * @param oldValue the value of the element before the change
     * @param newValue the new value of the element
     */
    public void publishValueChangeEventMessage(Reference reference, ElementValue oldValue, ElementValue newValue) {
        ValueChangeEventMessage eventMessage = new ValueChangeEventMessage();
        eventMessage.setElement(reference);
        eventMessage.setOldValue(oldValue);
        eventMessage.setNewValue(newValue);
        try {
            this.messageBus.publish(eventMessage);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    ;


    /**
     * Publish a OperationInvokeEventMessage to the message bus
     *
     * @param reference of the operation
     * @param input of the operation
     * @param inoutput of the operation
     */
    public void publishOperationInvokeEventMessage(Reference reference, List<ElementValue> input, List<ElementValue> inoutput) {
        OperationInvokeEventMessage eventMessage = new OperationInvokeEventMessage();
        eventMessage.setElement(reference);
        eventMessage.setInput(input);
        eventMessage.setInoutput(inoutput);
        try {
            this.messageBus.publish(eventMessage);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    ;


    /**
     * Publish a OperationFinishEventMessage to the message bus
     *
     * @param reference of the operation
     * @param output of the operation
     * @param inoutput of the operation
     */
    public void publishOperationFinishEventMessage(Reference reference, List<ElementValue> output, List<ElementValue> inoutput) {
        OperationFinishEventMessage eventMessage = new OperationFinishEventMessage();
        eventMessage.setElement(reference);
        eventMessage.setOutput(output);
        eventMessage.setInoutput(inoutput);
        try {
            this.messageBus.publish(eventMessage);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    ;


    /**
     * Write the given Value to an existing AssetConnection of a Reference
     * otherwise does nothing
     *
     * @param reference of the element to check for an AssetConnection
     * @param value which will be written to the AssetConnection
     */
    public void writeValueToAssetConnection(Reference reference, ElementValue value) {
        try {
            if (this.assetConnectionManager.hasValueProvider(reference)) {
                if (DataElementValue.class.isAssignableFrom(value.getClass())) {
                    AssetValueProvider assetValueProvider = this.assetConnectionManager.getValueProvider(reference);
                    assetValueProvider.setValue((DataElementValue) value);
                }
            }
        }
        catch (AssetConnectionException e) {
            //TODO:
            e.printStackTrace();
        }
    }


    /**
     * Read the Value from an existing AssetConnection of a Reference otherwise
     * returns null
     *
     * @param reference of the element to check for an AssetConnection
     * @return the DataElementValue from the AssetConnection. Returns null if no
     *         AssetConnection exist for the reference
     */
    public DataElementValue readDataElementValueFromAssetConnection(Reference reference) {
        try {
            if (this.assetConnectionManager.hasValueProvider(reference)) {
                AssetValueProvider assetValueProvider = this.assetConnectionManager.getValueProvider(reference);
                return assetValueProvider.getValue();
            }
            return null;
        }
        catch (AssetConnectionException e) {
            //TODO:
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Check for each SubmodelElement if there is an AssetConnection. If yes
     * read the value from it and compare it to the current value. If they
     * differ from each other update the submodelelement with the value from the
     * AssetConnection.
     *
     * @param parentReference of the SubmodelElement List
     * @param submodelElements List of SubmodelElements which should be
     *            considered and updated
     * @throws ResourceNotFoundException
     * @throws AssetConnectionException
     */
    public void readValueFromAssetConnectionAndUpdatePersistence(Reference parentReference, List<SubmodelElement> submodelElements)
            throws ResourceNotFoundException, AssetConnectionException, ValueMappingException {

        if (parentReference == null || submodelElements == null) {
            return;
        }

        for (SubmodelElement x: submodelElements) {
            Reference reference = AasUtils.toReference(parentReference, x);
            if (SubmodelElementCollection.class.isAssignableFrom(x.getClass())) {
                if (((SubmodelElementCollection) x).getValues() != null) {
                    readValueFromAssetConnectionAndUpdatePersistence(reference,
                            new ArrayList<>(((SubmodelElementCollection) x).getValues()));
                    return;
                }

            }
            if (this.assetConnectionManager.hasValueProvider(reference)) {
                ElementValue currentValue = ElementValueMapper.toValue(x);
                ElementValue assetValue = this.assetConnectionManager.getValueProvider(reference).getValue();

                try {
                    if (currentValue != null && assetValue != null && !currentValue.getClass().isAssignableFrom(assetValue.getClass())) {
                        throw new RuntimeException("Types are incompatible - " + currentValue.getClass().getSimpleName() + " -- " + assetValue.getClass().getSimpleName());
                    }
                    if (!Objects.equals(assetValue, currentValue)) {
                        x = ElementValueMapper.setValue(x, assetValue);
                        x = persistence.put(null, reference, x);
                        publishElementUpdateEventMessage(reference, x);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }


    /**
     * Converts a list of {@link io.adminshell.aas.v3.model.OperationVariable}
     * to a list of
     * {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue}
     *
     * @param variables list of operation variables
     * @return the corresponding list of element values
     */
    public static List<ElementValue> toValues(List<OperationVariable> variables) throws ValueMappingException {
        return variables.stream()
                .map(LambdaExceptionHelper.rethrowFunction(
                        x -> ElementValueMapper.<SubmodelElement, ElementValue> toValue(x.getValue())))
                .collect(Collectors.toList());
    }

}
