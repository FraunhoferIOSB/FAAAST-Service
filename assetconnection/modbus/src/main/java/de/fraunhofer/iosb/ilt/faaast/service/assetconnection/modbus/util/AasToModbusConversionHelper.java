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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.util;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.BlobValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.AnyURIValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Base64BinaryValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.BooleanValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ByteValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DecimalValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.HexBinaryValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.IntValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.IntegerValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.LongValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.NegativeIntegerValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.NonNegativeIntegerValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.NonPositiveIntegerValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.PositiveIntegerValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ShortValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.UnsignedByteValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.UnsignedIntValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.UnsignedLongValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.UnsignedShortValue;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.eclipse.digitaltwin.aas4j.v3.model.File;


/**
 * Modbus asset connection conversion helper from AAS data to byte array.
 */
public class AasToModbusConversionHelper {

    private AasToModbusConversionHelper() {

    }


    /**
     * Convert an AAS DataElementValue to a byte array.
     *
     * @param dataElementValue Value to convert.
     * @param numberBytes If true, the resulting array will have an even number of bytes.
     * @return converted data.
     */
    public static byte[] convert(DataElementValue dataElementValue, int numberBytes) throws AssetConnectionException {
        if (dataElementValue == null) {
            throw new AssetConnectionException("Trying to convert null value");
        }
        else if (dataElementValue instanceof BlobValue blobValue) {
            return ByteArrayHelper.padToWords(blobValue.getValue(), numberBytes, false);
        }
        else if (dataElementValue instanceof File file) {
            return ByteArrayHelper.padToWords(file.getValue().getBytes(StandardCharsets.UTF_8), numberBytes, false);
        }
        else if (dataElementValue instanceof PropertyValue propertyValue) {
            return convert(propertyValue.getValue(), numberBytes);
        }
        else {
            throw new UnsupportedOperationException(
                    String.format("Data type currently not supported for writing to modbus server: %s", dataElementValue.getClass().getName()));
        }
    }


    private static byte[] convert(TypedValue value, int numberBytes) throws AssetConnectionException {
        return switch (value.getDataType()) {
            case BOOLEAN -> toByteArray(((BooleanValue) value).getValue(), numberBytes);
            case BYTE -> toByteArray(((ByteValue) value).getValue(), numberBytes);
            case SHORT -> toByteArray(((ShortValue) value).getValue());
            case INT -> toByteArray(((IntValue) value).getValue());
            case LONG -> toByteArray(((LongValue) value).getValue());

            case UNSIGNED_BYTE -> toByteArray(((UnsignedByteValue) value).getValue());
            case UNSIGNED_SHORT -> toByteArray(((UnsignedShortValue) value).getValue());
            case UNSIGNED_INT -> toByteArray(((UnsignedIntValue) value).getValue());
            case UNSIGNED_LONG -> toByteArray(((UnsignedLongValue) value).getValue(), numberBytes, false);

            case INTEGER -> toByteArray(((IntegerValue) value).getValue(), numberBytes, true);
            case DECIMAL -> toByteArray(((DecimalValue) value).getValue().toBigInteger(), numberBytes, true);
            case POSITIVE_INTEGER -> toByteArray(((PositiveIntegerValue) value).getValue(), numberBytes, false);
            case NON_POSITIVE_INTEGER -> toByteArray(((NonPositiveIntegerValue) value).getValue(), numberBytes, true);
            case NEGATIVE_INTEGER -> toByteArray(((NegativeIntegerValue) value).getValue(), numberBytes, true);
            case NON_NEGATIVE_INTEGER -> toByteArray(((NonNegativeIntegerValue) value).getValue(), numberBytes, true);

            case STRING -> ByteArrayHelper.padToWords(value.asString().getBytes(StandardCharsets.UTF_8), numberBytes, false);
            case HEX_BINARY -> ByteArrayHelper.padToWords(((HexBinaryValue) value).getValue(), numberBytes, false);
            case BASE64_BINARY -> ByteArrayHelper.padToWords(((Base64BinaryValue) value).getValue(), numberBytes, false);
            case ANY_URI -> ByteArrayHelper.padToWords(((AnyURIValue) value).getValue().getBytes(StandardCharsets.UTF_8), numberBytes, false);
            // case LANG_STRING, DOUBLE, FLOAT
            default -> throw new AssetConnectionException(String.format("Data type currently not supported for writing to modbus server: %s", value.getDataType().getName()));
        };
    }


    private static byte[] toByteArray(BigInteger b, int numberBytes, boolean signed) {
        return ByteArrayHelper.padToWords(ByteArrayHelper.removePadding(b.toByteArray()), numberBytes, signed);
    }


    private static byte[] toByteArray(long l) {
        return ByteBuffer.allocate(8)
                .putLong(l)
                .array();
    }


    private static byte[] toByteArray(int i) {
        return ByteBuffer.allocate(4)
                .putInt(i)
                .array();
    }


    private static byte[] toByteArray(short s) {
        return ByteBuffer.allocate(2)
                .putShort(s)
                .array();
    }


    private static byte[] toByteArray(byte b, int numberBytes) {
        byte[] ret = new byte[numberBytes];
        ret[ret.length - 1] = b;
        return ret;
    }


    private static byte[] toByteArray(boolean b, int numberBytes) {
        return toByteArray((byte) (b ? 0x1 : 0x0), numberBytes);
    }
}
