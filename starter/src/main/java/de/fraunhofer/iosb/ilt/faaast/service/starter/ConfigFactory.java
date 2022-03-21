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
package de.fraunhofer.iosb.ilt.faaast.service.starter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.Config;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.EndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBusConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.PersistenceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for creating the {@link de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig}
 */
public class ConfigFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFactory.class);
    public static final String DEFAULT_CONFIG_JSON = "default-config.json";

    private ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    /**
     * Get the default {@link de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig}
     *
     * @return the default Service Configuration
     * @throws Exception if fails
     */
    public ServiceConfig getDefaultServiceConfig() throws Exception {
        return getDefaultServiceConfig(new HashMap<>());
    }


    /**
     * Get the default {@link de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig} with adjustments
     *
     * @param properties the adjustments for the default configuration file.
     * @return the adjusted default configuration
     * @throws Exception if fails
     */
    public ServiceConfig getDefaultServiceConfig(Map<String, Object> properties) throws Exception {
        if (properties == null) {
            properties = new HashMap<>();
        }
        try {
            return applyCommandlineProperties(properties, getDefaultConfig());
        }
        catch (IOException e) {
            throw new Exception("Configuration Error: " + e.getMessage());
        }
    }


    /**
     * Parses a given file to a {@link de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig} object
     *
     * @param configFile config file
     * @return the parsed ServiceConfig object
     * @throws Exception if fails
     */
    public ServiceConfig toServiceConfig(File configFile) throws Exception {
        return toServiceConfig(Files.readString(configFile.toPath()));
    }


    /**
     * Parses a given file path to a {@link de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig} object
     *
     * @param pathToConfigFile path to config file
     * @return the parsed ServiceConfig object
     * @throws Exception if fails
     */
    public ServiceConfig toServiceConfig(String pathToConfigFile) throws Exception {
        return toServiceConfig(pathToConfigFile, true, new HashMap<>(), null);
    }


    /**
     * Parses a given file path to a {@link de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig} object.
     * Adjust it with the given properties.
     *
     * @param pathToConfigFile path to config file
     * @param autoCompleteConfiguration if yes then missing components in the given config file are added
     *            with default values
     * @param commandLineProperties the adjustments for the default configuration file.
     * @param customConfigs list of custom configuration which should be applied
     * @return the parsed ServiceConfig object
     * @throws Exception if fails
     */
    public ServiceConfig toServiceConfig(String pathToConfigFile,
                                         boolean autoCompleteConfiguration,
                                         Map<String, Object> commandLineProperties,
                                         List<Config> customConfigs)
            throws Exception {
        try {
            JsonNode configNode = mapper.readTree(Files.readString(Path.of(pathToConfigFile)));
            ServiceConfig serviceConfig = mapper.readValue(mapper.writeValueAsString(configNode), ServiceConfig.class);
            LOGGER.info("Read config file '" + pathToConfigFile + "'");
            if (autoCompleteConfiguration) {
                autocompleteServiceConfiguration(serviceConfig);
            }
            if (customConfigs != null && !customConfigs.isEmpty()) {
                LOGGER.debug("Applying costum config components to config file");
                applyToServiceConfig(serviceConfig, customConfigs);
            }

            if (commandLineProperties != null && !commandLineProperties.isEmpty()) {
                LOGGER.debug("Applying properties to config file");
                serviceConfig = applyCommandlineProperties(commandLineProperties, serviceConfig);
            }

            LOGGER.info("Successfully read config file");
            LOGGER.debug("Used configuration file\n" + mapper.writeValueAsString(serviceConfig));
            return serviceConfig;
        }
        catch (NoSuchFileException e) {
            if (pathToConfigFile.equalsIgnoreCase(Application.DEFAULT_CONFIG_PATH)) {
                LOGGER.info("No custom configuration file was found");
                LOGGER.info("Using default configuration file");
                ServiceConfig serviceConfig = getDefaultServiceConfig(commandLineProperties);
                applyToServiceConfig(serviceConfig, customConfigs);
                LOGGER.debug("Used configuration file\n" + mapper.writeValueAsString(serviceConfig));
                return serviceConfig;
            }
            else {
                throw new Exception("Configuration Error - Could not find configuration file: " + pathToConfigFile);
            }

        }
        catch (IOException e) {
            throw new Exception("Configuration Error: " + e.getMessage());
        }
    }


    private void autocompleteServiceConfiguration(ServiceConfig serviceConfig) throws Exception {
        ServiceConfig defaultConfig = getDefaultServiceConfig();
        if (serviceConfig.getCore() == null) {
            serviceConfig.setCore(defaultConfig.getCore());
            LOGGER.debug("No configuration for core was found");
            LOGGER.debug("Using default configuration for core");
        }
        if (serviceConfig.getEndpoints() == null || serviceConfig.getEndpoints().size() == 0) {
            serviceConfig.setEndpoints(defaultConfig.getEndpoints());
            LOGGER.debug("No configuration for endpoints was found");
            LOGGER.debug("Using default configuration for endpoints");
        }
        if (serviceConfig.getPersistence() == null) {
            serviceConfig.setPersistence(defaultConfig.getPersistence());
            LOGGER.debug("No configuration for persistence was found");
            LOGGER.debug("Using default configuration for persistence");
        }
        if (serviceConfig.getMessageBus() == null) {
            serviceConfig.setMessageBus(defaultConfig.getMessageBus());
            LOGGER.debug("No configuration for messagebus was found");
            LOGGER.debug("Using default configuration for messagebus");
        }
    }


    private ServiceConfig applyCommandlineProperties(Map<String, Object> properties, ServiceConfig serviceConfig) throws Exception {
        JsonNode configNode = mapper.readTree(mapper.writeValueAsString(serviceConfig));
        for (Map.Entry<String, Object> prop: properties.entrySet()) {
            List<String> pathList = List.of(prop.getKey().split("\\."));
            JsonNode jsonNode = configNode;
            for (int i = 0; i < pathList.size() - 1; i++) {
                String path = pathList.get(i);
                if (isNumeric(path)) {
                    jsonNode = jsonNode.path(Integer.parseInt(path));
                }
                else {
                    jsonNode = jsonNode.path(path);
                }
            }
            if (MissingNode.class.isAssignableFrom(jsonNode.getClass()) || !jsonNode.has(pathList.get(pathList.size() - 1))) {
                throw new Exception("Configuration Error: Could not find attribute with path '" + prop.getKey() + "' in config file");
            }
            ((ObjectNode) jsonNode).put(pathList.get(pathList.size() - 1), prop.getValue().toString());
            LOGGER.debug("Apply config property '" + prop.getKey() + "' with value '" + prop.getValue().toString() + "'");
        }
        return mapper.readValue(mapper.writeValueAsString(configNode), ServiceConfig.class);
    }


    private ServiceConfig getDefaultConfig() throws IOException {
        return getDefaultConfigWithCustomConfig(null, null, null, null, null);
    }


    private ServiceConfig getDefaultConfigWithCustomConfig(List<EndpointConfig> endpointConfig,
                                                           MessageBusConfig messageBusConfig,
                                                           PersistenceConfig persistenceConfig,
                                                           CoreConfig coreConfig,
                                                           List<AssetConnectionConfig> assetConnectionConfigs)
            throws IOException {
        ServiceConfig serviceConfig = new ServiceConfig.Builder()
                .core(coreConfig != null ? coreConfig : new CoreConfig.Builder().requestHandlerThreadPoolSize(2).build())
                .endpoints(endpointConfig != null ? endpointConfig : List.of(new HttpEndpointConfig()))
                .persistence(persistenceConfig != null ? persistenceConfig : new PersistenceInMemoryConfig())
                .messageBus(messageBusConfig != null ? messageBusConfig : new MessageBusInternalConfig())
                .assetConnections(assetConnectionConfigs)
                .build();
        return serviceConfig;
    }


    private void applyToServiceConfig(ServiceConfig serviceConfig, List<Config> configs) throws Exception {
        boolean isEndpointAlreadySet = false;
        for (Config c: configs) {
            LOGGER.debug("Apply custom config parameter '" + c.getClass().getSimpleName() + "'");
            if (EndpointConfig.class.isAssignableFrom(c.getClass())) {
                //if yet no endpoint was set remove old enpoints (most likely default endpoint) and set new endpoint
                //else add the new endpoint to the existing list of endpoints
                serviceConfig.setEndpoints(!isEndpointAlreadySet ? new ArrayList<>() {
                    {
                        add((EndpointConfig) c);
                    }
                } : new ArrayList<>() {
                    {
                        addAll(serviceConfig.getEndpoints());
                        add((EndpointConfig) c);
                    }
                });
                isEndpointAlreadySet = true;
                continue;
            }
            if (PersistenceConfig.class.isAssignableFrom(c.getClass())) {
                serviceConfig.setPersistence((PersistenceConfig) c);
                continue;
            }
            if (MessageBusConfig.class.isAssignableFrom(c.getClass())) {
                serviceConfig.setMessageBus((MessageBusConfig) c);
                continue;
            }
            if (AssetConnectionConfig.class.isAssignableFrom(c.getClass())) {
                serviceConfig.getAssetConnections().add((AssetConnectionConfig) c);
                continue;
            }
            throw new Exception("Cannot set config component '" + c.getClass().getSimpleName() + "'");
        }
    }


    private boolean isNumeric(String s) {
        if (s == null || s.equalsIgnoreCase("")) {
            return false;
        }
        try {
            Integer.parseInt(s);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }
}
