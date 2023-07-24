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
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonEventDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.fixture.EventValues;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;


public class JsonEventDeserializerTest {

    private final JsonEventDeserializer deserializer = new JsonEventDeserializer();

    @Test
    public void testElementReadEventMessage() throws SerializationException, DeserializationException, JSONException, IOException {
        assertEquals(EventValues.ELEMENT_READ_EVENT, EventValues.ELEMENT_READ_EVENT_FILE);
    }


    @Test
    public void testOperationFinishEventMessage() throws SerializationException, DeserializationException, ValueFormatException, ValueMappingException, JSONException, IOException {
        assertEquals(EventValues.OPERATION_FINISH_EVENT, EventValues.OPERATION_FINISH_EVENT_FILE);
    }


    @Test
    public void testOperationInvokeEventMessage() throws SerializationException, DeserializationException, ValueFormatException, ValueMappingException, JSONException, IOException {
        assertEquals(EventValues.OPERATION_INVOKE_EVENT, EventValues.OPERATION_INVOKE_EVENT_FILE);
    }


    @Test
    public void testValueReadEventMessage() throws SerializationException, DeserializationException, JSONException, IOException {
        assertEquals(EventValues.VALUE_READ_EVENT, EventValues.VALUE_READ_EVENT_FILE);
    }


    @Test
    public void testElementCreateEventMessage() throws SerializationException, DeserializationException, ValueFormatException, JSONException, IOException {
        assertEquals(EventValues.ELEMENT_CREATE_EVENT, EventValues.ELEMENT_CREATE_EVENT_FILE);
    }


    @Test
    public void testElementDeleteEventMessage() throws SerializationException, DeserializationException, ValueFormatException, JSONException, IOException {
        assertEquals(EventValues.ELEMENT_DELETE_EVENT, EventValues.ELEMENT_DELETE_EVENT_FILE);
    }


    @Test
    public void testElementUpdateEventMessage() throws SerializationException, DeserializationException, ValueFormatException, JSONException, IOException {
        assertEquals(EventValues.ELEMENT_UPDATE_EVENT, EventValues.ELEMENT_UPDATE_EVENT_FILE);
    }


    @Test
    public void testValueChangeEventMessage() throws SerializationException, DeserializationException, ValueFormatException, JSONException, IOException {
        assertEquals(EventValues.VALUE_CHANGE_EVENT, EventValues.VALUE_CHANGE_EVENT_FILE);
    }


    @Test
    public void testErrorEventMessage() throws SerializationException, DeserializationException, ValueFormatException, JSONException, IOException {
        assertEquals(EventValues.ERROR_EVENT, EventValues.ERROR_EVENT_FILE);
    }


    private void assertEquals(EventMessage msg, String file) throws JSONException, IOException, SerializationException, DeserializationException {
        Assert.assertEquals(msg, deserializer.read(Files.readString(Paths.get(file))));
    }

}
