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
package de.fraunhofer.iosb.ilt.faaast.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultAssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.RegistryException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.Reference;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FaaastRegistryHandler {
    private static final String REGISTRY_BASE_PATH = "/registry/shell-descriptors";
    private static final Logger LOGGER = LoggerFactory.getLogger(FaaastRegistryHandler.class);

    private final MessageBus messageBus;
    private final Persistence persistence;
    private final CoreConfig coreConfig;
    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    private final AssetAdministrationShellEnvironment aasEnv;

    FaaastRegistryHandler(MessageBus messageBus, Persistence persistence, CoreConfig coreConfig) throws MessageBusException {
        this.messageBus = messageBus;
        this.persistence = persistence;
        this.coreConfig = coreConfig;
        httpClient = HttpClient.newBuilder().build();
        messageBus.subscribe(SubscriptionInfo.create(ElementCreateEventMessage.class, this::handleCreateEvent));
        messageBus.subscribe(SubscriptionInfo.create(ElementUpdateEventMessage.class, this::handleChangeEvent));
        messageBus.subscribe(SubscriptionInfo.create(ElementDeleteEventMessage.class, this::handleDeleteEvent));
        aasEnv = persistence.getEnvironment();

        try {
            createAllAasInRegistry();
        }
        catch (RegistryException e) {
            LOGGER.error(String.format("Synchronisation with Fa³st-Registry failed: %s", e.getMessage()), e);
        }
    }


    private void createAllAasInRegistry() throws RegistryException {
        if (aasEnv == null || aasEnv.getAssetAdministrationShells().isEmpty())
            return;

        for (AssetAdministrationShell aas: aasEnv.getAssetAdministrationShells()) {
            createAasInRegistry(aas);
        }

    }


    /**
     * Used to delete all shells when service is shut down.
     *
     * @throws RegistryException
     */
    public void deleteAllAasInRegistry() throws RegistryException {
        if (aasEnv == null || aasEnv.getAssetAdministrationShells().isEmpty())
            return;

        for (AssetAdministrationShell aas: aasEnv.getAssetAdministrationShells()) {
            AssetAdministrationShellDescriptor descriptor = DefaultAssetAdministrationShellDescriptor.builder().from(aas).build();
            deleteAasInRegistry(descriptor.getIdentification().getIdentifier());
        }
    }


    protected void handleCreateEvent(ElementCreateEventMessage eventMessage) {
        if (referenceIsAas(eventMessage.getElement())) {
            String identifier = eventMessage.getElement().getKeys().get(0).getValue();
            try {
                //TODO check if there is a race condition
                createAasInRegistry(getAasFromIdentifier(identifier));
            }
            catch (RegistryException | IllegalArgumentException e) {
                LOGGER.error(String.format("Synchronisation with Fa³st-Registry failed: %s", e.getMessage()), e);
            }
        }
    }


    protected void handleChangeEvent(ElementUpdateEventMessage eventMessage) {
        if (referenceIsAas(eventMessage.getElement())) {
            String identifier = eventMessage.getElement().getKeys().get(0).getValue();
            try {
                updateAasInRegistry(getAasFromIdentifier(identifier));
            }
            catch (RegistryException | IllegalArgumentException e) {
                LOGGER.error(String.format("Synchronisation with Fa³st-Registry failed: %s", e.getMessage()), e);
            }
        }
    }


    protected void handleDeleteEvent(ElementDeleteEventMessage eventMessage) {
        if (referenceIsAas(eventMessage.getElement())) {
            String identifier = eventMessage.getElement().getKeys().get(0).getValue();
            try {
                deleteAasInRegistry(identifier);
            }
            catch (RegistryException e) {
                LOGGER.error(String.format("Synchronisation with Fa³st-Registry failed: %s", e.getMessage()), e);
            }
        }
    }


    private boolean referenceIsAas(Reference reference) {
        return !reference.getKeys().isEmpty() && reference.getKeys().get(0).getType() == KeyElements.ASSET_ADMINISTRATION_SHELL;
    }


    private void createAasInRegistry(AssetAdministrationShell aas) throws RegistryException {
        AssetAdministrationShellDescriptor descriptor = DefaultAssetAdministrationShellDescriptor.builder().from(aas).build();

        String body;
        URL url;
        try {
            body = mapper.writeValueAsString(descriptor);
            url = new URL("HTTP", coreConfig.getRegistryHost(), coreConfig.getRegistryPort(), REGISTRY_BASE_PATH);
        }
        catch (JsonProcessingException | MalformedURLException e) {
            throw new RegistryException(e);
        }

        try {
            java.net.http.HttpResponse<String> response = execute(
                    url,
                    "",
                    "POST",
                    HttpRequest.BodyPublishers.ofString(body),
                    java.net.http.HttpResponse.BodyHandlers.ofString(),
                    null);

            if (!is2xxSuccessful(response)) {
                throw new RegistryException(String.format("HTTP request failed with %d", response.statusCode()));
            }
        }
        catch (Exception e) {
            throw new RegistryException("Connection to FA³ST-Registry failed!");
        }
    }


    private void updateAasInRegistry(AssetAdministrationShell aas) throws RegistryException {
        String body;
        URL url;

        AssetAdministrationShellDescriptor descriptor = DefaultAssetAdministrationShellDescriptor.builder().from(aas).build();
        String aasIdentifier = descriptor.getIdentification().getIdentifier();

        try {
            body = mapper.writeValueAsString(descriptor);
            url = new URL("HTTP", coreConfig.getRegistryHost(), coreConfig.getRegistryPort(),
                    REGISTRY_BASE_PATH + "/" + Base64.getEncoder().encodeToString(aasIdentifier.getBytes()));
        }
        catch (MalformedURLException | JsonProcessingException e) {
            throw new RegistryException(e);
        }

        try {
            java.net.http.HttpResponse<String> response = execute(
                    url,
                    "",
                    "PUT",
                    HttpRequest.BodyPublishers.ofString(body),
                    java.net.http.HttpResponse.BodyHandlers.ofString(),
                    null);

            if (!is2xxSuccessful(response)) {
                throw new RegistryException(String.format("HTTP request failed with %d", response.statusCode()));
            }
        }
        catch (Exception e) {
            throw new RegistryException("Connection to FA³ST-Registry failed!");
        }
    }


    private void deleteAasInRegistry(String aasIdentifier) throws RegistryException {
        URL url;
        try {
            url = new URL("HTTP", coreConfig.getRegistryHost(), coreConfig.getRegistryPort(),
                    REGISTRY_BASE_PATH + "/" + Base64.getEncoder().encodeToString(aasIdentifier.getBytes()));
        }
        catch (MalformedURLException e) {
            throw new RegistryException(e);
        }

        try {
            java.net.http.HttpResponse<String> response = execute(
                    url,
                    "",
                    "DELETE",
                    HttpRequest.BodyPublishers.noBody(),
                    java.net.http.HttpResponse.BodyHandlers.ofString(),
                    null);

            if (!is2xxSuccessful(response)) {
                throw new RegistryException(String.format("HTTP request failed with %d", response.statusCode()));
            }
        }
        catch (Exception e) {
            throw new RegistryException("Connection to FA³ST-Registry failed!");
        }
    }


    private <T> HttpResponse<T> execute(
                                        URL baseUrl,
                                        String path,
                                        String method,
                                        HttpRequest.BodyPublisher bodyPublisher,
                                        HttpResponse.BodyHandler<T> bodyHandler,
                                        Map<String, String> headers)
            throws URISyntaxException, IOException, InterruptedException {
        Ensure.requireNonNull(httpClient, "client must be non-null");
        Ensure.requireNonNull(baseUrl, "baseUrl must be non-null");
        Ensure.requireNonNull(path, "path must be non-null");
        Ensure.requireNonNull(method, "method must be non-null");
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(new URL(baseUrl, path).toURI());
        String mimeType = "application/json";
        if (!StringUtils.isBlank(mimeType)) {
            builder = builder.header("Content-Type", mimeType);
        }
        if (headers != null) {
            for (var header: headers.entrySet()) {
                builder = builder.header(header.getKey(), header.getValue());
            }
        }
        return httpClient.send(builder.method(method, bodyPublisher).build(), bodyHandler);
    }


    private boolean is2xxSuccessful(HttpResponse<?> response) {
        return response != null && is2xxSuccessful(response.statusCode());
    }


    public static boolean is2xxSuccessful(int statusCode) {
        return statusCode >= 200 && statusCode <= 299;
    }


    private AssetAdministrationShell getAasFromIdentifier(String identifier) throws IllegalArgumentException {
        for (AssetAdministrationShell aas: persistence.getEnvironment().getAssetAdministrationShells()) {
            if (aas.getIdentification().getIdentifier().equals(identifier))
                return aas;
        }
        throw new IllegalArgumentException("Identifier not found!");
    }
}
