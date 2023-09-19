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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaQualifiedName;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.core.Identifiers;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.UaHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import opc.i4aas.AASBasicEventElementType;
import org.eclipse.digitaltwin.aas4j.v3.model.BasicEventElement;
import org.eclipse.digitaltwin.aas4j.v3.model.EventElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;


/**
 * Helper class to create Events and integrate them into the
 * OPC UA address space.
 */
public class EventCreator extends SubmodelElementCreator {
    //private static final Logger LOGGER = LoggerFactory.getLogger(EventCreator.class);

    /**
     * Adds an AAS EventElement to the given node.
     *
     * @param node The desired UA node
     * @param aasEvent The AAS Event to add
     * @param eventRef The AAS reference to the event
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param ordered Specifies whether the entity should be added ordered
     *            (true) or unordered (false)
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     */
    public static void addAasEvent(UaNode node, EventElement aasEvent, Reference eventRef, Submodel submodel, boolean ordered, AasServiceNodeManager nodeManager)
            throws StatusException, ValueFormatException {
        if ((node != null) && (aasEvent != null)) {
            if (aasEvent instanceof BasicEventElement) {
                addAasBasicEventElement(node, (BasicEventElement) aasEvent, eventRef, submodel, ordered, nodeManager);
            }
        }
    }


    /**
     * Adds an AAS BasicEventElement to the given node.
     *
     * @param node The desired UA node
     * @param aasEvent The AAS Event to add
     * @param eventRef The AAS reference to the event
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param ordered Specifies whether the entity should be added ordered
     *            (true) or unordered (false)
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     */
    private static void addAasBasicEventElement(UaNode node, BasicEventElement aasEvent, Reference eventRef, Submodel submodel, boolean ordered, AasServiceNodeManager nodeManager)
            throws StatusException, ValueFormatException {
        String name = aasEvent.getIdShort();
        QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASBasicEventElementType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
        NodeId nid = nodeManager.getDefaultNodeId();
        AASBasicEventElementType eventNode = nodeManager.createInstance(AASBasicEventElementType.class, nid, browseName, LocalizedText.english(name));
        addSubmodelElementBaseData(eventNode, aasEvent, nodeManager);

        setBasicEventElementData(eventNode, (BasicEventElement) aasEvent, nodeManager);

        //Reference eventRef = AasUtils.toReference(parentRef, aasEvent);

        if (ordered) {
            node.addReference(eventNode, Identifiers.HasOrderedComponent, false);
        }
        else {
            node.addComponent(eventNode);
        }

        nodeManager.addReferable(eventRef, new ObjectData(aasEvent, eventNode, submodel));
    }


    private static void setBasicEventElementData(AASBasicEventElementType eventNode, BasicEventElement aasEvent, AasServiceNodeManager nodeManager)
            throws StatusException, ValueFormatException {
        if (aasEvent.getObserved() != null) {
            AasReferenceCreator.setAasReferenceData(aasEvent.getObserved(), eventNode.getObservedNode(), true);
        }

        if (aasEvent.getDirection() != null) {
            eventNode.setDirection(ValueConverter.getAasDirectionDataType(aasEvent.getDirection()));
        }

        if (aasEvent.getState() != null) {
            eventNode.setState(ValueConverter.getAasStateOfEventType(aasEvent.getState()));
        }

        String namespaceUri = opc.i4aas.ObjectTypeIds.AASBasicEventElementType.getNamespaceUri();
        if (aasEvent.getMessageTopic() != null) {
            if (eventNode.getMessageTopicNode() == null) {
                UaHelper.addStringUaProperty(eventNode, nodeManager, AASBasicEventElementType.MESSAGE_TOPIC, aasEvent.getMessageTopic(),
                        namespaceUri);
            }
            else {
                eventNode.setMessageTopic(aasEvent.getMessageTopic());
            }
        }

        if (aasEvent.getMessageBroker() != null) {
            if (eventNode.getMessageBrokerNode() == null) {
                AasReferenceCreator.addAasReference(eventNode, aasEvent.getMessageBroker(), AASBasicEventElementType.MESSAGE_BROKER,
                        namespaceUri, false,
                        nodeManager);
            }
            else {
                AasReferenceCreator.setAasReferenceData(aasEvent.getMessageBroker(), eventNode.getMessageBrokerNode(), true);
            }
        }

        if (aasEvent.getLastUpdate() != null) {
            if (eventNode.getLastUpdateNode() == null) {
                UaHelper.addStringUaProperty(eventNode, nodeManager, AASBasicEventElementType.LAST_UPDATE, aasEvent.getLastUpdate(),
                        namespaceUri);
            }
            else {
                eventNode.setMessageTopic(aasEvent.getLastUpdate());
            }
        }

        if (aasEvent.getMinInterval() != null) {
            if (eventNode.getMinIntervalNode() == null) {
                UaHelper.addStringUaProperty(eventNode, nodeManager, AASBasicEventElementType.MIN_INTERVAL, aasEvent.getMinInterval(),
                        namespaceUri);
            }
            else {
                eventNode.setMessageTopic(aasEvent.getMinInterval());
            }
        }

        if (aasEvent.getMaxInterval() != null) {
            if (eventNode.getMaxIntervalNode() == null) {
                UaHelper.addStringUaProperty(eventNode, nodeManager, AASBasicEventElementType.MAX_INTERVAL, aasEvent.getMaxInterval(),
                        namespaceUri);
            }
            else {
                eventNode.setMessageTopic(aasEvent.getMaxInterval());
            }
        }
    }

}
