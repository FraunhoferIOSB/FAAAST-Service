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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.PathJsonSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.util.Path;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;


public class PathJsonSerializerTest {

    PathJsonSerializer serializer = new PathJsonSerializer();
    private static final String ID_SUBMODEL_1 = "submodel1";
    private static final String ID_COLLECTION_1 = "collection1";
    private static final String ID_COLLECTION_2 = "collection2";
    private static final String ID_COLLECTION_3 = "collection3";
    private static final String ID_PROPERTY_1 = "property1";
    private static final String ID_PROPERTY_2 = "property2";
    private static final String ID_PROPERTY_3 = "property3";

    @Test
    public void testProperty() throws SerializationException, JSONException, IOException, ValueMappingException {
        Object input = new DefaultProperty.Builder()
                .idShort(ID_PROPERTY_1)
                .build();
        Path expected = Path.builder()
                .id(ID_PROPERTY_1)
                .build();
        assertEquals(input, expected);
    }


    @Test
    public void testCollection() throws SerializationException, JSONException, IOException, ValueMappingException {
        Object input = new DefaultSubmodelElementCollection.Builder()
                .idShort(ID_COLLECTION_1)
                .value(new DefaultProperty.Builder()
                        .idShort(ID_PROPERTY_1)
                        .build())
                .value(new DefaultSubmodelElementCollection.Builder()
                        .idShort(ID_COLLECTION_2)
                        .value(new DefaultProperty.Builder()
                                .idShort(ID_PROPERTY_2)
                                .build())
                        .value(new DefaultSubmodelElementCollection.Builder()
                                .idShort(ID_COLLECTION_3)
                                .value(new DefaultProperty.Builder()
                                        .idShort(ID_PROPERTY_3)
                                        .build())
                                .build())
                        .build())
                .build();
        Path expected = Path.builder()
                .id(ID_COLLECTION_1)
                .child(ID_PROPERTY_1)
                .child(Path.builder()
                        .id(ID_COLLECTION_2)
                        .child(ID_PROPERTY_2)
                        .child(Path.builder()
                                .id(ID_COLLECTION_3)
                                .child(ID_PROPERTY_3)
                                .build())
                        .build())
                .build();
        assertEquals(input, expected);
    }


    @Test
    public void testSubmodelEmpty() throws SerializationException, JSONException, IOException, ValueMappingException {
        Object input = new DefaultSubmodel.Builder()
                .idShort(ID_SUBMODEL_1)
                .build();
        Path expected = Path.builder()
                .id(ID_SUBMODEL_1)
                .build();
        assertEquals(input, expected);
    }


    @Test
    public void testSubmodel() throws SerializationException, JSONException, IOException, ValueMappingException {
        Object input = new DefaultSubmodel.Builder()
                .idShort(ID_SUBMODEL_1)
                .submodelElement(new DefaultProperty.Builder()
                        .idShort(ID_PROPERTY_1)
                        .build())
                .submodelElement(new DefaultProperty.Builder()
                        .idShort(ID_PROPERTY_2)
                        .build())
                .submodelElement(new DefaultSubmodelElementCollection.Builder()
                        .idShort(ID_COLLECTION_1)
                        .value(new DefaultProperty.Builder()
                                .idShort(ID_PROPERTY_3)
                                .build())
                        .build())
                .build();
        Path expected = Path.builder()
                .id(ID_SUBMODEL_1)
                .child(ID_PROPERTY_1)
                .child(ID_PROPERTY_2)
                .child(Path.builder()
                        .id(ID_COLLECTION_1)
                        .child(ID_PROPERTY_3)
                        .build())
                .build();
        assertEquals(input, expected);
    }


    @Test
    public void testComplex() throws SerializationException, JSONException, IOException, ValueMappingException {
        Object input = AASFull.SUBMODEL_3;
        Path expected = Path.builder()
                .id("TestSubmodel3")
                .child("ExampleRelationshipElement")
                .child("ExampleAnnotatedRelationshipElement")
                .child("ExampleOperation")
                .child("ExampleCapability")
                .child("ExampleBasicEvent")
                .child(Path.builder()
                        .id("ExampleSubmodelCollectionOrdered")
                        .child("ExampleProperty")
                        .child("ExampleMultiLanguageProperty")
                        .child("ExampleRange")
                        .build())
                .child(Path.builder()
                        .id("ExampleSubmodelCollectionUnordered")
                        .child("ExampleBlob")
                        .child("ExampleFile")
                        .child("ExampleReferenceElement")
                        .build())
                .build();
        assertEquals(input, expected);
    }


    private void assertEquals(Object obj, Path path, Level level) throws SerializationException, JsonProcessingException, JSONException {
        String actual = serializer.write(obj, level);
        String expected = new ObjectMapper().writeValueAsString(path.getPaths());
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }


    private void assertEquals(Object obj, Path path) throws SerializationException, JsonProcessingException, JSONException {
        assertEquals(obj, path.asCorePath(), Level.CORE);
        assertEquals(obj, path, Level.DEEP);
    }

}
