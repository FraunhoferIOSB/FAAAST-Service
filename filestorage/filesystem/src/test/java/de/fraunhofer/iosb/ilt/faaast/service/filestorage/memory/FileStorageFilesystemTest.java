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

import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.AbstractFileStorageTest;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.filesystem.FileStorageFilesystem;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.filesystem.FileStorageFilesystemConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.InMemoryFile;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;


public class FileStorageFilesystemTest extends AbstractFileStorageTest<FileStorageFilesystem, FileStorageFilesystemConfig> {

    @Override
    public FileStorageFilesystemConfig getFileStorageConfig() {
        return FileStorageFilesystemConfig.builder()
                .build();
    }


    @Test
    public void testCustomPath() throws ConfigurationException, IOException, ResourceNotFoundException, PersistenceException {
        Path rootPath = Path.of("foo/bar");
        Path filePath = Path.of("my/path/file.txt");
        rootPath.toFile().deleteOnExit();
        rootPath.getParent().toFile().deleteOnExit();
        byte[] content = "foo".getBytes();
        FileStorageFilesystemConfig config = FileStorageFilesystemConfig.builder()
                .path(rootPath.toString())
                .build();
        FileStorageFilesystem fileStorage = config.newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        InMemoryFile expected = InMemoryFile.builder()
                .path(filePath.toString())
                .content(content)
                .build();
        fileStorage.save(expected);
        Assert.assertTrue(Files.exists(rootPath));
        List<Path> files = Files.list(rootPath).collect(Collectors.toList());
        Assert.assertEquals(1, files.size());
        byte[] actual = Files.readAllBytes(files.get(0));
        Assert.assertArrayEquals(expected.getContent(), actual);
        fileStorage.delete(expected.getPath());
    }


    @Test
    public void testExistingFiles() throws ConfigurationException, IOException, ResourceNotFoundException {
        Path tempDir = Files.createTempDirectory("faast-filesystem-storage-test");
        Path nestedDir = Files.createDirectory(tempDir.resolve("foo"));
        Path tempFile = Files.createTempFile(nestedDir, "dummy-data-file", "");
        tempFile.toFile().deleteOnExit();
        nestedDir.toFile().deleteOnExit();
        tempDir.toFile().deleteOnExit();
        byte[] content = "foo".getBytes();
        Files.write(tempFile, content);
        FileStorageFilesystemConfig config = FileStorageFilesystemConfig.builder()
                .existingDataPath(tempDir.toString())
                .build();
        FileStorageFilesystem fileStorage = config.newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        byte[] actual = fileStorage.get(tempDir.relativize(tempFile).toString());
        Assert.assertArrayEquals(content, actual);
    }
}
