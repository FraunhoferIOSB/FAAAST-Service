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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.StringValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.UnsignedByteValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.UnsignedIntValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.UnsignedLongValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.UnsignedShortValue;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.bouncycastle.util.Arrays;
import org.eclipse.digitaltwin.aas4j.v3.model.File;


/**
 * Modbus asset connection conversion helper from byte array to AAS data.
 */
public class AasToModbusConversionHelper {

    private static final String TOO_MANY_BYTES_READ = "too many bytes read: %d, should be %d";

    private AasToModbusConversionHelper() {

    }


    /**
     * Convert an AAS DataElementValue to a byte array.
     *
     * @param dataElementValue Value to convert.
     * @param minBytes The resulting byte array shall contain at least minBytes bytes.
     * @return converted data.
     */
    public static byte[] convert(DataElementValue dataElementValue, int minBytes) throws AssetConnectionException {
        if (dataElementValue instanceof BlobValue blobValue) {
            return blobValue.getValue();
        }
        else if (dataElementValue instanceof File file) {
            return file.getValue().getBytes(StandardCharsets.UTF_8);
        }
        else if (dataElementValue instanceof PropertyValue propertyValue) {
            return convert(propertyValue.getValue(), minBytes);
        }
        else {
            throw new UnsupportedOperationException(
                    String.format("Data type currently not supported for writing to modbus server: %s", dataElementValue.getClass().getName()));
        }
    }


    private static byte[] convert(TypedValue value, int minBytes) throws AssetConnectionException {

        return switch (value.getDataType()) {
            case BOOLEAN -> toByteArray(((BooleanValue) value).getValue());
            case BYTE -> toByteArray(((ByteValue) value).getValue());
            case SHORT -> toByteArray(((ShortValue) value).getValue());
            case INT -> toByteArray(((IntValue) value).getValue());
            case LONG -> toByteArray(((LongValue) value).getValue());

            case UNSIGNED_BYTE -> fromUnsigned(toByteArray(((UnsignedByteValue) value).getValue()), minBytes);
            case UNSIGNED_SHORT -> fromUnsigned(toByteArray(((UnsignedShortValue) value).getValue()), minBytes);
            case UNSIGNED_INT -> fromUnsigned(toByteArray(((UnsignedIntValue) value).getValue()), minBytes);
            case UNSIGNED_LONG -> fromUnsigned(toByteArray(((UnsignedLongValue) value).getValue()), minBytes);

            case INTEGER -> toByteArray(((IntegerValue) value).getValue(), minBytes);
            case DECIMAL -> toByteArray(((DecimalValue) value).getValue().toBigInteger(), minBytes);
            case POSITIVE_INTEGER -> toByteArray(((PositiveIntegerValue) value).getValue(), minBytes);
            case NON_POSITIVE_INTEGER -> toByteArray(((NonPositiveIntegerValue) value).getValue(), minBytes);
            case NEGATIVE_INTEGER -> toByteArray(((NegativeIntegerValue) value).getValue(), minBytes);
            case NON_NEGATIVE_INTEGER -> toByteArray(((NonNegativeIntegerValue) value).getValue(), minBytes);

            case STRING -> value.asString().getBytes(StandardCharsets.UTF_8);
            case HEX_BINARY -> ((HexBinaryValue) value).getValue();
            case BASE64_BINARY -> ((Base64BinaryValue) value).getValue();
            case ANY_URI -> ((AnyURIValue) value).getValue().getBytes(StandardCharsets.UTF_8);
            // case LANG_STRING, DOUBLE, FLOAT
            default -> throw new AssetConnectionException(String.format("Data type currently not supported for writing to modbus server: %s", value.getDataType().getName()));
        };
    }


    /**
     * Convert a byte array to an AAS DataElementValue.
     *
     * @param rawBytes Value to convert.
     * @param datatype the datatype to convert the data into.
     * @return converted data.
     */
    public static TypedValue convert(byte[] rawBytes, Datatype datatype) throws AssetConnectionException {

        return switch (datatype) {
            case BOOLEAN -> new BooleanValue(toBigInteger(rawBytes, 1).signum() != 0);
            case BYTE -> new ByteValue(toBigInteger(rawBytes, 1).byteValueExact());
            case SHORT -> new ShortValue(toBigInteger(rawBytes, 2).shortValueExact());
            case INT -> new IntValue(toBigInteger(rawBytes, 4).intValueExact());
            case LONG -> new LongValue(toBigInteger(rawBytes, 8).longValueExact());

            case UNSIGNED_BYTE -> new UnsignedByteValue(toUnsignedBigInteger(rawBytes, 1).shortValueExact());
            case UNSIGNED_SHORT -> new UnsignedShortValue(toUnsignedBigInteger(rawBytes, 2).intValueExact());
            case UNSIGNED_INT -> new UnsignedIntValue(toUnsignedBigInteger(rawBytes, 4).longValueExact());
            case UNSIGNED_LONG -> new UnsignedLongValue(toUnsignedBigInteger(rawBytes, 8));

            case INTEGER -> new IntegerValue(new BigInteger(rawBytes));
            case DECIMAL -> new DecimalValue(new BigDecimal(new BigInteger(rawBytes)));
            case POSITIVE_INTEGER -> new PositiveIntegerValue(new BigInteger(rawBytes));
            case NON_POSITIVE_INTEGER -> new NonPositiveIntegerValue(new BigInteger(rawBytes));
            case NEGATIVE_INTEGER -> new NegativeIntegerValue(new BigInteger(rawBytes));
            case NON_NEGATIVE_INTEGER -> new NonNegativeIntegerValue(new BigInteger(rawBytes));

            case STRING -> new StringValue(new String(rawBytes, StandardCharsets.UTF_8));
            case HEX_BINARY -> new HexBinaryValue(rawBytes);
            case BASE64_BINARY -> new Base64BinaryValue(rawBytes);
            case ANY_URI -> new AnyURIValue(new String(rawBytes, StandardCharsets.UTF_8));
            // case LANG_STRING, DOUBLE, FLOAT
            default -> throw new AssetConnectionException(String.format("type %s not supported by modbus asset connection", datatype));
        };
    }


    private static byte[] toByteArray(BigInteger big, int minLength) {
        byte[] base = big.toByteArray();
        byte[] returnArray = new byte[Math.max(base.length, minLength)];
        if (big.signum() == -1) {
            Arrays.fill(returnArray, (byte) 0xFF);
        }
        System.arraycopy(base, 0, returnArray, returnArray.length - base.length, base.length);
        return returnArray;
    }


    private static int amountNonzero(byte[] array) {
        int nonzero = 0;
        for (byte b: Arrays.reverse(array)) {
            if (b != 0x0) {
                nonzero++;
            }
            else {
                break;
            }
        }
        return nonzero;
    }


    private static int amountNonzeroNonnegative(byte[] array) {
        int nonzero = array.length;
        boolean zeroPaddingOver = false;
        for (byte b: array) {
            if (!zeroPaddingOver && b == (byte) 0x0) {
                nonzero--;
                continue;
            }
            zeroPaddingOver = true;

            if (b == (byte) 0xFF) {
                nonzero--;
            }
            else {
                break;
            }
        }
        return nonzero;
    }


    private static BigInteger toUnsignedBigInteger(byte[] rawBytes, long maxBytes) throws AssetConnectionException {
        if (amountNonzero(rawBytes) > maxBytes) {
            throw new AssetConnectionException(String.format(TOO_MANY_BYTES_READ, amountNonzero(rawBytes), maxBytes));
        }
        return new BigInteger(1, rawBytes);
    }


    private static BigInteger toBigInteger(byte[] rawBytes, long maxBytes) throws AssetConnectionException {
        if (amountNonzeroNonnegative(rawBytes) > maxBytes) {
            throw new AssetConnectionException(String.format(TOO_MANY_BYTES_READ, amountNonzeroNonnegative(rawBytes), maxBytes));
        }
        return new BigInteger(ByteArrayHelper.removePadding(rawBytes));
    }


    private static byte[] fromUnsigned(byte[] rawBytes, int minLength) {
        // Unsigned datatypes are represented as the next bigger datatype in AAS-land
        // e.g., unsigned short --> int, the first half of the byte array is always zero.
        // Modbus is not concerned with signed/unsigned data, so we simply store the bare data.
        // When converting read unsigned byte[]-data into AAS data again, zero-padding is applied again.
        int cutoffLength = Math.min(Math.max(1, (int) (rawBytes.length / 2.0)), rawBytes.length - minLength);

        if (cutoffLength < 1) {
            return rawBytes;
        }
        byte[] cutoff = new byte[rawBytes.length - cutoffLength];

        // big endian, so cut off first byte (most significant)
        System.arraycopy(rawBytes, cutoffLength, cutoff, 0, rawBytes.length - cutoffLength);
        return cutoff;
    }


    private static byte[] toByteArray(long l) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(l);
        return buffer.array();
    }


    private static byte[] toByteArray(BigInteger b) {
        return b.toByteArray();
    }


    private static byte[] toByteArray(int i) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(i);
        return buffer.array();
    }


    private static byte[] toByteArray(short s) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(s);
        return buffer.array();
    }


    private static byte[] toByteArray(byte b) {
        return new byte[] {
                b
        };
    }


    private static byte[] toByteArray(boolean b) {
        return toByteArray((b ? 1 : 0));
    }
}
