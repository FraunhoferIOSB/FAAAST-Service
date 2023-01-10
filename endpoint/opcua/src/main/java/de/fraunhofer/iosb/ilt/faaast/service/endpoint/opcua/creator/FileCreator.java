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

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager.VALUES_READ_ONLY;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaQualifiedName;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.types.opcua.server.FileTypeNode;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.AasSubmodelElementHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.File;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import opc.i4aas.AASFileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create AAS files and integrate them into the
 * OPC UA address space.
 */
public class FileCreator extends SubmodelElementCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileCreator.class);

    /**
     * Adds an AAS file to the given node.
     *
     * @param node The desired UA node
     * @param aasFile The AAS file object
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The AAS reference to the parent node
     * @param ordered Specifies whether the file should be added ordered (true) or unordered (false)
     * @param nodeName The desired Name of the node. If this value is not set,
     *            the IdShort of the file is used.
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addAasFile(UaNode node, File aasFile, Submodel submodel, Reference parentRef, boolean ordered, String nodeName, AasServiceNodeManager nodeManager)
            throws StatusException {
        if ((node != null) && (aasFile != null)) {
            String name = aasFile.getIdShort();
            if ((nodeName != null) && (!nodeName.isEmpty())) {
                name = nodeName;
            }

            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASFileType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.getDefaultNodeId();
            AASFileType fileNode = nodeManager.createInstance(AASFileType.class, nid, browseName, LocalizedText.english(name));
            addSubmodelElementBaseData(fileNode, aasFile, nodeManager);

            setFileData(aasFile, fileNode, nodeManager);

            if (ordered) {
                node.addReference(fileNode, Identifiers.HasOrderedComponent, false);
            }
            else {
                node.addComponent(fileNode);
            }

            if (parentRef != null) {
                Reference fileRef = AasUtils.toReference(parentRef, aasFile);

                nodeManager.addReferable(fileRef, new ObjectData(aasFile, fileNode, submodel));
            }
        }
    }


    private static void setFileData(File aasFile, AASFileType fileNode, AasServiceNodeManager nodeManager) throws StatusException {
        // MimeType
        if (!aasFile.getMimeType().isEmpty()) {
            fileNode.setMimeType(aasFile.getMimeType());
        }

        // Value
        if (aasFile.getValue() != null) {
            setValueData(fileNode, aasFile, nodeManager);
        }

        if (VALUES_READ_ONLY) {
            fileNode.getMimeTypeNode().setAccessLevel(AccessLevelType.CurrentRead);
        }
    }


    private static void setValueData(AASFileType fileNode, File aasFile, AasServiceNodeManager nodeManager) throws StatusException {
        if (fileNode.getValueNode() == null) {
            AasSubmodelElementHelper.addFileValueNode(fileNode, nodeManager);
        }

        fileNode.setValue(aasFile.getValue());

        if (!aasFile.getValue().isEmpty()) {
            java.io.File f = new java.io.File(aasFile.getValue());
            if (!f.exists()) {
                LOGGER.warn("addAasFile: File '{}' does not exist!", f.getAbsolutePath());
            }
            else {
                // File Object: include only when the file exists
                QualifiedName fileBrowseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASFileType.getNamespaceUri(), AASFileType.FILE)
                        .toQualifiedName(nodeManager.getNamespaceTable());
                NodeId fileId = new NodeId(nodeManager.getNamespaceIndex(), fileNode.getNodeId().getValue().toString() + "." + AASFileType.FILE);
                FileTypeNode fileType = nodeManager.createInstance(FileTypeNode.class, fileId, fileBrowseName, LocalizedText.english(AASFileType.FILE));
                fileType.setFile(new java.io.File(aasFile.getValue()));
                fileType.setWritable(false);
                fileType.setUserWritable(false);
                if (fileType.getNodeVersion() != null) {
                    fileType.getNodeVersion().setDescription(new LocalizedText("", ""));
                }

                fileNode.addReference(fileType, Identifiers.HasAddIn, false);
            }
        }
    }

}
