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
package de.fraunhofer.iosb.ilt.faaast.service.filestorage;

import de.fraunhofer.iosb.ilt.faaast.service.config.Config;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.EnvironmentSerializationManager;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.EnvironmentContext;
import java.io.File;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;


/**
 * Generic file storage configuration. When implementing a custom file storage inherit from this class to create a
 * custom configuration.
 *
 * @param <T> type of the file storage
 */
public abstract class FileStorageConfig<T extends FileStorage> extends Config<T> {

    protected File initialModelFile;

    public File getInitialModelFile() {
        return initialModelFile;
    }


    public void setInitialModelFile(File initialModelFile) {
        this.initialModelFile = initialModelFile;
    }


    /**
     * Loads the initial model and files from code/memory if present, otherwise from file.
     *
     * @return the loaded initial model with files or an empty model if neither an initial in-memory model nor an
     *         initial model file is specified.
     * @throws InvalidConfigurationException if initial model file should be used and file does not exist or is not a
     *             file
     * @throws DeserializationException if deserialization fails
     */
    public EnvironmentContext loadInitialModelAndFiles() throws InvalidConfigurationException, DeserializationException {
        if (Objects.nonNull(initialModelFile)) {
            if (!initialModelFile.exists()) {
                throw new InvalidConfigurationException(String.format("model file not found (file: %s)", initialModelFile));
            }
            if (!initialModelFile.isFile()) {
                throw new InvalidConfigurationException(String.format("model file is not file (file: %s)", initialModelFile));
            }
            return EnvironmentSerializationManager
                    .deserialize(initialModelFile);
        }
        return EnvironmentContext.builder()
                .environment(new DefaultEnvironment.Builder().build())
                .build();
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return Objects.equals(getClass(), obj.getClass());
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    /**
     * Abstract builder class that should be used for builders of inheriting classes.
     *
     * @param <T> type of the file storage of the config to build
     * @param <C> type of the config to build
     * @param <B> type of this builder, needed for inheritance builder pattern
     */
    public abstract static class AbstractBuilder<T extends FileStorage, C extends FileStorageConfig<T>, B extends AbstractBuilder<T, C, B>> extends ExtendableBuilder<C, B> {

    }
}
