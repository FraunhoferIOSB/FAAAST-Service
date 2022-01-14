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
package de.fraunhofer.iosb.ilt.faaast.service.requesthandlers;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationFinishEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationInvokeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.Reference;
import java.util.List;


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
     * Processes a request and returns the resulting response
     * 
     * @param request the request
     * @return the response
     */
    public abstract O process(I request);


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
    };


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

    };


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

    };


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

    };


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
    };


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
    };


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
    };

}
