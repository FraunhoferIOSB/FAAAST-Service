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

import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class CommandLineFactory {

    public static final String CMD_CONFIG_PARAMETER = "c";
    public static final String CMD_AASENVIRONMENT_FILEPATH_PARAMETER = "e";
    public static final String CMD_ENVIRONMENT_PARAMETER = "D";
    public static final String CMD_COMPLETE_CONFIGURATION_PARAMETER = "a";

    public static CommandLine setCommandLineOptions(String[] args) {

        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        Options options = initOptions();
        try {
            return parser.parse(options, args);
        }
        catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("FA³ST Service Starter Info", options);
            System.exit(1);
            return null;
        }

    }


    private static Options initOptions() {
        List<Option> commandLineParameters = List.of(
                new Option(CMD_CONFIG_PARAMETER, "config", true, "Configuration Filepath"),
                new Option(CMD_AASENVIRONMENT_FILEPATH_PARAMETER, "aasEnvironment", true, "AAS Environment FilePath"),
                Option.builder(CMD_ENVIRONMENT_PARAMETER)
                        .hasArgs()
                        .valueSeparator('=')
                        .build(),
                new Option(CMD_COMPLETE_CONFIGURATION_PARAMETER, "autoCompleteConfiguration", false,
                        "Autocompletes the configuration with default values for required configuration sections"));

        Options options = new Options();
        commandLineParameters.forEach(options::addOption);
        return options;

    }

}
