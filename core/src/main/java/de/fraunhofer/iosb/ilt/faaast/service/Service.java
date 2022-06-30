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
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.InternalErrorResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.request.RequestHandlerManager;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Central class of the FA³ST Service accumulating and connecting all different
 * components.
 */
public class Service implements ServiceContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);
    private AssetConnectionManager assetConnectionManager;
    private ServiceConfig config;
    private List<Endpoint> endpoints;
    private MessageBus messageBus;
    private Persistence persistence;
    private RequestHandlerManager requestHandler;

    /**
     * Creates a new instance of {@link Service}
     *
     * @param coreConfig core configuration
     * @param persistence persistence implementation
     * @param messageBus message bus implementation
     * @param endpoints endpoints
     * @param assetConnections asset connections
     * @throws IllegalArgumentException if coreConfig is null
     * @throws IllegalArgumentException if persistence is null
     * @throws IllegalArgumentException if messageBus is null
     * @throws RuntimeException if creating a deep copy of aasEnvironment fails
     * @throws ConfigurationException the configuration the
     *             {@link AssetConnectionManager} fails
     * @throws AssetConnectionException when initializing asset connections
     *             fails
     */
    public Service(CoreConfig coreConfig,
            Persistence persistence,
            MessageBus messageBus,
            List<Endpoint> endpoints,
            List<AssetConnection> assetConnections) throws ConfigurationException, AssetConnectionException {
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
        this.messageBus = messageBus;
        this.assetConnectionManager = new AssetConnectionManager(config.getCore(), assetConnections, this);
        this.requestHandler = new RequestHandlerManager(this.config.getCore(), this.persistence, this.messageBus, this.assetConnectionManager);
    }


    /**
     * Creates a new instance of {@link Service}
     *
     * @param config service configuration
     * @throws IllegalArgumentException if config is null
     * @throws ConfigurationException if invalid configuration is provided
     * @throws AssetConnectionException when initializing asset connections
     *             fails
     */
    public Service(ServiceConfig config)
            throws ConfigurationException, AssetConnectionException {
        Ensure.requireNonNull(config, "config must be non-null");
        this.config = config;
        init();
    }


    @Override
    public Response execute(Request request) {
        try {
            return this.requestHandler.execute(request);
        }
        catch (Exception e) {
            return new InternalErrorResponse(e.getMessage());
        }
    }


    @Override
    public OperationVariable[] getOperationOutputVariables(Reference reference) {
        return persistence.getOperationOutputVariables(reference);
    }


    @Override
    public TypeInfo getTypeInfo(Reference reference) {
        return persistence.getTypeInfo(reference);
    }


    @Override
    public AssetAdministrationShellEnvironment getAASEnvironment() {
        return DeepCopyHelper.deepCopy(persistence.getEnvironment());
    }


    /**
     * Executes a request asynchroniously
     *
     * @param request request to execute
     * @param callback callback handler that is called when execution if
     *            finished
     * @throws IllegalArgumentException if request is null
     * @throws IllegalArgumentException if callback is null
     */
    public void executeAsync(Request request, Consumer<Response> callback) {
        Ensure.requireNonNull(request, "request must be non-null");
        Ensure.requireNonNull(callback, "callback must be non-null");
        this.requestHandler.executeAsync(request, callback);
    }


    @Override
    public MessageBus getMessageBus() {
        return messageBus;
    }


    /**
     * Starts the service.This includes starting the message bus and endpoints.
     *
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException if starting message bus fails
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.EndpointException if starting endpoints fails
     * @throws IllegalArgumentException if AAS environment is null/has not been
     *             properly initialized
     */
    public void start() throws MessageBusException, EndpointException {
        LOGGER.info("Get command for starting FA³ST Service");
        messageBus.start();
        for (Endpoint endpoint: endpoints) {
            LOGGER.info("Starting endpoint {}", endpoint.getClass().getSimpleName());
            endpoint.start();
        }
        LOGGER.info("FA³ST Service is running!");
    }


    /**
     * Stop the service. This includes stopping the message bus and all
     * endpoints
     */
    public void stop() {
        LOGGER.info("Get command for stopping FA³ST Service");
        messageBus.stop();
        endpoints.forEach(Endpoint::stop);
    }


    private void init() throws ConfigurationException, AssetConnectionException {
        if (config.getPersistence() == null) {
            throw new InvalidConfigurationException("config.persistence must be non-null");
        }
        persistence = (Persistence) config.getPersistence().newInstance(config.getCore(), this);
        if (config.getMessageBus() == null) {
            throw new InvalidConfigurationException("config.messagebus must be non-null");
        }
        messageBus = (MessageBus) config.getMessageBus().newInstance(config.getCore(), this);
        if (config.getAssetConnections() != null) {
            List<AssetConnection> assetConnections = new ArrayList<>();
            for (AssetConnectionConfig assetConnectionConfig: config.getAssetConnections()) {
                assetConnections.add((AssetConnection) assetConnectionConfig.newInstance(config.getCore(), this));
            }

            assetConnectionManager = new AssetConnectionManager(config.getCore(), assetConnections, this);
        }
        endpoints = new ArrayList<>();
        if (config.getEndpoints() == null || config.getEndpoints().isEmpty()) {
            // TODO maybe be less restrictive and only print warning
            //throw new InvalidConfigurationException("at least endpoint must be defined in the configuration");
            LOGGER.warn("no endpoint configuration found, starting service without endpoint which means the service will not be accessible via any kind of API");
        }
        else {
            for (EndpointConfig endpointConfig: config.getEndpoints()) {
                Endpoint endpoint = (Endpoint) endpointConfig.newInstance(config.getCore(), this);
                endpoints.add(endpoint);
            }
        }
        this.requestHandler = new RequestHandlerManager(this.config.getCore(), this.persistence, this.messageBus, this.assetConnectionManager);
    }
}
