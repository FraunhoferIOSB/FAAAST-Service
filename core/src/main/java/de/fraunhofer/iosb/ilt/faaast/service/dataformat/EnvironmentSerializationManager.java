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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat;

import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.Map;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provides access to {@link EnvironmentSerializer}s and
 * {@link EnvironmentDeserializer}s based on desired {@link DataFormat}.
 */
public class EnvironmentSerializationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentSerializationManager.class);
    private static boolean initialized = false;
    private static Map<DataFormat, Class<? extends EnvironmentSerializer>> serializers;
    private static Map<DataFormat, Class<? extends EnvironmentDeserializer>> deserializers;

    private EnvironmentSerializationManager() {}


    private static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        serializers = new EnumMap<>(DataFormat.class);
        deserializers = new EnumMap<>(DataFormat.class);
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .scan()) {
            for (var classInfo: scanResult.getClassesWithAnnotation(SupportedDataformat.class)) {
                DataFormat dataFormat = ((SupportedDataformat) classInfo.getAnnotationInfo(SupportedDataformat.class).loadClassAndInstantiate()).value();
                if (classInfo.implementsInterface(EnvironmentSerializer.class)) {
                    try {
                        // ensure default constructor is present
                        classInfo.loadClass().getConstructor();
                        serializers.put(dataFormat, classInfo.loadClass(EnvironmentSerializer.class));
                    }
                    catch (NoSuchMethodException e) {
                        LOGGER.warn("ignoring serializer because of missing default constructor (class: {})", classInfo.getName());
                    }
                }
                if (classInfo.implementsInterface(EnvironmentDeserializer.class)) {
                    try {
                        // ensure default constructor is present
                        classInfo.loadClass().getConstructor();
                        deserializers.put(dataFormat, classInfo.loadClass(EnvironmentDeserializer.class));
                    }
                    catch (NoSuchMethodException e) {
                        LOGGER.warn("ignoring deserializer because of missing default constructor (class: {})", classInfo.getName());
                    }
                }
            }
        }
    }


    /**
     * Find {@link EnvironmentSerializer} for given dataFormat.
     *
     * @param dataFormat the dataFormat
     * @return suitable {@link EnvironmentSerializer} for the dataFormat
     * @throws IllegalArgumentException if no {@link EnvironmentSerializer}
     *             exists for given dataType or instantiation fails
     */
    public static EnvironmentSerializer serializerFor(DataFormat dataFormat) {
        Ensure.requireNonNull(dataFormat, "dataFormat must be non-null");
        init();
        Ensure.require(serializers.containsKey(dataFormat), String.format("no serializer found for data format %s", dataFormat));
        try {
            return ConstructorUtils.invokeConstructor(serializers.get(dataFormat));
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new IllegalArgumentException(String.format("unable to instantiate serializer for %s (class: %s)",
                    dataFormat,
                    serializers.get(dataFormat).getName()),
                    e);
        }
    }


    /**
     * Find {@link EnvironmentDeserializer} for given dataFormat.
     *
     * @param dataFormat the dataFormat
     * @return suitable {@link EnvironmentDeserializer} for the dataFormat
     * @throws IllegalArgumentException if no {@link EnvironmentDeserializer}
     *             exists for given dataType or instantiation fails
     */
    public static EnvironmentDeserializer deserializerFor(DataFormat dataFormat) {
        Ensure.requireNonNull(dataFormat, "dataFormat must be non-null");
        init();
        Ensure.require(deserializers.containsKey(dataFormat), String.format("no deserializer found for data format %s", dataFormat));
        try {
            return ConstructorUtils.invokeConstructor(deserializers.get(dataFormat));
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new IllegalArgumentException(String.format("unable to instantiate deserializer for %s (class: %s)",
                    dataFormat,
                    deserializers.get(dataFormat).getName()),
                    e);
        }
    }

}
