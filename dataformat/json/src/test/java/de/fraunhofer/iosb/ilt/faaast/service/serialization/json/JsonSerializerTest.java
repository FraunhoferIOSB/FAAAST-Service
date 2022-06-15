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

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.fixture.PropertyValues;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetKind;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShell;
import io.adminshell.aas.v3.model.impl.DefaultAssetInformation;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;


public class JsonSerializerTest {

    private final JsonSerializer serializer = new JsonSerializer();

    @Test
    public void testIdentifiableSerialization() throws Exception {
        AssetAdministrationShell shell = new DefaultAssetAdministrationShell.Builder()
                .idShort("testShell")
                .assetInformation(new DefaultAssetInformation.Builder().assetKind(AssetKind.INSTANCE).build())
                .build();
        assertAdminShellIoSerialization(shell);
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
        assertAdminShellIoSerialization(shells);
    }


    @Test
    public void testReferableSerialization() throws Exception {
        Property property = new DefaultProperty.Builder()
                .idShort("testShell")
                .value("Test")
                .build();
        assertAdminShellIoSerialization(property);
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

        assertAdminShellIoSerialization(submodelElements);
    }


    @Test
    public void testSubmodelElementListValueOnly() throws SerializationException, JSONException {
        Map<SubmodelElement, File> data = Map.of(
                PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE,
                PropertyValues.RANGE_INT, PropertyValues.RANGE_INT_FILE);
        String expected = data.entrySet().stream()
                .map(x -> {
                    try {
                        return Files.readString(x.getValue().toPath());
                    }
                    catch (IOException e) {
                        Assert.fail(String.format("error reading file %s", x.getValue()));
                    }
                    return "";
                })
                .collect(Collectors.joining(",", "[", "]"));
        String actual = serializer.write(data.keySet(), new OutputModifier.Builder()
                .content(Content.VALUE)
                .build());
        assertEquals(expected, actual);
    }


    @Test
    public void testEnumsWithCustomNaming() throws SerializationException {
        Assert.assertEquals("\"SuccessCreated\"", serializer.write(StatusCode.SUCCESS_CREATED));
    }


    @Test
    public void testEnumsWithoutCustomNaming() throws SerializationException {
        Assert.assertEquals("\"UTF-8\"", serializer.write(StandardCharsets.UTF_8));
    }


    @Test
    public void testFullExampleSerialization() throws Exception {
        String expected = new io.adminshell.aas.v3.dataformat.json.JsonSerializer().write(AASFull.createEnvironment());
        String actual = serializer.write(AASFull.createEnvironment(), new OutputModifier.Builder().build());
        assertEquals(expected, actual);
    }


    private void assertAdminShellIoSerialization(Referable referable) throws Exception {
        String expected = new io.adminshell.aas.v3.dataformat.json.JsonSerializer().write(referable);
        String actual = serializer.write(referable, new OutputModifier.Builder().build());
        assertEquals(expected, actual);
    }


    private void assertAdminShellIoSerialization(List<Referable> referables) throws Exception {
        String expected = new io.adminshell.aas.v3.dataformat.json.JsonSerializer().write(referables);
        String actual = serializer.write(referables, new OutputModifier.Builder().build());
        assertEquals(expected, actual);
    }


    private void assertEquals(String expected, String actual) throws JSONException {
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }

}
