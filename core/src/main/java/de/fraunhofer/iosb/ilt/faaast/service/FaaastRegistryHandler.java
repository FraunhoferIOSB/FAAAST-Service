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


/**
 * Class to handle the synchronisation of assetAdministrationShells and submodels
 * with the Fa³st Registry.
 */
public class FaaastRegistryHandler {
    private static final String SYNC_ERROR_FORMAT_STRING = "Synchronisation with Fa³st-Registry failed: %s";
    private static final String REGISTRY_CONNECTION_ERROR = "Connection to FA³ST-Registry failed!";
    private static final String REQUEST_ERROR_FORMAT_STRING = "HTTP request failed with %d";
    private static final String REQUEST_INTERRUPTION_ERROR = "HTTP request failed";
    private static final Logger LOGGER = LoggerFactory.getLogger(FaaastRegistryHandler.class);

    private final Persistence persistence;
    private final CoreConfig coreConfig;
    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    private final AssetAdministrationShellEnvironment aasEnv;

    FaaastRegistryHandler(MessageBus messageBus, Persistence persistence, CoreConfig coreConfig) throws MessageBusException {
        this.persistence = persistence;
        this.coreConfig = coreConfig;
        httpClient = HttpClient.newBuilder().build();
        messageBus.subscribe(SubscriptionInfo.create(ElementCreateEventMessage.class, m -> {
            try {
                handleCreateEvent(m);
            }
            catch (Exception e) {
                LOGGER.error(String.format(SYNC_ERROR_FORMAT_STRING, e.getMessage()), e);
            }
        }));
        messageBus.subscribe(SubscriptionInfo.create(ElementUpdateEventMessage.class, m -> {
            try {
                handleChangeEvent(m);
            }
            catch (Exception e) {
                LOGGER.error(String.format(SYNC_ERROR_FORMAT_STRING, e.getMessage()), e);
            }
        }));
        messageBus.subscribe(SubscriptionInfo.create(ElementDeleteEventMessage.class, m -> {
            try {
                handleDeleteEvent(m);
            }
            catch (Exception e) {
                LOGGER.error(String.format(SYNC_ERROR_FORMAT_STRING, e.getMessage()), e);
            }
        }));
        aasEnv = persistence.getEnvironment();

        try {
            createAllAasInRegistry();
        }
        catch (Exception e) {
            LOGGER.error(String.format(SYNC_ERROR_FORMAT_STRING, e.getMessage()), e);
        }
    }


    private void createAllAasInRegistry() throws RegistryException, InterruptedException {
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
    public void deleteAllAasInRegistry() throws RegistryException, InterruptedException {
        if (aasEnv == null || aasEnv.getAssetAdministrationShells().isEmpty())
            return;

        for (AssetAdministrationShell aas: aasEnv.getAssetAdministrationShells()) {
            AssetAdministrationShellDescriptor descriptor = DefaultAssetAdministrationShellDescriptor.builder().from(aas).build();
            deleteAasInRegistry(descriptor.getIdentification().getIdentifier());
        }
    }


    /**
     * Sends the request for creating new aas or submodels in the registry.
     *
     * @param eventMessage Event that signals the creation of an element.
     * @throws RegistryException
     */
    protected void handleCreateEvent(ElementCreateEventMessage eventMessage) throws RegistryException, InterruptedException {
        if (referenceIsAas(eventMessage.getElement())) {
            String identifier = eventMessage.getElement().getKeys().get(0).getValue();
            createAasInRegistry(getAasFromIdentifier(identifier));
        }
    }


    /**
     * Sends the request for updating an aas or submodel in the registry.
     *
     * @param eventMessage Event that signals the update of an element.
     * @throws RegistryException
     */
    protected void handleChangeEvent(ElementUpdateEventMessage eventMessage) throws RegistryException, InterruptedException {
        if (referenceIsAas(eventMessage.getElement())) {
            String identifier = eventMessage.getElement().getKeys().get(0).getValue();
            updateAasInRegistry(getAasFromIdentifier(identifier));
        }
    }


    /**
     * Sends the request for deleting an aas or submodel in the registry.
     *
     * @param eventMessage Event that signals the deletion of an element.
     * @throws RegistryException
     */
    protected void handleDeleteEvent(ElementDeleteEventMessage eventMessage) throws RegistryException, InterruptedException {
        if (referenceIsAas(eventMessage.getElement())) {
            String identifier = eventMessage.getElement().getKeys().get(0).getValue();
            deleteAasInRegistry(identifier);
        }
    }


    private boolean referenceIsAas(Reference reference) {
        return !reference.getKeys().isEmpty() && reference.getKeys().get(0).getType() == KeyElements.ASSET_ADMINISTRATION_SHELL;
    }


    private void createAasInRegistry(AssetAdministrationShell aas) throws RegistryException, InterruptedException {
        AssetAdministrationShellDescriptor descriptor = DefaultAssetAdministrationShellDescriptor.builder().from(aas).build();

        String body;
        URL url;
        try {
            body = mapper.writeValueAsString(descriptor);
            url = new URL("HTTP", coreConfig.getRegistryHost(), coreConfig.getRegistryPort(), coreConfig.getRegistryBasePath());
        }
        catch (JsonProcessingException | MalformedURLException e) {
            throw new RegistryException(e);
        }

        try {
            HttpResponse<String> response = execute(
                    url,
                    "",
                    "POST",
                    HttpRequest.BodyPublishers.ofString(body),
                    HttpResponse.BodyHandlers.ofString(),
                    null);

            if (response == null) {
                throw new RegistryException(REQUEST_INTERRUPTION_ERROR);
            }

            if (!is2xxSuccessful(response)) {
                throw new RegistryException(String.format(REQUEST_ERROR_FORMAT_STRING, response.statusCode()));
            }
        }
        catch (InterruptedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RegistryException(REGISTRY_CONNECTION_ERROR);
        }
    }


    private void updateAasInRegistry(AssetAdministrationShell aas) throws RegistryException, InterruptedException {
        String body;
        URL url;

        AssetAdministrationShellDescriptor descriptor = DefaultAssetAdministrationShellDescriptor.builder().from(aas).build();
        String aasIdentifier = descriptor.getIdentification().getIdentifier();

        try {
            body = mapper.writeValueAsString(descriptor);
            url = new URL("HTTP", coreConfig.getRegistryHost(), coreConfig.getRegistryPort(),
                    coreConfig.getRegistryBasePath() + "/" + Base64.getEncoder().encodeToString(aasIdentifier.getBytes()));
        }
        catch (MalformedURLException | JsonProcessingException e) {
            throw new RegistryException(e);
        }

        try {
            HttpResponse<String> response = execute(
                    url,
                    "",
                    "PUT",
                    HttpRequest.BodyPublishers.ofString(body),
                    HttpResponse.BodyHandlers.ofString(),
                    null);

            if (response == null) {
                throw new RegistryException(REQUEST_INTERRUPTION_ERROR);
            }

            if (!is2xxSuccessful(response)) {
                throw new RegistryException(String.format(REQUEST_ERROR_FORMAT_STRING, response.statusCode()));
            }
        }
        catch (InterruptedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RegistryException(REGISTRY_CONNECTION_ERROR);
        }
    }


    private void deleteAasInRegistry(String aasIdentifier) throws RegistryException, InterruptedException {
        URL url;
        try {
            url = new URL("HTTP", coreConfig.getRegistryHost(), coreConfig.getRegistryPort(),
                    coreConfig.getRegistryBasePath() + "/" + Base64.getEncoder().encodeToString(aasIdentifier.getBytes()));
        }
        catch (MalformedURLException e) {
            throw new RegistryException(e);
        }

        try {
            HttpResponse<String> response = execute(
                    url,
                    "",
                    "DELETE",
                    HttpRequest.BodyPublishers.noBody(),
                    HttpResponse.BodyHandlers.ofString(),
                    null);

            if (response == null) {
                throw new RegistryException(REQUEST_INTERRUPTION_ERROR);
            }

            if (!is2xxSuccessful(response)) {
                throw new RegistryException(String.format(REQUEST_ERROR_FORMAT_STRING, response.statusCode()));
            }
        }
        catch (InterruptedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RegistryException(REGISTRY_CONNECTION_ERROR);
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


    private static boolean is2xxSuccessful(int statusCode) {
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
