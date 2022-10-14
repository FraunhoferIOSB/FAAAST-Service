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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
 */
public class ValueConverter {

    private static final String UNKNOWN_KEY_TYPE = "unknown KeyType: ";
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueConverter.class);
    private static final ArrayList<DatatypeMapper> typeList;
    private static final Map<ModelingKind, AASModelingKindDataType> MODELING_KIND_MAP;
    private static final Map<IdentifierType, AASIdentifierTypeDataType> IDENTIFIER_TYPE_MAP;
    private static final Map<AssetKind, AASAssetKindDataType> ASSET_KIND_MAP;
    private static final ArrayList<TypeMapper<EntityType, AASEntityTypeDataType>> ENTITY_TYPE_LIST;
    private static final ArrayList<TypeMapper<KeyElements, AASKeyElementsDataType>> KEY_ELEMENTS_LIST;
    private static final ArrayList<TypeMapper<KeyType, AASKeyTypeDataType>> KEY_TYPE_LIST;

    private static class DatatypeMapper {
        private final String typeString;
        private final NodeId typeNode;
        private final AASValueTypeDataType valueType;
        private final Datatype datatype;

        public DatatypeMapper(String typeString, NodeId typeNode, AASValueTypeDataType valueType, Datatype datatype) {
            this.typeString = typeString;
            this.typeNode = typeNode;
            this.valueType = valueType;
            this.datatype = datatype;
        }
    }

    private static class TypeMapper<aasType, opcuaType> {
        private final aasType aasObject;
        private final opcuaType opcuaObject;

        public TypeMapper(aasType aasObject, opcuaType opcuaObject) {
            this.aasObject = aasObject;
            this.opcuaObject = opcuaObject;
        }
    }

    static {
        typeList = new ArrayList<>();
        typeList.add(new DatatypeMapper("bytestring", Identifiers.ByteString, AASValueTypeDataType.ByteString, null));
        typeList.add(new DatatypeMapper("boolean", Identifiers.Boolean, AASValueTypeDataType.Boolean, Datatype.BOOLEAN));
        typeList.add(new DatatypeMapper("datetime", Identifiers.DateTime, AASValueTypeDataType.DateTime, Datatype.DATE_TIME));
        typeList.add(new DatatypeMapper("decimal", Identifiers.Decimal, AASValueTypeDataType.Int64, Datatype.DECIMAL));
        typeList.add(new DatatypeMapper("integer", Identifiers.Integer, AASValueTypeDataType.Int64, Datatype.INTEGER));
        typeList.add(new DatatypeMapper("int", Identifiers.Int32, AASValueTypeDataType.Int32, Datatype.INT));
        typeList.add(new DatatypeMapper("unsignedint", Identifiers.UInt32, AASValueTypeDataType.UInt32, null));
        typeList.add(new DatatypeMapper("long", Identifiers.Int64, AASValueTypeDataType.Int64, Datatype.LONG));
        typeList.add(new DatatypeMapper("unsignedlong", Identifiers.UInt64, AASValueTypeDataType.UInt64, null));
        typeList.add(new DatatypeMapper("short", Identifiers.Int16, AASValueTypeDataType.Int16, Datatype.SHORT));
        typeList.add(new DatatypeMapper("unsignedshort", Identifiers.UInt16, AASValueTypeDataType.UInt16, null));
        typeList.add(new DatatypeMapper("byte", Identifiers.SByte, AASValueTypeDataType.SByte, Datatype.BYTE));
        typeList.add(new DatatypeMapper("unsignedbyte", Identifiers.Byte, AASValueTypeDataType.Byte, null));
        typeList.add(new DatatypeMapper("double", Identifiers.Double, AASValueTypeDataType.Double, Datatype.DOUBLE));
        typeList.add(new DatatypeMapper("float", Identifiers.Float, AASValueTypeDataType.Float, Datatype.FLOAT));
        typeList.add(new DatatypeMapper("langstring", Identifiers.LocalizedText, AASValueTypeDataType.LocalizedText, null));
        typeList.add(new DatatypeMapper("string", Identifiers.String, AASValueTypeDataType.String, Datatype.STRING));
        typeList.add(new DatatypeMapper("time", Identifiers.UtcTime, AASValueTypeDataType.UtcTime, null));

        MODELING_KIND_MAP = new HashMap<>();
        MODELING_KIND_MAP.put(ModelingKind.INSTANCE, AASModelingKindDataType.Instance);
        MODELING_KIND_MAP.put(ModelingKind.TEMPLATE, AASModelingKindDataType.Template);

        IDENTIFIER_TYPE_MAP = new HashMap<>();
        IDENTIFIER_TYPE_MAP.put(IdentifierType.IRDI, AASIdentifierTypeDataType.IRDI);
        IDENTIFIER_TYPE_MAP.put(IdentifierType.IRI, AASIdentifierTypeDataType.IRI);
        IDENTIFIER_TYPE_MAP.put(IdentifierType.CUSTOM, AASIdentifierTypeDataType.Custom);

        ASSET_KIND_MAP = new HashMap<>();
        ASSET_KIND_MAP.put(AssetKind.TYPE, AASAssetKindDataType.Type);
        ASSET_KIND_MAP.put(AssetKind.INSTANCE, AASAssetKindDataType.Instance);

        ENTITY_TYPE_LIST = new ArrayList<>();
        ENTITY_TYPE_LIST.add(new TypeMapper<>(EntityType.CO_MANAGED_ENTITY, AASEntityTypeDataType.CoManagedEntity));
        ENTITY_TYPE_LIST.add(new TypeMapper<>(EntityType.SELF_MANAGED_ENTITY, AASEntityTypeDataType.SelfManagedEntity));

        // BASIC_EVENT not available in AASKeyElementsDataType
        KEY_ELEMENTS_LIST = new ArrayList<>();
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.ACCESS_PERMISSION_RULE, AASKeyElementsDataType.AccessPermissionRule));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.ANNOTATED_RELATIONSHIP_ELEMENT, AASKeyElementsDataType.AnnotatedRelationshipElement));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.ASSET, AASKeyElementsDataType.Asset));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.ASSET_ADMINISTRATION_SHELL, AASKeyElementsDataType.AssetAdministrationShell));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.BLOB, AASKeyElementsDataType.Blob));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.CAPABILITY, AASKeyElementsDataType.Capability));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.CONCEPT_DESCRIPTION, AASKeyElementsDataType.ConceptDescription));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.CONCEPT_DICTIONARY, AASKeyElementsDataType.ConceptDictionary));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.DATA_ELEMENT, AASKeyElementsDataType.DataElement));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.ENTITY, AASKeyElementsDataType.Entity));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.EVENT, AASKeyElementsDataType.Event));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.FILE, AASKeyElementsDataType.File));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.FRAGMENT_REFERENCE, AASKeyElementsDataType.FragmentReference));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.GLOBAL_REFERENCE, AASKeyElementsDataType.GlobalReference));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.MULTI_LANGUAGE_PROPERTY, AASKeyElementsDataType.MultiLanguageProperty));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.OPERATION, AASKeyElementsDataType.Operation));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.PROPERTY, AASKeyElementsDataType.Property));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.RANGE, AASKeyElementsDataType.Range));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.REFERENCE_ELEMENT, AASKeyElementsDataType.ReferenceElement));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.RELATIONSHIP_ELEMENT, AASKeyElementsDataType.RelationshipElement));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.SUBMODEL, AASKeyElementsDataType.Submodel));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.SUBMODEL_ELEMENT, AASKeyElementsDataType.SubmodelElement));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.SUBMODEL_ELEMENT_COLLECTION, AASKeyElementsDataType.SubmodelElementCollection));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyElements.VIEW, AASKeyElementsDataType.View));

        KEY_TYPE_LIST = new ArrayList<>();
        KEY_TYPE_LIST.add(new TypeMapper<>(KeyType.CUSTOM, AASKeyTypeDataType.Custom));
        KEY_TYPE_LIST.add(new TypeMapper<>(KeyType.FRAGMENT_ID, AASKeyTypeDataType.FragmentId));
        KEY_TYPE_LIST.add(new TypeMapper<>(KeyType.ID_SHORT, AASKeyTypeDataType.IdShort));
        KEY_TYPE_LIST.add(new TypeMapper<>(KeyType.IRDI, AASKeyTypeDataType.IRDI));
        KEY_TYPE_LIST.add(new TypeMapper<>(KeyType.IRI, AASKeyTypeDataType.IRI));
    }

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
            Optional<DatatypeMapper> rv = typeList.stream().filter(t -> t.typeString.equalsIgnoreCase(valueType)).findAny();
            if (rv.isEmpty()) {
                LOGGER.warn("convertValueTypeStringToNodeId: Unknown type: {}", valueType);
                retval = NodeId.NULL;
            }
            else {
                retval = rv.get().typeNode;
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
            Optional<DatatypeMapper> rv = typeList.stream().filter(t -> t.typeString.equalsIgnoreCase(value)).findAny();
            if (rv.isEmpty()) {
                LOGGER.warn("stringToValueType: unknown value: {}", value);
                throw new IllegalArgumentException("unknown value: " + value);
            }
            else {
                retval = rv.get().valueType;
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

        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        Optional<DatatypeMapper> rv = typeList.stream().filter(t -> t.datatype == type).findAny();
        if (rv.isEmpty()) {
            LOGGER.warn("datatypeToValueType: unknown type: {}", type);
            throw new IllegalArgumentException("unknown type: " + type);
        }
        else {
            retval = rv.get().valueType;
        }

        return retval;
    }


    /**
     * Converts the given ModelingKind to the corresponding AASModelingKindDataType.
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
        else if (MODELING_KIND_MAP.containsKey(value)) {
            retval = MODELING_KIND_MAP.get(value);
        }
        else {
            LOGGER.warn("convertModelingKind: unknown value {}", value);
            throw new IllegalArgumentException("unknown ModelingKind: " + value);
        }

        return retval;
    }


    /**
     * Converts the given IdentifierType to the corresponding AASIdentifierTypeDataType.
     *
     * @param value The desired IdentifierType
     * @return The corresponding AASIdentifierTypeDataType.
     */
    public static AASIdentifierTypeDataType convertIdentifierType(IdentifierType value) {
        AASIdentifierTypeDataType retval;
        if (IDENTIFIER_TYPE_MAP.containsKey(value)) {
            retval = IDENTIFIER_TYPE_MAP.get(value);
        }
        else {
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
        if (ASSET_KIND_MAP.containsKey(value)) {
            retval = ASSET_KIND_MAP.get(value);
        }
        else {
            LOGGER.warn("convertAssetKind: unknown value {}", value);
            throw new IllegalArgumentException(UNKNOWN_KEY_TYPE + value);
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
        AASEntityTypeDataType retval;
        var rv = ENTITY_TYPE_LIST.stream().filter(m -> m.aasObject == value).findAny();
        if (rv.isEmpty()) {
            LOGGER.warn("getAasEntityType: unknown value {}", value);
            throw new IllegalArgumentException("unknown EntityType: " + value);
        }
        else {
            retval = rv.get().opcuaObject;
        }

        return retval;
    }


    /**
     * Converts the given AASEntityTypeDataType to the corresponding EntityType.
     *
     * @param value The desired AASEntityTypeDataType
     * @return The corresponding EntityType
     */
    public static EntityType getEntityType(AASEntityTypeDataType value) {
        EntityType retval;
        var rv = ENTITY_TYPE_LIST.stream().filter(m -> m.opcuaObject == value).findAny();
        if (rv.isEmpty()) {
            LOGGER.warn("getEntityType: unknown value {}", value);
            throw new IllegalArgumentException("unknown EntityType: " + value);
        }
        else {
            retval = rv.get().aasObject;
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
     * Converts the given KeyElements value to the corresponding AASKeyElementsDataType
     *
     * @param value The desired KeyElements value.
     * @return The converted AASKeyElementsDataType.
     */
    public static AASKeyElementsDataType getAasKeyElementsDataType(KeyElements value) {
        AASKeyElementsDataType retval = null;
        var rv = KEY_ELEMENTS_LIST.stream().filter(m -> m.aasObject == value).findAny();
        if (rv.isEmpty()) {
            LOGGER.warn("getAasKeyElementsDataType: unknown value {}", value);
            throw new IllegalArgumentException("unknown KeyElementsDataType: " + value);
        }
        else {
            retval = rv.get().opcuaObject;
        }

        return retval;
    }


    /**
     * Converts the given AASKeyElementsDataType to the corresponding KeyElements
     *
     * @param value The desired AASKeyElementsDataType
     * @return The corresponding KeyElements type
     */
    public static KeyElements getKeyElements(AASKeyElementsDataType value) {
        KeyElements retval = null;
        var rv = KEY_ELEMENTS_LIST.stream().filter(m -> m.opcuaObject == value).findAny();
        if (rv.isEmpty()) {
            LOGGER.warn("getAasKeyElementsDataType: unknown value {}", value);
            throw new IllegalArgumentException("unknown KeyElementsDataType: " + value);
        }
        else {
            retval = rv.get().aasObject;
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
        var rv = KEY_TYPE_LIST.stream().filter(m -> m.aasObject == value).findAny();
        if (rv.isEmpty()) {
            LOGGER.warn("getAasKeyType: unknown value {}", value);
            throw new IllegalArgumentException(UNKNOWN_KEY_TYPE + value);
        }
        else {
            retval = rv.get().opcuaObject;
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
        var rv = KEY_TYPE_LIST.stream().filter(m -> m.opcuaObject == value).findAny();
        if (rv.isEmpty()) {
            LOGGER.warn("getKeyType: unknown value {}", value);
            throw new IllegalArgumentException(UNKNOWN_KEY_TYPE + value);
        }
        else {
            retval = rv.get().aasObject;
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
     * Sets the output arguments for an operation from the given output variables.
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


    /**
     * Creates an OPC UA date time from Java date time
     *
     * @param value The input Java date time
     * @return The OPC UA date time
     */
    public static DateTime createDateTime(ZonedDateTime value) {
        return new DateTime(GregorianCalendar.from(value));
    }


    /**
     * Creates an OPC UA date time from Java date time
     *
     * @param value The input Java date time
     * @return The OPC UA date time
     */
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
