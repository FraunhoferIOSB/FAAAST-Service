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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.AasSubmodelElementHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.ReferenceElement;
import io.adminshell.aas.v3.model.Submodel;
import opc.i4aas.AASReferenceElementType;
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
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The reference to the parent object
     * @param ordered Specifies whether the reference element should be added
     *            ordered (true) or unordered (false)
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addAasReferenceElement(UaNode node, ReferenceElement aasRefElem, Submodel submodel, Reference parentRef, boolean ordered, AasServiceNodeManager nodeManager)
            throws StatusException {
        try {
            if ((node != null) && (aasRefElem != null)) {
                String name = aasRefElem.getIdShort();
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASReferenceElementType.getNamespaceUri(), name)
                        .toQualifiedName(nodeManager.getNamespaceTable());
                NodeId nid = nodeManager.getDefaultNodeId();
                AASReferenceElementType refElemNode = nodeManager.createInstance(AASReferenceElementType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(refElemNode, aasRefElem, nodeManager);

                if (aasRefElem.getValue() != null) {
                    AasSubmodelElementHelper.setAasReferenceData(aasRefElem.getValue(), refElemNode.getValueNode(), false);
                }

                Reference refElemRef = AasUtils.toReference(parentRef, aasRefElem);

                nodeManager.addSubmodelElementAasMap(refElemNode.getValueNode().getKeysNode().getNodeId(),
                        new SubmodelElementData(aasRefElem, submodel, SubmodelElementData.Type.REFERENCE_ELEMENT_VALUE, refElemRef));

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
            throw ex;
        }
    }

}
