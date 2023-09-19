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

import com.google.common.base.Objects;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DecimalValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.IntegerValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import opc.i4aas.AASAssetKindDataType;
import opc.i4aas.AASDataTypeDefXsd;
import opc.i4aas.AASDirectionDataType;
import opc.i4aas.AASEntityTypeDataType;
import opc.i4aas.AASKeyDataType;
import opc.i4aas.AASKeyTypesDataType;
import opc.i4aas.AASModellingKindDataType;
import opc.i4aas.AASQualifierKindDataType;
import opc.i4aas.AASStateOfEventDataType;
import opc.i4aas.AASSubmodelElementsDataType;
import org.eclipse.digitaltwin.aas4j.v3.model.AASSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXSD;
import org.eclipse.digitaltwin.aas4j.v3.model.Direction;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.EntityType;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.ModellingKind;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.QualifierKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.RelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.StateOfEvent;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to convert values between the AASService types and the OPC UA Types
 */
public class ValueConverter {

    private static final String UNKNOWN_KEY_TYPE = "unknown KeyType: ";
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueConverter.class);
    private static final List<DatatypeMapper> typeList;
    private static final Map<ModellingKind, AASModellingKindDataType> MODELING_KIND_MAP;
    private static final Map<QualifierKind, AASQualifierKindDataType> QUALIFIER_KIND_MAP;
    private static final Map<AssetKind, AASAssetKindDataType> ASSET_KIND_MAP;
    private static final List<TypeMapper<EntityType, AASEntityTypeDataType>> ENTITY_TYPE_LIST;
    private static final List<TypeMapper<KeyTypes, AASKeyTypesDataType>> KEY_ELEMENTS_LIST;
    private static final List<TypeMapper<Direction, AASDirectionDataType>> DIRECTION_LIST;
    private static final List<TypeMapper<StateOfEvent, AASStateOfEventDataType>> STATE_OF_EVENT_LIST;
    private static final List<TypeMapper<AASSubmodelElements, AASSubmodelElementsDataType>> SUBMODEL_ELEMENTS_DATATYPE;

    private static class DatatypeMapper {
        private final NodeId typeNode;
        private final Datatype datatype;
        private final AASDataTypeDefXsd dataTypeDefXsd;

        public DatatypeMapper(NodeId typeNode, Datatype datatype, AASDataTypeDefXsd dataTypeDefXsd) {
            this.typeNode = typeNode;
            this.datatype = datatype;
            this.dataTypeDefXsd = dataTypeDefXsd;
        }
    }

    private static class TypeMapper<A, O> {
        private final A aasObject;
        private final O opcuaObject;

        public TypeMapper(A aasObject, O opcuaObject) {
            this.aasObject = aasObject;
            this.opcuaObject = opcuaObject;
        }
    }

    static {
        typeList = new ArrayList<>();
        typeList.add(new DatatypeMapper(Identifiers.ByteString, null, AASDataTypeDefXsd.Base64Binary));
        typeList.add(new DatatypeMapper(Identifiers.Boolean, Datatype.BOOLEAN, AASDataTypeDefXsd.Boolean));
        typeList.add(new DatatypeMapper(Identifiers.DateTime, Datatype.DATE_TIME, AASDataTypeDefXsd.DateTime));
        typeList.add(new DatatypeMapper(Identifiers.Decimal, Datatype.DECIMAL, AASDataTypeDefXsd.Decimal));
        typeList.add(new DatatypeMapper(Identifiers.Integer, Datatype.INTEGER, AASDataTypeDefXsd.Integer));
        typeList.add(new DatatypeMapper(Identifiers.Int32, Datatype.INT, AASDataTypeDefXsd.Int));
        typeList.add(new DatatypeMapper(Identifiers.UInt32, null, AASDataTypeDefXsd.UnsignedInt));
        typeList.add(new DatatypeMapper(Identifiers.Int64, Datatype.LONG, AASDataTypeDefXsd.Long));
        typeList.add(new DatatypeMapper(Identifiers.UInt64, null, AASDataTypeDefXsd.UnsignedLong));
        typeList.add(new DatatypeMapper(Identifiers.Int16, Datatype.SHORT, AASDataTypeDefXsd.Short));
        typeList.add(new DatatypeMapper(Identifiers.UInt16, null, AASDataTypeDefXsd.UnsignedShort));
        typeList.add(new DatatypeMapper(Identifiers.SByte, Datatype.BYTE, AASDataTypeDefXsd.Byte));
        typeList.add(new DatatypeMapper(Identifiers.Byte, null, AASDataTypeDefXsd.UnsignedByte));
        typeList.add(new DatatypeMapper(Identifiers.Double, Datatype.DOUBLE, AASDataTypeDefXsd.Double));
        typeList.add(new DatatypeMapper(Identifiers.Float, Datatype.FLOAT, AASDataTypeDefXsd.Float));
        //typeList.add(new DatatypeMapper(Identifiers.LocalizedText, AASValueTypeDataType.LocalizedText, null));
        typeList.add(new DatatypeMapper(Identifiers.String, Datatype.STRING, AASDataTypeDefXsd.String));
        //typeList.add(new DatatypeMapper(Identifiers.UtcTime, AASValueTypeDataType.UtcTime, null));

        MODELING_KIND_MAP = new EnumMap<>(ModellingKind.class);
        MODELING_KIND_MAP.put(ModellingKind.INSTANCE, AASModellingKindDataType.Instance);
        MODELING_KIND_MAP.put(ModellingKind.TEMPLATE, AASModellingKindDataType.Template);

        QUALIFIER_KIND_MAP = new EnumMap<>(QualifierKind.class);
        QUALIFIER_KIND_MAP.put(QualifierKind.CONCEPT_QUALIFIER, AASQualifierKindDataType.ConceptQualifier);
        QUALIFIER_KIND_MAP.put(QualifierKind.TEMPLATE_QUALIFIER, AASQualifierKindDataType.TemplateQualifier);
        QUALIFIER_KIND_MAP.put(QualifierKind.VALUE_QUALIFIER, AASQualifierKindDataType.ValueQualifier);

        ASSET_KIND_MAP = new EnumMap<>(AssetKind.class);
        ASSET_KIND_MAP.put(AssetKind.TYPE, AASAssetKindDataType.Type);
        ASSET_KIND_MAP.put(AssetKind.INSTANCE, AASAssetKindDataType.Instance);

        ENTITY_TYPE_LIST = new ArrayList<>();
        ENTITY_TYPE_LIST.add(new TypeMapper<>(EntityType.CO_MANAGED_ENTITY, AASEntityTypeDataType.CoManagedEntity));
        ENTITY_TYPE_LIST.add(new TypeMapper<>(EntityType.SELF_MANAGED_ENTITY, AASEntityTypeDataType.SelfManagedEntity));
        // BASIC_EVENT not available in AASKeyTypesDataType
        KEY_ELEMENTS_LIST = new ArrayList<>();
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.ANNOTATED_RELATIONSHIP_ELEMENT, AASKeyTypesDataType.AnnotatedRelationshipElement));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.ASSET_ADMINISTRATION_SHELL, AASKeyTypesDataType.AssetAdministrationShell));
        // BASIC_EVENT_ELEMENT
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.BLOB, AASKeyTypesDataType.Blob));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.CAPABILITY, AASKeyTypesDataType.Capability));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.CONCEPT_DESCRIPTION, AASKeyTypesDataType.ConceptDescription));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.DATA_ELEMENT, AASKeyTypesDataType.DataElement));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.ENTITY, AASKeyTypesDataType.Entity));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.EVENT_ELEMENT, AASKeyTypesDataType.EventElement));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.FILE, AASKeyTypesDataType.File));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.FRAGMENT_REFERENCE, AASKeyTypesDataType.FragmentReference));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.GLOBAL_REFERENCE, AASKeyTypesDataType.GlobalReference));
        // IDENTIFIABLE
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.MULTI_LANGUAGE_PROPERTY, AASKeyTypesDataType.MultiLanguageProperty));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.OPERATION, AASKeyTypesDataType.Operation));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.PROPERTY, AASKeyTypesDataType.Property));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.RANGE, AASKeyTypesDataType.Range));
        // REFERABLE
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.REFERENCE_ELEMENT, AASKeyTypesDataType.ReferenceElement));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.RELATIONSHIP_ELEMENT, AASKeyTypesDataType.RelationshipElement));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.SUBMODEL, AASKeyTypesDataType.Submodel));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.SUBMODEL_ELEMENT, AASKeyTypesDataType.SubmodelElement));
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.SUBMODEL_ELEMENT_COLLECTION, AASKeyTypesDataType.SubmodelElementCollection));
        // TODO
        KEY_ELEMENTS_LIST.add(new TypeMapper<>(KeyTypes.SUBMODEL_ELEMENT_LIST, AASKeyTypesDataType.SubmodelElementList));

        DIRECTION_LIST = new ArrayList<>();
        DIRECTION_LIST.add(new TypeMapper<>(Direction.INPUT, AASDirectionDataType.Input));
        DIRECTION_LIST.add(new TypeMapper<>(Direction.OUTPUT, AASDirectionDataType.Output));

        STATE_OF_EVENT_LIST = new ArrayList<>();
        STATE_OF_EVENT_LIST.add(new TypeMapper<>(StateOfEvent.ON, AASStateOfEventDataType.On));
        STATE_OF_EVENT_LIST.add(new TypeMapper<>(StateOfEvent.OFF, AASStateOfEventDataType.Off));

        SUBMODEL_ELEMENTS_DATATYPE = new ArrayList<>();
        SUBMODEL_ELEMENTS_DATATYPE.add(new TypeMapper<>(AASSubmodelElements.ANNOTATED_RELATIONSHIP_ELEMENT, AASSubmodelElementsDataType.AnnotatedRelationShipElement));
        SUBMODEL_ELEMENTS_DATATYPE.add(new TypeMapper<>(AASSubmodelElements.BASIC_EVENT_ELEMENT, AASSubmodelElementsDataType.BasicEventElement));
        SUBMODEL_ELEMENTS_DATATYPE.add(new TypeMapper<>(AASSubmodelElements.BLOB, AASSubmodelElementsDataType.Blob));
        SUBMODEL_ELEMENTS_DATATYPE.add(new TypeMapper<>(AASSubmodelElements.CAPABILITY, AASSubmodelElementsDataType.Capability));
        SUBMODEL_ELEMENTS_DATATYPE.add(new TypeMapper<>(AASSubmodelElements.DATA_ELEMENT, AASSubmodelElementsDataType.DataElement));
        SUBMODEL_ELEMENTS_DATATYPE.add(new TypeMapper<>(AASSubmodelElements.ENTITY, AASSubmodelElementsDataType.Entity));
        SUBMODEL_ELEMENTS_DATATYPE.add(new TypeMapper<>(AASSubmodelElements.EVENT_ELEMENT, AASSubmodelElementsDataType.EventElement));
        SUBMODEL_ELEMENTS_DATATYPE.add(new TypeMapper<>(AASSubmodelElements.FILE, AASSubmodelElementsDataType.File));
        SUBMODEL_ELEMENTS_DATATYPE.add(new TypeMapper<>(AASSubmodelElements.MULTI_LANGUAGE_PROPERTY, AASSubmodelElementsDataType.MultiLanguageProperty));
        SUBMODEL_ELEMENTS_DATATYPE.add(new TypeMapper<>(AASSubmodelElements.OPERATION, AASSubmodelElementsDataType.Operation));
        SUBMODEL_ELEMENTS_DATATYPE.add(new TypeMapper<>(AASSubmodelElements.PROPERTY, AASSubmodelElementsDataType.Property));
        SUBMODEL_ELEMENTS_DATATYPE.add(new TypeMapper<>(AASSubmodelElements.RANGE, AASSubmodelElementsDataType.Range));
        SUBMODEL_ELEMENTS_DATATYPE.add(new TypeMapper<>(AASSubmodelElements.REFERENCE_ELEMENT, AASSubmodelElementsDataType.ReferenceElement));
        SUBMODEL_ELEMENTS_DATATYPE.add(new TypeMapper<>(AASSubmodelElements.RELATIONSHIP_ELEMENT, AASSubmodelElementsDataType.RelationshipElement));
        SUBMODEL_ELEMENTS_DATATYPE.add(new TypeMapper<>(AASSubmodelElements.SUBMODEL_ELEMENT, AASSubmodelElementsDataType.SubmodelElement));
        SUBMODEL_ELEMENTS_DATATYPE.add(new TypeMapper<>(AASSubmodelElements.SUBMODEL_ELEMENT_COLLECTION, AASSubmodelElementsDataType.SubmodelElementCollection));
        SUBMODEL_ELEMENTS_DATATYPE.add(new TypeMapper<>(AASSubmodelElements.SUBMODEL_ELEMENT_LIST, AASSubmodelElementsDataType.SubmodelElementList));
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
    public static NodeId convertValueTypeStringToNodeId(DataTypeDefXSD valueType) {
        NodeId retval;

        //LOGGER.info("convertValueTypeStringToNodeId: {}", valueType);
        Optional<DatatypeMapper> rv = typeList.stream()
                .filter(t -> (t.datatype != null) && Objects.equal(t.datatype.getAas4jDatatype(), valueType))
                .findAny();
        if (rv.isEmpty()) {
            LOGGER.warn("convertValueTypeStringToNodeId: Unknown type: {}", valueType);
            retval = NodeId.NULL;
        }
        else {
            retval = rv.get().typeNode;
        }

        return retval;
    }

    //    /**
    //     * Converts the given DataTypeDefXSD to the corresponding AASValueTypeDataType
    //     *
    //     * @param value The desired value.
    //     * @return The corresponding AASValueTypeDataType
    //     */
    //    public static AASValueTypeDataType dataTypeXsdToValueType(DataTypeDefXSD value) {
    //        AASValueTypeDataType retval = null;
    //
    //        Optional<DatatypeMapper> rv = typeList.stream()
    //                .filter(t -> (t.datatype != null) && Objects.equal(t.datatype.getAas4jDatatype(), value))
    //                .findAny();
    //        if (rv.isEmpty()) {
    //            LOGGER.warn("dataTypeXsdToValueType: unknown value: {}", value);
    //            throw new IllegalArgumentException("unknown value: " + value);
    //        }
    //        else {
    //            retval = rv.get().valueType;
    //        }
    //
    //        return retval;
    //    }

    //    /**
    //     * Converts the given datatype to the corresponding AASValueTypeDataType.
    //     *
    //     * @param type The desired datatype
    //     * @return The corresponding AASValueTypeDataType
    //     */
    //    public static AASValueTypeDataType datatypeToValueType(Datatype type) {
    //        AASValueTypeDataType retval;
    //
    //        Ensure.requireNonNull(type, "type must not be null");
    //        Optional<DatatypeMapper> rv = typeList.stream().filter(t -> t.datatype == type).findAny();
    //        if (rv.isEmpty()) {
    //            LOGGER.warn("datatypeToValueType: unknown type: {}", type);
    //            throw new IllegalArgumentException("unknown type: " + type);
    //        }
    //        else {
    //            retval = rv.get().valueType;
    //        }
    //
    //        return retval;
    //    }


    /**
     * Converts the given datatype to the corresponding AASDataTypeDefXsd.
     *
     * @param type The desired datatype
     * @return The corresponding AASValueTypeDataType
     */
    public static AASDataTypeDefXsd datatypeToOpcDataType(Datatype type) {
        AASDataTypeDefXsd retval;

        Ensure.requireNonNull(type, "type must not be null");
        Optional<DatatypeMapper> rv = typeList.stream().filter(t -> t.datatype == type).findAny();
        if (rv.isEmpty()) {
            LOGGER.warn("datatypeToOpcDataType: unknown type: {}", type);
            throw new IllegalArgumentException("unknown type: " + type);
        }
        else {
            retval = rv.get().dataTypeDefXsd;
        }

        return retval;
    }


    /**
     * Converts the given DataTypeDefXSD to the corresponding AASValueTypeDataType
     *
     * @param value The desired value.
     * @return The corresponding AASValueTypeDataType
     */
    public static AASDataTypeDefXsd convertDataTypeDefXsd(DataTypeDefXSD value) {
        AASDataTypeDefXsd retval = null;

        Optional<DatatypeMapper> rv = typeList.stream()
                .filter(t -> (t.datatype != null) && Objects.equal(t.datatype.getAas4jDatatype(), value))
                .findAny();
        if (rv.isEmpty()) {
            LOGGER.warn("convertDataTypeDefXsd: unknown value: {}", value);
            throw new IllegalArgumentException("unknown value: " + value);
        }
        else {
            retval = rv.get().dataTypeDefXsd;
        }

        return retval;
    }


    /**
     * Converts the given ModellingKind to the corresponding AASModellingKindDataType.
     *
     * @param value the desired ModellingKind
     * @return The corresponding AASModellingKindDataType
     */
    public static AASModellingKindDataType convertModellingKind(ModellingKind value) {
        AASModellingKindDataType retval;

        if (value == null) {
            LOGGER.warn("convertModellingKind: value == null");
            retval = AASModellingKindDataType.Instance;
        }
        else if (MODELING_KIND_MAP.containsKey(value)) {
            retval = MODELING_KIND_MAP.get(value);
        }
        else {
            LOGGER.warn("convertModellingKind: unknown value {}", value);
            throw new IllegalArgumentException("unknown ModellingKind: " + value);
        }

        return retval;
    }


    /**
     * Converts the given QualifierKind to the corresponding AASQualifierKindDataType.
     *
     * @param value the desired QualifierKind
     * @return The corresponding AASQualifierKindDataType
     */
    public static AASQualifierKindDataType convertQualifierKind(QualifierKind value) {
        AASQualifierKindDataType retval;

        if (value == null) {
            LOGGER.warn("convertQualifierKind: value == null");
            retval = null;
        }
        else if (QUALIFIER_KIND_MAP.containsKey(value)) {
            retval = QUALIFIER_KIND_MAP.get(value);
        }
        else {
            LOGGER.warn("convertQualifierKind: unknown value {}", value);
            throw new IllegalArgumentException("unknown QualifierKind: " + value);
        }

        return retval;
    }

    //    /**
    //     * Converts the given IdentifierType to the corresponding AASIdentifierTypeDataType.
    //     *
    //     * @param value The desired IdentifierType
    //     * @return The corresponding AASIdentifierTypeDataType.
    //     */
    //    public static AASIdentifierTypeDataType convertIdentifierType(IdentifierType value) {
    //        AASIdentifierTypeDataType retval;
    //        if (IDENTIFIER_TYPE_MAP.containsKey(value)) {
    //            retval = IDENTIFIER_TYPE_MAP.get(value);
    //        }
    //        else {
    //            LOGGER.warn("convertIdentifierType: unknown value {}", value);
    //            throw new IllegalArgumentException("unknown IdentifierType: " + value);
    //        }
    //        return retval;
    //    }


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
    public static LocalizedText[] getLocalizedTextFromLangStringSet(List<LangStringTextType> value) {
        LocalizedText[] retval;

        ArrayList<LocalizedText> arr = new ArrayList<>();
        value.forEach(ls -> arr.add(new LocalizedText(ls.getText(), ls.getLanguage())));

        retval = arr.toArray(LocalizedText[]::new);

        return retval;
    }


    /**
     * Gets AAS LangString Set from a LocalizedText array.
     *
     * @param value The desired Lang String Set
     * @return The corresponding LocalizedText array
     */
    public static List<LangStringTextType> getLangStringSetFromLocalizedText(LocalizedText[] value) {
        Ensure.requireNonNull(value, "value must not be null");

        List<LangStringTextType> retval = new ArrayList<>();

        for (LocalizedText lt: value) {
            retval.add(new DefaultLangStringTextType.Builder().text(lt.getText()).language(lt.getLocaleId()).build());
        }

        return retval;
    }


    /**
     * Converts the given KeyTypes value to the corresponding AASKeyTypesDataType
     *
     * @param value The desired KeyTypes value.
     * @return The converted AASKeyTypesDataType.
     */
    public static AASKeyTypesDataType getAasKeyTypesDataType(KeyTypes value) {
        AASKeyTypesDataType retval = null;
        var rv = KEY_ELEMENTS_LIST.stream().filter(m -> m.aasObject == value).findAny();
        if (rv.isEmpty()) {
            LOGGER.warn("getAasKeyTypesDataType: unknown value {}", value);
            throw new IllegalArgumentException("unknown KeyTypesDataType: " + value);
        }
        else {
            retval = rv.get().opcuaObject;
        }

        return retval;
    }


    /**
     * Converts the given AASKeyTypesDataType to the corresponding KeyTypes
     *
     * @param value The desired AASKeyTypesDataType
     * @return The corresponding KeyTypes type
     */
    public static KeyTypes getKeyTypes(AASKeyTypesDataType value) {
        KeyTypes retval = null;
        var rv = KEY_ELEMENTS_LIST.stream().filter(m -> m.opcuaObject == value).findAny();
        if (rv.isEmpty()) {
            LOGGER.warn("getAasKeyTypesDataType: unknown value {}", value);
            throw new IllegalArgumentException("unknown KeyTypesDataType: " + value);
        }
        else {
            retval = rv.get().aasObject;
        }

        return retval;
    }

    //    /**
    //     * Gets the corresponding AASKeyTypesDataType from the given KeyType.
    //     *
    //     * @param value The desired KeyType
    //     * @return The corresponding AASKeyTypesDataType
    //     */
    //    public static AASKeyTypesDataType getAasKeyType(KeyType value) {
    //        AASKeyTypesDataType retval;
    //        var rv = KEY_TYPE_LIST.stream().filter(m -> m.aasObject == value).findAny();
    //        if (rv.isEmpty()) {
    //            LOGGER.warn("getAasKeyType: unknown value {}", value);
    //            throw new IllegalArgumentException(UNKNOWN_KEY_TYPE + value);
    //        }
    //        else {
    //            retval = rv.get().opcuaObject;
    //        }
    //        return retval;
    //    }

    //    /**
    //     * Gets the corresponding KeyType from the given AASKeyTypesDataType.
    //     *
    //     * @param value The desired AASKeyTypesDataType
    //     * @return The corresponding KeyType
    //     */
    //    public static KeyType getKeyType(AASKeyTypesDataType value) {
    //        KeyType retval;
    //        var rv = KEY_TYPE_LIST.stream().filter(m -> m.opcuaObject == value).findAny();
    //        if (rv.isEmpty()) {
    //            LOGGER.warn("getKeyType: unknown value {}", value);
    //            throw new IllegalArgumentException(UNKNOWN_KEY_TYPE + value);
    //        }
    //        else {
    //            retval = rv.get().aasObject;
    //        }
    //        return retval;
    //    }


    /**
     * Creates a reference from the given List of Keys.
     *
     * @param value The desired list of Keys.
     * @return The created reference.
     */
    public static Reference getReferenceFromKeys(AASKeyDataType[] value) {
        Ensure.requireNonNull(value, "value must not be null");

        Reference retval;

        List<Key> keys = new ArrayList<>();
        for (AASKeyDataType key: value) {
            keys.add(new DefaultKey.Builder().type(getKeyTypes(key.getType())).value(key.getValue()).build());
        }
        retval = new DefaultReference.Builder().keys(keys).build();

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
                    aasMultiProp.setValue(ValueConverter.getLangStringSetFromLocalizedText((LocalizedText[]) variant.getValue()));
                }
                else if (variant.isEmpty()) {
                    aasMultiProp.setValue(new ArrayList<>());
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
                aasEntity.setGlobalAssetID(convertVariantValueToString(variant));
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

        if (type == SubmodelElementData.Type.PROPERTY_VALUE) {
            retval = createVariant(ElementValueMapper.<Property, PropertyValue> toValue(submodelElement).getValue().getValue());
        }
        else {
            LOGGER.warn("getSubmodelElementValue: SubmodelElement {}: unkown or invalid type {}", submodelElement.getIdShort(), type);
            throw new IllegalArgumentException("unkown type " + type);
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
                retval = ((DateTime) variant.getValue()).getUtcCalendar().toZonedDateTime().toString();
            }
            else {
                retval = variant.getValue().toString();
            }
        }

        return retval;
    }


    private static Variant createVariant(Object value) {
        Variant retval;

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


    public static Object convertTypedValue(TypedValue<?> typedValue) throws NumberFormatException {
        if (typedValue == null) {
            return null;
        }
        Object retval = typedValue.getValue();
        if ((typedValue instanceof DecimalValue) || (typedValue instanceof IntegerValue)) {
            retval = Long.valueOf(retval.toString());
        }
        else if (typedValue instanceof DateTimeValue) {
            retval = ValueConverter.createDateTime((ZonedDateTime) retval);
        }
        return retval;
    }


    /**
     * Converts the given Direction value to the corresponding AASDirectionDataType
     *
     * @param value The desired Direction value.
     * @return The converted AASDirectionDataType.
     */
    public static AASDirectionDataType getAasDirectionDataType(Direction value) {
        AASDirectionDataType retval = null;
        var rv = DIRECTION_LIST.stream().filter(m -> m.aasObject == value).findAny();
        if (rv.isEmpty()) {
            LOGGER.warn("getAasDirectionDataType: unknown value {}", value);
            throw new IllegalArgumentException("unknown Direction: " + value);
        }
        else {
            retval = rv.get().opcuaObject;
        }

        return retval;
    }


    /**
     * Converts the given StateOfEvent to the corresponding AASStateOfEventDataType.
     *
     * @param value The desired StateOfEvent
     * @return The corresponding AASStateOfEventDataType
     */
    public static AASStateOfEventDataType getAasStateOfEventType(StateOfEvent value) {
        AASStateOfEventDataType retval;
        var rv = STATE_OF_EVENT_LIST.stream().filter(m -> m.aasObject == value).findAny();
        if (rv.isEmpty()) {
            LOGGER.warn("getAasStateOfEvent: unknown value {}", value);
            throw new IllegalArgumentException("unknown StateOfEvent: " + value);
        }
        else {
            retval = rv.get().opcuaObject;
        }

        return retval;
    }


    /**
     * Converts the given StateOfEvent to the corresponding AASStateOfEventDataType.
     *
     * @param value The desired StateOfEvent
     * @return The corresponding AASStateOfEventDataType
     */
    public static AASSubmodelElementsDataType getAasSubmodelElementsType(AASSubmodelElements value) {
        AASSubmodelElementsDataType retval;
        var rv = SUBMODEL_ELEMENTS_DATATYPE.stream().filter(m -> m.aasObject == value).findAny();
        if (rv.isEmpty()) {
            LOGGER.warn("getAasSubmodelElementsType: unknown value {}", value);
            throw new IllegalArgumentException("unknown StateOfEvent: " + value);
        }
        else {
            retval = rv.get().opcuaObject;
        }

        return retval;
    }

    //    /**
    //     * Converts the given KeyElements value to the corresponding AASKeyElementsDataType
    //     *
    //     * @param value The desired KeyElements value.
    //     * @return The converted AASKeyElementsDataType.
    //     */
    //    public static AASKeyElementsDataType getAasKeyElementsDataType(KeyTypes value) {
    //        AASKeyElementsDataType retval = null;
    //        var rv = KEY_ELEMENTS_LIST.stream()
    //                .filter(m -> m.aasObject == value)
    //                .findAny();
    //        if (rv.isEmpty()) {
    //            LOGGER.warn("getAasKeyElementsDataType: unknown value {}", value);
    //            throw new IllegalArgumentException("unknown KeyElementsDataType: " + value);
    //        }
    //        else {
    //            retval = rv.get().opcuaObject;
    //        }
    //
    //        return retval;
    //    }

    //    /**
    //     * Converts the given ModelingKind to the corresponding AASModelingKindDataType.
    //     *
    //     * @param value the desired ModelingKind
    //     * @return The corresponding AASModelingKindDataType
    //     */
    //    public static AASModellingKindDataType convertModelingKind(ModellingKind value) {
    //        AASModellingKindDataType retval;
    //
    //        if (value == null) {
    //            LOGGER.warn("convertModelingKind: value == null");
    //            retval = AASModellingKindDataType.Instance;
    //        }
    //        else if (MODELING_KIND_MAP.containsKey(value)) {
    //            retval = MODELING_KIND_MAP.get(value);
    //        }
    //        else {
    //            LOGGER.warn("convertModelingKind: unknown value {}", value);
    //            throw new IllegalArgumentException("unknown ModelingKind: " + value);
    //        }
    //
    //        return retval;
    //    }
}
