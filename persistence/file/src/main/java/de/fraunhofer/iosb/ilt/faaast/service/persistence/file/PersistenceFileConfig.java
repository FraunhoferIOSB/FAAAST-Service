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

import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.PersistenceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.util.AASEnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.adminshell.aas.v3.dataformat.DeserializationException;
import java.nio.file.Path;
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
    public static final String DEFAULT_FILENAME_PREFIX = "environment_createdByFAAAST";
    public static final String DEFAULT_FILENAME = DEFAULT_FILENAME_PREFIX + "." + DEFAULT_DATAFORMAT.toString().toLowerCase();

    private String dataDir;

    private boolean keepInitial;

    private String filename = DEFAULT_FILENAME;

    private DataFormat dataformat;

    public PersistenceFileConfig() {
        dataDir = DEFAULT_BASE_PATH;
        keepInitial = DEFAULT_KEEP_INITIAL;
    }


    /**
     * Sets the file name according to the configuration parameters
     *
     * @throws DeserializationException if parsing of aas environment fails
     */
    public void init() throws DeserializationException {
        if (!isKeepInitial()) {
            Ensure.requireNonNull(getInitialModel());
            filename = getInitialModel().getName();
            dataDir = getInitialModel().getParent();
            dataformat = AASEnvironmentHelper.getDataformat(getInitialModel());
            Path filePath = getFilePath().toAbsolutePath();
            LOGGER.info("File Persistence overrides the original model file {}", filePath);
        }
        else if (getInitialModel() != null && dataformat == null) {
            dataformat = AASEnvironmentHelper.getDataformat(getInitialModel());
            filename = DEFAULT_FILENAME_PREFIX + "." + dataformat.toString().toLowerCase();
        }
        else if (dataformat != null) {
            filename = DEFAULT_FILENAME_PREFIX + "." + dataformat.toString().toLowerCase();
        }
        else {
            dataformat = DEFAULT_DATAFORMAT;
        }
    }


    /**
     * Get the current file path of the model file used by the file persistence
     *
     * @return file path of the model file
     */
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

        return Objects.equals(this.dataDir, other.dataDir) &&
                Objects.equals(this.keepInitial, other.keepInitial) &&
                Objects.equals(this.dataformat, other.dataformat);
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
