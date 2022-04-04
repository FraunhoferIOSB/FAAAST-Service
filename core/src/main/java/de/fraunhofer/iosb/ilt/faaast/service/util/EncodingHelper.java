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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


/**
 * Helper class for base64 and base64URL encoding and decoding
 */
public class EncodingHelper {

    private EncodingHelper() {

    }


    /**
     * Encode url decoded string with charset UTF-8
     *
     * @param value string to encode
     * @return encoded string
     */
    public static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }


    /**
     * Url decode string with charset UTF-8
     *
     * @param value url encoded string
     * @return decoded string
     */
    public static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }


    /**
     * Base64 encode string
     *
     * @param value string to encode
     * @return encoded string
     */
    public static String base64Encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }


    /**
     * Decode a base64 encoded string
     *
     * @param value base64 encoded string
     * @return decoded string
     */
    public static String base64Decode(String value) {
        return new String(Base64.getDecoder().decode(value));

    }


    /**
     * Base64URL encode string
     *
     * @param value string to encode
     * @return encoded string
     */
    public static String base64UrlEncode(String value) {
        return Base64.getUrlEncoder().encodeToString(value.getBytes());
    }


    /**
     * Decode a base64Url encoded string
     *
     * @param value base64Url encoded string
     * @return decoded string
     */
    public static String base64UrlDecode(String value) {
        return new String(Base64.getUrlDecoder().decode(value));
    }
}
