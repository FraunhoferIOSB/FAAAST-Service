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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.BasicEvent;
import io.adminshell.aas.v3.model.Event;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import opc.i4aas.AASEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create Events and integrate them into the
 * OPC UA address space.
 */
public class EventCreator extends SubmodelElementCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventCreator.class);

    /**
     * Adds an AAS Event to the given node.
     *
     * @param node The desired UA node
     * @param aasEvent The AAS Event to add
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The AAS reference to the parent object
     * @param ordered Specifies whether the entity should be added ordered
     *            (true) or unordered (false)
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addAasEvent(UaNode node, Event aasEvent, Submodel submodel, Reference parentRef, boolean ordered, AasServiceNodeManager nodeManager)
            throws StatusException {
        if ((node != null) && (aasEvent != null)) {
            String name = aasEvent.getIdShort();
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASEventType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.getDefaultNodeId();
            AASEventType eventNode = nodeManager.createInstance(AASEventType.class, nid, browseName, LocalizedText.english(name));
            addSubmodelElementBaseData(eventNode, aasEvent, nodeManager);

            if (aasEvent instanceof BasicEvent) {
                setBasicEventData(eventNode, (BasicEvent) aasEvent);
            }

            Reference eventRef = AasUtils.toReference(parentRef, aasEvent);

            if (ordered) {
                node.addReference(eventNode, Identifiers.HasOrderedComponent, false);
            }
            else {
                node.addComponent(eventNode);
            }

            nodeManager.addReferable(eventRef, new ObjectData(aasEvent, eventNode, submodel));
        }
    }


    /**
     * Sets the Basic event data.
     *
     * @param eventNode The desired UA event node
     * @param aasEvent The corresponding AAS BasicEvent
     */
    private static void setBasicEventData(AASEventType eventNode, BasicEvent aasEvent) {
        if (aasEvent.getObserved() != null) {
            LOGGER.warn("setBasicEventData: not implemented! Event: {}", eventNode.getBrowseName().getName());
        }
    }

}
