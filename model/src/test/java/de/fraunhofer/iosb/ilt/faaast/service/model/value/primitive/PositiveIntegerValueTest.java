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
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Test;


public class PositiveIntegerValueTest {

    @Test
    public void testLargeNumber() throws ValueFormatException {
        String value = "8889496729588";
        BigInteger expected = new BigInteger(value);
        TypedValue actual = TypedValueFactory.create(Datatype.POSITIVE_INTEGER, value);
        Assert.assertEquals(expected, actual.getValue());
        Assert.assertEquals(value, actual.asString());
    }


    @Test
    public void testExplicitPlusPrefix() throws ValueFormatException {
        String value = "+10000";
        BigInteger expected = new BigInteger(value);
        TypedValue actual = TypedValueFactory.create(Datatype.POSITIVE_INTEGER, value);
        Assert.assertEquals(expected, actual.getValue());
        Assert.assertEquals("10000", actual.asString());
    }


    @Test(expected = ValueFormatException.class)
    public void testZero() throws ValueFormatException {
        String value = "0";
        TypedValueFactory.create(Datatype.POSITIVE_INTEGER, value);
    }


    @Test(expected = ValueFormatException.class)
    public void testNegative() throws ValueFormatException {
        String value = "-1";
        TypedValueFactory.create(Datatype.POSITIVE_INTEGER, value);
    }

}
