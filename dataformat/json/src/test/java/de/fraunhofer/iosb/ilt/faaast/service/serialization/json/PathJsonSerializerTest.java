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
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.UnsupportedModifierException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.util.Path;
import java.io.IOException;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultBasicEventElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultBlob;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultCapability;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultFile;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;
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
    public void testProperty() throws SerializationException, JSONException, IOException, ValueMappingException, UnsupportedModifierException {
        Object input = new DefaultProperty.Builder()
                .idShort(ID_PROPERTY_1)
                .build();
        Path expected = Path.builder()
                .id(ID_PROPERTY_1)
                .build();
        assertEquals(null, input, expected);
    }


    @Test
    public void testCollection() throws SerializationException, JSONException, IOException, ValueMappingException, UnsupportedModifierException {
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
        assertEquals(null, input, expected);
    }


    @Test
    public void testCollectionWithParent() throws SerializationException, JSONException, IOException, ValueMappingException, UnsupportedModifierException {
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
        assertEquals(
                IdShortPath.builder()
                        .idShort("parent")
                        .build(),
                input,
                List.of("parent.collection1",
                        "parent.collection1.property1",
                        "parent.collection1.collection2"),
                Level.CORE);
        assertEquals(
                IdShortPath.builder()
                        .idShort("parent")
                        .build(),
                input,
                List.of("parent.collection1",
                        "parent.collection1.property1",
                        "parent.collection1.collection2",
                        "parent.collection1.collection2.property2",
                        "parent.collection1.collection2.collection3",
                        "parent.collection1.collection2.collection3.property3"),
                Level.DEEP);
    }


    @Test
    public void testListProperty() throws SerializationException, JSONException, IOException, ValueMappingException, UnsupportedModifierException {
        Object input = new DefaultSubmodelElementList.Builder()
                .idShort(ID_COLLECTION_1)
                .value(new DefaultProperty.Builder()
                        .idShort(ID_PROPERTY_1)
                        .build())
                .value(new DefaultProperty.Builder()
                        .idShort(ID_PROPERTY_2)
                        .build())
                .build();
        Path expected = Path.builder()
                .isList()
                .id(ID_COLLECTION_1)
                .child(ID_PROPERTY_1)
                .child(ID_PROPERTY_2)
                .build();
        assertEquals(null, input, expected);
    }


    @Test
    public void testListComplex() throws SerializationException, JSONException, IOException, ValueMappingException, UnsupportedModifierException {
        Object input = new DefaultSubmodel.Builder()
                .idShort("Submodel")
                .submodelElements(new DefaultProperty.Builder()
                        .idShort("Property1")
                        .build())
                .submodelElements(new DefaultSubmodelElementCollection.Builder()
                        .idShort("Collection1")
                        .value(new DefaultProperty.Builder()
                                .idShort("Property11")
                                .build())
                        .value(new DefaultProperty.Builder()
                                .idShort("Property12")
                                .build())
                        .value(new DefaultSubmodelElementCollection.Builder()
                                .idShort("Collection2")
                                .value(new DefaultProperty.Builder()
                                        .idShort("Property21")
                                        .build())
                                .value(new DefaultProperty.Builder()
                                        .idShort("Property22")
                                        .build())
                                .value(new DefaultSubmodelElementList.Builder()
                                        .idShort("List0")
                                        .value(new DefaultProperty.Builder()
                                                .idShort("Property01")
                                                .build())
                                        .value(new DefaultProperty.Builder()
                                                .idShort("Property02")
                                                .build())
                                        .build())
                                .value(new DefaultSubmodelElementList.Builder()
                                        .idShort("List1")
                                        .value(new DefaultSubmodelElementCollection.Builder()
                                                .idShort("Collection4")
                                                .value(new DefaultProperty.Builder()
                                                        .idShort("Property41")
                                                        .build())
                                                .value(new DefaultProperty.Builder()
                                                        .idShort("Property42")
                                                        .build())
                                                .build())
                                        .value(new DefaultSubmodelElementCollection.Builder()
                                                .idShort("Collection5")
                                                .value(new DefaultProperty.Builder()
                                                        .idShort("Property51")
                                                        .build())
                                                .value(new DefaultProperty.Builder()
                                                        .idShort("Property52")
                                                        .build())
                                                .build())
                                        .build())
                                .value(new DefaultSubmodelElementList.Builder()
                                        .idShort("List2")
                                        .value(new DefaultSubmodelElementList.Builder()
                                                .idShort("List3")
                                                .value(new DefaultProperty.Builder()
                                                        .idShort("Property31")
                                                        .build())
                                                .value(new DefaultProperty.Builder()
                                                        .idShort("Property32")
                                                        .build())
                                                .build())
                                        .value(new DefaultSubmodelElementList.Builder()
                                                .idShort("List4")
                                                .value(new DefaultProperty.Builder()
                                                        .idShort("Property41")
                                                        .build())
                                                .value(new DefaultProperty.Builder()
                                                        .idShort("Property42")
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();
        Path expected = Path.builder()
                .child("Property1")
                .child(Path.builder()
                        .id("Collection1")
                        .child("Property11")
                        .child("Property12")
                        .child(Path.builder()
                                .id("Collection2")
                                .child("Property21")
                                .child("Property22")
                                .child(Path.builder()
                                        .isList()
                                        .id("List0")
                                        .child("Property01")
                                        .child("Property02")
                                        .build())
                                .child(Path.builder()
                                        .isList()
                                        .id("List1")
                                        .child(Path.builder()
                                                .id("Collection4")
                                                .child("Property41")
                                                .child("Property42")
                                                .build())
                                        .child(Path.builder()
                                                .id("Collection5")
                                                .child("Property51")
                                                .child("Property52")
                                                .build())
                                        .build())
                                .child(Path.builder()
                                        .isList()
                                        .id("List2")
                                        .child(Path.builder()
                                                .isList()
                                                .id("List3")
                                                .child("Property31")
                                                .child("Property32")
                                                .build())
                                        .child(Path.builder()
                                                .isList()
                                                .id("List4")
                                                .child("Property41")
                                                .child("Property42")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();
        assertEquals(null, input, expected);
    }


    @Test
    public void testSubmodelEmpty() throws SerializationException, JSONException, IOException, ValueMappingException, UnsupportedModifierException {
        Object input = new DefaultSubmodel.Builder()
                .idShort(ID_SUBMODEL_1)
                .build();
        Path expected = Path.builder()
                .build();
        assertEquals(null, input, expected);
    }


    @Test
    public void testSubmodel() throws SerializationException, JSONException, IOException, ValueMappingException, UnsupportedModifierException {
        Object input = new DefaultSubmodel.Builder()
                .idShort(ID_SUBMODEL_1)
                .submodelElements(new DefaultProperty.Builder()
                        .idShort(ID_PROPERTY_1)
                        .build())
                .submodelElements(new DefaultProperty.Builder()
                        .idShort(ID_PROPERTY_2)
                        .build())
                .submodelElements(new DefaultSubmodelElementCollection.Builder()
                        .idShort(ID_COLLECTION_1)
                        .value(new DefaultProperty.Builder()
                                .idShort(ID_PROPERTY_3)
                                .build())
                        .build())
                .build();
        Path expected = Path.builder()
                .child(ID_PROPERTY_1)
                .child(ID_PROPERTY_2)
                .child(Path.builder()
                        .id(ID_COLLECTION_1)
                        .child(ID_PROPERTY_3)
                        .build())
                .build();
        assertEquals(null, input, expected);
    }


    @Test
    public void testComplex() throws SerializationException, JSONException, IOException, ValueMappingException, JsonProcessingException, UnsupportedModifierException {
        Object input = new DefaultSubmodel.Builder()
                .idShort("TestSubmodel3")
                .id("https://acplt.org/Test_Submodel")
                .submodelElements(new DefaultRelationshipElement.Builder()
                        .idShort("ExampleRelationshipElement")
                        .build())
                .submodelElements(new DefaultAnnotatedRelationshipElement.Builder()
                        .idShort("ExampleAnnotatedRelationshipElement")
                        .annotations(new DefaultProperty.Builder()
                                .idShort("ExampleProperty3")
                                .category("PARAMETER")
                                .value("some example annotation")
                                .valueType(DataTypeDefXsd.STRING)
                                .build())
                        .build())
                .submodelElements(new DefaultOperation.Builder()
                        .idShort("ExampleOperation")
                        .inputVariables(new DefaultOperationVariable.Builder()
                                .value(new DefaultProperty.Builder()
                                        .idShort("ExampleProperty1")
                                        .build())
                                .build())
                        .outputVariables(new DefaultOperationVariable.Builder()
                                .value(new DefaultProperty.Builder()
                                        .idShort("ExampleProperty2")
                                        .build())
                                .build())
                        .inoutputVariables(new DefaultOperationVariable.Builder()
                                .value(new DefaultProperty.Builder()
                                        .idShort("ExampleProperty3")
                                        .build())
                                .build())
                        .build())
                .submodelElements(new DefaultCapability.Builder()
                        .idShort("ExampleCapability")
                        .build())
                .submodelElements(new DefaultBasicEventElement.Builder()
                        .idShort("ExampleBasicEvent")
                        .build())
                .submodelElements(new DefaultSubmodelElementList.Builder()
                        .idShort("ExampleSubmodelElementListOrdered")
                        .orderRelevant(true)
                        .value(new DefaultMultiLanguageProperty.Builder()
                                .idShort("ExampleMultiLanguageProperty1")
                                .build())
                        .value(new DefaultMultiLanguageProperty.Builder()
                                .idShort("ExampleMultiLanguageProperty2")
                                .build())
                        .build())
                .submodelElements(new DefaultSubmodelElementCollection.Builder()
                        .idShort("ExampleSubmodelElementCollection")
                        .value(new DefaultBlob.Builder()
                                .idShort("ExampleBlob")
                                .build())
                        .value(new DefaultFile.Builder()
                                .idShort("ExampleFile")
                                .build())
                        .value(new DefaultReferenceElement.Builder()
                                .idShort("ExampleReferenceElement")
                                .build())
                        .build())
                .build();
        Path expected = Path.builder()
                .child("ExampleRelationshipElement")
                .child("ExampleAnnotatedRelationshipElement")
                .child("ExampleOperation")
                .child("ExampleCapability")
                .child("ExampleBasicEvent")
                .child(Path.builder()
                        .isList()
                        .id("ExampleSubmodelElementListOrdered")
                        .child("ExampleMultiLanguageProperty1")
                        .child("ExampleMultiLanguageProperty2")
                        .build())
                .child(Path.builder()
                        .id("ExampleSubmodelElementCollection")
                        .child("ExampleBlob")
                        .child("ExampleFile")
                        .child("ExampleReferenceElement")
                        .build())
                .build();
        assertEquals(null, input, expected);
    }


    private void assertEquals(IdShortPath parent, Object obj, Path path, Level level)
            throws SerializationException, JsonProcessingException, JSONException, UnsupportedModifierException {
        String actual = serializer.write(parent, obj, level);
        String expected = new ObjectMapper().writeValueAsString(path.getPaths());
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }


    private void assertEquals(IdShortPath parent, Object obj, List<String> expectedPaths, Level level)
            throws SerializationException, JsonProcessingException, JSONException, UnsupportedModifierException {
        String actual = serializer.write(parent, obj, level);
        String expected = new ObjectMapper().writeValueAsString(expectedPaths);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }


    private void assertEquals(IdShortPath parent, Object obj, Path path) throws SerializationException, JsonProcessingException, JSONException, UnsupportedModifierException {
        assertEquals(parent, obj, path.asCorePath(), Level.CORE);
        assertEquals(parent, obj, path, Level.DEEP);
    }

}
