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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Helper class for hashing values.
 */
public class HashHelper {

    private static final String SHA_256 = "SHA-256";

    private HashHelper() {

    }


    /**
     * Hash string using SHA-256 and return result as lowercase hex string.
     *
     * @param value string to hash
     * @return SHA-256 hash as lowercase hex string or null if null was the input.
     */
    public static String sha256(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot compute hash value from null");
        }
        return toHex(sha256(value.getBytes(StandardCharsets.UTF_8)));
    }


    /**
     * Hash bytes using SHA-256.
     *
     * @param value bytes to hash
     * @return SHA-256 hash bytes
     */
    private static byte[] sha256(byte[] value) {
        if (value == null) {
            return new byte[0];
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            return digest.digest(value);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }


    /**
     * Convert bytes to lowercase hex string.
     *
     * @param value bytes to convert
     * @return lowercase hex string
     */
    private static String toHex(byte[] value) {
        StringBuilder result = new StringBuilder(value.length * 2);
        for (byte b: value) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
