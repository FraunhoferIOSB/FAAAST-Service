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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization;

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.MessageType;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;


public class HttpJsonSerializerTest {

    private final HttpJsonSerializer serializer = new HttpJsonSerializer();

    @Test
    public void testEnumsWithCustomNaming() throws SerializationException {
        Assert.assertEquals("\"Error\"", serializer.write(MessageType.ERROR));
    }


    @Test
    public void testResult() throws SerializationException, ParseException, JSONException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Result result = Result.builder()
                .message(Message.builder()
                        .text(HttpStatus.getMessage(404))
                        .messageType(MessageType.ERROR)
                        .code(HttpStatus.getMessage(404))
                        .timestamp(dateFormat.parse("01-01-2022 00:00:00"))
                        .build())
                .success(false)
                .build();
        String actual = serializer.write(result);
        String expected = "{\n"
                + "  \"success\" : false,\n"
                + "  \"messages\" : [ {\n"
                + "    \"messageType\" : \"Error\",\n"
                + "    \"text\" : \"Not Found\",\n"
                + "    \"code\" : \"Not Found\",\n"
                + "    \"timestamp\" : \"2022-01-01T00:00:00.000+00:00\"\n"
                + "  } ]\n"
                + "}";
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }
}
