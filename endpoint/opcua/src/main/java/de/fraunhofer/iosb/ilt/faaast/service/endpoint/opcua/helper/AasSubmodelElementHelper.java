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
import com.prosysopc.ua.nodes.UaObject;
import com.prosysopc.ua.nodes.UaReference;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.nodes.PlainProperty;
import com.prosysopc.ua.stack.builtintypes.ByteString;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.Identifiers;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.EntityCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.ReferenceCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ValueData;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import opc.ua.aas.Ids;
import opc.ua.aas.objecttypes.AASAnnotatedRelationshipElementType;
import opc.ua.aas.objecttypes.AASBlobType;
import opc.ua.aas.objecttypes.AASDataElementObjectType;
import opc.ua.aas.objecttypes.AASEntityType;
import opc.ua.aas.objecttypes.AASFileType;
import opc.ua.aas.objecttypes.AASRelationshipElementType;
import opc.ua.aas.objecttypes.AASSubmodelElementObjectType;
import opc.ua.aas.variabletypes.AASMultiLanguagePropertyType;
import opc.ua.aas.variabletypes.AASPropertyType;
import opc.ua.aas.variabletypes.AASReferenceElementType;
import opc.ua.iosb.aas.datatypes.AASRange;
import opc.ua.iosb.aas.variabletypes.AASRangeType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class with helper methods for SubmodelElements used by the NodeManager
 */
public class AasSubmodelElementHelper {
    /**
     * The logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AasSubmodelElementHelper.class);

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
     * @throws ServiceResultException If the operation fails
     */
    public static void setRelationshipValue(AASRelationshipElementType aasElement, RelationshipElementValue value, NodeManagerUaNode nodeManager)
            throws StatusException, ServiceResultException {
        Ensure.requireNonNull(aasElement, "aasElement must not be null");
        Ensure.requireNonNull(value, VALUE_NULL);

        ReferenceCreator.setAasReferenceData(value.getFirst(), aasElement.getFirst());
        ReferenceCreator.setAasReferenceData(value.getSecond(), aasElement.getSecond());

        if ((aasElement instanceof AASAnnotatedRelationshipElementType aasAnnotated) && (value instanceof AnnotatedRelationshipElementValue annotated)) {
            var annotationVariables = aasAnnotated.getS_AnnotationVariable_Nodes();
            var annotationObjects = getSubmodelElementComponentObjects(aasAnnotated, nodeManager, AASDataElementObjectType.class);

            Map<String, DataElementValue> valueMap = annotated.getAnnotations();
            for (var annotationNode: annotationVariables) {
                setDataElementValue(annotationNode, valueMap.get(annotationNode.getBrowseName().getName()), nodeManager);
            }
            for (var annotationNode: annotationObjects) {
                setDataElementValue(annotationNode, valueMap.get(annotationNode.getBrowseName().getName()), nodeManager);
            }

            int annotationCount = annotationObjects.size() + annotationVariables.size();
            if (annotationCount != valueMap.size()) {
                LOGGER.error("setRelationshipValue Size of Value ({}) doesn't match the number of AnnotationNodes ({})", valueMap.size(), annotationCount);
                throw new IllegalArgumentException("Size of Value doesn't match the number of AnnotationNodes");
            }
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
     * @throws ServiceResultException If the operation fails
     */
    public static void setSubmodelElementValue(UaNode subElem, ElementValue value, NodeManagerUaNode nodeManager)
            throws StatusException, ValueFormatException, ServiceResultException {
        LOGGER.trace("setSubmodelElementValue: {}", subElem.getBrowseName().getName());

        // changed the order because of an error in the derivation hierarchy of ElementValue
        // perhaps the order will be changed back to normal as soon as the error is fixed
        if ((value instanceof RelationshipElementValue relElemValue) && (subElem instanceof AASRelationshipElementType relElemSub)) {
            setRelationshipValue(relElemSub, relElemValue, nodeManager);
        }
        else if ((value instanceof EntityValue entityValue) && (subElem instanceof AASEntityType entityTypeSub)) {
            setEntityPropertyValue(entityTypeSub, entityValue, nodeManager);
        }
        else if (value instanceof DataElementValue dataElementValue) {
            setDataElementValue(subElem, dataElementValue, nodeManager);
        }
        else {
            LOGGER.warn("setSubmodelElementValue: SubmodelElement {} type not supported", subElem.getBrowseName().getName());
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
                UaQualifiedName.from(Ids.AASBlobType.getNamespaceUri(), AASBlobType.VALUE).toQualifiedName(nodeManager.getNamespaceTable()),
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
                UaQualifiedName.from(Ids.AASFileType.getNamespaceUri(), AASFileType.VALUE).toQualifiedName(nodeManager.getNamespaceTable()),
                LocalizedText.english(AASFileType.VALUE));
        property.setDataTypeId(Identifiers.String);
        if (AasServiceNodeManager.VALUES_READ_ONLY) {
            property.setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
        }
        property.setDescription(new LocalizedText("", ""));
        fileNode.addProperty(property);
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
            LOGGER.atInfo().log("setPropertyValueAndType: {}", aasProperty.getIdShort());
            PropertyValue typedValue = ElementValueMapper.toValue(aasProperty, PropertyValue.class);

            setPropertyValue(prop, typedValue);
            prop.setDataTypeId(ValueConverter.convertDataTypeDefToNodeId(aasProperty.getValueType(), valueData.getNodeManager()));

            if (prop.getDescription() == null) {
                prop.setDescription(new LocalizedText("", ""));
            }
        }
        catch (Exception ex) {
            LOGGER.error("setPropertyValueAndType Exception", ex);
        }
    }


    /**
     * Sets the values for the given Range from the corresponding AAS Tange.
     *
     * @param aasRange The AAS Range.
     * @param range The OPC UA Range.
     * @throws ValueMappingException Error when mapping to ElementValue fails
     * @throws StatusException If the operation fails
     */
    public static void setRangeValue(Range aasRange, AASRangeType range) throws ValueMappingException, StatusException {
        RangeValue<?> typedValue = ElementValueMapper.toValue(aasRange, RangeValue.class);
        setRangeValue(range, typedValue);
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
        if ((node instanceof AASPropertyType propertyTypeNode) && (value instanceof PropertyValue propertyValue)) {
            setPropertyValue(propertyTypeNode, propertyValue);
        }
        else if ((node instanceof AASFileType fileTypeNode) && (value instanceof FileValue fileValue)) {
            setFilePropertyValue(fileTypeNode, fileValue, nodeManager);
        }
        else if ((node instanceof AASBlobType blobTypeNode) && (value instanceof BlobValue blobValue)) {
            setBlobValue(blobTypeNode, blobValue, nodeManager);
        }
        else if ((node instanceof AASReferenceElementType referenceElementNode) && (value instanceof ReferenceElementValue referenceElementValue)) {
            setReferenceElementValue(referenceElementNode, referenceElementValue);
        }
        else if ((node instanceof AASRangeType rangeNode) && (value instanceof RangeValue rangeValue)) {
            setRangeValue(rangeNode, rangeValue);
        }
        else if ((node instanceof AASMultiLanguagePropertyType multiLanguageNode) && (value instanceof MultiLanguagePropertyValue multiLanguageValue)) {
            setMultiLanguagePropertyValue(multiLanguageNode, multiLanguageValue);
        }
        else if (value != null) {
            LOGGER.warn("setDataElementValue: unknown or invalid DataElement or value: {}; Class: {}; Value Class: {}", node.getBrowseName().getName(), node.getClass(),
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
        LOGGER.trace("setPropertyValue: {} to {}", property.getBrowseName().getName(), value.getValue());
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
    private static void setEntityPropertyValue(AASEntityType entity, EntityValue value, NodeManagerUaNode nodeManager)
            throws StatusException, ValueFormatException, ServiceResultException {
        // EntityType
        entity.setEntityType(ValueConverter.convertEntityType(value.getEntityType()));

        // globalAssetId
        if ((value.getGlobalAssetId() != null) && (!value.getGlobalAssetId().isEmpty())) {
            EntityCreator.setGlobalAssetIdData(entity, value.getGlobalAssetId(), nodeManager);
        }

        // Statements
        Map<String, ElementValue> valueMap = value.getStatements();
        var statementVariables = entity.getS_StatementVariable_Nodes();
        var statementObjects = getSubmodelElementComponentObjects(entity, nodeManager, AASSubmodelElementObjectType.class);
        int statementCount = getListCount(statementObjects, statementVariables);
        if (statementCount != valueMap.size()) {
            LOGGER.warn("Size of Value ({}) doesn't match the number of StatementNodes ({})", valueMap.size(), statementCount);
            throw new IllegalArgumentException("Size of Value doesn't match the number of StatementNodes");
        }

        if (statementVariables != null) {
            for (var statementNode: statementVariables) {
                if (value.getStatements().containsKey(statementNode.getBrowseName().getName())) {
                    setSubmodelElementValue(statementNode, value.getStatements().get(statementNode.getBrowseName().getName()), nodeManager);
                }
            }
        }
        for (var statementNode: statementObjects) {
            if (value.getStatements().containsKey(statementNode.getBrowseName().getName())) {
                setSubmodelElementValue(statementNode, value.getStatements().get(statementNode.getBrowseName().getName()), nodeManager);
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
        ReferenceCreator.setAasReferenceData(value.getValue(), refElement);
    }


    private static void setMultiLanguagePropertyValue(AASMultiLanguagePropertyType multiLangProp, MultiLanguagePropertyValue value)
            throws StatusException {
        List<LangStringTextType> values = new ArrayList<>(value.getLangStringSet());

        multiLangProp.setValue(ValueConverter.convertLangStringSet(values));
    }


    private static <T extends AASSubmodelElementObjectType> List<T> getSubmodelElementComponentObjects(UaObject baseNode, NodeManagerUaNode nodeManager, Class<T> type)
            throws ServiceResultException {
        List<T> retval = new ArrayList<>();
        UaReference[] refs = baseNode.getForwardReferences(nodeManager.getNamespaceTable().toNodeId(Ids.AASHasComponent));
        for (var ref: refs) {
            if (ref.getTargetNode().getClass().equals(type)) {
                retval.add((T) ref.getTargetNode());
            }
        }
        return retval;
    }


    private static int getListCount(List<?> list1, List<?> list2) {
        int retval = 0;
        if (list1 != null) {
            retval += list1.size();
        }
        if (list2 != null) {
            retval += list2.size();
        }
        return retval;
    }


    /**
     * Sets the value for the given Range.
     *
     * @param range The desired Range.
     * @param value The new value
     * @throws StatusException If the operation fails
     */
    private static void setRangeValue(AASRangeType range, RangeValue<?> value) throws StatusException {
        AASRange rangeValue = new AASRange();
        rangeValue.setMin(ValueConverter.convertTypedValue(value.getMin()));
        rangeValue.setMax(ValueConverter.convertTypedValue(value.getMax()));
        range.setValue(rangeValue);
    }

}
