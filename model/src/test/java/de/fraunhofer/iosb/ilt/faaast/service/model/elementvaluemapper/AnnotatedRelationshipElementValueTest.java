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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultAnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;


public class AnnotatedRelationshipElementValueTest {

    @Test
    public void testSetValueMapping() throws ValueFormatException {
        SubmodelElement actual = new DefaultAnnotatedRelationshipElement.Builder()
                .annotation(new DefaultProperty.Builder()
                        .idShort("property")
                        .build())
                .build();
        AnnotatedRelationshipElementValue value = createAnnotatedRelationshipElementValue();
        SubmodelElement expected = new DefaultAnnotatedRelationshipElement.Builder()
                .first(new DefaultReference.Builder()
                        .keys(value.getFirst())
                        .build())
                .second(new DefaultReference.Builder()
                        .keys(value.getSecond())
                        .build())
                .annotation(new DefaultProperty.Builder()
                        .idShort(value.getAnnotations().keySet().iterator().next())
                        .valueType(Datatype.STRING.getName())
                        .value("foo")
                        .build())
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSetValueMappingWithNull() {
        SubmodelElement actual = new DefaultAnnotatedRelationshipElement.Builder()
                .annotation(null)
                .first(null)
                .second(null)
                .build();
        AnnotatedRelationshipElementValue value = new AnnotatedRelationshipElementValue.Builder()
                .first(List.of(
                        new DefaultKey.Builder()
                                .idType(KeyType.IRI)
                                .type(KeyElements.SUBMODEL)
                                .value("http://example.org/submodel/1")
                                .build()))
                .second(null)
                .build();
        SubmodelElement expected = new DefaultAnnotatedRelationshipElement.Builder()
                .first(new DefaultReference.Builder()
                        .keys(value.getFirst())
                        .build())
                .second(null)
                .annotation(null)
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testToValueMapping() throws ValueFormatException, ValueMappingException {
        AnnotatedRelationshipElementValue expected = createAnnotatedRelationshipElementValue();
        SubmodelElement input = new DefaultAnnotatedRelationshipElement.Builder()
                .first(new DefaultReference.Builder()
                        .keys(expected.getFirst())
                        .build())
                .second(new DefaultReference.Builder()
                        .keys(expected.getSecond())
                        .build())
                .annotation(new DefaultProperty.Builder()
                        .idShort(expected.getAnnotations().keySet().iterator().next())
                        .valueType(Datatype.STRING.getName())
                        .value("foo")
                        .build())
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testToValueMappingWithNull() throws ValueMappingException {
        SubmodelElement element = new DefaultAnnotatedRelationshipElement.Builder()
                .annotation(null)
                .first(null)
                .second(null)
                .build();
        AnnotatedRelationshipElementValue expected = new AnnotatedRelationshipElementValue.Builder()
                .first(null)
                .second(null)
                .build();
        AnnotatedRelationshipElementValue value = ElementValueMapper.toValue(element);
        Assert.assertEquals(expected, value);
    }


    private AnnotatedRelationshipElementValue createAnnotatedRelationshipElementValue() throws ValueFormatException {
        return new AnnotatedRelationshipElementValue.Builder()
                .first(List.of(
                        new DefaultKey.Builder()
                                .idType(KeyType.IRI)
                                .type(KeyElements.SUBMODEL)
                                .value("http://example.org/submodel/1")
                                .build(),
                        new DefaultKey.Builder()
                                .idType(KeyType.ID_SHORT)
                                .type(KeyElements.PROPERTY)
                                .value("property1")
                                .build()))
                .second(List.of(
                        new DefaultKey.Builder()
                                .idType(KeyType.IRI)
                                .type(KeyElements.SUBMODEL)
                                .value("http://example.org/submodel/2")
                                .build(),
                        new DefaultKey.Builder()
                                .idType(KeyType.ID_SHORT)
                                .type(KeyElements.PROPERTY)
                                .value("property2")
                                .build()))
                .annotation("property", PropertyValue.of(Datatype.STRING, "foo"))
                .build();
    }

}
