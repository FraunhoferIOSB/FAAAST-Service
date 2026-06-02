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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.acl.repository.file;

import java.nio.file.Path;


/**
 * Functionality to listen to a directory watcher, being notified of added, modified and deleted events.
 */
public interface DirectoryWatcherListener {

    /**
     * Called when a file in a file system directory has been created.
     *
     * @param path The path to the file that has been created
     */
    void onFileCreated(Path path);


    /**
     * Called when a file in a file system directory has been deleted.
     *
     * @param path The path to the file that has been deleted
     */
    void onFileDeleted(Path path);


    /**
     * Called when a file in a file system directory has been modified.
     *
     * @param path The path to the file that has been modified
     */
    void onFileModified(Path path);
}
