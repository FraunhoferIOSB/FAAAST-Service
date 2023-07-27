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

import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.ReferenceCollector;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Helper class for working with {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment}.
 */
public class EnvironmentHelper {

    private static final Map<Environment, Map<Reference, Referable>> CACHE = new ConcurrentHashMap<>();

    private EnvironmentHelper() {}


    /**
     * Resolves a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} in a given
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment}. This also works when
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes} do not match exactly but content-wise, e.g.
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes#SUBMODEL_ELEMENT} vs
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes#PROPERTY}.
     *
     * @param <T> return type of the element
     * @param reference the reference to resolve
     * @param environment the eenvironment to resolve the reference in
     * @param returnType return type of the element
     * @return the resolved element
     * @throws IllegalArgumentException if reference is null or empty
     * @throws IllegalArgumentException if environment is null
     * @throws IllegalArgumentException if returnType is null or resolved element does not match the return type
     */
    public static <T extends Referable> T resolve(Reference reference, Environment environment, Class<T> returnType) {
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.require(!reference.getKeys().isEmpty(), "reference must contain at least one key");
        Ensure.requireNonNull(environment, "environment must be non-null");
        Ensure.requireNonNull(returnType, "type must be non-null");
        ensureCache(environment);
        Referable result = CACHE.get(environment).getOrDefault(reference,
                CACHE.get(environment).entrySet().stream()
                        .filter(x -> ReferenceHelper.equals(reference, x.getKey()))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(null));
        if (Objects.nonNull(result) && !returnType.isAssignableFrom(result.getClass())) {
            throw new IllegalArgumentException(String.format(
                    "unable to resolve reference as actual type does not match expected type (reference: %s, actual type: %s, expected type: %s)",
                    ReferenceHelper.toString(reference),
                    result.getClass(),
                    returnType));
        }
        return returnType.cast(result);
    }


    /**
     * Resolves a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} in a given
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment}.
     *
     * @param reference the reference to resolve
     * @param environment the eenvironment to resolve the reference in
     * @return the resolved element
     * @throws IllegalArgumentException if reference is null or empty
     * @throws IllegalArgumentException if environment is null
     * @throws IllegalArgumentException if returnType is null
     */
    public static Referable resolve(Reference reference, Environment environment) {
        return resolve(reference, environment, Referable.class);
    }


    /**
     * Generates a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} for a
     * {{@link org.eclipse.digitaltwin.aas4j.v3.model.Referable} given an
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} as context.
     *
     * @param referable the referable to generate the reference for
     * @param environment the environment containing the referable
     * @return a reference pointing to the referable
     * @throws IllegalArgumentException if referable or environment is null
     * @throws IllegalArgumentException if referable is not present in environment
     */
    public static Reference asReference(Referable referable, Environment environment) {
        Ensure.requireNonNull(referable, "referable must be non-null");
        Ensure.requireNonNull(environment, "environment must be non-null");
        ensureCache(environment);
        return CACHE.get(environment).entrySet().stream()
                .filter(x -> Objects.equals(referable, x.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "referable not present in environment (idShort: %s)",
                        referable.getIdShort())));
    }


    private static void ensureCache(Environment environment) {
        if (!CACHE.containsKey(environment)) {
            CACHE.put(environment, ReferenceCollector.collect(environment));
        }
    }
}
