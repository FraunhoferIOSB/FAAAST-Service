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
package de.fraunhofer.iosb.ilt.faaast.service.model.elementvaluemapper;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DoubleValue;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultRange;
import org.junit.Assert;
import org.junit.Test;


public class RangeValueTest {
    @Test
    public void testSetValueMapping() {
        SubmodelElement actual = new DefaultRange.Builder()
                .build();
        RangeValue value = RangeValue.builder()
                .min(new DoubleValue(2.3))
                .max(new DoubleValue(5.1))
                .build();
        SubmodelElement expected = new DefaultRange.Builder()
                .valueType(value.getMin().getDataType().getName())
                .min(value.getMin().asString())
                .max(value.getMax().asString())
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSetValueMappingWithNull() throws ValueMappingException {
        SubmodelElement actual = new DefaultRange.Builder()
                .valueType("int")
                .max(null)
                .min("2")
                .build();

        RangeValue rangeValue = ElementValueMapper.toValue(actual);

        SubmodelElement expected = new DefaultRange.Builder()
                .valueType("int")
                .min("2")
                .max(null)
                .build();
        ElementValueMapper.setValue(actual, rangeValue);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testToValueMapping() throws ValueMappingException {
        RangeValue expected = RangeValue.builder()
                .min(new DoubleValue(2.3))
                .max(new DoubleValue(5.1))
                .build();
        SubmodelElement input = new DefaultRange.Builder()
                .valueType(expected.getMin().getDataType().getName())
                .min(expected.getMin().asString())
                .max(expected.getMax().asString())
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }

}
