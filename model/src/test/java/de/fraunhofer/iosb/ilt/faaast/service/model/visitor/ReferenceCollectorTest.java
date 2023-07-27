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

import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.Map;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author jab
 */
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
        Map<Reference, Referable> expected = Map.of(
                ReferenceHelper.build(submodel.getId(), Submodel.class), submodel,
                ReferenceHelper.build(null, submodel.getId(), property1.getIdShort()), property1,
                ReferenceHelper.build(null, submodel.getId(), collection1.getIdShort()), collection1,
                ReferenceHelper.build(null, submodel.getId(), collection1.getIdShort(), property2.getIdShort()), property2,
                ReferenceHelper.build(null, submodel.getId(), list1.getIdShort()), list1,
                ReferenceHelper.build(null, submodel.getId(), list1.getIdShort(), "0"), property3);
        Map<Reference, Referable> actual = ReferenceCollector.collect(submodel);
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
