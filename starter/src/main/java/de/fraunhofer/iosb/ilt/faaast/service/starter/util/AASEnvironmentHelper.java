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
package de.fraunhofer.iosb.ilt.faaast.service.starter.util;

import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import io.adminshell.aas.v3.dataformat.DeserializationException;
import io.adminshell.aas.v3.dataformat.Deserializer;
import io.adminshell.aas.v3.dataformat.aasx.AASXDeserializer;
import io.adminshell.aas.v3.dataformat.aml.AmlDeserializer;
import io.adminshell.aas.v3.dataformat.i4aas.I4AASDeserializer;
import io.adminshell.aas.v3.dataformat.json.JsonDeserializer;
import io.adminshell.aas.v3.dataformat.xml.XmlDeserializer;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    private static Map<DataFormat, Deserializer> deserializers;

    static {
        deserializers = Map.of(DataFormat.JSON, new JsonDeserializer(),
                DataFormat.AML, new AmlDeserializer(),
                DataFormat.XML, new XmlDeserializer(),
                DataFormat.UANODESET, new I4AASDeserializer(),
                DataFormat.RDF, new io.adminshell.aas.v3.dataformat.rdf.Serializer(),
                DataFormat.JSONLD, new io.adminshell.aas.v3.dataformat.jsonld.Serializer());
    }

    private AASEnvironmentHelper() {

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
    public static AssetAdministrationShellEnvironment fromFile(File file, DataFormat dataFormat) throws DeserializationException, FileNotFoundException {
        if (file == null) {
            throw new IllegalArgumentException("file must be non-null");
        }
        if (dataFormat == null) {
            throw new IllegalArgumentException("dataFormat must be non-null");
        }
        String fileExtension = FilenameUtils.getExtension(file.getName());
        if (!dataFormat.getFileExtensions().contains(fileExtension)) {
            LOGGER.warn("attempting to read AAS environment file with unsupported file extension (data format: {}, supported file extensions: {}, actual file extension: {}",
                    dataFormat,
                    dataFormat.getFileExtensions().stream().collect(Collectors.joining(",")),
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
            throw new DeserializationException(String.format("unsupported data format: ", dataFormat));
        }
        return deserializers.get(dataFormat).read(file);
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
     *             * @throws DeserializationException if deserialization fails
     */
    public static AssetAdministrationShellEnvironment fromFile(File file) throws DeserializationException {
        if (file == null) {
            throw new IllegalArgumentException("file must be non-null");
        }
        String fileExtension = FilenameUtils.getExtension(file.getName());
        List<DataFormat> potentialDataFormats = DataFormat.forFileExtension(fileExtension);
        if (potentialDataFormats.isEmpty()) {
            throw new DeserializationException(String.format("error reading AAS file - no supported data format found for extension '%s'", fileExtension));
        }
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
                                .map(x -> x.name())
                                .collect(Collectors.joining(","))));
    }


    /**
     * Returns a new empty instance of
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     *
     * @return an empty Asset Administration Shell Environment
     */
    public static AssetAdministrationShellEnvironment newEmpty() {
        return new DefaultAssetAdministrationShellEnvironment.Builder().build();
    }
}
