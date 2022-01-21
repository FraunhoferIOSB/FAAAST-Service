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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Extend;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Level;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.fixture.PropertyValues;
import de.fraunhofer.iosb.ilt.faaast.service.util.DataElementValueMapper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;


public class ValueOnlyJsonSerializerTest {

    ValueOnlyJsonSerializer serializer = new ValueOnlyJsonSerializer();

    @Test
    public void testNonValue() throws SerializationException, JSONException, IOException {
        compare("{}", AasUtils.parseReference("(Property)[IRI]foo)"));
    }


    @Test
    public void testSubmodel() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.SUBMODEL_FILE, PropertyValues.SUBMODEL);
    }


    @Test
    public void testPropertyValue() throws SerializationException, JSONException, IOException {
        compareValue(PropertyValues.PROPERTY_FILE, PropertyValues.PROPERTY);
    }


    @Test
    public void testProperty() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.PROPERTY_FILE, PropertyValues.PROPERTY);
    }


    @Test
    public void testRangeValue() throws SerializationException, JSONException, IOException {
        compareValue(PropertyValues.RANGE_FILE, PropertyValues.RANGE);
    }


    @Test
    public void testRange() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.RANGE_FILE, PropertyValues.RANGE);
    }


    @Test
    public void testMultiLanguagePropertyValue() throws SerializationException, JSONException, IOException {
        compareValue(PropertyValues.MULTI_LANGUAGE_PROPERTY_FILE, PropertyValues.MULTI_LANGUAGE_PROPERTY);
    }


    @Test
    public void testMultiLanguageProperty() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.MULTI_LANGUAGE_PROPERTY_FILE, PropertyValues.MULTI_LANGUAGE_PROPERTY);
    }


    @Test
    public void testReferenceElementValue() throws SerializationException, JSONException, IOException {
        compareValue(PropertyValues.REFERENCE_ELEMENT_GLOBAL_FILE, PropertyValues.REFERENCE_ELEMENT_GLOBAL);
        compareValue(PropertyValues.REFERENCE_ELEMENT_MODEL_FILE, PropertyValues.REFERENCE_ELEMENT_MODEL);
    }


    @Test
    public void testReferenceElement() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.REFERENCE_ELEMENT_GLOBAL_FILE, PropertyValues.REFERENCE_ELEMENT_GLOBAL);
        compare(PropertyValues.REFERENCE_ELEMENT_MODEL_FILE, PropertyValues.REFERENCE_ELEMENT_MODEL);
    }


    @Test
    public void testFileValue() throws SerializationException, JSONException, IOException {
        compareValue(PropertyValues.FILE_FILE, PropertyValues.FILE);
    }


    @Test
    public void testFile() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.FILE_FILE, PropertyValues.FILE);
    }


    @Test
    public void testBlobValue() throws SerializationException, JSONException, IOException {
        compareValue(PropertyValues.BLOB_FILE_WITH_BLOB, PropertyValues.BLOB, Extend.WithBLOBValue);
        compareValue(PropertyValues.BLOB_FILE_WITHOUT_BLOB, PropertyValues.BLOB, Extend.WithoutBLOBValue);
    }


    @Test
    public void testBlob() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.BLOB_FILE_WITH_BLOB, PropertyValues.BLOB, Extend.WithBLOBValue);
        compare(PropertyValues.BLOB_FILE_WITHOUT_BLOB, PropertyValues.BLOB, Extend.WithoutBLOBValue);
    }


    @Test
    public void testRelationshipElementValue() throws SerializationException, JSONException, IOException {
        compareValue(PropertyValues.RELATIONSHIP_ELEMENT_FILE, PropertyValues.RELATIONSHIP_ELEMENT);
    }


    @Test
    public void testRelationshipElement() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.RELATIONSHIP_ELEMENT_FILE, PropertyValues.RELATIONSHIP_ELEMENT);
    }


    @Test
    public void testAnnotatedRelationshipElementValue() throws SerializationException, JSONException, IOException {
        compareValue(PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT_FILE, PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT);
    }


    @Test
    public void testAnnotatedRelationshipElement() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT_FILE, PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT);
    }


    @Test
    public void testEntityValue() throws SerializationException, JSONException, IOException {
        compareValue(PropertyValues.ENTITY_FILE, PropertyValues.ENTITY);
    }


    @Test
    public void testEntity() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.ENTITY_FILE, PropertyValues.ENTITY);
    }


    @Test
    public void testElementCollectionValue() throws SerializationException, JSONException, IOException {
        compareValue(PropertyValues.ELEMENT_COLLECTION_FILE, PropertyValues.ELEMENT_COLLECTION);
    }


    @Test
    public void testElementCollection() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.ELEMENT_COLLECTION_FILE, PropertyValues.ELEMENT_COLLECTION);
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


    private void compare(File expectedFile, Object value, Level level) throws JSONException, IOException, SerializationException {
        compare(Files.readString(expectedFile.toPath()), value, level, Extend.DEFAULT);
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


    private void compareValue(File expectedFile, SubmodelElement submodelElement, Level level) throws JSONException, IOException, SerializationException {
        compareValue(expectedFile, submodelElement, level, Extend.DEFAULT);
    }


    private void compareValue(File expectedFile, SubmodelElement submodelElement, Extend extend) throws JSONException, IOException, SerializationException {
        compareValue(expectedFile, submodelElement, Level.DEFAULT, extend);
    }


    private void compareValue(File expectedFile, SubmodelElement submodelElement, Level level, Extend extend) throws JSONException, IOException, SerializationException {
        compare(extractValue(expectedFile, submodelElement), DataElementValueMapper.toDataElement(submodelElement), level, extend);
    }


    private String extractValue(File file, SubmodelElement submodelElement) throws IOException {
        return new ObjectMapper().readTree(file).get(submodelElement.getIdShort()).toString();
    }
}
