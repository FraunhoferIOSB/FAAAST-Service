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
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;


/**
 * Manages loading of external JAR files used to provide custom implementations. Loads all JAR files that are located in
 * the same directory as the JAR containing this class.
 */
public class ImplementationManager {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ImplementationManager.class);
    private static ClassLoader classLoader = ImplementationManager.class.getClassLoader();

    private ImplementationManager() {}

    static {
        File jar = new File(JarFilePathHelper.getJarFilePath(ImplementationManager.class));
        File dir = jar.getParentFile();
        LOGGER.info("Scanning directory '{}' for jar files...", dir);
        try {
            File[] files = dir.listFiles((File dir1, String name) -> name.toLowerCase().endsWith(".jar") && !name.equals(jar.getName()));
            if (files != null) {
                List<URL> jars = new ArrayList<>();
                for (File file: files) {
                    try {
                        jars.add(file.toURI().toURL());
                        LOGGER.info("Loaded external jar: {}", file.getName());
                    }
                    catch (MalformedURLException e) {
                        LOGGER.error("Failed to load external jar: {}", file.getName(), e);
                    }
                }
                classLoader = new URLClassLoader(
                        jars.toArray(new URL[0]),
                        ImplementationManager.class.getClassLoader());
            }
        }
        catch (SecurityException e) {
            LOGGER.error("Scanning directory '{}' for jar files failed", dir, e);
        }
    }

    /**
     * Returns the {@link java.lang.ClassLoader} that contains all the dynamically loaded JAR files.
     *
     * @return the {@link java.lang.ClassLoader} that contains all the dynamically loaded JAR files
     */
    public static ClassLoader getClassLoader() {
        return classLoader;
    }
}
