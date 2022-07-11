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
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.ValueOnlyJsonSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.fixture.PropertyValues;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.util.ValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
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
    public void testAnnotatedRelationshipElement() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT_FILE, PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT);
        assertValue(PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT_FILE, PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT);
    }


    @Test
    public void testBlob() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(PropertyValues.BLOB_FILE_WITH_BLOB, PropertyValues.BLOB, Extent.WITH_BLOB_VALUE);
        assertEquals(PropertyValues.BLOB_FILE_WITHOUT_BLOB, PropertyValues.BLOB, Extent.WITHOUT_BLOB_VALUE);
        assertValue(PropertyValues.BLOB_FILE_WITH_BLOB, PropertyValues.BLOB, Extent.WITH_BLOB_VALUE);
        assertValue(PropertyValues.BLOB_FILE_WITHOUT_BLOB, PropertyValues.BLOB, Extent.WITHOUT_BLOB_VALUE);
    }


    @Test
    public void testElementCollection() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(PropertyValues.ELEMENT_COLLECTION_FILE, PropertyValues.ELEMENT_COLLECTION);
        assertValue(PropertyValues.ELEMENT_COLLECTION_FILE, PropertyValues.ELEMENT_COLLECTION);
    }


    @Test
    public void testEntity() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(PropertyValues.ENTITY_FILE, PropertyValues.ENTITY);
        assertValue(PropertyValues.ENTITY_FILE, PropertyValues.ENTITY);
    }


    @Test
    public void testFile() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(PropertyValues.FILE_FILE, PropertyValues.FILE);
        assertValue(PropertyValues.FILE_FILE, PropertyValues.FILE);
    }


    @Test
    public void testMultiLanguageProperty() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(PropertyValues.MULTI_LANGUAGE_PROPERTY_FILE, PropertyValues.MULTI_LANGUAGE_PROPERTY);
        assertValue(PropertyValues.MULTI_LANGUAGE_PROPERTY_FILE, PropertyValues.MULTI_LANGUAGE_PROPERTY);
    }


    @Test(expected = SerializationException.class)
    public void testNonValue() throws SerializationException, JSONException, IOException {
        serializer.write(AasUtils.parseReference("(Property)[IRI]foo)"));
    }


    @Test
    public void testProperty() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(PropertyValues.PROPERTY_STRING_FILE, PropertyValues.PROPERTY_STRING);
        assertValue(PropertyValues.PROPERTY_STRING_FILE, PropertyValues.PROPERTY_STRING);
    }


    @Test
    public void testList() throws SerializationException, JSONException, IOException, ValueMappingException {
        Map<SubmodelElement, File> data = Map.of(
                PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE,
                PropertyValues.RANGE_INT, PropertyValues.RANGE_INT_FILE);
        String expected = data.entrySet().stream()
                .map(x -> {
                    try {
                        return ValueHelper.extractValueJson(x.getValue(), x.getKey());
                    }
                    catch (IOException e) {
                        Assert.fail("error extracting value from file");
                    }
                    return "";
                })
                .collect(Collectors.joining(",", "[", "]"));
        List<Object> values = data.keySet().stream()
                .map(LambdaExceptionHelper.rethrowFunction(x -> ElementValueMapper.toValue(x)))
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
    public void testRange() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(PropertyValues.RANGE_DOUBLE_FILE, PropertyValues.RANGE_DOUBLE);
        assertValue(PropertyValues.RANGE_DOUBLE_FILE, PropertyValues.RANGE_DOUBLE);
    }


    @Test
    public void testReferenceElement() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(PropertyValues.REFERENCE_ELEMENT_GLOBAL_FILE, PropertyValues.REFERENCE_ELEMENT_GLOBAL);
        assertEquals(PropertyValues.REFERENCE_ELEMENT_MODEL_FILE, PropertyValues.REFERENCE_ELEMENT_MODEL);
        assertValue(PropertyValues.REFERENCE_ELEMENT_GLOBAL_FILE, PropertyValues.REFERENCE_ELEMENT_GLOBAL);
        assertValue(PropertyValues.REFERENCE_ELEMENT_MODEL_FILE, PropertyValues.REFERENCE_ELEMENT_MODEL);
    }


    @Test
    public void testRelationshipElement() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(PropertyValues.RELATIONSHIP_ELEMENT_FILE, PropertyValues.RELATIONSHIP_ELEMENT);
        assertValue(PropertyValues.RELATIONSHIP_ELEMENT_FILE, PropertyValues.RELATIONSHIP_ELEMENT);
    }


    @Test
    public void testSubmodel() throws SerializationException, JSONException, IOException {
        assertEquals(PropertyValues.SUBMODEL_FILE, PropertyValues.SUBMODEL);
    }


    private void assertEquals(File expectedFile, Object value) throws JSONException, IOException, SerializationException {
        assertEquals(expectedFile, value, Level.DEFAULT, Extent.DEFAULT);
    }


    private void assertEquals(String expected, Object value) throws JSONException, IOException, SerializationException {
        assertEquals(expected, value, Level.DEFAULT, Extent.DEFAULT);
    }


    private void assertEquals(File expectedFile, Object value, Level level, Extent extend) throws JSONException, IOException, SerializationException {
        assertEquals(Files.readString(expectedFile.toPath()), value, level, extend);
    }


    private void assertEquals(File expectedFile, Object value, Extent extend) throws JSONException, IOException, SerializationException {
        assertEquals(Files.readString(expectedFile.toPath()), value, Level.DEFAULT, extend);
    }


    private void assertEquals(String expected, Object value, Level level, Extent extend) throws JSONException, IOException, SerializationException {
        String actual = serializer.write(value, level, extend);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }


    private void assertValue(File expectedFile, SubmodelElement submodelElement) throws JSONException, IOException, SerializationException, ValueMappingException {
        assertValue(expectedFile, submodelElement, Level.DEFAULT, Extent.DEFAULT);
    }


    private void assertValue(File expectedFile, SubmodelElement submodelElement, Extent extend) throws JSONException, IOException, SerializationException, ValueMappingException {
        assertValue(expectedFile, submodelElement, Level.DEFAULT, extend);
    }


    private void assertValue(File expectedFile, SubmodelElement submodelElement, Level level, Extent extend)
            throws JSONException, IOException, SerializationException, ValueMappingException {
        assertEquals(ValueHelper.extractValueJson(expectedFile, submodelElement), ElementValueMapper.toValue(submodelElement), level, extend);
    }

}
