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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.ArrayList;
import java.util.List;
import opc.ua.aas.datatypes.AASKey;
import opc.ua.aas.datatypes.AASReference;
import opc.ua.aas.variabletypes.AASReferenceElementType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create AAS References and integrate then into
 * the OPC UA address space.
 */
public class AasReferenceCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AasReferenceCreator.class);

    private AasReferenceCreator() {
        throw new IllegalStateException("Class not instantiable");
    }

    /**
     * Creates a node with the given name and adds the given list of references.
     *
     * @param node The UA node in which the list of references should be created
     * @param list The desired list of references
     * @param name The desired name of the Node
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    //    public static void addAasReferenceListNode(UaNode node, List<Reference> list, String name, AasServiceNodeManager nodeManager) throws StatusException {
    //        if (node == null) {
    //            throw new IllegalArgumentException(AasServiceNodeManager.NODE_NULL);
    //        }
    //        else if (list == null) {
    //            throw new IllegalArgumentException("list = null");
    //        }
    //
    //        LOGGER.debug("addAasReferenceList {}; to Node: {}", name, node);
    //        QualifiedName browseName = UaQualifiedName.from(ObjectTypeIds.AASReferenceList.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
    //        NodeId nid = nodeManager.getDefaultNodeId();
    //        AASReferenceList referenceListNode = nodeManager.createInstance(AASReferenceList.class, nid, browseName, LocalizedText.english(name));
    //
    //        addAasReferencesToList(referenceListNode, list, name, nodeManager);
    //
    //        node.addComponent(referenceListNode);
    //    }

    /**
     * Adds a given list of references to the desired node.
     * 
     * @param referenceListNode The node where the references are added
     * @param list The list of references
     * @param name The desired name
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    //    public static void addAasReferencesToList(AASReferenceList referenceListNode, List<Reference> list, String name, AasServiceNodeManager nodeManager) throws StatusException {
    //        int counter = 1;
    //        for (Reference ref: list) {
    //            addAasReferenceAasNS(referenceListNode, ref, name + counter++, nodeManager);
    //        }
    //    }

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
    //public static UaNode addAasReferenceAasNS(UaNode node, Reference ref, String name, AasServiceNodeManager nodeManager) throws StatusException {
    //    return addAasReferenceAasNS(node, ref, name, true, nodeManager);
    //}

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
    //public static UaNode addAasReferenceAasNS(UaNode node, Reference ref, String name, boolean readOnly, AasServiceNodeManager nodeManager) throws StatusException {
    //    UaNode retval = addAasReference(node, ref, name, opc.ua.aas.DataTypeIds.AASReference.getNamespaceUri(), readOnly, nodeManager);

    //    return retval;
    //}

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
    //    public static UaNode addAasReference(UaNode node, Reference ref, String name, String namespaceUri, boolean readOnly, AasServiceNodeManager nodeManager) throws StatusException {
    //        UaNode retval = null;
    //
    //        if (ref != null) {
    //            QualifiedName browseName = UaQualifiedName.from(namespaceUri, name).toQualifiedName(nodeManager.getNamespaceTable());
    //            NodeId nid = nodeManager.getDefaultNodeId();
    //            //AASReferenceType nodeRef = nodeManager.createInstance(AASReferenceType.class, nid, browseName, LocalizedText.english(name));
    //
    //            LOGGER.debug("addAasReference: add Node {} to Node {}", nid, node.getNodeId());
    //
    //            setAasReferenceData(ref, nodeRef, readOnly);
    //
    //            node.addComponent(nodeRef);
    //
    //            retval = nodeRef;
    //        }
    //
    //        return retval;
    //    }


    /**
     * Sets the data in the given Reference node.
     *
     * @param ref The desired UA reference object
     * @param refNode The AAS Reference object with the source data
     * @throws StatusException If the operation fails
     */
    public static void setAasReferenceData(Reference ref, AASReferenceElementType refNode) throws StatusException {
        if (ref == null) {
            return;
        }
        LOGGER.info("setAasReferenceData: TODO");
        AASReference aasref = new AASReference();
        setAasReferenceData(ref, aasref);
        refNode.setValue(aasref);
    }


    /**
     * Sets the data in the given Reference node.
     *
     * @param ref The desired UA reference object
     * @param refNode The AAS Reference object with the source data
     * @throws StatusException If the operation fails
     */
    private static void setAasReferenceData(Reference ref, AASReference refNode) throws StatusException {
        Ensure.requireNonNull(refNode, "refNode must be non-null");
        Ensure.requireNonNull(ref, "ref must be non-null");

        AASKey[] keys = ref.getKeys().stream().map(k -> {
            AASKey keyValue = new AASKey();
            keyValue.setType(ValueConverter.getAasKeyTypesDataType(k.getType()));
            keyValue.setValue(k.getValue());
            return keyValue;
        }).toArray(AASKey[]::new);

        //refNode.getKeysNode().setArrayDimensions(new UnsignedInteger[] {
        //        UnsignedInteger.valueOf(keys.length)
        //});
        //if (readOnly) {
        //    refNode.getKeysNode().setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
        //}

        refNode.setType(ValueConverter.convertReferenceTypes(ref.getType()));
        refNode.setKey(keys);
    }


    /**
     * Gets an OPC UA reference from an AAS reference.
     *
     * @param ref The desired AAS reference.
     * @return The corresponding OPC UA reference.
     * @throws StatusException If the operation fails
     */
    public static AASReference getAasReference(Reference ref) throws StatusException {
        if (ref == null) {
            return null;
        }
        AASReference referenceNode = new AASReference();
        setAasReferenceData(ref, referenceNode);
        return referenceNode;
    }


    /**
     * Gets a list of OPC UA references from a list of AAS references.
     *
     * @param refs The desired list of AAS references.
     * @return The corresponding list of OPC UA references.
     * @throws StatusException If the operation fails
     */
    public static List<AASReference> getAasReferences(List<Reference> refs) throws StatusException {
        List<AASReference> retval = new ArrayList<>();
        for (Reference ref: refs) {
            retval.add(getAasReference(ref));
        }
        return retval;
    }
}
