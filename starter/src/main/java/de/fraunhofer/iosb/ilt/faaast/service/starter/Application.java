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


public class Application {

    public static final String DEFAULT_CONFIG_PATH = "/config.json";
    public static final String DEFAULT_AASENV_PATH = "/aasenvironment.*";

    @picocli.CommandLine.Option(names = {
            "-c",
            "--configFile"
    }, description = "The config file path. Default Value = ${DEFAULT-VALUE}", defaultValue = DEFAULT_CONFIG_PATH)
    static String configFilePath;

    @picocli.CommandLine.Option(names = {
            "-e",
            "--aasEnvironment"
    }, description = "Asset Administration Shell Environment FilePath. Default Value = ${DEFAULT-VALUE}", defaultValue = DEFAULT_AASENV_PATH)
    static String aasEnvironmentFilePath;

    @picocli.CommandLine.Option(names = {
            "--no-autoCompleteConfig"
    }, negatable = true, description = "Autocompletes the configuration with default values for required configuration sections. True by default")
    public static boolean autoCompleteConfiguration = true;

    @picocli.CommandLine.Option(names = {
            "--emptyEnvironment"
    }, description = "Starts the FA³ST service with an empty Asset Administration Shell Environment. False by default")
    public static boolean useEmptyAASEnvironment = false;

    @picocli.CommandLine.Option(names = "-D")
    static Map<String, Object> properties;

    public static void main(String[] args) throws Exception {
        properties.entrySet().stream().forEach(x -> print("Apply Config parameter: " + x.getKey() + " = " + x.getValue().toString()));

        ServiceConfig config = null;
        AssetAdministrationShellEnvironment environment = null;

        try {
            config = ConfigFactory.toServiceConfig(configFilePath);
            if (useEmptyAASEnvironment) {
                environment = AASEnvironmentFactory.getEmptyAASEnvironment();
            }
            else {
                environment = AASEnvironmentFactory.getAASEnvironment(aasEnvironmentFilePath);
            }

        }
        catch (StarterConfigurationException ex) {
            print(ex.getMessage());
            System.exit(1);
        }
        Service service = new Service(config);
        service.setAASEnvironment(environment);
        service.start();
    }


    protected static void print(String msg) {
        System.out.println("[FA³ST] " + msg);
    }

}
