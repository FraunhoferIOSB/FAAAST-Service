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
import com.prosysopc.ua.stack.core.AccessLevelType;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import java.util.List;
import opc.i4aas.objecttypes.AASEntityType;
import opc.i4aas.objecttypes.AASReferenceType;
import opc.i4aas.objecttypes.AASSpecificAssetIdType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create IdentifierKeyValuePairs and integrate them into the
 * OPC UA address space.
 */
public class SpecificAssetIdCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpecificAssetIdCreator.class);

    private SpecificAssetIdCreator() {
        throw new IllegalStateException("Class not instantiable");
    }


    /**
     * Adds a list of SpecificAssetIds to the given Node.
     * 
     * @param node The UA node in which the SpecificAssetId should be created
     * @param specificAssetIds The desired list of SpecificAssetIds
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addSpecificAssetIdList(UaNode node, List<SpecificAssetId> specificAssetIds, AasServiceNodeManager nodeManager) throws StatusException {

        int index = 1;
        for (var specificAssetId: specificAssetIds) {
            String name = String.format("%s %d", AASEntityType.SPECIFIC_ASSET_ID, index);
            if ((specificAssetId.getName() != null) && (!specificAssetId.getName().isEmpty())) {
                name = specificAssetId.getName();
            }

            addSpecificAssetId(node, specificAssetId, name, AasServiceNodeManager.VALUES_READ_ONLY, nodeManager);
        }
    }


    /**
     * Adds a SpecificAssetId to the given Node.
     *
     * @param node The UA node in which the SpecificAssetId should be created
     * @param specificAssetId The desired SpecificAssetId
     * @param name The desired name of the SpecificAssetId node
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addSpecificAssetId(UaNode node, SpecificAssetId specificAssetId, String name, AasServiceNodeManager nodeManager) throws StatusException {
        addSpecificAssetId(node, specificAssetId, name, AasServiceNodeManager.VALUES_READ_ONLY, nodeManager);
    }


    /**
     * Sets the data for the given IdentifierKeyValuePair Node from the corresponding AAS object.
     * 
     * @param specificAssetIdNode The desired SpecificAssetId Node
     * @param aasIdentifierPair The corresponding AAS IdentifierKeyValuePair
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void setSpecificAssetIdData(AASSpecificAssetIdType specificAssetIdNode, SpecificAssetId aasIdentifierPair, AasServiceNodeManager nodeManager)
            throws StatusException {
        setSpecificAssetIdData(specificAssetIdNode, aasIdentifierPair, AasServiceNodeManager.VALUES_READ_ONLY, nodeManager);
    }


    /**
     * Adds an SpecificAssetId to the given Node.
     *
     * @param node The UA node in which the SpecificAssetId should be created
     * @param specificAssetId The desired SpecificAssetId
     * @param name The desired name of the SpecificAssetId node
     * @param readOnly True if the value should be read-only
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    private static void addSpecificAssetId(UaNode node, SpecificAssetId specificAssetId, String name, boolean readOnly, AasServiceNodeManager nodeManager)
            throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException(AasServiceNodeManager.NODE_NULL);
        }
        else if (specificAssetId == null) {
            throw new IllegalArgumentException("specificAssetId = null");
        }

        LOGGER.debug("addSpecificAssetId {}; to Node: {}", name, node);
        QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASSpecificAssetIdType.getNamespaceUri(), name)
                .toQualifiedName(nodeManager.getNamespaceTable());
        NodeId nid = nodeManager.createNodeId(node, browseName);
        AASSpecificAssetIdType specificAssetIdNode = nodeManager.createInstance(AASSpecificAssetIdType.class, nid, browseName, LocalizedText.english(name));

        setSpecificAssetIdData(specificAssetIdNode, specificAssetId, readOnly, nodeManager);

        node.addComponent(specificAssetIdNode);
    }


    /**
     * Sets the data for the given IdentifierKeyValuePair Node from the corresponding AAS object.
     * 
     * @param specificAssetIdNode The desired IdentifierKeyValuePair Node
     * @param aasIdentifierPair The corresponding AAS IdentifierKeyValuePair
     * @param readOnly True if the value should be read-only
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    private static void setSpecificAssetIdData(AASSpecificAssetIdType specificAssetIdNode, SpecificAssetId aasIdentifierPair, boolean readOnly,
                                               AasServiceNodeManager nodeManager)
            throws StatusException {
        // ExternalSubjectId
        Reference externalSubjectId = aasIdentifierPair.getExternalSubjectId();
        if (externalSubjectId != null) {
            AASReferenceType extSubjectNode = specificAssetIdNode.getExternalSubjectIdNode();
            if (extSubjectNode == null) {
                AasReferenceCreator.addAasReferenceAasNS(specificAssetIdNode, externalSubjectId, AASSpecificAssetIdType.EXTERNAL_SUBJECT_ID, nodeManager);
            }
            else {
                AasReferenceCreator.setAasReferenceData(externalSubjectId, extSubjectNode);
            }
        }

        // Key
        specificAssetIdNode.setName(aasIdentifierPair.getName());

        // Value
        specificAssetIdNode.setValue(aasIdentifierPair.getValue());

        if (readOnly) {
            specificAssetIdNode.getNameNode().setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
            specificAssetIdNode.getValueNode().setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
        }
    }

}
