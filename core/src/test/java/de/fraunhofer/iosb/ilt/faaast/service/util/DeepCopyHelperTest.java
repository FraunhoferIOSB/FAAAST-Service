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

import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;


public class DeepCopyHelperTest {

    @Test
    public void testDeepCopyEnvNull() {
        AssetAdministrationShellEnvironment expected = null;
        AssetAdministrationShellEnvironment actual = DeepCopyHelper.deepCopy(expected);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeepCopyList() {
        List<Property> expected = List.of(
                new DefaultProperty.Builder()
                        .idShort("property1")
                        .build(),
                new DefaultProperty.Builder()
                        .idShort("property2")
                        .build());
        List<Property> actual = DeepCopyHelper.deepCopy(expected, Property.class);
        Assert.assertEquals(expected, actual);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDeepCopyListNull() {
        DeepCopyHelper.deepCopy((List) null, null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDeepCopyListNullType() {
        List<Property> expected = List.of(
                new DefaultProperty.Builder()
                        .idShort("property1")
                        .build(),
                new DefaultProperty.Builder()
                        .idShort("property2")
                        .build());
        DeepCopyHelper.deepCopy(expected, null);
    }


    @Test
    public void testDeepCopyListSupertype() {
        List<Property> expected = List.of(
                new DefaultProperty.Builder()
                        .idShort("property1")
                        .build(),
                new DefaultProperty.Builder()
                        .idShort("property2")
                        .build());
        List<SubmodelElement> actual = DeepCopyHelper.deepCopy(expected, SubmodelElement.class);
        Assert.assertEquals(expected, actual);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDeepCopyReferableNull() {
        DeepCopyHelper.deepCopy((Referable) null, null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDeepCopyReferableNullType() {
        DeepCopyHelper.deepCopy(new DefaultProperty.Builder()
                .idShort("property1")
                .build(),
                null);
    }


    @Test
    public void testDeepCopyReferableSupertype() {
        Property expected = new DefaultProperty.Builder()
                .idShort("property1")
                .build();
        SubmodelElement actual = DeepCopyHelper.deepCopy(expected, SubmodelElement.class);
        Assert.assertEquals(expected, actual);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDeepCopyReferableTypeMismatch() {
        DeepCopyHelper.deepCopy(new DefaultProperty.Builder()
                .idShort("property1")
                .build(),
                Range.class);
    }


    @Test
    public void testDeepEnvCopy() {
        AssetAdministrationShellEnvironment expected = AASFull.ENVIRONMENT;
        AssetAdministrationShellEnvironment actual = DeepCopyHelper.deepCopy(expected);
        Assert.assertEquals(expected, actual);
    }

}
