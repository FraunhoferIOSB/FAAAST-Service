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
package de.fraunhofer.iosb.ilt.faaast.service.registry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultAssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultSubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.SslHelper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to handle the synchronisation of assetAdministrationShells and submodels
 * with the Registry.
 */
public class RegistrySynchronization {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrySynchronization.class);
    private static final String MSG_REGISTER_AAS_FAILED = "Registering AAS descriptor in registry failed (id: %s, registry: %s, reason: %s)";
    private static final String MSG_REGISTER_SUBMODEL_FAILED = "Registering submodel descriptor in registry failed (id: %s, registry: %s, reason: %s)";
    private static final String MSG_UPDATE_AAS_FAILED = "Updating AAS descriptor in registry failed (id: %s, registry: %s, reason: %s)";
    private static final String MSG_UPDATE_SUBMODEL_FAILED = "Updating submodel descriptor in registry failed (id: %s, registry: %s, reason: %s)";
    private static final String MSG_UNREGISTER_AAS_FAILED = "Removing AAS descriptor from registry failed (id: %s, registry: %s, reason: %s)";
    private static final String MSG_UNREGISTER_SUBMODEL_FAILED = "Removing submodel descriptor from registry failed (id: %s, registry: %s, reason: %s)";
    private static final String MSG_AAS_NOT_FOUND = "AAS could not be found in persistence";
    private static final String MSG_SUBMODEL_NOT_FOUND = "submodel could not be found in persistence";
    private static final String MSG_BAD_RETURN_CODE = "bad return code %s";

    private static final String AAS_URL_PATH = "/api/v3.0/shell-descriptors";
    private static final String SUBMODEL_URL_PATH = "/api/v3.0/submodel-descriptors";

    private final CoreConfig coreConfig;
    private final Persistence<?> persistence;
    private final MessageBus<?> messageBus;
    private final List<de.fraunhofer.iosb.ilt.faaast.service.endpoint.Endpoint> endpoints;
    private HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    public RegistrySynchronization(
            CoreConfig coreConfig,
            Persistence<?> persistence,
            MessageBus<?> messageBus,
            List<de.fraunhofer.iosb.ilt.faaast.service.endpoint.Endpoint> endpoints) {
        Ensure.requireNonNull(coreConfig, "coreConfig must be non-null");
        Ensure.requireNonNull(persistence, "persistence must be non-null");
        Ensure.requireNonNull(messageBus, "messageBus must be non-null");
        this.coreConfig = coreConfig;
        this.persistence = persistence;
        this.messageBus = messageBus;
        this.endpoints = Optional.ofNullable(endpoints).orElse(List.of());
    }


    /**
     * Starts the synchronization with the registry.
     */
    public void start() {
        if (!synchronizationActive()) {
            return;
        }
        if (!coreConfig.getAasRegistries().isEmpty()) {
            LOGGER.info("Registering all AssetAdministrationShells to the following registries: ");
            printRegistries(coreConfig.getAasRegistries());
        }
        if (!coreConfig.getSubmodelRegistries().isEmpty()) {
            LOGGER.info("Registering all submodels to the following registries: ");
            printRegistries(coreConfig.getSubmodelRegistries());
        }
        try {
            httpClient = SslHelper.newClientAcceptingAllCertificates();
            messageBus.subscribe(SubscriptionInfo.create(ElementCreateEventMessage.class, LambdaExceptionHelper.wrap(this::handleCreateEvent)));
            messageBus.subscribe(SubscriptionInfo.create(ElementUpdateEventMessage.class, LambdaExceptionHelper.wrap(this::handleChangeEvent)));
            messageBus.subscribe(SubscriptionInfo.create(ElementDeleteEventMessage.class, LambdaExceptionHelper.wrap(this::handleDeleteEvent)));
            registerAllAass();
            registerAllSubmodels();
        }
        catch (KeyManagementException | NoSuchAlgorithmException e) {
            LOGGER.warn("Error creating HTTP client for synchronization with registry", e);
        }
        catch (MessageBusException e) {
            LOGGER.warn("Error creating messageBus subscriptions for synchronization with registry", e);
        }

    }


    /**
     * Stops the synchronization with the registry and unregisters all AASs and submodels.
     */
    public void stop() {
        unregisterAllAass();
        unregisterAllSubmodels();
    }


    /**
     * Handles create events received via the messageBus.
     *
     * @param event the received event
     */
    protected void handleCreateEvent(ElementCreateEventMessage event) {
        handleEvent(event, this::registerAas, this::registerSubmodel);
    }


    /**
     * Handles change events received via the messageBus.
     *
     * @param event the received event
     */
    protected void handleChangeEvent(ElementUpdateEventMessage event) {
        handleEvent(event, this::updateAas, this::updateSubmodel);
    }


    /**
     * Handles delete events received via the messageBus.
     *
     * @param event the received event
     */
    protected void handleDeleteEvent(ElementDeleteEventMessage event) {
        handleEvent(event, this::unregisterAas, this::unregisterSubmodel);
    }


    private void handleEvent(ElementChangeEventMessage event, Consumer<String> aasHandler, Consumer<String> submodelHandler) {
        if (Objects.isNull(event)) {
            return;
        }
        Key key = ReferenceHelper.getEffectiveKey(event.getElement());
        if (Objects.isNull(key)) {
            return;
        }
        switch (key.getType()) {
            case ASSET_ADMINISTRATION_SHELL:
                aasHandler.accept(key.getValue());
                break;
            case SUBMODEL:
                submodelHandler.accept(key.getValue());
                break;
        }
    }


    private void registerAllAass() {
        getPageSafe(persistence.getAllAssetAdministrationShells(QueryModifier.MINIMAL, PagingInfo.ALL))
                .getContent()
                .forEach(this::registerAas);
    }


    private void registerAas(AssetAdministrationShell aas) {
        register(AAS_URL_PATH, asDescriptor(aas), aas.getId(), MSG_REGISTER_AAS_FAILED);
    }


    private void registerAas(String id) {
        try {
            registerAas(persistence.getAssetAdministrationShell(id, QueryModifier.MINIMAL));
        }
        catch (ResourceNotFoundException e) {
            LOGGER.warn(String.format(
                    MSG_REGISTER_AAS_FAILED,
                    id,
                    "-",
                    MSG_AAS_NOT_FOUND),
                    e);
        }
    }


    private void unregisterAllAass() {
        getPageSafe(persistence.getAllAssetAdministrationShells(QueryModifier.MINIMAL, PagingInfo.ALL))
                .getContent()
                .forEach(this::unregisterAas);
    }


    private void unregisterAas(AssetAdministrationShell aas) {
        unregister(AAS_URL_PATH, asDescriptor(aas), aas.getId(), MSG_UNREGISTER_AAS_FAILED);
    }


    private void unregisterAas(String id) {
        try {
            unregisterAas(persistence.getAssetAdministrationShell(id, QueryModifier.MINIMAL));
        }
        catch (ResourceNotFoundException e) {
            LOGGER.warn(String.format(
                    MSG_UNREGISTER_AAS_FAILED,
                    id,
                    "-",
                    MSG_AAS_NOT_FOUND),
                    e);
        }
    }


    private void updateAas(AssetAdministrationShell aas) {
        update(AAS_URL_PATH, asDescriptor(aas), aas.getId(), MSG_UPDATE_AAS_FAILED);
    }


    private void updateAas(String id) {
        try {
            updateAas(persistence.getAssetAdministrationShell(id, QueryModifier.MINIMAL));
        }
        catch (ResourceNotFoundException e) {
            LOGGER.warn(String.format(
                    MSG_UPDATE_AAS_FAILED,
                    id,
                    "-",
                    MSG_AAS_NOT_FOUND),
                    e);
        }
    }


    private void registerAllSubmodels() {
        getPageSafe(persistence.getAllSubmodels(QueryModifier.MINIMAL, PagingInfo.ALL))
                .getContent()
                .forEach(this::registerSubmodel);
    }


    private void registerSubmodel(Submodel submodel) {
        register(SUBMODEL_URL_PATH, asDescriptor(submodel), submodel.getId(), MSG_REGISTER_SUBMODEL_FAILED);
    }


    private void registerSubmodel(String id) {
        try {
            registerSubmodel(persistence.getSubmodel(id, QueryModifier.MINIMAL));
        }
        catch (ResourceNotFoundException e) {
            LOGGER.warn(String.format(
                    MSG_REGISTER_SUBMODEL_FAILED,
                    id,
                    "-",
                    MSG_SUBMODEL_NOT_FOUND),
                    e);
        }
    }


    private void unregisterAllSubmodels() {
        getPageSafe(persistence.getAllSubmodels(QueryModifier.MINIMAL, PagingInfo.ALL))
                .getContent()
                .forEach(this::unregisterSubmodel);
    }


    private void unregisterSubmodel(Submodel submodel) {
        unregister(SUBMODEL_URL_PATH, asDescriptor(submodel), submodel.getId(), MSG_UNREGISTER_SUBMODEL_FAILED);
    }


    private void unregisterSubmodel(String id) {
        try {
            unregisterSubmodel(persistence.getSubmodel(id, QueryModifier.MINIMAL));
        }
        catch (ResourceNotFoundException e) {
            LOGGER.warn(String.format(
                    MSG_UNREGISTER_SUBMODEL_FAILED,
                    id,
                    "-",
                    MSG_SUBMODEL_NOT_FOUND),
                    e);
        }
    }


    private void updateSubmodel(Submodel submodel) {
        update(SUBMODEL_URL_PATH, asDescriptor(submodel), submodel.getId(), MSG_UPDATE_SUBMODEL_FAILED);
    }


    private void updateSubmodel(String id) {
        try {
            updateSubmodel(persistence.getSubmodel(id, QueryModifier.MINIMAL));
        }
        catch (ResourceNotFoundException e) {
            LOGGER.warn(String.format(
                    MSG_UPDATE_SUBMODEL_FAILED,
                    id,
                    "-",
                    MSG_SUBMODEL_NOT_FOUND),
                    e);
        }
    }


    private void register(String path, Object payload, String id, String errorMsg) {
        for (String registry: coreConfig.getAasRegistries()) {
            try {
                HttpResponse<String> response = execute("POST", registry, path, payload);
                if (response.statusCode() == 409) {
                    LOGGER.warn(String.format(
                            errorMsg,
                            id,
                            registry,
                            "submodel descriptor already exists"));
                }
                else if (!is2xxSuccessful(response.statusCode())) {
                    LOGGER.warn(String.format(
                            errorMsg,
                            id,
                            registry,
                            String.format(MSG_BAD_RETURN_CODE, response.statusCode())));
                }

            }
            catch (URISyntaxException | IOException | InterruptedException e) {
                LOGGER.warn(String.format(
                        errorMsg,
                        id,
                        registry,
                        e.getMessage()),
                        e);
                if (InterruptedException.class.isInstance(e)) {
                    Thread.currentThread().interrupt();;
                }
            }
        }
    }


    private void update(String path, Object payload, String id, String errorMsg) {
        for (String registry: coreConfig.getAasRegistries()) {
            try {
                HttpResponse<String> response = execute(
                        "PUT",
                        registry,
                        String.format("%s/%s", path, EncodingHelper.base64UrlEncode(id)),
                        payload);
                if (!is2xxSuccessful(response.statusCode())) {
                    LOGGER.warn(String.format(
                            errorMsg,
                            id,
                            registry,
                            String.format(MSG_BAD_RETURN_CODE, response.statusCode())));
                }

            }
            catch (URISyntaxException | IOException | InterruptedException e) {
                LOGGER.warn(String.format(
                        errorMsg,
                        id,
                        registry,
                        e.getMessage()),
                        e);
                if (InterruptedException.class.isInstance(e)) {
                    Thread.currentThread().interrupt();;
                }
            }
        }
    }


    private void unregister(String path, Object payload, String id, String errorMsg) {
        for (String registry: coreConfig.getAasRegistries()) {
            try {
                HttpResponse<String> response = execute(
                        "DELETE",
                        registry,
                        String.format("%s/%s", path, EncodingHelper.base64UrlEncode(id)),
                        payload);
                if (!is2xxSuccessful(response.statusCode())) {
                    LOGGER.warn(String.format(
                            errorMsg,
                            id,
                            registry,
                            String.format(MSG_BAD_RETURN_CODE, response.statusCode())));
                }

            }
            catch (URISyntaxException | IOException | InterruptedException e) {
                LOGGER.warn(String.format(
                        errorMsg,
                        id,
                        registry,
                        e.getMessage()),
                        e);
                if (InterruptedException.class.isInstance(e)) {
                    Thread.currentThread().interrupt();;
                }
            }
        }
    }


    private AssetAdministrationShellDescriptor asDescriptor(AssetAdministrationShell aas) {
        return DefaultAssetAdministrationShellDescriptor.builder()
                .from(aas)
                .submodels(aas.getSubmodels().stream()
                        .map(x -> ReferenceHelper.findFirstKeyType(x, KeyTypes.SUBMODEL))
                        .filter(persistence::submodelExists)
                        .map(LambdaExceptionHelper.wrapFunction(x -> persistence.getSubmodel(x, QueryModifier.MINIMAL)))
                        .map(this::asDescriptor)
                        .toList())
                .endpoints(endpoints.stream()
                        .flatMap(x -> x.getAasEndpointInformation(aas.getId()).stream())
                        .toList())
                .build();
    }


    private SubmodelDescriptor asDescriptor(Submodel submodel) {
        return DefaultSubmodelDescriptor.builder()
                .from(submodel)
                .endpoints(endpoints.stream()
                        .flatMap(x -> x.getSubmodelEndpointInformation(submodel.getId()).stream())
                        .toList())
                .build();
    }


    private HttpResponse<String> execute(String method, String baseUrl, String path, Object payload)
            throws URISyntaxException, IOException, InterruptedException {
        Ensure.requireNonNull(method, "method must be non-null");
        Ensure.requireNonNull(baseUrl, "baseUrl must be non-null");
        Ensure.requireNonNull(path, "path must be non-null");
        Ensure.requireNonNull(payload, "payload must be non-null");
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(new URL(new URL(baseUrl), path).toURI())
                .header("Content-Type", "application/json");
        HttpRequest request = builder.method(method, HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload))).build();
        return httpClient.send(request, BodyHandlers.ofString());
    }


    private boolean synchronizationActive() {
        return !coreConfig.getAasRegistries().isEmpty() || !coreConfig.getSubmodelRegistries().isEmpty();
    }


    private static boolean is2xxSuccessful(int statusCode) {
        return statusCode >= 200 && statusCode <= 299;
    }


    private static <T> Page<T> getPageSafe(Page<T> page) {
        if (Objects.isNull(page) || Objects.isNull(page.getContent())) {
            return Page.empty();
        }
        return page;
    }


    private static void printRegistries(List<String> registries) {
        LOGGER.info(registries.stream()
                .map(x -> String.format("     %s", x))
                .collect(Collectors.joining(System.lineSeparator())));
    }
}