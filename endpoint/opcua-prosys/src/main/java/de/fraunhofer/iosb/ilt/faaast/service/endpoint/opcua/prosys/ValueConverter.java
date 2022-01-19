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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.prosys;

import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.core.Identifiers;
import io.adminshell.aas.v3.model.AssetKind;
import io.adminshell.aas.v3.model.EntityType;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.ModelingKind;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.ArrayList;
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
public class ValueConverter {

    private static final Logger logger = LoggerFactory.getLogger(ValueConverter.class);

    /**
     * Creates a new instance of ValueConverter
     */
    public ValueConverter() {}


    //    /**
    //     * Converts a value from the OPC UA type (DataValue) to its corresponding AAS Service Type (ValueDataType)
    //     * @param dv The OPC UA value to convert
    //     * @param valueType The desired AAS Service data type for the destination type
    //     * @return The converted AAS Service type
    //     * @throws IOException I/O exception
    //     * @throws DatatypeConfigurationException Indicates a serious configuration error.
    //     */
    //    public static ValueDataType convertToValueType(DataValue dv, DataTypeDef valueType) throws IOException, DatatypeConfigurationException {
    //        ValueDataType retval = null;
    //        
    //        try {
    //            switch(valueType) {
    //                case AnyURI:
    //                    retval = valueType.create(dv.getValue().toString());
    //                    break;
    //            
    //                case Base64Binary:
    //                    retval = valueType.create(dv.getValue().toString());
    //                    break;
    //
    //                case Boolean:
    //                    retval = valueType.create(dv.getValue().toString());
    //                    break;
    //
    //                case Date:
    //                    if (dv.getValue().getValue() instanceof DateTime) {
    //                        DateTime dt = (DateTime)dv.getValue().getValue();
    //                        retval = new DateValue(DatatypeFactory.newInstance().newXMLGregorianCalendar(dt.getUtcCalendar()));
    //                    }
    //                    else {
    //                        logger.warn("convertToValueType (Date): cannot convert value to DateTime. Try default handler.");
    //                        retval = valueType.create(dv.getValue().toString());
    //                    }
    //                    break;
    //
    //                case Decimal:
    //                    retval = valueType.create(dv.getValue().toString());
    //                    break;
    //            
    //                case Integer:
    //                    retval = valueType.create(dv.getValue().toString());
    //                    break;
    //
    //                case Int:
    //                    retval = valueType.create(dv.getValue().toString());
    //                    break;
    //
    //                case Long:
    //                    retval = valueType.create(dv.getValue().toString());
    //                    break;
    //
    //                case Short:
    //                    retval = valueType.create(dv.getValue().toString());
    //                    break;
    //
    //                case Byte:
    //                    retval = valueType.create(dv.getValue().toString());
    //                    break;
    //
    //                case Double:
    //                    retval = new DoubleValue(dv.getValue().doubleValue());
    //                    break;
    //
    //                case Float:
    //                    retval = new FloatValue(dv.getValue().floatValue());
    //                    break;
    //            
    //                case QName:
    //                    retval = valueType.create(dv.getValue().toString());
    //                    break;
    //
    //                case String:
    //                    retval = valueType.create(dv.getValue().toString());
    //                    break;
    //
    //                case Time:
    //                    if (dv.getValue().getValue() instanceof DateTime) {
    //                        DateTime dt = (DateTime)dv.getValue().getValue();
    //                        retval = new TimeValue(DatatypeFactory.newInstance().newXMLGregorianCalendar(dt.getUtcCalendar()));
    //                    }
    //                    else {
    //                        logger.warn("convertToValueType (Time): cannot convert value to DateTime. Try default handler.");
    //                        retval = valueType.create(dv.getValue().toString());
    //                    }
    //                    break;
    //                    
    //                case Duration:
    //                    retval = valueType.create(dv.getValue().toString());
    //                    break;
    //                    
    //                default:
    //                    logger.warn("convertToValueType: unknown type: " + valueType);
    //                    throw new IllegalArgumentException("unknown type: " + valueType);
    //            }
    //        }
    //        catch (Throwable ex) {
    //            logger.error("convertToValueType Exception", ex);
    //            throw ex;
    //        }
    //        
    //        return retval;
    //    }
    //    
    //    /**
    //     * Converts a value from the AAS Service Type (ValueDataType) to its corresponding OPC UA type 
    //     * @param val The AAS source value
    //     * @param valueType The desired AAS Service data type 
    //     * @return The converted OPC UA value
    //     */
    //    public static Object convertToOpcUAType(ValueDataType val, DataTypeDef valueType) {
    //        Object retval = null;
    //        
    //        try {
    //            switch(valueType) {
    //                case AnyURI:
    //                    retval = ((AnyUri)val).getValue().toString();
    //                    break;
    //
    //                case Base64Binary:
    //                    retval = ((Base64Binary)val).getValue();
    //                    break;
    //
    //                case Boolean:
    //                    retval = ((BooleanValue)val).getValue();
    //                    break;
    //
    //                case Date:
    //                    retval = new DateTime(((DateValue)val).getValue().toGregorianCalendar());
    //                    break;
    //
    //                case Decimal:
    //                    retval = ((DecimalValue)val).getValue().longValue();
    //                    break;
    //
    //                case Integer:
    //                    retval = ((IntegerValue)val).getValue().longValue();
    //                    break;
    //                    
    //                case Int:
    //                    retval = ((IntValue)val).getValue();
    //                    break;
    //
    //                case Long:
    //                    retval = ((LongValue)val).getValue();
    //                    break;
    //
    //                case Short:
    //                    retval = ((ShortValue)val).getValue();
    //                    break;
    //
    //                case Byte:
    //                    retval = ((ByteValue)val).getValue();
    //                    break;
    //
    //                case Double:
    //                    retval = ((DoubleValue)val).getValue();
    //                    break;
    //
    //                case Float:
    //                    retval = ((FloatValue)val).getValue();
    //                    break;
    //
    //                case QName:
    //                    retval = ((QNameValue)val).getValue().toString();
    //                    break;
    //
    //                case String:
    //                    retval = ((StringValue)val).getValue();
    //                    break;
    //
    //                case Time:
    //                    retval = new DateTime(((TimeValue)val).getValue().toGregorianCalendar());
    //                    break;
    //
    //                case Duration:
    //                    retval = ((DurationValue)val).getValue().toString();
    //                    break;
    //                    
    //                default:
    //                    logger.warn("convertToOpcUAType: unknown type: " + valueType);
    //                    throw new IllegalArgumentException("unknown type: " + valueType);
    //            }
    //        }
    //        catch (Throwable ex) {
    //            logger.error("convertToOpcUAType Exception", ex);
    //            throw ex;
    //        }
    //        
    //        return retval;
    //    }
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
                //                    case AnyURI:
                //                        retval = Identifiers.String;
                //                        break;

                case "bytestring":
                    retval = Identifiers.ByteString;
                    break;

                case "boolean":
                    retval = Identifiers.Boolean;
                    break;

                case "datetime":
                    retval = Identifiers.DateTime;
                    break;

                //                    case Decimal:
                //                        retval = Identifiers.Int64;
                //                        break;
                //                    case Integer:
                //                        retval = Identifiers.Int64;
                //                        break;
                case "int":
                case "integer":
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

                //                    case Duration:
                //                        retval = Identifiers.String;
                //                        break;
                default:
                    logger.warn("convertValueTypeStringToNodeId: Unknown type: " + valueType);
                    retval = NodeId.NULL;
                    break;
            }
        }
        catch (Throwable ex) {
            logger.error("convertValueTypeStringToNodeId Exception", ex);
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
                case "integer":
                    retval = AASValueTypeDataType.Int32;
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

                default:
                    logger.warn("stringToValueType: unknown value: " + value);
                    throw new IllegalArgumentException("unknown value: " + value);
            }
        }
        catch (Throwable ex) {
            logger.error("stringToValueType Exception", ex);
            throw ex;
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
            logger.warn("convertModelingKind: value == null");
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
                    logger.warn("convertModelingKind: unknown value " + value);
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
                logger.warn("convertIdentifierType: unknown value " + value);
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
                logger.warn("convertAssetKind: unknown value " + value);
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
    public static AASEntityTypeDataType convertEntityType(EntityType value) {
        AASEntityTypeDataType retval = AASEntityTypeDataType.valueOf(value.ordinal());
        return retval;
    }


    /**
     * Gets a LocalizedText array from an AAS Lang String Set
     *
     * @param value The desired AAS Lang String
     * @return The corresponding LocalizedText array
     */
    public static LocalizedText[] getLocalizedTextFromLangStringSet(List<LangString> value) {
        LocalizedText[] retval = null;

        try {
            ArrayList<LocalizedText> arr = new ArrayList<>();
            value.forEach(ls -> {
                arr.add(new LocalizedText(ls.getValue(), ls.getLanguage()));
            });

            retval = arr.toArray(LocalizedText[]::new);
        }
        catch (Throwable ex) {
            logger.error("getLocalizedTextFromLangStringSet Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Gets AAS Lang String Set from a LocalizedText array.
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
        catch (Throwable ex) {
            logger.error("getLangStringSetFromLocalizedText Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Converts the given KeyElements value to AASKeyElementsDataType
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
                    logger.warn("getKeyElementsDataTypeFromKeyElements: BASIC_EVENT not available in AASKeyElementsDataType");
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
                    logger.warn("getKeyElementsDataType: unknown KeyElement: " + keyElement);
                    break;
            }
        }
        catch (Throwable ex) {
            logger.error("getKeyElementsDataType Exception", ex);
            throw ex;
        }

        return retval;
    }


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

                //                case BASIC_EVENT:
                //                    logger.warn("getKeyElementsDataTypeFromKeyElements: BASIC_EVENT not available in KeyElements");
                //                    throw new IllegalArgumentException("BASIC_EVENT not available in KeyElements");

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
                    logger.warn("getKeyElements: unknown AASKeyElementsDataType: " + value);
                    break;
            }
        }
        catch (Throwable ex) {
            logger.error("getKeyElements Exception", ex);
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
                logger.warn("getAasKeyType: unknown value " + value);
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
                logger.warn("getKeyType: unknown value " + value);
                throw new IllegalArgumentException("unknown AASKeyTypeDataType: " + value);
        }

        return retval;
    }


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
        catch (Throwable ex) {
            logger.error("getReferenceFromKeys Exception", ex);
            throw ex;
        }

        return retval;
    }
}
