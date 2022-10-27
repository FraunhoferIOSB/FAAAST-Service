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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.format;

import de.fraunhofer.iosb.ilt.faaast.service.util.ImplementationManager;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Factory to resolve implementations of Format interface based on key (given by Dataformat annotation).
 */
public class FormatFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormatFactory.class);
    private static Map<String, Class<? extends Format>> formats;

    private FormatFactory() {}


    private static synchronized void init() {
        if (formats != null) {
            return;
        }
        formats = new ConcurrentHashMap<>();
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .addClassLoader(ImplementationManager.getClassLoader())
                .scan()) {
            for (var classInfo: scanResult.getClassesWithAnnotation(Dataformat.class)) {
                String key = ((Dataformat) classInfo.getAnnotationInfo(Dataformat.class).loadClassAndInstantiate()).key();
                if (StringUtils.isBlank(key)) {
                    LOGGER.warn("Ignoring data format with empty key (class: {})", classInfo.getName());
                    continue;
                }
                if (!classInfo.implementsInterface(Format.class)) {
                    LOGGER.warn("Ignoring data format with key '{}' because implementing class does not implement interface 'Format' (class: {})",
                            key, classInfo.getName());
                    continue;
                }
                formats.put(key, classInfo.loadClass(Format.class));
            }
        }
    }


    /**
     * Instantiates a new format for given key via reflection.
     *
     * @param key the key to identify the format
     * @return new format representation
     * @throws IllegalArgumentException if no matching format can be found for {@code key}
     * @throws RuntimeException is instantiation of format fails
     */
    public static Format create(String key) {
        init();
        if (formats.containsKey(key)) {
            try {
                return formats.get(key).getDeclaredConstructor().newInstance();
            }
            catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(String.format("error instantiating data format (key: %s, class: %s)", key, formats.get(key).getName()), e);
            }
        }
        throw new IllegalArgumentException(String.format("unsupported content format (%s)", key));
    }
}
