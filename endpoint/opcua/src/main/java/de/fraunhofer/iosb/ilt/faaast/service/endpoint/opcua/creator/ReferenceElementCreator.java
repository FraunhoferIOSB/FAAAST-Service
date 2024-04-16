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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import opc.i4aas.objecttypes.AASReferenceElementType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create ReferenceElements and integrate them into the
 * OPC UA address space.
 */
public class ReferenceElementCreator extends SubmodelElementCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceElementCreator.class);

    /**
     * Adds an AAS reference element to the given node.
     *
     * @param node The desired UA node
     * @param aasRefElem The AAS reference element to add
     * @param refElemRef The reference to the AAS reference element
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param ordered Specifies whether the reference element should be added
     *            ordered (true) or unordered (false)
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addAasReferenceElement(UaNode node, ReferenceElement aasRefElem, Reference refElemRef, Submodel submodel, boolean ordered, AasServiceNodeManager nodeManager)
            throws StatusException {
        try {
            if ((node != null) && (aasRefElem != null)) {
                String name = aasRefElem.getIdShort();
                if ((name == null) || name.isEmpty()) {
                    name = getNameFromReference(refElemRef);
                }
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASReferenceElementType.getNamespaceUri(), name)
                        .toQualifiedName(nodeManager.getNamespaceTable());
                NodeId nid = nodeManager.getDefaultNodeId();
                AASReferenceElementType refElemNode = nodeManager.createInstance(AASReferenceElementType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(refElemNode, aasRefElem, nodeManager);

                setValue(aasRefElem, refElemNode, nodeManager);

                if (refElemNode.getValueNode() != null) {
                    nodeManager.addSubmodelElementAasMap(refElemNode.getValueNode().getKeysNode().getNodeId(),
                            new SubmodelElementData(aasRefElem, submodel, SubmodelElementData.Type.REFERENCE_ELEMENT_VALUE, refElemRef));
                }

                nodeManager.addSubmodelElementOpcUA(refElemRef, refElemNode);

                if (ordered) {
                    node.addReference(refElemNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(refElemNode);
                }

                nodeManager.addReferable(refElemRef, new ObjectData(aasRefElem, refElemNode, submodel));
            }
        }
        catch (Exception ex) {
            LOGGER.error("addAasReferenceElement Exception", ex);
        }
    }


    private static void setValue(ReferenceElement aasRefElem, AASReferenceElementType refElemNode, AasServiceNodeManager nodeManager) throws StatusException {
        if (aasRefElem.getValue() != null) {
            if (refElemNode.getValueNode() == null) {
                AasReferenceCreator.addAasReference(refElemNode, aasRefElem.getValue(), AASReferenceElementType.VALUE,
                        opc.i4aas.ObjectTypeIds.AASReferenceElementType.getNamespaceUri(), false,
                        nodeManager);
            }
            else {
                AasReferenceCreator.setAasReferenceData(aasRefElem.getValue(), refElemNode.getValueNode(), false);
            }
        }
    }

}
