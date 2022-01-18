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
import io.adminshell.aas.v3.model.LangString;
import java.util.ArrayList;
import java.util.List;
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
}
