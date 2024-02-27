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


public class DoubleValueTest {

    @Test
    public void testNegative() throws ValueFormatException {
        String value = "-1.0";
        double expected = -1.0d;
        TypedValue actual = TypedValueFactory.create(Datatype.DOUBLE, value);
        Assert.assertEquals(expected, actual.getValue());
        Assert.assertEquals(value, actual.asString());
    }


    @Test
    public void testExplicitPlusPrefix() throws ValueFormatException {
        String value = "+100000";
        double expected = 100000d;
        TypedValue actual = TypedValueFactory.create(Datatype.DOUBLE, value);
        Assert.assertEquals(expected, actual.getValue());
        Assert.assertEquals("100000.0", actual.asString());
    }


    @Test
    public void testPositiveZero() throws ValueFormatException {
        String value = "+0.0";
        double expected = 0.0d;
        TypedValue actual = TypedValueFactory.create(Datatype.DOUBLE, value);
        Assert.assertEquals(expected, actual.getValue());
        Assert.assertEquals("0.0", actual.asString());
    }


    @Test
    public void testNegativeZero() throws ValueFormatException {
        String value = "-0.0";
        double expected = -0.0d;
        TypedValue actual = TypedValueFactory.create(Datatype.DOUBLE, value);
        Assert.assertEquals(expected, actual.getValue());
        Assert.assertEquals("-0.0", actual.asString());
    }


    @Test
    public void testE() throws ValueFormatException {
        String value = "234.567e8";
        double expected = 234.567e8;
        TypedValue actual = TypedValueFactory.create(Datatype.DOUBLE, value);
        Assert.assertEquals(expected, actual.getValue());
    }


    @Test
    public void testNegativeInf() throws ValueFormatException {
        String value = "-INF";
        double expected = Double.NEGATIVE_INFINITY;
        TypedValue actual = TypedValueFactory.create(Datatype.DOUBLE, value);
        Assert.assertEquals(expected, actual.getValue());
        Assert.assertEquals(value, actual.asString());
    }


    @Test
    public void testNaN() throws ValueFormatException {
        String value = "NaN";
        double expected = Double.NaN;
        TypedValue actual = TypedValueFactory.create(Datatype.DOUBLE, value);
        Assert.assertEquals(expected, actual.getValue());
        Assert.assertEquals(value, actual.asString());
    }

}
