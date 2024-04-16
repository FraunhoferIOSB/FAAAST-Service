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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.RelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultRelationshipElement;
import org.junit.Assert;
import org.junit.Test;


public class RelationshipElementValueTest {

    @Test
    public void testSetValueMapping() throws ValueMappingException {
        SubmodelElement actual = new DefaultRelationshipElement.Builder()
                .build();
        RelationshipElementValue value = createRelationshipElementValue();
        SubmodelElement expected = new DefaultRelationshipElement.Builder()
                .first(value.getFirst())
                .second(value.getSecond())
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSetValueMappingWithNull() throws ValueMappingException {
        SubmodelElement actual = new DefaultRelationshipElement.Builder()
                .build();
        RelationshipElementValue value = RelationshipElementValue.builder()
                .first(null)
                .second(null)
                .build();
        SubmodelElement expected = new DefaultRelationshipElement.Builder()
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testToValueMapping() throws ValueMappingException {
        RelationshipElementValue expected = createRelationshipElementValue();
        SubmodelElement input = new DefaultRelationshipElement.Builder()
                .first(expected.getFirst())
                .second(expected.getSecond())
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testToValueMappingWithNull() throws ValueMappingException {
        RelationshipElementValue expected = RelationshipElementValue.builder()
                .first(null)
                .second(null)
                .build();
        SubmodelElement input = new DefaultRelationshipElement.Builder()
                .first(null)
                .second(null)
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }


    private RelationshipElementValue createRelationshipElementValue() {
        return RelationshipElementValue.builder()
                .first(new DefaultReference.Builder()
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
                .second(new DefaultReference.Builder()
                        .type(ReferenceTypes.MODEL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.SUBMODEL)
                                .value("http://example.org/submodel/2")
                                .build())
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.PROPERTY)
                                .value("property2")
                                .build())
                        .build())
                .build();
    }

}
