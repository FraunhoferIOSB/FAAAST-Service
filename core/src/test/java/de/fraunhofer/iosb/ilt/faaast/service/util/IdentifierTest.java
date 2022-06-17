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
import org.junit.Assert;
import org.junit.Test;


public class IdentifierTest {

    private void assertIRI(String value) {
        IdentifierType actual = IdentifierHelper.guessIdentifierType(value);
        Assert.assertEquals(IdentifierType.IRI, actual);
    }


    @Test
    public void testHttpIRI() {
        assertIRI("http://test.de");
    }


    @Test
    public void testHttpsIRI() {
        assertIRI("https://test.de");
    }


    @Test
    public void testHttpsUserIRI() {
        assertIRI("https://username:test@test.de");
    }


    @Test
    public void testHttpComplexIRI() {
        assertIRI("http://example.com/demo/aas/1/1/1234859590");
    }


    @Test
    public void testFtpIRI() {
        assertIRI("ftp://test.de");
    }


    @Test
    public void testIRDI() {
        IdentifierType actual = IdentifierHelper.guessIdentifierType("0173-1#02-BAA120#008");
        Assert.assertEquals(IdentifierType.IRDI, actual);
    }


    @Test
    public void testCustom() {
        IdentifierType actual = IdentifierHelper.guessIdentifierType("ExampleId");
        Assert.assertEquals(IdentifierType.CUSTOM, actual);
    }


    @Test
    public void testKeyTypeIRDI() {
        KeyType actual = IdentifierHelper.guessKeyType("0173-1#02-BAA120#008");
        Assert.assertEquals(KeyType.IRDI, actual);
    }


    @Test
    public void testKeyTypeIRI() {
        KeyType actual = IdentifierHelper.guessKeyType("ftp://test.de");
        Assert.assertEquals(KeyType.IRI, actual);
    }


    @Test
    public void testKeyTypeFragment() {
        KeyType actual = IdentifierHelper.guessKeyType("#Whatever-12");
        Assert.assertEquals(KeyType.FRAGMENT_ID, actual);
    }


    @Test
    public void testKeyTypeIDShort() {
        KeyType actual = IdentifierHelper.guessKeyType("Example1_IdShort");
        Assert.assertEquals(KeyType.ID_SHORT, actual);
    }


    @Test
    public void testParseIdentifier() {
        Identifier expected = new DefaultIdentifier.Builder()
                .identifier("http://test.de")
                .idType(IdentifierType.IRI)
                .build();
        Identifier actual = IdentifierHelper.parseIdentifier("http://test.de");
        Assert.assertEquals(expected, actual);
    }

}
