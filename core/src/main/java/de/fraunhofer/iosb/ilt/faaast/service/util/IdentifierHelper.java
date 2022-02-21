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

import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import java.util.regex.Pattern;


public class IdentifierHelper {
    private static final Pattern IRI_PATTERN = Pattern.compile(
            "^(?:(?:https?|ftp):\\/\\/)(?:\\S+(?::\\S*)?@)?(?:(?!10(?:\\.\\d{1,3}){3})(?!127(?:\\.\\d{1,3}){3})(?!169\\.254(?:\\.\\d{1,3}){2})(?!192\\.168(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\x{00a1}-\\x{ffff}0-9]+-?)*[a-z\\x{00a1}-\\x{ffff}0-9]+)(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}0-9]+-?)*[a-z\\x{00a1}-\\x{ffff}0-9]+)*(?:\\.(?:[a-z\\x{00a1}-\\x{ffff}]{2,})))(?::\\d{2,5})?(?:\\/[^\\s]*)?$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern IRDI_PATTERN = Pattern.compile("^\\d{4}-.{1,4}(-.{6}-.-.{6})?#.{2}-.{1,6}#\\d+$");
    private static final Pattern FRAGMENT_PATTERN = Pattern.compile("^#(.+)$");
    private static final Pattern ID_SHORT_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]+");

    private IdentifierHelper() {

    }


    public static IdentifierType guessIdentifierType(String value) {
        if (IRDI_PATTERN.matcher(value).matches()) {
            return IdentifierType.IRDI;
        }
        if (IRI_PATTERN.matcher(value).matches()) {
            return IdentifierType.IRI;
        }
        return IdentifierType.CUSTOM;
    }


    public static KeyType guessKeyType(String value) {
        if (IRDI_PATTERN.matcher(value).matches()) {
            return KeyType.IRDI;
        }
        if (IRI_PATTERN.matcher(value).matches()) {
            return KeyType.IRI;
        }
        if (FRAGMENT_PATTERN.matcher(value).matches()) {
            return KeyType.FRAGMENT_ID;
        }
        if (ID_SHORT_PATTERN.matcher(value).matches()) {
            return KeyType.ID_SHORT;
        }
        return KeyType.CUSTOM;
    }


    public static Identifier parseIdentifier(String value) {
        return new DefaultIdentifier.Builder()
                .identifier(value)
                .idType(IdentifierHelper.guessIdentifierType(value))
                .build();
    }
}
