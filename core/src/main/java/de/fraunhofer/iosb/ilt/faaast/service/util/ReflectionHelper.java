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
import java.util.List;
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

}
