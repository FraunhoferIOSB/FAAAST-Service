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

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.config.Config;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.EndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.validator.ShaclValidator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    public static final String DEFAULT_CONFIG_PATH = "config.json";
    public static final String DEFAULT_AASENV_PATH = "aasenvironment.*";

    private static final String FAAAST_ENV_PREFIX = "faaast.";
    private static final String CONFIG_PARAMETER_ENV_PREFIX = FAAAST_ENV_PREFIX + "configParameter.";

    public static final String CONFIG_FILE_PATH_ENVIRONMENT_VARIABLE = FAAAST_ENV_PREFIX + "configFilePath";
    public static final String AASENV_FILE_PATH_ENVIRONMENT_VARIABLE = FAAAST_ENV_PREFIX + "aasEnvFilePath";

    private static Service service;

    private final Map<String, Class<? extends EndpointConfig>> availableEndpoints = new HashMap<>() {
        {
            put("http", HttpEndpointConfig.class);

            //only usable if OPCUA Endpoint dependency in pom can be resolved
            //put("ocua", OpcUaEndpointConfig.class);
        }
    };

    @Option(names = {
            "-c",
            "--configFile"
    }, description = "The config file path. Default Value = ${DEFAULT-VALUE}", defaultValue = DEFAULT_CONFIG_PATH)
    public String configFilePath;

    @Option(names = {
            "-e",
            "--environmentFile"
    }, description = "Asset Administration Shell Environment FilePath. Default Value = ${DEFAULT-VALUE}", defaultValue = DEFAULT_AASENV_PATH)
    public String aasEnvironmentFilePath;

    @Option(names = {
            "--no-autoCompleteConfig"
    }, negatable = true, description = "Autocompletes the configuration with default values for required configuration sections. True by default")
    public boolean autoCompleteConfiguration = true;

    @Option(names = {
            "--no-modelValidation"
    }, negatable = true, description = "Validates the AAS Environment. True by default")
    public boolean validateAASEnv = true;

    @Option(names = {
            "--emptyEnvironment"
    }, description = "Starts the FA³ST service with an empty Asset Administration Shell Environment. False by default")
    public boolean useEmptyAASEnvironment = false;

    @Option(names = {
            "-D"
    }, mapFallbackValue = "", paramLabel = "KEY=VALUE")
    public Map<String, Object> properties = new HashMap<>();

    @Option(names = "--endpoints", arity = "0..*")
    public List<String> endpoints;

    public static void main(String[] args) throws Exception {
        LOGGER.info("Start configuration of FAAAST Service");
        new CommandLine(new Application()).execute(args);

        CountDownLatch doneSignal = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            /** This handler will be called on Control-C pressed */
            @Override
            public void run() {
                // Decrement counter.
                // It will became 0 and main thread who waits for this barrier could continue run (and fulfill all proper shutdown steps)
                doneSignal.countDown();
            }
        });
        // Here we enter wait state until control-c will be pressed
        try {
            doneSignal.await();
            if (service != null) {
                LOGGER.info("Shutting down FA³ST Service");
                service.stop();
            }
        }
        catch (InterruptedException e) {
            LOGGER.warn("Interrupted!", e);
            Thread.currentThread().interrupt();
        }
    }


    @Override
    public void run() {
        try {
            ConfigFactory configFactory = new ConfigFactory();
            AASEnvironmentFactory environmentFactory = new AASEnvironmentFactory();

            readConfigurationParametersOverEnvironmentVariables();
            readFilePathsOverEnvironmentVariables();

            List<Config> customConfigComponents = getCustomConfigComponents();

            ServiceConfig config = configFactory.toServiceConfig(configFilePath, autoCompleteConfiguration, properties, customConfigComponents);
            AssetAdministrationShellEnvironment environment = null;
            if (useEmptyAASEnvironment) {
                LOGGER.info("Using empty Asset Administration Shell Environment");
                environment = environmentFactory.getEmptyAASEnvironment();
            }
            else {
                environment = environmentFactory.getAASEnvironment(aasEnvironmentFilePath);
                LOGGER.info("Successfully parsed Asset Administration Shell Environment");
            }
            if (validateAASEnv) {
                validate(environment);
            }
            service = new Service(config);
            service.setAASEnvironment(environment);
            service.start();
            LOGGER.info("FAAAST Service is running!");
        }
        catch (Exception ex) {
            if (service != null) {
                service.stop();
            }
            LOGGER.error(ex.getMessage());
            LOGGER.error("Abort starting FAAAST Service");
        }

    }


    private List<Config> getCustomConfigComponents() throws Exception {
        List<Config> customConfigs = new ArrayList<>();
        if (endpoints != null && !endpoints.isEmpty()) {
            for (String endpoint: endpoints) {
                if (availableEndpoints.containsKey(endpoint.toLowerCase())) {
                    customConfigs.add(availableEndpoints.get(endpoint.toLowerCase()).getDeclaredConstructor().newInstance());
                }
                else {
                    throw new Exception("Endpoint '" + endpoint + "' is not supported. Supported endpoints: " + availableEndpoints.keySet());
                }
            }
        }
        return customConfigs;
    }


    private void readConfigurationParametersOverEnvironmentVariables() {
        Set<Map.Entry<String, String>> env = System.getenv().entrySet().stream().filter(x -> x.getKey().contains(CONFIG_PARAMETER_ENV_PREFIX)).collect(Collectors.toSet());
        if (!env.isEmpty()) {
            Map<String, String> cleanedEnvs = new HashMap<>();
            env.stream().forEach(x -> cleanedEnvs.put(x.getKey().replace(CONFIG_PARAMETER_ENV_PREFIX, ""), x.getValue()));
            LOGGER.info("Got following configuration parameters through environment variables:");
            cleanedEnvs.forEach((key, value) -> {
                LOGGER.info("  -- {}={}", key, value);
                properties.put(key, value);
            });
        }
    }


    private void readFilePathsOverEnvironmentVariables() {
        if (System.getenv(CONFIG_FILE_PATH_ENVIRONMENT_VARIABLE) != null && !System.getenv(CONFIG_FILE_PATH_ENVIRONMENT_VARIABLE).isBlank()) {
            configFilePath = System.getenv(CONFIG_FILE_PATH_ENVIRONMENT_VARIABLE);
            LOGGER.info("Read environment variable '" + CONFIG_FILE_PATH_ENVIRONMENT_VARIABLE + "' and override config file path");
        }
        if (System.getenv(AASENV_FILE_PATH_ENVIRONMENT_VARIABLE) != null && !System.getenv(AASENV_FILE_PATH_ENVIRONMENT_VARIABLE).isBlank()) {
            aasEnvironmentFilePath = System.getenv(AASENV_FILE_PATH_ENVIRONMENT_VARIABLE);
            LOGGER.info("Read environment variable '{}={}'", AASENV_FILE_PATH_ENVIRONMENT_VARIABLE, aasEnvironmentFilePath);
        }
    }


    private boolean validate(AssetAdministrationShellEnvironment aasEnv) throws Exception {
        LOGGER.debug("Validate Asset Administration Shell Environment model");
        ShaclValidator shaclValidator = ShaclValidator.getInstance();
        ValidationReport report = shaclValidator.validateGetReport(aasEnv);
        if (report.conforms()) {
            LOGGER.info("Valid Asset Administration Shell Environment model");
            return true;
        }
        LOGGER.error("Invalid Asset Administration Shell Environment model. Found '{}' failures", report.getEntries().size());
        ShLib.printReport(report);
        throw new Exception("Invalid Asset Administration Shell Environment model.");
    }
}
