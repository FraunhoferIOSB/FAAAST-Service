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
package de.fraunhofer.iosb.ilt.faaast.service.serialization.json;

import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.OutputModifier;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetKind;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShell;
import io.adminshell.aas.v3.model.impl.DefaultAssetInformation;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;


public class JsonSerializerTest {

    JsonSerializer jsonSerializer = new JsonSerializer();

    @Test
    public void testIdentifiableSerialization() throws Exception {
        AssetAdministrationShell shell = new DefaultAssetAdministrationShell.Builder()
                .idShort("testShell")
                .assetInformation(new DefaultAssetInformation.Builder().assetKind(AssetKind.INSTANCE).build())
                .build();

        compareToAdminShellIoSerialization(shell);
    }


    @Test
    public void testIdentifiableListSerialization() throws Exception {
        List<Referable> shells = List.of(new DefaultAssetAdministrationShell.Builder()
                .idShort("testShell")
                .assetInformation(new DefaultAssetInformation.Builder().assetKind(AssetKind.INSTANCE).build())
                .build(),
                new DefaultAssetAdministrationShell.Builder()
                        .idShort("testShell2")
                        .assetInformation(new DefaultAssetInformation.Builder().assetKind(AssetKind.INSTANCE).build())
                        .build());

        compareToAdminShellIoSerialization(shells);
    }


    @Test
    public void testReferableSerialization() throws Exception {
        Property property = new DefaultProperty.Builder()
                .idShort("testShell")
                .value("Test")
                .build();

        compareToAdminShellIoSerialization(property);
    }


    @Test
    public void testReferableListSerialization() throws Exception {
        List<Referable> submodelElements = List.of(new DefaultProperty.Builder()
                .idShort("testShell")
                .value("Test")
                .build(),
                new DefaultProperty.Builder()
                        .idShort("testShell2")
                        .value("Test")
                        .build());

        compareToAdminShellIoSerialization(submodelElements);
    }


    @Test
    public void testFullExampleSerialization() throws Exception {
        String expected = new io.adminshell.aas.v3.dataformat.json.JsonSerializer().write(AASFull.createEnvironment());
        String actual = jsonSerializer.write(AASFull.createEnvironment(), new OutputModifier.Builder().build());
        compare(expected, actual);
    }


    private void compareToAdminShellIoSerialization(Referable referable) throws Exception {
        String expected = new io.adminshell.aas.v3.dataformat.json.JsonSerializer().write(referable);
        String actual = jsonSerializer.write(referable, new OutputModifier.Builder().build());

        compare(expected, actual);
    }


    private void compareToAdminShellIoSerialization(List<Referable> referables) throws Exception {
        String expected = new io.adminshell.aas.v3.dataformat.json.JsonSerializer().write(referables);
        String actual = jsonSerializer.write(referables, new OutputModifier.Builder().build());

        compare(expected, actual);
    }


    private void compare(String expected, String actual) {
        //System.out.println("Actual: " + actual);
        //System.out.println("Expected: " + expected);

        Assert.assertEquals(expected, actual);
    }

}
