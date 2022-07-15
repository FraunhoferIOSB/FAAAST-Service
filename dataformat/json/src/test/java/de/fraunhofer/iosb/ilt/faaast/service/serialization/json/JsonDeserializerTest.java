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

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.fixture.PropertyValues;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.util.ValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeExtractor;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;


public class JsonDeserializerTest {

    private final JsonDeserializer deserializer = new JsonDeserializer();

    @Test
    public void testAnnotatedRelationshipElementProperty() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT, PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT_FILE);
    }


    @Test
    public void testBlob() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(PropertyValues.BLOB, PropertyValues.BLOB_FILE_WITH_BLOB);
    }


    @Test
    public void testFile() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(PropertyValues.FILE, PropertyValues.FILE_FILE);
    }


    @Test
    public void testList() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValueList(Map.of(
                PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE,
                PropertyValues.PROPERTY_INT, PropertyValues.PROPERTY_INT_FILE,
                PropertyValues.PROPERTY_DOUBLE, PropertyValues.PROPERTY_DOUBLE_FILE));

        assertValueList(Map.of(
                PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE,
                PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT, PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT_FILE,
                PropertyValues.ENTITY, PropertyValues.ENTITY_FILE));
    }


    @Test
    public void testArray() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValueArray(Map.of(
                PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE,
                PropertyValues.PROPERTY_INT, PropertyValues.PROPERTY_INT_FILE,
                PropertyValues.PROPERTY_DOUBLE, PropertyValues.PROPERTY_DOUBLE_FILE));

        assertValueArray(Map.of(
                PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE,
                PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT, PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT_FILE,
                PropertyValues.ENTITY, PropertyValues.ENTITY_FILE));
    }


    @Test
    public void testMap() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValueMap(Map.of(
                PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE,
                PropertyValues.PROPERTY_INT, PropertyValues.PROPERTY_INT_FILE,
                PropertyValues.PROPERTY_DOUBLE, PropertyValues.PROPERTY_DOUBLE_FILE));

        assertValueMap(Map.of(
                PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE,
                PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT, PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT_FILE,
                PropertyValues.ENTITY, PropertyValues.ENTITY_FILE));
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


    private void assertValueList(Map<SubmodelElement, File> input) throws DeserializationException, IOException, ValueMappingException {
        List<Object> expected = input.keySet().stream()
                .map(LambdaExceptionHelper.rethrowFunction(x -> ElementValueMapper.toValue(x)))
                .collect(Collectors.toList());
        TypeInfo typeInfo = TypeExtractor.extractTypeInfo(input.keySet());
        List<ElementValue> actual = deserializer.readValueList(filesAsJsonArray(input), typeInfo);
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


    private void assertValueArray(Map<SubmodelElement, File> input) throws DeserializationException, IOException, ValueMappingException {
        Object[] expected = input.keySet().stream()
                .map(LambdaExceptionHelper.rethrowFunction(x -> ElementValueMapper.toValue(x)))
                .toArray();
        TypeInfo typeInfo = TypeExtractor.extractTypeInfo(input.keySet().toArray());
        ElementValue[] actual = deserializer.readValueArray(filesAsJsonArray(input), typeInfo);
        Assert.assertArrayEquals(expected, actual);
    }


    @Test
    public void testMultiLanguageProperty() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(PropertyValues.MULTI_LANGUAGE_PROPERTY, PropertyValues.MULTI_LANGUAGE_PROPERTY_FILE);
    }


    @Test
    public void testProperty() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE);
        assertValue(PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE, PropertyValue.class, Datatype.STRING);
        assertValue(PropertyValues.PROPERTY_DOUBLE, PropertyValues.PROPERTY_DOUBLE_FILE);
        assertValue(PropertyValues.PROPERTY_DOUBLE, PropertyValues.PROPERTY_DOUBLE_FILE, PropertyValue.class, Datatype.DOUBLE);
        assertValue(PropertyValues.PROPERTY_INT, PropertyValues.PROPERTY_INT_FILE);
        assertValue(PropertyValues.PROPERTY_INT, PropertyValues.PROPERTY_INT_FILE, PropertyValue.class, Datatype.INT);
        assertValue(PropertyValues.PROPERTY_DATETIME, PropertyValues.PROPERTY_DATETIME_FILE);
        assertValue(PropertyValues.PROPERTY_DATETIME, PropertyValues.PROPERTY_DATETIME_FILE, PropertyValue.class, Datatype.DATE_TIME);
    }


    @Test
    public void testRange() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(PropertyValues.RANGE_DOUBLE, PropertyValues.RANGE_DOUBLE_FILE);
        assertValue(PropertyValues.RANGE_DOUBLE, PropertyValues.RANGE_DOUBLE_FILE, RangeValue.class, Datatype.DOUBLE);
        assertValue(PropertyValues.RANGE_INT, PropertyValues.RANGE_INT_FILE);
        assertValue(PropertyValues.RANGE_INT, PropertyValues.RANGE_INT_FILE, RangeValue.class, Datatype.INT);
    }


    @Test
    public void testReferenceElementProperty() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(PropertyValues.REFERENCE_ELEMENT_MODEL, PropertyValues.REFERENCE_ELEMENT_MODEL_FILE);
        assertValue(PropertyValues.REFERENCE_ELEMENT_GLOBAL, PropertyValues.REFERENCE_ELEMENT_GLOBAL_FILE);
    }


    @Test
    public void testRelationshipElementProperty() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(PropertyValues.RELATIONSHIP_ELEMENT, PropertyValues.RELATIONSHIP_ELEMENT_FILE);
    }


    @Test
    public void testSubmodelElementCollection() throws DeserializationException, FileNotFoundException, IOException, ValueMappingException {
        assertValue(PropertyValues.ELEMENT_COLLECTION, PropertyValues.ELEMENT_COLLECTION_FILE);
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

}
