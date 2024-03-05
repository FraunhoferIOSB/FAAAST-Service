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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.file;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.EnvironmentSerializationManager;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.PersistenceConfig;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Configuration class for {@link PersistenceFile}.
 */
public class PersistenceFileConfig extends PersistenceConfig<PersistenceFile> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceFileConfig.class);
    private static final String DEFAULT_BASE_PATH = Path.of("").toAbsolutePath().toString();
    private static final boolean DEFAULT_KEEP_INITIAL = true;
    public static final DataFormat DEFAULT_DATAFORMAT = DataFormat.JSON;
    public static final String DEFAULT_FILENAME_PREFIX = "model_persistence";
    public static final String DEFAULT_FILENAME = DEFAULT_FILENAME_PREFIX + "." + DEFAULT_DATAFORMAT.toString().toLowerCase();

    private String dataDir;

    private boolean keepInitial;

    private String filename;

    private DataFormat dataformat;

    public PersistenceFileConfig() {
        keepInitial = DEFAULT_KEEP_INITIAL;
    }


    /**
     * Sets the file name according to the configuration parameters.
     *
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException if initialModelFile
     *             is present and cannot be parsed
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException if dataDir is not a
     *             valid path
     */
    public void init() throws ConfigurationInitializationException {
        try {
            if (Objects.nonNull(initialModelFile)) {
                if (keepInitial) {
                    if (Objects.isNull(dataformat)) {
                        dataformat = EnvironmentSerializationManager.getDataFormat(initialModelFile);
                    }
                    filename = DEFAULT_FILENAME_PREFIX + "." + dataformat.toString().toLowerCase();
                }
                else {
                    filename = initialModelFile.getName();
                    dataDir = initialModelFile.getParent();
                    dataformat = EnvironmentSerializationManager.getDataFormat(initialModelFile);
                }
            }
        }
        catch (DeserializationException e) {
            throw new ConfigurationInitializationException("error loading initial model file", e);
        }
        if (Objects.isNull(dataformat)) {
            dataformat = DEFAULT_DATAFORMAT;
        }
        if (Objects.isNull(dataDir)) {
            dataDir = DEFAULT_BASE_PATH;
        }
        try {
            Paths.get(dataDir).toRealPath();
        }
        catch (IOException | InvalidPathException | NullPointerException e) {
            throw new ConfigurationInitializationException(String.format("dataDir is not a valid directory (dataDir: %s)", dataDir), e);
        }
        if (Objects.isNull(filename)) {
            filename = DEFAULT_FILENAME_PREFIX + "." + dataformat.toString().toLowerCase();
        }
        LOGGER.debug("File Persistence uses file {}", getFilePath().toAbsolutePath());
    }


    /**
     * Get the current file path of the initialModel file used by the file persistence.
     *
     * @return file path of the initialModel file
     */
    @JsonIgnore
    public Path getFilePath() {
        return Path.of(dataDir, filename);
    }


    public String getDataDir() {
        return dataDir;
    }


    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }


    public boolean isKeepInitial() {
        return keepInitial;
    }


    public void setKeepInitial(boolean keepInitial) {
        this.keepInitial = keepInitial;
    }


    public DataFormat getDataformat() {
        return dataformat;
    }


    public void setDataformat(DataFormat dataformat) {
        this.dataformat = dataformat;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PersistenceFileConfig other = (PersistenceFileConfig) obj;

        return Objects.equals(this.dataDir, other.dataDir)
                && Objects.equals(this.keepInitial, other.keepInitial)
                && Objects.equals(this.dataformat, other.dataformat);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.dataDir, this.keepInitial, this.dataformat);
    }


    public static Builder builder() {
        return new Builder();
    }

    private abstract static class AbstractBuilder<T extends PersistenceFileConfig, B extends AbstractBuilder<T, B>>
            extends PersistenceConfig.AbstractBuilder<PersistenceFile, T, B> {

        public B keepInitial(boolean value) {
            getBuildingInstance().setKeepInitial(value);
            return getSelf();
        }


        public B dataDir(String value) {
            getBuildingInstance().setDataDir(value);
            return getSelf();
        }


        public B dataformat(DataFormat value) {
            getBuildingInstance().setDataformat(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<PersistenceFileConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected PersistenceFileConfig newBuildingInstance() {
            return new PersistenceFileConfig();
        }
    }

}
