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

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Extend;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Level;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.fixture.PropertyValues;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueMapper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;


public class ValueOnlyJsonSerializerTest {

    ValueOnlyJsonSerializer serializer = new ValueOnlyJsonSerializer();

    @Test
    public void testAnnotatedRelationshipElement() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT_FILE, PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT);
        compareValue(PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT_FILE, PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT);
    }


    @Test
    public void testBlob() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.BLOB_FILE_WITH_BLOB, PropertyValues.BLOB, Extend.WithBLOBValue);
        compare(PropertyValues.BLOB_FILE_WITHOUT_BLOB, PropertyValues.BLOB, Extend.WithoutBLOBValue);
        compareValue(PropertyValues.BLOB_FILE_WITH_BLOB, PropertyValues.BLOB, Extend.WithBLOBValue);
        compareValue(PropertyValues.BLOB_FILE_WITHOUT_BLOB, PropertyValues.BLOB, Extend.WithoutBLOBValue);
    }


    @Test
    public void testElementCollection() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.ELEMENT_COLLECTION_FILE, PropertyValues.ELEMENT_COLLECTION);
        compareValue(PropertyValues.ELEMENT_COLLECTION_FILE, PropertyValues.ELEMENT_COLLECTION);
    }


    @Test
    public void testEntity() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.ENTITY_FILE, PropertyValues.ENTITY);
        compareValue(PropertyValues.ENTITY_FILE, PropertyValues.ENTITY);
    }


    @Test
    public void testFile() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.FILE_FILE, PropertyValues.FILE);
        compareValue(PropertyValues.FILE_FILE, PropertyValues.FILE);
    }


    @Test
    public void testMultiLanguageProperty() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.MULTI_LANGUAGE_PROPERTY_FILE, PropertyValues.MULTI_LANGUAGE_PROPERTY);
        compareValue(PropertyValues.MULTI_LANGUAGE_PROPERTY_FILE, PropertyValues.MULTI_LANGUAGE_PROPERTY);
    }


    @Test(expected = SerializationException.class)
    public void testNonValue() throws SerializationException, JSONException, IOException {
        serializer.write(AasUtils.parseReference("(Property)[IRI]foo)"));
    }


    @Test
    public void testProperty() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.PROPERTY_STRING_FILE, PropertyValues.PROPERTY_STRING);
        compareValue(PropertyValues.PROPERTY_STRING_FILE, PropertyValues.PROPERTY_STRING);
    }


    @Test
    public void testList() throws SerializationException, JSONException, IOException {
        Map<SubmodelElement, File> data = Map.of(
                PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE,
                PropertyValues.RANGE_INT, PropertyValues.RANGE_INT_FILE);
        String expected = data.entrySet().stream()
                .map(x -> {
                    try {
                        return TestUtils.extractValueJson(x.getValue(), x.getKey());
                    }
                    catch (IOException ex) {
                        Assert.fail("error extracting value from file");
                    }
                    return "";
                })
                .collect(Collectors.joining(",", "[", "]"));
        List<Object> values = data.keySet().stream()
                .map(x -> ElementValueMapper.toValue(x))
                .collect(Collectors.toList());
        String actual = serializer.write(values);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }


    @Test
    public void testArray() throws SerializationException, JSONException, IOException {
        Object[] array = new Object[] {
                PropertyValues.PROPERTY_STRING,
                PropertyValues.RANGE_INT
        };
        String expected = String.format("[%s,%s]",
                Files.readString(PropertyValues.PROPERTY_STRING_FILE.toPath()),
                Files.readString(PropertyValues.RANGE_INT_FILE.toPath()));
        String actual = serializer.write(array);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }


    @Test
    public void testMap() throws SerializationException, JSONException, IOException {
        Map<String, Object> map = Map.of("first", PropertyValues.PROPERTY_STRING,
                "second", PropertyValues.RANGE_INT);
        String expected = String.format("{ \"first\": %s,\"second\":%s}",
                Files.readString(PropertyValues.PROPERTY_STRING_FILE.toPath()),
                Files.readString(PropertyValues.RANGE_INT_FILE.toPath()));
        String actual = serializer.write(map);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }


    @Test
    public void testRange() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.RANGE_DOUBLE_FILE, PropertyValues.RANGE_DOUBLE);
        compareValue(PropertyValues.RANGE_DOUBLE_FILE, PropertyValues.RANGE_DOUBLE);
    }


    @Test
    public void testReferenceElement() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.REFERENCE_ELEMENT_GLOBAL_FILE, PropertyValues.REFERENCE_ELEMENT_GLOBAL);
        compare(PropertyValues.REFERENCE_ELEMENT_MODEL_FILE, PropertyValues.REFERENCE_ELEMENT_MODEL);
        compareValue(PropertyValues.REFERENCE_ELEMENT_GLOBAL_FILE, PropertyValues.REFERENCE_ELEMENT_GLOBAL);
        compareValue(PropertyValues.REFERENCE_ELEMENT_MODEL_FILE, PropertyValues.REFERENCE_ELEMENT_MODEL);
    }


    @Test
    public void testRelationshipElement() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.RELATIONSHIP_ELEMENT_FILE, PropertyValues.RELATIONSHIP_ELEMENT);
        compareValue(PropertyValues.RELATIONSHIP_ELEMENT_FILE, PropertyValues.RELATIONSHIP_ELEMENT);
    }


    @Test
    public void testSubmodel() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.SUBMODEL_FILE, PropertyValues.SUBMODEL);
    }


    private void compare(File expectedFile, Object value) throws JSONException, IOException, SerializationException {
        compare(expectedFile, value, Level.DEFAULT, Extend.DEFAULT);
    }


    private void compare(String expected, Object value) throws JSONException, IOException, SerializationException {
        compare(expected, value, Level.DEFAULT, Extend.DEFAULT);
    }


    private void compare(File expectedFile, Object value, Level level, Extend extend) throws JSONException, IOException, SerializationException {
        compare(Files.readString(expectedFile.toPath()), value, level, extend);
    }


    private void compare(File expectedFile, Object value, Extend extend) throws JSONException, IOException, SerializationException {
        compare(Files.readString(expectedFile.toPath()), value, Level.DEFAULT, extend);
    }


    private void compare(String expected, Object value, Level level, Extend extend) throws JSONException, IOException, SerializationException {
        String actual = serializer.write(value, level, extend);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }


    private void compareValue(File expectedFile, SubmodelElement submodelElement) throws JSONException, IOException, SerializationException {
        compareValue(expectedFile, submodelElement, Level.DEFAULT, Extend.DEFAULT);
    }


    private void compareValue(File expectedFile, SubmodelElement submodelElement, Extend extend) throws JSONException, IOException, SerializationException {
        compareValue(expectedFile, submodelElement, Level.DEFAULT, extend);
    }


    private void compareValue(File expectedFile, SubmodelElement submodelElement, Level level, Extend extend) throws JSONException, IOException, SerializationException {
        compare(TestUtils.extractValueJson(expectedFile, submodelElement), ElementValueMapper.toValue(submodelElement), level, extend);
    }

}
