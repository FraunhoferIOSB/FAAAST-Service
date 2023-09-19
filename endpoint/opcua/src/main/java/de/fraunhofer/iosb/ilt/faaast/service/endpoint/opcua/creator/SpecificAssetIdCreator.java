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
import opc.i4aas.AASEntityType;
import opc.i4aas.AASReferenceType;
import opc.i4aas.AASSpecificAssetIDType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetID;
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

    //    /**
    //     * Adds an IdentifierKeyValuePair to the given Node.
    //     *
    //     * @param node The UA node in which the IdentifierKeyValuePair should be created
    //     * @param identifierPair The desired IdentifierKeyValuePair
    //     * @param name The desired name of the IdentifierKeyValuePair node
    //     * @param nodeManager The corresponding Node Manager
    //     * @throws StatusException If the operation fails
    //     */
    //    public static void addIdentifierKeyValuePair(UaNode node, SpecificAssetID identifierPair, String name, AasServiceNodeManager nodeManager) throws StatusException {
    //        addIdentifierKeyValuePair(node, identifierPair, name, AasServiceNodeManager.VALUES_READ_ONLY, nodeManager);
    //    }


    /**
     * Adds a list of SpecificAssetIDs to the given Node.
     * 
     * @param node The UA node in which the SpecificAssetID should be created
     * @param specificAssetIDs The desired list of SpecificAssetIDs
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addSpecificAssetIDList(UaNode node, List<SpecificAssetID> specificAssetIDs, AasServiceNodeManager nodeManager) throws StatusException {

        int index = 1;
        for (var specificAssetID: specificAssetIDs) {
            String name = String.format("%s %d", AASEntityType.SPECIFIC_ASSET_ID, index);
            if ((specificAssetID.getName() != null) && (!specificAssetID.getName().isEmpty())) {
                name = specificAssetID.getName();
            }

            addSpecificAssetID(node, specificAssetID, name, AasServiceNodeManager.VALUES_READ_ONLY, nodeManager);
        }
    }


    /**
     * Adds a SpecificAssetID to the given Node.
     *
     * @param node The UA node in which the SpecificAssetID should be created
     * @param specificAssetID The desired SpecificAssetID
     * @param name The desired name of the SpecificAssetID node
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addSpecificAssetID(UaNode node, SpecificAssetID specificAssetID, String name, AasServiceNodeManager nodeManager) throws StatusException {
        addSpecificAssetID(node, specificAssetID, name, AasServiceNodeManager.VALUES_READ_ONLY, nodeManager);
    }

    ////    /**
    ////     * Sets the data for the given IdentifierKeyValuePair Node from the corresponding AAS object.
    ////     * 
    ////     * @param identifierPairNode The desired IdentifierKeyValuePair Node
    ////     * @param aasIdentifierPair The corresponding AAS IdentifierKeyValuePair
    ////     * @param nodeManager The corresponding Node Manager
    ////     * @throws StatusException If the operation fails
    ////     */
    ////    public static void setIdentifierKeyValuePairData(AASIdentifierKeyValuePairType identifierPairNode, SpecificAssetID aasIdentifierPair, AasServiceNodeManager nodeManager)
    ////            throws StatusException {
    ////        setIdentifierKeyValuePairData(identifierPairNode, aasIdentifierPair, AasServiceNodeManager.VALUES_READ_ONLY, nodeManager);
    ////    }


    /**
     * Sets the data for the given IdentifierKeyValuePair Node from the corresponding AAS object.
     * 
     * @param specificAssetIDNode The desired SpecificAssetID Node
     * @param aasIdentifierPair The corresponding AAS IdentifierKeyValuePair
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void setSpecificAssetIDData(AASSpecificAssetIDType specificAssetIDNode, SpecificAssetID aasIdentifierPair, AasServiceNodeManager nodeManager)
            throws StatusException {
        setSpecificAssetIDData(specificAssetIDNode, aasIdentifierPair, AasServiceNodeManager.VALUES_READ_ONLY, nodeManager);
    }

    //    /**
    //     * Adds an IdentifierKeyValuePair to the given Node.
    //     *
    //     * @param node The UA node in which the IdentifierKeyValuePair should be created
    //     * @param identifierPair The desired IdentifierKeyValuePair
    //     * @param name The desired name of the IdentifierKeyValuePair node
    //     * @param readOnly True if the value should be read-only
    //     * @param nodeManager The corresponding Node Manager
    //     * @throws StatusException If the operation fails
    //     */
    //    private static void addIdentifierKeyValuePair(UaNode node, SpecificAssetID identifierPair, String name, boolean readOnly, AasServiceNodeManager nodeManager)
    //            throws StatusException {
    //        if (node == null) {
    //            throw new IllegalArgumentException(AasServiceNodeManager.NODE_NULL);
    //        }
    //        else if (identifierPair == null) {
    //            throw new IllegalArgumentException("identifierPair = null");
    //        }
    //
    //        LOGGER.debug("addIdentifierKeyValuePair {}; to Node: {}", name, node);
    //        QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASIdentifierKeyValuePairType.getNamespaceUri(), name)
    //                .toQualifiedName(nodeManager.getNamespaceTable());
    //        NodeId nid = nodeManager.createNodeId(node, browseName);
    //        AASIdentifierKeyValuePairType identifierPairNode = nodeManager.createInstance(AASIdentifierKeyValuePairType.class, nid, browseName, LocalizedText.english(name));
    //
    //        setIdentifierKeyValuePairData(identifierPairNode, identifierPair, readOnly, nodeManager);
    //
    //        node.addComponent(identifierPairNode);
    //    }


    /**
     * Adds an SpecificAssetID to the given Node.
     *
     * @param node The UA node in which the SpecificAssetID should be created
     * @param specificAssetID The desired SpecificAssetID
     * @param name The desired name of the SpecificAssetID node
     * @param readOnly True if the value should be read-only
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    private static void addSpecificAssetID(UaNode node, SpecificAssetID specificAssetID, String name, boolean readOnly, AasServiceNodeManager nodeManager)
            throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException(AasServiceNodeManager.NODE_NULL);
        }
        else if (specificAssetID == null) {
            throw new IllegalArgumentException("specificAssetID = null");
        }

        LOGGER.debug("addSpecificAssetID {}; to Node: {}", name, node);
        QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASSpecificAssetIDType.getNamespaceUri(), name)
                .toQualifiedName(nodeManager.getNamespaceTable());
        NodeId nid = nodeManager.createNodeId(node, browseName);
        AASSpecificAssetIDType specificAssetIDNode = nodeManager.createInstance(AASSpecificAssetIDType.class, nid, browseName, LocalizedText.english(name));

        setSpecificAssetIDData(specificAssetIDNode, specificAssetID, readOnly, nodeManager);

        node.addComponent(specificAssetIDNode);
    }

    ////    /**
    ////     * Sets the data for the given IdentifierKeyValuePair Node from the corresponding AAS object.
    ////     * 
    ////     * @param identifierPairNode The desired IdentifierKeyValuePair Node
    ////     * @param aasIdentifierPair The corresponding AAS IdentifierKeyValuePair
    ////     * @param readOnly True if the value should be read-only
    ////     * @param nodeManager The corresponding Node Manager
    ////     * @throws StatusException If the operation fails
    ////     */
    ////    private static void setIdentifierKeyValuePairData(AASIdentifierKeyValuePairType identifierPairNode, SpecificAssetID aasIdentifierPair, boolean readOnly,
    ////                                                      AasServiceNodeManager nodeManager)
    ////            throws StatusException {
    ////        // ExternalSubjectId
    ////        Reference externalSubjectId = aasIdentifierPair.getExternalSubjectID();
    ////        if (externalSubjectId != null) {
    ////            AASReferenceType extSubjectNode = identifierPairNode.getExternalSubjectIdNode();
    ////            if (extSubjectNode == null) {
    ////                AasReferenceCreator.addAasReferenceAasNS(identifierPairNode, externalSubjectId, AASIdentifierKeyValuePairType.EXTERNAL_SUBJECT_ID, nodeManager);
    ////            }
    ////            else {
    ////                AasSubmodelElementHelper.setAasReferenceData(externalSubjectId, extSubjectNode);
    ////            }
    ////        }
    ////
    ////        // Key
    ////        identifierPairNode.setKey(aasIdentifierPair.getName());
    ////
    ////        // Value
    ////        identifierPairNode.setValue(aasIdentifierPair.getValue());
    ////
    ////        if (readOnly) {
    ////            identifierPairNode.getKeyNode().setAccessLevel(AccessLevelType.CurrentRead);
    ////            identifierPairNode.getValueNode().setAccessLevel(AccessLevelType.CurrentRead);
    ////        }
    ////    }


    /**
     * Sets the data for the given IdentifierKeyValuePair Node from the corresponding AAS object.
     * 
     * @param specificAssetIDNode The desired IdentifierKeyValuePair Node
     * @param aasIdentifierPair The corresponding AAS IdentifierKeyValuePair
     * @param readOnly True if the value should be read-only
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    private static void setSpecificAssetIDData(AASSpecificAssetIDType specificAssetIDNode, SpecificAssetID aasIdentifierPair, boolean readOnly,
                                               AasServiceNodeManager nodeManager)
            throws StatusException {
        // ExternalSubjectId
        Reference externalSubjectId = aasIdentifierPair.getExternalSubjectID();
        if (externalSubjectId != null) {
            AASReferenceType extSubjectNode = specificAssetIDNode.getExternalSubjectIdNode();
            if (extSubjectNode == null) {
                AasReferenceCreator.addAasReferenceAasNS(specificAssetIDNode, externalSubjectId, AASSpecificAssetIDType.EXTERNAL_SUBJECT_ID, nodeManager);
            }
            else {
                AasReferenceCreator.setAasReferenceData(externalSubjectId, extSubjectNode);
            }
        }

        // Key
        specificAssetIDNode.setName(aasIdentifierPair.getName());

        // Value
        specificAssetIDNode.setValue(aasIdentifierPair.getValue());

        if (readOnly) {
            specificAssetIDNode.getNameNode().setAccessLevel(AccessLevelType.CurrentRead);
            specificAssetIDNode.getValueNode().setAccessLevel(AccessLevelType.CurrentRead);
        }
    }

}
