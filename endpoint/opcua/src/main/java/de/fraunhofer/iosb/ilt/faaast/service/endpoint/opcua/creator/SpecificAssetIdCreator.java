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
import com.prosysopc.ua.ValueRanks;
import com.prosysopc.ua.types.opcua.BaseDataVariableType;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import java.util.ArrayList;
import java.util.List;
import opc.ua.aas.datatypes.AASSpecificAssetId;
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
    public static void addSpecificAssetIdList(BaseDataVariableType node, List<SpecificAssetId> specificAssetIds, AasServiceNodeManager nodeManager) throws StatusException {

        //int index = 1;
        List<AASSpecificAssetId> list = new ArrayList<>();
        for (var specificAssetId: specificAssetIds) {
            //String name = String.format("%s %d", AASEntityType.SPECIFIC_ASSET_ID, index);
            //if ((specificAssetId.getName() != null) && (!specificAssetId.getName().isEmpty())) {
            //    name = specificAssetId.getName();
            //}

            LOGGER.debug("addSpecificAssetIdList {}; to Node: {}", specificAssetId.getName(), node);
            AASSpecificAssetId specificAssetIdNode = getSpecificAssetId(specificAssetId);
            list.add(specificAssetIdNode);
            //addSpecificAssetId(node, specificAssetId, name, AasServiceNodeManager.VALUES_READ_ONLY, nodeManager);
        }

        if (list.size() == 1) {
            node.setValue(list.get(0));
            node.setValueRank(ValueRanks.Scalar);
        }
        else if (list.size() > 1) {
            node.setValue(list.toArray());
            node.setValueRank(ValueRanks.OneDimension);
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
    //public static void addSpecificAssetId(BaseDataVariableType node, SpecificAssetId specificAssetId, String name, AasServiceNodeManager nodeManager) throws StatusException {
    //    addSpecificAssetId(node, specificAssetId, name, AasServiceNodeManager.VALUES_READ_ONLY, nodeManager);
    //}

    /**
     * Sets the data for the given IdentifierKeyValuePair Node from the corresponding AAS object.
     * 
     * @param specificAssetIdNode The desired SpecificAssetId Node
     * @param aasIdentifierPair The corresponding AAS IdentifierKeyValuePair
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    //public static void setSpecificAssetIdData(AASSpecificAssetId specificAssetIdNode, SpecificAssetId aasIdentifierPair, AasServiceNodeManager nodeManager)
    //        throws StatusException {
    //    setSpecificAssetIdData(specificAssetIdNode, aasIdentifierPair, AasServiceNodeManager.VALUES_READ_ONLY, nodeManager);
    //}

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
    //private static void addSpecificAssetId(BaseDataVariableType node, SpecificAssetId specificAssetId, String name, boolean readOnly, AasServiceNodeManager nodeManager)
    //        throws StatusException {
    //    if (node == null) {
    //        throw new IllegalArgumentException(AasServiceNodeManager.NODE_NULL);
    //    }
    //    else if (specificAssetId == null) {
    //        throw new IllegalArgumentException("specificAssetId = null");
    //    }

    //    LOGGER.debug("addSpecificAssetId {}; to Node: {}", name, node);
    //    //QualifiedName browseName = UaQualifiedName.from(opc.ua.aas.DataTypeIds.AASSpecificAssetId.getNamespaceUri(), name)
    //    //        .toQualifiedName(nodeManager.getNamespaceTable());
    //    //NodeId nid = nodeManager.createNodeId(node, browseName);
    //    AASSpecificAssetId specificAssetIdNode = new AASSpecificAssetId();
    //    //AASSpecificAssetId specificAssetIdNode = nodeManager.createInstance(AASSpecificAssetId.class, nid, browseName, LocalizedText.english(name));

    //    setSpecificAssetIdData(specificAssetIdNode, specificAssetId, readOnly, nodeManager);

    //    //node.setValue(name);
    //    node.addComponent(specificAssetIdNode);
    //}


    /**
     * Sets the data for the given IdentifierKeyValuePair Node from the corresponding AAS object.
     * 
     * @param specificAssetIdNode The desired IdentifierKeyValuePair Node
     * @param aasIdentifierPair The corresponding AAS IdentifierKeyValuePair
     * @param readOnly True if the value should be read-only
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    private static void setSpecificAssetIdData(AASSpecificAssetId specificAssetIdNode, SpecificAssetId aasIdentifierPair, boolean readOnly,
                                               AasServiceNodeManager nodeManager)
            throws StatusException {
        // ExternalSubjectId
        Reference externalSubjectId = aasIdentifierPair.getExternalSubjectId();
        if (externalSubjectId != null) {
            specificAssetIdNode.setExternalSubjectId(AasReferenceCreator.getAasReference(externalSubjectId));
            //AASReference extSubjectNode = specificAssetIdNode.getExternalSubjectId();
            //if (extSubjectNode == null) {
            //    AasReferenceCreator.addAasReferenceAasNS(specificAssetIdNode, externalSubjectId, AASSpecificAssetIdType.EXTERNAL_SUBJECT_ID, nodeManager);
            //}
            //else {
            //AasReferenceCreator.setAasReferenceData(externalSubjectId, extSubjectNode);
            //}
        }

        // Key
        specificAssetIdNode.setName(aasIdentifierPair.getName());

        // Value
        specificAssetIdNode.setValue(aasIdentifierPair.getValue());

        //if (readOnly) {
        //    specificAssetIdNode.getNameNode().setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
        //    specificAssetIdNode.getValueNode().setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
        //}
    }


    private static AASSpecificAssetId getSpecificAssetId(SpecificAssetId aasIdentifierPair) throws StatusException {

        AASSpecificAssetId specificAssetIdNode = new AASSpecificAssetId();
        setSpecificAssetIdData(specificAssetIdNode, aasIdentifierPair, false, null);
        return specificAssetIdNode;
    }
}
