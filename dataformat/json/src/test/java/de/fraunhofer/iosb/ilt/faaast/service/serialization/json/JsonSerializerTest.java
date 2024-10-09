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
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.UnsupportedModifierException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.fixture.ValueOnlyExamples;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;


public class JsonSerializerTest {

    private final JsonApiSerializer serializer = new JsonApiSerializer();

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
    public void testSubmodelElementListValueOnly() throws SerializationException, JSONException, UnsupportedModifierException {
        Map<SubmodelElement, File> data = Map.of(ValueOnlyExamples.PROPERTY_STRING, ValueOnlyExamples.PROPERTY_STRING_FILE,
                ValueOnlyExamples.RANGE_INT, ValueOnlyExamples.RANGE_INT_FILE);
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
    public void testEnumsWithCustomNaming() throws SerializationException, UnsupportedModifierException {
        Assert.assertEquals("\"SuccessCreated\"", serializer.write(StatusCode.SUCCESS_CREATED));
    }


    @Test
    public void testEnumsWithoutCustomNaming() throws SerializationException, UnsupportedModifierException {
        Assert.assertEquals("\"UTF-8\"", serializer.write(StandardCharsets.UTF_8));
    }


    @Test
    public void testFullExampleSerialization() throws Exception {
        String expected = new org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer().write(AASFull.createEnvironment());
        String actual = serializer.write(AASFull.createEnvironment(), new OutputModifier.Builder().build());
        assertEquals(expected, actual);
    }


    private void assertAdminShellIoSerialization(Referable referable) throws Exception {
        String expected = new org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer().write(referable);
        String actual = serializer.write(referable, new OutputModifier.Builder().build());
        assertEquals(expected, actual);
    }


    private void assertAdminShellIoSerialization(List<Referable> referables) throws Exception {
        String expected = new org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer().writeList(referables);
        String actual = serializer.write(referables, new OutputModifier.Builder().build());
        assertEquals(expected, actual);
    }


    private void assertEquals(String expected, String actual) throws JSONException {
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }


    @Test
    public void testPageWithoutCursor() throws SerializationException, JSONException, UnsupportedModifierException {
        Page<SubmodelElement> page = Page.<SubmodelElement> builder()
                .metadata(PagingMetadata.builder()
                        .build())
                .result(new DefaultProperty.Builder()
                        .idShort("idShort")
                        .value("foo")
                        .valueType(DataTypeDefXsd.STRING)
                        .build())
                .build();
        String actual = serializer.write(page);
        String expected = "{\n"
                + "  \"result\" : [ {\n"
                + "    \"modelType\" : \"Property\",\n"
                + "    \"value\" : \"foo\",\n"
                + "    \"valueType\" : \"xs:string\",\n"
                + "    \"idShort\" : \"idShort\"\n"
                + "  } ],\n"
                + "  \"paging_metadata\" : {\n"
                + "  }\n"
                + "}";
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }


    @Test
    public void testPageWithCursor() throws SerializationException, JSONException, UnsupportedModifierException {
        Page<SubmodelElement> page = Page.<SubmodelElement> builder()
                .metadata(PagingMetadata.builder()
                        .cursor("foo")
                        .build())
                .result(new DefaultProperty.Builder()
                        .idShort("idShort")
                        .value("foo")
                        .valueType(DataTypeDefXsd.STRING)
                        .build())
                .build();
        String actual = serializer.write(page);
        String expected = "{\n"
                + "  \"result\" : [ {\n"
                + "    \"modelType\" : \"Property\",\n"
                + "    \"value\" : \"foo\",\n"
                + "    \"valueType\" : \"xs:string\",\n"
                + "    \"idShort\" : \"idShort\"\n"
                + "  } ],\n"
                + "  \"paging_metadata\" : {\n"
                + "    \"cursor\" : \"Zm9v\"\n"
                + "  }\n"
                + "}";
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }

}
