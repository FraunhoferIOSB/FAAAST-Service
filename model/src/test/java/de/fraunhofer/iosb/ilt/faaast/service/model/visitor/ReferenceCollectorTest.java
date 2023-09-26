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
package de.fraunhofer.iosb.ilt.faaast.service.model.visitor;

import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;
import org.junit.Assert;
import org.junit.Test;


public class ReferenceCollectorTest {

    @Test
    public void testCollect() {
        Property property1 = new DefaultProperty.Builder()
                .idShort("property1")
                .build();
        Property property2 = new DefaultProperty.Builder()
                .idShort("property2")
                .build();
        Property property3 = new DefaultProperty.Builder()
                .idShort("property3")
                .build();
        DefaultSubmodelElementCollection collection1 = new DefaultSubmodelElementCollection.Builder()
                .idShort("collection1")
                .value(property2)
                .build();
        DefaultSubmodelElementList list1 = new DefaultSubmodelElementList.Builder()
                .idShort("list1")
                .value(property3)
                .build();
        Submodel submodel = new DefaultSubmodel.Builder()
                .id("submodel")
                .submodelElements(property1)
                .submodelElements(collection1)
                .submodelElements(list1)
                .build();
        AssetAdministrationShell aas1 = new DefaultAssetAdministrationShell.Builder()
                .id("aas1")
                .submodels(ReferenceBuilder.forSubmodel(submodel.getId()))
                .build();
        AssetAdministrationShell aas2 = new DefaultAssetAdministrationShell.Builder()
                .id("aas2")
                .submodels(ReferenceBuilder.forSubmodel(submodel.getId()))
                .build();
        Environment environment = new DefaultEnvironment.Builder()
                .assetAdministrationShells(aas1)
                .assetAdministrationShells(aas2)
                .submodels(submodel)
                .build();
        Reference aas1Ref = ReferenceBuilder.forAas(aas1);
        Reference aas2Ref = ReferenceBuilder.forAas(aas2);
        Reference submodelRef = ReferenceBuilder.forSubmodel(submodel);
        Reference property1Ref = new ReferenceBuilder()
                .submodel(submodel)
                .element(property1)
                .build();
        Reference collection1Ref = new ReferenceBuilder()
                .submodel(submodel)
                .element(collection1)
                .build();
        Reference property2Ref = new ReferenceBuilder()
                .submodel(submodel)
                .elements(collection1, property2)
                .build();
        Reference list1Ref = new ReferenceBuilder()
                .submodel(submodel)
                .elements(list1)
                .build();
        Reference property3Ref = new ReferenceBuilder()
                .submodel(submodel)
                .elements(list1.getIdShort(), "0")
                .build();
        Map<Reference, Referable> expected = new HashMap<>();
        expected.put(submodelRef, submodel);
        expected.put(property1Ref, property1);
        expected.put(collection1Ref, collection1);
        expected.put(property2Ref, property2);
        expected.put(list1Ref, list1);
        expected.put(property3Ref, property3);
        expected.put(aas1Ref, aas1);
        expected.put(ReferenceHelper.combine(aas1Ref, submodelRef), submodel);
        expected.put(ReferenceHelper.combine(aas1Ref, property1Ref), property1);
        expected.put(ReferenceHelper.combine(aas1Ref, collection1Ref), collection1);
        expected.put(ReferenceHelper.combine(aas1Ref, property2Ref), property2);
        expected.put(ReferenceHelper.combine(aas1Ref, list1Ref), list1);
        expected.put(ReferenceHelper.combine(aas1Ref, property3Ref), property3);
        expected.put(aas2Ref, aas2);
        expected.put(ReferenceHelper.combine(aas2Ref, submodelRef), submodel);
        expected.put(ReferenceHelper.combine(aas2Ref, property1Ref), property1);
        expected.put(ReferenceHelper.combine(aas2Ref, collection1Ref), collection1);
        expected.put(ReferenceHelper.combine(aas2Ref, property2Ref), property2);
        expected.put(ReferenceHelper.combine(aas2Ref, list1Ref), list1);
        expected.put(ReferenceHelper.combine(aas2Ref, property3Ref), property3);
        Map<Reference, Referable> actual = ReferenceCollector.collect(environment);
        Assert.assertEquals(expected.size(), actual.size());
        // cannot compare using .equals on references as keyTypes do not match
        for (var reference: expected.keySet()) {
            Optional<Reference> actualReference = actual.keySet().stream()
                    .filter(x -> ReferenceHelper.equals(reference, x))
                    .findAny();
            Assert.assertTrue(actualReference.isPresent());
            Assert.assertEquals(expected.get(reference), actual.get(actualReference.get()));
        }
    }
}
