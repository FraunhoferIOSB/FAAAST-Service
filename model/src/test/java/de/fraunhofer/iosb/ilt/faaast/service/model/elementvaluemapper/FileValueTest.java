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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.FileValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultFile;
import org.junit.Assert;
import org.junit.Test;


public class FileValueTest {

    @Test
    public void testSetValueMapping() {
        SubmodelElement actual = new DefaultFile.Builder()
                .build();
        FileValue value = FileValue.builder()
                .mimeType("application/json")
                .value("{}")
                .build();
        SubmodelElement expected = new DefaultFile.Builder()
                .mimeType(value.getMimeType())
                .value(value.getValue())
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSetValueMappingWithNull() {
        SubmodelElement actual = new DefaultFile.Builder()
                .build();
        FileValue value = FileValue.builder()
                .mimeType(null)
                .value(null)
                .build();
        SubmodelElement expected = new DefaultFile.Builder()
                .mimeType(null)
                .value(null)
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testToValueMapping() throws ValueMappingException {
        FileValue expected = FileValue.builder()
                .mimeType("application/json")
                .value("{}")
                .build();
        SubmodelElement input = new DefaultFile.Builder()
                .mimeType(expected.getMimeType())
                .value(expected.getValue())
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testToValueMappingWithNull() throws ValueMappingException {
        FileValue expected = FileValue.builder()
                .mimeType(null)
                .value(null)
                .build();
        SubmodelElement input = new DefaultFile.Builder()
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }

}
