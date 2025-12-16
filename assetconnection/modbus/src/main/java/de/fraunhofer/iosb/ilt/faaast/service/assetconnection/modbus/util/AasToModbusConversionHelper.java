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

import static de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype.BOOLEAN;
import static de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype.BYTE;
import static de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype.DECIMAL;
import static de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype.INT;
import static de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype.LONG;

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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DoubleValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.FloatValue;
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
import org.eclipse.digitaltwin.aas4j.v3.model.File;


/**
 * Modbus asset connection conversion helper from byte array to AAS data.
 */
public class AasToModbusConversionHelper {

    private static final String TOO_MANY_BYTES_READ = "too many bytes read for %s: %d";

    private AasToModbusConversionHelper() {

    }


    /**
     * Convert an AAS DataElementValue to a byte array.
     *
     * @param dataElementValue Value to convert.
     * @return converted data.
     */
    public static byte[] convert(DataElementValue dataElementValue) throws AssetConnectionException {
        if (dataElementValue instanceof BlobValue blobValue) {
            return blobValue.getValue();
        }
        else if (dataElementValue instanceof File file) {
            return file.getValue().getBytes(StandardCharsets.UTF_8);
        }
        else if (dataElementValue instanceof PropertyValue propertyValue) {
            return convert(propertyValue.getValue());
        }
        else {
            throw new UnsupportedOperationException(
                    String.format("Data type currently not supported for writing to modbus server: %s", dataElementValue.getClass().getName()));
        }
    }


    private static byte[] convert(TypedValue value) throws AssetConnectionException {

        return switch (value.getDataType()) {
            case STRING -> value.asString().getBytes(StandardCharsets.UTF_8);
            case BOOLEAN -> toByteArray(((BooleanValue) value).getValue());
            case DECIMAL -> ((DecimalValue) value).getValue().toBigInteger().toByteArray();
            case INTEGER -> ((IntegerValue) value).getValue().toByteArray();
            // TODO double and float could be undefined: what would be the byte encoding? IEEE 754? BCD?
            // TODO furthermore, how to convert bytes to double is also undefined
            case DOUBLE -> toByteArray(Double.doubleToLongBits(((DoubleValue) value).getValue()));
            case FLOAT -> toByteArray(Double.doubleToLongBits(((FloatValue) value).getValue().doubleValue()));
            case BYTE -> toByteArray(((ByteValue) value).getValue());
            case SHORT -> toByteArray(((ShortValue) value).getValue());
            case INT -> toByteArray(((IntValue) value).getValue());
            case LONG -> toByteArray(((LongValue) value).getValue());
            case UNSIGNED_BYTE -> fromUnsigned(toByteArray(((ByteValue) value).getValue()));
            case UNSIGNED_SHORT -> fromUnsigned(toByteArray(((ShortValue) value).getValue()));
            case UNSIGNED_INT -> fromUnsigned(toByteArray(((IntValue) value).getValue()));
            case UNSIGNED_LONG -> fromUnsigned(toByteArray(((LongValue) value).getValue()));
            case POSITIVE_INTEGER -> toByteArray(((PositiveIntegerValue) value).getValue().intValueExact());
            case NON_NEGATIVE_INTEGER -> toByteArray(((NonNegativeIntegerValue) value).getValue().intValueExact());
            case NEGATIVE_INTEGER -> toByteArray(((NegativeIntegerValue) value).getValue().intValueExact());
            case NON_POSITIVE_INTEGER -> toByteArray(((NonPositiveIntegerValue) value).getValue().intValueExact());
            case HEX_BINARY -> ((HexBinaryValue) value).getValue();
            case BASE64_BINARY -> ((Base64BinaryValue) value).getValue();
            case ANY_URI -> ((AnyURIValue) value).getValue().getBytes(StandardCharsets.UTF_8);
            // LANG_STRING not supported (out of scope of modbus protocol)
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
    public static TypedValue<?> convert(byte[] rawBytes, Datatype datatype) throws AssetConnectionException {
        return switch (datatype) {
            case STRING -> new StringValue(new String(rawBytes, StandardCharsets.UTF_8));
            case BOOLEAN -> {
                if (rawBytes.length > 1) {
                    doThrow(TOO_MANY_BYTES_READ, BOOLEAN, rawBytes.length);
                }
                yield new BooleanValue(rawBytes[0] != 0);
            }
            case DECIMAL -> {
                if (rawBytes.length > 1) {
                    doThrow(TOO_MANY_BYTES_READ, DECIMAL, rawBytes.length);
                }
                yield new DecimalValue(new BigDecimal(toBigInteger(rawBytes)));
            }
            case INTEGER -> new IntegerValue(toBigInteger(rawBytes));
            // TODO double and float could be undefined: what would be the byte encoding? IEEE 754?
            case DOUBLE -> new DoubleValue(ByteBuffer.wrap(rawBytes).getDouble());
            case FLOAT -> new FloatValue(ByteBuffer.wrap(rawBytes).getFloat());
            case BYTE -> {
                if (rawBytes.length > 1) {
                    doThrow(TOO_MANY_BYTES_READ, BYTE, rawBytes.length);
                }
                yield new ByteValue(rawBytes[0]);
            }
            case SHORT -> {
                if (rawBytes.length > 2) {
                    doThrow(TOO_MANY_BYTES_READ, BYTE, rawBytes.length);
                }
                yield new ShortValue(toBigInteger(rawBytes).shortValueExact());
            }
            case INT -> {
                if (rawBytes.length > 4) {
                    doThrow(TOO_MANY_BYTES_READ, INT, rawBytes.length);
                }
                yield new IntValue(toBigInteger(rawBytes).intValueExact());
            }
            case LONG -> {
                if (rawBytes.length > 8) {
                    doThrow(TOO_MANY_BYTES_READ, LONG, rawBytes.length);
                }
                yield new LongValue(toBigInteger(rawBytes).longValueExact());
            }
            case UNSIGNED_BYTE -> {
                if (rawBytes.length > 1) {
                    doThrow(TOO_MANY_BYTES_READ, BYTE, rawBytes.length);
                }
                yield new UnsignedByteValue(toUnsignedBigInteger(rawBytes).shortValueExact());
            }
            case UNSIGNED_SHORT -> {
                if (rawBytes.length > 2) {
                    doThrow(TOO_MANY_BYTES_READ, BYTE, rawBytes.length);
                }
                yield new UnsignedShortValue(toUnsignedBigInteger(rawBytes).intValueExact());
            }
            case UNSIGNED_INT -> {
                if (rawBytes.length > 4) {
                    doThrow(TOO_MANY_BYTES_READ, BYTE, rawBytes.length);
                }
                yield new UnsignedIntValue(toUnsignedBigInteger(rawBytes).longValueExact());
            }
            case UNSIGNED_LONG -> {
                if (rawBytes.length > 8) {
                    doThrow(TOO_MANY_BYTES_READ, BYTE, rawBytes.length);
                }
                yield new UnsignedLongValue(toUnsignedBigInteger(rawBytes));
            }
            case POSITIVE_INTEGER -> {
                if (rawBytes.length > 4) {
                    doThrow(TOO_MANY_BYTES_READ, BYTE, rawBytes.length);
                }
                yield new PositiveIntegerValue(toBigInteger(rawBytes));
            }
            case NON_NEGATIVE_INTEGER -> {
                if (rawBytes.length > 4) {
                    doThrow(TOO_MANY_BYTES_READ, BYTE, rawBytes.length);
                }
                yield new NonNegativeIntegerValue(toBigInteger(rawBytes));
            }
            case NEGATIVE_INTEGER -> {
                if (rawBytes.length > 4) {
                    doThrow(TOO_MANY_BYTES_READ, BYTE, rawBytes.length);
                }
                yield new NegativeIntegerValue(toBigInteger(rawBytes));
            }
            case NON_POSITIVE_INTEGER -> {
                if (rawBytes.length > 4) {
                    doThrow(TOO_MANY_BYTES_READ, BYTE, rawBytes.length);
                }
                yield new NonPositiveIntegerValue(toBigInteger(rawBytes));
            }
            case HEX_BINARY -> new HexBinaryValue(rawBytes);
            case BASE64_BINARY -> new Base64BinaryValue(rawBytes);
            case ANY_URI -> new AnyURIValue(new String(rawBytes, StandardCharsets.UTF_8));
            // LANG_STRING not supported (out of scope of modbus protocol)
            default -> throw new AssetConnectionException(String.format("Data type currently not supported for writing to modbus server: %s", datatype.getName()));
        };
    }


    private static void doThrow(String reason, Datatype datatype, int totalBytes) throws AssetConnectionException {
        throw new AssetConnectionException(String.format(reason, datatype, totalBytes));
    }


    private static BigInteger toUnsignedBigInteger(byte[] rawBytes) {
        return new BigInteger(1, rawBytes);
    }


    private static BigInteger toBigInteger(byte[] rawBytes) {
        return new BigInteger(rawBytes);
    }


    private static byte[] fromUnsigned(byte[] rawBytes) {
        byte[] cutoff = new byte[rawBytes.length - 1];

        // big endian, so cut off first byte (most significant)
        System.arraycopy(rawBytes, 1, cutoff, 0, rawBytes.length - 1);
        return cutoff;
    }


    private static byte[] toByteArray(long l) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(l);
        return buffer.array();
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
