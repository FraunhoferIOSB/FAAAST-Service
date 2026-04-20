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

import static de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.util.ByteArrayHelper.removePadding;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
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
import java.nio.charset.StandardCharsets;
import org.bouncycastle.util.Arrays;


/**
 * Modbus asset connection conversion helper from byte array to AAS data.
 */
public class ModbusToAasConversionHelper {

    private static final String TOO_MANY_BYTES_READ = "too many bytes read: %d, should be %d";

    private ModbusToAasConversionHelper() {}


    /**
     * Convert a byte array to an AAS DataElementValue.
     *
     * @param rawBytes Value to convert.
     * @param datatype the datatype to convert the data into.
     * @return converted data.
     * @throws ValueFormatException if datatype is not supported
     */
    public static TypedValue convert(byte[] rawBytes, Datatype datatype) throws ValueFormatException {
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
            default -> throw new ValueFormatException(String.format("type %s not supported by modbus asset connection", datatype));
        };
    }


    private static BigInteger toUnsignedBigInteger(byte[] rawBytes, long maxBytes) throws ValueFormatException {
        if (amountNonzero(rawBytes) > maxBytes) {
            throw new ValueFormatException(String.format(TOO_MANY_BYTES_READ, amountNonzero(rawBytes), maxBytes));
        }
        return new BigInteger(1, rawBytes);
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


    private static BigInteger toBigInteger(byte[] rawBytes, long maxBytes) throws ValueFormatException {
        if (amountNonzeroNonnegative(rawBytes) > maxBytes) {
            throw new ValueFormatException(String.format(TOO_MANY_BYTES_READ, amountNonzeroNonnegative(rawBytes), maxBytes));
        }
        return new BigInteger(removePadding(rawBytes));
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
}
