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
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.Identifiers;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ValueData;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DateTimeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DecimalValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.IntegerValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import opc.i4aas.AASAnnotatedRelationshipElementType;
import opc.i4aas.AASBlobType;
import opc.i4aas.AASEntityType;
import opc.i4aas.AASFileType;
import opc.i4aas.AASKeyDataType;
import opc.i4aas.AASMultiLanguagePropertyType;
import opc.i4aas.AASPropertyType;
import opc.i4aas.AASRangeType;
import opc.i4aas.AASReferenceElementType;
import opc.i4aas.AASReferenceType;
import opc.i4aas.AASRelationshipElementType;
import opc.i4aas.AASSubmodelElementList;
import opc.i4aas.AASSubmodelElementType;
import opc.i4aas.AASValueTypeDataType;
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
    private static final String VALUE_NULL = "value is null";

    /**
     * Make certain variable values read-only, because writing would not make
     * sense
     */
    private static final boolean VALUES_READ_ONLY = true;

    /**
     * Text if node is null
     */
    private static final String NODE_NULL = "node is null";

    /**
     * Sonar wants a private constructor.
     */
    private AasSubmodelElementHelper() {

    }


    /**
     * Sets the NodeManager.
     */
    public static void setNodeManager() {}


    /**
     * Sets the values for the given RelationshipElement.
     *
     * @param aasElement The desired RelationshipElement.
     * @param value The new value.
     * @param nodeManager The corresponding Node Manager.
     * @throws StatusException If the operation fails
     */
    public static void setRelationshipValue(AASRelationshipElementType aasElement, RelationshipElementValue value, NodeManagerUaNode nodeManager) throws StatusException {
        if (aasElement == null) {
            throw new IllegalArgumentException("aasElement is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException(VALUE_NULL);
        }

        Reference ref = new DefaultReference.Builder().keys(value.getFirst()).build();
        setAasReferenceData(ref, aasElement.getFirstNode(), false);
        ref = new DefaultReference.Builder().keys(value.getSecond()).build();
        setAasReferenceData(ref, aasElement.getSecondNode(), false);

        if ((aasElement instanceof AASAnnotatedRelationshipElementType) && (value instanceof AnnotatedRelationshipElementValue)) {
            AASAnnotatedRelationshipElementType annotatedElement = (AASAnnotatedRelationshipElementType) aasElement;
            AnnotatedRelationshipElementValue annotatedValue = (AnnotatedRelationshipElementValue) value;
            UaNode[] annotationNodes = annotatedElement.getAnnotationNode().getComponents();
            Map<String, DataElementValue> valueMap = annotatedValue.getAnnotations();
            if (annotationNodes.length != valueMap.size()) {
                LOG.warn("Size of Value ({}) doesn't match the number of AnnotationNodes ({})", valueMap.size(), annotationNodes.length);
                throw new IllegalArgumentException("Size of Value doesn't match the number of AnnotationNodes");
            }

            // The Key of the Map is the IDShort of the DataElement (in our case the BrowseName)
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
     */
    public static void setSubmodelElementValue(AASSubmodelElementType subElem, ElementValue value, NodeManagerUaNode nodeManager) throws StatusException {
        LOG.debug("setSubmodelElementValue: {}", subElem.getBrowseName().getName());

        // changed the order because of an error in the derivation hierarchy of ElementValue
        // perhaps the order will be changed back to normal as soon as the error is fixed
        if ((value instanceof RelationshipElementValue) && (subElem instanceof AASRelationshipElementType)) {
            setRelationshipValue((AASRelationshipElementType) subElem, (RelationshipElementValue) value, nodeManager);
        }
        else if ((value instanceof EntityValue) && (subElem instanceof AASEntityType)) {
            setEntityPropertyValue((AASEntityType) subElem, (EntityValue) value, nodeManager);
        }
        else if (value instanceof DataElementValue) {
            setDataElementValue(subElem, (DataElementValue) value, nodeManager);
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
        NodeId myPropertyId = new NodeId(nodeManager.getNamespaceIndex(), node.getNodeId().getValue().toString() + "." + AASBlobType.VALUE);
        PlainProperty<ByteString> myProperty = new PlainProperty<>(nodeManager, myPropertyId,
                UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASBlobType.getNamespaceUri(), AASBlobType.VALUE).toQualifiedName(nodeManager.getNamespaceTable()),
                LocalizedText.english(AASBlobType.VALUE));
        myProperty.setDataTypeId(Identifiers.ByteString);
        myProperty.setDescription(new LocalizedText("", ""));
        node.addProperty(myProperty);
    }


    /**
     * Adds a File Value Property to the given Node.
     *
     * @param fileNode The desired File Node.
     * @param nodeManager The corresponding Node Manager.
     */
    public static void addFileValueNode(UaNode fileNode, NodeManagerUaNode nodeManager) {
        NodeId myPropertyId = new NodeId(nodeManager.getNamespaceIndex(), fileNode.getNodeId().getValue().toString() + "." + AASFileType.VALUE);
        PlainProperty<String> myProperty = new PlainProperty<>(nodeManager, myPropertyId,
                UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASFileType.getNamespaceUri(), AASFileType.VALUE).toQualifiedName(nodeManager.getNamespaceTable()),
                LocalizedText.english(AASFileType.VALUE));
        myProperty.setDataTypeId(Identifiers.String);
        if (VALUES_READ_ONLY) {
            myProperty.setAccessLevel(AccessLevelType.CurrentRead);
        }
        myProperty.setDescription(new LocalizedText("", ""));
        fileNode.addProperty(myProperty);
    }


    /**
     * Adds the Value Node for the MultiLanguageProperty.
     *
     * @param node The desired MultiLanguageProperty Node
     * @param arraySize The desired Array Size.
     * @param nodeManager The corresponding Node Manager.
     */
    public static void addMultiLanguageValueNode(UaNode node, int arraySize, NodeManagerUaNode nodeManager) {
        NodeId propId = new NodeId(nodeManager.getNamespaceIndex(), node.getNodeId().getValue().toString() + "." + AASMultiLanguagePropertyType.VALUE);
        PlainProperty<LocalizedText[]> myLTProperty = new PlainProperty<>(nodeManager, propId,
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
     * @param nodeManager The corresponding Node Manager.
     * @throws StatusException If an error occurs
     */
    public static void setPropertyValueAndType(Property aasProperty, AASPropertyType prop, ValueData valueData, NodeManagerUaNode nodeManager)
            throws StatusException {
        try {
            AASValueTypeDataType valueDataType;

            PropertyValue typedValue = PropertyValue.of(aasProperty.getValueType(), aasProperty.getValue());
            if ((typedValue != null) && (typedValue.getValue() != null)) {
                valueDataType = ValueConverter.datatypeToValueType(typedValue.getValue().getDataType());
            }
            else {
                valueDataType = ValueConverter.stringToValueType(aasProperty.getValueType());
            }

            prop.setValueType(valueDataType);

            switch (valueDataType) {
                case Boolean:
                    setBooleanPropertyValue(valueData, typedValue, prop, nodeManager);
                    break;

                case DateTime:
                    setDateTimePropertyValue(valueData, typedValue, prop, nodeManager);
                    break;

                case Int32:
                    setInt32PropertyValue(valueData, typedValue, prop, nodeManager);
                    break;

                case Int64:
                    setInt64PropertyValue(valueData, typedValue, prop, nodeManager);
                    break;

                case Int16:
                    setInt16PropertyValue(valueData, typedValue, prop, nodeManager);
                    break;

                case SByte:
                    setSByteValue(valueData, typedValue, prop, nodeManager);
                    break;

                case Double:
                    setDoublePropertyValue(valueData, typedValue, prop, nodeManager);
                    break;

                case Float:
                    setFloatPropertyValue(valueData, typedValue, prop, nodeManager);
                    break;

                case String:
                    setStringValue(valueData, typedValue, prop, nodeManager);
                    break;

                default:
                    LOG.warn("setPropertyValueAndType: Property {}: Unknown type: {}; use string as default", prop.getBrowseName().getName(), aasProperty.getValueType());
                    PlainProperty<String> myDefaultProperty = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
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


    private static void setStringValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop, NodeManagerUaNode nodeManager) throws StatusException {
        PlainProperty<String> myStringProperty = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        myStringProperty.setDataTypeId(Identifiers.String);
        if ((typedValue != null) && (typedValue.getValue() != null) && (typedValue.getValue().getValue() != null)) {
            myStringProperty.setValue(typedValue.getValue().getValue());
        }
        prop.addProperty(myStringProperty);
    }


    private static void setFloatPropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop, NodeManagerUaNode nodeManager) throws StatusException {
        PlainProperty<Float> myFloatProperty = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        myFloatProperty.setDataTypeId(Identifiers.Float);
        if ((typedValue != null) && (typedValue.getValue() != null) && (typedValue.getValue().getValue() != null)) {
            myFloatProperty.setValue(typedValue.getValue().getValue());
        }
        prop.addProperty(myFloatProperty);
    }


    private static void setDoublePropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop, NodeManagerUaNode nodeManager) throws StatusException {
        PlainProperty<Double> myDoubleProperty = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        myDoubleProperty.setDataTypeId(Identifiers.Double);
        if ((typedValue != null) && (typedValue.getValue() != null) && (typedValue.getValue().getValue() != null)) {
            myDoubleProperty.setValue(typedValue.getValue().getValue());
        }
        prop.addProperty(myDoubleProperty);
    }


    private static void setSByteValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop, NodeManagerUaNode nodeManager) throws StatusException {
        PlainProperty<Byte> mySByteProperty = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        mySByteProperty.setDataTypeId(Identifiers.SByte);
        if ((typedValue != null) && (typedValue.getValue() != null) && (typedValue.getValue().getValue() != null)) {
            mySByteProperty.setValue(typedValue.getValue().getValue());
        }
        prop.addProperty(mySByteProperty);
    }


    private static void setInt16PropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop, NodeManagerUaNode nodeManager) throws StatusException {
        PlainProperty<Short> myInt16Property = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        myInt16Property.setDataTypeId(Identifiers.Int16);
        if ((typedValue != null) && (typedValue.getValue() != null) && (typedValue.getValue().getValue() != null)) {
            myInt16Property.setValue(typedValue.getValue().getValue());
        }
        prop.addProperty(myInt16Property);
    }


    private static void setInt64PropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop, NodeManagerUaNode nodeManager)
            throws StatusException, NumberFormatException {
        PlainProperty<Long> myLongProperty = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        myLongProperty.setDataTypeId(Identifiers.Int64);
        if ((typedValue != null) && (typedValue.getValue() != null) && (typedValue.getValue().getValue() != null)) {
            Object obj = typedValue.getValue().getValue();
            if ((obj != null) && (!(obj instanceof Long))) {
                obj = Long.valueOf(obj.toString());
            }
            myLongProperty.setValue(obj);
        }
        prop.addProperty(myLongProperty);
    }


    private static void setInt32PropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop, NodeManagerUaNode nodeManager) throws StatusException {
        PlainProperty<Integer> myIntProperty = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        myIntProperty.setDataTypeId(Identifiers.Int32);
        if ((typedValue != null) && (typedValue.getValue() != null) && (typedValue.getValue().getValue() != null)) {
            myIntProperty.setValue(typedValue.getValue().getValue());
        }
        prop.addProperty(myIntProperty);
    }


    private static void setDateTimePropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop, NodeManagerUaNode nodeManager) throws StatusException {
        PlainProperty<DateTime> myDateTimeProperty = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        myDateTimeProperty.setDataTypeId(Identifiers.DateTime);
        if ((typedValue != null) && (typedValue.getValue() != null) && (typedValue.getValue().getValue() != null)) {
            if (typedValue.getValue() instanceof DateTimeValue) {
                DateTimeValue dtval = (DateTimeValue) typedValue.getValue();
                DateTime dt = ValueConverter.createDateTime(dtval.getValue());
                myDateTimeProperty.setValue(dt);
            }
            else {
                myDateTimeProperty.setValue(typedValue.getValue().getValue());
            }
        }
        prop.addProperty(myDateTimeProperty);
    }


    private static void setBooleanPropertyValue(ValueData valueData, PropertyValue typedValue, AASPropertyType prop, NodeManagerUaNode nodeManager) throws StatusException {
        PlainProperty<Boolean> myBoolProperty = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
        myBoolProperty.setDataTypeId(Identifiers.Boolean);
        if ((typedValue != null) && (typedValue.getValue() != null) && (typedValue.getValue().getValue() != null)) {
            myBoolProperty.setValue(typedValue.getValue().getValue());
        }
        prop.addProperty(myBoolProperty);
    }


    public static void setRangeValueAndType(String valueType, String minValue, String maxValue, AASRangeType range, ValueData minData,
                                            ValueData maxData, NodeManagerUaNode nodeManager)
            throws StatusException {
        try {
            TypedValue<?> minTypedValue = TypedValueFactory.create(valueType, minValue);
            TypedValue<?> maxTypedValue = TypedValueFactory.create(valueType, maxValue);
            AASValueTypeDataType valueDataType;
            if (minTypedValue != null) {
                valueDataType = ValueConverter.datatypeToValueType(minTypedValue.getDataType());
            }
            else {
                valueDataType = ValueConverter.stringToValueType(valueType);
            }

            range.setValueType(valueDataType);

            switch (valueDataType) {
                case Boolean:
                    setBooleanRangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue, nodeManager);
                    break;

                case DateTime:
                    setDateTimeRangeMin(minValue, minData, minTypedValue, range, nodeManager);
                    setDateTimeRangeMax(maxValue, maxData, maxTypedValue, range, nodeManager);
                    break;

                case Int32:
                    setInt32RangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue, nodeManager);
                    break;

                case Int64:
                    setInt64RangeMin(minValue, minData, minTypedValue, range, nodeManager);
                    setInt64RangeMax(maxValue, maxData, maxTypedValue, range, nodeManager);
                    break;

                case Int16:
                    setInt16RangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue, nodeManager);
                    break;

                case SByte:
                    setSByteRangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue, nodeManager);
                    break;

                case Double:
                    setDoubleRangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue, nodeManager);
                    break;

                case Float:
                    setFloatRangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue, nodeManager);
                    break;

                case String:
                    setStringRangeValues(minValue, minData, minTypedValue, range, maxValue, maxData, maxTypedValue, nodeManager);
                    break;

                default:
                    LOG.warn("setRangeValueAndType: Range {}: Unknown type: {}; use string as default", range.getBrowseName().getName(), valueType);
                    if (minValue != null) {
                        PlainProperty<String> myStringProperty = new PlainProperty<>(nodeManager, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
                        myStringProperty.setDataTypeId(Identifiers.String);
                        myStringProperty.setValue(minValue);
                        myStringProperty.setDescription(new LocalizedText("", ""));
                        range.addProperty(myStringProperty);
                    }

                    if (maxValue != null) {
                        PlainProperty<String> myStringProperty = new PlainProperty<>(nodeManager, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
                        myStringProperty.setDataTypeId(Identifiers.String);
                        myStringProperty.setValue(maxValue);
                        myStringProperty.setDescription(new LocalizedText("", ""));
                        range.addProperty(myStringProperty);
                    }
                    break;
            }
        }
        catch (Exception ex) {
            LOG.error("setPropertyValueAndType Exception", ex);
        }
    }


    private static void setStringRangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                             TypedValue<?> maxTypedValue, NodeManagerUaNode nodeManager)
            throws StatusException {
        if (minValue != null) {
            PlainProperty<String> myStringProperty = new PlainProperty<>(nodeManager, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
            myStringProperty.setDataTypeId(Identifiers.String);
            if ((minTypedValue != null) && (minTypedValue.getValue() != null)) {
                myStringProperty.setValue(minTypedValue.getValue());
            }
            myStringProperty.setDescription(new LocalizedText("", ""));
            range.addProperty(myStringProperty);
        }

        if (maxValue != null) {
            PlainProperty<String> myStringProperty = new PlainProperty<>(nodeManager, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
            myStringProperty.setDataTypeId(Identifiers.String);
            if ((maxTypedValue != null) && (maxTypedValue.getValue() != null)) {
                myStringProperty.setValue(maxTypedValue.getValue());
            }
            myStringProperty.setDescription(new LocalizedText("", ""));
            range.addProperty(myStringProperty);
        }
    }


    private static void setFloatRangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                            TypedValue<?> maxTypedValue, NodeManagerUaNode nodeManager)
            throws StatusException {
        if (minValue != null) {
            PlainProperty<Float> myFloatProperty = new PlainProperty<>(nodeManager, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
            myFloatProperty.setDataTypeId(Identifiers.Float);
            if ((minTypedValue != null) && (minTypedValue.getValue() != null)) {
                myFloatProperty.setValue(minTypedValue.getValue());
            }
            myFloatProperty.setDescription(new LocalizedText("", ""));
            range.addProperty(myFloatProperty);
        }

        if (maxValue != null) {
            PlainProperty<Float> myFloatProperty = new PlainProperty<>(nodeManager, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
            myFloatProperty.setDataTypeId(Identifiers.Float);
            if ((maxTypedValue != null) && (maxTypedValue.getValue() != null)) {
                myFloatProperty.setValue(maxTypedValue.getValue());
            }
            myFloatProperty.setDescription(new LocalizedText("", ""));
            range.addProperty(myFloatProperty);
        }
    }


    private static void setDoubleRangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                             TypedValue<?> maxTypedValue, NodeManagerUaNode nodeManager)
            throws StatusException {
        if (minValue != null) {
            PlainProperty<Double> myDoubleProperty = new PlainProperty<>(nodeManager, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
            myDoubleProperty.setDataTypeId(Identifiers.Double);
            if ((minTypedValue != null) && (minTypedValue.getValue() != null)) {
                myDoubleProperty.setValue(minTypedValue.getValue());
            }
            myDoubleProperty.setDescription(new LocalizedText("", ""));
            range.addProperty(myDoubleProperty);
        }

        if (maxValue != null) {
            PlainProperty<Double> myDoubleProperty = new PlainProperty<>(nodeManager, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
            myDoubleProperty.setDataTypeId(Identifiers.Double);
            if ((maxTypedValue != null) && (maxTypedValue.getValue() != null)) {
                myDoubleProperty.setValue(maxTypedValue.getValue());
            }
            myDoubleProperty.setDescription(new LocalizedText("", ""));
            range.addProperty(myDoubleProperty);
        }
    }


    private static void setSByteRangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                            TypedValue<?> maxTypedValue, NodeManagerUaNode nodeManager)
            throws StatusException {
        if (minValue != null) {
            PlainProperty<Byte> mySByteProperty = new PlainProperty<>(nodeManager, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
            mySByteProperty.setDataTypeId(Identifiers.SByte);
            if ((minTypedValue != null) && (minTypedValue.getValue() != null)) {
                mySByteProperty.setValue(minTypedValue.getValue());
            }
            mySByteProperty.setDescription(new LocalizedText("", ""));
            range.addProperty(mySByteProperty);
        }

        if (maxValue != null) {
            PlainProperty<Byte> mySByteProperty = new PlainProperty<>(nodeManager, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
            mySByteProperty.setDataTypeId(Identifiers.SByte);
            if ((maxTypedValue != null) && (maxTypedValue.getValue() != null)) {
                mySByteProperty.setValue(maxTypedValue.getValue());
            }
            mySByteProperty.setDescription(new LocalizedText("", ""));
            range.addProperty(mySByteProperty);
        }
    }


    private static void setInt16RangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                            TypedValue<?> maxTypedValue, NodeManagerUaNode nodeManager)
            throws StatusException {
        if (minValue != null) {
            PlainProperty<Short> myInt16Property = new PlainProperty<>(nodeManager, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
            myInt16Property.setDataTypeId(Identifiers.Int16);
            if ((minTypedValue != null) && (minTypedValue.getValue() != null)) {
                myInt16Property.setValue(minTypedValue.getValue());
            }
            myInt16Property.setDescription(new LocalizedText("", ""));
            range.addProperty(myInt16Property);
        }

        if (maxValue != null) {
            PlainProperty<Short> myInt16Property = new PlainProperty<>(nodeManager, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
            myInt16Property.setDataTypeId(Identifiers.Int16);
            if ((maxTypedValue != null) && (maxTypedValue.getValue() != null)) {
                myInt16Property.setValue(maxTypedValue.getValue());
            }
            myInt16Property.setDescription(new LocalizedText("", ""));
            range.addProperty(myInt16Property);
        }
    }


    private static void setInt64RangeMax(String maxValue, ValueData maxData, TypedValue<?> maxTypedValue, AASRangeType range, NodeManagerUaNode nodeManager)
            throws NumberFormatException, StatusException {
        if (maxValue != null) {
            PlainProperty<Long> myLongProperty = new PlainProperty<>(nodeManager, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
            myLongProperty.setDataTypeId(Identifiers.Int64);
            if ((maxTypedValue != null) && (maxTypedValue.getValue() != null)) {
                Object obj = maxTypedValue.getValue();
                if ((obj != null) && (!(obj instanceof Long))) {
                    obj = Long.valueOf(obj.toString());
                }
                myLongProperty.setValue(obj);
            }
            myLongProperty.setDescription(new LocalizedText("", ""));
            range.addProperty(myLongProperty);
        }
    }


    private static void setInt64RangeMin(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, NodeManagerUaNode nodeManager)
            throws StatusException, NumberFormatException {
        if (minValue != null) {
            PlainProperty<Long> myLongProperty = new PlainProperty<>(nodeManager, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
            myLongProperty.setDataTypeId(Identifiers.Int64);
            if ((minTypedValue != null) && (minTypedValue.getValue() != null)) {
                Object obj = minTypedValue.getValue();
                if ((obj != null) && (!(obj instanceof Long))) {
                    obj = Long.valueOf(obj.toString());
                }
                myLongProperty.setValue(obj);
            }
            myLongProperty.setDescription(new LocalizedText("", ""));
            range.addProperty(myLongProperty);
        }
    }


    private static void setInt32RangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                            TypedValue<?> maxTypedValue, NodeManagerUaNode nodeManager)
            throws StatusException {
        if (minValue != null) {
            PlainProperty<Integer> myIntProperty = new PlainProperty<>(nodeManager, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
            myIntProperty.setDataTypeId(Identifiers.Int32);
            if ((minTypedValue != null) && (minTypedValue.getValue() != null)) {
                myIntProperty.setValue(minTypedValue.getValue());
            }
            myIntProperty.setDescription(new LocalizedText("", ""));
            range.addProperty(myIntProperty);
        }

        if (maxValue != null) {
            PlainProperty<Integer> myIntProperty = new PlainProperty<>(nodeManager, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
            myIntProperty.setDataTypeId(Identifiers.Int32);
            if ((maxTypedValue != null) && (maxTypedValue.getValue() != null)) {
                myIntProperty.setValue(maxTypedValue.getValue());
            }
            myIntProperty.setDescription(new LocalizedText("", ""));
            range.addProperty(myIntProperty);
        }
    }


    private static void setDateTimeRangeMax(String maxValue, ValueData maxData, TypedValue<?> maxTypedValue, AASRangeType range, NodeManagerUaNode nodeManager)
            throws StatusException {
        if (maxValue != null) {
            PlainProperty<DateTime> myDateTimeProperty = new PlainProperty<>(nodeManager, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
            myDateTimeProperty.setDataTypeId(Identifiers.DateTime);
            if ((maxTypedValue != null) && (maxTypedValue.getValue() != null)) {
                if (maxTypedValue instanceof DateTimeValue) {
                    DateTimeValue dtval = (DateTimeValue) maxTypedValue;
                    DateTime dt = ValueConverter.createDateTime(dtval.getValue());
                    myDateTimeProperty.setValue(dt);
                }
                else {
                    myDateTimeProperty.setValue(maxTypedValue.getValue());
                }
            }
            myDateTimeProperty.setDescription(new LocalizedText("", ""));
            range.addProperty(myDateTimeProperty);
        }
    }


    private static void setDateTimeRangeMin(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, NodeManagerUaNode nodeManager)
            throws StatusException {
        if (minValue != null) {
            PlainProperty<DateTime> myDateTimeProperty = new PlainProperty<>(nodeManager, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
            myDateTimeProperty.setDataTypeId(Identifiers.DateTime);
            if ((minTypedValue != null) && (minTypedValue.getValue() != null)) {
                if (minTypedValue instanceof DateTimeValue) {
                    DateTimeValue dtval = (DateTimeValue) minTypedValue;
                    DateTime dt = ValueConverter.createDateTime(dtval.getValue());
                    myDateTimeProperty.setValue(dt);
                }
                else {
                    myDateTimeProperty.setValue(minTypedValue.getValue());
                }
            }
            myDateTimeProperty.setDescription(new LocalizedText("", ""));
            range.addProperty(myDateTimeProperty);
        }
    }


    private static void setBooleanRangeValues(String minValue, ValueData minData, TypedValue<?> minTypedValue, AASRangeType range, String maxValue, ValueData maxData,
                                              TypedValue<?> maxTypedValue, NodeManagerUaNode nodeManager)
            throws StatusException {
        if (minValue != null) {
            PlainProperty<Boolean> myBoolProperty = new PlainProperty<>(nodeManager, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
            myBoolProperty.setDataTypeId(Identifiers.Boolean);
            if ((minTypedValue != null) && (minTypedValue.getValue() != null)) {
                myBoolProperty.setValue(minTypedValue.getValue());
            }
            myBoolProperty.setDescription(new LocalizedText("", ""));
            range.addProperty(myBoolProperty);
        }

        if (maxValue != null) {
            PlainProperty<Boolean> myBoolProperty = new PlainProperty<>(nodeManager, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
            myBoolProperty.setDataTypeId(Identifiers.Boolean);
            if ((maxTypedValue != null) && (maxTypedValue.getValue() != null)) {
                myBoolProperty.setValue(maxTypedValue.getValue());
            }
            myBoolProperty.setDescription(new LocalizedText("", ""));
            range.addProperty(myBoolProperty);
        }
    }


    /**
     * Sets the data in the given Reference node.
     *
     * @param ref The desired UA reference object
     * @param refNode The AAS Reference object with the source data
     * @throws StatusException If the operation fails
     */
    public static void setAasReferenceData(Reference ref, AASReferenceType refNode) throws StatusException {
        setAasReferenceData(ref, refNode, VALUES_READ_ONLY);
    }


    /**
     * Sets the data in the given Reference node.
     *
     * @param ref The desired UA reference object
     * @param refNode The AAS Reference object with the source data
     * @param readOnly True if the value should be read-only
     * @throws StatusException If the operation fails
     */
    public static void setAasReferenceData(Reference ref, AASReferenceType refNode, boolean readOnly) throws StatusException {
        Ensure.requireNonNull(refNode, "refNode must be non-null");
        Ensure.requireNonNull(ref, "ref must be non-null");

        List<AASKeyDataType> keyList = new ArrayList<>();
        ref.getKeys().stream().map(k -> {
            AASKeyDataType keyValue = new AASKeyDataType();
            keyValue.setIdType(ValueConverter.getAasKeyType(k.getIdType()));
            keyValue.setType(ValueConverter.getAasKeyElementsDataType(k.getType()));
            keyValue.setValue(k.getValue());
            return keyValue;
        }).forEachOrdered(keyList::add);

        refNode.getKeysNode().setArrayDimensions(new UnsignedInteger[] {
                UnsignedInteger.valueOf(keyList.size())
        });
        if (readOnly) {
            refNode.getKeysNode().setAccessLevel(AccessLevelType.CurrentRead);
        }
        refNode.setKeys(keyList.toArray(AASKeyDataType[]::new));
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
        if (node == null) {
            throw new IllegalArgumentException(NODE_NULL);
        }
        else if (value == null) {
            throw new IllegalArgumentException(VALUE_NULL);
        }

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
        else {
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
        if (property == null) {
            throw new IllegalArgumentException("property is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException(VALUE_NULL);
        }

        LOG.debug("setPropertyValue: {} to {}", property.getBrowseName().getName(), value.getValue());

        // special treatment for some not directly supported types
        TypedValue<?> tv = value.getValue();
        Object obj = tv.getValue();
        if ((tv instanceof DecimalValue) || (tv instanceof IntegerValue)) {
            obj = Long.valueOf(obj.toString());
        }
        else if (tv instanceof DateTimeValue) {
            ZonedDateTime zdt = (ZonedDateTime) obj;
            obj = new DateTime(GregorianCalendar.from(zdt));
        }
        property.setValue(obj);
    }


    /**
     * Sets the values for the given Entity.
     *
     * @param entity The desired Entity.
     * @param value The new value.
     * @param nodeManager The corresponding Node Manager.
     * @throws StatusException If the operation fails
     */
    private static void setEntityPropertyValue(AASEntityType entity, EntityValue value, NodeManagerUaNode nodeManager) throws StatusException {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException(VALUE_NULL);
        }

        // EntityType
        entity.setEntityType(ValueConverter.getAasEntityType(value.getEntityType()));

        // GlobalAssetId
        if ((value.getGlobalAssetId() != null) && (!value.getGlobalAssetId().isEmpty())) {
            DefaultReference ref = new DefaultReference.Builder().keys(value.getGlobalAssetId()).build();
            setAasReferenceData(ref, entity.getGlobalAssetIdNode());
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
        if (file == null) {
            throw new IllegalArgumentException("file is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException(VALUE_NULL);
        }

        file.setMimeType(value.getMimeType());
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
        if (blob == null) {
            throw new IllegalArgumentException("blob is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException(VALUE_NULL);
        }

        // MimeType
        blob.setMimeType(value.getMimeType());

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
        if (refElement == null) {
            throw new IllegalArgumentException("refElement is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException(VALUE_NULL);
        }

        DefaultReference ref = new DefaultReference.Builder().keys(value.getKeys()).build();
        setAasReferenceData(ref, refElement.getValueNode());
    }


    /**
     * Sets the value for the given Range.
     *
     * @param range The desired Range.
     * @param value The new value
     * @throws StatusException If the operation fails
     */
    private static void setRangeValue(AASRangeType range, RangeValue<?> value) throws StatusException {
        if (range == null) {
            throw new IllegalArgumentException("range is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException(VALUE_NULL);
        }

        // special treatment for some not directly supported types
        TypedValue<?> tvmin = value.getMin();
        Object objmin = tvmin.getValue();
        if ((tvmin instanceof DecimalValue) || (tvmin instanceof IntegerValue)) {
            objmin = Long.valueOf(objmin.toString());
        }
        else if (tvmin instanceof DateTimeValue) {
            objmin = ValueConverter.createDateTime((ZonedDateTime) objmin);
        }

        TypedValue<?> tvmax = value.getMax();
        Object objmax = tvmax.getValue();
        if ((tvmax instanceof DecimalValue) || (tvmax instanceof IntegerValue)) {
            objmax = Long.valueOf(objmax.toString());
        }
        else if (tvmax instanceof DateTimeValue) {
            objmax = ValueConverter.createDateTime((ZonedDateTime) objmax);
        }

        if (range.getMinNode() != null) {
            range.setMin(objmin);
        }
        if (range.getMaxNode() != null) {
            range.setMax(objmax);
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
        if (multiLangProp == null) {
            throw new IllegalArgumentException("multiLangProp is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException(VALUE_NULL);
        }

        List<LangString> values = new ArrayList<>(value.getLangStringSet());
        if (multiLangProp.getValueNode() == null) {
            addMultiLanguageValueNode(multiLangProp, values.size(), nodeManager);
        }

        multiLangProp.getValueNode().setValue(ValueConverter.getLocalizedTextFromLangStringSet(values));
    }

}
