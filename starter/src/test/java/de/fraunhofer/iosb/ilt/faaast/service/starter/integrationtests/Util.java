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
package de.fraunhofer.iosb.ilt.faaast.service.starter.integrationtests;

import static de.fraunhofer.iosb.ilt.faaast.service.starter.integrationtests.IntegrationTestHttpEndpoint.messageBus;
import static de.fraunhofer.iosb.ilt.faaast.service.starter.integrationtests.IntegrationTestHttpEndpoint.subscriptionIds;

import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionId;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import io.adminshell.aas.v3.dataformat.DeserializationException;
import io.adminshell.aas.v3.dataformat.SerializationException;
import io.adminshell.aas.v3.dataformat.json.JsonDeserializer;
import io.adminshell.aas.v3.dataformat.json.JsonSerializer;
import io.adminshell.aas.v3.model.Referable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;


public class Util {

    public static <T extends Referable> T deepCopy(Referable referable, Class<T> outputClass) {
        try {
            Referable deepCopy = new JsonDeserializer().readReferable(new JsonSerializer().write(referable), outputClass);
            if (deepCopy.getClass().isAssignableFrom(outputClass)) {
                return (T) deepCopy;
            }
        }
        catch (SerializationException | DeserializationException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }


    public static <T> List<T> getListCall(String url, Class<T> clazz) {
        try {
            HttpResponse response = getListCall(url);
            return retrieveResourceFromResponseList(response, clazz);
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
        return null;
    }


    public static HttpResponse getListCall(String url) {
        try {
            return getHttpResponse(url);
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
        return null;
    }


    public static HttpResponse getCall(String url) {
        try {
            return getHttpResponse(url);
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
        return null;
    }


    public static <T> T getCall(String url, Class<T> clazz) {
        try {
            HttpResponse response = getCall(url);
            return retrieveResourceFromResponse(response, clazz);
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
        return null;
    }


    public static <T> T postCall(String url, T payload, Class<T> clazz) {
        try {
            HttpResponse response = postCall(url, payload);
            return retrieveResourceFromResponse(response, clazz);
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
        return null;
    }


    public static <T> HttpResponse postCall(String url, T payload) {
        try {
            HttpPost request = new HttpPost(url);
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(
                    new ByteArrayInputStream(new de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonSerializer().write(payload).getBytes(StandardCharsets.UTF_8)));
            request.setEntity(entity);
            return HttpClientBuilder.create().build().execute(request);
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
        return null;
    }


    public static <T> T putCall(String url, T payload, Class<T> clazz) {
        try {
            HttpResponse response = putCall(url, payload);
            return retrieveResourceFromResponse(response, clazz);
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
        return null;
    }


    public static HttpResponse putCall(String url, Object payload) {
        try {
            HttpPut request = new HttpPut(url);
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(
                    new ByteArrayInputStream(new de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonSerializer().write(payload).getBytes(StandardCharsets.UTF_8)));
            request.setEntity(entity);
            return HttpClientBuilder.create().build().execute(request);
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
        return null;
    }


    public static <T> T deleteCall(String url, Class<T> clazz) {
        try {
            HttpResponse response = deleteCall(url);
            return retrieveResourceFromResponse(response, clazz);
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
        return null;
    }


    public static HttpResponse deleteCall(String url) {
        try {
            HttpDelete request = new HttpDelete(url);
            return HttpClientBuilder.create().build().execute(request);
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
        return null;
    }


    public static HttpResponse getHttpResponse(String url) throws IOException {
        HttpUriRequest request = new HttpGet(url);
        return HttpClientBuilder.create().build().execute(request);
    }


    public static <T> T retrieveResourceFromResponse(HttpResponse response, Class<T> clazz)
            throws IOException, de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException {

        String jsonFromResponse = EntityUtils.toString(response.getEntity());
        de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonDeserializer deserializer = new de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonDeserializer();
        return deserializer.read(jsonFromResponse, clazz);
    }


    public static <T> List<T> retrieveResourceFromResponseList(HttpResponse response, Class<T> clazz)
            throws IOException, de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException {

        String jsonFromResponse = EntityUtils.toString(response.getEntity());
        de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonDeserializer deserializer = new de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonDeserializer();
        return deserializer.readList(jsonFromResponse, clazz);
    }


    public static void setUpEventCheck(Referable expected, Class<? extends EventMessage> clazz, Supplier<?> call) {
        AtomicBoolean fired = new AtomicBoolean(false);
        SubscriptionId subscriptionId = messageBus.subscribe(SubscriptionInfo.create(clazz, x -> {
            if (ElementReadEventMessage.class.isAssignableFrom(x.getClass())) {
                Assert.assertEquals(expected, ((ElementReadEventMessage) x).getValue());
                fired.set(true);
            }
            if (ElementCreateEventMessage.class.isAssignableFrom(x.getClass())) {
                Assert.assertEquals(expected, ((ElementCreateEventMessage) x).getValue());
                fired.set(true);
            }
            if (ElementUpdateEventMessage.class.isAssignableFrom(x.getClass())) {
                Assert.assertEquals(expected, ((ElementUpdateEventMessage) x).getValue());
                fired.set(true);
            }
            if (ElementDeleteEventMessage.class.isAssignableFrom(x.getClass())) {
                Assert.assertEquals(expected, ((ElementDeleteEventMessage) x).getValue());
                fired.set(true);
            }

        }));
        subscriptionIds.add(subscriptionId);
        call.get();
        Assert.assertTrue(fired.get());
    }

}
