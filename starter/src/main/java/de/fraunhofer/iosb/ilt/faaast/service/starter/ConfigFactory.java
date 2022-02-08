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
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for creating the service configuration
 */
public class ConfigFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFactory.class);
    public static final String DEFAULT_CONFIG_JSON = "default-config.json";

    private ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    /**
     * Get the default Service Configuration
     * 
     * @return the default Service Configuration
     * @throws Exception
     */
    public ServiceConfig getDefaultServiceConfig() throws Exception {
        return getDefaultServiceConfig(new HashMap<>());
    }


    /**
     * Get the default Service Configuration with adjustments
     *
     * @param properties the adjustments for the default configuration file.
     * @return the adjusted default configuration
     * @throws Exception
     */
    public ServiceConfig getDefaultServiceConfig(Map<String, Object> properties) throws Exception {
        if (properties == null) {
            properties = new HashMap<>();
        }
        try {
            JsonNode configNode = readDefaultConfigFile();
            applyCommandlineProperties(properties, configNode);
            return mapper.readValue(mapper.writeValueAsString(configNode), ServiceConfig.class);
        }
        catch (IOException e) {
            throw new Exception("Configuration Error: " + e.getMessage());
        }
    }


    /**
     * Parses a given file to a ServiceConfig object
     *
     * @param configFile
     * @return the parsed ServiceConfig object
     * @throws Exception
     */
    public ServiceConfig toServiceConfig(File configFile) throws Exception {
        return toServiceConfig(Files.readString(configFile.toPath()));
    }


    /**
     * Parses a given file path to a ServiceConfig object
     *
     * @param pathToConfigFile
     * @return the parsed ServiceConfig object
     * @throws Exception
     */
    public ServiceConfig toServiceConfig(String pathToConfigFile) throws Exception {
        return toServiceConfig(pathToConfigFile, Application.autoCompleteConfiguration, new HashMap<>());
    }


    /**
     * Parses a given file path to a ServiceConfig object.
     * Adjust it with the given properties.
     *
     * @param pathToConfigFile
     * @param autoCompleteConfiguration if yes then missing components in the given config file are added
     *            with default values
     * @param commandLineProperties the adjustments for the default configuration file.
     * @return the parsed ServiceConfig object
     * @throws Exception
     */
    public ServiceConfig toServiceConfig(String pathToConfigFile, boolean autoCompleteConfiguration, Map<String, Object> commandLineProperties)
            throws Exception {
        try {
            JsonNode configNode = mapper.readTree(Files.readString(Path.of(pathToConfigFile)));
            LOGGER.info("Read config file '" + pathToConfigFile + "'");
            if (commandLineProperties != null && !commandLineProperties.isEmpty()) {
                LOGGER.debug("Applying properties to config file");
                applyCommandlineProperties(commandLineProperties, configNode);
            }
            ServiceConfig serviceConfig = mapper.readValue(mapper.writeValueAsString(configNode), ServiceConfig.class);
            LOGGER.info("Successfully read config file");
            if (autoCompleteConfiguration) {
                autocompleteServiceConfiguration(serviceConfig);
            }
            LOGGER.debug("Used configuration file\n" + mapper.writeValueAsString(serviceConfig));
            return serviceConfig;
        }
        catch (NoSuchFileException ex) {
            if (pathToConfigFile.equalsIgnoreCase(Application.DEFAULT_CONFIG_PATH)) {
                LOGGER.info("No custom configuration file was found");
                LOGGER.info("Using default configuration file");
                ServiceConfig serviceConfig = getDefaultServiceConfig(commandLineProperties);
                LOGGER.debug("Used configuration file\n" + mapper.writeValueAsString(serviceConfig));
                return serviceConfig;
            }
            else {
                throw new Exception("Configuration Error - Could not find configuration file: " + pathToConfigFile);
            }

        }
        catch (IOException ex) {
            throw new Exception("Configuration Error: " + ex.getMessage());
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


    private void applyCommandlineProperties(Map<String, Object> properties, JsonNode configNode) throws Exception {
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
    }


    private JsonNode readDefaultConfigFile() throws IOException {
        //LOGGER.info("Read default config file '" + DEFAULT_CONFIG_JSON + "'");
        JsonNode configNode = mapper.readTree(ConfigFactory.class.getClassLoader().getResource(DEFAULT_CONFIG_JSON));
        return configNode;
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
