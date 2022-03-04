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

import static de.fraunhofer.iosb.ilt.faaast.service.starter.integrationtests.Util.*;

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionId;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.AssetInformation;
import io.adminshell.aas.v3.model.AssetKind;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShell;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class IntegrationTestHttpEndpoint {

    static Service service;
    static AssetAdministrationShellEnvironment environment;
    public static MessageBus messageBus;

    private static final String HOST = "http://localhost";
    private static final String PORT = "8080";
    public static final String HTTP_SHELLS = HOST + ":" + PORT + "/shells";
    public static final String HTTP_SUBMODELS = HOST + ":" + PORT + "/submodels";

    @Before
    public void init() throws Exception {
        environment = AASFull.createEnvironment();
        ServiceConfig serviceConfig = new ServiceConfig.Builder()
                .core(new CoreConfig.Builder()
                        .requestHandlerThreadPoolSize(2)
                        .build())
                .persistence(new PersistenceInMemoryConfig())
                .endpoints(List.of(new HttpEndpointConfig()))
                .messageBus(new MessageBusInternalConfig())
                .build();
        service = new Service(serviceConfig);
        messageBus = service.getMessageBus();
        service.setAASEnvironment(environment);
        service.start();
    }


    @After
    public void shutdown() {
        service.stop();
    }


    @Test
    public void testGETShells() throws IOException, DeserializationException {
        List<AssetAdministrationShell> expected = environment.getAssetAdministrationShells();
        HttpResponse response = getListCall(HTTP_SHELLS);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        List<AssetAdministrationShell> actual = retrieveResourceFromResponseList(response, AssetAdministrationShell.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGETShellsWithIdShort() throws IOException, DeserializationException {
        List<AssetAdministrationShell> expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getIdShort()
                        .equalsIgnoreCase(environment.getAssetAdministrationShells().get(0).getIdShort()))
                .collect(Collectors.toList());

        HttpResponse response = getListCall(HTTP_SHELLS + "?idShort=" +
                environment.getAssetAdministrationShells().get(0).getIdShort());
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        List<AssetAdministrationShell> actual = retrieveResourceFromResponseList(response, AssetAdministrationShell.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGETShellsWithAssetIds() throws IOException, DeserializationException {
        String assetIdValue = "https://acplt.org/Test_Asset";
        List<AssetAdministrationShell> expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getAssetInformation().getGlobalAssetId().getKeys().stream()
                        .anyMatch(y -> y.getValue().equalsIgnoreCase(assetIdValue)))
                .collect(Collectors.toList());

        String assetIdsParameter = "[{\"key\": \"globalAssetId\",\"value\":\"" + assetIdValue + "\"}]";
        HttpResponse response = getListCall(HTTP_SHELLS + "?assetIds=" + Base64.getUrlEncoder().encodeToString(assetIdsParameter.getBytes(StandardCharsets.UTF_8)));
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        List<AssetAdministrationShell> actual = retrieveResourceFromResponseList(response, AssetAdministrationShell.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGETShellsEvent() {
        List<AssetAdministrationShell> actual = new ArrayList<>();
        List<AssetAdministrationShell> expected = environment.getAssetAdministrationShells();
        SubscriptionId subscriptionId = messageBus.subscribe(SubscriptionInfo.create(ElementReadEventMessage.class, x -> {
            actual.add((AssetAdministrationShell) x.getValue());
        }));
        getListCall(HTTP_SHELLS, AssetAdministrationShell.class);
        Assert.assertEquals(expected, actual);
        messageBus.unsubscribe(subscriptionId);
    }


    @Test
    public void testPOSTShell() throws IOException, DeserializationException {
        AssetAdministrationShell newShell = getNewShell();
        HttpResponse response = postCall(HTTP_SHELLS, newShell);
        Assert.assertEquals(newShell, retrieveResourceFromResponse(response, AssetAdministrationShell.class));
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

        List<AssetAdministrationShell> actual = getListCall(HTTP_SHELLS, AssetAdministrationShell.class);
        List<AssetAdministrationShell> expected = new ArrayList<>() {
            {
                addAll(environment.getAssetAdministrationShells());
                add(newShell);
            }
        };
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPOSTShellEvent() {
        AssetAdministrationShell newShell = getNewShell();
        setUpEventCheck(newShell, ElementCreateEventMessage.class, () -> postCall(HTTP_SHELLS, newShell, AssetAdministrationShell.class));
    }


    private AssetAdministrationShell getNewShell() {
        return new DefaultAssetAdministrationShell.Builder()
                .identification(new DefaultIdentifier.Builder()
                        .identifier("http://newOne")
                        .idType(IdentifierType.IRI)
                        .build())
                .idShort("newOne")
                .description(new LangString("Täst"))
                .build();
    }


    @Test
    public void testGETSpecificShell() throws IOException, DeserializationException {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        HttpResponse actual = getCall(HTTP_SHELLS + "/"
                + Base64.getUrlEncoder().encodeToString(expected
                        .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8)));

        Assert.assertEquals(expected, retrieveResourceFromResponse(actual, AssetAdministrationShell.class));
        Assert.assertEquals(HttpStatus.SC_OK, actual.getStatusLine().getStatusCode());
    }


    @Test
    public void testGETSpecificShell_NotExists() {
        HttpResponse actual = getCall(HTTP_SHELLS + "/acb123");
        Assert.assertEquals(HttpStatus.SC_NOT_FOUND, actual.getStatusLine().getStatusCode());
    }


    @Test
    public void testGETSpecificShellEvent() {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        setUpEventCheck(expected, ElementReadEventMessage.class, () -> getCall(HTTP_SHELLS + "/"
                + Base64.getUrlEncoder().encodeToString(expected
                        .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8)),
                AssetAdministrationShell.class));
    }


    @Test
    public void testPUTSpecificShell() throws IOException, DeserializationException {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        expected.setIdShort("changed");
        String url = HTTP_SHELLS + "/"
                + Base64.getUrlEncoder().encodeToString(expected
                        .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8));

        HttpResponse response = putCall(url, expected);
        Assert.assertEquals(expected, retrieveResourceFromResponse(response, AssetAdministrationShell.class));
        //TODO: StatusCode of spec seems to be wrong 204
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        AssetAdministrationShell actual = getCall(url, AssetAdministrationShell.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPUTSpecificShellEvent() {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        expected.setIdShort("changed");
        String url = HTTP_SHELLS + "/"
                + Base64.getUrlEncoder().encodeToString(expected
                        .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8));
        setUpEventCheck(expected, ElementUpdateEventMessage.class, () -> putCall(url, expected, AssetAdministrationShell.class));
    }


    @Test
    public void testDELETESpecificShell() throws IOException, DeserializationException {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        String identifier = Base64.getUrlEncoder().encodeToString(expected
                .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8));
        String url = HTTP_SHELLS + "/" + identifier;
        HttpResponse response = deleteCall(url);
        //TODO: StatusCode of spec seems to be wrong 204
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        Assert.assertEquals(0, response.getEntity().getContentLength());

        List<AssetAdministrationShell> actual = getListCall(HTTP_SHELLS, AssetAdministrationShell.class);
        Assert.assertFalse(actual.contains(expected));
    }


    @Test
    public void testDELETESpecificShellEvent() {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        String identifier = Base64.getUrlEncoder().encodeToString(expected
                .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8));
        String url = HTTP_SHELLS + "/" + identifier;
        setUpEventCheck(expected, ElementDeleteEventMessage.class, () -> deleteCall(url));
    }


    @Test
    public void testGET_AASShell() throws IOException, DeserializationException {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        HttpResponse actual = getCall(HTTP_SHELLS + "/"
                + Base64.getUrlEncoder().encodeToString(expected
                        .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8))
                + "/aas");

        Assert.assertEquals(expected, retrieveResourceFromResponse(actual, AssetAdministrationShell.class));
        Assert.assertEquals(HttpStatus.SC_OK, actual.getStatusLine().getStatusCode());
    }


    @Test
    public void testGET_AASShell_Event() {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        String url = HTTP_SHELLS + "/"
                + Base64.getUrlEncoder().encodeToString(expected
                        .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8))
                + "/aas";
        setUpEventCheck(expected, ElementReadEventMessage.class, () -> getCall(url));
    }


    @Test
    public void testGET_AASShell_ContentNormal() throws IOException, DeserializationException {
        call_GET_AASShell_Content("normal", environment.getAssetAdministrationShells().get(1), AssetAdministrationShell.class);
    }


    private void call_GET_AASShell_Content(String content, Object expected, Class<?> expectedClass) throws IOException, DeserializationException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        HttpResponse actual = getCall(HTTP_SHELLS + "/"
                + Base64.getUrlEncoder().encodeToString(aas
                        .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8))
                + "/aas?content=" + content);

        Assert.assertEquals(expected, retrieveResourceFromResponse(actual, expectedClass));
        Assert.assertEquals(HttpStatus.SC_OK, actual.getStatusLine().getStatusCode());
    }


    @Test
    public void testPUT_AASShell() throws IOException, DeserializationException {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        expected.setIdShort("changed");
        String url = HTTP_SHELLS + "/"
                + Base64.getUrlEncoder().encodeToString(expected
                        .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8))
                + "/aas";

        HttpResponse response = putCall(url, expected);
        Assert.assertEquals(expected, retrieveResourceFromResponse(response, AssetAdministrationShell.class));
        //TODO: StatusCode of spec seems to be wrong 204
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        AssetAdministrationShell actual = getCall(url, AssetAdministrationShell.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPUT_AASShellEvent() {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        expected.setIdShort("changed");
        String url = HTTP_SHELLS + "/"
                + Base64.getUrlEncoder().encodeToString(expected
                        .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8))
                + "/aas";
        setUpEventCheck(expected, ElementUpdateEventMessage.class, () -> putCall(url, expected, AssetAdministrationShell.class));
    }


    @Test
    public void testGETAssetInformation() throws IOException, DeserializationException {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        HttpResponse actual = getCall(HTTP_SHELLS + "/"
                + Base64.getUrlEncoder().encodeToString(expected
                        .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8))
                + "/aas/asset-information");

        Assert.assertEquals(expected.getAssetInformation(), retrieveResourceFromResponse(actual, AssetInformation.class));
        Assert.assertEquals(HttpStatus.SC_OK, actual.getStatusLine().getStatusCode());
    }


    @Test
    public void testGETAssetInformationEvent() {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        String url = HTTP_SHELLS + "/"
                + Base64.getUrlEncoder().encodeToString(expected
                        .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8))
                + "/aas/asset-information";
        setUpEventCheck(expected, ElementReadEventMessage.class, () -> getCall(url));
    }


    @Test
    public void testPUTAssetInformation() throws IOException, DeserializationException {
        AssetAdministrationShell aas = environment.getAssetAdministrationShells().get(1);
        AssetInformation expected = aas.getAssetInformation();
        expected.setAssetKind(AssetKind.TYPE);
        String url = HTTP_SHELLS + "/"
                + Base64.getUrlEncoder().encodeToString(aas
                        .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8))
                + "/aas/asset-information";
        HttpResponse response = putCall(url, expected);

        //TODO: StatusCode of spec seems to be wrong 204
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        AssetInformation actual = getCall(url, AssetInformation.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutAssetInformationEvent() {
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(1);
        expected.getAssetInformation().setAssetKind(AssetKind.TYPE);
        String url = HTTP_SHELLS + "/"
                + Base64.getUrlEncoder().encodeToString(expected
                        .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8))
                + "/aas/asset-information";
        setUpEventCheck(expected, ElementUpdateEventMessage.class, () -> putCall(url, expected.getAssetInformation()));
    }


    @Test
    public void testGETSubmodelReferences() throws IOException, DeserializationException {
        List<Reference> expected = environment.getAssetAdministrationShells().get(0).getSubmodels();
        String identifier = Base64.getUrlEncoder().encodeToString(environment.getAssetAdministrationShells().get(0)
                .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8));
        String url = HTTP_SHELLS + "/" + identifier + "/aas/submodels";
        HttpResponse response = getListCall(url);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        List<Reference> actual = retrieveResourceFromResponseList(response, Reference.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGETSubmodelReferencesEvent() {
        String identifier = Base64.getUrlEncoder().encodeToString(environment.getAssetAdministrationShells().get(0)
                .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8));
        String url = HTTP_SHELLS + "/" + identifier + "/aas/submodels";
        setUpEventCheck(environment.getAssetAdministrationShells().get(0), ElementReadEventMessage.class, () -> getListCall(url));
    }


    @Test
    public void testPOSTSubmodelReference() throws IOException, DeserializationException {
        List<Reference> expected = environment.getAssetAdministrationShells().get(0).getSubmodels();
        Reference newReference = new DefaultReference.Builder().key(new DefaultKey.Builder().value("test").idType(KeyType.IRI).build()).build();
        expected.add(newReference);
        String identifier = Base64.getUrlEncoder().encodeToString(environment.getAssetAdministrationShells().get(0)
                .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8));
        String url = HTTP_SHELLS + "/" + identifier + "/aas/submodels";
        HttpResponse response = postCall(url, newReference);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

        List<Reference> actual = getListCall(url, Reference.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPOSTSubmodelReferenceEvent() {
        List<Reference> expected = environment.getAssetAdministrationShells().get(0).getSubmodels();
        Reference newReference = new DefaultReference.Builder().key(new DefaultKey.Builder().value("test").idType(KeyType.IRI).build()).build();
        expected.add(newReference);
        String identifier = Base64.getUrlEncoder().encodeToString(environment.getAssetAdministrationShells().get(0)
                .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8));
        String url = HTTP_SHELLS + "/" + identifier + "/aas/submodels";
        setUpEventCheck(environment.getAssetAdministrationShells().get(0), ElementUpdateEventMessage.class, () -> postCall(url, newReference));
    }


    @Test
    public void testDELETESubmodelReference() throws IOException, DeserializationException {
        List<Reference> expected = environment.getAssetAdministrationShells().get(0).getSubmodels();
        String identifier = Base64.getUrlEncoder().encodeToString(environment.getAssetAdministrationShells().get(0)
                .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8));
        String url = HTTP_SHELLS + "/" + identifier + "/aas/submodels/"
                + Base64.getUrlEncoder().encodeToString(expected.get(0).getKeys().get(0).getValue().getBytes(StandardCharsets.UTF_8));
        HttpResponse response = deleteCall(url);

        //TODO: StatusCode of spec seems to be wrong 204
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        List<Reference> actual = getListCall(HTTP_SHELLS + "/" + identifier + "/aas/submodels", Reference.class);
        Assert.assertFalse(actual.contains(expected.get(0)));
    }


    @Test
    public void testDELETESubmodelReferenceEvent() {
        List<Reference> expected = environment.getAssetAdministrationShells().get(0).getSubmodels();
        String identifier = Base64.getUrlEncoder().encodeToString(environment.getAssetAdministrationShells().get(0)
                .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8));
        String url = HTTP_SHELLS + "/" + identifier + "/aas/submodels/"
                + Base64.getUrlEncoder().encodeToString(expected.get(0).getKeys().get(0).getValue().getBytes(StandardCharsets.UTF_8));
        expected.remove(0);
        setUpEventCheck(environment.getAssetAdministrationShells().get(0), ElementUpdateEventMessage.class, () -> deleteCall(url));
    }


    @Test
    public void testGetSubmodels() throws IOException, DeserializationException {
        HttpResponse response = getListCall(HTTP_SUBMODELS);
        List<Submodel> actual = retrieveResourceFromResponseList(response, Submodel.class);
        List<Submodel> expected = environment.getSubmodels();
        Assert.assertEquals(expected, actual);

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }


    @Test
    public void testGetSubmodelsWithIdShort() {
        Submodel expected = environment.getSubmodels().get(1);
        List<Submodel> actual = getListCall(HTTP_SUBMODELS + "?idShort=" + expected.getIdShort(), Submodel.class);
        Assert.assertEquals(List.of(expected), actual);
    }


    @Test
    public void testGetSubmodelsWithSemanticId() throws SerializationException {
        Submodel expected = environment.getSubmodels().get(1);
        String semnaticId = Base64.getUrlEncoder().encodeToString(new JsonSerializer().write(expected.getSemanticId()).getBytes(StandardCharsets.UTF_8));
        List<Submodel> actual = getListCall(HTTP_SUBMODELS + "?semanticId=" + semnaticId, Submodel.class);
        Assert.assertEquals(List.of(expected), actual);
    }


    @Test
    public void testGetSubmodelsEvent() throws SerializationException {
        Submodel expected = environment.getSubmodels().get(1);
        String semnaticId = Base64.getUrlEncoder().encodeToString(new JsonSerializer().write(expected.getSemanticId()).getBytes(StandardCharsets.UTF_8));
        setUpEventCheck(expected, ElementReadEventMessage.class, () -> getListCall(HTTP_SUBMODELS + "?semanticId=" + semnaticId));
    }


    @Test
    public void testPOSTSubmodels() throws IOException, DeserializationException {
        Submodel expected = new DefaultSubmodel.Builder()
                .identification(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.IRI)
                        .identifier("newSubmodel")
                        .build())
                .build();
        HttpResponse response = postCall(HTTP_SUBMODELS, expected);

        Assert.assertEquals(expected, retrieveResourceFromResponse(response, Submodel.class));
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());

        Assert.assertTrue(retrieveResourceFromResponseList(getListCall(HTTP_SUBMODELS), Submodel.class).contains(expected));
    }


    @Test
    public void testPostSubmodelsEvent() throws SerializationException {
        Submodel expected = new DefaultSubmodel.Builder()
                .identification(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.IRI)
                        .identifier("newTestSubmodel")
                        .build())
                .build();
        setUpEventCheck(expected, ElementCreateEventMessage.class, () -> postCall(HTTP_SUBMODELS, expected));
    }


    @Test
    public void testGetSpecificSubmodel() throws SerializationException, IOException, DeserializationException {
        Submodel expected = environment.getSubmodels().get(1);
        String identifier = Base64.getUrlEncoder().encodeToString(expected.getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8));
        String url = HTTP_SUBMODELS + "/" + identifier + "/submodel";
        HttpResponse response = getCall(url);
        Submodel actual = retrieveResourceFromResponse(response, Submodel.class);

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetSpecificSubmodelContent() throws SerializationException, IOException, DeserializationException {
        Submodel expected = environment.getSubmodels().get(3);
        String identifier = Base64.getUrlEncoder().encodeToString(expected.getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8));
        String url = HTTP_SUBMODELS + "/" + identifier + "/submodel?content=normal";
        HttpResponse response = getCall(url);
        Submodel actual = retrieveResourceFromResponse(response, Submodel.class);

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        Assert.assertEquals(expected, actual);
        String finalUrl = url;
        setUpEventCheck(expected, ElementReadEventMessage.class, () -> getCall(finalUrl));

        //TODO: Fix value serialization of DefaultOperation
        /*
         * url = HTTP_SUBMODELS + "/" + identifier + "/submodel?content=value";
         * response = getCall(url);
         * Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
         * Assert.assertEquals(new JsonSerializer().write(expected, new
         * OutputModifier.Builder().content(Content.Value).build()),
         * EntityUtils.toString(response.getEntity()));
         */
    }


    @Test
    public void testGetSpecificSubmodelEvent() throws SerializationException {
        Submodel expected = environment.getSubmodels().get(1);
        String identifier = Base64.getUrlEncoder().encodeToString(expected.getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8));
        setUpEventCheck(expected, ElementReadEventMessage.class, () -> getCall(HTTP_SUBMODELS + "/" + identifier + "/submodel"));
    }


    @Test
    public void testGetSpecificSubmodelLevel() throws SerializationException, IOException, DeserializationException {
        Submodel expected = environment.getSubmodels().get(2);
        String identifier = Base64.getUrlEncoder().encodeToString(expected.getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8));
        String baseUrl = HTTP_SUBMODELS + "/" + identifier + "/submodel";

        //Level = deep
        String url = baseUrl + "?level=deep";
        HttpResponse response = getCall(url);
        Submodel actual = retrieveResourceFromResponse(response, Submodel.class);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        Assert.assertEquals(expected, actual);
        String finalUrl = url;
        setUpEventCheck(expected, ElementReadEventMessage.class, () -> getCall(finalUrl));

        //Level = core
        url = baseUrl + "?level=core";
        response = getCall(url);
        actual = retrieveResourceFromResponse(response, Submodel.class);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        Assert.assertNotEquals(expected, actual);
        Assert.assertTrue(((SubmodelElementCollection) actual.getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase("ExampleSubmodelCollectionOrdered"))
                .findFirst()
                .get()).getValues().size() == 0);
        String finalUrl2 = url;
        SubmodelElementCollection submodelElementCollection = ((SubmodelElementCollection) expected.getSubmodelElements().get(5));
        submodelElementCollection.setValues(null);
        submodelElementCollection = ((SubmodelElementCollection) expected.getSubmodelElements().get(6));
        submodelElementCollection.setValues(null);
        setUpEventCheck(expected, ElementReadEventMessage.class, () -> getCall(finalUrl2));
    }


    @Test
    public void testDELETESubmodelElement() throws IOException, DeserializationException {
        Submodel expected = environment.getSubmodels().get(0);
        String identifier = Base64.getUrlEncoder().encodeToString(expected
                .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8));
        String url = HTTP_SUBMODELS + "/" + identifier + "/submodel/submodel-elements/" + expected.getSubmodelElements().get(0).getIdShort();
        HttpResponse response = deleteCall(url);
        //TODO: StatusCode of spec seems to be wrong 204
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        Assert.assertEquals(0, response.getEntity().getContentLength());

        List<SubmodelElement> actual = getListCall(HTTP_SUBMODELS + "/" + identifier + "/submodel/submodel-elements", SubmodelElement.class);
        Assert.assertFalse(actual.contains(expected.getSubmodelElements().get(0)));
    }


    @Test
    public void testDELETESubmodelElementEvent() {
        Submodel expected = environment.getSubmodels().get(0);
        String identifier = Base64.getUrlEncoder().encodeToString(expected
                .getIdentification().getIdentifier().getBytes(StandardCharsets.UTF_8));
        String url = HTTP_SUBMODELS + "/" + identifier + "/submodel/submodel-elements/" + expected.getSubmodelElements().get(0).getIdShort();
        setUpEventCheck(expected.getSubmodelElements().get(0), ElementDeleteEventMessage.class, () -> deleteCall(url));
    }

}
