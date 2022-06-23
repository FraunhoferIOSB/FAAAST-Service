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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DecimalValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.IntegerValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.ArrayList;
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
     * The corresponding NodeManager.
     */
    private static NodeManagerUaNode nodeManager;

    /**
     * Sonar wants a private constructor.
     */
    private AasSubmodelElementHelper() {

    }


    /**
     * Sets the NodeManager.
     * 
     * @param value The corresponding NodeManager.
     */
    public static void setNodeManager(NodeManagerUaNode value) {
        nodeManager = value;
    }


    /**
     * Sets the values for the given RelationshipElement.
     *
     * @param aasElement The desired RelationshipElement.
     * @param value The new value.
     * @throws StatusException If the operation fails
     */
    public static void setRelationshipValue(AASRelationshipElementType aasElement, RelationshipElementValue value) throws StatusException {
        if (aasElement == null) {
            throw new IllegalArgumentException("aasElement is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException(VALUE_NULL);
        }

        try {
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
                        setDataElementValue(annotationNode, valueMap.get(annotationNode.getBrowseName().getName()));
                    }
                }
            }
            else {
                LOG.debug("setRelationshipValue: No AnnotatedRelationshipElement {}", aasElement.getBrowseName().getName());
            }

        }
        catch (Exception ex) {
            LOG.error("setAnnotatedRelationshipValue Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the value of the given SubmodelElement.
     *
     * @param subElem The desired SubmodelElement.
     * @param value The new value
     * @throws StatusException If the operation fails
     */
    public static void setSubmodelElementValue(AASSubmodelElementType subElem, ElementValue value) throws StatusException {
        try {
            LOG.debug("setSubmodelElementValue: {}", subElem.getBrowseName().getName());

            // changed the order because of an error in the derivation hierarchy of ElementValue
            // perhaps the order will be changed back to normal as soon as the error is fixed
            if ((value instanceof RelationshipElementValue) && (subElem instanceof AASRelationshipElementType)) {
                setRelationshipValue((AASRelationshipElementType) subElem, (RelationshipElementValue) value);
            }
            else if ((value instanceof EntityValue) && (subElem instanceof AASEntityType)) {
                setEntityValue((AASEntityType) subElem, (EntityValue) value);
            }
            else if (value instanceof DataElementValue) {
                setDataElementValue(subElem, (DataElementValue) value);
            }
            else {
                LOG.warn("SubmodelElement {} type not supported", subElem.getBrowseName().getName());
            }
        }
        catch (Exception ex) {
            LOG.error("setSubmodelElementValue Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds a Value Property to the given Blob Node.
     *
     * @param node The desired Blob Node
     */
    public static void addBlobValueNode(UaNode node) {
        try {
            NodeId myPropertyId = new NodeId(nodeManager.getNamespaceIndex(), node.getNodeId().getValue().toString() + "." + AASBlobType.VALUE);
            PlainProperty<ByteString> myProperty = new PlainProperty<>(nodeManager, myPropertyId,
                    UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASBlobType.getNamespaceUri(), AASBlobType.VALUE).toQualifiedName(nodeManager.getNamespaceTable()),
                    LocalizedText.english(AASBlobType.VALUE));
            myProperty.setDataTypeId(Identifiers.ByteString);
            myProperty.setDescription(new LocalizedText("", ""));
            node.addProperty(myProperty);
        }
        catch (Exception ex) {
            LOG.error("addBlobValueNode Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds a File Value Property to the given Node.
     *
     * @param fileNode The desired File Node.
     */
    public static void addFileValueNode(UaNode fileNode) {
        try {
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
        catch (Exception ex) {
            LOG.error("addFileFileNode Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the Value Node for the MultiLanguageProperty.
     *
     * @param node The desired MultiLanguageProperty Node
     * @param arraySize The desired Array Size.
     */
    public static void addMultiLanguageValueNode(UaNode node, int arraySize) {
        try {
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
        catch (Exception ex) {
            LOG.error("addMultiLanguageValueNode Exception", ex);
            throw ex;
        }
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
                //                case ByteString:
                //                    PlainProperty<ByteString> myBSProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
                //                    myBSProperty.setDataTypeId(Identifiers.ByteString);
                //                    // TO DO integrate Property value
                //                    //if (val != null) {
                //                    //    myBSProperty.setValue(((Base64Binary)val).getValue());
                //                    //}
                //                    prop.addProperty(myBSProperty);
                //                    break;
                //
                case Boolean:
                    PlainProperty<Boolean> myBoolProperty = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
                    myBoolProperty.setDataTypeId(Identifiers.Boolean);
                    if ((typedValue != null) && (typedValue.getValue() != null) && (typedValue.getValue().getValue() != null)) {
                        myBoolProperty.setValue(typedValue.getValue().getValue());
                    }
                    prop.addProperty(myBoolProperty);
                    break;

                //                case DateTime:
                //                    PlainProperty<DateTime> myDateProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
                //                    myDateProperty.setDataTypeId(Identifiers.DateTime);
                //                    // TO DO integrate Property value
                //                    //if (val != null) {
                //                    //    myDateProperty.setValue(new DateTime(((DateValue)val).getValue().toGregorianCalendar()));
                //                    //}
                //                    prop.addProperty(myDateProperty);
                //                    break;
                //
                case Int32:
                    PlainProperty<Integer> myIntProperty = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
                    myIntProperty.setDataTypeId(Identifiers.Int32);
                    if ((typedValue != null) && (typedValue.getValue() != null) && (typedValue.getValue().getValue() != null)) {
                        myIntProperty.setValue(typedValue.getValue().getValue());
                    }
                    prop.addProperty(myIntProperty);
                    break;
                //
                //                case UInt32:
                //                    PlainProperty<UnsignedInteger> myUIntProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
                //                    myUIntProperty.setDataTypeId(Identifiers.UInt32);
                //                    // TO DO integrate Property value
                //                    //if (val != null) {
                //                    //    myIntProperty.setValue(((IntValue)val).getValue());
                //                    //}
                //                    prop.addProperty(myUIntProperty);
                //                    break;
                //
                case Int64:
                    PlainProperty<Long> myLongProperty = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
                    myLongProperty.setDataTypeId(Identifiers.Int64);
                    if ((typedValue != null) && (typedValue.getValue() != null) && (typedValue.getValue().getValue() != null)) {
                        Object obj = typedValue.getValue().getValue();
                        if ((obj != null) && (!(obj instanceof Long))) {
                            obj = Long.parseLong(obj.toString());
                        }
                        myLongProperty.setValue(obj);
                    }
                    prop.addProperty(myLongProperty);
                    break;

                //                case UInt64:
                //                    PlainProperty<UnsignedLong> myULongProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
                //                    myULongProperty.setDataTypeId(Identifiers.UInt64);
                //                    // TO DO integrate Property value
                //                    //if (val != null) {
                //                    //    myLongProperty.setValue(((LongValue)val).getValue());
                //                    //}
                //                    prop.addProperty(myULongProperty);
                //                    break;
                //
                case Int16:
                    PlainProperty<Short> myInt16Property = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
                    myInt16Property.setDataTypeId(Identifiers.Int16);
                    if ((typedValue != null) && (typedValue.getValue() != null) && (typedValue.getValue().getValue() != null)) {
                        myInt16Property.setValue(typedValue.getValue().getValue());
                    }
                    prop.addProperty(myInt16Property);
                    break;

                //                case UInt16:
                //                    PlainProperty<UnsignedShort> myUInt16Property = new PlainProperty<>(this, myPropertyId, browseName, displayName);
                //                    myUInt16Property.setDataTypeId(Identifiers.UInt16);
                //                    // TO DO integrate Property value
                //                    //if (val != null) {
                //                    //    myInt16Property.setValue(((ShortValue)val).getValue());
                //                    //}
                //                    prop.addProperty(myUInt16Property);
                //                    break;
                //
                //                case Byte:
                //                    PlainProperty<UnsignedByte> myByteProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
                //                    myByteProperty.setDataTypeId(Identifiers.Byte);
                //                    // TO DO integrate Property value
                //                    //if (val != null) {
                //                    //    myByteProperty.setValue(((ByteValue)val).getValue());
                //                    //}
                //                    prop.addProperty(myByteProperty);
                //                    break;
                //
                case SByte:
                    PlainProperty<Byte> mySByteProperty = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
                    mySByteProperty.setDataTypeId(Identifiers.SByte);
                    if ((typedValue != null) && (typedValue.getValue() != null) && (typedValue.getValue().getValue() != null)) {
                        mySByteProperty.setValue(typedValue.getValue().getValue());
                    }
                    prop.addProperty(mySByteProperty);
                    break;

                case Double:
                    PlainProperty<Double> myDoubleProperty = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
                    myDoubleProperty.setDataTypeId(Identifiers.Double);
                    if ((typedValue != null) && (typedValue.getValue() != null) && (typedValue.getValue().getValue() != null)) {
                        myDoubleProperty.setValue(typedValue.getValue().getValue());
                    }
                    prop.addProperty(myDoubleProperty);
                    break;

                case Float:
                    PlainProperty<Float> myFloatProperty = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
                    myFloatProperty.setDataTypeId(Identifiers.Float);
                    if ((typedValue != null) && (typedValue.getValue() != null) && (typedValue.getValue().getValue() != null)) {
                        myFloatProperty.setValue(typedValue.getValue().getValue());
                    }
                    prop.addProperty(myFloatProperty);
                    break;

                //                case LocalizedText:
                //                    PlainProperty<LocalizedText> myLTProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
                //                    myLTProperty.setDataTypeId(Identifiers.LocalizedText);
                //                    // TO DO integrate Property value
                //                    myLTProperty.setValue(LocalizedText.english(stringVal));
                //                    //if (val != null) {
                //                    //    myLTProperty.setValue(((QNameValue)val).getValue().toString());
                //                    //}
                //                    prop.addProperty(myLTProperty);
                //                    break;
                //
                case String:
                    PlainProperty<String> myStringProperty = new PlainProperty<>(nodeManager, valueData.getNodeId(), valueData.getBrowseName(), valueData.getDisplayName());
                    myStringProperty.setDataTypeId(Identifiers.String);
                    if ((typedValue != null) && (typedValue.getValue() != null) && (typedValue.getValue().getValue() != null)) {
                        myStringProperty.setValue(typedValue.getValue().getValue());
                    }
                    prop.addProperty(myStringProperty);
                    break;
                //
                //                case UtcTime:
                //                    PlainProperty<DateTime> myTimeProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
                //                    myTimeProperty.setDataTypeId(Identifiers.UtcTime);
                //                    // TO DO integrate Property value
                //                    //if (val != null) {
                //                    //    myTimeProperty.setValue(new DateTime(((TimeValue)val).getValue().toGregorianCalendar()));
                //                    //}
                //                    prop.addProperty(myTimeProperty);
                //                    break;
                //
                //                //                case Duration:
                //                //                    PlainProperty<String> myDurProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
                //                //                    myDurProperty.setDataTypeId(Identifiers.String);
                //                //                    if (val != null) {
                //                //                        myDurProperty.setValue(((DurationValue)val).getValue().toString());
                //                //                    }
                //                //                    prop.addProperty(myDurProperty);
                //                //                    break;
                //
                default:
                    LOG.warn("setValueAndType: Property {}: Unknown type: {}; use string as default", prop.getBrowseName().getName(), aasProperty.getValueType());
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


    public static void setRangeValueAndType(String valueType, String minValue, String maxValue, AASRangeType range, ValueData minData,
                                            ValueData maxData)
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
                //                case ByteString:
                //                    if (minValue != null) {
                //                        PlainProperty<ByteString> myBSProperty = new PlainProperty<>(this, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
                //                        myBSProperty.setDataTypeId(Identifiers.ByteString);
                //                        // TO DO integrate Range value
                //                        //myBSProperty.setValue(((Base64Binary)minVal).getValue());
                //                        myBSProperty.setDescription(new LocalizedText("", ""));
                //                        range.addProperty(myBSProperty);
                //                    }
                //
                //                    if (maxValue != null) {
                //                        PlainProperty<ByteString> myBSProperty = new PlainProperty<>(this, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
                //                        myBSProperty.setDataTypeId(Identifiers.ByteString);
                //                        // TO DO integrate Range value
                //                        //myBSProperty.setValue(((Base64Binary)maxVal).getValue());
                //                        myBSProperty.setDescription(new LocalizedText("", ""));
                //                        range.addProperty(myBSProperty);
                //                    }
                //                    break;
                //
                case Boolean:
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
                    break;

                //                case DateTime:
                //                    if (minValue != null) {
                //                        PlainProperty<DateTime> myDateProperty = new PlainProperty<>(this, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
                //                        myDateProperty.setDataTypeId(Identifiers.DateTime);
                //                        // TO DO integrate Range value
                //                        //myDateProperty.setValue(new DateTime(((DateValue)minVal).getValue().toGregorianCalendar()));
                //                        myDateProperty.setDescription(new LocalizedText("", ""));
                //                        range.addProperty(myDateProperty);
                //                    }
                //
                //                    if (maxValue != null) {
                //                        PlainProperty<DateTime> myDateProperty = new PlainProperty<>(this, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
                //                        myDateProperty.setDataTypeId(Identifiers.DateTime);
                //                        // TO DO integrate Range value
                //                        //myDateProperty.setValue(new DateTime(((DateValue)maxVal).getValue().toGregorianCalendar()));
                //                        myDateProperty.setDescription(new LocalizedText("", ""));
                //                        range.addProperty(myDateProperty);
                //                    }
                //                    break;
                case Int32:
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
                    break;

                //                case UInt32:
                //                    if (minValue != null) {
                //                        PlainProperty<UnsignedInteger> myUIntProperty = new PlainProperty<>(this, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
                //                        myUIntProperty.setDataTypeId(Identifiers.UInt32);
                //                        // TO DO integrate Range value
                //                        //myIntProperty.setValue(((IntValue)minVal).getValue());
                //                        myUIntProperty.setDescription(new LocalizedText("", ""));
                //                        range.addProperty(myUIntProperty);
                //                    }
                //
                //                    if (maxValue != null) {
                //                        PlainProperty<UnsignedInteger> myUIntProperty = new PlainProperty<>(this, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
                //                        myUIntProperty.setDataTypeId(Identifiers.UInt32);
                //                        // TO DO integrate Range value
                //                        //myIntProperty.setValue(((IntValue)maxVal).getValue());
                //                        myUIntProperty.setDescription(new LocalizedText("", ""));
                //                        range.addProperty(myUIntProperty);
                //                    }
                //                    break;

                case Int64:
                    if (minValue != null) {
                        PlainProperty<Long> myLongProperty = new PlainProperty<>(nodeManager, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
                        myLongProperty.setDataTypeId(Identifiers.Int64);
                        if ((minTypedValue != null) && (minTypedValue.getValue() != null)) {
                            Object obj = minTypedValue.getValue();
                            if ((obj != null) && (!(obj instanceof Long))) {
                                obj = Long.parseLong(obj.toString());
                            }
                            myLongProperty.setValue(obj);
                        }
                        myLongProperty.setDescription(new LocalizedText("", ""));
                        range.addProperty(myLongProperty);
                    }

                    if (maxValue != null) {
                        PlainProperty<Long> myLongProperty = new PlainProperty<>(nodeManager, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
                        myLongProperty.setDataTypeId(Identifiers.Int64);
                        if ((maxTypedValue != null) && (maxTypedValue.getValue() != null)) {
                            Object obj = maxTypedValue.getValue();
                            if ((obj != null) && (!(obj instanceof Long))) {
                                obj = Long.parseLong(obj.toString());
                            }
                            myLongProperty.setValue(obj);
                        }
                        myLongProperty.setDescription(new LocalizedText("", ""));
                        range.addProperty(myLongProperty);
                    }
                    break;

                //                case UInt64:
                //                    if (minValue != null) {
                //                        PlainProperty<UnsignedLong> myULongProperty = new PlainProperty<>(this, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
                //                        myULongProperty.setDataTypeId(Identifiers.UInt64);
                //                        // TO DO integrate Range value
                //                        //myLongProperty.setValue(((LongValue)minVal).getValue());
                //                        myULongProperty.setDescription(new LocalizedText("", ""));
                //                        range.addProperty(myULongProperty);
                //                    }
                //
                //                    if (maxValue != null) {
                //                        PlainProperty<UnsignedLong> myULongProperty = new PlainProperty<>(this, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
                //                        myULongProperty.setDataTypeId(Identifiers.UInt64);
                //                        // TO DO integrate Range value
                //                        //myLongProperty.setValue(((LongValue)maxVal).getValue());
                //                        myULongProperty.setDescription(new LocalizedText("", ""));
                //                        range.addProperty(myULongProperty);
                //                    }
                //                    break;

                case Int16:
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
                    break;

                //                case UInt16:
                //                    if (minValue != null) {
                //                        PlainProperty<UnsignedShort> myUInt16Property = new PlainProperty<>(this, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
                //                        myUInt16Property.setDataTypeId(Identifiers.UInt16);
                //                        // TO DO integrate Range value
                //                        //myInt16Property.setValue(((ShortValue)minVal).getValue());
                //                        myUInt16Property.setDescription(new LocalizedText("", ""));
                //                        range.addProperty(myUInt16Property);
                //                    }
                //
                //                    if (maxValue != null) {
                //                        PlainProperty<UnsignedShort> myUInt16Property = new PlainProperty<>(this, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
                //                        myUInt16Property.setDataTypeId(Identifiers.UInt16);
                //                        // TO DO integrate Range value
                //                        //myInt16Property.setValue(((ShortValue)maxVal).getValue());
                //                        myUInt16Property.setDescription(new LocalizedText("", ""));
                //                        range.addProperty(myUInt16Property);
                //                    }
                //                    break;

                case SByte:
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
                    break;

                //                case Byte:
                //                    if (minValue != null) {
                //                        PlainProperty<UnsignedByte> myByteProperty = new PlainProperty<>(this, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
                //                        myByteProperty.setDataTypeId(Identifiers.Byte);
                //                        // TO DO integrate Range value
                //                        //myByteProperty.setValue(((ByteValue)minVal).getValue());
                //                        myByteProperty.setDescription(new LocalizedText("", ""));
                //                        range.addProperty(myByteProperty);
                //                    }
                //
                //                    if (maxValue != null) {
                //                        PlainProperty<UnsignedByte> myByteProperty = new PlainProperty<>(this, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
                //                        myByteProperty.setDataTypeId(Identifiers.Byte);
                //                        // TO DO integrate Range value
                //                        //myByteProperty.setValue(((ByteValue)maxVal).getValue());
                //                        myByteProperty.setDescription(new LocalizedText("", ""));
                //                        range.addProperty(myByteProperty);
                //                    }
                //                    break;
                //
                case Double:
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
                    break;

                case Float:
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
                    break;

                //                case LocalizedText:
                //                    if (minValue != null) {
                //                        PlainProperty<LocalizedText> myLTProperty = new PlainProperty<>(this, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
                //                        myLTProperty.setDataTypeId(Identifiers.String);
                //                        // TO DO integrate Range value
                //                        //myLTProperty.setValue(((QNameValue)minVal).getValue().toString());
                //                        myLTProperty.setDescription(new LocalizedText("", ""));
                //                        range.addProperty(myLTProperty);
                //                    }
                //
                //                    if (maxValue != null) {
                //                        PlainProperty<LocalizedText> myLTProperty = new PlainProperty<>(this, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
                //                        myLTProperty.setDataTypeId(Identifiers.LocalizedText);
                //                        // TO DO integrate Range value
                //                        //myQNameProperty.setValue(((QNameValue)maxVal).getValue().toString());
                //                        myLTProperty.setDescription(new LocalizedText("", ""));
                //                        range.addProperty(myLTProperty);
                //                    }
                //                    break;
                //
                case String:
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
                    break;

                //                case UtcTime:
                //                    if (minValue != null) {
                //                        PlainProperty<DateTime> myTimeProperty = new PlainProperty<>(this, minData.getNodeId(), minData.getBrowseName(), minData.getDisplayName());
                //                        myTimeProperty.setDataTypeId(Identifiers.DateTime);
                //                        // TO DO integrate Range value
                //                        //myTimeProperty.setValue(new DateTime(((TimeValue)minVal).getValue().toGregorianCalendar()));
                //                        myTimeProperty.setDescription(new LocalizedText("", ""));
                //                        range.addProperty(myTimeProperty);
                //                    }
                //
                //                    if (maxValue != null) {
                //                        PlainProperty<DateTime> myTimeProperty = new PlainProperty<>(this, maxData.getNodeId(), maxData.getBrowseName(), maxData.getDisplayName());
                //                        myTimeProperty.setDataTypeId(Identifiers.DateTime);
                //                        // TO DO integrate Range value
                //                        //myTimeProperty.setValue(new DateTime(((TimeValue)maxVal).getValue().toGregorianCalendar()));
                //                        myTimeProperty.setDescription(new LocalizedText("", ""));
                //                        range.addProperty(myTimeProperty);
                //                    }
                //                    break;
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

        try {
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
        catch (Exception ex) {
            LOG.error("setAasReferenceData Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the values for the given DataElement.
     *
     * @param node The desired DataElement.
     * @param value The new value.
     * @throws StatusException If the operation fails
     */
    private static void setDataElementValue(UaNode node, DataElementValue value) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException(NODE_NULL);
        }
        else if (value == null) {
            throw new IllegalArgumentException(VALUE_NULL);
        }

        try {
            if ((node instanceof AASPropertyType) && (value instanceof PropertyValue)) {
                setPropertyValue((AASPropertyType) node, (PropertyValue) value);
            }
            else if ((node instanceof AASFileType) && (value instanceof FileValue)) {
                setFileValue((AASFileType) node, (FileValue) value);
            }
            else if ((node instanceof AASBlobType) && (value instanceof BlobValue)) {
                setBlobValue((AASBlobType) node, (BlobValue) value);
            }
            else if ((node instanceof AASReferenceElementType) && (value instanceof ReferenceElementValue)) {
                setReferenceElementValue((AASReferenceElementType) node, (ReferenceElementValue) value);
            }
            else if ((node instanceof AASRangeType) && (value instanceof RangeValue)) {
                setRangeValue((AASRangeType) node, (RangeValue<?>) value);
            }
            else if ((node instanceof AASMultiLanguagePropertyType) && (value instanceof MultiLanguagePropertyValue)) {
                setMultiLanguagePropertyValue((AASMultiLanguagePropertyType) node, (MultiLanguagePropertyValue) value);
            }
            else {
                LOG.warn("setDataElementValue: unknown or invalid DataElement or value: {}; Class: {}; Value Class: {}", node.getBrowseName().getName(), node.getClass(),
                        value.getClass());
            }
        }
        catch (Exception ex) {
            LOG.error("setDataElementValue Exception", ex);
            throw ex;
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

        try {
            // special treatment for some not directly supported types
            TypedValue<?> tv = value.getValue();
            Object obj = tv.getValue();
            if ((tv instanceof DecimalValue) || (tv instanceof IntegerValue)) {
                obj = Long.parseLong(obj.toString());
            }
            property.setValue(obj);

            //            switch (property.getValueType()) {
            //                case ByteString:
            //                    // TO DO integrate Property value
            //                    property.setValue(value.getValue());
            //                    break;
            //
            //                case Boolean:
            //                    // TO DO integrate Property value
            //                    property.setValue(value.getValue());
            //                    break;
            //
            //                case DateTime:
            //                    // TO DO integrate Property value
            //                    property.setValue(value.getValue());
            //                    break;
            //
            //                case Int32:
            //                    // TO DO integrate Property value
            //                    property.setValue(value.getValue());
            //                    break;
            //
            //                case UInt32:
            //                    // TO DO integrate Property value
            //                    property.setValue(value.getValue());
            //                    break;
            //
            //                case Int64:
            //                    // TO DO integrate Property value
            //                    property.setValue(value.getValue());
            //                    break;
            //
            //                case UInt64:
            //                    // TO DO integrate Property value
            //                    property.setValue(value.getValue());
            //                    break;
            //
            //                case Int16:
            //                    // TO DO integrate Property value
            //                    property.setValue(value.getValue());
            //                    break;
            //
            //                case UInt16:
            //                    // TO DO integrate Property value
            //                    property.setValue(value.getValue());
            //                    break;
            //
            //                case Byte:
            //                    // TO DO integrate Property value
            //                    property.setValue(value.getValue());
            //                    break;
            //
            //                case SByte:
            //                    // TO DO integrate Property value
            //                    property.setValue(value.getValue());
            //                    break;
            //
            //                case Double:
            //                    // TO DO integrate Property value
            //                    property.setValue(value.getValue());
            //                    break;
            //
            //                case Float:
            //                    // TO DO integrate Property value
            //                    property.setValue(value.getValue());
            //                    break;
            //
            //                case LocalizedText:
            //                    // TO DO integrate Property value
            //                    property.setValue(value.getValue());
            //                    break;
            //
            //                case String:
            //                    // TO DO integrate Property value
            //                    property.setValue(value.getValue());
            //                    break;
            //
            //                case UtcTime:
            //                    // TO DO integrate Property value
            //                    property.setValue(value.getValue());
            //                    break;
            //
            //                default:
            //                    logger.warn("setPropertyValue: Property " + property.getBrowseName().getName() + ": Unknown type: " + property.getValueType());
            //                    break;
            //            }
        }
        catch (Exception ex) {
            LOG.error("setPropertyValue Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the values for the given Entity.
     *
     * @param entity The desired Entity.
     * @param value The new value.
     * @throws StatusException If the operation fails
     */
    private static void setEntityValue(AASEntityType entity, EntityValue value) throws StatusException {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException(VALUE_NULL);
        }

        try {
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
                        setSubmodelElementValue((AASSubmodelElementType) statementNode1, value.getStatements().get(statementNode1.getBrowseName().getName()));
                    }
                }
            }
        }
        catch (Exception ex) {
            LOG.error("setEntityValue Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the values for the given File.
     *
     * @param file The desired file.
     * @param value The new value
     * @throws StatusException If the operation fails
     */
    private static void setFileValue(AASFileType file, FileValue value) throws StatusException {
        if (file == null) {
            throw new IllegalArgumentException("file is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException(VALUE_NULL);
        }

        try {
            file.setMimeType(value.getMimeType());
            if (value.getValue() != null) {
                if (file.getValueNode() == null) {
                    addFileValueNode(file);
                }

                file.setValue(value.getValue());
            }
        }
        catch (Exception ex) {
            LOG.error("setFileValue Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the values for the given Blob.
     *
     * @param blob The desired blob.
     * @param value The new value
     * @throws StatusException If the operation fails
     */
    private static void setBlobValue(AASBlobType blob, BlobValue value) throws StatusException {
        if (blob == null) {
            throw new IllegalArgumentException("blob is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException(VALUE_NULL);
        }

        try {
            // MimeType
            blob.setMimeType(value.getMimeType());

            // Value
            if (value.getValue() != null) {
                if (blob.getValueNode() == null) {
                    addBlobValueNode(blob);
                }

                blob.setValue(ByteString.valueOf(value.getValue()));
            }
        }
        catch (Exception ex) {
            LOG.error("setBlobValue Exception", ex);
            throw ex;
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

        try {
            DefaultReference ref = new DefaultReference.Builder().keys(value.getKeys()).build();
            setAasReferenceData(ref, refElement.getValueNode());
        }
        catch (Exception ex) {
            LOG.error("setReferenceElementValue Exception", ex);
            throw ex;
        }
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

        try {
            // special treatment for some not directly supported types
            TypedValue<?> tvmin = value.getMin();
            Object objmin = tvmin.getValue();
            if ((tvmin instanceof DecimalValue) || (tvmin instanceof IntegerValue)) {
                objmin = Long.parseLong(objmin.toString());
            }

            TypedValue<?> tvmax = value.getMax();
            Object objmax = tvmax.getValue();
            if ((tvmax instanceof DecimalValue) || (tvmax instanceof IntegerValue)) {
                objmax = Long.parseLong(objmax.toString());
            }

            range.setMin(objmin);
            range.setMax(objmax);
        }
        catch (Exception ex) {
            LOG.error("setRangeValue Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the values for the given MultiLanguageProperty.
     *
     * @param multiLangProp The desired MultiLanguageProperty.
     * @param value The new value
     * @throws StatusException If the operation fails
     */
    private static void setMultiLanguagePropertyValue(AASMultiLanguagePropertyType multiLangProp, MultiLanguagePropertyValue value) throws StatusException {
        if (multiLangProp == null) {
            throw new IllegalArgumentException("multiLangProp is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException(VALUE_NULL);
        }

        try {
            List<LangString> values = new ArrayList<>(value.getLangStringSet());
            if (multiLangProp.getValueNode() == null) {
                addMultiLanguageValueNode(multiLangProp, values.size());
            }

            multiLangProp.getValueNode().setValue(ValueConverter.getLocalizedTextFromLangStringSet(values));
        }
        catch (Exception ex) {
            LOG.error("setMultiLanguagePropertyValue Exception", ex);
            throw ex;
        }
    }

}
