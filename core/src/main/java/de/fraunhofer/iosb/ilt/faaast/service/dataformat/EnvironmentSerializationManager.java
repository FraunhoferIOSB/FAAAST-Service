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

import de.fraunhofer.iosb.ilt.faaast.service.model.EnvironmentContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.FileHelper;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provides access to {@link EnvironmentSerializer}s and {@link EnvironmentDeserializer}s based on desired
 * {@link DataFormat}.
 */
public class EnvironmentSerializationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentSerializationManager.class);
    public static final String MSG_DATA_FORMAT_MUST_BE_NON_NULL = "dataFormat must be non-null";
    private static final String MSG_FILE_MUST_BE_NON_NULL = "file must be non-null";
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
     * @throws IllegalArgumentException if no {@link EnvironmentSerializer} exists for given dataType or instantiation
     *             fails
     */
    public static EnvironmentSerializer serializerFor(DataFormat dataFormat) {
        Ensure.requireNonNull(dataFormat, MSG_DATA_FORMAT_MUST_BE_NON_NULL);
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
     * @throws IllegalArgumentException if no {@link EnvironmentDeserializer} exists for given dataType or instantiation
     *             fails
     */
    public static EnvironmentDeserializer deserializerFor(DataFormat dataFormat) {
        Ensure.requireNonNull(dataFormat, MSG_DATA_FORMAT_MUST_BE_NON_NULL);
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


    /**
     * Reads an {@link Environment} from given file while automatically determining used data
     * format based on file extension.
     *
     * @param file the file to read
     * @return the deserialized environment context
     * @throws DeserializationException if file extensions is not supported
     * @throws DeserializationException if deserialization fails
     */
    public static EnvironmentContext deserialize(File file) throws DeserializationException {
        Ensure.requireNonNull(file, MSG_FILE_MUST_BE_NON_NULL);
        init();
        List<DataFormat> potentialDataFormats = getPotentialDataFormats(file);
        for (DataFormat dataFormat: potentialDataFormats) {
            try {
                return deserializerFor(dataFormat).read(file);
            }
            catch (DeserializationException e) {
                // intentionally suppress exception as this probably indicates that we have an ambiguous file extension and this was not the correct deserializer
            }
        }

        throw new DeserializationException(
                String.format("error reading AAS file - could be not parsed using any of the potential data formats identified by file extension (potential data formats: %s)",
                        potentialDataFormats.stream()
                                .map(Enum::name)
                                .collect(Collectors.joining(","))));
    }


    /**
     * Gets the {@link de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat} for a given file. This is
     * based on the file extension and in a second step the content of the file.
     *
     * @param file the input file
     * @return the data format of the file
     * @throws DeserializationException if there is now data format matching the file extension
     * @throws DeserializationException file cannot be deserialized in any supported data format
     */
    public static DataFormat getDataFormat(File file) throws DeserializationException {
        List<DataFormat> potentialDataFormats = getPotentialDataFormats(file);
        for (DataFormat dataFormat: potentialDataFormats) {
            try {
                deserializerFor(dataFormat).read(file);
                return dataFormat;
            }
            catch (DeserializationException e) {
                // intentionally suppress exception as this probably indicates that we have an ambiguous file extension and this was not the correct deserializer
            }
        }
        throw new DeserializationException(
                String.format("error reading AAS file - could be not parsed using any of the potential data formats identified by file extension (potential data formats: %s)",
                        potentialDataFormats.stream()
                                .map(Enum::name)
                                .collect(Collectors.joining(","))));
    }


    private static List<DataFormat> getPotentialDataFormats(File file) throws DeserializationException {
        Ensure.requireNonNull(file, MSG_FILE_MUST_BE_NON_NULL);
        String fileExtension = FileHelper.getFileExtensionWithoutSeparator(file);
        List<DataFormat> potentialDataFormats = DataFormat.forFileExtension(fileExtension);
        if (potentialDataFormats.isEmpty()) {
            throw new DeserializationException(String.format("error reading AAS file - no supported data format found for extension '%s'", fileExtension));
        }
        return potentialDataFormats;
    }
}
