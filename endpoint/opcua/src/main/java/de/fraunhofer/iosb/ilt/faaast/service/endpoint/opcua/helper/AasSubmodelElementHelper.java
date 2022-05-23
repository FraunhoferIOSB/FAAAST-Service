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
import io.adminshell.aas.v3.model.LangString;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tino Bischoff
 */
public class AasSubmodelElementHelper {
    /**
     * The corresponding NodeManager.
     */
    public static NodeManagerUaNode NODE_MANAGER;

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
                LOG.info("setRelationshipValue: No AnnotatedRelationshipElement {}", aasElement.getBrowseName().getName());
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
            NodeId myPropertyId = new NodeId(NODE_MANAGER.getNamespaceIndex(), node.getNodeId().getValue().toString() + "." + AASBlobType.VALUE);
            PlainProperty<ByteString> myProperty = new PlainProperty<>(NODE_MANAGER, myPropertyId,
                    UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASBlobType.getNamespaceUri(), AASBlobType.VALUE).toQualifiedName(NODE_MANAGER.getNamespaceTable()),
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
            NodeId myPropertyId = new NodeId(NODE_MANAGER.getNamespaceIndex(), fileNode.getNodeId().getValue().toString() + "." + AASFileType.VALUE);
            PlainProperty<String> myProperty = new PlainProperty<>(NODE_MANAGER, myPropertyId,
                    UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASFileType.getNamespaceUri(), AASFileType.VALUE).toQualifiedName(NODE_MANAGER.getNamespaceTable()),
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
            NodeId propId = new NodeId(NODE_MANAGER.getNamespaceIndex(), node.getNodeId().getValue().toString() + "." + AASMultiLanguagePropertyType.VALUE);
            PlainProperty<LocalizedText[]> myLTProperty = new PlainProperty<>(NODE_MANAGER, propId,
                    UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASMultiLanguagePropertyType.getNamespaceUri(), AASMultiLanguagePropertyType.VALUE)
                            .toQualifiedName(NODE_MANAGER.getNamespaceTable()),
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
    @SuppressWarnings("java:S125")
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
     * Sets the data in the given Reference node.
     *
     * @param ref The desired UA reference object
     * @param refNode The AAS Reference object with the source data
     * @throws StatusException If the operation fails
     */
    private static void setAasReferenceData(Reference ref, AASReferenceType refNode) throws StatusException {
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
    private static void setAasReferenceData(Reference ref, AASReferenceType refNode, boolean readOnly) throws StatusException {
        if (refNode == null) {
            throw new IllegalArgumentException("refNode is null");
        }
        else if (ref == null) {
            throw new IllegalArgumentException("ref is null");
        }

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
