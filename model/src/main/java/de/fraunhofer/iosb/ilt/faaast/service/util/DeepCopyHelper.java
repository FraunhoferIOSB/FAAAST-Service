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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonMapperFactory;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.SimpleAbstractTypeResolverFactory;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;


/**
 * Helper class with methods to create deep copies. Following types are supported:
 * <ul>
 * <li>{@link org.eclipse.digitaltwin.aas4j.v3.model.Identifiable}
 * <li>{@link org.eclipse.digitaltwin.aas4j.v3.model.Referable}
 * <li>{@link org.eclipse.digitaltwin.aas4j.v3.model.Environment}
 * </ul>
 */
public class DeepCopyHelper {

    private static final JsonMapper mapper;

    static {
        mapper = new JsonMapperFactory().create(new SimpleAbstractTypeResolverFactory().create());
        mapper.setTypeFactory(mapper.getTypeFactory().withClassLoader(ImplementationManager.getClassLoader()));
    }

    private DeepCopyHelper() {}


    /**
     * Create a deep copy of a {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} object.
     *
     * @param env the asset administration shell environment which should be deep copied
     * @return a deep copied instance of the asset administration shell environment
     * @throws RuntimeException when operation fails
     */
    public static Environment deepCopy(Environment env) {
        try {
            return mapper.readValue(mapper.writeValueAsString(env), Environment.class);
        }
        catch (JsonProcessingException e) {
            throw new IllegalArgumentException("deep copy of AAS environment failed", e);
        }
    }


    /**
     * Create a deep copy of a {@link org.eclipse.digitaltwin.aas4j.v3.model.Referable} object.
     *
     * @param referable which should be deep copied
     * @param <T> type of the referable
     * @return the deep copied referable
     * @throws RuntimeException when operation fails
     */
    public static <T extends Referable> T deepCopy(T referable) {
        if (Objects.isNull(referable)) {
            return null;
        }
        return (T) deepCopy(referable, referable.getClass());
    }


    /**
     * Create a deep copy of a {@link org.eclipse.digitaltwin.aas4j.v3.model.Referable} object.
     *
     * @param referable which should be deep copied
     * @param outputClass of the referable
     * @param <T> type of the referable
     * @return the deep copied referable
     * @throws IllegalArgumentException if outputClass is null
     * @throws IllegalArgumentException if type of referable if not a subclass of outputClass
     * @throws RuntimeException when operation fails
     */
    public static <T extends Referable> T deepCopy(Referable referable, Class<T> outputClass) {
        if (outputClass == null) {
            throw new IllegalArgumentException("outputClass must be non-null");
        }
        if (referable != null && !outputClass.isAssignableFrom(referable.getClass())) {
            throw new IllegalArgumentException(
                    String.format("type mismatch - can not create deep copy of instance of type %s with target type %s", referable.getClass(), outputClass));
        }
        try {
            return mapper.readValue(mapper.writeValueAsString(referable), outputClass);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException("deep copy of AAS environment failed", e);
        }
    }


    /**
     * Create a deep copy of a list of {@link org.eclipse.digitaltwin.aas4j.v3.model.Referable} objects.
     *
     * @param referables list with referables which should be deep copied
     * @param outputClass of the referables
     * @param <T> type of the referables
     * @return a list with deep copied referables
     * @throws IllegalArgumentException if outputClass is null
     * @throws IllegalArgumentException if referables is null
     * @throws RuntimeException when operation fails
     */
    public static <T extends Referable> List<T> deepCopy(Collection<? extends T> referables, Class<? extends T> outputClass) {
        if (outputClass == null) {
            throw new IllegalArgumentException("outputClass must be non-null");
        }
        if (referables == null) {
            throw new IllegalArgumentException("referables must be non-null");
        }
        return referables.stream().map(x -> deepCopy(x, outputClass)).collect(Collectors.toList());
    }


    /**
     * Create a deep copy of a page of {@link org.eclipse.digitaltwin.aas4j.v3.model.Referable} objects.
     *
     * @param page page with referables which should be deep copied
     * @param outputClass of the referables
     * @param <T> type of the referables
     * @return a list with deep copied referables
     * @throws IllegalArgumentException if outputClass is null
     * @throws RuntimeException when operation fails
     */
    public static <T extends Referable> Page<T> deepCopy(Page<? extends T> page, Class<? extends T> outputClass) {
        if (page == null) {
            return null;
        }
        if (outputClass == null) {
            throw new IllegalArgumentException("outputClass must be non-null");
        }
        return Page.<T> builder()
                .metadata(PagingMetadata.builder()
                        .cursor(page.getMetadata().getCursor())
                        .build())
                .result(page.getContent().stream().map(x -> deepCopy(x, outputClass)).collect(Collectors.toList()))
                .build();
    }


    /**
     * Create a deep copy of a list of {@link org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable} objects.
     *
     * @param original the original list
     * @return a deep copy of the list or null if input is null
     */
    public static List<OperationVariable> deepCopy(Collection<OperationVariable> original) {
        if (Objects.isNull(original)) {
            return null;
        }
        return original.stream().map(x -> new DefaultOperationVariable.Builder()
                .value(deepCopy(x.getValue()))
                .build())
                .collect(Collectors.toList());
    }


    /**
     * Create a deep copy of any object by serializing and deserializing it using Jackson.
     *
     * @param <T> type of the object
     * @param original the original object
     * @param type type of the object
     * @return a deep copy of the object
     * @throws RuntimeException when deep copy fails
     */
    public static <T> T deepCopyAny(T original, Class<T> type) {
        return deepCopyAny(original, TypeFactory.defaultInstance().constructType(type));
    }


    /**
     * Create a deep copy of any object by serializing and deserializing it using Jackson.
     *
     * @param <T> type of the object
     * @param original the original object
     * @param type type of the object
     * @return a deep copy of the object
     * @throws RuntimeException when deep copy fails
     */
    public static <T> T deepCopyAny(T original, JavaType type) {
        try {
            if (Objects.nonNull(type) && type.hasGenericTypes() && type.getRawClass() == Page.class) {
                JavaType[] typeParams = type.findTypeParameters(Page.class);
                if (typeParams != null && typeParams.length == 1) {
                    Page page = (Page) original;
                    JavaType contentType = typeParams[0];
                    Page result = (Page) Page.builder()
                            .result((List) page.getContent().stream()
                                    .map(LambdaExceptionHelper.rethrowFunction(x -> mapper.readValue(mapper.writeValueAsString(x), contentType)))
                                    .collect(Collectors.toList()))
                            .build();
                    if (Objects.nonNull(page.getMetadata())) {
                        result.setMetadata(PagingMetadata.builder()
                                .cursor(page.getMetadata().getCursor())
                                .build());
                    }
                    return (T) result;
                }
            }

            String json = mapper.writeValueAsString(original);
            return mapper.readValue(json, type);
        }
        catch (Exception e) {
            throw new RuntimeException("Deep copy failed", e);
        }
    }
}
