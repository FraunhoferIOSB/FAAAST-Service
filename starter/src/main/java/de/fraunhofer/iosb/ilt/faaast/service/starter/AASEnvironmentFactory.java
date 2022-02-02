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

import io.adminshell.aas.v3.dataformat.DeserializationException;
import io.adminshell.aas.v3.dataformat.Deserializer;
import io.adminshell.aas.v3.dataformat.aml.AmlDeserializer;
import io.adminshell.aas.v3.dataformat.i4aas.I4AASDeserializer;
import io.adminshell.aas.v3.dataformat.json.JsonDeserializer;
import io.adminshell.aas.v3.dataformat.xml.XmlDeserializer;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;


public class AASEnvironmentFactory {

    private static Map<String, Deserializer> deserializer;

    public static AssetAdministrationShellEnvironment getAASEnvironment(CommandLine cmd, File env) throws IOException {
        return getAASEnvironment(cmd, Files.readString(env.toPath()));
    }


    public static AssetAdministrationShellEnvironment getAASEnvironment(CommandLine cmd) throws IOException {
        try {
            if (cmd.hasOption(CommandLineFactory.CMD_AASENVIRONMENT_FILEPATH_PARAMETER)) {
                String envFilePath = cmd.getOptionValue(CommandLineFactory.CMD_AASENVIRONMENT_FILEPATH_PARAMETER);
                File aasFile = new File(envFilePath);
                String env = Files.readString(aasFile.toPath());
                return getAASEnvironment(cmd, env);
            }
            return getAASEnvironment(cmd, (String) null);

        }
        catch (FileNotFoundException ex) {
            Application.print("File " + cmd.getOptionValue(CommandLineFactory.CMD_AASENVIRONMENT_FILEPATH_PARAMETER) + " not found!");
            ex.printStackTrace();
            System.exit(1);
        }
        return null;
    }


    public static AssetAdministrationShellEnvironment getAASEnvironment(CommandLine cmd, String env) throws IOException {
        initDeserializer();
        AssetAdministrationShellEnvironment environment = null;
        if (cmd.hasOption(CommandLineFactory.CMD_AASENVIRONMENT_FILEPATH_PARAMETER) && env != null) {
            String formats = "";
            for (Map.Entry<String, Deserializer> deserializer: deserializer.entrySet()) {
                try {
                    formats += "\t" + deserializer.getKey() + "\n";
                    environment = deserializer.getValue().read(env);
                    return environment;
                }

                catch (DeserializationException ex) {}
            }
            Application.print("Could not deserialize content to an AASEnvironment. Used File: " + cmd.getOptionValue(CommandLineFactory.CMD_AASENVIRONMENT_FILEPATH_PARAMETER)
                    + "\nSupported Formats:\n" + formats);
            System.exit(1);
        }
        else {
            Application.print("No AASEnvironment was found. Creating empty Environment");
            return new DefaultAssetAdministrationShellEnvironment.Builder().build();
        }
        return null;
    }


    private static void initDeserializer() {
        //TODO: AASX Deserializer seems to be a little bit different since it needs an input in constructor
        deserializer = new HashMap<>();
        deserializer.put("JSON", new JsonDeserializer());
        deserializer.put("AML", new AmlDeserializer());
        deserializer.put("XML", new XmlDeserializer());
        deserializer.put("I4AAS/OPC UA Nodeset", new I4AASDeserializer());
        deserializer.put("RDF", new io.adminshell.aas.v3.dataformat.rdf.Serializer());
        deserializer.put("JSON-LD", new io.adminshell.aas.v3.dataformat.jsonld.Serializer());
    }

}
