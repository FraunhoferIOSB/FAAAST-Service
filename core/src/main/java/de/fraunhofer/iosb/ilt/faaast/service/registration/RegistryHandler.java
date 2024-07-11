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
package de.fraunhofer.iosb.ilt.faaast.service.registration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.EndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.Endpoint;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.AbstractIdentifiableDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultAssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultEndpoint;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultProtocolInformation;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl.DefaultSubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.RegistryException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.SslHelper;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to handle the synchronisation of assetAdministrationShells and submodels
 * with the Registry.
 */
public class RegistryHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryHandler.class);
    private static final String SYNC_EXCEPTION = "Synchronisation with Registry exception: %s";
    private static final String SYNC_EVENT_ERROR = "Synchronisation of changes with Registry failed.";
    private static final String CONFLICT_ERROR = "Synchronisation with registry failed - reason: conflict (id: %s)";
    private static final String AAS_URL_PATH = "/api/v3.0/shell-descriptors";
    private static final String SUBMODEL_URL_PATH = "/api/v3.0/submodel-descriptors";

    private final Persistence<?> persistence;
    private final CoreConfig coreConfig;
    private final List<EndpointConfig> endpointConfigs;
    private HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    private final String aasInterface = "AAS-3.0";

    public RegistryHandler(MessageBus messageBus, Persistence persistence, ServiceConfig serviceConfig) throws MessageBusException {
        this.persistence = persistence;
        this.coreConfig = serviceConfig.getCore();
        this.endpointConfigs = serviceConfig.getEndpoints();
        try {
            httpClient = SslHelper.newClientAcceptingAllCertificates();
        }
        catch (KeyManagementException | NoSuchAlgorithmException e) {
            LOGGER.error(String.format(SYNC_EXCEPTION, e.getMessage()), e);
        }

        messageBus.subscribe(SubscriptionInfo.create(ElementCreateEventMessage.class, LambdaExceptionHelper.wrap(this::handleCreateEvent)));
        messageBus.subscribe(SubscriptionInfo.create(ElementUpdateEventMessage.class, LambdaExceptionHelper.wrap(this::handleChangeEvent)));
        messageBus.subscribe(SubscriptionInfo.create(ElementDeleteEventMessage.class, LambdaExceptionHelper.wrap(this::handleDeleteEvent)));

        LOGGER.info("Registering FA³ST Service in Registry");
        try {
            persistence.getAllAssetAdministrationShells(OutputModifier.DEFAULT, PagingInfo.ALL)
                    .getContent()
                    .forEach(x -> createIdentifiableInRegistries(getAasDescriptor(x, endpointConfigs), coreConfig.getAasRegistryBasePath()));
        }
        catch (RegistryException | InterruptedException e) {
            LOGGER.error(String.format(SYNC_EXCEPTION, e.getMessage()), e);
            Thread.currentThread().interrupt();
        }
    }


    /**
     * Used to delete all shells when service is shut down.
     *
     * @throws RegistryException
     * @throws java.lang.InterruptedException
     */
    public void deleteAllAasInRegistry() throws RegistryException, InterruptedException {
        persistence.getAllAssetAdministrationShells(OutputModifier.DEFAULT, PagingInfo.ALL)
                .getContent()
                .forEach(LambdaExceptionHelper.rethrowConsumer(x -> deleteIdentifiableInRegistry(x.getId(), coreConfig.getAasRegistryBasePath())));
        if (persistence.getAllAssetAdministrationShells(OutputModifier.DEFAULT, PagingInfo.ALL).getContent().isEmpty())
            return;
    }


    /**
     * Sends the request for creating new aas or submodels in the registry.
     *
     * @param eventMessage Event that signals the creation of an element.
     * @throws RegistryException
     */
    protected void handleCreateEvent(ElementCreateEventMessage eventMessage) throws RegistryException, InterruptedException {
        String identifier = eventMessage.getElement().getKeys().get(0).getValue();
        if (referenceIsAas(eventMessage.getElement())) {
            // TODO use persistence.getAssetAdministrationShell(identifier, QueryModifier.MINIMAL) instead of fetching all AASs
            createIdentifiableInRegistries(getAasDescriptor(getAasFromIdentifier(identifier), endpointConfigs), coreConfig.getAasRegistryBasePath());
        }
        else if (referenceIsSubmodel(eventMessage.getElement())) {
            AbstractIdentifiableDescriptor descriptor = DefaultSubmodelDescriptor.builder()
                    .from(getSubmodelFromIdentifier(identifier))
                    .build();
            createIdentifiableInRegistries(descriptor, coreConfig.getSubmodelRegistryBasePath());
        }
    }


    /**
     * Sends the request for updating an aas or submodel in the registry.
     *
     * @param eventMessage Event that signals the update of an element.
     */
    protected void handleChangeEvent(ElementUpdateEventMessage eventMessage) throws InterruptedException {
        String identifier = eventMessage.getElement().getKeys().get(0).getValue();
        if (referenceIsAas(eventMessage.getElement())) {
            updateIdentifiableInRegistry(identifier, getAasDescriptor(getAasFromIdentifier(identifier), endpointConfigs), coreConfig.getAasRegistryBasePath());
        }
        else if (referenceIsSubmodel(eventMessage.getElement())) {
            AbstractIdentifiableDescriptor descriptor = DefaultSubmodelDescriptor.builder()
                    .from(getSubmodelFromIdentifier(identifier))
                    .build();
            updateIdentifiableInRegistry(identifier, descriptor, coreConfig.getSubmodelRegistryBasePath());
        }
    }


    /**
     * Sends the request for deleting an aas or submodel in the registry.
     *
     * @param eventMessage Event that signals the deletion of an element.
     */
    protected void handleDeleteEvent(ElementDeleteEventMessage eventMessage) throws InterruptedException {
        String identifier = eventMessage.getElement().getKeys().get(0).getValue();
        if (referenceIsAas(eventMessage.getElement())) {
            deleteIdentifiableInRegistry(identifier, coreConfig.getAasRegistryBasePath());
        }
        else if (referenceIsSubmodel(eventMessage.getElement())) {
            deleteIdentifiableInRegistry(identifier, coreConfig.getSubmodelRegistryBasePath());
        }
    }


    private boolean referenceIsAas(Reference reference) {
        return !reference.getKeys().isEmpty() && reference.getKeys().get(0).getType().equals(KeyTypes.ASSET_ADMINISTRATION_SHELL);
    }


    private boolean referenceIsSubmodel(Reference reference) {
        return !reference.getKeys().isEmpty() && reference.getKeys().get(0).getType().equals(KeyTypes.SUBMODEL);
    }


    private static boolean is2xxSuccessful(int statusCode) {
        return statusCode >= 200 && statusCode <= 299;
    }


    private void createAasInRegistries(AssetAdministrationShellDescriptor descriptor) throws RegistryException, InterruptedException {
        createIdentifiableInRegistries(descriptor, coreConfig.getAasRegistries());
    }


    private void createIdentifiableInRegistries(AbstractIdentifiableDescriptor descriptor, List<String> registries) throws RegistryException {
        for (String registry: registries) {
            try {

                HttpResponse<String> response = execute(
                        new URL(registry),
                        AAS_URL_PATH,
                        "POST",
                        HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(descriptor)),
                        HttpResponse.BodyHandlers.ofString(),
                        null);
                if (response.statusCode() == 409) {
                    LOGGER.warn(String.format(CONFLICT_ERROR, descriptor.getId()));
                }
                if (!is2xxSuccessful(response.statusCode())) {
                    LOGGER.warn(String.format(SYNC_EVENT_ERROR));
                }
            }
            catch (URISyntaxException | IOException e) {
                LOGGER.error(String.format(SYNC_EXCEPTION, e.getMessage()), e);
            }
        }

    }


    private void updateIdentifiableInRegistry(String identifier, AbstractIdentifiableDescriptor descriptor, String basePath) throws InterruptedException {
        try {
            HttpResponse<String> response = execute(
                    new URL(this.protocol, coreConfig.getRegistryHost(), coreConfig.getRegistryPort(),
                            basePath + "/" + Base64.getEncoder().encodeToString(identifier.getBytes())),
                    "",
                    "PUT",
                    HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(descriptor)),
                    HttpResponse.BodyHandlers.ofString(),
                    null);
            if (Objects.isNull(response) || !is2xxSuccessful(response.statusCode())) {
                LOGGER.warn(String.format(SYNC_EVENT_ERROR));
            }
        }
        catch (URISyntaxException | IOException e) {
            LOGGER.error(String.format(SYNC_EXCEPTION, e.getMessage()), e);
        }
    }


    private void deleteIdentifiableInRegistry(String identifier, String basePath) throws InterruptedException {
        try {
            HttpResponse<String> response = execute(
                    new URL(this.protocol, coreConfig.getRegistryHost(), coreConfig.getRegistryPort(),
                            basePath + "/" + Base64.getEncoder().encodeToString(identifier.getBytes())),
                    "",
                    "DELETE",
                    HttpRequest.BodyPublishers.noBody(),
                    HttpResponse.BodyHandlers.ofString(),
                    null);
            if (Objects.isNull(response) || !is2xxSuccessful(response.statusCode())) {
                LOGGER.warn(String.format(SYNC_EVENT_ERROR));
            }
        }
        catch (URISyntaxException | IOException e) {
            LOGGER.error(String.format(SYNC_EXCEPTION, e.getMessage()), e);
        }
    }


    private <T> HttpResponse<T> execute(
                                        URL baseUrl,
                                        String path,
                                        String method,
                                        HttpRequest.BodyPublisher bodyPublisher,
                                        HttpResponse.BodyHandler<T> bodyHandler,
                                        Map<String, String> headers)
            throws URISyntaxException, IOException {
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
        try {
            return httpClient.send(builder.method(method, bodyPublisher).build(), bodyHandler);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }


    private AbstractIdentifiableDescriptor getAasDescriptor(AssetAdministrationShell aas, List<EndpointConfig> endpointConfigs) {
        return DefaultAssetAdministrationShellDescriptor.builder()
                .from(aas)
                .submodels(getSubmodelDescriptorsFromAas(aas))
                .endpoints(createEndpoints(endpointConfigs))
                .build();
    }


    private List<Endpoint> createEndpoints(List<EndpointConfig> endpointConfigs) {
        List<Endpoint> endpoints = new ArrayList<>();
        endpointConfigs.stream().forEach(c -> endpoints.add(DefaultEndpoint.builder()
                ._interface(aasInterface)
                .protocolInformation(DefaultProtocolInformation.builder()
                        .endpointProtocol(determineProtocol(c))
                        .href(getLocalHostAddress())
                        .build())
                .build()));
        return endpoints;
    }


    private String getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e) {
            return "localhost";
        }
    }


    //@TODO: determine protocol from config class type but not imported through MVN
    private String determineProtocol(EndpointConfig config) {
        if (config.getClass().toString().contains("http")) {
            return "HTTP";
        }
        else if (config.getClass().toString().contains("opc")) {
            return "OPC UA";
        }
        else {
            return "";
        }
    }


    private AssetAdministrationShell getAasFromIdentifier(String identifier) throws IllegalArgumentException {
        return getAasList().stream()
                .filter(a -> a.getId().equals(identifier))
                .findFirst()
                .get();
    }


    private List<SubmodelDescriptor> getSubmodelDescriptorsFromAas(AssetAdministrationShell aas) {
        List<SubmodelDescriptor> submodelDescriptors = new ArrayList<>();
        for (Reference submodelReference: aas.getSubmodels())
            try {
                submodelDescriptors.add(DefaultSubmodelDescriptor.builder()
                        .from(getSubmodelFromIdentifier(submodelReference.getKeys().get(0).getValue()))
                        .endpoints(createEndpoints(endpointConfigs))
                        .build());
            }
            catch (Exception e) {
                LOGGER.error(String.format(SYNC_EXCEPTION, e.getMessage()), e);
            }
        return submodelDescriptors;
    }


    private Submodel getSubmodelFromIdentifier(String identifier) throws ResourceNotFoundException {
        return persistence.getSubmodel(identifier, QueryModifier.MINIMAL);
    }
}
