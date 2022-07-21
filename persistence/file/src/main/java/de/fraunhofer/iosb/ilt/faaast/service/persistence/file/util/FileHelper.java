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

import de.fraunhofer.iosb.ilt.faaast.service.persistence.file.PersistenceFileConfig;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.adminshell.aas.v3.dataformat.DeserializationException;
import io.adminshell.aas.v3.dataformat.SerializationException;
import io.adminshell.aas.v3.dataformat.json.JsonDeserializer;
import io.adminshell.aas.v3.dataformat.json.JsonSerializer;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileHelper.class);
    public static final String FILENAME_SUFFIX = "json";

    public static final String DEFAULT_FILENAME = "environment_createdByFAAAST" + "." + FILENAME_SUFFIX;

    private String filename = DEFAULT_FILENAME;

    private String destination;

    private final ExecutorService executorService;

    public FileHelper(PersistenceFileConfig config) {
        this.destination = config.getDestination();

        if (config.isOverrideOriginalModelFile()) {
            Ensure.requireNonNull(config.getModelPath());
            File modelFile = new File(config.getModelPath());
            filename = modelFile.getName();
            destination = modelFile.getParent();
        }

        executorService = Executors.newFixedThreadPool(
                1,
                new BasicThreadFactory.Builder()
                        .namingPattern("FileHelper" + "-%d")
                        .build());
    }


    public Path getFilePath() {
        return Path.of(destination, filename);
    }


    public void save(AssetAdministrationShellEnvironment environment) {
        try (FileWriter myWriter = new FileWriter(getFilePath().toString())) {
            myWriter.write(new JsonSerializer().write(environment));
        }
        catch (IOException | SerializationException e) {
            LOGGER.error(String.format("Could not save environment to file %s", Path.of(destination, filename)), e);
        }
    }


    public void saveAsync(AssetAdministrationShellEnvironment environment) {
        executorService.submit(() -> save(environment));
    }


    public AssetAdministrationShellEnvironment loadAASEnvironment() throws IOException, DeserializationException {
        String path = Path.of(destination, filename).toString();
        File f = new File(path);
        if (f.exists() && !f.isDirectory()) {
            String env = FileUtils.readFileToString(new File(path), Charsets.UTF_8);
            return new JsonDeserializer().read(env);
        }
        return null;
    }

}
