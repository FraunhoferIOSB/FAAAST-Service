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
import com.prosysopc.ua.server.nodes.PlainProperty;
import com.prosysopc.ua.stack.builtintypes.ByteString;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.Identifiers;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import io.adminshell.aas.v3.model.Constraint;
import io.adminshell.aas.v3.model.Qualifier;
import java.util.List;
import opc.i4aas.AASQualifierList;
import opc.i4aas.AASQualifierType;
import opc.i4aas.AASSubmodelElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create Qualifier and integrate them into the
 * OPC UA address space.
 */
public class QualifierCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(QualifierCreator.class);

    /**
     * Adds a QualifierNode to the given Node.
     *
     * @param node The desired base node
     * @param nodeManager The corresponding Node Manager
     */
    public static void addQualifierNode(UaNode node, AasServiceNodeManager nodeManager) {
        try {
            String name = AASSubmodelElementType.QUALIFIER;
            LOGGER.debug("addQualifierNode {}; to Node: {}", name, node);
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASQualifierList.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.createNodeId(node, browseName);
            AASQualifierList listNode = nodeManager.createInstance(AASQualifierList.class, nid, browseName, LocalizedText.english(name));

            node.addComponent(listNode);
        }
        catch (Exception ex) {
            LOGGER.error("addQualifierNode Exception", ex);
        }
    }


    /**
     * Adds a list of Qualifiers to the given Node.
     *
     * @param listNode The UA node in which the Qualifiers should be created
     * @param qualifiers The desired list of Qualifiers
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addQualifiers(AASQualifierList listNode, List<Constraint> qualifiers, AasServiceNodeManager nodeManager) throws StatusException {
        if (listNode == null) {
            throw new IllegalArgumentException("listNode = null");
        }
        else if (qualifiers == null) {
            throw new IllegalArgumentException("qualifiers = null");
        }

        try {

            int index = 1;
            for (Constraint constraint: qualifiers) {
                if ((constraint != null) && (Qualifier.class.isAssignableFrom(constraint.getClass()))) {
                    addQualifier(listNode, (Qualifier) constraint, "Qualifier " + index, nodeManager);
                }

                index++;
            }
        }
        catch (Exception ex) {
            LOGGER.error("addQualifiers Exception", ex);
            throw ex;
        }
    }


    /**
     * Creates and adds a Qualifier to the given Node.
     *
     * @param node The UA node in which the Qualifier should be created
     * @param qualifier The desired Qualifier
     * @param name The name of the qualifier
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addQualifier(UaNode node, Qualifier qualifier, String name, AasServiceNodeManager nodeManager) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException(AasServiceNodeManager.NODE_NULL);
        }
        else if (qualifier == null) {
            throw new IllegalArgumentException("qualifier = null");
        }

        try {
            LOGGER.debug("addQualifier {}; to Node: {}", name, node);
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASQualifierType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.createNodeId(node, browseName);
            AASQualifierType qualifierNode = nodeManager.createInstance(AASQualifierType.class, nid, browseName, LocalizedText.english(name));

            // Type
            qualifierNode.setType(qualifier.getType());

            // ValueType
            qualifierNode.setValueType(ValueConverter.stringToValueType(qualifier.getValueType()));

            // Value
            if (qualifier.getValue() != null) {
                if (qualifierNode.getValueNode() == null) {
                    addQualifierValueNode(qualifierNode, nodeManager);
                }

                qualifierNode.setValue(qualifier.getValue());
            }

            // ValueId
            if (qualifier.getValueId() != null) {
                AasReferenceCreator.addAasReferenceAasNS(qualifierNode, qualifier.getValueId(), AASQualifierType.VALUE_ID, nodeManager);
            }

            if (AasServiceNodeManager.VALUES_READ_ONLY) {
                if (qualifierNode.getValueNode() != null) {
                    qualifierNode.getValueNode().setAccessLevel(AccessLevelType.CurrentRead);
                }
                if (qualifierNode.getValueTypeNode() != null) {
                    qualifierNode.getValueTypeNode().setAccessLevel(AccessLevelType.CurrentRead);
                }
                if (qualifierNode.getTypeNode() != null) {
                    qualifierNode.getTypeNode().setAccessLevel(AccessLevelType.CurrentRead);
                }
            }

            node.addComponent(qualifierNode);
        }
        catch (Exception ex) {
            LOGGER.error("addQualifier Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds a Value Property to the given Qualifier Node.
     *
     * @param node The desired Blob Node
     * @param nodeManager The corresponding Node Manager
     */
    private static void addQualifierValueNode(UaNode node, AasServiceNodeManager nodeManager) {
        try {
            NodeId myPropertyId = new NodeId(nodeManager.getNamespaceIndex(), node.getNodeId().getValue().toString() + "." + AASQualifierType.VALUE);
            PlainProperty<ByteString> myProperty = new PlainProperty<>(nodeManager, myPropertyId,
                    UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASQualifierType.getNamespaceUri(), AASQualifierType.VALUE).toQualifiedName(nodeManager.getNamespaceTable()),
                    LocalizedText.english(AASQualifierType.VALUE));
            myProperty.setDataTypeId(Identifiers.String);
            if (VALUES_READ_ONLY) {
                myProperty.setAccessLevel(AccessLevelType.CurrentRead);
            }
            myProperty.setDescription(new LocalizedText("", ""));
            node.addProperty(myProperty);
        }
        catch (Exception ex) {
            LOGGER.error("addQualifierValueNode Exception", ex);
            throw ex;
        }
    }

}
