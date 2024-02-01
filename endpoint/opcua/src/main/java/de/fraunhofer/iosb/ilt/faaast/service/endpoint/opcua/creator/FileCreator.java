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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.AasSubmodelElementHelper;
import opc.i4aas.objecttypes.AASFileType;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
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
     * @param fileRef The AAS reference to the AAS file
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param ordered Specifies whether the file should be added ordered (true) or unordered (false)
     * @param nodeName The desired Name of the node. If this value is not set,
     *            the IdShort of the file is used.
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addAasFile(UaNode node, File aasFile, Reference fileRef, Submodel submodel, boolean ordered, String nodeName, AasServiceNodeManager nodeManager)
            throws StatusException {
        try {
            if ((node != null) && (aasFile != null)) {
                String name = aasFile.getIdShort();
                if ((nodeName != null) && (!nodeName.isEmpty())) {
                    name = nodeName;
                }
                if ((name == null) || name.isEmpty()) {
                    name = getNameFromReference(fileRef);
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

                if (fileRef != null) {
                    nodeManager.addReferable(fileRef, new ObjectData(aasFile, fileNode, submodel));
                }
            }
        }
        catch (Exception ex) {
            LOGGER.error("addAasFile Exception", ex);
        }
    }


    private static void setFileData(File aasFile, AASFileType fileNode, AasServiceNodeManager nodeManager) throws StatusException {
        // ContentType
        if (!aasFile.getContentType().isEmpty()) {
            fileNode.setContentType(aasFile.getContentType());
        }

        // Value
        if (aasFile.getValue() != null) {
            setValueData(fileNode, aasFile, nodeManager);
        }

        if (VALUES_READ_ONLY) {
            fileNode.getContentTypeNode().setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
        }
    }


    private static void setValueData(AASFileType fileNode, File aasFile, AasServiceNodeManager nodeManager) throws StatusException {
        if (fileNode.getValueNode() == null) {
            AasSubmodelElementHelper.addFileValueNode(fileNode, nodeManager);
        }

        fileNode.setValue(aasFile.getValue());
    }

}
