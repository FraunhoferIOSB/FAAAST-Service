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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import java.util.ArrayList;
import java.util.List;
import opc.ua.aas.datatypes.AASQualifiable;
import opc.ua.aas.datatypes.AASQualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create Qualifier and integrate them into the
 * OPC UA address space.
 */
public class QualifierCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(QualifierCreator.class);

    private QualifierCreator() {
        throw new IllegalStateException("Class not instantiable");
    }

    /**
     * Adds a QualifierNode to the given Node.
     *
     * @param node The desired base node
     * @param nodeManager The corresponding Node Manager
     */
    //public static void addQualifierNode(UaNode node, AasServiceNodeManager nodeManager) {
    //    String name = AASSubmodelElementType.QUALIFIER;
    //    LOGGER.debug("addQualifierNode {}; to Node: {}", name, node);
    //    QualifiedName browseName = UaQualifiedName.from(ObjectTypeIds.AASQualifierList.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
    //    NodeId nid = nodeManager.createNodeId(node, browseName);
    //    AASQualifierList listNode = nodeManager.createInstance(AASQualifierList.class, nid, browseName, LocalizedText.english(name));

    //    node.addComponent(listNode);
    //}


    /**
     * Adds a list of Qualifiers to the given Node.
     *
     * @param opcQualifiable The UA node in which the Qualifiers should be created
     * @param qualifiers The desired list of Qualifiers
     * @throws StatusException If the operation fails
     */
    public static void addQualifiers(AASQualifiable opcQualifiable, List<Qualifier> qualifiers) throws StatusException {
        if (opcQualifiable == null) {
            throw new IllegalArgumentException("listNode = null");
        }
        else if (qualifiers == null) {
            throw new IllegalArgumentException("qualifiers = null");
        }

        LOGGER.info("addQualifiers:; add {} qualifiers", qualifiers.size());
        List<AASQualifier> opcQualifiers = new ArrayList<>();
        int index = 1;
        for (Qualifier qualifier: qualifiers) {
            if (qualifier != null) {
                opcQualifiers.add(getQualifier(qualifier));
            }

            index++;
        }
        opcQualifiable.setQualifier(opcQualifiers.toArray(AASQualifier[]::new));
    }


    /**
     * Creates and adds a Qualifier to the given Node.
     *
     * @param qualifierNode The UA Qualifier node
     * @param qualifier The desired Qualifier
     * @throws StatusException If the operation fails
     */
    public static void setQualifierData(AASQualifier qualifierNode, Qualifier qualifier) throws StatusException {
        if (qualifierNode == null) {
            throw new IllegalArgumentException(AasServiceNodeManager.NODE_NULL);
        }
        else if (qualifier == null) {
            throw new IllegalArgumentException("qualifier = null");
        }

        //LOGGER.debug("setQualifierData {}; OPC UA Node: {}", name, qualifierNode);
        //QualifiedName browseName = UaQualifiedName.from(ObjectTypeIds.AASQualifierType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
        //NodeId nid = nodeManager.createNodeId(node, browseName);
        //AASQualifierType qualifierNode = nodeManager.createInstance(AASQualifierType.class, nid, browseName, LocalizedText.english(name));

        if (qualifier.getKind() != null) {
            qualifierNode.setKind(ValueConverter.convertQualifierKind(qualifier.getKind()));
            //if (qualifierNode.getKindNode() == null) {
            //    UaHelper.addQualifierKindProperty(qualifierNode, nodeManager, AASQualifierType.KIND, qualifier.getKind(),
            //            ObjectTypeIds.AASQualifierType.getNamespaceUri());
            //}
            //else {
            //    qualifierNode.setKind(ValueConverter.convertQualifierKind(qualifier.getKind()));
            //}
        }

        // SemanticId
        qualifierNode.setHasSemantics(BaseDataCreator.getHasSemantics(qualifier));
        //if (qualifier.getSemanticId() != null) {
        //    if (qualifierNode.getHasSemantics() == null) {
        //        qualifierNode.setHasSemantics(new AASHasSemantics());
        //    }
        //    qualifierNode.getHasSemantics().setSemanticId(AasReferenceCreator.getAasReference(qualifier.getSemanticId()));
        //}

        //if (qualifier.getSupplementalSemanticIds() != null) {
        //    if (qualifierNode.getHasSemantics() == null) {
        //        qualifierNode.setHasSemantics(new AASHasSemantics());
        //    }
        //    List<AASReference> refs = AasReferenceCreator.getAasReferences(qualifier.getSupplementalSemanticIds());
        //    qualifierNode.getHasSemantics().setSupplementalSemanticId(refs.toArray(AASReference[]::new));
        //}

        // Type
        qualifierNode.setType(qualifier.getType());

        // ValueType
        qualifierNode.setValueType(ValueConverter.convertDataTypeDefToString(qualifier.getValueType()));

        // Value
        qualifierNode.setValue(qualifier.getValue());
        //setValue(qualifier.getValue(), qualifierNode, nodeManager);

        // ValueId
        //if (qualifier.getValueId() != null) {
        //AasReferenceCreator.addAasReferenceAasNS(qualifierNode, qualifier.getValueId(), AASQualifierType.VALUE_ID, nodeManager);
        qualifierNode.setValueId(ReferenceCreator.getAasReference(qualifier.getValueId()));
        //}

        //setAccessRights(qualifierNode);

        //node.addComponent(qualifierNode);
    }

    //    private static void setAccessRights(AASQualifierType qualifierNode) {
    //        if (AasServiceNodeManager.VALUES_READ_ONLY) {
    //            if (qualifierNode.getValueNode() != null) {
    //                qualifierNode.getValueNode().setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
    //            }
    //            if (qualifierNode.getValueTypeNode() != null) {
    //                qualifierNode.getValueTypeNode().setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
    //            }
    //            if (qualifierNode.getTypeNode() != null) {
    //                qualifierNode.getTypeNode().setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
    //            }
    //        }
    //    }

    //    private static void setValue(String value, AASQualifierType qualifierNode, AasServiceNodeManager nodeManager) throws StatusException {
    //        if (value != null) {
    //            if (qualifierNode.getValueNode() == null) {
    //                addQualifierValueNode(qualifierNode, nodeManager);
    //            }
    //
    //            qualifierNode.setValue(value);
    //        }
    //    }


    /**
     * Adds a Value Property to the given Qualifier Node.
     *
     * @param node The desired Blob Node
     * @param nodeManager The corresponding Node Manager
     */
    //    private static void addQualifierValueNode(UaNode node, AasServiceNodeManager nodeManager) {
    //        NodeId myPropertyId = new NodeId(nodeManager.getNamespaceIndex(), node.getNodeId().getValue().toString() + "." + AASQualifierType.VALUE);
    //        PlainProperty<ByteString> myProperty = new PlainProperty<>(nodeManager, myPropertyId,
    //                UaQualifiedName.from(ObjectTypeIds.AASQualifierType.getNamespaceUri(), AASQualifierType.VALUE).toQualifiedName(nodeManager.getNamespaceTable()),
    //                LocalizedText.english(AASQualifierType.VALUE));
    //        myProperty.setDataTypeId(Identifiers.String);
    //        if (AasServiceNodeManager.VALUES_READ_ONLY) {
    //            myProperty.setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
    //        }
    //        myProperty.setDescription(new LocalizedText("", ""));
    //        node.addProperty(myProperty);
    //    }

    private static AASQualifier getQualifier(Qualifier qualifier) throws StatusException {
        if (qualifier == null) {
            return null;
        }
        AASQualifier retval = new AASQualifier();
        setQualifierData(retval, qualifier);
        return retval;
    }
}
