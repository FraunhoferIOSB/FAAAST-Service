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
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.fixture.PropertyValues;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class ValueSerializationTest {

    private JsonSerializer serializer = new JsonSerializer();

    @Test
    public void testPropertyValue() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.PROPERTY_VALUE_FILE, PropertyValues.PROPERTY_VALUE);
    }

    @Test
    public void testRangeValue() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.RANGE_VALUE_FILE, PropertyValues.RANGE_VALUE);
    }

    @Test
    public void testMultiLanguagePropertyValue() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.MULTI_LANGUAGE_PROPERTY_VALUE_FILE, PropertyValues.MULTI_LANGUAGE_PROPERTY_VALUE);
    }

    @Test
    public void testReferenceElementValueGlobal() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.REFERENCE_ELEMENT_VALUE_GLOBAL_FILE, PropertyValues.REFERENCE_ELEMENT_GLOBAL_VALUE);
    }

    @Test
    public void testReferenceElementValueModel() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.REFERENCE_ELEMENT_VALUE_MODEL_FILE, PropertyValues.REFERENCE_ELEMENT_MODEL_VALUE);
    }

    @Test
    public void testFileValue() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.FILE_VALUE_FILE, PropertyValues.FILE_VALUE);
    }

    @Test
    public void testBlobValue_WithBlob() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.BLOB_VALUE_FILE_WITH_BLOB, PropertyValues.BLOB_VALUE, new OutputModifier.Builder()
                .extend(Extend.WithBLOBValue)
                .build());
    }

    @Test
    public void testBlobValue_WithoutBlob() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.BLOB_VALUE_FILE_WITHOUT_BLOB, PropertyValues.BLOB_VALUE, new OutputModifier.Builder()
                .extend(Extend.WithoutBLOBValue)
                .build());
    }

    @Test
    public void testRelationshipElementValue() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.RELATIONSHIP_ELEMENT_VALUE_FILE, PropertyValues.RELATIONSHIP_ELEMENT_GLOBAL_VALUE);
    }

    @Test
    public void testAnnotatedRelationshipElementValue() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT_VALUE_FILE, PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT_GLOBAL_VALUE);
    }

    @Test
    public void testEntityValue() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.ENTITY_VALUE_FILE, PropertyValues.ENTITY_ELEMENT_VALUE);
    }

    @Test
    public void testElementCollectionValue() throws SerializationException, JSONException, IOException {
        compare(PropertyValues.ELEMENT_COLLECTION_VALUE_FILE, PropertyValues.ELEMENT_COLLECTION_VALUE);
    }

    private void compare(File expectedFile, ElementValue value) throws JSONException, IOException, SerializationException {
        compare(expectedFile, value, OutputModifier.DEFAULT);
    }

    private void compare(File expectedFile, ElementValue value, OutputModifier modifier) throws JSONException, IOException, SerializationException {
        String expected = Files.readString(expectedFile.toPath());
        String actual = serializer.write(value, modifier);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }
}
