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

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.SetSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class AssetConnectionManager {

    private final List<AssetConnection> connections;
    private final CoreConfig coreConfig;
    private final ServiceContext serviceContext;

    public AssetConnectionManager(CoreConfig coreConfig, List<AssetConnection> connections, ServiceContext context) throws ConfigurationException, AssetConnectionException {
        this.coreConfig = coreConfig;
        this.connections = connections != null ? connections : new ArrayList<>();
        this.serviceContext = context;
        validateConnections();
        for (var assetConnection: connections) {
            final Map<Reference, AssetSubscriptionProvider> subscriptionProviders = assetConnection.getSubscriptionProviders();
            for (var subscriptionInfo: subscriptionProviders.entrySet()) {
                subscriptionInfo.getValue().addNewDataListener((DataElementValue data) -> {
                    serviceContext.execute(SetSubmodelElementValueByPathRequest.builder()
                            .id(new DefaultIdentifier.Builder()
                                    .identifier(subscriptionInfo.getKey().getKeys().get(0).getValue())
                                    .idType(IdentifierType.IRI)
                                    .build())
                            .path(subscriptionInfo.getKey().getKeys().subList(1, subscriptionInfo.getKey().getKeys().size()))
                            .value(data)
                            .build());
                });
            }
        }
    }


    /**
     * Adds a new AssetConnection created from an AssetConnectionConfig
     *
     * @param connectionConfig the AssetConnectionConfig describing the
     *            AssetConnection to add
     * @throws
     * de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException if
     *             provided connectionConfig is invalid
     * @throws
     * de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException
     *             if initializing asset connection fails
     */
    public void add(AssetConnectionConfig<? extends AssetConnection, ? extends AssetValueProviderConfig, ? extends AssetOperationProviderConfig, ? extends AssetSubscriptionProviderConfig> connectionConfig)
            throws ConfigurationException, AssetConnectionException {
        AssetConnection newConnection = connectionConfig.newInstance(coreConfig, serviceContext);
        if (connections.stream().anyMatch(x -> Objects.equals(x, newConnection))) {
            AssetConnection connection = connections.stream().filter(x -> Objects.equals(x, newConnection)).findFirst().get();
            connectionConfig.getValueProviders().forEach(LambdaExceptionHelper.rethrowBiConsumer(
                    (k, v) -> connection.registerValueProvider(k, (AssetValueProviderConfig) v)));
            connectionConfig.getSubscriptionProviders().forEach(LambdaExceptionHelper.rethrowBiConsumer(
                    (k, v) -> connection.registerSubscriptionProvider(k, (AssetSubscriptionProviderConfig) v)));
            connectionConfig.getOperationProviders().forEach(LambdaExceptionHelper.rethrowBiConsumer(
                    (k, v) -> connection.registerOperationProvider(k, (AssetOperationProviderConfig) v)));
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
     * Gets the operation provider for the AAS element defined by reference
     *
     * @param reference AAS element
     * @return operation provider for the AAS element defined by reference or
     *         null if there is none defined
     */
    public AssetOperationProvider getOperationProvider(Reference reference) {
        return connections.stream().filter(x -> x.getOperationProviders().containsKey(reference)).map(x -> (AssetOperationProvider) x.getOperationProviders().get(reference))
                .findFirst().orElse(null);
    }


    /**
     * Gets the subscription provider for the AAS element defined by reference
     *
     * @param reference AAS element
     * @return subscription provider for the AAS element defined by reference or
     *         null if there is none defined
     */
    public AssetSubscriptionProvider getSubscriptionProvider(Reference reference) {
        return connections.stream().filter(x -> x.getSubscriptionProviders().containsKey(reference))
                .map(x -> (AssetSubscriptionProvider) x.getSubscriptionProviders().get(reference)).findFirst().orElse(null);
    }


    /**
     * Gets the value provider for the AAS element defined by reference
     *
     * @param reference AAS element
     * @return value provider for the AAS element defined by reference or null
     *         if there is none defined
     */
    public AssetValueProvider getValueProvider(Reference reference) {
        return connections.stream().filter(x -> x.getValueProviders().containsKey(reference)).map(x -> (AssetValueProvider) x.getValueProviders().get(reference)).findFirst()
                .orElse(null);
    }


    /**
     * If a {@link AssetValueProvider} exists for given reference, the provided will
     * be written; otherwise nothing happens
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
        Reference temp = reference;
        try {
            ReferenceHelper.completeReferenceWithProperKeyElements(temp, this.serviceContext.getAASEnvironment());
        }
        catch (ResourceNotFoundException ex) {
            // ignore
        }
        return connections.stream().anyMatch(x -> x.getOperationProviders().containsKey(temp));
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
        return connections.stream().anyMatch(x -> x.getSubscriptionProviders().containsKey(reference));
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
        return connections.stream().anyMatch(x -> x.getValueProviders().containsKey(reference));
    }


    private void validateConnections() throws ConfigurationException {
        Map<Reference, List<AssetValueProvider>> valueProviders = connections.stream()
                .flatMap(x -> (Stream<Map.Entry<Reference, AssetValueProvider>>) x.getValueProviders().entrySet().stream())
                .collect(Collectors.groupingBy(x -> x.getKey(), Collectors.mapping(x -> x.getValue(), Collectors.toList()))).entrySet().stream()
                .filter(x -> x.getValue().size() > 1).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
        for (Reference reference: valueProviders.keySet()) {
            throw new InvalidConfigurationException(
                    String.format("found %d value connections for reference %s but maximum 1 allowed", valueProviders.get(reference).size(), reference));
        }
        Map<Reference, List<AssetOperationProvider>> operationProviders = connections.stream()
                .flatMap(x -> (Stream<Map.Entry<Reference, AssetOperationProvider>>) x.getOperationProviders().entrySet().stream())
                .collect(Collectors.groupingBy(x -> x.getKey(), Collectors.mapping(x -> x.getValue(), Collectors.toList()))).entrySet().stream()
                .filter(x -> x.getValue().size() > 1).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
        for (Reference reference: operationProviders.keySet()) {
            throw new InvalidConfigurationException(
                    String.format("found %d operation connections for reference %s but maximum 1 allowed", operationProviders.get(reference).size(), reference));
        }
        Map<Reference, List<AssetSubscriptionProvider>> subscriptionProviders = connections.stream()
                .flatMap(x -> (Stream<Map.Entry<Reference, AssetSubscriptionProvider>>) x.getSubscriptionProviders().entrySet().stream())
                .collect(Collectors.groupingBy(x -> x.getKey(), Collectors.mapping(x -> x.getValue(), Collectors.toList()))).entrySet().stream()
                .filter(x -> x.getValue().size() > 1).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
        for (Reference reference: subscriptionProviders.keySet()) {
            throw new InvalidConfigurationException(
                    String.format("found %d subscription connections for reference %s but maximum 1 allowed", subscriptionProviders.get(reference).size(), reference));
        }
    }
}
