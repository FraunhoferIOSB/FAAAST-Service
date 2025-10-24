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

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;


/**
 * Helper class to find JAR file path for a given class.
 */
public class JarFilePathHelper {

    private JarFilePathHelper() {}


    /**
     * Find JAR file path for a given class.
     *
     * @param <T> type of the class to find
     * @param clazz the class to find the JAR file path for
     * @return the file path of the JAR containing the class if available
     * @throws RuntimeException if path could not be resolved or class is not starter from a JAR file
     */
    public static <T> String getJarFilePath(Class<T> clazz) {
        try {
            return byGetProtectionDomain(clazz);
        }
        catch (Exception e) {
            // intentionally empty
        }
        return byGetResource(clazz);
    }


    private static <T> String byGetProtectionDomain(Class<T> clazz) throws URISyntaxException {
        URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        return Paths.get(url.toURI()).toString();
    }


    private static <T> String byGetResource(Class<T> clazz) {
        URL classResource = clazz.getResource(clazz.getSimpleName() + ".class");
        if (classResource == null) {
            throw new RuntimeException("class resource is null");
        }
        String url = classResource.toString();
        if (url.startsWith("jar:file:")) {
            String path = url.replaceAll("^jar:(file:.*[.]jar)!/.*", "$1");
            try {
                return Paths.get(new URL(path).toURI()).toString();
            }
            catch (Exception e) {
                throw new RuntimeException(String.format("Invalid Jar File URL String (%s)", url));
            }
        }
        throw new RuntimeException(String.format("Invalid Jar File URL String (%s)", url));
    }
}
