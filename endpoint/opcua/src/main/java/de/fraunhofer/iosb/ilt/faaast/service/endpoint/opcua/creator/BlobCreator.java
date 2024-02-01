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
import com.prosysopc.ua.stack.builtintypes.ByteString;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.Identifiers;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.AasSubmodelElementHelper;
import opc.i4aas.objecttypes.AASBlobType;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create Blobs and integrate them into the
 * OPC UA address space.
 */
public class BlobCreator extends SubmodelElementCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlobCreator.class);

    /**
     * Adds an AAS Blob to the given UA node.
     *
     * @param node The desired UA node
     * @param aasBlob The AAS blob to add
     * @param blobRef Tne reference to the AAS blob
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param ordered Specifies whether the blob should be added ordered (true)
     *            or unordered (false)
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addAasBlob(UaNode node, Blob aasBlob, Reference blobRef, Submodel submodel, boolean ordered, AasServiceNodeManager nodeManager)
            throws StatusException {
        try {
            if ((node != null) && (aasBlob != null)) {
                String name = aasBlob.getIdShort();
                if ((name == null) || name.isEmpty()) {
                    name = getNameFromReference(blobRef);
                }
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASBlobType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
                NodeId nid = nodeManager.getDefaultNodeId();
                AASBlobType blobNode = nodeManager.createInstance(AASBlobType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(blobNode, aasBlob, nodeManager);

                // ContentType
                blobNode.setContentType(aasBlob.getContentType());

                setValue(aasBlob, blobNode, nodeManager, submodel, blobRef);

                if (AasServiceNodeManager.VALUES_READ_ONLY) {
                    blobNode.getContentTypeNode().setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
                }

                if (ordered) {
                    node.addReference(blobNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(blobNode);
                }

                if (blobRef != null) {
                    nodeManager.addReferable(blobRef, new ObjectData(aasBlob, blobNode, submodel));
                }
            }
        }
        catch (Exception ex) {
            LOGGER.error("addAasBlob Exception", ex);
        }
    }


    private static void setValue(Blob aasBlob, AASBlobType blobNode, AasServiceNodeManager nodeManager, Submodel submodel, Reference blobRef) throws StatusException {
        // Value
        if (aasBlob.getValue() != null) {
            if (blobNode.getValueNode() == null) {
                AasSubmodelElementHelper.addBlobValueNode(blobNode, nodeManager);
            }

            nodeManager.addSubmodelElementAasMap(blobNode.getValueNode().getNodeId(),
                    new SubmodelElementData(aasBlob, submodel, SubmodelElementData.Type.BLOB_VALUE, blobRef));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("addAasBlob: NodeId {}; Blob: {}", blobNode.getValueNode().getNodeId(), aasBlob.getIdShort());
            }

            if (blobRef != null) {
                nodeManager.addSubmodelElementOpcUA(blobRef, blobNode);
            }

            blobNode.setValue(ByteString.valueOf(aasBlob.getValue()));
        }
    }

}
