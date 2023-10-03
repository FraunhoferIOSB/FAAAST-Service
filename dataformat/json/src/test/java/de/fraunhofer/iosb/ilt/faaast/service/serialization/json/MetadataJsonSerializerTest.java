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
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.MetadataJsonSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.fixture.MetadataExamples;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;


public class MetadataJsonSerializerTest {

    private final MetadataJsonSerializer serializer = new MetadataJsonSerializer();

    @Test
    public void testAssetAdministrationShell() throws SerializationException, JSONException, IOException {
        assertEquals(MetadataExamples.ASSET_ADMINISTRATION_SHELL_FILE, MetadataExamples.ASSET_ADMINISTRATION_SHELL);
    }


    @Test
    public void testSubmodel() throws SerializationException, JSONException, IOException {
        assertEquals(MetadataExamples.SUBMODEL_FILE, MetadataExamples.SUBMODEL);
    }


    @Test
    public void testBlob() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(MetadataExamples.BLOB_FILE, MetadataExamples.BLOB);
    }


    @Test
    public void testElementCollection() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(MetadataExamples.ELEMENT_COLLECTION_FILE, MetadataExamples.ELEMENT_COLLECTION);
    }


    @Test
    public void testElementList() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(MetadataExamples.ELEMENT_LIST_FILE, MetadataExamples.ELEMENT_LIST);
    }


    @Test
    public void testEntity() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(MetadataExamples.ENTITY_FILE, MetadataExamples.ENTITY);
    }


    @Test
    public void testFile() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(MetadataExamples.FILE_FILE, MetadataExamples.FILE);
    }


    @Test
    public void testMultiLanguageProperty() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(MetadataExamples.MULTI_LANGUAGE_PROPERTY_FILE, MetadataExamples.MULTI_LANGUAGE_PROPERTY);
    }


    @Test
    public void testProperty() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(MetadataExamples.PROPERTY_DATETIME_FILE, MetadataExamples.PROPERTY_DATETIME);
        assertEquals(MetadataExamples.PROPERTY_DOUBLE_FILE, MetadataExamples.PROPERTY_DOUBLE);
        assertEquals(MetadataExamples.PROPERTY_INT_FILE, MetadataExamples.PROPERTY_INT);
        assertEquals(MetadataExamples.PROPERTY_STRING_FILE, MetadataExamples.PROPERTY_STRING);
    }


    @Test
    public void testRange() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(MetadataExamples.RANGE_DOUBLE_FILE, MetadataExamples.RANGE_DOUBLE);
        assertEquals(MetadataExamples.RANGE_INT_FILE, MetadataExamples.RANGE_INT);
    }


    @Test
    public void testReferenceElement() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(MetadataExamples.REFERENCE_ELEMENT_GLOBAL_FILE, MetadataExamples.REFERENCE_ELEMENT_GLOBAL);
        assertEquals(MetadataExamples.REFERENCE_ELEMENT_MODEL_FILE, MetadataExamples.REFERENCE_ELEMENT_MODEL);
    }


    @Test
    public void testRelationshipElement() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(MetadataExamples.RELATIONSHIP_ELEMENT_FILE, MetadataExamples.RELATIONSHIP_ELEMENT);
    }


    @Test
    public void testAnnotatedRelationshipElement() throws SerializationException, JSONException, IOException, ValueMappingException {
        assertEquals(MetadataExamples.ANNOTATED_RELATIONSHIP_ELEMENT_FILE, MetadataExamples.ANNOTATED_RELATIONSHIP_ELEMENT);
    }


    private void assertEquals(File expectedFile, Object value) throws JSONException, IOException, SerializationException {
        assertEquals(Files.readString(expectedFile.toPath()), value);
    }


    private void assertEquals(String expected, Object value) throws JSONException, IOException, SerializationException {
        String actual = serializer.write(value);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }

}
