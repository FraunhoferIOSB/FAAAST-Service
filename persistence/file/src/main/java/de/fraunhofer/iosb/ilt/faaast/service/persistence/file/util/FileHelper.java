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
            LOGGER.warn(String.format("Found multiple possible Serializers. Using Serializer for %s",
                    possibleDataFormats.get(0).toString()));
        }
        return possibleDataFormats.get(0);
    }


    public Path getFilePath() {
        return Path.of(destination, filename);
    }


    public void save(AssetAdministrationShellEnvironment environment) {
        try {
            AASEnvironmentHelper.toFile(environment, dataFormat, new File(String.valueOf(getFilePath())));
        }
        catch (IOException | SerializationException e) {
            LOGGER.error(String.format("Could not save environment to file %s", Path.of(destination, filename)), e);
        }
    }


    public AssetAdministrationShellEnvironment loadAASEnvironment() throws IOException, DeserializationException {
        String path = Path.of(destination, filename).toString();
        File f = new File(path);
        if (f.exists() && !f.isDirectory()) {
            return AASEnvironmentHelper.fromFile(f);
        }
        return null;
    }

}
