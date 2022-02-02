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
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;


public class ConfigFactory {

    private static final String DEFAULT_CONFIG_JSON = "default-config.json";

    private static ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    protected static ServiceConfig getServiceConfig(CommandLine cmd) throws IOException {
        ServiceConfig config;
        if (cmd.hasOption(CommandLineFactory.CMD_CONFIG_PARAMETER)) {
            config = ConfigFactory.toServiceConfig(cmd.getOptionValue(CommandLineFactory.CMD_CONFIG_PARAMETER),
                    cmd.hasOption(CommandLineFactory.CMD_COMPLETE_CONFIGURATION_PARAMETER),
                    cmd.getOptionProperties(CommandLineFactory.CMD_ENVIRONMENT_PARAMETER));
            Application.print("Using " + cmd.getOptionValue(CommandLineFactory.CMD_CONFIG_PARAMETER) + " as config file");
        }
        else if (new File(Application.DEFAULT_CONFIG_PATH).exists()) {
            config = ConfigFactory.toServiceConfig(Application.DEFAULT_CONFIG_PATH,
                    cmd.hasOption(CommandLineFactory.CMD_COMPLETE_CONFIGURATION_PARAMETER),
                    cmd.getOptionProperties(CommandLineFactory.CMD_ENVIRONMENT_PARAMETER));
            Application.print("Using " + Application.DEFAULT_CONFIG_PATH + " as config file");
        }
        else {
            config = ConfigFactory.getDefaultServiceConfig(cmd.getOptionProperties(CommandLineFactory.CMD_ENVIRONMENT_PARAMETER));
            Application.print("No config file was found - using default config");
        }
        return config;
    }


    public static ServiceConfig getDefaultServiceConfig() throws JsonProcessingException {
        return getDefaultServiceConfig(new Properties());
    }


    public static ServiceConfig getDefaultServiceConfig(Properties properties) throws JsonProcessingException {
        if (properties == null) {
            properties = new Properties();
        }
        try {
            JsonNode configNode = readDefaultConfigFile();
            applyCommandlineProperties(properties, configNode);
            Application.print("Used Configuration:\n" + mapper.writeValueAsString(configNode));
            ServiceConfig config = mapper.readValue(mapper.writeValueAsString(configNode), ServiceConfig.class);
            return config;
        }
        catch (IOException e) {
            Application.print("Configuration Error: " + e.getMessage());
            System.exit(1);
        }
        return null;
    }


    public static ServiceConfig toServiceConfig(File configFile) throws IOException {
        return toServiceConfig(Files.readString(configFile.toPath()));
    }


    public static ServiceConfig toServiceConfig(String pathToConfigFile) throws IOException {
        return toServiceConfig(pathToConfigFile, false, new Properties());
    }


    public static ServiceConfig toServiceConfig(String pathToConfigFile, boolean autoCompleteConfiguration, Properties commandLineProperties) {
        try {
            JsonNode configNode = mapper.readTree(Files.readString(Path.of(pathToConfigFile)));
            if (commandLineProperties != null && !commandLineProperties.isEmpty()) {
                applyCommandlineProperties(commandLineProperties, configNode);
            }
            ServiceConfig serviceConfig = mapper.readValue(mapper.writeValueAsString(configNode), ServiceConfig.class);

            if (autoCompleteConfiguration) {
                ServiceConfig defaultConfig = getDefaultServiceConfig();
                if (serviceConfig.getCore() == null) {
                    serviceConfig.setCore(defaultConfig.getCore());
                }
                if (serviceConfig.getEndpoints() == null || serviceConfig.getEndpoints().size() == 0) {
                    serviceConfig.setEndpoints(defaultConfig.getEndpoints());
                }
                if (serviceConfig.getPersistence() == null) {
                    serviceConfig.setPersistence(defaultConfig.getPersistence());
                }
                if (serviceConfig.getMessageBus() == null) {
                    serviceConfig.setMessageBus(defaultConfig.getMessageBus());
                }
            }
            return serviceConfig;
        }
        catch (NoSuchFileException ex) {
            Application.print("Configuration Error - Could not find configuration file: " + ex.getMessage());
            System.exit(1);
        }
        catch (IOException ex) {
            Application.print("Configuration Error: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
        return null;
    }


    private static void applyCommandlineProperties(Properties properties, JsonNode configNode) {
        for (Map.Entry<Object, Object> prop: properties.entrySet()) {
            List<String> pathList = List.of(prop.getKey().toString().split("\\."));
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
            if (MissingNode.class.isAssignableFrom(jsonNode.getClass())) {
                Application.print("Configuration Error: Could not find attribute with path '" + prop.getKey() + "' in config file");
                System.exit(1);
            }
            ((ObjectNode) jsonNode).put(pathList.get(pathList.size() - 1), prop.getValue().toString());
        }
    }


    private static JsonNode readDefaultConfigFile() throws IOException {
        JsonNode configNode = mapper.readTree(ConfigFactory.class.getClassLoader().getResource(DEFAULT_CONFIG_JSON));
        return configNode;
    }


    private static boolean isNumeric(String s) {
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
