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
package de.fraunhofer.iosb.ilt.faaast.service.typing;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementCollectionValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.EntityType;
import io.adminshell.aas.v3.model.ModelingKind;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.impl.DefaultAnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.impl.DefaultEntity;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultRange;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;


/*
 * Copyright 2022 Fraunhofer IOSB.
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
public class TypeExtractorTest {

    @Test
    public void testSubmodel() {
        TypeInfo expected = ContainerTypeInfo.builder()
                .type(Submodel.class)
                .element("stringProp1", ElementValueTypeInfo.builder()
                        .datatype(Datatype.STRING)
                        .type(PropertyValue.class)
                        .build())
                .element("intProp1", ElementValueTypeInfo.builder()
                        .datatype(Datatype.INT)
                        .type(PropertyValue.class)
                        .build())
                .element("collection1", ElementValueTypeInfo.builder()
                        .type(SubmodelElementCollectionValue.class)
                        .element("doubleRange1", ElementValueTypeInfo.builder()
                                .datatype(Datatype.DOUBLE)
                                .type(RangeValue.class)
                                .build())
                        .element("intProp2", ElementValueTypeInfo.builder()
                                .datatype(Datatype.INT)
                                .type(PropertyValue.class)
                                .build())
                        .build())
                .build();
        Object data = new DefaultSubmodel.Builder()
                .submodelElement(new DefaultProperty.Builder()
                        .idShort("stringProp1")
                        .valueType("string")
                        .build())
                .submodelElement(new DefaultProperty.Builder()
                        .idShort("intProp1")
                        .valueType("int")
                        .build())
                .submodelElement(new DefaultSubmodelElementCollection.Builder()
                        .idShort("collection1")
                        .value(new DefaultRange.Builder()
                                .idShort("doubleRange1")
                                .valueType("double")
                                .min("17.00")
                                .max("42")
                                .build())
                        .value(new DefaultProperty.Builder()
                                .idShort("intProp2")
                                .valueType("int")
                                .build())
                        .build())
                .build();
        TypeInfo actual = TypeExtractor.extractTypeInfo(data);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testAnnotatedRelationshipElement() {
        TypeInfo expected = ElementValueTypeInfo.builder()
                .type(AnnotatedRelationshipElementValue.class)
                .element("stringProp1", ElementValueTypeInfo.builder()
                        .datatype(Datatype.STRING)
                        .type(PropertyValue.class)
                        .build())
                .element("intProp1", ElementValueTypeInfo.builder()
                        .datatype(Datatype.INT)
                        .type(PropertyValue.class)
                        .build())
                .build();
        Object data = new DefaultAnnotatedRelationshipElement.Builder()
                .annotation(new DefaultProperty.Builder()
                        .idShort("stringProp1")
                        .valueType("string")
                        .build())
                .annotation(new DefaultProperty.Builder()
                        .idShort("intProp1")
                        .valueType("int")
                        .build())
                .idShort("annotatedRelationship1")
                .build();
        TypeInfo actual = TypeExtractor.extractTypeInfo(data);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPropertyString() {
        TypeInfo expected = ElementValueTypeInfo.builder()
                .datatype(Datatype.STRING)
                .type(PropertyValue.class)
                .build();
        Object data = new DefaultProperty.Builder()
                .idShort("stringProp1")
                .valueType("string")
                .build();
        TypeInfo actual = TypeExtractor.extractTypeInfo(data);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testListProperty() {
        TypeInfo expected = ContainerTypeInfo.builder()
                .type(Collection.class)
                .contentType(Object.class)
                .element(0, ElementValueTypeInfo.builder()
                        .datatype(Datatype.STRING)
                        .type(PropertyValue.class)
                        .build())
                .element(1, ElementValueTypeInfo.builder()
                        .datatype(Datatype.INTEGER)
                        .type(PropertyValue.class)
                        .build())
                .build();
        Object data = List.of(
                new DefaultProperty.Builder()
                        .idShort("stringProp1")
                        .valueType("string")
                        .build(),
                new DefaultProperty.Builder()
                        .idShort("stringProp2")
                        .valueType("integer")
                        .build());
        TypeInfo actual = TypeExtractor.extractTypeInfo(data);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testList() {
        TypeInfo expected = ContainerTypeInfo.builder()
                .contentType(Object.class)
                .type(Collection.class)
                .element(0, ElementValueTypeInfo.builder()
                        .datatype(Datatype.STRING)
                        .type(PropertyValue.class)
                        .build())
                .element(1, ElementValueTypeInfo.builder()
                        .datatype(Datatype.INTEGER)
                        .type(PropertyValue.class)
                        .build())
                .build();
        Object data = List.of(
                new DefaultProperty.Builder()
                        .idShort("stringProp1")
                        .valueType("string")
                        .build(),
                new DefaultProperty.Builder()
                        .idShort("stringProp2")
                        .valueType("integer")
                        .build());
        TypeInfo actual = TypeExtractor.extractTypeInfo(data);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSubmodelElementCollection() {
        TypeInfo expected = ElementValueTypeInfo.builder()
                .type(SubmodelElementCollectionValue.class)
                .element("stringProp1", ElementValueTypeInfo.builder()
                        .datatype(Datatype.STRING)
                        .type(PropertyValue.class)
                        .build())
                .element("doubleRange1", ElementValueTypeInfo.builder()
                        .datatype(Datatype.DOUBLE)
                        .type(RangeValue.class)
                        .build())
                .element("entity1", ElementValueTypeInfo.builder()
                        .type(EntityValue.class)
                        .element("MaxRotationSpeed", ElementValueTypeInfo.builder()
                                .datatype(Datatype.INT)
                                .type(PropertyValue.class)
                                .build())
                        .build())
                .build();
        Object data = new DefaultSubmodelElementCollection.Builder()
                .idShort("collection1")
                .kind(ModelingKind.INSTANCE)
                .value(new DefaultProperty.Builder()
                        .category("category")
                        .idShort("stringProp1")
                        .valueType(Datatype.STRING.getName())
                        .value("foo")
                        .build())
                .value(new DefaultRange.Builder()
                        .idShort("doubleRange1")
                        .kind(ModelingKind.INSTANCE)
                        .valueType(Datatype.DOUBLE.getName())
                        .min("3.0")
                        .max("5.0")
                        .build())
                .value(new DefaultEntity.Builder()
                        .idShort("entity1")
                        .kind(ModelingKind.INSTANCE)
                        .entityType(EntityType.SELF_MANAGED_ENTITY)
                        .statement(new DefaultProperty.Builder()
                                .idShort("MaxRotationSpeed")
                                .valueType(Datatype.INT.getName())
                                .value("5000")
                                .build())
                        .globalAssetId(AasUtils.parseReference("(GlobalReference)[IRI]http://customer.com/demo/asset/1/1/MySubAsset"))
                        .build())
                .build();
        TypeInfo actual = TypeExtractor.extractTypeInfo(data);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testNestedSubmodelElementCollection() {
        TypeInfo expected = ElementValueTypeInfo.builder()
                .type(SubmodelElementCollectionValue.class)
                .element("stringProp1", ElementValueTypeInfo.builder()
                        .datatype(Datatype.STRING)
                        .type(PropertyValue.class)
                        .build())
                .element("doubleRange1", ElementValueTypeInfo.builder()
                        .datatype(Datatype.DOUBLE)
                        .type(RangeValue.class)
                        .build())
                .element("entity1", ElementValueTypeInfo.builder()
                        .type(EntityValue.class)
                        .element("MaxRotationSpeed", ElementValueTypeInfo.builder()
                                .datatype(Datatype.INT)
                                .type(PropertyValue.class)
                                .build())
                        .build())
                .element("collection2", ElementValueTypeInfo.builder()
                        .type(SubmodelElementCollectionValue.class)
                        .element("stringProp2", ElementValueTypeInfo.builder()
                                .datatype(Datatype.STRING)
                                .type(PropertyValue.class)
                                .build())
                        .build())
                .build();
        Object data = new DefaultSubmodelElementCollection.Builder()
                .idShort("collection1")
                .kind(ModelingKind.INSTANCE)
                .value(new DefaultProperty.Builder()
                        .category("category")
                        .idShort("stringProp1")
                        .valueType(Datatype.STRING.getName())
                        .value("foo")
                        .build())
                .value(new DefaultRange.Builder()
                        .idShort("doubleRange1")
                        .kind(ModelingKind.INSTANCE)
                        .valueType(Datatype.DOUBLE.getName())
                        .min("3.0")
                        .max("5.0")
                        .build())
                .value(new DefaultEntity.Builder()
                        .idShort("entity1")
                        .kind(ModelingKind.INSTANCE)
                        .entityType(EntityType.SELF_MANAGED_ENTITY)
                        .statement(new DefaultProperty.Builder()
                                .idShort("MaxRotationSpeed")
                                .valueType(Datatype.INT.getName())
                                .value("5000")
                                .build())
                        .globalAssetId(AasUtils.parseReference("(GlobalReference)[IRI]http://customer.com/demo/asset/1/1/MySubAsset"))
                        .build())
                .value(new DefaultSubmodelElementCollection.Builder()
                        .idShort("collection2")
                        .kind(ModelingKind.INSTANCE)
                        .value(new DefaultProperty.Builder()
                                .category("category")
                                .idShort("stringProp2")
                                .valueType(Datatype.STRING.getName())
                                .value("bar")
                                .build())
                        .build())
                .build();
        TypeInfo actual = TypeExtractor.extractTypeInfo(data);
        Assert.assertEquals(expected, actual);
    }
}
