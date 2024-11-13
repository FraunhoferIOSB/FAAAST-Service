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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.fixture.ValueOnlyExamples;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.util.ValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeExtractor;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.junit.Assert;
import org.junit.Test;


public class JsonDeserializerTest {

    private final JsonApiDeserializer deserializer = new JsonApiDeserializer();

    @Test
    public void testAnnotatedRelationshipElementProperty() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(ValueOnlyExamples.ANNOTATED_RELATIONSHIP_ELEMENT, ValueOnlyExamples.ANNOTATED_RELATIONSHIP_ELEMENT_FILE);
    }


    @Test
    public void testArray() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValueArray(Map.of(ValueOnlyExamples.PROPERTY_STRING, ValueOnlyExamples.PROPERTY_STRING_FILE,
                ValueOnlyExamples.PROPERTY_INT, ValueOnlyExamples.PROPERTY_INT_FILE,
                ValueOnlyExamples.PROPERTY_DOUBLE, ValueOnlyExamples.PROPERTY_DOUBLE_FILE));

        assertValueArray(Map.of(ValueOnlyExamples.PROPERTY_STRING, ValueOnlyExamples.PROPERTY_STRING_FILE,
                ValueOnlyExamples.ANNOTATED_RELATIONSHIP_ELEMENT, ValueOnlyExamples.ANNOTATED_RELATIONSHIP_ELEMENT_FILE,
                ValueOnlyExamples.ENTITY, ValueOnlyExamples.ENTITY_FILE));
    }


    @Test
    public void testBlob() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(ValueOnlyExamples.BLOB, ValueOnlyExamples.BLOB_FILE_WITH_BLOB);
    }


    @Test
    public void testFile() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(ValueOnlyExamples.FILE, ValueOnlyExamples.FILE_FILE);
    }


    @Test
    public void testList() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValueList(Map.of(ValueOnlyExamples.PROPERTY_STRING, ValueOnlyExamples.PROPERTY_STRING_FILE,
                ValueOnlyExamples.PROPERTY_INT, ValueOnlyExamples.PROPERTY_INT_FILE,
                ValueOnlyExamples.PROPERTY_DOUBLE, ValueOnlyExamples.PROPERTY_DOUBLE_FILE));

        assertValueList(Map.of(ValueOnlyExamples.PROPERTY_STRING, ValueOnlyExamples.PROPERTY_STRING_FILE,
                ValueOnlyExamples.ANNOTATED_RELATIONSHIP_ELEMENT, ValueOnlyExamples.ANNOTATED_RELATIONSHIP_ELEMENT_FILE,
                ValueOnlyExamples.ENTITY, ValueOnlyExamples.ENTITY_FILE));
    }


    @Test
    public void testMap() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValueMap(Map.of(ValueOnlyExamples.PROPERTY_STRING, ValueOnlyExamples.PROPERTY_STRING_FILE,
                ValueOnlyExamples.PROPERTY_INT, ValueOnlyExamples.PROPERTY_INT_FILE,
                ValueOnlyExamples.PROPERTY_DOUBLE, ValueOnlyExamples.PROPERTY_DOUBLE_FILE));

        assertValueMap(Map.of(ValueOnlyExamples.PROPERTY_STRING, ValueOnlyExamples.PROPERTY_STRING_FILE,
                ValueOnlyExamples.ANNOTATED_RELATIONSHIP_ELEMENT, ValueOnlyExamples.ANNOTATED_RELATIONSHIP_ELEMENT_FILE,
                ValueOnlyExamples.ENTITY, ValueOnlyExamples.ENTITY_FILE));
    }


    @Test
    public void testPage() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValuePage(Map.of(ValueOnlyExamples.PROPERTY_STRING, ValueOnlyExamples.PROPERTY_STRING_FILE,
                ValueOnlyExamples.PROPERTY_INT, ValueOnlyExamples.PROPERTY_INT_FILE,
                ValueOnlyExamples.PROPERTY_DOUBLE, ValueOnlyExamples.PROPERTY_DOUBLE_FILE));

        assertValuePage(Map.of(ValueOnlyExamples.PROPERTY_STRING, ValueOnlyExamples.PROPERTY_STRING_FILE,
                ValueOnlyExamples.ANNOTATED_RELATIONSHIP_ELEMENT, ValueOnlyExamples.ANNOTATED_RELATIONSHIP_ELEMENT_FILE,
                ValueOnlyExamples.ENTITY, ValueOnlyExamples.ENTITY_FILE));
    }


    @Test
    public void testMultiLanguageProperty() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(ValueOnlyExamples.MULTI_LANGUAGE_PROPERTY, ValueOnlyExamples.MULTI_LANGUAGE_PROPERTY_FILE);
    }


    @Test
    public void testProperty() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(ValueOnlyExamples.PROPERTY_STRING, ValueOnlyExamples.PROPERTY_STRING_FILE);
        assertValue(ValueOnlyExamples.PROPERTY_STRING, ValueOnlyExamples.PROPERTY_STRING_FILE, PropertyValue.class, Datatype.STRING);
        assertValue(ValueOnlyExamples.PROPERTY_DOUBLE, ValueOnlyExamples.PROPERTY_DOUBLE_FILE);
        assertValue(ValueOnlyExamples.PROPERTY_DOUBLE, ValueOnlyExamples.PROPERTY_DOUBLE_FILE, PropertyValue.class, Datatype.DOUBLE);
        assertValue(ValueOnlyExamples.PROPERTY_INT, ValueOnlyExamples.PROPERTY_INT_FILE);
        assertValue(ValueOnlyExamples.PROPERTY_INT, ValueOnlyExamples.PROPERTY_INT_FILE, PropertyValue.class, Datatype.INT);
        assertValue(ValueOnlyExamples.PROPERTY_DATETIME, ValueOnlyExamples.PROPERTY_DATETIME_FILE);
        assertValue(ValueOnlyExamples.PROPERTY_DATETIME, ValueOnlyExamples.PROPERTY_DATETIME_FILE, PropertyValue.class, Datatype.DATE_TIME);
    }


    @Test
    public void testRange() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(ValueOnlyExamples.RANGE_DOUBLE, ValueOnlyExamples.RANGE_DOUBLE_FILE);
        assertValue(ValueOnlyExamples.RANGE_DOUBLE, ValueOnlyExamples.RANGE_DOUBLE_FILE, RangeValue.class, Datatype.DOUBLE);
        assertValue(ValueOnlyExamples.RANGE_INT, ValueOnlyExamples.RANGE_INT_FILE);
        assertValue(ValueOnlyExamples.RANGE_INT, ValueOnlyExamples.RANGE_INT_FILE, RangeValue.class, Datatype.INT);
    }


    @Test
    public void testReferenceElementProperty() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(ValueOnlyExamples.REFERENCE_ELEMENT_MODEL, ValueOnlyExamples.REFERENCE_ELEMENT_MODEL_FILE);
        assertValue(ValueOnlyExamples.REFERENCE_ELEMENT_GLOBAL, ValueOnlyExamples.REFERENCE_ELEMENT_GLOBAL_FILE);
    }


    @Test
    public void testRelationshipElementProperty() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(ValueOnlyExamples.RELATIONSHIP_ELEMENT, ValueOnlyExamples.RELATIONSHIP_ELEMENT_FILE);
    }


    @Test
    public void testSubmodelElementCollection() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(ValueOnlyExamples.SUBMODEL_ELEMENT_COLLECTION, ValueOnlyExamples.SUBMODEL_ELEMENT_COLLECTION_FILE);
    }


    @Test
    public void testSubmodelElementList() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(ValueOnlyExamples.SUBMODEL_ELEMENT_LIST_SIMPLE, ValueOnlyExamples.SUBMODEL_ELEMENT_LIST_SIMPLE_FILE);
        assertValue(ValueOnlyExamples.SUBMODEL_ELEMENT_LIST, ValueOnlyExamples.SUBMODEL_ELEMENT_LIST_FILE);
    }


    @Test
    public void testInvokeOperationRequestSync() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        ServiceContext serviceContext = mock(ServiceContext.class);
        when(serviceContext.execute(any(), any()))
                .thenReturn(GetSubmodelElementByPathResponse.builder()
                        .payload(ValueOnlyExamples.CONTEXT_OPERATION_INVOKE)
                        .success()
                        .build());
        InvokeOperationRequest expected = ValueOnlyExamples.INVOKE_OPERATION_SYNC_REQUEST;

        InvokeOperationSyncRequest actual = deserializer.readValueOperationRequest(
                ValueOnlyExamples.INVOKE_OPERATION_REQUEST_FILE,
                InvokeOperationSyncRequest.class,
                serviceContext,
                SubmodelElementIdentifier.builder()
                        .submodelId("http://example.org/submodels/1")
                        .idShortPath(IdShortPath.parse("my.test.operation"))
                        .build());
        Assert.assertEquals(expected, actual);
    }


    private void assertValue(SubmodelElement element, File file) throws DeserializationException, IOException, ValueMappingException {
        ElementValue expected = ElementValueMapper.toValue(element);
        TypeInfo typeInfo = TypeExtractor.extractTypeInfo(element);
        ElementValue actual = deserializer.readValue(ValueHelper.extractValueJson(file, element), typeInfo);
        Assert.assertEquals(expected, actual);
    }


    private void assertValue(SubmodelElement element, File file, Class<? extends ElementValue> type, Datatype datatype)
            throws DeserializationException, IOException, ValueMappingException {
        ElementValue expected = ElementValueMapper.toValue(element);
        ElementValue actual = deserializer.readValue(ValueHelper.extractValueJson(file, element), type, datatype);
        deserializer.readValue(file, type, datatype);
        Assert.assertEquals(expected, actual);
    }


    private void assertValueArray(Map<SubmodelElement, File> input) throws DeserializationException, IOException, ValueMappingException {
        Object[] expected = input.keySet().stream()
                .map(LambdaExceptionHelper.rethrowFunction(x -> ElementValueMapper.toValue(x)))
                .toArray();
        TypeInfo typeInfo = TypeExtractor.extractTypeInfo(input.keySet().toArray());
        ElementValue[] actual = deserializer.readValueArray(filesAsJsonArray(input), typeInfo);
        Assert.assertArrayEquals(expected, actual);
    }


    private void assertValueList(Map<SubmodelElement, File> input) throws DeserializationException, IOException, ValueMappingException {
        List<ElementValue> expected = input.keySet().stream()
                .map(LambdaExceptionHelper.rethrowFunction(x -> ElementValueMapper.toValue(x)))
                .collect(Collectors.toList());
        TypeInfo typeInfo = TypeExtractor.extractTypeInfo(input.keySet());
        List<ElementValue> actual = deserializer.readValueList(filesAsJsonArray(input), typeInfo);
        Assert.assertEquals(expected, actual);
    }


    private void assertValuePage(Map<SubmodelElement, File> input) throws DeserializationException, IOException, ValueMappingException {
        Page<ElementValue> expected = Page.of(
                input.keySet().stream()
                        .map(LambdaExceptionHelper.rethrowFunction(x -> ElementValueMapper.toValue(x)))
                        .collect(Collectors.toList()));
        TypeInfo typeInfo = TypeExtractor.extractTypeInfo(input.keySet());
        Page<ElementValue> actual = deserializer.readValuePage(filesAsJsonPage(input), typeInfo);
        Assert.assertEquals(expected, actual);
    }


    private void assertValueMap(Map<SubmodelElement, File> input) throws DeserializationException, IOException, ValueMappingException {
        Map<Object, ElementValue> expected = input.keySet().stream().collect(Collectors.toMap(
                x -> x.getIdShort(),
                LambdaExceptionHelper.rethrowFunction(x -> ElementValueMapper.toValue(x))));
        TypeInfo typeInfo = TypeExtractor.extractTypeInfo(input.keySet().stream().collect(Collectors.toMap(
                x -> x.getIdShort(),
                x -> x)));
        Map<Object, ElementValue> actual = deserializer.readValueMap(filesAsJsonObject(input), typeInfo);
        Assert.assertEquals(expected, actual);
    }


    private String filesAsJsonPage(Map<SubmodelElement, File> input) {
        return String.format("{\"result\": %s}", filesAsJsonArray(input));
    }


    private String filesAsJsonArray(Map<SubmodelElement, File> input) {
        return input.entrySet().stream()
                .map(x -> {
                    try {
                        return ValueHelper.extractValueJson(x.getValue(), x.getKey());
                    }
                    catch (IOException e) {
                        // TODO proper error handling
                        Logger.getLogger(JsonDeserializerTest.class.getName()).log(Level.SEVERE, null, e);
                    }
                    return null;
                })
                .collect(Collectors.joining(",", "[", "]"));
    }


    private String filesAsJsonObject(Map<SubmodelElement, File> input) {
        return input.entrySet().stream()
                .map(x -> {
                    try {
                        return String.format("\"%s\": %s",
                                x.getKey().getIdShort(),
                                ValueHelper.extractValueJson(x.getValue(), x.getKey()));
                    }
                    catch (IOException e) {
                        // TODO proper error handling
                        Logger.getLogger(JsonDeserializerTest.class.getName()).log(Level.SEVERE, null, e);
                    }
                    return null;
                })
                .collect(Collectors.joining(",", "{", "}"));
    }

}
