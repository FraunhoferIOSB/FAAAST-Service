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
import io.adminshell.aas.v3.model.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class AssetConnectionManager {

    private List<AssetConnection> connections = new ArrayList<>();
    private final CoreConfig coreConfig;
    private final ServiceContext context;

    public AssetConnectionManager(CoreConfig coreConfig, List<AssetConnection> connections, ServiceContext context) throws ConfigurationException {
        this.coreConfig = coreConfig;
        this.connections = connections;
        this.context = context;
        validateConnections();
    }


    /**
     * Adds a new AssetConnection created from an AssetConnectionConfig
     *
     * @param connectionConfig the AssetConnectionConfig describing the
     *            AssetConnection to add
     * @throws
     * de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException
     *             when provided connectionConfig is invalid
     */
    public void add(AssetConnectionConfig<? extends AssetConnection, ? extends AssetValueProviderConfig, ? extends AssetOperationProviderConfig, ? extends AssetSubscriptionProviderConfig> connectionConfig)
            throws ConfigurationException {
        AssetConnection newConnection = (AssetConnection) connectionConfig.newInstance(coreConfig, context);
        if (connections.stream().anyMatch(x -> x.sameAs(newConnection))) {
            AssetConnection connection = connections.stream().filter(x -> x.sameAs(newConnection)).findFirst().get();
            connectionConfig.getValueProviders().forEach((k, v) -> {
                try {
                    connection.registerValueProvider(k, (AssetValueProviderConfig) v);
                }
                catch (AssetConnectionException ex) {
                    // TODO rethrow
                }
            });
            connectionConfig.getOperationProviders().forEach((k, v) -> {
                try {
                    connection.registerOperationProvider(k, (AssetOperationProviderConfig) v);
                }
                catch (AssetConnectionException ex) {
                    // TODO rethrow
                }
            });
            connectionConfig.getSubscriptionProviders().forEach((k, v) -> {
                try {
                    connection.registerSubscriptionProvider(k, (AssetSubscriptionProviderConfig) v);
                }
                catch (AssetConnectionException ex) {
                    // TODO rethrow
                }
            });
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
     * Returns whether there is a operation provider defined for the provided
     * AAS element or not.
     *
     * @param reference AAS element
     * @return true if there is a operation provider defined for the provided
     *         AAS element, otherwise false
     */
    public boolean hasOperationProvider(Reference reference) {
        return connections.stream().anyMatch(x -> x.getOperationProviders().containsKey(reference));
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
        Map<String, List<AssetValueProvider>> valueProviders = connections.stream()
                .flatMap(x -> (Stream<Map.Entry<String, AssetValueProvider>>) x.getValueProviders().entrySet().stream())
                .collect(Collectors.groupingBy(x -> x.getKey(), Collectors.mapping(x -> x.getValue(), Collectors.toList()))).entrySet().stream()
                .filter(x -> x.getValue().size() > 1).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
        for (String reference: valueProviders.keySet()) {
            throw new InvalidConfigurationException(
                    String.format("found %d value connections for reference %s but maximum 1 allowed", valueProviders.get(reference).size(), reference));
        }
        Map<String, List<AssetOperationProvider>> operationProviders = connections.stream()
                .flatMap(x -> (Stream<Map.Entry<String, AssetOperationProvider>>) x.getOperationProviders().entrySet().stream())
                .collect(Collectors.groupingBy(x -> x.getKey(), Collectors.mapping(x -> x.getValue(), Collectors.toList()))).entrySet().stream()
                .filter(x -> x.getValue().size() > 1).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
        for (String reference: operationProviders.keySet()) {
            throw new InvalidConfigurationException(
                    String.format("found %d operation connections for reference %s but maximum 1 allowed", operationProviders.get(reference).size(), reference));
        }
        Map<String, List<AssetSubscriptionProvider>> subscriptionProviders = connections.stream()
                .flatMap(x -> (Stream<Map.Entry<String, AssetSubscriptionProvider>>) x.getSubscriptionProviders().entrySet().stream())
                .collect(Collectors.groupingBy(x -> x.getKey(), Collectors.mapping(x -> x.getValue(), Collectors.toList()))).entrySet().stream()
                .filter(x -> x.getValue().size() > 1).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
        for (String reference: subscriptionProviders.keySet()) {
            throw new InvalidConfigurationException(
                    String.format("found %d subscription connections for reference %s but maximum 1 allowed", subscriptionProviders.get(reference).size(), reference));
        }
    }
}
