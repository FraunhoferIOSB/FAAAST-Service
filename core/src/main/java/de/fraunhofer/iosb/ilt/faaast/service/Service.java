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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.Endpoint;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.EndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EndpointException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.InternalErrorResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.registry.RegistrySynchronization;
import de.fraunhofer.iosb.ilt.faaast.service.request.RequestHandlerManager;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.DynamicRequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.SubmodelTemplateManager;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.SubmodelTemplateProcessor;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.SubmodelTemplateProcessorConfig;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeExtractor;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.FileHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Central class of the FA³ST Service accumulating and connecting all different components.
 */
public class Service implements ServiceContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);
    private final ServiceConfig config;
    private AssetConnectionManager assetConnectionManager;
    private List<Endpoint> endpoints;
    private MessageBus messageBus;
    private Persistence persistence;
    private FileStorage fileStorage;
    private RequestExecutionContext requestExecutionContext;
    private SubmodelTemplateManager submodelTemplateManager;

    private RegistrySynchronization registrySynchronization;
    private RequestHandlerManager requestHandler;

    /**
     * Creates a new instance of {@link Service}.
     *
     * @param coreConfig core configuration
     * @param persistence persistence implementation
     * @param fileStorage fileStorage implementation
     * @param messageBus message bus implementation
     * @param endpoints endpoints
     * @param assetConnections asset connections
     * @param submodelTemplateProcessors submodel template processor to use
     * @throws IllegalArgumentException if coreConfig is null
     * @throws IllegalArgumentException if persistence is null
     * @throws PersistenceException if storage error occurs
     * @throws MessageBusException if message bus error occurs
     * @throws IllegalArgumentException if messageBus is null
     * @throws RuntimeException if creating a deep copy of aasEnvironment fails
     * @throws ConfigurationException the configuration the {@link AssetConnectionManager} fails
     * @throws AssetConnectionException when initializing asset connections fails
     */
    public Service(CoreConfig coreConfig,
            Persistence persistence,
            FileStorage fileStorage,
            MessageBus messageBus,
            List<Endpoint> endpoints,
            List<AssetConnection> assetConnections,
            List<SubmodelTemplateProcessor> submodelTemplateProcessors) throws ConfigurationException, AssetConnectionException, PersistenceException, MessageBusException {
        Ensure.requireNonNull(coreConfig, "coreConfig must be non-null");
        Ensure.requireNonNull(persistence, "persistence must be non-null");
        Ensure.requireNonNull(messageBus, "messageBus must be non-null");
        if (endpoints == null) {
            this.endpoints = new ArrayList<>();
            LOGGER.warn("no endpoint configuration found, starting service without endpoint which means the service will not be accessible via any kind of API");
        }
        else {
            this.endpoints = endpoints;
        }
        this.config = ServiceConfig.builder()
                .core(coreConfig)
                .build();
        this.persistence = persistence;
        this.fileStorage = fileStorage;
        this.messageBus = messageBus;
        this.assetConnectionManager = new AssetConnectionManager(config.getCore(), assetConnections, this);
        this.requestHandler = new RequestHandlerManager(config.getCore());
        this.requestExecutionContext = new DynamicRequestExecutionContext(this);
        this.registrySynchronization = new RegistrySynchronization(config.getCore(), persistence, messageBus, endpoints);
        this.submodelTemplateManager = new SubmodelTemplateManager(this, submodelTemplateProcessors);
    }


    /**
     * Creates a new instance of {@link Service}.
     *
     * @param config service configuration
     * @throws IllegalArgumentException if config is null
     * @throws ConfigurationException if invalid configuration is provided
     * @throws PersistenceException if storage error occurs
     * @throws MessageBusException if message bus error occurs
     * @throws AssetConnectionException when initializing asset connections fails
     */
    public Service(ServiceConfig config)
            throws ConfigurationException, AssetConnectionException, PersistenceException, MessageBusException {
        Ensure.requireNonNull(config, "config must be non-null");
        this.config = config;
        init();
    }


    @Override
    public Response execute(Endpoint source, Request request) {
        try {
            return requestHandler.execute(request, requestExecutionContext.withEndpoint(source));
        }
        catch (Exception e) {
            LOGGER.trace("Error executing request", e);
            return new InternalErrorResponse(e.getMessage());
        }
    }


    @Override
    public TypeInfo getTypeInfo(Reference reference) throws ResourceNotFoundException, PersistenceException {
        return TypeExtractor.extractTypeInfo(persistence.getSubmodelElement(reference, QueryModifier.DEFAULT));
    }


    /**
     * Executes a request asynchroniously.
     *
     * @param request request to execute
     * @param callback callback handler that is called when execution if finished
     * @throws IllegalArgumentException if request is null
     * @throws IllegalArgumentException if callback is null
     */
    public void executeAsync(Request request, Consumer<Response> callback) {
        Ensure.requireNonNull(request, "request must be non-null");
        Ensure.requireNonNull(callback, "callback must be non-null");
        this.requestHandler.executeAsync(request, callback, requestExecutionContext);
    }


    @Override
    public MessageBus getMessageBus() {
        return messageBus;
    }


    @Override
    public Persistence getPersistence() {
        return persistence;
    }


    @Override
    public FileStorage getFileStorage() {
        return fileStorage;
    }


    @Override
    public AssetConnectionManager getAssetConnectionManager() {
        return assetConnectionManager;
    }


    public ServiceConfig getConfig() {
        return config;
    }


    public SubmodelTemplateManager getSubmodelTemplateManager() {
        return submodelTemplateManager;
    }


    /**
     * Starts the service.This includes starting the message bus and endpoints.
     *
     * @throws MessageBusException if starting message bus fails
     * @throws EndpointException if starting endpoints fails
     * @throws PersistenceException if storage error occurs
     * @throws IllegalArgumentException if AAS environment is null/has not been properly initialized
     */
    public void start() throws MessageBusException, EndpointException, PersistenceException {
        LOGGER.debug("Get command for starting FA³ST Service");
        persistence.start();
        messageBus.start();
        if (!endpoints.isEmpty()) {
            LOGGER.info("Starting endpoints...");
        }
        for (Endpoint endpoint: endpoints) {
            LOGGER.debug("Starting endpoint {}", endpoint.getClass().getSimpleName());
            endpoint.start();
        }
        registrySynchronization.start();
        assetConnectionManager.start();
        submodelTemplateManager.start();
        LOGGER.debug("FA³ST Service is running!");
    }


    /**
     * Stop the service. This includes stopping the message bus and all endpoints.
     */
    public void stop() {
        LOGGER.debug("Get command for stopping FA³ST Service");
        submodelTemplateManager.stop();
        messageBus.stop();
        assetConnectionManager.stop();
        registrySynchronization.stop();
        persistence.stop();
        endpoints.forEach(Endpoint::stop);
    }


    private void init() throws ConfigurationException {
        Ensure.requireNonNull(config.getPersistence(), new InvalidConfigurationException("config.persistence must be non-null"));
        Ensure.requireNonNull(config.getFileStorage(), new InvalidConfigurationException("config.filestorage must be non-null"));
        Ensure.requireNonNull(config.getMessageBus(), new InvalidConfigurationException("config.messagebus must be non-null"));
        ZipSecureFile.setMinInflateRatio(config.getCore().getMinInflateRatio());
        ensureInitialModelFilesAreLoaded();
        persistence = (Persistence) config.getPersistence().newInstance(config.getCore(), this);
        fileStorage = (FileStorage) config.getFileStorage().newInstance(config.getCore(), this);
        messageBus = (MessageBus) config.getMessageBus().newInstance(config.getCore(), this);
        this.requestHandler = new RequestHandlerManager(config.getCore());
        this.requestExecutionContext = new DynamicRequestExecutionContext(this);
        if (config.getAssetConnections() != null) {
            List<AssetConnection> assetConnections = new ArrayList<>();
            for (AssetConnectionConfig assetConnectionConfig: config.getAssetConnections()) {
                assetConnections.add((AssetConnection) assetConnectionConfig.newInstance(config.getCore(), this));
            }
            assetConnectionManager = new AssetConnectionManager(config.getCore(), assetConnections, this);
        }
        if (submodelTemplateManager == null) {
            List<SubmodelTemplateProcessor> submodelTemplateProcessors = new ArrayList<>();
            for (SubmodelTemplateProcessorConfig submodelTemplateProcessorConfig: config.getSubmodelTemplateProcessors()) {
                submodelTemplateProcessors.add((SubmodelTemplateProcessor) submodelTemplateProcessorConfig.newInstance(config.getCore(), this));
            }
            submodelTemplateManager = new SubmodelTemplateManager(this, submodelTemplateProcessors);
        }
        endpoints = new ArrayList<>();
        if (config.getEndpoints() == null || config.getEndpoints().isEmpty()) {
            LOGGER.warn("no endpoint configuration found, starting service without endpoint which means the service will not be accessible via any kind of API");
        }
        else {
            for (EndpointConfig endpointConfig: config.getEndpoints()) {
                Endpoint endpoint = (Endpoint) endpointConfig.newInstance(config.getCore(), this);
                endpoints.add(endpoint);
            }
        }
        this.registrySynchronization = new RegistrySynchronization(config.getCore(), persistence, messageBus, endpoints);
    }


    private void ensureInitialModelFilesAreLoaded() {
        if (Objects.nonNull(config.getPersistence().getInitialModelFile())
                && DataFormat.forFileExtension(FileHelper.getFileExtensionWithoutSeparator(config.getPersistence().getInitialModelFile())).stream()
                        .anyMatch(DataFormat::getCanStoreFiles)) {
            config.getFileStorage().setInitialModelFile(config.getPersistence().getInitialModelFile());
        }
    }

}
