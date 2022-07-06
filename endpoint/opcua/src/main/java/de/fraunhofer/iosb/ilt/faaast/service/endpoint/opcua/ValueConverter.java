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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.stack.builtintypes.ByteString;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.builtintypes.DateTime;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.Variant;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.core.StatusCodes;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DateTimeValue;
import io.adminshell.aas.v3.model.AssetKind;
import io.adminshell.aas.v3.model.Blob;
import io.adminshell.aas.v3.model.Entity;
import io.adminshell.aas.v3.model.EntityType;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.ModelingKind;
import io.adminshell.aas.v3.model.MultiLanguageProperty;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.ReferenceElement;
import io.adminshell.aas.v3.model.RelationshipElement;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import opc.i4aas.AASAssetKindDataType;
import opc.i4aas.AASEntityTypeDataType;
import opc.i4aas.AASIdentifierTypeDataType;
import opc.i4aas.AASKeyDataType;
import opc.i4aas.AASKeyElementsDataType;
import opc.i4aas.AASKeyTypeDataType;
import opc.i4aas.AASModelingKindDataType;
import opc.i4aas.AASValueTypeDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to convert values between the AASService types and the OPC UA Types
 *
 * @author Tino Bischoff
 */
@SuppressWarnings({
        "java:S3252",
        "java:S2139"
})
public class ValueConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValueConverter.class);

    /**
     * Private constructor to prevent class from being instantiated.
     */
    private ValueConverter() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * Converts the AAS DataTypeDef into the corresponding OPC UA type (NodeId)
     *
     * @param valueType The desired valueType
     * @return The corresponding OPC UA type (NodeId)
     */
    public static NodeId convertValueTypeStringToNodeId(String valueType) {
        NodeId retval = null;

        try {
            switch (valueType.toLowerCase()) {
                case "bytestring":
                    retval = Identifiers.ByteString;
                    break;

                case "boolean":
                    retval = Identifiers.Boolean;
                    break;

                case "datetime":
                    retval = Identifiers.DateTime;
                    break;

                case "decimal":
                    retval = Identifiers.Decimal;
                    break;

                case "integer":
                    retval = Identifiers.Integer;
                    break;

                case "int":
                    retval = Identifiers.Int32;
                    break;

                case "unsignedint":
                    retval = Identifiers.UInt32;
                    break;

                case "long":
                    retval = Identifiers.Int64;
                    break;

                case "unsignedlong":
                    retval = Identifiers.Int64;
                    break;

                case "short":
                    retval = Identifiers.Int16;
                    break;

                case "unsignedshort":
                    retval = Identifiers.UInt16;
                    break;

                case "byte":
                    retval = Identifiers.SByte;
                    break;

                case "unsignedbyte":
                    retval = Identifiers.Byte;
                    break;

                case "double":
                    retval = Identifiers.Double;
                    break;

                case "float":
                    retval = Identifiers.Float;
                    break;

                case "langstring":
                    retval = Identifiers.LocalizedText;
                    break;

                case "string":
                    retval = Identifiers.String;
                    break;

                case "time":
                    retval = Identifiers.UtcTime;
                    break;
                default:
                    LOGGER.warn("convertValueTypeStringToNodeId: Unknown type: {}", valueType);
                    retval = NodeId.NULL;
                    break;
            }
        }
        catch (Exception ex) {
            LOGGER.error("convertValueTypeStringToNodeId Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Converts the given String to the corresponding AASValueTypeDataType
     *
     * @param value The desired value.
     * @return The corresponding AASValueTypeDataType
     */
    public static AASValueTypeDataType stringToValueType(String value) {
        AASValueTypeDataType retval = null;

        try {
            switch (value.toLowerCase()) {
                case "boolean":
                    retval = AASValueTypeDataType.Boolean;
                    break;

                case "unsignedbyte":
                    retval = AASValueTypeDataType.Byte;
                    break;

                case "byte":
                    retval = AASValueTypeDataType.SByte;
                    break;

                case "short":
                    retval = AASValueTypeDataType.UInt16;
                    break;

                case "unsignedshort":
                    retval = AASValueTypeDataType.Int16;
                    break;

                case "int":
                    retval = AASValueTypeDataType.Int32;
                    break;

                case "integer":
                    LOGGER.warn("stringToValueType: Integer not supported");
                    retval = AASValueTypeDataType.Int64;
                    break;

                case "unsignedint":
                    retval = AASValueTypeDataType.UInt32;
                    break;

                case "long":
                    retval = AASValueTypeDataType.Int64;
                    break;

                case "unsignedlong":
                    retval = AASValueTypeDataType.UInt64;
                    break;

                case "float":
                    retval = AASValueTypeDataType.Float;
                    break;

                case "double":
                    retval = AASValueTypeDataType.Double;
                    break;

                case "string":
                    retval = AASValueTypeDataType.String;
                    break;

                case "datetime":
                    retval = AASValueTypeDataType.DateTime;
                    break;

                case "bytestring":
                    retval = AASValueTypeDataType.ByteString;
                    break;

                case "langstring":
                    retval = AASValueTypeDataType.LocalizedText;
                    break;

                case "time":
                    retval = AASValueTypeDataType.UtcTime;
                    break;

                case "decimal":
                    LOGGER.warn("stringToValueType: Decimal not supported!");
                    retval = AASValueTypeDataType.Int64;
                    break;

                default:
                    LOGGER.warn("stringToValueType: unknown value: {}", value);
                    throw new IllegalArgumentException("unknown value: " + value);
            }
        }
        catch (Exception ex) {
            LOGGER.error("stringToValueType Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Converts the given datatype to the corresponding AASValueTypeDataType.
     *
     * @param type The desired datatype
     * @return The corresponding AASValueTypeDataType
     */
    public static AASValueTypeDataType datatypeToValueType(Datatype type) {
        AASValueTypeDataType retval;

        switch (type) {
            case DOUBLE:
                retval = AASValueTypeDataType.Double;
                break;

            case INT:
                retval = AASValueTypeDataType.Int32;
                break;

            case STRING:
                retval = AASValueTypeDataType.String;
                break;

            case BOOLEAN:
                retval = AASValueTypeDataType.Boolean;
                break;

            case BYTE:
                retval = AASValueTypeDataType.SByte;
                break;

            case DECIMAL:
                LOGGER.warn("datatypeToValueType: Decimal not supported!");
                retval = AASValueTypeDataType.Int64;
                break;

            case FLOAT:
                retval = AASValueTypeDataType.Float;
                break;

            case INTEGER:
                LOGGER.warn("datatypeToValueType: Integer not supported - map to Long!");
                retval = AASValueTypeDataType.Int64;
                break;

            case LONG:
                retval = AASValueTypeDataType.Int64;
                break;

            case SHORT:
                retval = AASValueTypeDataType.Int16;
                break;

            case DATE_TIME:
                retval = AASValueTypeDataType.DateTime;
                break;

            default:
                LOGGER.warn("datatypeToValueType: unknown type: {}", type);
                throw new IllegalArgumentException("unknown type: " + type);
        }

        return retval;
    }


    /**
     * Converts the given ModelingKind to the corresponding
     * AASModelingKindDataType.
     *
     * @param value the desired ModelingKind
     * @return The corresponding AASModelingKindDataType
     */
    public static AASModelingKindDataType convertModelingKind(ModelingKind value) {
        AASModelingKindDataType retval;
        if (value == null) {
            LOGGER.warn("convertModelingKind: value == null");
            retval = AASModelingKindDataType.Instance;
        }
        else {
            switch (value) {
                case INSTANCE:
                    retval = AASModelingKindDataType.Instance;
                    break;
                case TEMPLATE:
                    retval = AASModelingKindDataType.Template;
                    break;
                default:
                    LOGGER.warn("convertModelingKind: unknown value {}", value);
                    throw new IllegalArgumentException("unknown ModelingKind: " + value);
            }
        }

        return retval;
    }


    /**
     * Converts the given IdentifierType to the corresponding
     * AASIdentifierTypeDataType.
     *
     * @param value The desired IdentifierType
     * @return The corresponding AASIdentifierTypeDataType.
     */
    public static AASIdentifierTypeDataType convertIdentifierType(IdentifierType value) {
        AASIdentifierTypeDataType retval;
        switch (value) {
            case CUSTOM:
                retval = AASIdentifierTypeDataType.Custom;
                break;
            case IRI:
                retval = AASIdentifierTypeDataType.IRI;
                break;
            case IRDI:
                retval = AASIdentifierTypeDataType.IRDI;
                break;
            default:
                LOGGER.warn("convertIdentifierType: unknown value {}", value);
                throw new IllegalArgumentException("unknown IdentifierType: " + value);
        }
        return retval;
    }


    /**
     * Converts the given AssetKind to the corresponding AASAssetKindDataType.
     *
     * @param value The desired AssetKind
     * @return The corresponding AASAssetKindDataType
     */
    public static AASAssetKindDataType convertAssetKind(AssetKind value) {
        AASAssetKindDataType retval;
        switch (value) {
            case INSTANCE:
                retval = AASAssetKindDataType.Instance;
                break;
            case TYPE:
                retval = AASAssetKindDataType.Type;
                break;
            default:
                LOGGER.warn("convertAssetKind: unknown value {}", value);
                throw new IllegalArgumentException("unknown KeyType: " + value);
        }
        return retval;
    }


    /**
     * Converts the given EntityType to the corresponding AASEntityTypeDataType.
     *
     * @param value The desired EntityType
     * @return The corresponding AASEntityTypeDataType
     */
    public static AASEntityTypeDataType getAasEntityType(EntityType value) {
        return AASEntityTypeDataType.valueOf(value.ordinal());
    }


    /**
     * Converts the given AASEntityTypeDataType to the corresponding EntityType.
     *
     * @param value The desired AASEntityTypeDataType
     * @return The corresponding EntityType
     */
    public static EntityType getEntityType(AASEntityTypeDataType value) {
        EntityType retval;

        switch (value) {
            case CoManagedEntity:
                retval = EntityType.CO_MANAGED_ENTITY;
                break;

            case SelfManagedEntity:
                retval = EntityType.SELF_MANAGED_ENTITY;
                break;

            default:
                LOGGER.warn("getEntityType: unknown value: {}", value);
                throw new IllegalArgumentException("unknown value: " + value);
        }

        return retval;
    }


    /**
     * Gets a LocalizedText array from an AAS LangString Set.
     *
     * @param value The desired AAS Lang String
     * @return The corresponding LocalizedText array
     */
    public static LocalizedText[] getLocalizedTextFromLangStringSet(List<LangString> value) {
        LocalizedText[] retval = null;

        try {
            ArrayList<LocalizedText> arr = new ArrayList<>();
            value.forEach(ls -> arr.add(new LocalizedText(ls.getValue(), ls.getLanguage())));

            retval = arr.toArray(LocalizedText[]::new);
        }
        catch (Exception ex) {
            LOGGER.error("getLocalizedTextFromLangStringSet Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Gets AAS LangString Set from a LocalizedText array.
     *
     * @param value The desired Lang String Set
     * @return The corresponding LocalizedText array
     */
    public static List<LangString> getLangStringSetFromLocalizedText(LocalizedText[] value) {
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        List<LangString> retval = new ArrayList<>();

        try {
            for (LocalizedText lt: value) {
                retval.add(new LangString(lt.getText(), lt.getLocaleId()));
            }
        }
        catch (Exception ex) {
            LOGGER.error("getLangStringSetFromLocalizedText Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Converts the given KeyElements value to the corresponding
     * AASKeyElementsDataType
     *
     * @param keyElement The desired KeyElements value.
     * @return The converted AASKeyElementsDataType.
     */
    public static AASKeyElementsDataType getAasKeyElementsDataType(KeyElements keyElement) {
        AASKeyElementsDataType retval = null;

        try {
            switch (keyElement) {
                case ASSET:
                    retval = AASKeyElementsDataType.Asset;
                    break;

                case ASSET_ADMINISTRATION_SHELL:
                    retval = AASKeyElementsDataType.AssetAdministrationShell;
                    break;

                case CONCEPT_DESCRIPTION:
                    retval = AASKeyElementsDataType.ConceptDescription;
                    break;

                case SUBMODEL:
                    retval = AASKeyElementsDataType.Submodel;
                    break;

                case FRAGMENT_REFERENCE:
                    retval = AASKeyElementsDataType.FragmentReference;
                    break;

                case GLOBAL_REFERENCE:
                    retval = AASKeyElementsDataType.GlobalReference;
                    break;

                case ACCESS_PERMISSION_RULE:
                    retval = AASKeyElementsDataType.AccessPermissionRule;
                    break;

                case ANNOTATED_RELATIONSHIP_ELEMENT:
                    retval = AASKeyElementsDataType.AnnotatedRelationshipElement;
                    break;

                case BASIC_EVENT:
                    LOGGER.warn("getKeyElementsDataTypeFromKeyElements: BASIC_EVENT not available in AASKeyElementsDataType");
                    throw new IllegalArgumentException("BASIC_EVENT not available in AASKeyElementsDataType");

                case BLOB:
                    retval = AASKeyElementsDataType.Blob;
                    break;

                case CAPABILITY:
                    retval = AASKeyElementsDataType.Capability;
                    break;

                case CONCEPT_DICTIONARY:
                    retval = AASKeyElementsDataType.ConceptDictionary;
                    break;

                case DATA_ELEMENT:
                    retval = AASKeyElementsDataType.DataElement;
                    break;

                case ENTITY:
                    retval = AASKeyElementsDataType.Entity;
                    break;

                case EVENT:
                    retval = AASKeyElementsDataType.Event;
                    break;

                case FILE:
                    retval = AASKeyElementsDataType.File;
                    break;

                case MULTI_LANGUAGE_PROPERTY:
                    retval = AASKeyElementsDataType.MultiLanguageProperty;
                    break;

                case OPERATION:
                    retval = AASKeyElementsDataType.Operation;
                    break;

                case PROPERTY:
                    retval = AASKeyElementsDataType.Property;
                    break;

                case RANGE:
                    retval = AASKeyElementsDataType.Range;
                    break;

                case REFERENCE_ELEMENT:
                    retval = AASKeyElementsDataType.ReferenceElement;
                    break;

                case RELATIONSHIP_ELEMENT:
                    retval = AASKeyElementsDataType.RelationshipElement;
                    break;

                case SUBMODEL_ELEMENT:
                    retval = AASKeyElementsDataType.SubmodelElement;
                    break;

                case SUBMODEL_ELEMENT_COLLECTION:
                    retval = AASKeyElementsDataType.SubmodelElementCollection;
                    break;

                case VIEW:
                    retval = AASKeyElementsDataType.View;
                    break;

                default:
                    LOGGER.warn("getKeyElementsDataType: unknown KeyElement: {}", keyElement);
                    break;
            }
        }
        catch (Exception ex) {
            LOGGER.error("getKeyElementsDataType Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Converts the given AASKeyElementsDataType to the corresponding
     * KeyElements
     *
     * @param value The desired AASKeyElementsDataType
     * @return The corresponding KeyElements type
     */
    public static KeyElements getKeyElements(AASKeyElementsDataType value) {
        KeyElements retval = null;

        try {
            switch (value) {
                case Asset:
                    retval = KeyElements.ASSET;
                    break;

                case AssetAdministrationShell:
                    retval = KeyElements.ASSET_ADMINISTRATION_SHELL;
                    break;

                case ConceptDescription:
                    retval = KeyElements.CONCEPT_DESCRIPTION;
                    break;

                case Submodel:
                    retval = KeyElements.SUBMODEL;
                    break;

                case FragmentReference:
                    retval = KeyElements.FRAGMENT_REFERENCE;
                    break;

                case GlobalReference:
                    retval = KeyElements.GLOBAL_REFERENCE;
                    break;

                case AccessPermissionRule:
                    retval = KeyElements.ACCESS_PERMISSION_RULE;
                    break;

                case AnnotatedRelationshipElement:
                    retval = KeyElements.ANNOTATED_RELATIONSHIP_ELEMENT;
                    break;

                case Blob:
                    retval = KeyElements.BLOB;
                    break;

                case Capability:
                    retval = KeyElements.CAPABILITY;
                    break;

                case ConceptDictionary:
                    retval = KeyElements.CONCEPT_DICTIONARY;
                    break;

                case DataElement:
                    retval = KeyElements.DATA_ELEMENT;
                    break;

                case Entity:
                    retval = KeyElements.ENTITY;
                    break;

                case Event:
                    retval = KeyElements.EVENT;
                    break;

                case File:
                    retval = KeyElements.FILE;
                    break;

                case MultiLanguageProperty:
                    retval = KeyElements.MULTI_LANGUAGE_PROPERTY;
                    break;

                case Operation:
                    retval = KeyElements.OPERATION;
                    break;

                case Property:
                    retval = KeyElements.PROPERTY;
                    break;

                case Range:
                    retval = KeyElements.RANGE;
                    break;

                case ReferenceElement:
                    retval = KeyElements.REFERENCE_ELEMENT;
                    break;

                case RelationshipElement:
                    retval = KeyElements.RELATIONSHIP_ELEMENT;
                    break;

                case SubmodelElement:
                    retval = KeyElements.SUBMODEL_ELEMENT;
                    break;

                case SubmodelElementCollection:
                    retval = KeyElements.SUBMODEL_ELEMENT_COLLECTION;
                    break;

                case View:
                    retval = KeyElements.VIEW;
                    break;

                default:
                    LOGGER.warn("getKeyElements: unknown AASKeyElementsDataType: {}", value);
                    break;
            }
        }
        catch (Exception ex) {
            LOGGER.error("getKeyElements Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Gets the corresponding AASKeyTypeDataType from the given KeyType.
     *
     * @param value The desired KeyType
     * @return The corresponding AASKeyTypeDataType
     */
    public static AASKeyTypeDataType getAasKeyType(KeyType value) {
        AASKeyTypeDataType retval;

        switch (value) {
            case CUSTOM:
                retval = AASKeyTypeDataType.Custom;
                break;
            case FRAGMENT_ID:
                retval = AASKeyTypeDataType.FragmentId;
                break;
            case ID_SHORT:
                retval = AASKeyTypeDataType.IdShort;
                break;
            case IRDI:
                retval = AASKeyTypeDataType.IRDI;
                break;
            case IRI:
                retval = AASKeyTypeDataType.IRI;
                break;
            default:
                LOGGER.warn("getAasKeyType: unknown value {}", value);
                throw new IllegalArgumentException("unknown KeyType: " + value);
        }

        return retval;
    }


    /**
     * Gets the corresponding KeyType from the given AASKeyTypeDataType.
     *
     * @param value The desired AASKeyTypeDataType
     * @return The corresponding KeyType
     */
    public static KeyType getKeyType(AASKeyTypeDataType value) {
        KeyType retval;

        switch (value) {
            case Custom:
                retval = KeyType.CUSTOM;
                break;
            case FragmentId:
                retval = KeyType.FRAGMENT_ID;
                break;
            case IdShort:
                retval = KeyType.ID_SHORT;
                break;
            case IRDI:
                retval = KeyType.IRDI;
                break;
            case IRI:
                retval = KeyType.IRI;
                break;
            default:
                LOGGER.warn("getKeyType: unknown value {}", value);
                throw new IllegalArgumentException("unknown AASKeyTypeDataType: " + value);
        }

        return retval;
    }


    /**
     * Creates a reference from the given List of Keys.
     *
     * @param value The desired list of Keys.
     * @return The created reference.
     */
    public static Reference getReferenceFromKeys(AASKeyDataType[] value) {
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        Reference retval = null;

        try {
            List<Key> keys = new ArrayList<>();
            for (AASKeyDataType key: value) {
                keys.add(new DefaultKey.Builder().type(getKeyElements(key.getType())).idType(getKeyType(key.getIdType())).value(key.getValue()).build());
            }
            retval = new DefaultReference.Builder().keys(keys).build();
        }
        catch (Exception ex) {
            LOGGER.error("getReferenceFromKeys Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Sets the desired value in the given SubmodelElement.
     *
     * @param data The desired SubmodelElementData.
     * @param dv The desired Value.
     */
    public static void setSubmodelElementValue(SubmodelElementData data, DataValue dv) {
        setSubmodelElementValue(data.getSubmodelElement(), data.getType(), dv.getValue());
    }


    /**
     * Sets the desired value in the given SubmodelElement.
     *
     * @param submodelElement The desired SubmodelElement.
     * @param type The desired type.
     * @param variant The desired Value.
     */
    public static void setSubmodelElementValue(SubmodelElement submodelElement, SubmodelElementData.Type type, Variant variant) {
        try {
            switch (type) {
                case PROPERTY_VALUE: {
                    Property aasProp = (Property) submodelElement;
                    String newValue = convertVariantValueToString(variant);
                    aasProp.setValue(newValue);
                    break;
                }
                case RANGE_MIN: {
                    Range aasRange = (Range) submodelElement;
                    String newValue = convertVariantValueToString(variant);
                    aasRange.setMin(newValue);
                    break;
                }
                case RANGE_MAX: {
                    Range aasRange = (Range) submodelElement;
                    String newValue = convertVariantValueToString(variant);
                    aasRange.setMax(newValue);
                    break;
                }
                case BLOB_VALUE: {
                    Blob aasBlob = (Blob) submodelElement;
                    ByteString bs = null;
                    if (variant.getValue() != null) {
                        bs = (ByteString) variant.getValue();
                    }
                    aasBlob.setValue(ByteString.asByteArray(bs));
                    break;
                }
                case MULTI_LANGUAGE_VALUE: {
                    MultiLanguageProperty aasMultiProp = (MultiLanguageProperty) submodelElement;
                    if (variant.isArray() && (variant.getValue() instanceof LocalizedText[])) {
                        aasMultiProp.setValues(ValueConverter.getLangStringSetFromLocalizedText((LocalizedText[]) variant.getValue()));
                    }
                    else if (variant.isEmpty()) {
                        aasMultiProp.setValues(new ArrayList<>());
                    }
                    break;
                }
                case REFERENCE_ELEMENT_VALUE: {
                    ReferenceElement aasRefElem = (ReferenceElement) submodelElement;
                    if (variant.isArray() && (variant.getValue() instanceof AASKeyDataType[])) {
                        aasRefElem.setValue(ValueConverter.getReferenceFromKeys((AASKeyDataType[]) variant.getValue()));
                    }
                    else if (variant.isEmpty()) {
                        aasRefElem.setValue(null);
                    }
                    break;
                }
                case RELATIONSHIP_ELEMENT_FIRST: {
                    RelationshipElement aasRelElem = (RelationshipElement) submodelElement;
                    if (variant.isArray() && (variant.getValue() instanceof AASKeyDataType[])) {
                        aasRelElem.setFirst(ValueConverter.getReferenceFromKeys((AASKeyDataType[]) variant.getValue()));
                    }
                    else if (variant.isEmpty()) {
                        aasRelElem.setFirst(null);
                    }
                    break;
                }
                case RELATIONSHIP_ELEMENT_SECOND: {
                    RelationshipElement aasRelElem = (RelationshipElement) submodelElement;
                    if (variant.isArray() && (variant.getValue() instanceof AASKeyDataType[])) {
                        aasRelElem.setSecond(ValueConverter.getReferenceFromKeys((AASKeyDataType[]) variant.getValue()));
                    }
                    else if (variant.isEmpty()) {
                        aasRelElem.setSecond(null);
                    }
                    break;
                }
                case ENTITY_GLOBAL_ASSET_ID: {
                    Entity aasEntity = (Entity) submodelElement;
                    aasEntity.setGlobalAssetId(ValueConverter.getReferenceFromKeys((AASKeyDataType[]) variant.getValue()));
                    break;
                }
                case ENTITY_TYPE: {
                    Entity aasEntity = (Entity) submodelElement;
                    if (variant.isEmpty()) {
                        aasEntity.setEntityType(null);
                    }
                    else {
                        aasEntity.setEntityType(ValueConverter.getEntityType(AASEntityTypeDataType.valueOf((int) variant.getValue())));
                    }
                    break;
                }
                default:
                    LOGGER.warn("setSubmodelElementValue: SubmodelElement {}: unkown type {}", submodelElement.getIdShort(), type);
                    throw new IllegalArgumentException("unkown type " + type);
            }
        }
        catch (Exception ex) {
            LOGGER.error("setSubmodelElementValue Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the input arguments for an operation into the given inputVariables.
     *
     * @param inputVariables The desired inputVariables.
     * @param inputArguments The desired inputArguments
     * @throws StatusException If the operation fails
     */
    public static void setOperationValues(List<OperationVariable> inputVariables, Variant[] inputArguments) throws StatusException {
        if (inputArguments.length < inputVariables.size()) {
            throw new StatusException(StatusCodes.Bad_ArgumentsMissing);
        }
        else if (inputArguments.length > inputVariables.size()) {
            throw new StatusException(StatusCodes.Bad_TooManyArguments);
        }
        else {
            for (int i = 0; i < inputVariables.size(); i++) {
                SubmodelElement smelem = inputVariables.get(i).getValue();
                SubmodelElementData.Type type;
                if (smelem instanceof Property) {
                    type = SubmodelElementData.Type.PROPERTY_VALUE;
                }
                else {
                    throw new StatusException(StatusCodes.Bad_InvalidArgument);
                }

                setSubmodelElementValue(smelem, type, inputArguments[i]);
            }
        }
    }


    /**
     * Sets the output arguments for an operation from the given output
     * variables.
     *
     * @param outputVariables The desired output variables
     * @param outputArguments The desired output arguments
     * @throws StatusException If the operation fails
     * @throws ValueMappingException Error when mapping to ElementValue fails
     */
    public static void setOutputArguments(List<OperationVariable> outputVariables, Variant[] outputArguments) throws StatusException, ValueMappingException {

        if (outputArguments.length != outputVariables.size()) {
            throw new StatusException(StatusCodes.Bad_InvalidArgument);
        }
        else {
            for (int i = 0; i < outputVariables.size(); i++) {
                SubmodelElement smelem = outputVariables.get(i).getValue();
                SubmodelElementData.Type type;
                if (smelem instanceof Property) {
                    type = SubmodelElementData.Type.PROPERTY_VALUE;
                }
                else {
                    throw new StatusException(StatusCodes.Bad_InvalidArgument);
                }

                outputArguments[i] = getSubmodelElementValue(smelem, type);
            }
        }
    }


    /**
     * Gets the corresponding variant value from a given SubmodelElement
     *
     * @param submodelElement The desired SubmodelElement
     * @param type The desired type
     * @return The corresponding value
     * @throws ValueMappingException Error when mapping to ElementValue fails
     */
    @SuppressWarnings("java:S1301")
    public static Variant getSubmodelElementValue(SubmodelElement submodelElement, SubmodelElementData.Type type) throws ValueMappingException {
        Variant retval;

        try {
            switch (type) {
                case PROPERTY_VALUE: {
                    retval = createVariant(ElementValueMapper.<Property, PropertyValue> toValue(submodelElement).getValue().getValue());
                    break;
                }

                default:
                    LOGGER.warn("getSubmodelElementValue: SubmodelElement {}: unkown or invalid type {}", submodelElement.getIdShort(), type);
                    throw new IllegalArgumentException("unkown type " + type);
            }
        }
        catch (Exception ex) {
            LOGGER.error("getSubmodelElementValue Exception", ex);
            throw ex;
        }

        return retval;
    }


    public static DateTime createDateTime(ZonedDateTime value) {
        return new DateTime(GregorianCalendar.from(value));
    }


    public static DateTime createDateTime(LocalDateTime value) {
        return new DateTime(GregorianCalendar.from(value.atZone(ZoneId.of(DateTimeValue.DEFAULT_TIMEZONE))));
    }


    private static String convertVariantValueToString(Variant variant) {
        String retval = "";
        if (variant.getValue() != null) {
            // special treatment for DateTime
            if (variant.getValue() instanceof DateTime) {
                //DateTime.
                retval = ((DateTime) variant.getValue()).getUtcCalendar().toZonedDateTime().toString();
            }
            else {
                retval = variant.getValue().toString();
            }
        }

        return retval;
    }


    private static Variant createVariant(Object value) {
        Variant retval = null;

        if (value == null) {
            retval = Variant.NULL;
        }
        else if (value instanceof ZonedDateTime) {
            // special treatment for DateTime
            retval = new Variant(createDateTime((ZonedDateTime) value));
        }
        else if (value instanceof LocalDateTime) {
            // special treatment for DateTime
            retval = new Variant(createDateTime((LocalDateTime) value));
        }
        else {
            retval = new Variant(value);
        }

        return retval;
    }
}
