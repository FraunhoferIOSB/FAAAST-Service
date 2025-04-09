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

import de.fraunhofer.iosb.ilt.faaast.service.config.Configurable;
import de.fraunhofer.iosb.ilt.faaast.service.model.InMemoryFile;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;


/**
 * Generic interface for file storage implementations.
 *
 * @param <C> type of the corresponding configuration class
 */
public interface FileStorage<C extends FileStorageConfig> extends Configurable<C> {

    /**
     * Gets a file from the storage.
     *
     * @param path the path to the file
     * @return the file content
     * @throws ResourceNotFoundException if the path does not exist
     * @throws PersistenceException if storage error occurs
     */
    public byte[] get(String path) throws ResourceNotFoundException, PersistenceException;


    /**
     * Returns a boolean for a file existing in the storage.
     *
     * @param path the path to the file
     * @return true if file is in storage
     * @throws PersistenceException if storage error occurs
     */
    public boolean contains(String path) throws PersistenceException;


    /**
     * Saves the file to given path.
     *
     * @param path the path to save the file under
     * @param content the file content to save
     * @throws PersistenceException if storage error occurs
     */
    public void save(String path, byte[] content) throws PersistenceException;


    /**
     * Deletes the file under given path.
     *
     * @param path the path to the file to delete
     * @throws ResourceNotFoundException if path does not exist
     * @throws PersistenceException if storage error occurs
     */
    public void delete(String path) throws ResourceNotFoundException, PersistenceException;


    /**
     * Deletes all files present in the storage.
     *
     * @throws PersistenceException if storage error occurs
     */
    public void deleteAll() throws PersistenceException;


    /**
     * Saves the file to given path.
     *
     * @param file the file to save
     * @throws PersistenceException if storage error occurs
     */
    public default void save(InMemoryFile file) throws PersistenceException {
        save(file.getPath(), file.getContent());
    }
}
