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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.file.util;

import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.file.PersistenceFileConfig;
import de.fraunhofer.iosb.ilt.faaast.service.util.AASEnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.adminshell.aas.v3.dataformat.DeserializationException;
import io.adminshell.aas.v3.dataformat.SerializationException;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class for loading and saving {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment} to/from a
 * file
 */
public class FileHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileHelper.class);

    public static final DataFormat DEFAULT_DATAFORMAT = DataFormat.JSON;

    public static final String DEFAULT_FILENAME_PREFIX = "environment_createdByFAAAST";
    public static final String DEFAULT_FILENAME = DEFAULT_FILENAME_PREFIX + "." + DEFAULT_DATAFORMAT.toString().toLowerCase();

    private String filename = DEFAULT_FILENAME;

    private String destination;

    private DataFormat dataFormat = DEFAULT_DATAFORMAT;

    private final PersistenceFileConfig config;

    public FileHelper(PersistenceFileConfig config) {
        this.destination = config.getDestination();
        this.config = config;
        settingFileName();
    }


    private void settingFileName() {
        if (config.isOverrideOriginalModelFile()) {
            Ensure.requireNonNull(config.getModelPath());
            File modelFile = new File(config.getModelPath());
            filename = modelFile.getName();
            destination = modelFile.getParent();
            dataFormat = findDataFormat(modelFile.getName());
            Path filePath = getFilePath().toAbsolutePath();
            LOGGER.info("File Persistence overrides the original model file {}", filePath);
        }
        else if (StringUtils.isNotBlank(config.getModelPath()) && config.getDesiredDataformat() == null) {
            dataFormat = findDataFormat(config.getModelPath());
            filename = DEFAULT_FILENAME_PREFIX + "." + dataFormat.toString().toLowerCase();
        }
        else if (config.getDesiredDataformat() != null) {
            dataFormat = config.getDesiredDataformat();
            filename = DEFAULT_FILENAME_PREFIX + "." + dataFormat.toString().toLowerCase();
        }
    }


    private DataFormat findDataFormat(String filename) {
        List<DataFormat> possibleDataFormats = DataFormat.forFileExtension(FilenameUtils.getExtension(filename));
        if (possibleDataFormats.size() > 1) {
            String possibleDataformat = possibleDataFormats.get(0).toString();
            LOGGER.warn("Found multiple possible Serializers. Using Serializer for {}", possibleDataformat);
        }
        return possibleDataFormats.get(0);
    }


    /**
     * Get the current file path of the model file used by the file persistence
     *
     * @return file path of the model file
     */
    public Path getFilePath() {
        return Path.of(destination, filename);
    }


    /**
     * Saves the given {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment} to a file
     *
     * @param environment to save
     */
    public void save(AssetAdministrationShellEnvironment environment) {
        try {
            AASEnvironmentHelper.toFile(environment, dataFormat, new File(String.valueOf(getFilePath())));
        }
        catch (IOException | SerializationException e) {
            LOGGER.error(String.format("Could not save environment to file %s", Path.of(destination, filename)), e);
        }
    }


    /**
     * Loads the {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment} from a file
     *
     * @return the parsed {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     * @throws IOException if reading of file fails
     * @throws DeserializationException if deserialization fails
     */
    public AssetAdministrationShellEnvironment loadAASEnvironment() throws IOException, DeserializationException {
        String path = Path.of(destination, filename).toString();
        File f = new File(path);
        if (f.exists() && !f.isDirectory()) {
            return AASEnvironmentHelper.fromFile(f);
        }
        return null;
    }

}
