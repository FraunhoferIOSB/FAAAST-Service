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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import io.adminshell.aas.v3.model.impl.DefaultReferenceElement;
import org.junit.Assert;
import org.junit.Test;


public class ReferenceElementValueTest {

    @Test
    public void testSetValueMapping() {
        SubmodelElement actual = new DefaultReferenceElement.Builder()
                .value(new DefaultReference.Builder()
                        .build())
                .build();
        ReferenceElementValue value = createReferenceElementValue();
        SubmodelElement expected = new DefaultReferenceElement.Builder()
                .value(new DefaultReference.Builder()
                        .keys(value.getKeys())
                        .build())
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSetValueMappingWithNull() {
        SubmodelElement actual = new DefaultReferenceElement.Builder()
                .value(null)
                .build();
        ReferenceElementValue value = ReferenceElementValue.builder()
                .keys(null)
                .build();
        SubmodelElement expected = new DefaultReferenceElement.Builder()
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testToValueMapping() throws ValueMappingException {
        ReferenceElementValue expected = createReferenceElementValue();
        SubmodelElement input = new DefaultReferenceElement.Builder()
                .value(new DefaultReference.Builder()
                        .keys(expected.getKeys())
                        .build())
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testToValueMappingWithNull() throws ValueMappingException {
        ReferenceElementValue expected = ReferenceElementValue.builder()
                .keys(null)
                .build();
        SubmodelElement input = new DefaultReferenceElement.Builder()
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }


    private ReferenceElementValue createReferenceElementValue() {
        return ReferenceElementValue.builder()
                .key(KeyType.IRI, KeyElements.SUBMODEL, "http://example.org/submodel/1")
                .key(KeyType.ID_SHORT, KeyElements.PROPERTY, "property1")
                .build();
    }

}
