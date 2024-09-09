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
import com.prosysopc.ua.ValueRanks;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.nodes.PlainProperty;
import com.prosysopc.ua.stack.builtintypes.ByteString;
import com.prosysopc.ua.stack.builtintypes.DateTime;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.UnsignedByte;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.stack.builtintypes.UnsignedLong;
import com.prosysopc.ua.stack.builtintypes.UnsignedShort;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.Identifiers;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.AasReferenceCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.EntityCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ValueData;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.BlobValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.FileValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.RelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import opc.i4aas.datatypes.AASDataTypeDefXsd;
import opc.i4aas.objecttypes.AASAnnotatedRelationshipElementType;
import opc.i4aas.objecttypes.AASBlobType;
import opc.i4aas.objecttypes.AASEntityType;
import opc.i4aas.objecttypes.AASFileType;
import opc.i4aas.objecttypes.AASMultiLanguagePropertyType;
import opc.i4aas.objecttypes.AASPropertyType;
import opc.i4aas.objecttypes.AASRangeType;
import opc.i4aas.objecttypes.AASReferenceElementType;
import opc.i4aas.objecttypes.AASRelationshipElementType;
import opc.i4aas.objecttypes.AASSubmodelElementList;
import opc.i4aas.objecttypes.AASSubmodelElementType;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class with helper methods for SubmodelElements used by the NodeManager
 */
public class AasSubmodelElementHelper {
    /**
     * The logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(AasSubmodelElementHelper.class);

    /**
     * Text if value is null
     */
    private static final String VALUE_NULL = "value must not be null";

    /**
     * Sonar wants a private constructor.
     */
    private AasSubmodelElementHelper() {

    }


    /**
     * Sets the values for the given RelationshipElement.
     *
     * @param aasElement The desired RelationshipElement.
     * @param value The new value.
     * @param nodeManager The corresponding Node Manager.
     * @throws StatusException If the operation fails
     */
    public static void setRelationshipValue(AASRelationshipElementType aasElement, RelationshipElementValue value, NodeManagerUaNode nodeManager) throws StatusException {
        Ensure.requireNonNull(aasElement, "aasElement must not be null");
        Ensure.requireNonNull(value, VALUE_NULL);

        AasReferenceCreator.setAasReferenceData(value.getFirst(), aasElement.getFirstNode(), false);
        AasReferenceCreator.setAasReferenceData(value.getSecond(), aasElement.getSecondNode(), false);

        if ((aasElement instanceof AASAnnotatedRelationshipElementType) && (value instanceof AnnotatedRelationshipElementValue)) {
            AASAnnotatedRelationshipElementType annotatedElement = (AASAnnotatedRelationshipElementType) aasElement;
            AnnotatedRelationshipElementValue annotatedValue = (AnnotatedRelationshipElementValue) value;
            UaNode[] annotationNodes = annotatedElement.getAnnotationNode().getComponents();
            Map<String, DataElementValue> valueMap = annotatedValue.getAnnotations();
            if (annotationNodes.length != valueMap.size()) {
                LOG.error("Size of Value ({}) doesn't match the number of AnnotationNodes ({})", valueMap.size(), annotationNodes.length);
                throw new IllegalArgumentException("Size of Value doesn't match the number of AnnotationNodes");
            }

            // The Key of the Map is the IdShort of the DataElement (in our case the BrowseName)
            for (UaNode annotationNode: annotationNodes) {
                if (valueMap.containsKey(annotationNode.getBrowseName().getName())) {
                    setDataElementValue(annotationNode, valueMap.get(annotationNode.getBrowseName().getName()), nodeManager);
                }
            }
        }
        else {
            LOG.debug("setRelationshipValue: No AnnotatedRelationshipElement {}", aasElement.getBrowseName().getName());
        }
    }


    /**
     * Sets the value of the given SubmodelElement.
     *
     * @param subElem The desired SubmodelElement.
     * @param value The new value
     * @param nodeManager The corresponding Node Manager.
     * @throws StatusException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     */
    public static void setSubmodelElementValue(AASSubmodelElementType subElem, ElementValue value, NodeManagerUaNode nodeManager) throws StatusException, ValueFormatException {
        LOG.debug("setSubmodelElementValue: {}", subElem.getBrowseName().getName());

        // changed the order because of an error in the derivation hierarchy of ElementValue
        // perhaps the order will be changed back to normal as soon as the error is fixed
        if ((value instanceof RelationshipElementValue) && (subElem instanceof AASRelationshipElementType)) {
            setRelationshipValue((AASRelationshipElementType) subElem, (RelationshipElementValue) value, nodeManager);
        }
        else if ((value instanceof EntityValue) && (subElem instanceof AASEntityType)) {
            setEntityPropertyValue((AASEntityType) subElem, (EntityValue) value, nodeManager);
        }
        else if (value instanceof DataElementValue dataElementValue) {
            setDataElementValue(subElem, dataElementValue, nodeManager);
        }
        else {
            LOG.warn("SubmodelElement {} type not supported", subElem.getBrowseName().getName());
        }
    }


    /**
     * Adds a Value Property to the given Blob Node.
     *
     * @param node The desired Blob Node
     * @param nodeManager The corresponding Node Manager.
     */
    public static void addBlobValueNode(UaNode node, NodeManagerUaNode nodeManager) {
        NodeId propertyId = new NodeId(nodeManager.getNamespaceIndex(), node.getNodeId().getValue().toString() + "." + AASBlobType.VALUE);
        PlainProperty<ByteString> property = new PlainProperty<>(nodeManager, propertyId,
                UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASBlobType.getNamespaceUri(), AASBlobType.VALUE).toQualifiedName(nodeManager.getNamespaceTable()),
                LocalizedText.english(AASBlobType.VALUE));
        property.setDataTypeId(Identifiers.ByteString);
        property.setDescription(new LocalizedText("", ""));
        node.addProperty(property);
    }


    /**
     * Adds a File Value Property to the given Node.
     *
     * @param fileNode The desired File Node.
     * @param nodeManager The corresponding Node Manager.
     */
    public static void addFileValueNode(UaNode fileNode, NodeManagerUaNode nodeManager) {
        NodeId propertyId = new NodeId(nodeManager.getNamespaceIndex(), fileNode.getNodeId().getValue().toString() + "." + AASFileType.VALUE);
        PlainProperty<String> property = new PlainProperty<>(nodeManager, propertyId,
                UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASFileType.getNamespaceUri(), AASFileType.VALUE).toQualifiedName(nodeManager.getNamespaceTable()),
                LocalizedText.english(AASFileType.VALUE));
        property.setDataTypeId(Identifiers.String);
        if (AasServiceNodeManager.VALUES_READ_ONLY) {
            property.setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
        }
        property.setDescription(new LocalizedText("", ""));
        fileNode.addProperty(property);
    }


    /**
     * Adds the Value Node for the MultiLanguageProperty.
     *
     * @param node The desired MultiLanguageProperty Node
     * @param arraySize The desired Array Size.
     * @param nodeManager The corresponding Node Manager.
     */
    public static void addMultiLanguageValueNode(UaNode node, int arraySize, NodeManagerUaNode nodeManager) {
        NodeId propertyId = new NodeId(nodeManager.getNamespaceIndex(), node.getNodeId().getValue().toString() + "." + AASMultiLanguagePropertyType.VALUE);
        PlainProperty<LocalizedText[]> myLTProperty = new PlainProperty<>(nodeManager, propertyId,
                UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASMultiLanguagePropertyType.getNamespaceUri(), AASMultiLanguagePropertyType.VALUE)
                        .toQualifiedName(nodeManager.getNamespaceTable()),
                LocalizedText.english(AASMultiLanguagePropertyType.VALUE));
        myLTProperty.setDataTypeId(Identifiers.LocalizedText);
        myLTProperty.setValueRank(ValueRanks.OneDimension);
        myLTProperty.setArrayDimensions(new UnsignedInteger[] {
                UnsignedInteger.valueOf(arraySize)
        });
        node.addProperty(myLTProperty);
        myLTProperty.setDescription(new LocalizedText("", ""));
    }


    /**
     * Set value and type for the desired Property.
     * 
     * @param aasProperty The desired Property
     * @param prop The desired AAS Property.
     * @param valueData The desired property data.
     * @throws StatusException If an error occurs
     */
    public static void setPropertyValueAndType(Property aasProperty, AASPropertyType prop, ValueData valueData)
            throws StatusException {
        try {
            LOG.atTrace().log("setPropertyValueAndType: {}", aasProperty.getIdShort());
            AASDataTypeDefXsd valueDataType;
            PropertyValue typedValue = ElementValueMapper.toValue(aasProperty, PropertyValue.class);
            if ((typedValue != null) && (typedValue.getValue() != null)) {
                valueDataType = ValueConverter.datatypeToOpcDataType(typedValue.getValue().getDataType());
            }
            else {
                valueDataType = ValueConverter.convertDataTypeDefXsd(aasProperty.getValueType());
            }

            prop.setValueType(valueDataType);

            switch (valueDataType) {
                case Boolean:
                    setBooleanPropertyValue(valueData, typedValue, prop);
                    break;

                case DateTime:
                    setDateTimePropertyValue(valueData, typedValue, prop);
                    break;

                case Int:
                    setInt32PropertyValue(valueData, typedValue, prop);
                    break;

                case UnsignedInt:
                    setUInt32PropertyValue(valueData, typedValue, prop);
                    break;

                case Long:
                    setInt64PropertyValue(valueData, typedValue, prop);
                    break;

                case UnsignedLong:
                    setUInt64PropertyValue(valueData, typedValue, prop);
                    break;

                case Short:
                    setInt16PropertyValue(valueData, typedValue, prop);
                    break;

                case UnsignedShort:
                    setUInt16PropertyValue(valueData, typedValue, prop);
                    break;

                case Byte:
                    setSBytePropertyValue(valueData, typedValue, prop);
                    break;

                case UnsignedByte:
                    setBytePropertyValue(valueData, typedValue, prop);
                    break;

                case Double:
                    setDoublePropertyValue(valueData, typedValue, prop);
                    break;

                case Float:
                    setFloatPropertyValue(valueData, typedValue, prop);
                    break;

                case String, AnyUri, Time, Duration, GDay, GMonth, GMonthDay, GYear, GYearMonth, Decimal, Integer, PositiveInteger, NonPositiveInteger, NegativeInteger,
                        NonNegativeInteger, Date:
                    setStringValue(valueData, typedValue, prop);
                    break;

                case Base64Binary, HexBinary:
                    setByteStringPropertyValue(valueData, typedValue, prop);
                    break;

                default:
                    LOG.warn("setPropertyValueAndType: Property {}: Unknown type: {}; use string as default", prop.getBrowseName().getName(), aasProperty.getValueType());
                    PlainProperty<String> myDefaultProperty = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(),
                            valueData.getDisplayName());
                    myDefaultProperty.setDataTypeId(Identifiers.String);
                    myDefaultProperty.setValue(aasProperty.getValue());
                    prop.addProperty(myDefaultProperty);
                    break;
            }
            if ((prop.getValueNode() != null) && (prop.getValueNode().getDescription() == null)) {
                prop.getValueNode().setDescription(new LocalizedText("", ""));
            }
        }
        catch (Exception ex) {
            LOG.error("setPropertyValueAndType Exception", ex);
        }
    }


    private static void setBooleanPropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop) throws StatusException {
        prop.addProperty(UaHelper.createBooleanProperty(valueData, typedValue != null ? typedValue.getValue() : null));
    }


    private static void setDateTimePropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop) throws StatusException {
        prop.addProperty(createDateTimeProperty(valueData, typedValue != null ? typedValue.getValue() : null));
    }


    private static void setInt16PropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop) throws StatusException {
        prop.addProperty(createInt16Property(valueData, typedValue != null ? typedValue.getValue() : null));
    }


    private static void setUInt16PropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop) throws StatusException {
        prop.addProperty(createUInt16Property(valueData, typedValue != null ? typedValue.getValue() : null));
    }


    private static void setInt32PropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop) throws StatusException {
        prop.addProperty(createInt32Property(valueData, typedValue != null ? typedValue.getValue() : null));
    }


    private static void setUInt32PropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop) throws StatusException {
        prop.addProperty(createUInt32Property(valueData, typedValue != null ? typedValue.getValue() : null));
    }


    private static void setInt64PropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop) throws StatusException {
        prop.addProperty(createInt64Property(valueData, typedValue != null ? typedValue.getValue() : null));
    }


    private static void setUInt64PropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop) throws StatusException {
        prop.addProperty(createUInt64Property(valueData, typedValue != null ? typedValue.getValue() : null));
    }


    private static void setSBytePropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop) throws StatusException {
        prop.addProperty(createSByteProperty(valueData, typedValue != null ? typedValue.getValue() : null));
    }


    private static void setBytePropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop) throws StatusException {
        prop.addProperty(createByteProperty(valueData, typedValue != null ? typedValue.getValue() : null));
    }


    private static void setStringValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop) throws StatusException {
        prop.addProperty(UaHelper.createStringProperty(valueData, typedValue != null ? typedValue.getValue() : null));
    }


    private static void setFloatPropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop) throws StatusException {
        prop.addProperty(createFloatProperty(valueData, typedValue != null ? typedValue.getValue() : null));
    }


    private static PlainProperty<Float> createFloatProperty(ValueData valueData, TypedValue<?> typedValue) throws StatusException {
        PlainProperty<Float> floatProperty = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        floatProperty.setDataTypeId(Identifiers.Float);
        floatProperty.setDescription(new LocalizedText("", ""));
        if ((typedValue != null) && (typedValue.getValue() != null)) {
            floatProperty.setValue(typedValue.getValue());
        }
        return floatProperty;
    }


    private static void setDoublePropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop) throws StatusException {
        prop.addProperty(createDoubleProperty(valueData, typedValue != null ? typedValue.getValue() : null));
    }


    private static PlainProperty<Double> createDoubleProperty(ValueData valueData, TypedValue<?> typedValue) throws StatusException {
        PlainProperty<Double> doubleProperty = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        doubleProperty.setDataTypeId(Identifiers.Double);
        doubleProperty.setDescription(new LocalizedText("", ""));
        if ((typedValue != null) && (typedValue.getValue() != null)) {
            doubleProperty.setValue(typedValue.getValue());
        }
        return doubleProperty;
    }


    private static PlainProperty<Byte> createSByteProperty(ValueData valueData, TypedValue<?> typedValue) throws StatusException {
        PlainProperty<Byte> sbyteProperty = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        sbyteProperty.setDataTypeId(Identifiers.SByte);
        sbyteProperty.setDescription(new LocalizedText("", ""));
        if ((typedValue != null) && (typedValue.getValue() != null)) {
            sbyteProperty.setValue(typedValue.getValue());
        }
        return sbyteProperty;
    }


    private static PlainProperty<UnsignedByte> createByteProperty(ValueData valueData, TypedValue<?> typedValue) throws StatusException {
        PlainProperty<UnsignedByte> usbyteProperty = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        usbyteProperty.setDataTypeId(Identifiers.Byte);
        usbyteProperty.setDescription(new LocalizedText("", ""));
        if ((typedValue != null) && (typedValue.getValue() != null)) {
            usbyteProperty.setValue(typedValue.getValue());
        }
        return usbyteProperty;
    }


    private static PlainProperty<Short> createInt16Property(ValueData valueData, TypedValue<?> typedValue) throws StatusException {
        PlainProperty<Short> int16Property = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        int16Property.setDataTypeId(Identifiers.Int16);
        int16Property.setDescription(new LocalizedText("", ""));
        if ((typedValue != null) && (typedValue.getValue() != null)) {
            int16Property.setValue(typedValue.getValue());
        }
        return int16Property;
    }


    private static PlainProperty<UnsignedShort> createUInt16Property(ValueData valueData, TypedValue<?> typedValue) throws StatusException {
        PlainProperty<UnsignedShort> uint16Property = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        uint16Property.setDataTypeId(Identifiers.UInt16);
        uint16Property.setDescription(new LocalizedText("", ""));
        if ((typedValue != null) && (typedValue.getValue() != null)) {
            uint16Property.setValue(typedValue.getValue());
        }
        return uint16Property;
    }


    private static PlainProperty<Long> createInt64Property(ValueData valueData, TypedValue<?> typedValue) throws NumberFormatException, StatusException {
        PlainProperty<Long> longProperty = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        longProperty.setDataTypeId(Identifiers.Int64);
        longProperty.setDescription(new LocalizedText("", ""));
        if (typedValue != null) {
            Object obj = typedValue.getValue();
            if ((obj != null) && (!(obj instanceof Long))) {
                obj = Long.valueOf(obj.toString());
            }
            longProperty.setValue(obj);
        }
        return longProperty;
    }


    private static PlainProperty<UnsignedLong> createUInt64Property(ValueData valueData, TypedValue<?> typedValue) throws NumberFormatException, StatusException {
        PlainProperty<UnsignedLong> ulongProperty = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        ulongProperty.setDataTypeId(Identifiers.UInt64);
        ulongProperty.setDescription(new LocalizedText("", ""));
        if (typedValue != null) {
            Object obj = typedValue.getValue();
            if ((obj != null) && (!(obj instanceof UnsignedLong))) {
                obj = UnsignedLong.valueOf(obj.toString());
            }
            ulongProperty.setValue(obj);
        }
        return ulongProperty;
    }


    private static PlainProperty<Integer> createInt32Property(ValueData valueData, TypedValue<?> typedValue) throws StatusException {
        PlainProperty<Integer> intProperty = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        intProperty.setDataTypeId(Identifiers.Int32);
        intProperty.setDescription(new LocalizedText("", ""));
        if ((typedValue != null) && (typedValue.getValue() != null)) {
            intProperty.setValue(typedValue.getValue());
        }
        return intProperty;
    }


    private static PlainProperty<UnsignedInteger> createUInt32Property(ValueData valueData, TypedValue<?> typedValue) throws StatusException {
        PlainProperty<UnsignedInteger> uintProperty = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        uintProperty.setDataTypeId(Identifiers.UInt32);
        uintProperty.setDescription(new LocalizedText("", ""));
        if ((typedValue != null) && (typedValue.getValue() != null)) {
            uintProperty.setValue(typedValue.getValue());
        }
        return uintProperty;
    }


    private static PlainProperty<DateTime> createDateTimeProperty(ValueData valueData, TypedValue<?> typedValue) throws StatusException {
        PlainProperty<DateTime> dateTimeProperty = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        dateTimeProperty.setDataTypeId(Identifiers.DateTime);
        dateTimeProperty.setDescription(new LocalizedText("", ""));
        if ((typedValue != null) && (typedValue.getValue() != null)) {
            if (typedValue.getValue() instanceof OffsetDateTime odt) {
                dateTimeProperty.setValue(ValueConverter.createDateTime(odt));
            }
            else {
                dateTimeProperty.setValue(typedValue.getValue());
            }
        }
        return dateTimeProperty;
    }


    private static void setByteStringPropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop) throws StatusException {
        prop.addProperty(createByteStringProperty(valueData, typedValue != null ? typedValue.getValue() : null));
    }


    private static void setByteStringRangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                                 TypedValue<?> maxTypedValue)
            throws StatusException {
        if (minValue != null) {
            range.addProperty(createByteStringProperty(minData, minTypedValue));
        }

        if (maxValue != null) {
            range.addProperty(createByteStringProperty(maxData, maxTypedValue));
        }
    }


    private static PlainProperty<ByteString> createByteStringProperty(ValueData valueData, TypedValue<?> typedValue) throws StatusException {
        PlainProperty<ByteString> byteStringProperty = new PlainProperty<>(valueData.getNodeManager(), valueData.getNodeId(), valueData.getBrowseName(),
                valueData.getDisplayName());
        byteStringProperty.setDataTypeId(Identifiers.ByteString);
        byteStringProperty.setDescription(new LocalizedText("", ""));
        if ((typedValue != null) && (typedValue.getValue() != null)) {
            byteStringProperty.setValue(typedValue.getValue());
        }
        return byteStringProperty;
    }


    public static void setRangeValueAndType(DataTypeDefXsd valueType, String minValue, String maxValue, AASRangeType range, ValueData minData,
                                            ValueData maxData)
            throws StatusException {
        try {
            TypedValue<?> minTypedValue = TypedValueFactory.create(valueType, minValue);
            TypedValue<?> maxTypedValue = TypedValueFactory.create(valueType, maxValue);
            AASDataTypeDefXsd valueDataType = getValueType(minTypedValue, valueType);
            range.setValueType(valueDataType);

            switch (valueDataType) {
                case Boolean:
                    setBooleanRangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue);
                    break;

                case DateTime:
                    setDateTimeRangeValues(minValue, minData, minTypedValue, maxValue, maxData, maxTypedValue, range);
                    break;

                case Int:
                    setInt32RangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue);
                    break;

                case UnsignedInt:
                    setUInt32RangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue);
                    break;

                case Long:
                    setInt64RangeValues(minValue, minData, minTypedValue, maxValue, maxData, maxTypedValue, range);
                    break;

                case UnsignedLong:
                    setUInt64RangeValues(minValue, minData, minTypedValue, maxValue, maxData, maxTypedValue, range);
                    break;

                case Short:
                    setInt16RangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue);
                    break;

                case UnsignedShort:
                    setUInt16RangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue);
                    break;

                case Byte:
                    setSByteRangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue);
                    break;

                case UnsignedByte:
                    setByteRangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue);
                    break;

                case Double:
                    setDoubleRangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue);
                    break;

                case Float:
                    setFloatRangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue);
                    break;

                case String, AnyUri, Time, Duration, GDay, GMonth, GMonthDay, GYear, GYearMonth, Decimal, Integer, PositiveInteger, NonPositiveInteger, NegativeInteger,
                        NonNegativeInteger, Date:
                    setStringRangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue);
                    break;

                case Base64Binary, HexBinary:
                    setByteStringRangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue);
                    break;

                default:
                    LOG.warn("setRangeValueAndType: Range {}: Unknown type: {}; use string as default", range.getBrowseName().getName(), valueType);
                    setStringRangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue);
                    break;
            }
        }
        catch (Exception ex) {
            LOG.error("setRangeValueAndType Exception", ex);
        }
    }


    private static AASDataTypeDefXsd getValueType(TypedValue<?> typedValue, DataTypeDefXsd valueType) {
        AASDataTypeDefXsd valueDataType;
        if (typedValue != null) {
            valueDataType = ValueConverter.datatypeToOpcDataType(typedValue.getDataType());
        }
        else {
            valueDataType = ValueConverter.convertDataTypeDefXsd(valueType);
        }
        return valueDataType;
    }


    private static void setStringRangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                             TypedValue<?> maxTypedValue)
            throws StatusException {
        if (minValue != null) {
            range.addProperty(UaHelper.createStringProperty(minData, minTypedValue));
        }

        if (maxValue != null) {
            range.addProperty(UaHelper.createStringProperty(maxData, maxTypedValue));
        }
    }


    private static void setFloatRangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                            TypedValue<?> maxTypedValue)
            throws StatusException {
        if (minValue != null) {
            range.addProperty(createFloatProperty(minData, minTypedValue));
        }

        if (maxValue != null) {
            range.addProperty(createFloatProperty(maxData, maxTypedValue));
        }
    }


    private static void setDoubleRangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                             TypedValue<?> maxTypedValue)
            throws StatusException {
        if (minValue != null) {
            range.addProperty(createDoubleProperty(minData, minTypedValue));
        }

        if (maxValue != null) {
            range.addProperty(createDoubleProperty(maxData, maxTypedValue));
        }
    }


    private static void setSByteRangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                            TypedValue<?> maxTypedValue)
            throws StatusException {
        if (minValue != null) {
            range.addProperty(createSByteProperty(minData, minTypedValue));
        }

        if (maxValue != null) {
            range.addProperty(createSByteProperty(maxData, maxTypedValue));
        }
    }


    private static void setByteRangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                           TypedValue<?> maxTypedValue)
            throws StatusException {
        if (minValue != null) {
            range.addProperty(createByteProperty(minData, minTypedValue));
        }

        if (maxValue != null) {
            range.addProperty(createByteProperty(maxData, maxTypedValue));
        }
    }


    private static void setInt16RangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                            TypedValue<?> maxTypedValue)
            throws StatusException {
        if (minValue != null) {
            range.addProperty(createInt16Property(minData, minTypedValue));
        }

        if (maxValue != null) {
            range.addProperty(createInt16Property(maxData, maxTypedValue));
        }
    }


    private static void setUInt16RangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                             TypedValue<?> maxTypedValue)
            throws StatusException {
        if (minValue != null) {
            range.addProperty(createUInt16Property(minData, minTypedValue));
        }

        if (maxValue != null) {
            range.addProperty(createUInt16Property(maxData, maxTypedValue));
        }
    }


    private static void setInt64RangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, String maxValue, ValueData maxData, TypedValue<?> maxTypedValue,
                                            AASRangeType range)
            throws NumberFormatException, StatusException {
        if (minValue != null) {
            range.addProperty(createInt64Property(minData, minTypedValue));
        }
        if (maxValue != null) {
            range.addProperty(createInt64Property(maxData, maxTypedValue));
        }
    }


    private static void setUInt64RangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, String maxValue, ValueData maxData, TypedValue<?> maxTypedValue,
                                             AASRangeType range)
            throws NumberFormatException, StatusException {
        if (minValue != null) {
            range.addProperty(createUInt64Property(minData, minTypedValue));
        }
        if (maxValue != null) {
            range.addProperty(createUInt64Property(maxData, maxTypedValue));
        }
    }


    private static void setInt32RangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                            TypedValue<?> maxTypedValue)
            throws StatusException {
        if (minValue != null) {
            range.addProperty(createInt32Property(minData, minTypedValue));
        }

        if (maxValue != null) {
            range.addProperty(createInt32Property(maxData, maxTypedValue));
        }
    }


    private static void setUInt32RangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                             TypedValue<?> maxTypedValue)
            throws StatusException {
        if (minValue != null) {
            range.addProperty(createUInt32Property(minData, minTypedValue));
        }

        if (maxValue != null) {
            range.addProperty(createUInt32Property(maxData, maxTypedValue));
        }
    }


    private static void setDateTimeRangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, String maxValue, ValueData maxData, TypedValue<?> maxTypedValue,
                                               AASRangeType range)
            throws StatusException {
        if (minValue != null) {
            range.addProperty(createDateTimeProperty(minData, minTypedValue));
        }
        if (maxValue != null) {
            range.addProperty(createDateTimeProperty(maxData, maxTypedValue));
        }
    }


    private static void setBooleanRangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                              TypedValue<?> maxTypedValue)
            throws StatusException {
        if (minValue != null) {
            range.addProperty(UaHelper.createBooleanProperty(minData, minTypedValue));
        }

        if (maxValue != null) {
            range.addProperty(UaHelper.createBooleanProperty(maxData, maxTypedValue));
        }
    }


    /**
     * Sets the values for the given DataElement.
     *
     * @param node The desired DataElement.
     * @param value The new value.
     * @param nodeManager The corresponding Node Manager.
     * @throws StatusException If the operation fails
     */
    private static void setDataElementValue(UaNode node, DataElementValue value, NodeManagerUaNode nodeManager) throws StatusException {
        if ((node instanceof AASPropertyType) && (value instanceof PropertyValue)) {
            setPropertyValue((AASPropertyType) node, (PropertyValue) value);
        }
        else if ((node instanceof AASFileType) && (value instanceof FileValue)) {
            setFilePropertyValue((AASFileType) node, (FileValue) value, nodeManager);
        }
        else if ((node instanceof AASBlobType) && (value instanceof BlobValue)) {
            setBlobValue((AASBlobType) node, (BlobValue) value, nodeManager);
        }
        else if ((node instanceof AASReferenceElementType) && (value instanceof ReferenceElementValue)) {
            setReferenceElementValue((AASReferenceElementType) node, (ReferenceElementValue) value);
        }
        else if ((node instanceof AASRangeType) && (value instanceof RangeValue)) {
            setRangeValue((AASRangeType) node, (RangeValue<?>) value);
        }
        else if ((node instanceof AASMultiLanguagePropertyType) && (value instanceof MultiLanguagePropertyValue)) {
            setMultiLanguagePropertyValue((AASMultiLanguagePropertyType) node, (MultiLanguagePropertyValue) value, nodeManager);
        }
        else if (value != null) {
            LOG.warn("setDataElementValue: unknown or invalid DataElement or value: {}; Class: {}; Value Class: {}", node.getBrowseName().getName(), node.getClass(),
                    value.getClass());
        }
    }


    /**
     * Sets the value of a property.
     *
     * @param property The desired Property
     * @param value The new value.
     * @throws StatusException If the operation fails.
     */
    private static void setPropertyValue(AASPropertyType property, PropertyValue value) throws StatusException {
        LOG.debug("setPropertyValue: {} to {}", property.getBrowseName().getName(), value.getValue());
        property.setValue(ValueConverter.convertTypedValue(value.getValue()));
    }


    /**
     * Sets the values for the given Entity.
     *
     * @param entity The desired Entity.
     * @param value The new value.
     * @param nodeManager The corresponding Node Manager.
     * @throws StatusException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     */
    private static void setEntityPropertyValue(AASEntityType entity, EntityValue value, NodeManagerUaNode nodeManager) throws StatusException, ValueFormatException {
        // EntityType
        entity.setEntityType(ValueConverter.getAasEntityType(value.getEntityType()));

        // globalAssetId
        if ((value.getGlobalAssetId() != null) && (!value.getGlobalAssetId().isEmpty())) {
            EntityCreator.setGlobalAssetIdData(entity, value.getGlobalAssetId(), nodeManager);
        }

        // Statements
        Map<String, ElementValue> valueMap = value.getStatements();
        AASSubmodelElementList statementNode = entity.getStatementNode();
        if (statementNode != null) {
            UaNode[] statementNodes = statementNode.getComponents();
            if (statementNodes.length != valueMap.size()) {
                LOG.warn("Size of Value ({}) doesn't match the number of StatementNodes ({})", valueMap.size(), statementNodes.length);
                throw new IllegalArgumentException("Size of Value doesn't match the number of StatementNodes");
            }

            for (UaNode statementNode1: statementNodes) {
                if ((statementNode1 instanceof AASSubmodelElementType) && value.getStatements().containsKey(statementNode1.getBrowseName().getName())) {
                    setSubmodelElementValue((AASSubmodelElementType) statementNode1, value.getStatements().get(statementNode1.getBrowseName().getName()), nodeManager);
                }
            }
        }
    }


    /**
     * Sets the values for the given File.
     *
     * @param file The desired file.
     * @param value The new value
     * @param nodeManager The corresponding Node Manager.
     * @throws StatusException If the operation fails
     */
    private static void setFilePropertyValue(AASFileType file, FileValue value, NodeManagerUaNode nodeManager) throws StatusException {
        file.setContentType(value.getContentType());
        if (value.getValue() != null) {
            if (file.getValueNode() == null) {
                addFileValueNode(file, nodeManager);
            }

            file.setValue(value.getValue());
        }
    }


    /**
     * Sets the values for the given Blob.
     *
     * @param blob The desired blob.
     * @param value The new value
     * @param nodeManager The corresponding Node Manager.
     * @throws StatusException If the operation fails
     */
    private static void setBlobValue(AASBlobType blob, BlobValue value, NodeManagerUaNode nodeManager) throws StatusException {
        // MimeType
        blob.setContentType(value.getContentType());

        // Value
        if (value.getValue() != null) {
            if (blob.getValueNode() == null) {
                addBlobValueNode(blob, nodeManager);
            }

            blob.setValue(ByteString.valueOf(value.getValue()));
        }
    }


    /**
     * Sets the value for the given ReferenceElement.
     *
     * @param refElement The desired ReferenceElement.
     * @param value The new value.
     * @throws StatusException If the operation fails
     */
    private static void setReferenceElementValue(AASReferenceElementType refElement, ReferenceElementValue value) throws StatusException {
        AasReferenceCreator.setAasReferenceData(value.getValue(), refElement.getValueNode());
    }


    /**
     * Sets the value for the given Range.
     *
     * @param range The desired Range.
     * @param value The new value
     * @throws StatusException If the operation fails
     */
    private static void setRangeValue(AASRangeType range, RangeValue<?> value) throws StatusException {
        if (range.getMinNode() != null) {
            range.setMin(ValueConverter.convertTypedValue(value.getMin()));
        }
        if (range.getMaxNode() != null) {
            range.setMax(ValueConverter.convertTypedValue(value.getMax()));
        }
    }


    /**
     * Sets the values for the given MultiLanguageProperty.
     *
     * @param multiLangProp The desired MultiLanguageProperty.
     * @param value The new value
     * @param nodeManager The corresponding Node Manager.
     * @throws StatusException If the operation fails
     */
    private static void setMultiLanguagePropertyValue(AASMultiLanguagePropertyType multiLangProp, MultiLanguagePropertyValue value, NodeManagerUaNode nodeManager)
            throws StatusException {
        List<LangStringTextType> values = new ArrayList<>(value.getLangStringSet());
        if (multiLangProp.getValueNode() == null) {
            addMultiLanguageValueNode(multiLangProp, values.size(), nodeManager);
        }

        multiLangProp.getValueNode().setValue(ValueConverter.getLocalizedTextFromLangStringSet(values));
    }

}
