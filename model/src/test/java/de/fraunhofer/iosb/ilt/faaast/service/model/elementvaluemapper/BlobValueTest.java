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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.BlobValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultBlob;
import org.junit.Assert;
import org.junit.Test;


public class BlobValueTest {

    @Test
    public void testSetValueMapping() {
        SubmodelElement actual = new DefaultBlob.Builder()
                .build();
        BlobValue value = BlobValue.builder()
                .mimeType("application/json")
                .value("foo")
                .build();
        SubmodelElement expected = new DefaultBlob.Builder()
                .mimeType(value.getMimeType())
                .value(value.getValue())
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSetValueMappingWithNull() {
        SubmodelElement actual = new DefaultBlob.Builder()
                .mimeType(null)
                .value(null)
                .build();
        BlobValue value = BlobValue.builder()
                .mimeType(null)
                .value("foo")
                .build();
        SubmodelElement expected = new DefaultBlob.Builder()
                .mimeType(null)
                .value(value.getValue())
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testToValueMapping() throws ValueMappingException {
        BlobValue expected = BlobValue.builder()
                .mimeType("application/json")
                .value("foo")
                .build();
        SubmodelElement input = new DefaultBlob.Builder()
                .mimeType(expected.getMimeType())
                .value(expected.getValue())
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testToValueMappingWithNull() throws ValueMappingException {
        BlobValue expected = BlobValue.builder()
                .mimeType(null)
                .value((String) null)
                .build();
        SubmodelElement input = new DefaultBlob.Builder()
                .mimeType(null)
                .value(null)
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }

}
