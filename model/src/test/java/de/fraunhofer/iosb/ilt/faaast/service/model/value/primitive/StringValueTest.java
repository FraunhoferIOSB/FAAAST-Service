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
package de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValueFactory;
import org.junit.Assert;
import org.junit.Test;


public class StringValueTest {

    @Test
    public void testEnglish() throws ValueFormatException {
        String expected = "Hello world";
        TypedValue actual = TypedValueFactory.create(Datatype.STRING, expected);
        Assert.assertEquals(expected, actual.getValue());
        Assert.assertEquals(expected, actual.asString());
    }


    @Test
    public void testGreek() throws ValueFormatException {
        String expected = "Καλημέρα κόσμε";
        TypedValue actual = TypedValueFactory.create(Datatype.STRING, expected);
        Assert.assertEquals(expected, actual.getValue());
        Assert.assertEquals(expected, actual.asString());
    }


    @Test
    public void testJapanese() throws ValueFormatException {
        String expected = "こんにちは世界";
        TypedValue actual = TypedValueFactory.create(Datatype.STRING, expected);
        Assert.assertEquals(expected, actual.getValue());
        Assert.assertEquals(expected, actual.asString());
    }

}
