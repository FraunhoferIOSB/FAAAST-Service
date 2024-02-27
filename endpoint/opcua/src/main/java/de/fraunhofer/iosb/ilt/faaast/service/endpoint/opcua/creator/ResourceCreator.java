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
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.UaHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import opc.i4aas.objecttypes.AASResourceType;
import org.eclipse.digitaltwin.aas4j.v3.model.Resource;


/**
 * Helper class to create AAS Resources and integrate them into the
 * OPC UA address space.
 */
public class ResourceCreator {

    /**
     * Sonar wants a private constructor.
     */
    private ResourceCreator() {}


    /**
     * Adds an AAS Resource to the given Node.
     *
     * @param node The UA node in which the SpecificAssetId should be created
     * @param aasResource The desired AAS Resource.
     * @param name The desired name.
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     */
    public static void addAasResource(UaNode node, Resource aasResource, String name, NodeManagerUaNode nodeManager) throws StatusException, ValueFormatException {
        if ((node != null) && (aasResource != null)) {
            NodeId nodeId = new NodeId(nodeManager.getNamespaceIndex(), node.getNodeId().getValue().toString() + "." + name);
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASResourceType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
            AASResourceType resourceNode = nodeManager.createInstance(AASResourceType.class, nodeId, browseName, LocalizedText.english(name));

            setResourceData(aasResource, resourceNode, nodeManager);
            node.addComponent(resourceNode);
        }
    }


    private static void setResourceData(Resource aasResource, AASResourceType resourceNode, NodeManagerUaNode nodeManager) throws StatusException, ValueFormatException {
        if (!aasResource.getContentType().isEmpty()) {
            if (resourceNode.getContentTypeNode() == null) {
                UaHelper.addStringUaProperty(resourceNode, nodeManager, AASResourceType.CONTENT_TYPE, aasResource.getContentType(),
                        opc.i4aas.ObjectTypeIds.AASResourceType.getNamespaceUri());
            }
            else {
                resourceNode.setContentType(aasResource.getContentType());
            }
        }

        if (aasResource.getPath() != null) {
            if (resourceNode.getPathNode() == null) {
                UaHelper.addStringUaProperty(resourceNode, nodeManager, AASResourceType.PATH, aasResource.getPath(), opc.i4aas.ObjectTypeIds.AASResourceType.getNamespaceUri());
            }
            else {
                resourceNode.setPath(aasResource.getPath());
            }
        }
    }
}
