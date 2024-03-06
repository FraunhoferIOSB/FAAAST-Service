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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.junit.Assert;
import org.junit.Test;


public class ReferenceBuilderTest {

    @Test
    public void testListElement() {
        String submodelId = "submodelId";
        String listIdShort = "list1";
        String elementIndex = "0";
        String elementIdShortPath = String.format("%s[%s]", listIdShort, elementIndex);
        Reference actual = new ReferenceBuilder()
                .submodel(submodelId)
                .idShortPath(elementIdShortPath)
                .build();
        Reference expected = new DefaultReference.Builder()
                .type(ReferenceTypes.MODEL_REFERENCE)
                .keys(new DefaultKey.Builder()
                        .type(KeyTypes.SUBMODEL)
                        .value(submodelId)
                        .build())
                .keys(new DefaultKey.Builder()
                        .type(KeyTypes.SUBMODEL_ELEMENT_LIST)
                        .value(listIdShort)
                        .build())
                .keys(new DefaultKey.Builder()
                        .type(KeyTypes.SUBMODEL_ELEMENT)
                        .value(elementIndex)
                        .build())
                .build();
        Assert.assertEquals(expected, actual);
    }
}
