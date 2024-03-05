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
package de.fraunhofer.iosb.ilt.faaast.service.starter.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.Config;
import de.fraunhofer.iosb.ilt.faaast.service.config.Configurable;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.EndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorageConfig;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.memory.FileStorageInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBusConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.PersistenceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.starter.model.ConfigOverride;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class for working with configuration files.
 */
public class ServiceConfigHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceConfigHelper.class);
    private static final Configuration JSON_PATH_CONFIG = Configuration
            .builder()
            .mappingProvider(new JacksonMappingProvider())
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .build();

    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    private ServiceConfigHelper() {}


    /**
     * Gets a default configuration.
     *
     * @return a default configuration
     */
    public static ServiceConfig getDefaultServiceConfig() {
        return new ServiceConfig.Builder()
                .core(new CoreConfig.Builder().requestHandlerThreadPoolSize(2).build())
                .persistence(new PersistenceInMemoryConfig())
                .fileStorage(new FileStorageInMemoryConfig())
                .endpoint(new HttpEndpointConfig())
                .messageBus(new MessageBusInternalConfig())
                .build();
    }


    /**
     * Loads config from a file.
     *
     * @param configFile config file
     * @return loaded config
     * @throws IOException if accessing file fails
     */
    public static ServiceConfig load(File configFile) throws IOException {
        return ServiceConfig.load(configFile);
    }


    /**
     * Loads config from a stream.
     *
     * @param configFile input stream
     * @return loaded config
     * @throws IOException if accessing stream fails
     */
    public static ServiceConfig load(InputStream configFile) throws IOException {
        return ServiceConfig.load(configFile);
    }


    /**
     * Tries to auto-complete a configuration by adding default values for missing mandatory parts.
     *
     * @param config the config to auto-complete
     * @return the modified config
     */
    public static ServiceConfig autoComplete(ServiceConfig config) {
        ServiceConfig defaultConfig = getDefaultServiceConfig();
        if (config.getCore() == null) {
            config.setCore(defaultConfig.getCore());
            LOGGER.debug("No configuration for core found - using default");
        }
        if (config.getEndpoints() == null || config.getEndpoints().isEmpty()) {
            config.setEndpoints(defaultConfig.getEndpoints());
            LOGGER.debug("No configuration for endpoints found - using default");
        }
        if (config.getPersistence() == null) {
            config.setPersistence(defaultConfig.getPersistence());
            LOGGER.debug("No configuration for persistence found - using default");
        }
        if (config.getFileStorage() == null) {
            config.setFileStorage(defaultConfig.getFileStorage());
            LOGGER.debug("No configuration for file storage found - using default");
        }
        if (config.getMessageBus() == null) {
            config.setMessageBus(defaultConfig.getMessageBus());
            LOGGER.debug("No configuration for messageBus found - using default");
        }
        return config;
    }


    /**
     * Builds a new config with updated values from {@code properties}.
     *
     * @param config the input config
     * @param properties properties to update, keys are JSONPath expressions, values the new values for that JSONPath
     * @return a new config with updated properties
     * @throws JsonProcessingException if deserializing updated config fails
     */
    public static ServiceConfig withProperties(ServiceConfig config, List<ConfigOverride> properties) throws JsonProcessingException {
        if (properties == null || properties.isEmpty()) {
            return config;
        }
        DocumentContext document = JsonPath.using(JSON_PATH_CONFIG).parse(mapper.valueToTree(config));
        properties.forEach(x -> {
            String jsonPath = String.format("$.%s", x.getUpdatedKey());
            try {
                document.set(jsonPath, x.getValue());
            }
            catch (JsonPathException e) {
                throw new JsonPathException(String.format("updating property failed (key: %s, value: %s)", x.getUpdatedKey(), x.getValue()), e);
            }
        });
        return mapper.treeToValue(document.json(), ServiceConfig.class);
    }


    private static <T extends Config> void applySingle(List<Config<? extends Configurable>> configs, Class<T> configType, Consumer<T> updater)
            throws InvalidConfigurationException {
        List<T> configsForType = configs.stream()
                .filter(x -> configType.isAssignableFrom(x.getClass()))
                .map(configType::cast)
                .collect(Collectors.toList());
        if (configsForType.size() > 1) {
            throw new InvalidConfigurationException(String.format("configuration exception - found %d configurations of type %s but expected at most 1",
                    configsForType.size(),
                    configType));
        }
        if (!configsForType.isEmpty()) {
            updater.accept(configsForType.get(0));
        }
    }


    private static <T extends Config> void applyMultiple(List<Config<? extends Configurable>> configs, Class<T> configType, Consumer<List<T>> updater) {
        List<T> configsForType = configs.stream()
                .filter(x -> configType.isAssignableFrom(x.getClass()))
                .map(configType::cast)
                .collect(Collectors.toList());
        if (!configsForType.isEmpty()) {
            updater.accept(configsForType);
        }
    }


    /**
     * Updates {@code config} with data from other configs.
     *
     * @param config target config
     * @param configs configs that should be merged into target config
     * @throws InvalidConfigurationException if configs to be merged contain multiple elements of a type that the target
     *             config can only have one
     */
    public static void apply(ServiceConfig config, List<Config<? extends Configurable>> configs) throws InvalidConfigurationException {
        if (config != null && configs != null) {
            applyMultiple(configs, EndpointConfig.class, config::setEndpoints);
            applySingle(configs, PersistenceConfig.class, config::setPersistence);
            applySingle(configs, FileStorageConfig.class, config::setFileStorage);
            applySingle(configs, MessageBusConfig.class, config::setMessageBus);
            applyMultiple(configs, AssetConnectionConfig.class, x -> config.getAssetConnections().addAll(x));
        }
    }
}
