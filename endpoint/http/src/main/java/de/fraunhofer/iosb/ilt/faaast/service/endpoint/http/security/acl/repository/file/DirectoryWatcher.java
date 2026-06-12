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

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;


/**
 * Keeps track of added, modified and deleted files within a file system directory.
 */
public class DirectoryWatcher {

    private static final String DOT_JSON = ".json";
    private final Path dir;
    private final WatchService watchService;
    private final List<DirectoryWatcherListener> listeners = new CopyOnWriteArrayList<>();
    private volatile boolean running = true;

    /**
     * Class constructor.
     *
     * @param dir Directory to keep track of
     * @throws IOException Registering WatchService on directory failed
     */
    public DirectoryWatcher(Path dir) throws IOException {
        this.dir = dir.toAbsolutePath().normalize();
        this.watchService = FileSystems.getDefault().newWatchService();
        this.dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        Thread workerThread = new Thread(this::processEvents, "DirectoryWatcher-" + dir);
        workerThread.setDaemon(true);
        workerThread.start();
    }


    /**
     * Add a listener to the watcher service.
     *
     * @param listener Listener to be added
     */
    public void addListener(DirectoryWatcherListener listener) {
        listeners.add(listener);
    }


    private void processEvents() {
        try {
            while (running) {
                WatchKey key;
                try {
                    key = watchService.take(); // blocks
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                catch (ClosedWatchServiceException e) {
                    break;
                }

                for (WatchEvent<?> event: key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path relative = (Path) event.context();

                    if (kind == StandardWatchEventKinds.OVERFLOW || !isJsonFileEnding(relative)) {
                        continue;
                    }

                    Path full = dir.resolve(relative);

                    if (kind == ENTRY_CREATE) {
                        notifyAbout(full, DirectoryWatcherListener::onFileCreated);
                    }
                    else if (kind == ENTRY_DELETE) {
                        notifyAbout(full, DirectoryWatcherListener::onFileDeleted);
                    }
                    else if (kind == ENTRY_MODIFY) {
                        notifyAbout(full, DirectoryWatcherListener::onFileModified);
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        }
        finally {
            running = false;
        }
    }


    private boolean isJsonFileEnding(Path path) {
        String pathString = path.toString();
        return pathString.substring(pathString.length() - DOT_JSON.length()).equalsIgnoreCase(DOT_JSON);
    }


    private void notifyAbout(Path file, BiConsumer<DirectoryWatcherListener, Path> action) {
        for (DirectoryWatcherListener l: listeners) {
            action.accept(l, file);
        }
    }
}
