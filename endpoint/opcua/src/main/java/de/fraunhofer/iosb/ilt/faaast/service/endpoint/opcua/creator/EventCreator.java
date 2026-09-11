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
import com.prosysopc.ua.server.instantiation.NodeBuilder;
import com.prosysopc.ua.server.instantiation.NodeBuilderConfiguration;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import opc.ua.iosb.aas.Ids;
import opc.ua.iosb.aas.objecttypes.AASBasicEventElementType;
import org.eclipse.digitaltwin.aas4j.v3.model.BasicEventElement;
import org.eclipse.digitaltwin.aas4j.v3.model.EventElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create Events and integrate them into the
 * OPC UA address space.
 */
public class EventCreator extends SubmodelElementCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventCreator.class);

    /**
     * Adds an AAS EventElement to the given node.
     *
     * @param aasEvent The AAS Event to add
     * @param eventRef The AAS reference to the event
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param nodeManager The corresponding Node Manager
     * @return The created node.
     */
    public static UaNode createAasEvent(EventElement aasEvent, Reference eventRef, Submodel submodel, AasServiceNodeManager nodeManager) {
        UaNode retval = null;
        if (aasEvent instanceof BasicEventElement basicEventElement) {
            retval = createAasBasicEventElement(basicEventElement, eventRef, submodel, nodeManager);
        }
        return retval;
    }


    /**
     * Adds an AAS BasicEventElement to the given node.
     *
     * @param aasEvent The AAS Event to add
     * @param eventRef The AAS reference to the event
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param nodeManager The corresponding Node Manager
     * @return The created node.
     * @throws StatusException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     */
    private static UaNode createAasBasicEventElement(BasicEventElement aasEvent, Reference eventRef, Submodel submodel, AasServiceNodeManager nodeManager) {
        UaNode retval = null;
        try {
            String name = aasEvent.getIdShort();
            if ((name == null) || name.isEmpty()) {
                name = getNameFromReference(eventRef);
            }
            QualifiedName browseName = UaQualifiedName.from(Ids.AASBasicEventElementType.getNamespaceUri(), name)
                    .toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.getDefaultNodeId();

            NodeBuilderConfiguration conf = new NodeBuilderConfiguration();
            if (aasEvent.getMessageTopic() != null) {
                conf.addOptional(Ids.AASBasicEventElementType_MessageTopic);
            }
            if (aasEvent.getMessageBroker() != null) {
                conf.addOptional(Ids.AASBasicEventElementType_MessageBroker);
            }
            if (aasEvent.getLastUpdate() != null) {
                conf.addOptional(Ids.AASBasicEventElementType_LastUpdate);
            }
            if (aasEvent.getMinInterval() != null) {
                conf.addOptional(Ids.AASBasicEventElementType_MinInterval);
            }
            if (aasEvent.getMaxInterval() != null) {
                conf.addOptional(Ids.AASBasicEventElementType_MaxInterval);
            }
            NodeBuilder<AASBasicEventElementType> nb = nodeManager.createNodeBuilder(AASBasicEventElementType.class, conf);
            nb.setBrowseName(browseName);
            nb.setDisplayName(LocalizedText.english(name));
            nb.setNodeId(nid);
            AASBasicEventElementType eventNode = nb.build();

            addSubmodelElementBaseData(eventNode, aasEvent, nodeManager);

            setBasicEventElementData(eventNode, aasEvent);

            if (eventRef != null) {
                nodeManager.addSubmodelElementOpcUA(eventRef, eventNode);
            }

            //nodeManager.addSubmodelElementAasMap(nid, new SubmodelElementData(aasEvent, submodel, SubmodelElementData.Type.PROPERTY_VALUE, eventRef));

            nodeManager.addReferable(eventRef, new ObjectData(aasEvent, eventNode, submodel));
            retval = eventNode;

            //
            //            //if (ordered) {
            //            //    node.addReference(eventNode, Identifiers.HasOrderedComponent, false);
            //            //}
            //            //else {
            //            //    node.addComponent(eventNode);
            //            //}
            //
            //            nodeManager.addReferable(eventRef, new ObjectData(aasEvent, eventNode, submodel));
        }
        catch (Exception ex) {
            LOGGER.error("createAasBasicEventElement Exception", ex);
        }
        return retval;
    }


    private static void setBasicEventElementData(AASBasicEventElementType eventNode, BasicEventElement aasEvent)
            throws StatusException, ValueFormatException {
        if (aasEvent.getObserved() != null) {
            eventNode.setObserved(ReferenceCreator.getAasReference(aasEvent.getObserved()));
        }

        if (aasEvent.getDirection() != null) {
            eventNode.setDirection(ValueConverter.getAasDirectionDataType(aasEvent.getDirection()));
        }

        if (aasEvent.getState() != null) {
            eventNode.setState(ValueConverter.getAasStateOfEventType(aasEvent.getState()));
        }

        //String namespaceUri = Ids.AASBasicEventElementType.getNamespaceUri();
        setMessageTopic(aasEvent.getMessageTopic(), eventNode);
        setMessageBroker(aasEvent.getMessageBroker(), eventNode);
        setLastUpdate(aasEvent.getLastUpdate(), eventNode);
        setMinInterval(aasEvent.getMinInterval(), eventNode);
        setMaxInterval(aasEvent.getMaxInterval(), eventNode);
    }


    private static void setMaxInterval(String maxInterval, AASBasicEventElementType eventNode)
            throws ValueFormatException, StatusException {
        if (maxInterval != null) {
            //if (eventNode.getMaxIntervalNode() == null) {
            //    UaHelper.addStringUaProperty(eventNode, nodeManager, AASBasicEventElementType.MAX_INTERVAL, maxInterval,
            //            namespaceUri);
            //}
            //else {
            eventNode.setMessageTopic(maxInterval);
            //}
        }
    }


    private static void setMinInterval(String minInterval, AASBasicEventElementType eventNode)
            throws StatusException, ValueFormatException {
        if (minInterval != null) {
            //if (eventNode.getMinIntervalNode() == null) {
            //    UaHelper.addStringUaProperty(eventNode, nodeManager, AASBasicEventElementType.MIN_INTERVAL, minInterval,
            //            namespaceUri);
            //}
            //else {
            eventNode.setMessageTopic(minInterval);
            //}
        }
    }


    private static void setLastUpdate(String lastUpdate, AASBasicEventElementType eventNode)
            throws StatusException, ValueFormatException {
        if (lastUpdate != null) {
            //if (eventNode.getLastUpdateNode() == null) {
            //    UaHelper.addStringUaProperty(eventNode, nodeManager, AASBasicEventElementType.LAST_UPDATE, lastUpdate, namespaceUri);
            //}
            //else {
            eventNode.setMessageTopic(lastUpdate);
            //}
        }
    }


    private static void setMessageBroker(Reference messageBroker, AASBasicEventElementType eventNode)
            throws StatusException {
        if (messageBroker != null) {
            //if (eventNode.getMessageBrokerNode() == null) {
            //    AasReferenceCreator.addAasReference(eventNode, messageBroker, AASBasicEventElementType.MESSAGE_BROKER,
            //            namespaceUri, false,
            //            nodeManager);
            //}
            //else {
            eventNode.setMessageBroker(ReferenceCreator.getAasReference(messageBroker));
            //}
        }
    }


    private static void setMessageTopic(String messageTopic, AASBasicEventElementType eventNode)
            throws StatusException, ValueFormatException {
        if (messageTopic != null) {
            //if (eventNode.getMessageTopicNode() == null) {
            //    UaHelper.addStringUaProperty(eventNode, nodeManager, AASBasicEventElementType.MESSAGE_TOPIC, messageTopic, namespaceUri);
            //}
            //else {
            eventNode.setMessageTopic(messageTopic);
            //}
        }
    }

}
