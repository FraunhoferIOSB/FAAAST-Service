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
package de.fraunhofer.iosb.ilt.faaast.service.filestorage.filesystem;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage;
import de.fraunhofer.iosb.ilt.faaast.service.model.EnvironmentContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.FileContent;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Implementation of {@link de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage} for file system storage.
 */
public class FileStorageFilesystem implements FileStorage<FileStorageFilesystemConfig> {

    private FileStorageFilesystemConfig config;
    private final Map<String, Path> filelist;

    public FileStorageFilesystem() {
        filelist = new ConcurrentHashMap<>();
    }


    public void init(CoreConfig coreConfig, FileStorageFilesystemConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
        EnvironmentContext environmentContext = null;
        try {
            environmentContext = config.loadInitialModelAndFiles();
            environmentContext.getFiles().stream().forEach(v -> save(v.getPath(),
                    FileContent.builder()
                            .content(v.getFileContent())
                            .build()));
        }
        catch (DeserializationException | InvalidConfigurationException e) {
            throw new ConfigurationInitializationException("error initializing file-system file storage", e);
        }
    }


    @Override
    public FileContent get(String path) throws ResourceNotFoundException {
        String base64 = Base64.getUrlEncoder().encodeToString(path.getBytes());
        if (!filelist.containsKey(base64)) {
            throw new ResourceNotFoundException(String.format("could not find file for path '%s'", path));
        }
        Path diskpath = filelist.get(base64);
        if (Objects.isNull(diskpath)) {
            throw new ResourceNotFoundException(String.format("could not find file for path '%s'", path));
        }
        try {
            return new FileContent.Builder()
                    .content(Files.readAllBytes(diskpath))
                    .build();
        }
        catch (IOException e) {
            throw new ResourceNotFoundException(String.format("could not find file for path '%s'", path));
        }
    }


    @Override
    public void save(String path, FileContent file) {
        Path diskPath = null;
        String base64 = Base64.getUrlEncoder().encodeToString(path.getBytes());
        try {
            diskPath = Files.write(Path.of(base64), file.getContent());
            filelist.put(base64, diskPath);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void delete(String path) throws ResourceNotFoundException {
        String base64 = Base64.getUrlEncoder().encodeToString(path.getBytes());
        if (!filelist.containsKey(base64)) {
            throw new ResourceNotFoundException(String.format("could not delete file for path '%s'", path));
        }
        try {
            Files.delete(filelist.get(base64));
            filelist.remove(base64);
        }
        catch (IOException e) {
            throw new ResourceNotFoundException(String.format("could not delete file for path '%s'", path));
        }

    }


    @Override
    public Map<String, FileContent> getAllFiles() {
        Map<String, FileContent> map = new HashMap<>();
        filelist.forEach((key, value) -> {
            try {
                map.put(new String(Base64.getUrlDecoder().decode(key)), FileContent.builder()
                        .content(Files.readAllBytes(value))
                        .build());
            }
            catch (IOException e) {
                throw new RuntimeException(String.format("could not find a file referenced by file storage"));
            }
        });
        return map;
    }


    @Override
    public FileStorageFilesystemConfig asConfig() {
        return config;
    }

}