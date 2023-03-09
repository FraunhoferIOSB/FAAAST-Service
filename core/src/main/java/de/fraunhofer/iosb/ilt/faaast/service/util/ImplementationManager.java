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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages loading of external JAR files used to provide custom implementations. Loads all JAR files that are located in
 * the same directory as the JAR containing this class.
 */
public class ImplementationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImplementationManager.class);
    private static ClassLoader classLoader = ImplementationManager.class.getClassLoader();
    private static boolean isInitialized = false;

    private ImplementationManager() {}


    /**
     * Returns the {@link java.lang.ClassLoader} that contains all the dynamically loaded JAR files.
     *
     * @return the {@link java.lang.ClassLoader} that contains all the dynamically loaded JAR files
     */
    public static ClassLoader getClassLoader() {
        init();
        return classLoader;
    }


    private static Class<?> getMainClass() throws ClassNotFoundException {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (trace.length > 0) {
            return Class.forName(trace[trace.length - 1].getClassName());
        }
        throw new ClassNotFoundException();
    }


    /**
     * Initializes the ImplementationManager by scanning current directory for additional *.jar files and
     * loading them.
     */
    public static synchronized void init() {
        if (isInitialized) {
            return;
        }
        File temp;
        try {
            temp = new File(JarFilePathHelper.getJarFilePath(getMainClass()));
        }
        catch (Exception e) {
            temp = new File(JarFilePathHelper.getJarFilePath(ImplementationManager.class));
        }
        final File jar = temp;
        File dir = jar.getParentFile();
        LOGGER.info("Scanning directory '{}' for jar files...", dir);
        try {
            File[] files = dir.listFiles((File dir1, String name) -> name.toLowerCase().endsWith(".jar")
                    && (!jar.isFile() || !name.equals(jar.getName())));
            if (files != null) {
                classLoader = new URLClassLoader(
                        Stream.of(files)
                                .map(ImplementationManager::fileToUrl)
                                .filter(Objects::nonNull)
                                .toArray(URL[]::new),
                        ImplementationManager.class.getClassLoader());
            }
        }
        catch (SecurityException e) {
            LOGGER.error("Scanning directory '{}' for jar files failed", dir, e);
        }
        isInitialized = true;
    }


    private static URL fileToUrl(File file) {
        URL result = null;
        try {
            result = file.toURI().toURL();
            LOGGER.info("Loaded external jar: {}", file.getName());
            return result;
        }
        catch (MalformedURLException e) {
            LOGGER.error("Failed to load external jar: {}", file.getName(), e);
        }
        return null;
    }

}
