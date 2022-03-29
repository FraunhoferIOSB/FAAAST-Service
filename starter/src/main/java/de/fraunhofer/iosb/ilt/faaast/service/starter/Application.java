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

import com.github.jsonldjava.shaded.com.google.common.base.Objects;
import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.config.Config;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.EndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.starter.util.AASEnvironmentHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.validator.ShaclValidator;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


/**
 * Class for configuring and starting a FA³ST Service
 */
@Command(name = "FAAAST_Starter", mixinStandardHelpOptions = true, version = "0.1", description = "Starts a FAAAST Service")
public class Application implements Runnable {

    public static final String DEFAULT_AASENV_FILE_NAME = "aasenvironment.*";
    public static final File DEFAULT_AASENV_FILE = new File(DEFAULT_AASENV_FILE_NAME);
    public static final String DEFAULT_AASENV_FILE_NAME_PATTERN = "aasenvironment\\..*";
    public static final String DEFAULT_CONFIG_PATH = "config.json";
    private static final String ENV_AAS_FILE_KEY = "aasEnvFilePath";
    private static final String ENV_CONFIG_FILE_KEY = "configFilePath";
    private static final String ENV_CONFIG_KEY = "config";
    private static final String ENV_FAAAST_KEY = "faaast";

    private static final String ENV_CONFIG_PREFIX = prefix(ENV_FAAAST_KEY, ENV_CONFIG_KEY);
    private static final String ENV_PATH_SEPERATOR = ".";
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    protected static final String ENV_AAS_FILE_PATH = path(ENV_FAAAST_KEY, ENV_AAS_FILE_KEY);
    protected static final String ENV_CONFIG_FILE_PATH = path(ENV_FAAAST_KEY, ENV_CONFIG_FILE_KEY);

    public static void main(String[] args) throws Exception {
        LOGGER.info("Starting FA³ST Service...");
        new CommandLine(new Application()).execute(args);
    }


    private static String path(String... args) {
        return Stream.of(args).collect(Collectors.joining(ENV_PATH_SEPERATOR));
    }


    private static String prefix(String... args) {
        return path(args) + ENV_PATH_SEPERATOR;
    }


    @SuppressWarnings("empty-statement")
    private static void waitForEnter() {
        try {
            System.out.println("\nPress ENTER to exit.\n");
            ExecutorService es = Executors.newFixedThreadPool(1);
            es.submit(() -> {
                Scanner scanner = new Scanner(System.in);
                while (!scanner.hasNextLine());
            }).get();
            es.shutdown();
        }
        catch (InterruptedException | ExecutionException e) {
            LOGGER.warn("error while waiting for user input", e);
        }
    }

    @Option(names = {
            "-e",
            "--environmentFile"
    }, description = "Asset Administration Shell Environment FilePath. Default Value = ${DEFAULT-VALUE}", defaultValue = DEFAULT_AASENV_FILE_NAME)
    public File aasEnvironmentFile;

    @Option(names = {
            "--no-autoCompleteConfig"
    }, negatable = true, description = "Autocompletes the configuration with default values for required configuration sections. True by default")
    public boolean autoCompleteConfiguration = true;
    @Option(names = {
            "-c",
            "--configFile"
    }, description = "The config file path. Default Value = ${DEFAULT-VALUE}", defaultValue = DEFAULT_CONFIG_PATH)
    public String configFilePath;
    @Option(names = "--endpoints", arity = "0..*")
    public List<String> endpoints;

    @Option(names = {
            "-D"
    }, mapFallbackValue = "", paramLabel = "KEY=VALUE")
    public Map<String, Object> properties = new HashMap<>();
    @Option(names = {
            "--emptyEnvironment"
    }, description = "Starts the FA³ST service with an empty Asset Administration Shell Environment. False by default")
    public boolean useEmptyAASEnvironment = false;

    @Option(names = {
            "--no-modelValidation"
    }, negatable = true, description = "Validates the AAS Environment. True by default")
    public boolean validateAASEnv = true;
    private final Map<String, Class<? extends EndpointConfig>> availableEndpoints = new HashMap<>() {
        {
            put("http", HttpEndpointConfig.class);
            /**
             * only usable if OPCUA Endpoint dependency in pom can be resolved
             * put("opcua", OpcUaEndpointConfig.class);
             */
        }
    };
    private Service service;

    @Override
    public void run() {
        try {
            ConfigFactory configFactory = new ConfigFactory();
            readEnvParameter();
            List<Config> customConfigComponents = getCustomConfigComponents();
            ServiceConfig config = configFactory.toServiceConfig(configFilePath, autoCompleteConfiguration, properties, customConfigComponents);
            AssetAdministrationShellEnvironment environment = null;
            if (useEmptyAASEnvironment) {
                LOGGER.info("Using empty Asset Administration Shell Environment");
                environment = AASEnvironmentHelper.newEmpty();
            }
            else {
                if (Objects.equal(aasEnvironmentFile, DEFAULT_AASENV_FILE)) {
                    @java.lang.SuppressWarnings("java:S2095")
                    List<Path> aasEnvFiles = Files.find(Paths.get(""), 1,
                            (file, attributes) -> file.toFile()
                                    .getName()
                                    .matches(DEFAULT_AASENV_FILE_NAME_PATTERN))
                            .collect(Collectors.toList());
                    if (aasEnvFiles.isEmpty()) {
                        LOGGER.info("no AAS environment file found (pattern: {})", DEFAULT_AASENV_FILE_NAME_PATTERN);
                        return;
                    }
                    if (aasEnvFiles.size() > 1) {
                        LOGGER.info("found more than one AAS environment file - use '-e' resp. '--environmentFile' to specify which one to use");
                        return;
                    }
                    aasEnvironmentFile = aasEnvFiles.get(0).toFile();
                }
                environment = AASEnvironmentHelper.fromFile(aasEnvironmentFile);
                LOGGER.info("Successfully parsed Asset Administration Shell Environment");
            }
            if (validateAASEnv) {
                validate(environment);
            }
            service = new Service(environment, config);
            service.start();
            LOGGER.info("FAAAST Service is running!");
            waitForEnter();
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        finally {
            if (service != null) {
                service.stop();
            }
            LOGGER.error("Abort starting FAAAST Service");
        }
    }


    private List<Config> getCustomConfigComponents()
            throws InvalidConfigurationException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<Config> customConfigs = new ArrayList<>();
        if (endpoints != null && !endpoints.isEmpty()) {
            for (String endpoint: endpoints) {
                if (availableEndpoints.containsKey(endpoint.toLowerCase())) {
                    customConfigs.add(availableEndpoints.get(endpoint.toLowerCase()).getDeclaredConstructor().newInstance());
                }
                else {
                    throw new InvalidConfigurationException("Endpoint '" + endpoint + "' is not supported. Supported endpoints: " + availableEndpoints.keySet());
                }
            }
        }
        return customConfigs;
    }


    private void readEnvParameter() {
        boolean envUsed = false;
        LOGGER.info("searching environment for configuration...");
        if (System.getenv(ENV_CONFIG_FILE_PATH) != null && !System.getenv(ENV_CONFIG_FILE_PATH).isBlank()) {
            configFilePath = System.getenv(ENV_CONFIG_FILE_PATH);
            LOGGER.info(" -- {}={}", ENV_CONFIG_FILE_KEY, configFilePath);
            envUsed = true;
        }
        if (System.getenv(ENV_AAS_FILE_PATH) != null && !System.getenv(ENV_AAS_FILE_PATH).isBlank()) {
            aasEnvironmentFile = new File(System.getenv(ENV_AAS_FILE_PATH));
            LOGGER.info(" -- {}={}", ENV_AAS_FILE_KEY, aasEnvironmentFile);
            envUsed = true;
        }
        properties = System.getenv().entrySet().stream()
                .filter(x -> x.getKey().startsWith(ENV_CONFIG_PREFIX))
                .collect(Collectors.toMap(
                        x -> x.getKey().substring(ENV_CONFIG_PREFIX.length() - 1),
                        x -> x.getValue()));
        properties.entrySet().forEach(x -> LOGGER.info("  -- {]={}", x.getKey(), x.getValue()));
        if (!envUsed && properties.isEmpty()) {
            LOGGER.info("no configuration found in environment");
        }
    }


    private void validate(AssetAdministrationShellEnvironment aasEnv) throws IOException {
        LOGGER.debug("Validate Asset Administration Shell Environment model");
        ShaclValidator shaclValidator = ShaclValidator.getInstance();
        ValidationReport report = shaclValidator.validateGetReport(aasEnv);
        if (report.conforms()) {
            LOGGER.info("Valid Asset Administration Shell Environment model");
            return;
        }
        ByteArrayOutputStream validationResultStream = new ByteArrayOutputStream();
        ShLib.printReport(validationResultStream, report);
        throw new IllegalArgumentException(String.format("Invalid Asset Administration Shell Environment model. Found the following %d failures: %s%s",
                report.getEntries().size(),
                System.lineSeparator(),
                validationResultStream.toString()));
    }
}
