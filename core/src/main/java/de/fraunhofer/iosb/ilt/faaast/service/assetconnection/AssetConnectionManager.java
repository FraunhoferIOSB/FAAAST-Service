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
import de.fraunhofer.iosb.ilt.faaast.service.config.Config;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.PatchSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
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
        this.service = service;
        this.connections = normalizeConnections(connections);
        validateConnections(this.connections);
        init();
    }


    /**
     * Cleans up dangling asset connections recursively after an element has been modified.
     *
     * @param modifiedElement the modified element
     */
    public void cleanupDanglingConnectionsAfterModify(Reference modifiedElement) {
        Predicate<Reference> condition = x -> ReferenceHelper.startsWith(x, modifiedElement) && !service.getPersistence().submodelElementExists(x);
        connections.stream()
                .forEach(LambdaExceptionHelper.rethrowConsumer(connection -> {
                    for (var providerType: AssetProviderType.values()) {
                        providerType.getProvidersFromConnectionAccessor().apply(connection).keySet().stream()
                                .filter(condition)
                                .forEach(x -> {
                                    try {
                                        providerType.getUnregisterProviderAccessor().accept(connection, x);
                                    }
                                    catch (Exception e) {
                                        LOGGER.info(
                                                "failed to unregister asset {} provider after element has been modified/deleted (modified/deleted element: {}, provider reference: {}, reason: {})",
                                                providerType,
                                                ReferenceHelper.asString(modifiedElement),
                                                ReferenceHelper.asString(x),
                                                e.getMessage(),
                                                e);
                                    }
                                });
                    }
                    if (connection.getValueProviders().isEmpty()
                            && connection.getOperationProviders().isEmpty()
                            && connection.getSubscriptionProviders().isEmpty()) {
                        try {
                            connection.disconnect();
                        }
                        catch (AssetConnectionException e) {
                            LOGGER.warn("Failed to clean up empty asset connection", e);
                        }
                    }
                }));

    }


    /**
     * Updates part of the existing asset connections by taking the delta between {@code oldConfigs} and {@code newConfigs}.
     * Connections and providers not part of {@code oldConfigs} are not modified and will continue to work. Providers might
     * be moved between connections during normalization step.
     *
     * @param oldConfigs the old connections to be updated/replaced
     * @param newConfigs the new version of the connections
     * @return a list of {@link Message} objects indicating issues encountered during processing. This can include infos,
     *         warnings, and exceptions.
     */
    public List<Message> updateConnections(List<AssetConnectionConfig> oldConfigs, List<AssetConnectionConfig> newConfigs) {
        List<AssetConnectionConfig> oldConnectionConfigs = normalizeConnectionConfigs(clone(oldConfigs));
        List<AssetConnectionConfig> newConnectionConfigs = normalizeConnectionConfigs(clone(newConfigs));
        ChangeSet changeSet = computeChangeSet(oldConnectionConfigs, newConnectionConfigs);
        try {
            List<AssetConnectionConfig> newConnections = mergeChanges(connections, changeSet);
            validateConnectionConfigs(newConnections);
            return apply(newConnectionConfigs);
        }
        catch (ExceptionWithDetails e) {
            return e.getMessages();
        }
        catch (ConfigurationException e) {
            return List.of(Message.builder()
                    .messageType(MessageTypeEnum.EXCEPTION)
                    .text(e.getMessage())
                    .build());
        }

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
    protected AssetOperationProvider<? extends AssetOperationProviderConfig> getOperationProvider(Reference reference) {
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
    protected AssetSubscriptionProvider getSubscriptionProvider(Reference reference) {
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
    protected AssetValueProvider getValueProvider(Reference reference) {
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
            return Optional.ofNullable(getValueProvider(reference).getValue());
        }
        return Optional.empty();
    }


    /**
     * Invokes an operation provide synchronously and returns the result if one exists for this reference.
     *
     * @param reference the reference
     * @param input the input
     * @param inoutput the inoutput
     * @return the result of the invocation or Optional.empty if none exists
     * @throws AssetConnectionException if invocation fails
     */
    public Optional<OperationVariable[]> invoke(Reference reference, OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
        if (hasOperationProvider(reference)) {
            return Optional.ofNullable(getOperationProvider(reference).invoke(input, inoutput));
        }
        return Optional.empty();
    }


    /**
     * Invokes an operation provide asynchronously if one exists for this reference.
     *
     * @param reference the reference
     * @param input the input
     * @param inoutput the inoutput
     * @param callbackSuccess callback upon success
     * @param callbackFailure callback upon failure
     * @throws AssetConnectionException if invocation fails
     */
    public void invokeAsync(Reference reference,
                            OperationVariable[] input,
                            OperationVariable[] inoutput,
                            BiConsumer<OperationVariable[], OperationVariable[]> callbackSuccess,
                            Consumer<Throwable> callbackFailure)
            throws AssetConnectionException {
        if (hasOperationProvider(reference)) {
            getOperationProvider(reference).invokeAsync(input, inoutput, callbackSuccess, callbackFailure);
        }
    }


    /**
     * Returns the input validation mode for the operation provider mapped to the reference if such a provider exists.
     *
     * @param reference the reference
     * @return the input validation mode if provider exists, otherwise Optional.empty
     */
    public Optional<ArgumentValidationMode> getOperationInputValidationMode(Reference reference) {
        if (hasOperationProvider(reference)) {
            return Optional.ofNullable(getOperationProvider(reference).getConfig().getInputValidationMode());
        }
        return Optional.empty();
    }


    /**
     * Returns the inoutput validation mode for the operation provider mapped to the reference if such a provider exists.
     *
     * @param reference the reference
     * @return the inoutput validation mode if provider exists, otherwise Optional.empty
     */
    public Optional<ArgumentValidationMode> getOperationInoutputValidationMode(Reference reference) {
        if (hasOperationProvider(reference)) {
            return Optional.ofNullable(getOperationProvider(reference).getConfig().getInoutputValidationMode());
        }
        return Optional.empty();
    }


    /**
     * Returns the output validation mode for the operation provider mapped to the reference if such a provider exists.
     *
     * @param reference the reference
     * @return the output validation mode if provider exists, otherwise Optional.empty
     */
    public Optional<ArgumentValidationMode> getOperationOutputValidationMode(Reference reference) {
        if (hasOperationProvider(reference)) {
            return Optional.ofNullable(getOperationProvider(reference).getConfig().getOutputValidationMode());
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


    /**
     * Returns if all connections are connected.
     *
     * @return true if all connections are connected, false otherwise
     */
    public boolean isFullyConnected() {
        return connections.stream().allMatch(AssetConnection::isConnected);
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
     * Normalizes a list of connections. Normalization means that there is only one connection with the same properties
     * containing all providers.
     *
     * @param connections the connections to normalize
     * @return normalized connections
     */
    private List<AssetConnection> normalizeConnections(List<AssetConnection> connections) {
        if (Objects.isNull(connections)) {
            return new ArrayList<>();
        }
        if (connections.stream().anyMatch(AssetConnection::isConnected)) {
            LOGGER.debug("skipped asset connection normalization (reason: at least one connection is connected and only unconnected connectoins can be normalized)");
            return connections;
        }
        try {
            return groupConfigs(connections.stream()
                    .map(AssetConnection::asConfig)
                    .map(AssetConnectionConfig.class::cast)
                    .toList())
                    .entrySet().stream()
                    .map(entry -> {
                        var representative = entry.getKey();
                        if (Objects.nonNull(entry.getValue())) {
                            entry.getValue().stream()
                                    .filter(connection -> connection != representative)
                                    .forEach(connection -> merge(connection, representative));
                        }
                        return representative;
                    })
                    .map(LambdaExceptionHelper.rethrowFunction(x -> x.newInstance(coreConfig, service)))
                    .map(AssetConnection.class::cast)
                    .collect(Collectors.toList());
        }
        catch (ConfigurationException e) {
            LOGGER.debug("skipped asset connection normalization (reason: re-creating asset connection from config after normalization failed)", e);
            return connections;
        }
    }


    /**
     * Normalizes a list of connection configs. Normalization means that there is only one connection config with the same
     * properties
     * containing all providers.
     *
     * @param connections the connection configs to normalize
     * @return normalized connection configs
     */
    private List<AssetConnectionConfig> normalizeConnectionConfigs(List<AssetConnectionConfig> connections) {
        if (Objects.isNull(connections)) {
            return new ArrayList<>();
        }
        return groupConfigs(connections).entrySet().stream()
                .map(entry -> {
                    AssetConnectionConfig representative = entry.getKey();
                    if (Objects.nonNull(entry.getValue())) {
                        entry.getValue().stream()
                                .filter(connection -> connection != representative)
                                .forEach(connection -> merge(connection, representative));
                    }
                    return representative;
                })
                .collect(Collectors.toList());
    }


    private void merge(AssetConnectionConfig source, AssetConnectionConfig target) {
        if (Objects.isNull(source)) {
            return;
        }
        if (Objects.isNull(target)) {
            target = source;
        }
        for (var providerType: AssetProviderType.values()) {
            Map<Reference, AssetProviderConfig> targetProviders = providerType.getProvidersFromConfigAccessor().apply(target);
            for (var sourceProvider: providerType.getProvidersFromConfigAccessor().apply(source).entrySet()) {
                if (targetProviders.containsKey(sourceProvider.getKey())) {
                    LOGGER.warn("Found multiple {} providers for element - all but one will be ignored (reference: {})",
                            providerType.name().toLowerCase(),
                            ReferenceHelper.asString(sourceProvider.getKey()));
                }
                else {
                    targetProviders.put(sourceProvider.getKey(), sourceProvider.getValue());
                }
            }
        }
    }


    private List<Message> apply(List<AssetConnectionConfig> newConnections) {
        List<Message> result = new ArrayList<>();
        for (var config: newConnections) {
            var connection = connections.stream().filter(x -> config.equalsIgnoringProviders(x.asConfig())).findFirst();
            if (connection.isPresent()) {
                result.addAll(addProvidersToConnection(connection.get(), config));
            }
            else {
                try {
                    AssetConnection newConnection = (AssetConnection) config.newInstance(coreConfig, service);
                    connections.add(newConnection);
                    if (started) {
                        setupConnectionAsync(newConnection);
                    }
                }
                catch (ConfigurationException e) {
                    result.add(Message.builder()
                            .messageType(MessageTypeEnum.EXCEPTION)
                            .text(String.format("Adding asset connection failed (reason: %s)",
                                    e.getMessage()))
                            .build());
                }
            }
        }
        var iterator = connections.iterator();
        while (iterator.hasNext()) {
            AssetConnection connection = iterator.next();
            Config connectionConfig = connection.asConfig();
            if (newConnections.stream().noneMatch(x -> x.equalsIgnoringProviders(connectionConfig))) {
                connection.stop();
                iterator.remove();
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


    private List<AssetConnectionConfig> mergeChanges(List<AssetConnection> currentConnections, ChangeSet changeSet) throws ExceptionWithDetails {
        List<Message> messages = new ArrayList<>();
        List<AssetConnectionConfig> result = currentConnections.stream()
                .map(AssetConnection::asConfig)
                .map(AssetConnectionConfig.class::cast)
                .collect(Collectors.toList());
        for (var toAdd: changeSet.add) {
            var connection = result.stream().filter(x -> toAdd.equalsIgnoringProviders(x)).findFirst();
            if (connection.isPresent()) {
                messages.addAll(addProviders(connection.get(), toAdd));
            }
            else {
                result.add(toAdd);
            }
        }
        for (var toDelete: changeSet.delete) {
            var connection = result.stream().filter(x -> toDelete.equalsIgnoringProviders(x)).findFirst();
            if (connection.isPresent()) {
                messages.addAll(deleteProviders(connection.get(), toDelete));
                boolean isEmpty = Stream.of(AssetProviderType.values())
                        .map(x -> x.getProvidersFromConfigAccessor().apply(connection.get()).isEmpty())
                        .allMatch(Boolean::booleanValue);

                if (isEmpty) {
                    result.remove(connection.get());
                }
            }
            else {
                messages.add(Message.builder()
                        .messageType(MessageTypeEnum.INFO)
                        .text("Deleting asset connection skipped (reason: connection does not exist)")
                        .build());
            }
        }
        if (messages.stream().anyMatch(x -> x.getMessageType() == MessageTypeEnum.ERROR || x.getMessageType() == MessageTypeEnum.EXCEPTION)) {
            throw new ExceptionWithDetails("failed to apply changes in asset connections", messages);
        }
        return result;
    }


    private List<Message> deleteProviders(AssetConnectionConfig target, AssetConnectionConfig source) {
        List<Message> result = new ArrayList<>();
        for (var providerType: AssetProviderType.values()) {
            for (var provider: providerType.getProvidersFromConfigAccessor().apply(source).entrySet()) {
                Reference reference = ReferenceHelper.findSameReference(providerType.getProvidersFromConfigAccessor().apply(target).keySet(), provider.getKey());
                if (Objects.nonNull(reference)) {
                    if (provider.getValue().sameAs(providerType.getProvidersFromConfigAccessor().apply(target).get(reference))) {
                        providerType.getProvidersFromConfigAccessor().apply(target).remove(reference);
                    }
                    else {
                        result.add(Message.builder()
                                .messageType(MessageTypeEnum.WARNING)
                                .text(String.format(
                                        "Failed to unregister %s provider (reference: %s, reason: existing provider details for reference differs from expected provider)",
                                        providerType.toString().toLowerCase(),
                                        ReferenceHelper.asString(reference)))
                                .build());
                    }
                }
                else {
                    result.add(Message.builder()
                            .messageType(MessageTypeEnum.INFO)
                            .text(String.format("Failed to unregister %s provider (reference: %s, reason: provider does not exist)",
                                    providerType.toString().toLowerCase(),
                                    ReferenceHelper.asString(reference)))
                            .build());
                }
            }
        }
        return result;
    }


    private List<Message> addProvidersToConnection(AssetConnection target, AssetConnectionConfig source) {
        List<Message> result = new ArrayList<>();
        for (var providerType: AssetProviderType.values()) {
            for (var provider: providerType.getProvidersFromConfigAccessor().apply(source).entrySet()) {
                Reference reference = ReferenceHelper.findSameReference(providerType.getProvidersFromConnectionAccessor().apply(target).keySet(), provider.getKey());
                if (Objects.nonNull(reference)) {
                    if (provider.getValue().sameAs(providerType.getProvidersFromConnectionAccessor().apply(target).get(reference))) {
                        LOGGER.debug("Skipped adding {} provider (reference: {}, reason: already exists)",
                                providerType.toString().toLowerCase(),
                                ReferenceHelper.asString(reference));
                    }
                    else {
                        LOGGER.debug("Skipped adding {} provider (reference: {}, reason: already exists but with different provider details)",
                                providerType.toString().toLowerCase(),
                                ReferenceHelper.asString(reference));
                    }
                }
                else {
                    try {
                        providerType.getRegisterProviderAccessor().accept(target, provider.getKey(), provider.getValue());
                        if (providerType == AssetProviderType.SUBSCRIPTION && target.isConnected()) {
                            setupSubscription(provider.getKey(), (AssetSubscriptionProvider) target.getSubscriptionProviders().get(provider.getKey()));
                        }
                    }
                    catch (AssetConnectionException e) {
                        result.add(Message.builder()
                                .messageType(MessageTypeEnum.EXCEPTION)
                                .text(String.format("Failed to register %s provider (reference: %s, reason: %s)",
                                        providerType.toString().toLowerCase(),
                                        ReferenceHelper.asString(reference),
                                        e.getMessage()))
                                .build());
                    }
                }
            }
            // iterate target and remove those not present in source
            for (var provider: providerType.getProvidersFromConnectionAccessor().apply(target).entrySet()) {
                Reference reference = ReferenceHelper.findSameReference(providerType.getProvidersFromConfigAccessor().apply(source).keySet(), provider.getKey());
                if (Objects.isNull(reference)) {
                    try {
                        providerType.getUnregisterProviderAccessor().accept(target, provider.getKey());
                    }
                    catch (AssetConnectionException e) {
                        result.add(Message.builder()
                                .messageType(MessageTypeEnum.EXCEPTION)
                                .text(String.format("Failed to unregister %s provider (reference: %s, reason: %s)",
                                        providerType.toString().toLowerCase(),
                                        ReferenceHelper.asString(reference),
                                        e.getMessage()))
                                .build());
                    }
                }
            }
        }
        return result;
    }


    private List<Message> addProviders(AssetConnectionConfig target, AssetConnectionConfig source) {
        List<Message> result = new ArrayList<>();
        for (var providerType: AssetProviderType.values()) {
            for (var provider: ((Map<Reference, AssetProviderConfig>) providerType.getProvidersFromConfigAccessor().apply(source)).entrySet()) {
                Reference reference = ReferenceHelper.findSameReference(providerType.getProvidersFromConfigAccessor().apply(target).keySet(), provider.getKey());
                if (Objects.nonNull(reference)) {
                    if (provider.getValue().equals(providerType.getProvidersFromConfigAccessor().apply(target).get(reference))) {
                        result.add(Message.builder()
                                .messageType(MessageTypeEnum.INFO)
                                .text(String.format("Skipped adding %s provider (reference: %s, reason: already exists)",
                                        providerType.toString().toLowerCase(),
                                        ReferenceHelper.asString(reference)))
                                .build());
                    }
                    else {
                        result.add(Message.builder()
                                .messageType(MessageTypeEnum.WARNING)
                                .text(String.format("Skipped adding %s provider (reference: %s, reason: already exists but with different provider details)",
                                        providerType.toString().toLowerCase(),
                                        ReferenceHelper.asString(reference)))
                                .build());
                    }
                }
                else {
                    providerType.getProvidersFromConfigAccessor().apply(target).put(provider.getKey(), provider.getValue());
                }
            }
        }
        return result;
    }


    private ChangeSet computeChangeSet(List<AssetConnectionConfig> oldConnectionConfigs, List<AssetConnectionConfig> newConnectionConfigs) {
        ChangeSet result = new ChangeSet();
        for (var oldConnectionConfig: oldConnectionConfigs) {
            var newConnectionConfig = newConnectionConfigs.stream()
                    .filter(x -> oldConnectionConfig.equalsIgnoringProviders(x))
                    .findFirst();
            if (!newConnectionConfig.isPresent()) {
                result.delete.add(oldConnectionConfig);
            }
            else {
                computeChangeSetForProviders(oldConnectionConfig, newConnectionConfig.get(), result);
            }
        }
        for (var newConnectionConfig: newConnectionConfigs) {
            if (oldConnectionConfigs.stream().noneMatch(x -> newConnectionConfig.equalsIgnoringProviders(x))) {
                result.add.add(newConnectionConfig);
            }
        }
        return result;
    }


    private void computeChangeSetForProviders(AssetConnectionConfig oldConnectionConfig, AssetConnectionConfig newConnectionConfig, ChangeSet changeSet) {
        AssetConnectionConfig connectionConfigAdd = cloneWithoutProviders(oldConnectionConfig);
        AssetConnectionConfig connectionConfigDelete = cloneWithoutProviders(oldConnectionConfig);
        for (var providerType: AssetProviderType.values()) {
            for (var oldProviderEntry: providerType.getProvidersFromConfigAccessor().apply(oldConnectionConfig).entrySet()) {
                // references might be slightly different although refering to the same element
                Reference referenceInNew = ReferenceHelper.findSameReference(providerType.getProvidersFromConfigAccessor().apply(newConnectionConfig).keySet(),
                        oldProviderEntry.getKey());
                if (Objects.isNull(referenceInNew)) {
                    // old has been deleted
                    providerType.getProvidersFromConfigAccessor().apply(connectionConfigDelete).put(oldProviderEntry.getKey(), oldProviderEntry.getValue());
                }
                else {
                    // still present by key, but provider settings might have changed
                    var newProviderConfig = providerType.getProvidersFromConfigAccessor().apply(newConnectionConfig).get(referenceInNew);
                    if (!oldProviderEntry.getValue().sameAs(newProviderConfig)) {
                        providerType.getProvidersFromConfigAccessor().apply(connectionConfigDelete).put(oldProviderEntry.getKey(), oldProviderEntry.getValue());
                        providerType.getProvidersFromConfigAccessor().apply(connectionConfigAdd).put(referenceInNew, newProviderConfig);
                    }
                }
            }
            for (var newProviderEntry: providerType.getProvidersFromConfigAccessor().apply(newConnectionConfig).entrySet()) {
                Reference referenceInOld = ReferenceHelper.findSameReference(providerType.getProvidersFromConfigAccessor().apply(oldConnectionConfig).keySet(),
                        newProviderEntry.getKey());
                if (Objects.isNull(referenceInOld)) {
                    providerType.getProvidersFromConfigAccessor().apply(connectionConfigAdd).put(newProviderEntry.getKey(), newProviderEntry.getValue());
                }
            }
        }
        if (hasProviders(connectionConfigAdd)) {
            changeSet.add.add(connectionConfigAdd);
        }
        if (hasProviders(connectionConfigDelete)) {
            changeSet.delete.add(connectionConfigDelete);
        }
    }


    private boolean hasProviders(AssetConnectionConfig connectionConfig) {
        return Objects.nonNull(connectionConfig)
                && Stream.of(AssetProviderType.values()).anyMatch(x -> !x.getProvidersFromConfigAccessor().apply(connectionConfig).isEmpty());
    }


    private AssetConnectionConfig cloneWithoutProviders(AssetConnectionConfig connectionConfig) {
        AssetConnectionConfig result = DeepCopyHelper.deepCopyAny(connectionConfig, AssetConnectionConfig.class);
        result.getValueProviders().clear();
        result.getSubscriptionProviders().clear();
        result.getOperationProviders().clear();
        return result;
    }


    private List<AssetConnectionConfig> clone(List<AssetConnectionConfig> input) {
        if (Objects.isNull(input)) {
            return new ArrayList<>();
        }
        return input.stream().map(x -> DeepCopyHelper.deepCopyAny(x, AssetConnectionConfig.class))
                .collect(Collectors.toList());
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
        executorService.execute(() -> {
            tryConnectingUntilSuccess(connection);
            setupSubscriptions(connection);
        });
    }


    private void validateConnectionConfigs(List<AssetConnectionConfig> connectionsConfigs) throws ConfigurationException {
        List<String> messages = new ArrayList<>();
        List<Reference> referencesForAllValueProviders = connectionsConfigs.stream()
                .filter(Objects::nonNull)
                .flatMap(x -> x.getValueProviders().keySet().stream())
                .toList();
        List<Reference> referencesForAllSubscriptionProviders = connectionsConfigs.stream()
                .filter(Objects::nonNull)
                .flatMap(x -> x.getSubscriptionProviders().keySet().stream())
                .toList();
        List<Reference> referencesForAllOperationProviders = connectionsConfigs.stream()
                .filter(Objects::nonNull)
                .flatMap(x -> x.getOperationProviders().keySet().stream())
                .toList();

        for (var providerType: AssetProviderType.values()) {
            List<Reference> referencesForAllProviders = connectionsConfigs.stream()
                    .flatMap(x -> providerType.getProvidersFromConfigAccessor().apply(x).keySet().stream())
                    .toList();
            List<List<Reference>> duplicateProviders = ReferenceHelper.groupBySame(referencesForAllProviders).stream()
                    .filter(x -> x.size() > 1)
                    .toList();
            for (var duplicates: duplicateProviders) {
                messages.add(String.format("Duplicate %s providers found for references (%s)",
                        providerType.toString().toLowerCase(),
                        duplicates.stream().map(ReferenceHelper::asString).collect(Collectors.joining(", "))));
            }
        }
        referencesForAllValueProviders.forEach(x -> {
            Reference duplicate = ReferenceHelper.findSameReference(referencesForAllOperationProviders, x);
            if (Objects.nonNull(duplicate)) {
                messages.add(String.format("Duplicate providers found (provider1: [value]%s, provider2: [operation]%s)",
                        ReferenceHelper.asString(x),
                        ReferenceHelper.asString(duplicate)));
            }
        });
        referencesForAllSubscriptionProviders.forEach(x -> {
            Reference duplicate = ReferenceHelper.findSameReference(referencesForAllOperationProviders, x);
            if (Objects.nonNull(duplicate)) {
                messages.add(String.format("Duplicate providers found (provider1: [subscription]%s, provider2: [operation]%s)",
                        ReferenceHelper.asString(x),
                        ReferenceHelper.asString(duplicate)));
            }
        });

        if (!messages.isEmpty()) {
            throw new InvalidConfigurationException(String.format("found %d validation errors for asset connections%s%s",
                    messages.size(),
                    System.lineSeparator(),
                    String.join(System.lineSeparator(), messages)));
        }
    }


    private void validateConnections(List<AssetConnection> connections) throws ConfigurationException {
        validateConnectionConfigs(connections.stream()
                .map(AssetConnection::asConfig)
                .filter(Objects::nonNull)
                .map(AssetConnectionConfig.class::cast)
                .toList());
    }

    private static class ChangeSet {
        private List<AssetConnectionConfig> add = new ArrayList<>();
        private List<AssetConnectionConfig> delete = new ArrayList<>();
    }

    private static class ExceptionWithDetails extends AssetConnectionException {

        private final List<Message> messages;

        public ExceptionWithDetails(String msg, List<Message> messages) {
            super(msg);
            this.messages = messages;
        }


        public ExceptionWithDetails(Throwable err, List<Message> messages) {
            super(err);
            this.messages = messages;
        }


        public ExceptionWithDetails(String msg, Throwable err, List<Message> messages) {
            super(msg, err);
            this.messages = messages;
        }


        public List<Message> getMessages() {
            return messages;
        }

    }
}
