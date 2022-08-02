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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import io.adminshell.aas.v3.dataformat.DeserializationException;
import io.adminshell.aas.v3.dataformat.Deserializer;
import io.adminshell.aas.v3.dataformat.SerializationException;
import io.adminshell.aas.v3.dataformat.Serializer;
import io.adminshell.aas.v3.dataformat.aasx.AASXDeserializer;
import io.adminshell.aas.v3.dataformat.aasx.AASXSerializer;
import io.adminshell.aas.v3.dataformat.aml.AmlDeserializer;
import io.adminshell.aas.v3.dataformat.aml.AmlSerializer;
import io.adminshell.aas.v3.dataformat.i4aas.I4AASDeserializer;
import io.adminshell.aas.v3.dataformat.i4aas.I4AASSerializer;
import io.adminshell.aas.v3.dataformat.json.JsonDeserializer;
import io.adminshell.aas.v3.dataformat.json.JsonSerializer;
import io.adminshell.aas.v3.dataformat.xml.XmlDeserializer;
import io.adminshell.aas.v3.dataformat.xml.XmlSerializer;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AASEnvironmentHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AASEnvironmentHelper.class);
    private static final String MSG_FILE_MUST_BE_NON_NULL = "file must be non-null";
    private static Map<DataFormat, Deserializer> deserializers;
    private static Map<DataFormat, Serializer> serializers;
    public static final AssetAdministrationShellEnvironment EMPTY_AAS = new DefaultAssetAdministrationShellEnvironment.Builder().build();

    static {
        deserializers = Map.of(DataFormat.JSON, new JsonDeserializer(),
                DataFormat.AML, new AmlDeserializer(),
                DataFormat.XML, new XmlDeserializer(),
                DataFormat.UANODESET, new I4AASDeserializer(),
                DataFormat.RDF, new io.adminshell.aas.v3.dataformat.rdf.Serializer(),
                DataFormat.JSONLD, new io.adminshell.aas.v3.dataformat.rdf.Serializer());

        serializers = Map.of(DataFormat.JSON, new JsonSerializer(),
                DataFormat.AML, new AmlSerializer(),
                DataFormat.XML, new XmlSerializer(),
                DataFormat.UANODESET, new I4AASSerializer(),
                DataFormat.RDF, new io.adminshell.aas.v3.dataformat.rdf.Serializer(),
                DataFormat.JSONLD, new io.adminshell.aas.v3.dataformat.rdf.Serializer());

    }

    private AASEnvironmentHelper() {

    }


    /**
     * Writes an
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     * to a given file using given data format.
     *
     * @param environment to write to file
     * @param dataFormat of the file
     * @param file the file to write with the environment
     * @throws IOException if writing to file fails
     * @throws SerializationException if serialization fails
     */
    public static void toFile(AssetAdministrationShellEnvironment environment, DataFormat dataFormat, File file) throws IOException, SerializationException {
        Ensure.requireNonNull(file, MSG_FILE_MUST_BE_NON_NULL);
        Ensure.requireNonNull(dataFormat, "dataFormat must be non-null");
        Ensure.requireNonNull(environment, "AAS environment must be non-null");

        String fileExtension = FilenameUtils.getExtension(file.getName());
        if (!dataFormat.getFileExtensions().contains(fileExtension)) {
            String dataFormatFileExtensions = String.join(",", dataFormat.getFileExtensions());
            LOGGER.warn("attempting to write AAS environment file with unsupported file extension (data format: {}, supported file extensions: {}, actual file extension: {}",
                    dataFormat,
                    dataFormatFileExtensions,
                    fileExtension);
        }
        if (dataFormat == DataFormat.AASX) {
            FileOutputStream out = new FileOutputStream(file);
            new AASXSerializer().write(environment, null, out);
        }
        if (!serializers.containsKey(dataFormat)) {
            throw new SerializationException(String.format("unsupported data format: '%s'", dataFormat));
        }
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(serializers.get(dataFormat).write(environment));
        }
    }


    /**
     * Reads an
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     * from given file using given data format.
     *
     * @param file the file to read
     * @param dataFormat the dataformat to use
     * @return the deserialized AAS enviroment
     * @throws DeserializationException if deserialization fails
     * @throws FileNotFoundException if file is not found
     */
    public static AssetAdministrationShellEnvironment fromFile(File file, DataFormat dataFormat) throws DeserializationException, IOException {
        Ensure.requireNonNull(file, MSG_FILE_MUST_BE_NON_NULL);
        Ensure.requireNonNull(dataFormat, "dataFormat must be non-null");
        String fileExtension = FilenameUtils.getExtension(file.getName());
        if (!dataFormat.getFileExtensions().contains(fileExtension)) {
            String dataFormatFileExtensions = String.join(",", dataFormat.getFileExtensions());
            LOGGER.warn("attempting to read AAS environment file with unsupported file extension (data format: {}, supported file extensions: {}, actual file extension: {}",
                    dataFormat,
                    dataFormatFileExtensions,
                    fileExtension);
        }
        if (dataFormat == DataFormat.AASX) {
            try {
                return new AASXDeserializer(new FileInputStream(file)).read();
            }
            catch (InvalidFormatException | IOException e) {
                throw new DeserializationException("error reading AASX file", e);
            }
        }
        if (!deserializers.containsKey(dataFormat)) {
            throw new DeserializationException(String.format("unsupported data format: '%s'", dataFormat));
        }
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return deserializers.get(dataFormat).read(fileInputStream);
        }
    }


    /**
     * Reads an
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     * from given file while automatically determining used data format based on
     * file extension.
     *
     * @param file the file to read
     * @return the deserialized AAS enviroment
     * @throws DeserializationException if file extensions is not supported
     * @throws DeserializationException if deserialization fails
     * @throws java.io.IOException if file access fails
     */
    public static AssetAdministrationShellEnvironment fromFile(File file) throws DeserializationException, IOException {
        List<DataFormat> potentialDataFormats = getDataFormats(file);
        for (DataFormat dataFormat: potentialDataFormats) {
            try {
                return fromFile(file, dataFormat);
            }
            catch (DeserializationException | FileNotFoundException e) {
                // intentionally suppress exception as this probably indicates that we have an ambiguous file extension and this was not the correct deserializer
            }
        }
        throw new DeserializationException(
                String.format("error reading AAS file - could be not parsed using any of the potential data formats identified by file extension (potential data formats: %s)",
                        potentialDataFormats.stream()
                                .map(Enum::name)
                                .collect(Collectors.joining(","))));
    }


    public static DataFormat getDataformat(File file) throws DeserializationException {
        List<DataFormat> potentialDataFormats = getDataFormats(file);
        for (DataFormat dataFormat: potentialDataFormats) {
            try {
                fromFile(file, dataFormat);
                return dataFormat;
            }
            catch (DeserializationException | IOException e) {
                // intentionally suppress exception as this probably indicates that we have an ambiguous file extension and this was not the correct deserializer
            }
        }
        throw new DeserializationException(
                String.format("error reading AAS file - could be not parsed using any of the potential data formats identified by file extension (potential data formats: %s)",
                        potentialDataFormats.stream()
                                .map(Enum::name)
                                .collect(Collectors.joining(","))));
    }


    private static List<DataFormat> getDataFormats(File file) throws DeserializationException {
        Ensure.requireNonNull(file, MSG_FILE_MUST_BE_NON_NULL);
        String fileExtension = FilenameUtils.getExtension(file.getName());
        List<DataFormat> potentialDataFormats = DataFormat.forFileExtension(fileExtension);
        if (potentialDataFormats.isEmpty()) {
            throw new DeserializationException(String.format("error reading AAS file - no supported data format found for extension '%s'", fileExtension));
        }
        return potentialDataFormats;
    }
}
