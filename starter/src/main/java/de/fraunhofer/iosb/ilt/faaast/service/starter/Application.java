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
import org.apache.commons.cli.CommandLine;


public class Application {

    public static final String DEFAULT_CONFIG_PATH = "/config.json";

    public static void main(String[] args) throws Exception {
        CommandLine cmd = CommandLineFactory.setCommandLineOptions(args);
        cmd.getOptionProperties(CommandLineFactory.CMD_ENVIRONMENT_PARAMETER).forEach((x, y) -> print("Apply Config parameter: " + x.toString() + " = " + y.toString()));

        ServiceConfig config = ConfigFactory.getServiceConfig(cmd);
        AssetAdministrationShellEnvironment environment = AASEnvironmentFactory.getAASEnvironment(cmd);
        Service service = new Service(config);
        service.setAASEnvironment(environment);
        service.start();
    }


    protected static void print(String msg) {
        System.out.println("[FA³ST] " + msg);
    }

}
