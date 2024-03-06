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
import opc.i4aas.objecttypes.AASCapabilityType;
import org.eclipse.digitaltwin.aas4j.v3.model.Capability;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create Capabilities and integrate them into the
 * OPC UA address space.
 */
public class CapabilityCreator extends SubmodelElementCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CapabilityCreator.class);

    /**
     * Adds an AAS Capability to the given node.
     *
     * @param node The desired UA node
     * @param aasCapability The corresponding AAS Capability to add
     * @param capabilityRef The AAS reference to the AAS Capability
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param ordered Specifies whether the capability should be added ordered
     *            (true) or unordered (false)
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addAasCapability(UaNode node, Capability aasCapability, Reference capabilityRef, Submodel submodel, boolean ordered, AasServiceNodeManager nodeManager)
            throws StatusException {
        try {
            if ((node != null) && (aasCapability != null)) {
                String name = aasCapability.getIdShort();
                if ((name == null) || name.isEmpty()) {
                    name = getNameFromReference(capabilityRef);
                }
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASCapabilityType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
                NodeId nid = nodeManager.getDefaultNodeId();
                AASCapabilityType capabilityNode = nodeManager.createInstance(AASCapabilityType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(capabilityNode, aasCapability, nodeManager);

                if (ordered) {
                    node.addReference(capabilityNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(capabilityNode);
                }

                nodeManager.addReferable(capabilityRef, new ObjectData(aasCapability, capabilityNode, submodel));
            }
        }
        catch (Exception ex) {
            LOGGER.error("addAasCapability Exception", ex);
        }
    }

}
