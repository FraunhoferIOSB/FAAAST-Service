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
package de.fraunhofer.iosb.ilt.faaast.service;

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.BlobValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementCollectionValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.FileValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.RelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.DoubleValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.StringValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueMapper;
import io.adminshell.aas.v3.model.EntityType;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultAnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.impl.DefaultBlob;
import io.adminshell.aas.v3.model.impl.DefaultEntity;
import io.adminshell.aas.v3.model.impl.DefaultFile;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultMultiLanguageProperty;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultRange;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import io.adminshell.aas.v3.model.impl.DefaultReferenceElement;
import io.adminshell.aas.v3.model.impl.DefaultRelationshipElement;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;


public class ElementValueMapperTest {

    @Test
    public void testAnnotatedRelationshipElementSetValueMapping() throws ValueFormatException {
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
                        .valueType(Datatype.String.getName())
                        .value("foo")
                        .build())
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testAnnotatedRelationshipElementToValueMapping() throws ValueFormatException {
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
                        .valueType(Datatype.String.getName())
                        .value("foo")
                        .build())
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
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
                .annotation("property", PropertyValue.of(Datatype.String, "foo"))
                .build();
    }


    @Test
    public void testBlobSetValueMapping() {
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
    public void testBlobToValueMapping() {
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
    public void testEntitySetValueMapping() throws ValueFormatException {
        SubmodelElement actual = new DefaultEntity.Builder()
                .statement(new DefaultProperty.Builder()
                        .idShort("property")
                        .build())
                .build();
        EntityValue value = EntityValue.builder()
                .statement("property", PropertyValue.of(Datatype.String, "foo"))
                .entityType(EntityType.SELF_MANAGED_ENTITY)
                .globalAssetId(List.of(new DefaultKey.Builder()
                        .idType(KeyType.IRI)
                        .type(KeyElements.SUBMODEL)
                        .value("http://example.org/submodel/1")
                        .build(),
                        new DefaultKey.Builder()
                                .idType(KeyType.ID_SHORT)
                                .type(KeyElements.PROPERTY)
                                .value("property1")
                                .build()))
                .build();
        SubmodelElement expected = new DefaultEntity.Builder()
                .statement(new DefaultProperty.Builder()
                        .idShort(value.getStatements().keySet().iterator().next())
                        .valueType(Datatype.String.getName())
                        .value("foo")
                        .build())
                .entityType(value.getEntityType())
                .globalAssetId(new DefaultReference.Builder()
                        .keys(value.getGlobalAssetId())
                        .build())
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testEntityToValueMapping() throws ValueFormatException {
        EntityValue expected = EntityValue.builder()
                .statement("property", PropertyValue.of(Datatype.String, "foo"))
                .entityType(EntityType.SELF_MANAGED_ENTITY)
                .globalAssetId(List.of(new DefaultKey.Builder()
                        .idType(KeyType.IRI)
                        .type(KeyElements.SUBMODEL)
                        .value("http://example.org/submodel/1")
                        .build(),
                        new DefaultKey.Builder()
                                .idType(KeyType.ID_SHORT)
                                .type(KeyElements.PROPERTY)
                                .value("property1")
                                .build()))
                .build();
        SubmodelElement input = new DefaultEntity.Builder()
                .statement(new DefaultProperty.Builder()
                        .category("Test")
                        .idShort(expected.getStatements().keySet().iterator().next())
                        .valueType(Datatype.String.getName())
                        .value("foo")
                        .build())
                .entityType(expected.getEntityType())
                .globalAssetId(new DefaultReference.Builder()
                        .keys(expected.getGlobalAssetId())
                        .build())
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testFileSetValueMapping() {
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
    public void testFileToValueMapping() {
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
    public void testMultiLanguagePropertyToValueMapping() {
        MultiLanguagePropertyValue expected = MultiLanguagePropertyValue.builder()
                .value("deutsch", "de")
                .value("english", "en")
                .build();
        SubmodelElement input = new DefaultMultiLanguageProperty.Builder()
                .values(List.copyOf(expected.getLangStringSet()))
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testMultilanguagePropertySetValueMapping() {
        SubmodelElement actual = new DefaultMultiLanguageProperty.Builder()
                .build();
        MultiLanguagePropertyValue value = MultiLanguagePropertyValue.builder()
                .value("deutsch", "de")
                .value("english", "en")
                .build();
        SubmodelElement expected = new DefaultMultiLanguageProperty.Builder()
                .values(List.copyOf(value.getLangStringSet()))
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPropertySetValueMapping() {
        PropertyValue value = new PropertyValue(new StringValue("foo"));
        SubmodelElement expected = new DefaultProperty.Builder()
                .valueType(value.getValue().getDataType().getName())
                .value(value.getValue().asString())
                .build();
        SubmodelElement actual = new DefaultProperty.Builder()
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPropertyToValueMapping() throws ValueFormatException {
        PropertyValue expected = PropertyValue.of(Datatype.String, "foo");
        SubmodelElement input = new DefaultProperty.Builder()
                .category("Test")
                .idShort("TestProperty")
                .valueType(expected.getValue().getDataType().getName())
                .value(expected.getValue().asString())
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testRangeSetValueMapping() {
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
    public void testRangeToValueMapping() {
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


    @Test
    public void testReferenceElementSetValueMapping() {
        SubmodelElement actual = new DefaultReferenceElement.Builder()
                .value(new DefaultReference.Builder()
                        .build())
                .build();
        ReferenceElementValue value = ReferenceElementValue.builder()
                .key(KeyType.IRI, KeyElements.SUBMODEL, "http://example.org/submodel/1")
                .key(KeyType.ID_SHORT, KeyElements.PROPERTY, "property1")
                .build();
        SubmodelElement expected = new DefaultReferenceElement.Builder()
                .value(new DefaultReference.Builder()
                        .keys(value.getKeys())
                        .build())
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testReferenceElementToValueMapping() {
        ReferenceElementValue expected = ReferenceElementValue.builder()
                .key(KeyType.IRI, KeyElements.SUBMODEL, "http://example.org/submodel/1")
                .key(KeyType.ID_SHORT, KeyElements.PROPERTY, "property1")
                .build();
        SubmodelElement input = new DefaultReferenceElement.Builder()
                .value(new DefaultReference.Builder()
                        .keys(expected.getKeys())
                        .build())
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testRelationshipElementSetValueMapping() {
        SubmodelElement actual = new DefaultRelationshipElement.Builder()
                .build();
        RelationshipElementValue value = RelationshipElementValue.builder()
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
                .build();
        SubmodelElement expected = new DefaultRelationshipElement.Builder()
                .first(new DefaultReference.Builder()
                        .keys(value.getFirst())
                        .build())
                .second(new DefaultReference.Builder()
                        .keys(value.getSecond())
                        .build())
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testRelationshipElementToValueMapping() {
        RelationshipElementValue expected = RelationshipElementValue.builder()
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
                .build();
        SubmodelElement input = new DefaultRelationshipElement.Builder()
                .first(new DefaultReference.Builder()
                        .keys(expected.getFirst())
                        .build())
                .second(new DefaultReference.Builder()
                        .keys(expected.getSecond())
                        .build())
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSubmodelElementCollectionToValueMapping() {
        PropertyValue propertyValue = PropertyValue.builder().value(new StringValue("testValue")).build();
        PropertyValue propertyValue2 = PropertyValue.builder().value(new StringValue("testValue2")).build();
        ElementCollectionValue expected = ElementCollectionValue.builder()
                .value("prop1", propertyValue)
                .value("prop2", propertyValue2)
                .build();

        SubmodelElement input = new DefaultSubmodelElementCollection.Builder()
                .value(new DefaultProperty.Builder()
                        .idShort("prop1")
                        .value("testValue")
                        .build())
                .value(new DefaultProperty.Builder()
                        .idShort("prop2")
                        .value("testValue2")
                        .build())
                .build();

        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSubmodelElementCollectionSetValueMapping() {
        SubmodelElement actual = new DefaultSubmodelElementCollection.Builder()
                .value(new DefaultProperty.Builder()
                        .idShort("prop1")
                        .build())
                .value(new DefaultProperty.Builder()
                        .idShort("prop2")
                        .build())
                .build();

        PropertyValue propertyValue = PropertyValue.builder().value(new StringValue("testValue")).build();
        PropertyValue propertyValue2 = PropertyValue.builder().value(new StringValue("testValue2")).build();
        ElementCollectionValue value = ElementCollectionValue.builder()
                .value("prop1", propertyValue)
                .value("prop2", propertyValue2)
                .build();

        SubmodelElement expected = new DefaultSubmodelElementCollection.Builder()
                .value(new DefaultProperty.Builder()
                        .idShort("prop1")
                        .value("testValue")
                        .valueType("string")
                        .build())
                .value(new DefaultProperty.Builder()
                        .idShort("prop2")
                        .value("testValue2")
                        .valueType("string")
                        .build())
                .build();

        actual = ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }
}
