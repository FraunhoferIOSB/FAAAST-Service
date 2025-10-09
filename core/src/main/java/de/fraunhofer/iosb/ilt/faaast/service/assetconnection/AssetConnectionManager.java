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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.PatchSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.function.TriConsumer;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum;
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
    private ExecutorService executorService;
    private LambdaAssetConnection lambdaAssetConnection;
    private volatile boolean active;
    private boolean started;

    public AssetConnectionManager(CoreConfig coreConfig, List<AssetConnection> connections, Service service) throws ConfigurationException {
        this.active = true;
        this.started = false;
        this.coreConfig = coreConfig;
        this.connections = connections != null ? new ArrayList<>(connections) : new ArrayList<>();
        this.service = service;
        validateConnections(this.connections);
        init();
    }


    public List<Message> updateConnections(List<AssetConnectionConfig> oldConnectionConfigs, List<AssetConnectionConfig> newConnectionConfigs) {
        return null;
    }


    /**
     * Updates the asset connections. This method tries to not close connections if possible. Also providers might be moved
     * between connections if there are multiple connections with same properties.
     *
     * @param connectionConfigs the configs of the new connections
     * @return A list of messages potentially containing errors, warnings, and infos. If successful, this returns an empty
     *         list.
     */
    private List<Message> updateConnections(List<AssetConnectionConfig> connectionConfigs) {
        // TODO add validation
        // TODO respect "static" connections
        List<Message> result = new ArrayList<>();
        Map<AssetConnection, List<AssetConnection>> oldConnections = groupConnections(connections);
        Map<AssetConnectionConfig, List<AssetConnectionConfig>> newConnectionConfigs = groupConfigs(connectionConfigs);
        for (var oldConnectionEntry: oldConnections.entrySet()) {
            var newConnectionEntry = find(newConnectionConfigs, oldConnectionEntry.getKey());
            if (!newConnectionEntry.isPresent()) {
                // connection removed
                // does this properly work even if connection is not started or failed to connect? 
                // does this cancle the async connection attempt?
                oldConnectionEntry.getValue().forEach(x -> executorService.execute(() -> x.stop()));
                connections.removeAll(oldConnectionEntry.getValue());
            }
            else {
                // connection still present but providers may have changed
                result.addAll(updateProviders(oldConnectionEntry, newConnectionEntry.get()));
            }
        }
        for (var newConnectionConfigEntry: newConnectionConfigs.entrySet()) {
            if (!find(oldConnections, newConnectionConfigEntry.getKey()).isPresent()) {
                // connection added
                for (var newConnectionConfig: newConnectionConfigEntry.getValue()) {
                    try {
                        AssetConnection newConnection = (AssetConnection) newConnectionConfig.newInstance(coreConfig, service);
                        connections.add(newConnection);
                        if (started) {
                            setupConnectionAsync(newConnection);
                        }
                    }
                    catch (ConfigurationException e) {
                        result.add(Message.builder()
                                .messageType(MessageTypeEnum.EXCEPTION)
                                .text(String.format("failed to instantiate new connection (reason: %s)", e.getMessage()))
                                .build());
                    }
                }
            }
        }
        return result;
    }


    private <P extends AssetProvider, C extends AssetProviderConfig<P>> List<Message> updateProviders(Map.Entry<AssetConnection, List<AssetConnection>> oldConnectionEntry,
                                                                                                      Map.Entry<AssetConnectionConfig, List<AssetConnectionConfig>> newConnectionEntry) {
        List<Message> result = new ArrayList<>();
        updateProviders(oldConnectionEntry,
                newConnectionEntry,
                AssetValueProvider.class,
                AssetValueProviderConfig.class,
                x -> x.getValueProviders(),
                x -> x.getValueProviders(),
                (connection, reference) -> {
                    try {
                        connection.unregisterValueProvider(reference);
                    }
                    catch (AssetConnectionException e) {
                        result.add(Message.builder()
                                .messageType(MessageTypeEnum.EXCEPTION)
                                .text(String.format("failed to unregister value provider (reference: %s, reason: %s)",
                                        ReferenceHelper.asString(reference),
                                        e.getMessage()))
                                .build());
                    }
                },
                (connection, reference, providerConfig) -> {
                    try {
                        connection.registerValueProvider(reference, providerConfig);
                    }
                    catch (AssetConnectionException e) {
                        result.add(Message.builder()
                                .messageType(MessageTypeEnum.EXCEPTION)
                                .text(String.format("failed to register value provider (reference: %s, reason: %s)",
                                        ReferenceHelper.asString(reference),
                                        e.getMessage()))
                                .build());
                    }
                });
        updateProviders(oldConnectionEntry,
                newConnectionEntry,
                AssetSubscriptionProvider.class,
                AssetSubscriptionProviderConfig.class,
                x -> x.getSubscriptionProviders(),
                x -> x.getSubscriptionProviders(),
                (connection, reference) -> {
                    try {
                        connection.unregisterSubscriptionProvider(reference);
                    }
                    catch (AssetConnectionException e) {
                        result.add(Message.builder()
                                .messageType(MessageTypeEnum.EXCEPTION)
                                .text(String.format("failed to unregister subscription provider (reference: %s, reason: %s)",
                                        ReferenceHelper.asString(reference),
                                        e.getMessage()))
                                .build());
                    }
                },
                (connection, reference, providerConfig) -> {
                    try {
                        connection.registerSubscriptionProvider(reference, providerConfig);
                    }
                    catch (AssetConnectionException e) {
                        result.add(Message.builder()
                                .messageType(MessageTypeEnum.EXCEPTION)
                                .text(String.format("failed to register subscription provider (reference: %s, reason: %s)",
                                        ReferenceHelper.asString(reference),
                                        e.getMessage()))
                                .build());
                    }
                });
        updateProviders(oldConnectionEntry,
                newConnectionEntry,
                AssetOperationProvider.class,
                AssetOperationProviderConfig.class,
                x -> x.getOperationProviders(),
                x -> x.getOperationProviders(),
                (connection, reference) -> {
                    try {
                        connection.unregisterOperationProvider(reference);
                    }
                    catch (AssetConnectionException e) {
                        result.add(Message.builder()
                                .messageType(MessageTypeEnum.EXCEPTION)
                                .text(String.format("failed to unregister operation provider (reference: %s, reason: %s)",
                                        ReferenceHelper.asString(reference),
                                        e.getMessage()))
                                .build());
                    }
                },
                (connection, reference, providerConfig) -> {
                    try {
                        connection.registerOperationProvider(reference, providerConfig);
                    }
                    catch (AssetConnectionException e) {
                        result.add(Message.builder()
                                .messageType(MessageTypeEnum.EXCEPTION)
                                .text(String.format("failed to register operation provider (reference: %s, reason: %s)",
                                        ReferenceHelper.asString(reference),
                                        e.getMessage()))
                                .build());
                    }
                });
        return result;
    }


    // constraint: there is at most one type of provider for a reference
    private <P extends AssetProvider, C extends AssetProviderConfig<P>> void updateProviders(Map.Entry<AssetConnection, List<AssetConnection>> oldConnectionEntry,
                                                                                             Map.Entry<AssetConnectionConfig, List<AssetConnectionConfig>> newConnectionEntry,
                                                                                             Class<P> providerType,
                                                                                             Class<C> configType,
                                                                                             Function<AssetConnection, Map<Reference, P>> getConnectionProviders,
                                                                                             Function<AssetConnectionConfig, Map<Reference, C>> getConfigProviders,
                                                                                             BiConsumer<AssetConnection, Reference> unregisterProvider,
                                                                                             TriConsumer<AssetConnection, Reference, C> registerProvider) {
        Map<Reference, P> oldProviders = oldConnectionEntry.getValue().stream()
                .flatMap(x -> getConnectionProviders.apply(x).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<Reference, C> newProviders = newConnectionEntry.getValue().stream()
                .flatMap(x -> getConfigProviders.apply(x).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (var oldConnection: oldConnectionEntry.getValue()) {
            for (var oldProviderEntry: ((Map<Reference, AssetValueProvider>) oldConnection.getValueProviders()).entrySet()) {
                if (!newProviders.containsKey(oldProviderEntry.getKey())
                        || !Objects.equals(oldProviderEntry.getValue(), newProviders.get(oldProviderEntry.getKey()))) {
                    // remove old provider
                    unregisterProvider.accept(oldConnection, oldProviderEntry.getKey());
                }
            }
        }
        for (var newProviderEntry: newProviders.entrySet()) {
            if (!oldProviders.containsKey(newProviderEntry.getKey())) {
                // add new provider
                registerProvider.accept(oldConnectionEntry.getKey(), newProviderEntry.getKey(), newProviderEntry.getValue());
            }
            else if (!Objects.equals(newProviderEntry.getValue(), oldProviders.get(newProviderEntry.getKey()))) {
                // ERROR - should not be possible
            }
        }
    }


    private <T> Optional<Map.Entry<AssetConnectionConfig, T>> find(Map<AssetConnectionConfig, T> map, AssetConnection connection) {
        return map.entrySet().stream().filter(x -> x.getKey().equalsIgnoringProviders(connection.asConfig()))
                .findFirst();
    }


    private <T> Optional<Map.Entry<AssetConnection, T>> find(Map<AssetConnection, T> map, AssetConnectionConfig connectionConfig) {
        return map.entrySet().stream().filter(x -> connectionConfig.equalsIgnoringProviders(x.getKey().asConfig()))
                .findFirst();
    }


    private Map<AssetConnection, List<AssetConnection>> groupConnections(List<AssetConnection> connections) {
        Map<AssetConnection, List<AssetConnection>> result = new LinkedHashMap<>();
        for (AssetConnection connection: connections) {
            boolean added = false;
            for (var entry: result.entrySet()) {
                if (((AssetConnectionConfig) connection.asConfig()).equalsIgnoringProviders(entry.getKey())) {
                    entry.getValue().add(connection);
                    added = true;
                    break;
                }
            }
            if (!added) {
                result.put(connection, new ArrayList<>(Arrays.asList(connection)));
            }
        }
        return result;
    }


    private Map<AssetConnectionConfig, List<AssetConnectionConfig>> groupConfigs(List<AssetConnectionConfig> connectionConfigs) {
        Map<AssetConnectionConfig, List<AssetConnectionConfig>> result = new LinkedHashMap<>();
        for (AssetConnectionConfig config: connectionConfigs) {
            boolean added = false;
            for (var entry: result.entrySet()) {
                if (config.equalsIgnoringProviders(entry.getKey())) {
                    entry.getValue().add(config);
                    added = true;
                    break;
                }
            }
            if (!added) {
                result.put(config, new ArrayList<>(Arrays.asList(config)));
            }
        }
        return result;
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
        executorService = Executors.newCachedThreadPool(threadFactory);
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
        started = true;
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
        while (active && connection.isActive() && !connection.isConnected()) {
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
        executorService.execute(() -> {
            tryConnectingUntilSuccess(connection);
            setupSubscriptions(connection);
        });
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
        List<AssetConnection> temp = new ArrayList<>(connections);
        temp.add(newConnection);
        validateConnections(temp);
        Optional<AssetConnection> connection = connections.stream().filter(x -> Objects.equals(x, newConnection)).findFirst();
        if (connection.isPresent()) {
            connectionConfig.getValueProviders().forEach(LambdaExceptionHelper.rethrowBiConsumer(
                    (k, v) -> connection.get().registerValueProvider(k, v)));
            connectionConfig.getSubscriptionProviders().forEach(LambdaExceptionHelper.rethrowBiConsumer(
                    (k, v) -> connection.get().registerSubscriptionProvider(k, v)));
            connectionConfig.getOperationProviders().forEach(LambdaExceptionHelper.rethrowBiConsumer(
                    (k, v) -> connection.get().registerOperationProvider(k, v)));
        }
        else {
            connections.add(newConnection);
            if (started) {
                setupConnectionAsync(newConnection);
            }
        }
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
            executorService.awaitTermination(coreConfig.getAssetConnectionRetryInterval() * 2, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException ex) {
            executorService.shutdownNow();
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


    private void validateConnectionConfigs(List<AssetConnectionConfig> connectionsConfigs) throws ConfigurationException {
        Optional<Map.Entry<Reference, List<AssetValueProviderConfig>>> valueProviders = connectionsConfigs.stream()
                .filter(Objects::nonNull)
                .flatMap(x -> (Stream<Map.Entry<Reference, AssetValueProviderConfig>>) x.getValueProviders().entrySet().stream())
                .collect(Collectors.groupingBy(x -> x.getKey(), Collectors.mapping(x -> x.getValue(), Collectors.toList()))).entrySet().stream()
                .filter(x -> x.getValue().size() > 1)
                .findFirst();
        if (valueProviders.isPresent()) {
            throw new InvalidConfigurationException(String.format("found %d value providers for reference %s but maximum 1 allowed",
                    valueProviders.get().getValue().size(),
                    ReferenceHelper.toString(valueProviders.get().getKey())));
        }
        Optional<Map.Entry<Reference, List<AssetOperationProviderConfig>>> operationProviders = connectionsConfigs.stream()
                .filter(Objects::nonNull)
                .flatMap(x -> (Stream<Map.Entry<Reference, AssetOperationProviderConfig>>) x.getOperationProviders().entrySet().stream())
                .collect(Collectors.groupingBy(x -> x.getKey(), Collectors.mapping(x -> x.getValue(), Collectors.toList()))).entrySet().stream()
                .filter(x -> x.getValue().size() > 1)
                .findFirst();
        if (operationProviders.isPresent()) {
            throw new InvalidConfigurationException(String.format("found %d operation providers for reference %s but maximum 1 allowed",
                    operationProviders.get().getValue().size(),
                    ReferenceHelper.toString(operationProviders.get().getKey())));
        }
        Optional<Map.Entry<Reference, List<AssetSubscriptionProviderConfig>>> subscriptionProviders = connectionsConfigs.stream()
                .filter(Objects::nonNull)
                .flatMap(x -> (Stream<Map.Entry<Reference, AssetSubscriptionProviderConfig>>) x.getSubscriptionProviders().entrySet().stream())
                .collect(Collectors.groupingBy(x -> x.getKey(), Collectors.mapping(x -> x.getValue(), Collectors.toList()))).entrySet().stream()
                .filter(x -> x.getValue().size() > 1)
                .findFirst();
        if (subscriptionProviders.isPresent()) {
            throw new InvalidConfigurationException(String.format("found %d subscription providers for reference %s but maximum 1 allowed",
                    subscriptionProviders.get().getValue().size(),
                    ReferenceHelper.toString(subscriptionProviders.get().getKey())));
        }
    }


    private void validateConnections(List<AssetConnection> connections) throws ConfigurationException {
        validateConnectionConfigs(connections.stream()
                .map(AssetConnection::asConfig)
                .map(AssetConnectionConfig.class::cast)
                .toList());
    }


    /**
     * Remove the given AssetConnection.
     *
     * @param connection The AssetConnection to remove.
     */
    public void remove(AssetConnection connection) {
        if (connections.contains(connection)) {
            connection.stop();

            connections.remove(connection);
        }
        else {
            throw new IllegalArgumentException("AssetConnection not found");
        }
    }
}
