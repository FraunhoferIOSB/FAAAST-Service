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
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


@Command(name = "FA³ST_Starter", mixinStandardHelpOptions = true, version = "0.1", description = "Starts a FA³ST Service")
public class Application implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    public static final String DEFAULT_CONFIG_PATH = "config.json";
    public static final String DEFAULT_AASENV_PATH = "aasenvironment.*";

    @Option(names = {
            "-c",
            "--configFile"
    }, description = "The config file path. Default Value = ${DEFAULT-VALUE}", defaultValue = DEFAULT_CONFIG_PATH)
    private String configFilePath;

    @Option(names = {
            "-e",
            "--aasEnvironment"
    }, description = "Asset Administration Shell Environment FilePath. Default Value = ${DEFAULT-VALUE}", defaultValue = DEFAULT_AASENV_PATH)
    private String aasEnvironmentFilePath;

    @Option(names = {
            "--no-autoCompleteConfig"
    }, negatable = true, description = "Autocompletes the configuration with default values for required configuration sections. True by default")
    public static boolean autoCompleteConfiguration = true;

    @Option(names = {
            "--emptyEnvironment"
    }, description = "Starts the FA³ST service with an empty Asset Administration Shell Environment. False by default")
    private boolean useEmptyAASEnvironment = false;

    @Option(names = {
            "-D",
            "--property"
    }, paramLabel = "KEY=VALUE")
    private Map<String, Object> properties;

    public static void main(String[] args) throws Exception {
        LOGGER.info("Start configuration of FAAAST Service");
        new CommandLine(new Application()).execute(args);
    }


    @Override
    public void run() {
        try {
            ServiceConfig config = ConfigFactory.toServiceConfig(configFilePath, autoCompleteConfiguration, properties);
            AssetAdministrationShellEnvironment environment = null;
            if (useEmptyAASEnvironment) {
                LOGGER.info("Using empty Asset Administration Shell Environment");
                environment = AASEnvironmentFactory.getEmptyAASEnvironment();
            }
            else {
                environment = AASEnvironmentFactory.getAASEnvironment(aasEnvironmentFilePath);
                LOGGER.info("Successfully parsed Asset Administration Shell Environment");
            }
            Service service = new Service(config);
            service.setAASEnvironment(environment);
            service.start();
            LOGGER.info("FAAAST Service is running!");
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            LOGGER.error("Abort starting FAAAST Service");
        }

    }
}
