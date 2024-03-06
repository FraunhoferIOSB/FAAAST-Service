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
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReferenceElement;
import org.junit.Assert;
import org.junit.Test;


public class ReferenceElementValueTest {

    @Test
    public void testSetValueMapping() throws ValueMappingException {
        SubmodelElement actual = new DefaultReferenceElement.Builder()
                .value(new DefaultReference.Builder()
                        .build())
                .build();
        ReferenceElementValue value = createReferenceElementValue();
        SubmodelElement expected = new DefaultReferenceElement.Builder()
                .value(value.getValue())
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSetValueMappingWithNull() throws ValueMappingException {
        SubmodelElement actual = new DefaultReferenceElement.Builder()
                .value(null)
                .build();
        ReferenceElementValue value = ReferenceElementValue.builder()
                .value(null)
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
                .value(expected.getValue())
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testToValueMappingWithNull() throws ValueMappingException {
        ReferenceElementValue expected = ReferenceElementValue.builder()
                .value(null)
                .build();
        SubmodelElement input = new DefaultReferenceElement.Builder()
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }


    private ReferenceElementValue createReferenceElementValue() {
        return ReferenceElementValue.builder()
                .value(new DefaultReference.Builder()
                        .type(ReferenceTypes.MODEL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.SUBMODEL)
                                .value("http://example.org/submodel/1")
                                .build())
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.PROPERTY)
                                .value("property1")
                                .build())
                        .build())
                .build();
    }

}
