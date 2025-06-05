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
package de.fraunhofer.iosb.ilt.faaast.service.filestorage.memory;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage;
import de.fraunhofer.iosb.ilt.faaast.service.model.EnvironmentContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.internal.AASXUtils;


/**
 * Implementation of {@link de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage} for in memory storage.
 */
public class FileStorageInMemory implements FileStorage<FileStorageInMemoryConfig> {

    private FileStorageInMemoryConfig config;
    private final Map<String, byte[]> files;

    public FileStorageInMemory() {
        files = new ConcurrentHashMap<>();
    }


    @Override
    public void init(CoreConfig coreConfig, FileStorageInMemoryConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
        EnvironmentContext environmentContext = null;
        try {
            environmentContext = config.loadInitialModelAndFiles();
            environmentContext.getFiles().stream().forEach(v -> save(v.getPath(), v.getFileContent()));
        }
        catch (DeserializationException | InvalidConfigurationException e) {
            throw new ConfigurationInitializationException("error initializing in-memory file storage", e);
        }
    }


    @Override
    public byte[] get(String path) throws ResourceNotFoundException {
        String key = AASXUtils.removeFilePartOfURI(path);
        if (!files.containsKey(key)) {
            throw new ResourceNotFoundException(String.format("could not find file for path '%s'", key));
        }
        return files.get(key);
    }


    @Override
    public boolean contains(String path) {
        return this.files.containsKey(AASXUtils.removeFilePartOfURI(path));
    }


    @Override
    public void save(String path, byte[] content) {
        files.put(AASXUtils.removeFilePartOfURI(path), content);
    }


    @Override
    public void delete(String path) throws ResourceNotFoundException {
        String key = AASXUtils.removeFilePartOfURI(path);
        if (!files.containsKey(key)) {
            throw new ResourceNotFoundException(String.format("could not find file for path '%s'", key));
        }
        files.remove(key);
    }


    @Override
    public void deleteAll() throws PersistenceException {
        files.clear();
    }


    @Override
    public FileStorageInMemoryConfig asConfig() {
        return config;
    }

}
