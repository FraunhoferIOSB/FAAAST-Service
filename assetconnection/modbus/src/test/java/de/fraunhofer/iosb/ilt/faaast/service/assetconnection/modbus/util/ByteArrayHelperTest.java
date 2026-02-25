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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;


public class ByteArrayHelperTest {
    @Test
    public void testPadZeroPaddingNoEffect() {
        byte[] unpadded = new byte[] {
                0x23,
                (byte) 0x84,
                (byte) 0xAB
        };
        assertArrayEquals(unpadded, ByteArrayHelper.pad(unpadded, 0));
    }


    @Test
    public void testPadNegativePaddingFails() {
        byte[] unpadded = new byte[] {
                0x23,
                (byte) 0x84,
                (byte) 0xAB
        };

        try {
            ByteArrayHelper.pad(unpadded, -1);
            fail();
        }
        catch (IllegalArgumentException expected) {
            // expecting an exception for a negative padding
        }
    }


    @Test
    public void testPaddingUnpaddingSucceeds() {
        byte[] unpadded = new byte[] {
                0x23,
                (byte) 0x84,
                (byte) 0xAB
        };

        for (int i = 1; i < 65536; i++) {
            byte[] padded = ByteArrayHelper.pad(unpadded, i);
            assertEquals(i + unpadded.length, padded.length);
            for (int j = 0; j < i; j++) {
                assertEquals(0x00, padded[j]);
            }

            byte[] unpaddedAgain = ByteArrayHelper.removePadding(padded);
            assertArrayEquals(unpadded, unpaddedAgain);
        }
    }


    @Test
    public void testMostSignificantBitIndex() {
        byte[] unpadded = new byte[] {
                0x00,
                0x00,
                0x00,
                0x01,
                0x23,
                (byte) 0x84,
                (byte) 0xAB
        };
        assertEquals(8 * 3, ByteArrayHelper.mostSignificantBitIndex(unpadded));
    }
}
