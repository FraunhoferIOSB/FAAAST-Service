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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection;

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.LambdaAssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.LambdaOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.LambdaSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.LambdaValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.PatchSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages all asset connections and provides convenient functions to
 * find/access providers.
 */
public class AssetConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetConnectionManager.class);
    private final List<AssetConnection> connections;
    private final CoreConfig coreConfig;
    private final Service service;
    private ScheduledExecutorService scheduledExecutorService;
    private LambdaAssetConnection lambdaAssetConnection;
    private volatile boolean active;

    public AssetConnectionManager(CoreConfig coreConfig, List<AssetConnection> connections, Service service) throws ConfigurationException {
        this.active = true;
        this.coreConfig = coreConfig;
        this.connections = connections != null ? new ArrayList<>(connections) : new ArrayList<>();
        this.service = service;
        validateConnections();
        init();
    }


    private void init() {
        lambdaAssetConnection = new LambdaAssetConnection();
        ThreadFactory threadFactory = new ThreadFactory() {
            AtomicLong count = new AtomicLong(0);

            @Override
            public Thread newThread(Runnable target) {
                return new Thread(target, String.format("asset connection establisher - %d", count.getAndIncrement()));
            }
        };
        scheduledExecutorService = Executors.newScheduledThreadPool(this.connections.size(), threadFactory);
    }


    /**
     * Starts the AssetConnectionManager and tries to establish asset
     * connections.
     */
    public void start() {
        if (!connections.isEmpty()) {
            LOGGER.info("Connecting to assets...");
        }
        for (var connection: connections) {
            setupConnectionAsync(connection);
        }
        lambdaAssetConnection.start();
    }


    /**
     * Register a {@link LambdaValueProvider}.
     *
     * @param reference the reference
     * @param provider the provider
     */
    public void registerLambdaValueProvider(Reference reference, LambdaValueProvider provider) {
        lambdaAssetConnection.registerValueProvider(reference, provider);
    }


    /**
     * Unregister a {@link LambdaValueProvider}.
     *
     * @param reference the reference
     */
    public void unregisterLambdaValueProvider(Reference reference) {
        lambdaAssetConnection.unregisterValueProvider(reference);
    }


    /**
     * Register a {@link LambdaSubscriptionProvider}.
     *
     * @param reference the reference
     * @param provider the provider
     */
    public void registerLambdaSubscriptionProvider(Reference reference, LambdaSubscriptionProvider provider) {
        setupSubscription(reference, provider);
        lambdaAssetConnection.registerSubscriptionProvider(reference, provider);
    }


    /**
     * Unregister a {@link LambdaSubscriptionProvider}.
     *
     * @param reference the reference
     */
    public void unregisterLambdaSubscriptionProvider(Reference reference) {
        lambdaAssetConnection.unregisterSubscriptionProvider(reference);
    }


    /**
     * Register a {@link LambdaOperationProvider}.
     *
     * @param reference the reference
     * @param provider the provider
     */
    public void registerLambdaOperationProvider(Reference reference, LambdaOperationProvider provider) {
        lambdaAssetConnection.registerOperationProvider(reference, provider);
    }


    /**
     * Reset the AssetConnectionManager by first stopping the manager if active, then removing all connections and
     * restarting the manager.
     */
    public void reset() {
        if (active) {
            stop();
        }
        connections.clear();
        init();
        start();
    }


    /**
     * Unregister a {@link LambdaOperationProvider}.
     *
     * @param reference the reference
     */
    public void unregisterLambdaOperationProvider(Reference reference) {
        lambdaAssetConnection.unregisterOperationProvider(reference);
    }


    private void tryConnecting(AssetConnection connection) throws AssetConnectionException {
        connection.connect();
        LOGGER.info("Asset connection established (endpoint: {})", connection.getEndpointInformation());
    }


    private void tryConnectingUntilSuccess(AssetConnection connection) {
        try {
            tryConnecting(connection);
        }
        catch (AssetConnectionException e) {
            LOGGER.info(
                    "Establishing asset connection failed on initial attempt (endpoint: {}, reason: {}). Connecting will be retried every {} ms but no more messages about failures will be shown.",
                    connection.getEndpointInformation(),
                    e.getMessage(),
                    coreConfig.getAssetConnectionRetryInterval(),
                    e);
        }
        while (active && !connection.isConnected()) {
            try {
                tryConnecting(connection);
            }
            catch (AssetConnectionException e) {
                LOGGER.trace("Establishing asset connection failed (endpoint: {})",
                        connection.getEndpointInformation(),
                        e);
                try {
                    Thread.sleep(coreConfig.getAssetConnectionRetryInterval());
                }
                catch (InterruptedException e2) {
                    // intentionally empty
                }
            }
        }
    }


    private void setupSubscription(Reference reference, AssetSubscriptionProvider provider) {
        if (!active) {
            return;
        }
        try {
            provider.addNewDataListener((DataElementValue data) -> {
                Response response = service.execute(PatchSubmodelElementValueByPathRequest.builder()
                        .submodelId(ReferenceHelper.findFirstKeyType(reference, KeyTypes.SUBMODEL))
                        .path(ReferenceHelper.toPath(reference))
                        .disableSyncWithAsset()
                        .value(data)
                        .build());
                if (!response.getStatusCode().isSuccess()) {
                    LOGGER.atInfo().log("Error updating value from asset connection subscription (reference: {})",
                            ReferenceHelper.toString(reference));
                    LOGGER.debug("Error updating value from asset connection subscription (reference: {}, reason: {})",
                            ReferenceHelper.toString(reference),
                            response.getResult().getMessages());
                }
            });
        }
        catch (AssetConnectionException e) {
            LOGGER.warn("Subscribing to asset connection failed (reference: {})",
                    ReferenceHelper.toString(reference),
                    e);
        }
    }


    private void setupSubscriptions(AssetConnection connection) {
        ((Map<Reference, AssetSubscriptionProvider>) connection.<Reference, AssetSubscriptionProvider> getSubscriptionProviders()).entrySet()
                .forEach(x -> setupSubscription(x.getKey(), x.getValue()));
    }


    private void setupConnectionAsync(AssetConnection connection) {
        scheduledExecutorService.schedule(
                () -> {
                    tryConnectingUntilSuccess(connection);
                    setupSubscriptions(connection);
                },
                0,
                TimeUnit.MILLISECONDS);
    }


    /**
     * Adds a new AssetConnection created from an AssetConnectionConfig.
     *
     * @param connectionConfig the AssetConnectionConfig describing the AssetConnection to add
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException if provided connectionConfig is
     *             invalid
     * @throws de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException if initializing asset
     *             connection fails
     */
    public void add(AssetConnectionConfig<? extends AssetConnection, ? extends AssetValueProviderConfig, ? extends AssetOperationProviderConfig, ? extends AssetSubscriptionProviderConfig> connectionConfig)
            throws ConfigurationException, AssetConnectionException {
        AssetConnection newConnection = connectionConfig.newInstance(coreConfig, service);
        Optional<AssetConnection> connection = connections.stream().filter(x -> Objects.equals(x, newConnection)).findFirst();
        if (connection.isPresent()) {
            connectionConfig.getValueProviders().forEach(LambdaExceptionHelper.rethrowBiConsumer(
                    (k, v) -> connection.get().registerValueProvider(k, (AssetValueProviderConfig) v)));
            connectionConfig.getSubscriptionProviders().forEach(LambdaExceptionHelper.rethrowBiConsumer(
                    (k, v) -> connection.get().registerSubscriptionProvider(k, (AssetSubscriptionProviderConfig) v)));
            connectionConfig.getOperationProviders().forEach(LambdaExceptionHelper.rethrowBiConsumer(
                    (k, v) -> connection.get().registerOperationProvider(k, (AssetOperationProviderConfig) v)));
        }
        else {
            connections.add(newConnection);
            validateConnections();
        }
        validateConnections();
    }


    /**
     * Gets all connections managed by this AssetConnectionManager.
     *
     * @return all managed connections
     */
    public List<AssetConnection> getConnections() {
        return connections;
    }


    /**
     * Stops all connection attempts and disconnects all connected assets.
     */
    public void stop() {
        active = false;
        try {
            scheduledExecutorService.awaitTermination(coreConfig.getAssetConnectionRetryInterval() * 2, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException ex) {
            scheduledExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        lambdaAssetConnection.stop();
        connections.stream()
                .filter(AssetConnection::isConnected)
                .forEach(x -> {
                    try {
                        x.disconnect();
                    }
                    catch (AssetConnectionException e) {
                        LOGGER.trace("Error closing asset connection (endpoint: {})",
                                x.getEndpointInformation(),
                                e);
                    }

                });
    }


    /**
     * Gets the operation provider for the AAS element defined by reference.
     *
     * @param reference AAS element
     * @return operation provider for the AAS element defined by reference or
     *         null if there is none defined
     */
    public AssetOperationProvider<? extends AssetOperationProviderConfig> getOperationProvider(Reference reference) {
        if (lambdaAssetConnection.hasOperationProvider(reference)) {
            return lambdaAssetConnection.getOperationProvider(reference);
        }
        return connections.stream()
                .flatMap(x -> (Stream<Map.Entry<Reference, AssetOperationProvider>>) x.getOperationProviders().entrySet().stream())
                .filter(x -> ReferenceHelper.equals(reference, x.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }


    /**
     * Gets the subscription provider for the AAS element defined by reference.
     *
     * @param reference AAS element
     * @return subscription provider for the AAS element defined by reference or
     *         null if there is none defined
     */
    public AssetSubscriptionProvider getSubscriptionProvider(Reference reference) {
        if (lambdaAssetConnection.hasSubscriptionProvider(reference)) {
            return lambdaAssetConnection.getSubscriptionProvider(reference);
        }
        return connections.stream()
                .flatMap(x -> (Stream<Map.Entry<Reference, AssetSubscriptionProvider>>) x.getSubscriptionProviders().entrySet().stream())
                .filter(x -> ReferenceHelper.equals(reference, x.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }


    /**
     * Gets the value provider for the AAS element defined by reference.
     *
     * @param reference AAS element
     * @return value provider for the AAS element defined by reference or null
     *         if there is none defined
     */
    public AssetValueProvider getValueProvider(Reference reference) {
        if (lambdaAssetConnection.hasValueProvider(reference)) {
            return lambdaAssetConnection.getValueProvider(reference);
        }
        return connections.stream()
                .flatMap(x -> (Stream<Map.Entry<Reference, AssetValueProvider>>) x.getValueProviders().entrySet().stream())
                .filter(x -> ReferenceHelper.equals(reference, x.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }


    /**
     * If a {@link AssetValueProvider} exists for given reference, the provided
     * will be written; otherwise nothing happens.
     *
     * @param reference reference to element to check for asset connection
     * @param value the value to write
     * @throws AssetConnectionException if writing value to asset connection
     *             fails
     */
    public void setValue(Reference reference, ElementValue value) throws AssetConnectionException {
        if (hasValueProvider(reference) && ElementValueHelper.isValidDataElementValue(value)) {
            try {
                getValueProvider(reference).setValue((DataElementValue) value);
            }
            catch (UnsupportedOperationException e) {
                // ignored on purpose
            }
        }
    }


    /**
     * Reads value from asset connection if available, otherwise empty optional
     * is returned.
     *
     * @param reference reference to element to check for asset connection
     * @return value read from the asset connection if available, empty optional
     *         otherwise
     * @throws AssetConnectionException if there is an asset connection but
     *             reading fails
     */
    public Optional<DataElementValue> readValue(Reference reference) throws AssetConnectionException {
        if (hasValueProvider(reference)) {
            try {
                return Optional.ofNullable(getValueProvider(reference).getValue());
            }
            catch (UnsupportedOperationException e) {
                // ignored on purpose
            }
        }
        return Optional.empty();
    }


    /**
     * Returns whether there is a operation provider defined for the provided
     * AAS element or not.
     *
     * @param reference AAS element
     * @return true if there is a operation provider defined for the provided
     *         AAS element, otherwise false
     */
    public boolean hasOperationProvider(Reference reference) {
        return Objects.nonNull(getOperationProvider(reference));
    }


    /**
     * Returns whether there is a subscription provider defined for the provided
     * AAS element or not.
     *
     * @param reference AAS element
     * @return true if there is a subscription provider defined for the provided
     *         AAS element, otherwise false
     */
    public boolean hasSubscriptionProvider(Reference reference) {
        return Objects.nonNull(getSubscriptionProvider(reference));
    }


    /**
     * Returns whether there is a value provider defined for the provided AAS
     * element or not.
     *
     * @param reference AAS element
     * @return true if there is a value provider defined for the provided AAS
     *         element, otherwise false
     */
    public boolean hasValueProvider(Reference reference) {
        return Objects.nonNull(getValueProvider(reference));
    }


    private void validateConnections() throws ConfigurationException {
        Optional<Map.Entry<Reference, List<AssetValueProvider>>> valueProviders = connections.stream()
                .flatMap(x -> (Stream<Map.Entry<Reference, AssetValueProvider>>) x.getValueProviders().entrySet().stream())
                .collect(Collectors.groupingBy(x -> x.getKey(), Collectors.mapping(x -> x.getValue(), Collectors.toList()))).entrySet().stream()
                .filter(x -> x.getValue().size() > 1)
                .findFirst();
        if (valueProviders.isPresent()) {
            throw new InvalidConfigurationException(String.format("found %d value providers for reference %s but maximum 1 allowed",
                    valueProviders.get().getValue().size(),
                    ReferenceHelper.toString(valueProviders.get().getKey())));
        }
        Optional<Map.Entry<Reference, List<AssetOperationProvider>>> operationProviders = connections.stream()
                .flatMap(x -> (Stream<Map.Entry<Reference, AssetOperationProvider>>) x.getOperationProviders().entrySet().stream())
                .collect(Collectors.groupingBy(x -> x.getKey(), Collectors.mapping(x -> x.getValue(), Collectors.toList()))).entrySet().stream()
                .filter(x -> x.getValue().size() > 1)
                .findFirst();
        if (operationProviders.isPresent()) {
            throw new InvalidConfigurationException(String.format("found %d operation providers for reference %s but maximum 1 allowed",
                    operationProviders.get().getValue().size(),
                    ReferenceHelper.toString(operationProviders.get().getKey())));
        }
        Optional<Map.Entry<Reference, List<AssetSubscriptionProvider>>> subscriptionProviders = connections.stream()
                .flatMap(x -> (Stream<Map.Entry<Reference, AssetSubscriptionProvider>>) x.getSubscriptionProviders().entrySet().stream())
                .collect(Collectors.groupingBy(x -> x.getKey(), Collectors.mapping(x -> x.getValue(), Collectors.toList()))).entrySet().stream()
                .filter(x -> x.getValue().size() > 1)
                .findFirst();
        if (subscriptionProviders.isPresent()) {
            throw new InvalidConfigurationException(String.format("found %d subscription providers for reference %s but maximum 1 allowed",
                    subscriptionProviders.get().getValue().size(),
                    ReferenceHelper.toString(subscriptionProviders.get().getKey())));
        }
    }
}
