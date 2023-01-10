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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.AasSubmodelElementHelper;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.Reference;
import opc.i4aas.AASIdentifierKeyValuePairType;
import opc.i4aas.AASReferenceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create IdentifierKeyValuePairs and integrate them into the
 * OPC UA address space.
 */
public class IdentifierKeyValuePairCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierKeyValuePairCreator.class);

    private IdentifierKeyValuePairCreator() {
        throw new IllegalStateException("Class not instantiable");
    }


    /**
     * Adds an IdentifierKeyValuePair to the given Node.
     *
     * @param node The UA node in which the IdentifierKeyValuePair should be created
     * @param identifierPair The desired IdentifierKeyValuePair
     * @param name The desired name of the IdentifierKeyValuePair node
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addIdentifierKeyValuePair(UaNode node, IdentifierKeyValuePair identifierPair, String name, AasServiceNodeManager nodeManager) throws StatusException {
        addIdentifierKeyValuePair(node, identifierPair, name, AasServiceNodeManager.VALUES_READ_ONLY, nodeManager);
    }


    /**
     * Sets the data for the given IdentifierKeyValuePair Node from the corresponding AAS object.
     * 
     * @param identifierPairNode The desired IdentifierKeyValuePair Node
     * @param aasIdentifierPair The corresponding AAS IdentifierKeyValuePair
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void setIdentifierKeyValuePairData(AASIdentifierKeyValuePairType identifierPairNode, IdentifierKeyValuePair aasIdentifierPair, AasServiceNodeManager nodeManager)
            throws StatusException {
        setIdentifierKeyValuePairData(identifierPairNode, aasIdentifierPair, AasServiceNodeManager.VALUES_READ_ONLY, nodeManager);
    }


    /**
     * Adds an IdentifierKeyValuePair to the given Node.
     *
     * @param node The UA node in which the IdentifierKeyValuePair should be created
     * @param identifierPair The desired IdentifierKeyValuePair
     * @param name The desired name of the IdentifierKeyValuePair node
     * @param readOnly True if the value should be read-only
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    private static void addIdentifierKeyValuePair(UaNode node, IdentifierKeyValuePair identifierPair, String name, boolean readOnly, AasServiceNodeManager nodeManager)
            throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException(AasServiceNodeManager.NODE_NULL);
        }
        else if (identifierPair == null) {
            throw new IllegalArgumentException("identifierPair = null");
        }

        LOGGER.debug("addIdentifierKeyValuePair {}; to Node: {}", name, node);
        QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASIdentifierKeyValuePairType.getNamespaceUri(), name)
                .toQualifiedName(nodeManager.getNamespaceTable());
        NodeId nid = nodeManager.createNodeId(node, browseName);
        AASIdentifierKeyValuePairType identifierPairNode = nodeManager.createInstance(AASIdentifierKeyValuePairType.class, nid, browseName, LocalizedText.english(name));

        setIdentifierKeyValuePairData(identifierPairNode, identifierPair, readOnly, nodeManager);

        node.addComponent(identifierPairNode);
    }


    /**
     * Sets the data for the given IdentifierKeyValuePair Node from the corresponding AAS object.
     * 
     * @param identifierPairNode The desired IdentifierKeyValuePair Node
     * @param aasIdentifierPair The corresponding AAS IdentifierKeyValuePair
     * @param readOnly True if the value should be read-only
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    private static void setIdentifierKeyValuePairData(AASIdentifierKeyValuePairType identifierPairNode, IdentifierKeyValuePair aasIdentifierPair, boolean readOnly,
                                                      AasServiceNodeManager nodeManager)
            throws StatusException {
        // ExternalSubjectId
        Reference externalSubjectId = aasIdentifierPair.getExternalSubjectId();
        if (externalSubjectId != null) {
            AASReferenceType extSubjectNode = identifierPairNode.getExternalSubjectIdNode();
            if (extSubjectNode == null) {
                AasReferenceCreator.addAasReferenceAasNS(identifierPairNode, externalSubjectId, AASIdentifierKeyValuePairType.EXTERNAL_SUBJECT_ID, nodeManager);
            }
            else {
                AasSubmodelElementHelper.setAasReferenceData(externalSubjectId, extSubjectNode);
            }
        }

        // Key
        identifierPairNode.setKey(aasIdentifierPair.getKey());

        // Value
        identifierPairNode.setValue(aasIdentifierPair.getValue());

        if (readOnly) {
            identifierPairNode.getKeyNode().setAccessLevel(AccessLevelType.CurrentRead);
            identifierPairNode.getValueNode().setAccessLevel(AccessLevelType.CurrentRead);
        }
    }

}
