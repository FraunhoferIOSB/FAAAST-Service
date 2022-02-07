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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AASEnvironmentFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AASEnvironmentFactory.class);
    private static Map<String, Deserializer> deserializer;

    private static List<String> supportedAASEnvFileSuffixes = List.of("json", "aml", "xml", "rdf");

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


    public static AssetAdministrationShellEnvironment getAASEnvironment(File env) throws Exception {
        return getAASEnvironment(getFileContent(env.getPath()));
    }


    public static AssetAdministrationShellEnvironment getEmptyAASEnvironment() {
        return new DefaultAssetAdministrationShellEnvironment.Builder().build();
    }


    public static AssetAdministrationShellEnvironment getAASEnvironment(String envFilePath) throws Exception {
        initDeserializer();
        String env = getFileContent(envFilePath);
        String fileEnding = envFilePath.split("\\.")[1];

        LOGGER.info("Try to resolve Asset Administration Shell Environment from file '" + envFilePath + "'");

        Deserializer approxDeserializer = null;
        if (!envFilePath.equalsIgnoreCase(Application.DEFAULT_AASENV_PATH)) {
            LOGGER.debug("Looking for Deserializer for file ending '" + fileEnding + "'");
            approxDeserializer = deserializer.getOrDefault(
                    deserializer.keySet().stream()
                            .filter(x -> x.equalsIgnoreCase(fileEnding))
                            .findFirst().orElseGet(null),
                    null);
        }

        if (approxDeserializer != null) {
            try {
                LOGGER.debug("Try resolving with '" + approxDeserializer.getClass().getSimpleName() + "'");
                AssetAdministrationShellEnvironment environment = approxDeserializer.read(env);

                //OPC UA nodeset file is also a xml file and deserializer doesn´t throw a DeserializationException
                //but returns an empty AASEnvironment
                if (!Objects.equals(environment, new DefaultAssetAdministrationShellEnvironment())) {
                    return approxDeserializer.read(env);
                }
            }
            catch (Exception ignored) {
                LOGGER.debug("Resolving with '" + approxDeserializer.getClass().getSimpleName() + "' was not successfull. Try other Deserializers.");
            }
        }

        String formats = "";
        for (Map.Entry<String, Deserializer> deserializer: deserializer.entrySet()) {
            try {
                LOGGER.debug("Try resolving with '" + deserializer.getValue().getClass().getSimpleName() + "'");
                formats += "\t" + deserializer.getKey() + "\n";
                return deserializer.getValue().read(env);
            }
            catch (DeserializationException ex) {}
        }
        throw new Exception(
                "Could not deserialize content to an Asset Administration Shell Environment. Used File: " + envFilePath + "\nSupported Formats:\n" + formats);
    }


    private static String getFileContent(String filePath) throws Exception {

        if (filePath.equalsIgnoreCase(Application.DEFAULT_AASENV_PATH)) {
            try {
                for (String suf: supportedAASEnvFileSuffixes) {
                    return Files.readString(Path.of(filePath.replace("*", suf)));
                }
            }
            catch (IOException ignored) {}
        }
        else {
            try {
                return Files.readString(Path.of(filePath));
            }
            catch (IOException ignored) {}
        }
        throw new Exception("Could not find Asset Administration Shell Environment file '" + filePath + "'\n" +
                "You can use --emptyEnvironment to start an empty Asset Administration Shell Environment.");
    }

}
