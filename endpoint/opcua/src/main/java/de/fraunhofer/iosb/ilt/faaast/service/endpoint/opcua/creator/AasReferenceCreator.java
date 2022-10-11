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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.AasSubmodelElementHelper;
import io.adminshell.aas.v3.model.Reference;
import java.util.List;
import opc.i4aas.AASReferenceList;
import opc.i4aas.AASReferenceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create AAS References and integrate then into
 * the OPC UA address space.
 */
public class AasReferenceCreator {
    /**
     * Text if node is null
     */
    private static final String NODE_NULL = "node is null";

    private static final Logger LOGGER = LoggerFactory.getLogger(AasReferenceCreator.class);

    /**
     * Creates a node with the given name and adds the given list of references.
     *
     * @param node The UA node in which the list of references should be created
     * @param list The desired list of references
     * @param name The desired name of the Node
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addAasReferenceList(UaNode node, List<Reference> list, String name, AasServiceNodeManager nodeManager) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException(NODE_NULL);
        }
        else if (list == null) {
            throw new IllegalArgumentException("list = null");
        }

        try {
            LOGGER.debug("addAasReferenceList {}; to Node: {}", name, node);
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASReferenceList.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.getDefaultNodeId();
            AASReferenceList referenceListNode = nodeManager.createInstance(AASReferenceList.class, nid, browseName, LocalizedText.english(name));

            int counter = 1;
            for (Reference ref: list) {
                addAasReferenceAasNS(referenceListNode, ref, name + counter++, nodeManager);
            }

            node.addComponent(referenceListNode);
        }
        catch (Exception ex) {
            LOGGER.error("addAasReferenceList Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS Reference to the given node with the AAS namespace (read-only).
     *
     * @param node The node in which the object is created
     * @param ref The desired AAS reference object to add
     * @param name The desired name
     * @param nodeManager The corresponding Node Manager
     * @return The created node
     * @throws StatusException If the operation fails
     */
    public static UaNode addAasReferenceAasNS(UaNode node, Reference ref, String name, AasServiceNodeManager nodeManager) throws StatusException {
        return addAasReferenceAasNS(node, ref, name, true, nodeManager);
    }


    /**
     * Adds an AAS Reference to the given node with the AAS namespace.
     *
     * @param node The node in which the object is created
     * @param ref The desired AAS reference object to add
     * @param name The desired name
     * @param readOnly True if the value should be read-only
     * @param nodeManager The corresponding Node Manager
     * @return The created node
     * @throws StatusException If the operation fails
     */
    public static UaNode addAasReferenceAasNS(UaNode node, Reference ref, String name, boolean readOnly, AasServiceNodeManager nodeManager) throws StatusException {
        UaNode retval = null;

        try {
            retval = addAasReference(node, ref, name, opc.i4aas.ObjectTypeIds.AASReferenceType.getNamespaceUri(), readOnly, nodeManager);
        }
        catch (Exception ex) {
            LOGGER.error("addAasReferenceAasNS Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Adds an AAS Reference to the given node with the given namespace.
     *
     * @param node The node in which the object is created
     * @param ref The desired AAS reference object to add
     * @param name The desired name
     * @param namespaceUri The desired namespace URI tu use
     * @param readOnly True if the value should be read-only
     * @param nodeManager The corresponding Node Manager
     * @return The created node
     * @throws StatusException If the operation fails
     */
    public static UaNode addAasReference(UaNode node, Reference ref, String name, String namespaceUri, boolean readOnly, AasServiceNodeManager nodeManager) throws StatusException {
        UaNode retval = null;

        try {
            if (ref != null) {
                QualifiedName browseName = UaQualifiedName.from(namespaceUri, name).toQualifiedName(nodeManager.getNamespaceTable());
                NodeId nid = nodeManager.getDefaultNodeId();
                AASReferenceType nodeRef = nodeManager.createInstance(AASReferenceType.class, nid, browseName, LocalizedText.english(name));

                LOGGER.debug("addAasReference: add Node {} to Node {}", nid, node.getNodeId());

                AasSubmodelElementHelper.setAasReferenceData(ref, nodeRef, readOnly);

                node.addComponent(nodeRef);

                retval = nodeRef;
            }
        }
        catch (Exception ex) {
            LOGGER.error("addAasReference Exception", ex);
            throw ex;
        }

        return retval;
    }

}
