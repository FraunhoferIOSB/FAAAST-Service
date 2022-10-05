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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http;

import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base class for mapping managers finding implementation classes via reflection.
 *
 * @param <T> type of mapping class to search for
 */
public abstract class AbstractMappingManager<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMappingManager.class);
    protected List<T> mappers;
    protected Class<T> mapperType;
    protected Object[] constructorArgs;

    protected AbstractMappingManager(Class<T> mapperType, Object... constructorArgs) {
        Ensure.requireNonNull(mapperType, "mapperType must be non-null");
        this.mapperType = mapperType;
        this.constructorArgs = constructorArgs != null
                ? constructorArgs
                : new Object[0];
        init();
    }


    private void init() {
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages(getClass().getPackageName())
                .scan()) {
            mappers = scanResult
                    .getSubclasses(mapperType.getName())
                    .filter(x -> !x.isAbstract() && !x.isInterface())
                    .loadClasses(mapperType)
                    .stream()
                    .map(x -> {
                        try {
                            return ConstructorUtils.invokeConstructor(x, constructorArgs);
                        }
                        catch (NoSuchMethodException | SecurityException e) {
                            LOGGER.warn("mapper implementation could not be loaded, "
                                    + "reason: missing constructor (implementation class: {}, required constructor signature: {})",
                                    x.getName(),
                                    Stream.of(constructorArgs).map(a -> a.getClass().getName()).collect(Collectors.joining(",")),
                                    e);
                        }
                        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            LOGGER.warn("mapper implementation could not be loaded, "
                                    + "reason: calling constructor failed (implementation class: {}, constructor arguments: {})",
                                    x.getName(),
                                    Stream.of(constructorArgs).map(a -> a.getClass().getName()).collect(Collectors.joining(",")),
                                    e);
                        }
                        LOGGER.debug("unable to instantiate class {}", x.getName());
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }
}
