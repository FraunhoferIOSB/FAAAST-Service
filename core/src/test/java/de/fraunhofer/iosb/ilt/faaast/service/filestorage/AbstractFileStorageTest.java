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

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.InMemoryFile;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * A test class for a file storage implementation should inherit from this abstract class. This class provides basic
 * tests for all methods defined in {@link de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage}.
 *
 * @param <T> type of the file storage to test
 * @param <C> type of the config matching the file storage to test
 */
public abstract class AbstractFileStorageTest<T extends FileStorage<C>, C extends FileStorageConfig<T>> {

    protected static final ServiceContext SERVICE_CONTEXT = Mockito.mock(ServiceContext.class);
    private T fileStorage;

    /**
     * Gets an instance of the concrete file storage config to use.
     *
     * @return the file storage configuration
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException if configuration fails
     */
    public abstract C getFileStorageConfig() throws ConfigurationException;


    @Test
    public void saveAndDelete() throws ResourceNotFoundException, ConfigurationException, PersistenceException {
        FileStorageConfig<T> config = getFileStorageConfig();
        fileStorage = config.newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        InMemoryFile expected = InMemoryFile.builder()
                .path("my/path/file.txt")
                .content("foo".getBytes())
                .build();
        fileStorage.save(expected);
        byte[] actual = fileStorage.get(expected.getPath());
        Assert.assertArrayEquals(expected.getContent(), actual);
        fileStorage.delete(expected.getPath());
        Assert.assertThrows(ResourceNotFoundException.class, () -> fileStorage.get(expected.getPath()));
    }
}
