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


public class AASEnvironmentFactory {

    private static Map<String, Deserializer> deserializer;

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


    public static AssetAdministrationShellEnvironment getAASEnvironment(File env) throws StarterConfigurationException {
        return getAASEnvironment(getFileContent(env.getPath()));
    }


    public static AssetAdministrationShellEnvironment getEmptyAASEnvironment() {
        return new DefaultAssetAdministrationShellEnvironment.Builder().build();
    }


    public static AssetAdministrationShellEnvironment getAASEnvironment(String envFilePath) throws StarterConfigurationException {
        initDeserializer();
        String env = getFileContent(envFilePath);

        Deserializer approxDeserializer = deserializer.getOrDefault(
                deserializer.keySet().stream()
                        .filter(x -> x.equalsIgnoreCase(envFilePath.split("\\.")[1]))
                        .findFirst().get(),
                null);
        if (approxDeserializer != null) {
            try {
                AssetAdministrationShellEnvironment environment = approxDeserializer.read(env);

                //OPC UA nodeset file is also a xml file and deserializer doesn´t throw a DeserializationException
                //but returns an empty AASEnvironment
                if (!Objects.equals(environment, new DefaultAssetAdministrationShellEnvironment())) {
                    return approxDeserializer.read(env);
                }
            }
            catch (DeserializationException ignored) {}
        }

        String formats = "";
        for (Map.Entry<String, Deserializer> deserializer: deserializer.entrySet()) {
            try {
                formats += "\t" + deserializer.getKey() + "\n";
                return deserializer.getValue().read(env);
            }
            catch (DeserializationException ex) {}
        }
        throw new StarterConfigurationException("Could not deserialize content to an Asset Administration Shell Environment. Used File: " + "\nSupported Formats:\n" + formats);
    }


    private static String getFileContent(String filePath) throws StarterConfigurationException {
        List<String> supportedAASEnvFileSuffixes = List.of("json", "aml", "xml", "rdf");
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
        throw new StarterConfigurationException("Could not find Asset Administration Shell Environment file '" + filePath + "'");
    }

}
