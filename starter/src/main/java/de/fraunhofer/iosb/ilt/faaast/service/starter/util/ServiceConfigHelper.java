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
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBusConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.PersistenceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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


    public static ServiceConfig getDefaultServiceConfig() {
        return new ServiceConfig.Builder()
                .core(new CoreConfig.Builder().requestHandlerThreadPoolSize(2).build())
                .persistence(new PersistenceInMemoryConfig())
                .endpoint(new HttpEndpointConfig())
                .messageBus(new MessageBusInternalConfig())
                .build();
    }


    public static ServiceConfig load(File configFile) throws IOException {
        return mapper.readValue(configFile, ServiceConfig.class);
    }


    public static ServiceConfig load(InputStream configFile) throws IOException {
        return mapper.readValue(configFile, ServiceConfig.class);
    }


    public static void autoComplete(ServiceConfig serviceConfig) {
        ServiceConfig defaultConfig = getDefaultServiceConfig();
        if (serviceConfig.getCore() == null) {
            serviceConfig.setCore(defaultConfig.getCore());
            LOGGER.debug("No configuration for core found - using default");
        }
        if (serviceConfig.getEndpoints() == null || serviceConfig.getEndpoints().isEmpty()) {
            serviceConfig.setEndpoints(defaultConfig.getEndpoints());
            LOGGER.debug("No configuration for endpoints found - using default");
        }
        if (serviceConfig.getPersistence() == null) {
            serviceConfig.setPersistence(defaultConfig.getPersistence());
            LOGGER.debug("No configuration for persistence found - using default");
        }
        if (serviceConfig.getMessageBus() == null) {
            serviceConfig.setMessageBus(defaultConfig.getMessageBus());
            LOGGER.debug("No configuration for messageBus found - using default");
        }
    }


    public static ServiceConfig withProperties(ServiceConfig config, Map<String, ?> properties) throws JsonProcessingException {
        if (properties == null || properties.isEmpty()) {
            return config;
        }
        DocumentContext document = JsonPath.using(JSON_PATH_CONFIG).parse(mapper.valueToTree(config));
        properties.forEach((k, v) -> {
            String jsonPath = String.format("$.%s", k);
            try {
                document.set(jsonPath, v);
            }
            catch (JsonPathException e) {
                throw new JsonPathException(String.format("updating property failed (key: %s, value: %s)", k, v), e);
            }
        });
        return mapper.treeToValue(document.json(), ServiceConfig.class);
    }


    private static <T extends Config> void applySingle(List<Config<? extends Configurable>> configs, Class<T> configType, Consumer<T> updater)
            throws InvalidConfigurationException {
        List<T> configsForType = configs.stream()
                .filter(x -> configType.isAssignableFrom(x.getClass()))
                .map(x -> (T) x)
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


    private static <T extends Config> void applyMultiple(List<Config<? extends Configurable>> configs, Class<T> configType, Consumer<List<T>> updater)
            throws InvalidConfigurationException {
        List<T> configsForType = configs.stream()
                .filter(x -> configType.isAssignableFrom(x.getClass()))
                .map(x -> (T) x)
                .collect(Collectors.toList());
        if (!configsForType.isEmpty()) {
            updater.accept(configsForType);
        }
    }


    public static void apply(ServiceConfig config, List<Config<? extends Configurable>> configs) throws InvalidConfigurationException {
        if (config != null && configs != null) {
            applyMultiple(configs, EndpointConfig.class, config::setEndpoints);
            applySingle(configs, PersistenceConfig.class, config::setPersistence);
            applySingle(configs, MessageBusConfig.class, config::setMessageBus);
            applyMultiple(configs, AssetConnectionConfig.class, x -> config.getAssetConnections().addAll(x));
        }
    }
}
