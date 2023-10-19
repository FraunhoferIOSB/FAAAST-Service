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
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Implementation of {@link de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage} for file system storage.
 */
public class FileStorageFilesystem implements FileStorage<FileStorageFilesystemConfig> {

    private static final String MSG_FILE_NOT_FOUND = "could not find file for path '%s'";
    private FileStorageFilesystemConfig config;
    private final Map<String, Path> filelist;

    public FileStorageFilesystem() {
        filelist = new ConcurrentHashMap<>();
    }


    @Override
    public void init(CoreConfig coreConfig, FileStorageFilesystemConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
        EnvironmentContext environmentContext = null;
        try {
            environmentContext = config.loadInitialModelAndFiles();
            environmentContext.getFiles().stream().forEach(LambdaExceptionHelper.rethrowConsumer(v -> save(v.getPath(), v.getFileContent())));
        }
        catch (DeserializationException | InvalidConfigurationException | IOException e) {
            throw new ConfigurationInitializationException("error initializing file-system file storage", e);
        }
    }


    @Override
    public byte[] get(String path) throws ResourceNotFoundException {
        String encoded = encodeFilePath(path);
        if (!filelist.containsKey(encoded)) {
            throw new ResourceNotFoundException(String.format(MSG_FILE_NOT_FOUND, path));
        }
        Path diskpath = filelist.get(encoded);
        if (Objects.isNull(diskpath)) {
            throw new ResourceNotFoundException(String.format(MSG_FILE_NOT_FOUND, path));
        }
        try {
            return Files.readAllBytes(diskpath);
        }
        catch (IOException e) {
            throw new ResourceNotFoundException(String.format(MSG_FILE_NOT_FOUND, path));
        }
    }


    private String encodeFilePath(String filePath) {
        return Base64.getUrlEncoder().encodeToString(filePath.getBytes());
    }


    private String decodeFilePath(String filePath) {
        return new String(Base64.getUrlDecoder().decode(filePath.getBytes()));
    }


    @Override
    public void save(String path, byte[] content) throws IOException {
        Path diskPath = null;
        String encoded = encodeFilePath(path);
        diskPath = Files.write(Path.of(encoded), content);
        filelist.put(encoded, diskPath);
    }


    @Override
    public void delete(String path) throws ResourceNotFoundException, IOException {
        String encoded = encodeFilePath(path);
        if (!filelist.containsKey(encoded)) {
            throw new ResourceNotFoundException(String.format("could not delete file for path '%s'", path));
        }
        Files.delete(filelist.get(encoded));
        filelist.remove(encoded);
    }


    @Override
    public boolean contains(String path) {
        return this.filelist.containsKey(path);
    }


    @Override
    public FileStorageFilesystemConfig asConfig() {
        return config;
    }

}
