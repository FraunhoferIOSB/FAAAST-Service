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
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import org.junit.Assert;
import org.junit.Test;


public class DurationValueTest {

    @Test
    public void testDaysOnly() throws ValueFormatException {
        String value = "P30D";
        Duration expected = DatatypeFactory.newDefaultInstance().newDuration(true, 0, 0, 30, 0, 0, 0);
        TypedValue actual = TypedValueFactory.create(Datatype.DURATION, value);
        Assert.assertEquals(expected, actual.getValue());
        Assert.assertEquals(value, actual.asString());
    }


    @Test
    public void testNegative() throws ValueFormatException {
        String value = "-P1Y2M3DT1H";
        Duration expected = DatatypeFactory.newDefaultInstance().newDuration(false, 1, 2, 3, 1, 0, 0);
        TypedValue actual = TypedValueFactory.create(Datatype.DURATION, value);
        Assert.assertEquals(expected, actual.getValue());
        Assert.assertEquals(value, actual.asString());
    }


    @Test
    public void testTimeOnly() throws ValueFormatException, DatatypeConfigurationException {
        String value = "PT1H5M0S";
        Duration expected = DatatypeFactory.newDefaultInstance().newDuration(true, 0, 0, 0, 1, 5, 0);
        TypedValue actual = TypedValueFactory.create(Datatype.DURATION, value);
        Assert.assertEquals(expected, actual.getValue());
        Assert.assertEquals(value, actual.asString());
    }

}
