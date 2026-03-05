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

/**
 * Modbus asset connection helper for byte array operations and checks.
 */
public class ByteArrayHelper {
    private ByteArrayHelper() {}


    /**
     * Pads the array with {@code padding} bytes. Big endian is ensured by padding from the front. Example: padding=4 ->
     * [0x0,0x0,0x0,0x0,*array].
     *
     * @param array array to pad.
     * @param padding how many zero bytes to pad.
     * @return padded array.
     */
    public static byte[] pad(byte[] array, int padding) {
        if (padding < 0) {
            throw new IllegalArgumentException(String.format("Tried to pad %d bytes", padding));
        }
        byte[] padded = new byte[array.length + padding];

        System.arraycopy(array, 0, padded, padded.length - array.length, array.length);

        return padded;
    }


    /**
     * Removes the padding of a byte array such that it does not contain zero-bytes before the actual non-zero data starts.
     *
     * <p>
     * NOTE: Assuming BigEndian. MSB is in the front of the array.
     *
     * @param array array to remove padding from
     * @return unpadded array.
     */
    public static byte[] removePadding(byte[] array) {
        int unpaddedLength = array.length;
        for (byte b: array) {
            if (b != 0x0) {
                break;
            }
            unpaddedLength--;

        }
        // Don't allow empty arrays. Zero is a number too
        if (unpaddedLength == 0) {
            unpaddedLength++;
        }

        byte[] notPadded = new byte[unpaddedLength];

        System.arraycopy(array, array.length - unpaddedLength, notPadded, 0, unpaddedLength);

        return notPadded;
    }


    /**
     * Returns the most significant bit index in an array of bytes, starting at index 0.
     *
     * @param bytes The byte array.
     * @return The most significant bit index or 0 if bytes is null/empty.
     */
    public static int mostSignificantBitIndex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return -1;
        }

        // Find first non-zero byte from the left (most significant side)
        int byteIndex = 0;
        while (byteIndex < bytes.length && bytes[byteIndex] == 0) {
            byteIndex++;
        }

        // All bytes are zero -> no set bit
        if (byteIndex == bytes.length) {
            return -1;
        }

        int b = bytes[byteIndex] & 0xFF; // assume unsigned

        int leadingZerosInByte = 0;
        while ((b & 0x80) == 0) {
            b <<= 1;
            leadingZerosInByte++;
        }

        int totalBits = bytes.length * 8;
        int totalLeadingZeroBits = byteIndex * 8 + leadingZerosInByte;

        return totalBits - totalLeadingZeroBits - 1;
    }
}
