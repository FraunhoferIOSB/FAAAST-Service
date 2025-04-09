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
package de.fraunhofer.iosb.ilt.faaast.service.model;

import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;
import org.junit.Assert;
import org.junit.Test;


public class SemanticIdPathTest {

    @Test
    public void resolveSematicIdPathInSubmodelUnique() {
        final String idShortCollection1 = "idShortCollection1";
        final String idShortCollection2 = "idShortCollection2";
        final String idShortProperty1 = "idShortProperty1";
        Reference semanticIdSubmodel = ReferenceBuilder.global("submodel");
        Reference semanticIdCollection = ReferenceBuilder.global("collection");
        Reference semanticIdProperty1 = ReferenceBuilder.global("property1");

        Submodel submodel = new DefaultSubmodel.Builder()
                .id("submodel-id")
                .semanticId(semanticIdSubmodel)
                .submodelElements(new DefaultSubmodelElementCollection.Builder()
                        .idShort(idShortCollection1)
                        .semanticId(semanticIdCollection)
                        .build())
                .submodelElements(new DefaultSubmodelElementCollection.Builder()
                        .idShort(idShortCollection2)
                        .semanticId(semanticIdCollection)
                        .value(new DefaultProperty.Builder()
                                .idShort(idShortProperty1)
                                .semanticId(semanticIdProperty1)
                                .build())
                        .build())
                .build();

        Reference expected = new ReferenceBuilder()
                .submodel(submodel)
                .element(idShortCollection2, KeyTypes.SUBMODEL_ELEMENT_COLLECTION)
                .element(idShortProperty1, KeyTypes.PROPERTY)
                .build();
        Reference actual = SemanticIdPath.builder()
                .semanticId(semanticIdCollection)
                .semanticId(semanticIdProperty1)
                .build()
                .resolveUnique(submodel);
        Assert.assertTrue(ReferenceHelper.equals(expected, actual));
    }


    @Test
    public void resolveSematicIdPathInCollection() {
        final String idShortCollection = "idShortCollection";
        final String idShortProperty = "idShortProperty";
        Reference semanticIdCollection = ReferenceBuilder.global("collection");
        Reference semanticIdProperty = ReferenceBuilder.global("property");

        SubmodelElementCollection submodel = new DefaultSubmodelElementCollection.Builder()
                .idShort(idShortCollection)
                .semanticId(semanticIdCollection)
                .value(new DefaultProperty.Builder()
                        .idShort(idShortProperty)
                        .semanticId(semanticIdProperty)
                        .build())
                .build();

        Reference expected = new ReferenceBuilder()
                .element(idShortCollection, KeyTypes.SUBMODEL_ELEMENT_COLLECTION)
                .element(idShortProperty, KeyTypes.PROPERTY)
                .build();
        Reference actual = SemanticIdPath.builder()
                .semanticId(semanticIdProperty)
                .build()
                .resolveUnique(submodel);
        Assert.assertTrue(ReferenceHelper.equals(expected, actual));
    }


    @Test
    public void resolveSematicIdPathInList() {
        final String idShortList1 = "idShortList1";
        final String idShortList2 = "idShortList2";
        final String idShortProperty1 = "idShortProperty1";
        final String idShortProperty2 = "idShortProperty2";
        Reference semanticIdList1 = ReferenceBuilder.global("list1");
        Reference semanticIdList2 = ReferenceBuilder.global("list2");
        Reference semanticIdProperty1 = ReferenceBuilder.global("property1");
        Reference semanticIdProperty2 = ReferenceBuilder.global("property2");

        SubmodelElementList submodel = new DefaultSubmodelElementList.Builder()
                .idShort(idShortList1)
                .semanticId(semanticIdList1)
                .value(new DefaultProperty.Builder()
                        .idShort("dummy1")
                        .build())
                .value(new DefaultProperty.Builder()
                        .idShort("dummy2")
                        .build())
                .value(new DefaultSubmodelElementList.Builder()
                        .idShort(idShortList2)
                        .semanticId(semanticIdList2)
                        .value(new DefaultProperty.Builder()
                                .idShort(idShortProperty1)
                                .semanticId(semanticIdProperty1)
                                .build())
                        .value(new DefaultProperty.Builder()
                                .idShort(idShortProperty2)
                                .semanticId(semanticIdProperty2)
                                .build())
                        .build())
                .build();

        Reference expected = new ReferenceBuilder()
                .element(idShortList1, KeyTypes.SUBMODEL_ELEMENT_LIST)
                .element("2", KeyTypes.SUBMODEL_ELEMENT_LIST)
                .element("1", KeyTypes.PROPERTY)
                .build();
        Reference actual = SemanticIdPath.builder()
                .semanticId(semanticIdList2)
                .semanticId(semanticIdProperty2)
                .build()
                .resolveUnique(submodel);
        Assert.assertTrue(ReferenceHelper.equals(expected, actual));
    }


    @Test
    public void resolveSematicIdPathInSubmodelNonUnique() {
        final String idShortCollection1 = "idShortCollection1";
        final String idShortCollection2 = "idShortCollection2";
        final String idShortProperty1 = "idShortProperty1";
        final String idShortProperty2 = "idShortProperty2";
        Reference semanticIdSubmodel = ReferenceBuilder.global("submodel");
        Reference semanticIdCollection = ReferenceBuilder.global("collection");
        Reference semanticIdProperty = ReferenceBuilder.global("property");

        Submodel submodel = new DefaultSubmodel.Builder()
                .id("submodel-id")
                .semanticId(semanticIdSubmodel)
                .submodelElements(new DefaultSubmodelElementCollection.Builder()
                        .idShort(idShortCollection1)
                        .semanticId(semanticIdCollection)
                        .value(new DefaultProperty.Builder()
                                .idShort(idShortProperty1)
                                .semanticId(semanticIdProperty)
                                .build())
                        .build())
                .submodelElements(new DefaultSubmodelElementCollection.Builder()
                        .idShort(idShortCollection2)
                        .semanticId(semanticIdCollection)
                        .value(new DefaultProperty.Builder()
                                .idShort(idShortProperty2)
                                .semanticId(semanticIdProperty)
                                .build())
                        .build())
                .build();

        List<Reference> expected = List.of(
                new ReferenceBuilder()
                        .submodel(submodel)
                        .element(idShortCollection1, KeyTypes.SUBMODEL_ELEMENT_COLLECTION)
                        .element(idShortProperty1, KeyTypes.PROPERTY)
                        .build(),
                new ReferenceBuilder()
                        .submodel(submodel)
                        .element(idShortCollection2, KeyTypes.SUBMODEL_ELEMENT_COLLECTION)
                        .element(idShortProperty2, KeyTypes.PROPERTY)
                        .build());
        List<Reference> actual = SemanticIdPath.builder()
                .semanticId(semanticIdCollection)
                .semanticId(semanticIdProperty)
                .build()
                .resolve(submodel);
        Assert.assertEquals(expected, actual);
        for (int i = 0; i < expected.size(); i++) {
            Assert.assertTrue(ReferenceHelper.equals(expected.get(i), actual.get(i)));
        }
    }
}
