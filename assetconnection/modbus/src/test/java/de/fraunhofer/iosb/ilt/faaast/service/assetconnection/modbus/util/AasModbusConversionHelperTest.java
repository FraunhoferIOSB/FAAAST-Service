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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;


public class AasModbusConversionHelperTest {

    @Test
    public void testNormalValueSucceeds() throws ValueFormatException {
        // Modbus uses big endian, so the resulting number would be 0x1234 if 0x12 were received before 0x34.
        byte[] bytesRead = new byte[] {
                0x12,
                0x34
        };
        // 8234h -> -32204d
        int shouldBe = 4660;
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, Datatype.INT);
        assertEquals(shouldBe, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        assertArrayEquals(bytesRead, removePadding(bytesConverted));
    }


    @Test
    public void testNegativeValueSucceeds() throws ValueFormatException {
        byte[] bytesRead = new byte[] {
                (byte) 0x82,
                0x34
        };
        // 1234h -> 4660d
        int shouldBe = -32204;
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, Datatype.INT);
        assertEquals(shouldBe, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        // Padding a (semantically) negative number with FF is valid
        byte[] bytesReadPadded = new byte[] {
                (byte) 0xFF,
                (byte) 0xFF,
                (byte) 0x82,
                0x34
        };
        assertArrayEquals(bytesReadPadded, removePadding(bytesConverted));
    }


    @Test(expected = ValueFormatException.class)
    public void testLargeValueFails() throws ValueFormatException {
        byte[] bytesRead = new byte[] {
                0x12,
                0x34,
                0x56,
                0x78,
                (byte) 0x9A
        };
        ModbusToAasConversionHelper.convert(bytesRead, Datatype.INT);
    }


    @Test
    public void testTooFewBytesUnsignedSucceeds() throws ValueFormatException {
        // Try e.g., one byte for an integer and see if it fails. should not.
        byte[] bytesRead = new byte[] {
                (byte) 0x80
        };
        // 80h -> -128d
        int shouldBe = -128;
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, Datatype.INT);
        assertEquals(shouldBe, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        // Padding a (semantically) negative number with FF is valid
        byte[] bytesReadPadded = new byte[] {
                (byte) 0xFF,
                (byte) 0xFF,
                (byte) 0xFF,
                (byte) 0x80
        };
        assertArrayEquals(bytesReadPadded, removePadding(bytesConverted));
    }


    @Test
    public void testTooFewBytesSucceeds() throws ValueFormatException {
        // Try e.g., one byte for an integer and see if it fails. should not.
        byte[] bytesRead = new byte[] {
                (byte) 0x80
        };
        // 80h -> -128d
        short shouldBe = 128;
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, Datatype.UNSIGNED_BYTE);
        assertEquals(shouldBe, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        assertArrayEquals(bytesRead, removePadding(bytesConverted));
    }


    @Test
    public void testNegativeUnsignedNumberSucceeds() throws ValueFormatException {
        // Put MSB to 1 to check if it is handled correctly
        byte[] bytesRead = new byte[] {
                (byte) 0x80,
                0x00,
                0x00,
                0x01
        };
        // 80000001h -> 2147483649d
        long shouldBe = ((long) Integer.MIN_VALUE) * (-1) + 1;
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, Datatype.UNSIGNED_INT);
        assertEquals(shouldBe, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        assertArrayEquals(bytesRead, removePadding(bytesConverted));
    }


    @Test
    public void testNegativeNumberSignedSucceeds() throws ValueFormatException {
        // Put MSB to 1 to check if it is handled correctly
        byte[] bytesRead = new byte[] {
                (byte) 0x80,
                0x00,
                0x01
        };
        // 800001h -> -8388607d
        int shouldBe = -8388607;
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, Datatype.INT);
        assertEquals(shouldBe, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        // Padding a (semantically) negative number with FF is valid
        byte[] bytesReadPadded = new byte[] {
                (byte) 0xFF,
                (byte) 0x80,
                0x00,
                0x01
        };
        assertArrayEquals(bytesReadPadded, removePadding(bytesConverted));
    }


    @Test
    public void testStringSucceeds() throws ValueFormatException {
        byte[] bytesRead = "Hello World".getBytes(StandardCharsets.UTF_8);
        String shouldBe = "Hello World";
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, Datatype.STRING);
        assertEquals(shouldBe, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        assertArrayEquals(bytesRead, removePadding(bytesConverted));
    }


    @Test
    public void testAnyUriSucceeds() throws ValueFormatException, URISyntaxException {
        byte[] bytesRead = new URI("https://invalid.local/path/param?query=2&other=4#test").toString().getBytes(StandardCharsets.UTF_8);
        String shouldBe = new URI("https://invalid.local/path/param?query=2&other=4#test").toString();
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, Datatype.ANY_URI);
        assertEquals(shouldBe, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        assertArrayEquals(bytesRead, removePadding(bytesConverted));
    }


    @Test
    public void testBase64BinarySucceeds() throws ValueFormatException {
        testConvert(Datatype.BASE64_BINARY);
    }


    @Test
    public void testHexBinarySucceeds() throws ValueFormatException {
        testConvert(Datatype.HEX_BINARY);
    }


    @Test(expected = ValueFormatException.class)
    public void testLangStringFails() throws ValueFormatException {
        testConvert(Datatype.LANG_STRING);
    }


    private void testConvert(Datatype datatype) throws ValueFormatException {
        byte[] bytesRead = new byte[] {
                (byte) 0xAB,
                (byte) 0xCD,
                (byte) 0xDE,
                (byte) 0xFF
        };
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, datatype);
        assertEquals(bytesRead, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        assertArrayEquals(bytesRead, removePadding(bytesConverted));
    }


    @Test
    public void testBooleanSucceeds() throws ValueFormatException {
        byte[] bytesRead = new byte[] {
                0x0,
                0x1
        };
        boolean shouldBe = true;
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, Datatype.BOOLEAN);
        assertEquals(shouldBe, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        assertArrayEquals(removePadding(bytesRead), removePadding(bytesConverted));
    }


    @Test(expected = ValueFormatException.class)
    public void testBooleanTooManyBytesFails() throws ValueFormatException {
        byte[] bytesRead = new byte[] {
                0x1,
                0x0
        };
        ModbusToAasConversionHelper.convert(bytesRead, Datatype.BOOLEAN);
    }


    @Test
    public void testFullUnsignedLongSucceeds() throws ValueFormatException {
        byte[] bytesRead = new byte[] {
                (byte) 0xFF,
                (byte) 0xFF,
                (byte) 0xFF,
                (byte) 0xFF
        };
        BigInteger shouldBe = new BigInteger(1, bytesRead);
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, Datatype.UNSIGNED_LONG);
        assertEquals(shouldBe, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        assertArrayEquals(bytesRead, removePadding(bytesConverted));
    }


    @Test
    public void testFullSignedLongSucceeds() throws ValueFormatException {
        byte[] bytesRead = new byte[] {
                (byte) 0x80,
                // 1000 0000
                (byte) 0x00,
                // 0000 0000
                (byte) 0x00,
                // 0000 0000
                (byte) 0x00,
                // 0000 0000
                (byte) 0x00,
                // 0000 0000
                (byte) 0x00,
                // 0000 0000
                (byte) 0x00,
                // 0000 0000
                (byte) 0x00
                // 0000 0000
        };
        long shouldBe = Long.MIN_VALUE;
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, Datatype.LONG);
        assertEquals(shouldBe, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        assertArrayEquals(bytesRead, removePadding(bytesConverted));
    }


    @Test
    public void testFullUnsignedIntSucceeds() throws ValueFormatException {
        byte[] bytesRead = new byte[] {
                (byte) 0xFF,
                (byte) 0xFF
        };
        long shouldBe = new BigInteger(1, bytesRead).longValue();
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, Datatype.UNSIGNED_INT);
        assertEquals(shouldBe, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        assertArrayEquals(bytesRead, removePadding(bytesConverted));
    }


    @Test
    public void testFullSignedIntSucceeds() throws ValueFormatException {
        byte[] bytesRead = new byte[] {
                (byte) 0x80,
                // 1000 0000
                (byte) 0x00,
                // 0000 0000
                (byte) 0x00,
                // 0000 0000
                (byte) 0x00
                // 0000 0000
        };
        int shouldBe = Integer.MIN_VALUE;
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, Datatype.INT);
        assertEquals(shouldBe, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        assertArrayEquals(bytesRead, removePadding(bytesConverted));
    }


    @Test
    public void testFullUnsignedShortSucceeds() throws ValueFormatException {
        byte[] bytesRead = new byte[] {
                (byte) 0xFF
        };
        int shouldBe = new BigInteger(1, bytesRead).intValue();
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, Datatype.UNSIGNED_SHORT);
        assertEquals(shouldBe, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        assertArrayEquals(bytesRead, removePadding(bytesConverted));
    }


    @Test
    public void testFullSignedShortSucceeds() throws ValueFormatException {
        byte[] bytesRead = new byte[] {
                (byte) 0x80,
                // 1000 0000
                (byte) 0x00
                // 0000 0000
        };
        short shouldBe = Short.MIN_VALUE;
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, Datatype.SHORT);
        assertEquals(shouldBe, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        assertArrayEquals(bytesRead, removePadding(bytesConverted));
    }


    @Test
    public void testFullUnsignedByteSucceeds() throws ValueFormatException {
        byte[] bytesRead = new byte[] {
                (byte) 0xF
        };
        short shouldBe = new BigInteger(1, bytesRead).shortValue();
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, Datatype.UNSIGNED_BYTE);
        assertEquals(shouldBe, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        assertArrayEquals(bytesRead, removePadding(bytesConverted));
    }


    @Test
    public void testFullSignedByteSucceeds() throws ValueFormatException {
        byte[] bytesRead = new byte[] {
                (byte) 0x80
                // 1000 0000
        };
        byte shouldBe = Byte.MIN_VALUE;
        var typedValue = ModbusToAasConversionHelper.convert(bytesRead, Datatype.BYTE);
        assertEquals(shouldBe, typedValue.getValue());
        var bytesConverted = AasToModbusConversionHelper.convert(new PropertyValue(typedValue), bytesRead.length);
        assertArrayEquals(bytesRead, removePadding(bytesConverted));
    }
}
