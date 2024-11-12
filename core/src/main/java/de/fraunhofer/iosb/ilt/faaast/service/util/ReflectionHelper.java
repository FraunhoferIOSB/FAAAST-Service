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

import de.fraunhofer.iosb.ilt.faaast.service.model.ServiceSpecificationProfile;
import io.github.classgraph.ClassGraph;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Helper for tasks related to reflection.
 */
public class ReflectionHelper {

    private static final String ROOT_PACKAGE_NAME = "de.fraunhofer.iosb.ilt.faaast.service";
    private static final String MODEL_PACKAGE_NAME = ROOT_PACKAGE_NAME + ".model";
    private static final List<Class<? extends Enum>> EXCLUDED = List.of(ServiceSpecificationProfile.class);

    /**
     * List of enum classes that are part of the FA³ST model.
     */
    public static final List<Class<? extends Enum>> ENUMS;

    private ReflectionHelper() {}

    static {
        ENUMS = new ClassGraph()
                .enableClassInfo()
                .acceptPackages(MODEL_PACKAGE_NAME)
                .scan()
                .getAllEnums()
                .loadClasses(Enum.class)
                .stream()
                .filter(x -> !EXCLUDED.contains(x))
                .collect(Collectors.toList());
    }

    /**
     * Reads the value of a field of the obj.
     *
     * @param <T> the type of the value to read
     * @param obj the obj to read the property from
     * @param fieldName the name of the field to read
     * @param type the type of the value to read
     * @return the value of the field
     * @throws NoSuchFieldException if the field does not exist
     * @throws IllegalAccessException if the field cannot be accessed
     */
    public static <T> T getField(Object obj, String fieldName, Class<T> type) throws NoSuchFieldException, IllegalAccessException {
        Field field = findField(obj, fieldName);
        field.setAccessible(true);
        return type.cast(field.get(obj));
    }


    /**
     * Sets the value of a field of the obj.
     *
     * @param obj the obj to set the property on
     * @param fieldName the name of the field to read
     * @param value the value to set
     * @throws NoSuchFieldException if the field does not exist
     * @throws IllegalAccessException if the field cannot be accessed
     */
    public static void setField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = findField(obj, fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }


    /**
     * Recursively finds a field with given name on the obj.
     *
     * @param obj the object to find the field on
     * @param fieldName the name of the field
     * @return the field if found, if not null
     * @throws java.lang.NoSuchFieldException if field does not exist
     */
    public static Field findField(Object obj, String fieldName) throws NoSuchFieldException {
        if (Objects.isNull(obj)) {
            return null;
        }
        Class<?> clazz = obj.getClass();
        while (Objects.nonNull(clazz)) {
            try {
                return clazz.getDeclaredField(fieldName);
            }
            catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(String.format("no field with name '%s' found", fieldName));
    }

}
