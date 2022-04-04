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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;


/**
 * Helper class for handling values of
 * {@link io.adminshell.aas.v3.model.Identifier}
 */
public class IdentifierHelper {

    private static final Pattern IRDI_PATTERN = Pattern.compile("^\\d{4}-.{1,4}(-.{6}-.-.{6})?#.{2}-.{1,6}#\\d+$");
    private static final Pattern FRAGMENT_PATTERN = Pattern.compile("^#(.+)$");
    private static final Pattern ID_SHORT_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]+");

    private IdentifierHelper() {

    }


    /**
     * check if url is a valid URL
     * {@link io.adminshell.aas.v3.model.Identifier}. Supported IdentifierTypes:
     * IRI
     *
     * @param value of the identifier
     * @return true/false
     */
    public static boolean isValidURI(String value) {
        try {
            URL obj = new URL(value);
            obj.toURI();
            return true;
        }
        catch (MalformedURLException | URISyntaxException exception) {
            return false;
        }
    }


    /**
     * Guess the identifier type out of a value of an
     * {@link io.adminshell.aas.v3.model.Identifier}. Supported IdentifierTypes:
     * IRDI, IRI, CUSTOM
     *
     * @param value of the identifier
     * @return the guessed identifier type
     */
    public static IdentifierType guessIdentifierType(String value) {
        if (IRDI_PATTERN.matcher(value).matches()) {
            return IdentifierType.IRDI;
        }
        if (isValidURI(value)) {
            return IdentifierType.IRI;
        }
        return IdentifierType.CUSTOM;
    }


    /**
     * Guess the key type out of a value of an
     * {@link io.adminshell.aas.v3.model.Identifier}. Supported key types: IRDI,
     * IRI, FRAGMENT_ID, ID_SHORT, CUSTOM
     *
     * @param value of the identifier
     * @return the guessed key type
     */
    public static KeyType guessKeyType(String value) {
        if (IRDI_PATTERN.matcher(value).matches()) {
            return KeyType.IRDI;
        }
        if (isValidURI(value)) {
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


    /**
     * Create a {@link io.adminshell.aas.v3.model.Identifier} out of an
     * identifier value
     *
     * @param value of the identifier
     * @return the parsed identifier with a guessed id type
     */
    public static Identifier parseIdentifier(String value) {
        return new DefaultIdentifier.Builder()
                .identifier(value)
                .idType(IdentifierHelper.guessIdentifierType(value))
                .build();
    }


    /**
     * Transforms an {@link io.adminshell.aas.v3.model.Identifier} to a string
     * with format [IDType]value. E.g. [IRI]http://example.com/abc123
     *
     * @param id the identifier which should be transformed to string
     * @return the string representation for the identifier
     */
    public static String asString(Identifier id) {
        return String.format("[%s]%s", id.getIdType(), id.getIdentifier());
    }
}
