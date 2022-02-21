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

import de.fraunhofer.iosb.ilt.faaast.service.model.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.valuedata.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.valuedata.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.valuedata.values.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.fixture.PropertyValues;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeExtractor;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueMapper;
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

    JsonDeserializer deserializer = new JsonDeserializer();

    @Test
    public void testAnnotatedRelationshipElementProperty() throws DeserializationException, FileNotFoundException, IOException {
        compareValue(PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT, PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT_FILE);
    }


    @Test
    public void testBlob() throws DeserializationException, FileNotFoundException, IOException {
        compareValue(PropertyValues.BLOB, PropertyValues.BLOB_FILE_WITH_BLOB);
    }


    @Test
    public void testFile() throws DeserializationException, FileNotFoundException, IOException {
        compareValue(PropertyValues.FILE, PropertyValues.FILE_FILE);
    }


    @Test
    public void testList() throws DeserializationException, FileNotFoundException, IOException {
        compareValueList(Map.of(
                PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE,
                PropertyValues.PROPERTY_INT, PropertyValues.PROPERTY_INT_FILE,
                PropertyValues.PROPERTY_DOUBLE, PropertyValues.PROPERTY_DOUBLE_FILE));

        compareValueList(Map.of(
                PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE,
                PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT, PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT_FILE,
                PropertyValues.ENTITY, PropertyValues.ENTITY_FILE));
    }


    @Test
    public void testArray() throws DeserializationException, FileNotFoundException, IOException {
        compareValueArray(Map.of(
                PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE,
                PropertyValues.PROPERTY_INT, PropertyValues.PROPERTY_INT_FILE,
                PropertyValues.PROPERTY_DOUBLE, PropertyValues.PROPERTY_DOUBLE_FILE));

        compareValueArray(Map.of(
                PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE,
                PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT, PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT_FILE,
                PropertyValues.ENTITY, PropertyValues.ENTITY_FILE));
    }


    @Test
    public void testMap() throws DeserializationException, FileNotFoundException, IOException {
        compareValueMap(Map.of(
                PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE,
                PropertyValues.PROPERTY_INT, PropertyValues.PROPERTY_INT_FILE,
                PropertyValues.PROPERTY_DOUBLE, PropertyValues.PROPERTY_DOUBLE_FILE));

        compareValueMap(Map.of(
                PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE,
                PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT, PropertyValues.ANNOTATED_RELATIONSHIP_ELEMENT_FILE,
                PropertyValues.ENTITY, PropertyValues.ENTITY_FILE));
    }


    private String filesAsJsonArray(Map<SubmodelElement, File> input) {
        return input.entrySet().stream()
                .map(x -> {
                    try {
                        return TestUtils.extractValueJson(x.getValue(), x.getKey());
                    }
                    catch (IOException ex) {
                        Logger.getLogger(JsonDeserializerTest.class.getName()).log(Level.SEVERE, null, ex);
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
                                TestUtils.extractValueJson(x.getValue(), x.getKey()));
                    }
                    catch (IOException ex) {
                        Logger.getLogger(JsonDeserializerTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return null;
                })
                .collect(Collectors.joining(",", "{", "}"));
    }


    private void compareValueList(Map<SubmodelElement, File> input) throws DeserializationException, IOException {
        List<Object> expected = input.keySet().stream().map(x -> ElementValueMapper.toValue(x))
                .collect(Collectors.toList());
        TypeInfo typeInfo = TypeExtractor.extractTypeInfo(input.keySet());
        List<ElementValue> actual = deserializer.readValueList(filesAsJsonArray(input), typeInfo);
        Assert.assertEquals(expected, actual);
    }


    private void compareValueMap(Map<SubmodelElement, File> input) throws DeserializationException, IOException {
        Map<Object, ElementValue> expected = input.keySet().stream().collect(Collectors.toMap(
                x -> x.getIdShort(),
                x -> ElementValueMapper.toValue(x)));
        TypeInfo typeInfo = TypeExtractor.extractTypeInfo(input.keySet().stream().collect(Collectors.toMap(
                x -> x.getIdShort(),
                x -> x)));
        Map<Object, ElementValue> actual = deserializer.readValueMap(filesAsJsonObject(input), typeInfo);
        Assert.assertEquals(expected, actual);
    }


    private void compareValueArray(Map<SubmodelElement, File> input) throws DeserializationException, IOException {
        Object[] expected = input.keySet().stream().map(x -> ElementValueMapper.toValue(x))
                .toArray();
        TypeInfo typeInfo = TypeExtractor.extractTypeInfo(input.keySet().toArray());
        ElementValue[] actual = deserializer.readValueArray(filesAsJsonArray(input), typeInfo);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testMultiLanguageProperty() throws DeserializationException, FileNotFoundException, IOException {
        compareValue(PropertyValues.MULTI_LANGUAGE_PROPERTY, PropertyValues.MULTI_LANGUAGE_PROPERTY_FILE);
    }


    @Test
    public void testProperty() throws DeserializationException, FileNotFoundException, IOException {
        compareValue(PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE);
        compareValue(PropertyValues.PROPERTY_STRING, PropertyValues.PROPERTY_STRING_FILE, PropertyValue.class, Datatype.String);
        compareValue(PropertyValues.PROPERTY_DOUBLE, PropertyValues.PROPERTY_DOUBLE_FILE);
        compareValue(PropertyValues.PROPERTY_DOUBLE, PropertyValues.PROPERTY_DOUBLE_FILE, PropertyValue.class, Datatype.Double);
        compareValue(PropertyValues.PROPERTY_INT, PropertyValues.PROPERTY_INT_FILE);
        compareValue(PropertyValues.PROPERTY_INT, PropertyValues.PROPERTY_INT_FILE, PropertyValue.class, Datatype.Int);
    }


    @Test
    public void testRange() throws DeserializationException, FileNotFoundException, IOException {
        compareValue(PropertyValues.RANGE_DOUBLE, PropertyValues.RANGE_DOUBLE_FILE);
        compareValue(PropertyValues.RANGE_DOUBLE, PropertyValues.RANGE_DOUBLE_FILE, RangeValue.class, Datatype.Double);
        compareValue(PropertyValues.RANGE_INT, PropertyValues.RANGE_INT_FILE);
        compareValue(PropertyValues.RANGE_INT, PropertyValues.RANGE_INT_FILE, RangeValue.class, Datatype.Int);
    }


    @Test
    public void testReferenceElementProperty() throws DeserializationException, FileNotFoundException, IOException {
        compareValue(PropertyValues.REFERENCE_ELEMENT_MODEL, PropertyValues.REFERENCE_ELEMENT_MODEL_FILE);
        compareValue(PropertyValues.REFERENCE_ELEMENT_GLOBAL, PropertyValues.REFERENCE_ELEMENT_GLOBAL_FILE);
    }


    @Test
    public void testRelationshipElementProperty() throws DeserializationException, FileNotFoundException, IOException {
        compareValue(PropertyValues.RELATIONSHIP_ELEMENT, PropertyValues.RELATIONSHIP_ELEMENT_FILE);
    }


    @Test
    public void testSubmodelElementCollection() throws DeserializationException, FileNotFoundException, IOException {
        compareValue(PropertyValues.ELEMENT_COLLECTION, PropertyValues.ELEMENT_COLLECTION_FILE);
    }


    private void compareValue(SubmodelElement element, File file) throws DeserializationException, IOException {
        ElementValue expected = ElementValueMapper.toValue(element);
        TypeInfo typeInfo = TypeExtractor.extractTypeInfo(element);
        ElementValue actual = deserializer.readValue(TestUtils.extractValueJson(file, element), typeInfo);
        Assert.assertEquals(expected, actual);
    }


    private void compareValue(SubmodelElement element, File file, Class<? extends ElementValue> type, Datatype datatype) throws DeserializationException, IOException {
        ElementValue expected = ElementValueMapper.toValue(element);
        ElementValue actual = deserializer.readValue(TestUtils.extractValueJson(file, element), type, datatype);
        deserializer.readValue(file, type, datatype);
        Assert.assertEquals(expected, actual);
    }

}
