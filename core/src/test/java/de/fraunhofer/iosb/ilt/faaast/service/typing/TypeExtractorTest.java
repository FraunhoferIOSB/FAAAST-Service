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

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementCollectionValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.Datatype;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.EntityType;
import io.adminshell.aas.v3.model.ModelingKind;
import io.adminshell.aas.v3.model.impl.DefaultAnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.impl.DefaultEntity;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultRange;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
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
        TypeContext expected = TypeContext.builder()
                .rootInfo(TypeInfo.builder()
                        .valueType(null)
                        .build())
                .typeInfo(TypeInfo.builder()
                        .idShortPath("stringProp1")
                        .datatype(Datatype.String)
                        .valueType(PropertyValue.class)
                        .build())
                .typeInfo(TypeInfo.builder()
                        .idShortPath("intProp1")
                        .datatype(Datatype.Int)
                        .valueType(PropertyValue.class)
                        .build())
                .typeInfo(TypeInfo.builder()
                        .idShortPath("collection1")
                        .valueType(ElementCollectionValue.class)
                        .build())
                .typeInfo(TypeInfo.builder()
                        .idShortPath("collection1", "doubleRange1")
                        .datatype(Datatype.Double)
                        .valueType(RangeValue.class)
                        .build())
                .typeInfo(TypeInfo.builder()
                        .idShortPath("collection1", "intProp2")
                        .datatype(Datatype.Int)
                        .valueType(PropertyValue.class)
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
        TypeContext actual = TypeExtractor.getTypeContext(data);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testAnnotatedRelationshipElement() {
        TypeContext expected = TypeContext.builder()
                .rootInfo(TypeInfo.builder()
                        .valueType(AnnotatedRelationshipElementValue.class)
                        .build())
                .typeInfo(TypeInfo.builder()
                        .idShortPath("stringProp1")
                        .datatype(Datatype.String)
                        .valueType(PropertyValue.class)
                        .build())
                .typeInfo(TypeInfo.builder()
                        .idShortPath("intProp1")
                        .datatype(Datatype.Int)
                        .valueType(PropertyValue.class)
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
        TypeContext actual = TypeExtractor.getTypeContext(data);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPropertyString() {
        TypeContext expected = TypeContext.builder()
                .rootInfo(TypeInfo.builder()
                        .datatype(Datatype.String)
                        .valueType(PropertyValue.class)
                        .build())
                .build();
        Object data = new DefaultProperty.Builder()
                .idShort("stringProp1")
                .valueType("string")
                .build();
        TypeContext actual = TypeExtractor.getTypeContext(data);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSubmodelElementCollection() {
        TypeContext expected = TypeContext.builder()
                .rootInfo(TypeInfo.builder()
                        .valueType(ElementCollectionValue.class)
                        .build())
                .typeInfo(TypeInfo.builder()
                        .idShortPath("stringProp1")
                        .datatype(Datatype.String)
                        .valueType(PropertyValue.class)
                        .build())
                .typeInfo(TypeInfo.builder()
                        .idShortPath("doubleRange1")
                        .datatype(Datatype.Double)
                        .valueType(RangeValue.class)
                        .build())
                .typeInfo(TypeInfo.builder()
                        .idShortPath("entity1")
                        .valueType(EntityValue.class)
                        .build())
                .typeInfo(TypeInfo.builder()
                        .idShortPath("entity1", "MaxRotationSpeed")
                        .datatype(Datatype.Int)
                        .valueType(PropertyValue.class)
                        .build())
                .build();
        Object data = new DefaultSubmodelElementCollection.Builder()
                .idShort("collection1")
                .kind(ModelingKind.INSTANCE)
                .value(new DefaultProperty.Builder()
                        .category("category")
                        .idShort("stringProp1")
                        .valueType(Datatype.String.getName())
                        .value("foo")
                        .build())
                .value(new DefaultRange.Builder()
                        .idShort("doubleRange1")
                        .kind(ModelingKind.INSTANCE)
                        .valueType(Datatype.Double.getName())
                        .min("3.0")
                        .max("5.0")
                        .build())
                .value(new DefaultEntity.Builder()
                        .idShort("entity1")
                        .kind(ModelingKind.INSTANCE)
                        .entityType(EntityType.SELF_MANAGED_ENTITY)
                        .statement(new DefaultProperty.Builder()
                                .idShort("MaxRotationSpeed")
                                .valueType(Datatype.Int.getName())
                                .value("5000")
                                .build())
                        .globalAssetId(AasUtils.parseReference("(GlobalReference)[IRI]http://customer.com/demo/asset/1/1/MySubAsset"))
                        .build())
                .build();
        TypeContext actual = TypeExtractor.getTypeContext(data);
        Assert.assertEquals(expected, actual);
    }
}
