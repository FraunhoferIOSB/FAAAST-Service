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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaQualifiedName;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.nodes.PlainProperty;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.Variant;
import com.prosysopc.ua.stack.core.Identifiers;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ValueData;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValueFactory;
import opc.i4aas.datatypes.AASDataTypeDefXsd;
import opc.i4aas.datatypes.AASModellingKindDataType;
import opc.i4aas.datatypes.AASQualifierKindDataType;
import opc.i4aas.datatypes.AASSubmodelElementsDataType;
import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.ModellingKind;
import org.eclipse.digitaltwin.aas4j.v3.model.QualifierKind;


/**
 * Helper class with general OPC UA helper methods.
 */
public class UaHelper {

    /**
     * Sonar wants a private constructor.
     */
    private UaHelper() {}


    /**
     * Creates an OPC UA String property.
     * 
     * @param valueData The desired Value Data
     * @param typedValue The desired value
     * @return The created OPC UA property
     * @throws StatusException If an error occurs
     */
    public static PlainProperty<String> createStringProperty(ValueData valueData, TypedValue<?> typedValue) throws StatusException {
        PlainProperty<String> stringProperty = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        stringProperty.setDataTypeId(Identifiers.String);
        stringProperty.setDescription(new LocalizedText("", ""));
        if ((typedValue != null) && (typedValue.getValue() != null)) {
            stringProperty.setValue(typedValue.asString());
        }
        return stringProperty;
    }


    /**
     * Creates an OPC UA boolean property.
     * 
     * @param valueData The desired Value Data
     * @param typedValue The desired value
     * @return The created OPC UA property
     * @throws StatusException If an error occurs
     */
    public static PlainProperty<Boolean> createBooleanProperty(ValueData valueData, TypedValue<?> typedValue) throws StatusException {
        PlainProperty<Boolean> boolProperty = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        boolProperty.setDataTypeId(Identifiers.Boolean);
        boolProperty.setDescription(new LocalizedText("", ""));
        if ((typedValue != null) && (typedValue.getValue() != null)) {
            boolProperty.setValue(typedValue.getValue());
        }
        return boolProperty;
    }


    /**
     * Adds an OPC UA String property to the given node.
     * 
     * @param parentNode The node where the property should be added.
     * @param nodeManager The corresponding NodeManager.
     * @param name The name of the desired property.
     * @param value The value of the desired property.
     * @param namespaceUri The URI of the desired Namespace.
     * @throws StatusException If an error occurs
     * @throws ValueFormatException The data format of the value is invalid
     */
    public static void addStringUaProperty(UaNode parentNode, NodeManagerUaNode nodeManager, String name, String value, String namespaceUri)
            throws StatusException, ValueFormatException {
        NodeId nodeId = new NodeId(nodeManager.getNamespaceIndex(), parentNode.getNodeId().getValue().toString() + "." + name);
        QualifiedName browseName = UaQualifiedName.from(namespaceUri, name).toQualifiedName(nodeManager.getNamespaceTable());
        LocalizedText displayName = LocalizedText.english(name);
        parentNode.addProperty(createStringProperty(new ValueData(nodeId, browseName, displayName, nodeManager), TypedValueFactory.create(Datatype.STRING, value)));
    }


    /**
     * Adds an OPC UA Boolean property to the given node.
     * 
     * @param parentNode The node where the property should be added.
     * @param nodeManager The corresponding NodeManager.
     * @param name The name of the desired property.
     * @param value The value of the desired property.
     * @param namespaceUri The URI of the desired Namespace.
     * @throws StatusException If an error occurs
     * @throws ValueFormatException The data format of the value is invalid
     */
    public static void addBooleanUaProperty(UaNode parentNode, NodeManagerUaNode nodeManager, String name, boolean value, String namespaceUri)
            throws StatusException, ValueFormatException {
        NodeId nodeId = new NodeId(nodeManager.getNamespaceIndex(), parentNode.getNodeId().getValue().toString() + "." + name);
        QualifiedName browseName = UaQualifiedName.from(namespaceUri, name).toQualifiedName(nodeManager.getNamespaceTable());
        LocalizedText displayName = LocalizedText.english(name);
        parentNode.addProperty(
                createBooleanProperty(new ValueData(nodeId, browseName, displayName, nodeManager), TypedValueFactory.create(Datatype.BOOLEAN, Boolean.toString(value))));
    }


    /**
     * Creates a kind property.
     *
     * @param valueData the desired value data.
     * @param kind The desired Kind.
     * @return The created property.
     * @throws StatusException If an error occurs
     */
    public static PlainProperty<AASModellingKindDataType> createKindProperty(ValueData valueData, ModellingKind kind) throws StatusException {
        PlainProperty<AASModellingKindDataType> kindProperty = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(),
                valueData.getDisplayName());
        kindProperty.setDataTypeId(AASModellingKindDataType.SPECIFICATION.getTypeId().asNodeId(valueData.getNodeManager().getNamespaceTable()));
        kindProperty.setDescription(new LocalizedText("", ""));
        DataValue value = new DataValue(new Variant(ValueConverter.convertModellingKind(kind).getValue()));
        kindProperty.setValue(value);

        return kindProperty;
    }


    /**
     * Adds a kind property to the given node.
     *
     * @param parentNode The node where the property should be added.
     * @param nodeManager The corresponding NodeManager.
     * @param name The name of the desired property.
     * @param value The value of the desired property.
     * @param namespaceUri The URI of the desired Namespace.
     * @throws StatusException If an error occurs
     */
    public static void addKindProperty(UaNode parentNode, NodeManagerUaNode nodeManager, String name, ModellingKind value, String namespaceUri) throws StatusException {
        parentNode.addProperty(createKindProperty(createValueData(parentNode, nodeManager, name, namespaceUri), value));
    }


    /**
     * Creates a Qualifier Kind Property.
     *
     * @param valueData The desired Value Data
     * @param kind The desired Kind
     * @return The created OPC UA property
     * @throws StatusException If an error occurs
     */
    public static PlainProperty<AASQualifierKindDataType> createQualifierKindProperty(ValueData valueData, QualifierKind kind) throws StatusException {
        PlainProperty<AASQualifierKindDataType> kindProperty = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(),
                valueData.getDisplayName());
        kindProperty.setDataTypeId(AASQualifierKindDataType.SPECIFICATION.getTypeId().asNodeId(valueData.getNodeManager().getNamespaceTable()));
        kindProperty.setDescription(new LocalizedText("", ""));
        DataValue value = new DataValue(new Variant(ValueConverter.convertQualifierKind(kind).getValue()));
        kindProperty.setValue(value);

        return kindProperty;
    }


    /**
     * Adds a qualifier kind property to the given node.
     *
     * @param parentNode The node where the property should be added.
     * @param nodeManager The corresponding NodeManager.
     * @param name The name of the desired property.
     * @param value The value of the desired property.
     * @param namespaceUri The URI of the desired Namespace.
     * @throws StatusException If an error occurs
     */
    public static void addQualifierKindProperty(UaNode parentNode, NodeManagerUaNode nodeManager, String name, QualifierKind value, String namespaceUri) throws StatusException {
        parentNode.addProperty(createQualifierKindProperty(createValueData(parentNode, nodeManager, name, namespaceUri), value));
    }


    /**
     * Creates a DataTypeDefXsd property.
     *
     * @param valueData the desired value data.
     * @param datatype The desired datatype value.
     * @return The created property.
     * @throws StatusException If an error occurs
     */
    public static PlainProperty<AASDataTypeDefXsd> createDataTypeDefProperty(ValueData valueData, DataTypeDefXsd datatype) throws StatusException {
        PlainProperty<AASDataTypeDefXsd> datatypeProperty = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(),
                valueData.getDisplayName());
        datatypeProperty.setDataTypeId(AASDataTypeDefXsd.SPECIFICATION.getTypeId().asNodeId(valueData.getNodeManager().getNamespaceTable()));
        datatypeProperty.setDescription(new LocalizedText("", ""));
        DataValue value = new DataValue(new Variant(ValueConverter.convertDataTypeDefXsd(datatype).getValue()));
        datatypeProperty.setValue(value);

        return datatypeProperty;
    }


    /**
     * Adds a DataTypeDefXsd property to the given node.
     *
     * @param parentNode The node where the property should be added.
     * @param nodeManager The corresponding NodeManager.
     * @param name The name of the desired property.
     * @param value The value of the desired property.
     * @param namespaceUri The URI of the desired Namespace.
     * @throws StatusException If an error occurs
     */
    public static void addDataTypeDefProperty(UaNode parentNode, NodeManagerUaNode nodeManager, String name, DataTypeDefXsd value, String namespaceUri) throws StatusException {
        parentNode.addProperty(createDataTypeDefProperty(createValueData(parentNode, nodeManager, name, namespaceUri), value));
    }


    /**
     * Creates a AasSubmodelElements property.
     *
     * @param valueData the desired value data.
     * @param submodelElement The desired SubmodelElement value.
     * @return The created property.
     * @throws StatusException If an error occurs
     */
    public static PlainProperty<AASSubmodelElementsDataType> createAasSubmodelElementsProperty(ValueData valueData, AasSubmodelElements submodelElement) throws StatusException {
        PlainProperty<AASSubmodelElementsDataType> smelemProperty = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(),
                valueData.getDisplayName());
        smelemProperty.setDataTypeId(AASSubmodelElementsDataType.SPECIFICATION.getTypeId().asNodeId(valueData.getNodeManager().getNamespaceTable()));
        smelemProperty.setDescription(new LocalizedText("", ""));
        DataValue value = new DataValue(new Variant(ValueConverter.getAasSubmodelElementsType(submodelElement).getValue()));
        smelemProperty.setValue(value);

        return smelemProperty;
    }


    /**
     * Adds a AasSubmodelElements property to the given node.
     *
     * @param parentNode The node where the property should be added.
     * @param nodeManager The corresponding NodeManager.
     * @param name The name of the desired property.
     * @param value The value of the desired property.
     * @param namespaceUri The URI of the desired Namespace.
     * @throws StatusException If an error occurs
     */
    public static void addAasSubmodelElementsProperty(UaNode parentNode, NodeManagerUaNode nodeManager, String name, AasSubmodelElements value, String namespaceUri)
            throws StatusException {
        parentNode.addProperty(createAasSubmodelElementsProperty(createValueData(parentNode, nodeManager, name, namespaceUri), value));
    }


    private static ValueData createValueData(UaNode parentNode, NodeManagerUaNode nodeManager, String name, String namespaceUri) {
        ValueData retval;
        NodeId nodeId = new NodeId(nodeManager.getNamespaceIndex(), parentNode.getNodeId().getValue().toString() + "." + name);
        QualifiedName browseName = UaQualifiedName.from(namespaceUri, name).toQualifiedName(nodeManager.getNamespaceTable());
        LocalizedText displayName = LocalizedText.english(name);
        retval = new ValueData(nodeId, browseName, displayName, nodeManager);
        return retval;
    }
}
