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
package de.fraunhofer.iosb.ilt.faaast.service.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.faaast.service.config.Config;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.EnvironmentSerializationManager;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import java.io.File;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Generic persistence configuration. When implementing a custom persistence inherit from this class to create a custom
 * configuration.
 *
 * @param <T> type of the persistence
 */
public abstract class PersistenceConfig<T extends Persistence> extends Config<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceConfig.class);
    protected File initialModelFile;
    @JsonIgnore
    protected Environment initialModel;

    public File getInitialModelFile() {
        return initialModelFile;
    }


    public Environment getInitialModel() {
        return initialModel;
    }


    public void setInitialModel(Environment initialModel) {
        this.initialModel = initialModel;
    }


    public void setInitialModelFile(File initialModelFile) {
        this.initialModelFile = initialModelFile;
    }


    /**
     * Loads the initial model from code/memory if present, otherwise from file.
     *
     * @return the loaded initial model or an empty model if neither an initial in-memory model nor an initial model
     *         file is specified.
     * @throws InvalidConfigurationException if initial model file should be used and file does not exist or is not a
     *             file
     * @throws DeserializationException if deserialization fails
     */
    public Environment loadInitialModel() throws InvalidConfigurationException, DeserializationException {
        if (Objects.nonNull(initialModel)) {
            LOGGER.debug("using model from code/memory");
            return initialModel;
        }
        if (Objects.nonNull(initialModelFile)) {
            if (!initialModelFile.exists()) {
                throw new InvalidConfigurationException(String.format("model file not found (file: %s)", initialModelFile));
            }
            if (!initialModelFile.isFile()) {
                throw new InvalidConfigurationException(String.format("model file is not file (file: %s)", initialModelFile));
            }
            return EnvironmentSerializationManager
                    .deserialize(initialModelFile)
                    .getEnvironment();
        }
        return new DefaultEnvironment.Builder().build();
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
        final PersistenceConfig<?> other = (PersistenceConfig<?>) obj;
        return Objects.equals(initialModelFile, other.initialModelFile)
                && Objects.equals(initialModel, other.initialModel);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), initialModelFile, initialModel);
    }

    /**
     * Abstract builder class that should be used for builders of inheriting classes.
     *
     * @param <T> type of the persistence of the config to build
     * @param <C> type of the config to build
     * @param <B> type of this builder, needed for inheritance builder pattern
     */
    public abstract static class AbstractBuilder<T extends Persistence, C extends PersistenceConfig<T>, B extends AbstractBuilder<T, C, B>> extends ExtendableBuilder<C, B> {

        public B initialModelFile(File value) {
            getBuildingInstance().setInitialModelFile(value);
            return getSelf();
        }


        public B initialModel(Environment value) {
            getBuildingInstance().setInitialModel(value);
            return getSelf();
        }

    }
}
