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

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.fixture.PropertyValues;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueMapper;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
        TypeContext context = TypeContext.fromElement(element);
        ElementValue actual = deserializer.readValue(TestUtils.extractValueJson(file, element), context);
        Assert.assertEquals(expected, actual);
    }


    private void compareValue(SubmodelElement element, File file, Class<? extends ElementValue> type, Datatype datatype) throws DeserializationException, IOException {
        ElementValue expected = ElementValueMapper.toValue(element);
        ElementValue actual = deserializer.readValue(TestUtils.extractValueJson(file, element), type, datatype);
        deserializer.readValue(file, type, datatype);
        Assert.assertEquals(expected, actual);
    }

}
