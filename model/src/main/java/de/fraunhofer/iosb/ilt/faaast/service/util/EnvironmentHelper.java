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

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.AmbiguousElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.ReferenceCollector;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Helper class for working with {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment}.
 */
public class EnvironmentHelper {

    private EnvironmentHelper() {}


    /**
     * Resolves a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} in a given
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment}. This
     * also works when {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes} do not match exactly but content-wise, e.g.
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes#SUBMODEL_ELEMENT} vs
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes#PROPERTY}.
     *
     * @param <T> return type of the element
     * @param reference the reference to resolve
     * @param root the root element to resolve the reference in, e.g., Environment, Submodel, SubmodelElementCollection
     * @param returnType return type of the element
     * @return the resolved element
     * @throws IllegalArgumentException if reference is null or empty
     * @throws IllegalArgumentException if environment is null
     * @throws IllegalArgumentException if resolved element does not match the return type
     * @throws ResourceNotFoundException if reference cannot be resolved because element does not exist
     */
    public static synchronized <T extends Referable> T resolve(Reference reference, Object root, Class<T> returnType) throws ResourceNotFoundException {
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.require(!reference.getKeys().isEmpty(), "reference must contain at least one key");
        Ensure.requireNonNull(root, "root must be non-null");
        Ensure.requireNonNull(returnType, "type must be non-null");
        Referable result = ReferenceCollector.collect(root).entrySet().stream()
                .filter(x -> ReferenceHelper.equals(reference, x.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(reference));
        if (!returnType.isAssignableFrom(result.getClass())) {
            throw new IllegalArgumentException(String.format(
                    "unable to resolve reference as actual type does not match expected type (reference: %s, actual type: %s, expected type: %s)",
                    ReferenceHelper.toString(reference),
                    result.getClass(),
                    returnType));
        }
        return returnType.cast(result);
    }


    /**
     * Resolves a list of {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} in a given
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment}. This also works when
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes} do not
     * match exactly but content-wise, e.g. {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes#SUBMODEL_ELEMENT} vs
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes#PROPERTY}. This is much better performance-wise than calling
     * resolve(Reference, Object)
     * multiple times.
     *
     * @param references the reference to resolve
     * @param root the root element to resolve the reference in, e.g., Environment, Submodel, SubmodelElementCollection
     * @return the resolved elements
     * @throws IllegalArgumentException if environment is null
     * @throws ResourceNotFoundException if reference cannot be resolved because element does not exist
     */
    public static synchronized Map<Reference, ? extends Referable> resolve(Collection<Reference> references, Object root) throws ResourceNotFoundException {
        if (Objects.isNull(references) || references.isEmpty()) {
            return Map.of();
        }
        Ensure.requireNonNull(root, "root must be non-null");
        return ReferenceCollector.collect(root).entrySet().stream()
                .filter(x -> ReferenceHelper.containsSameReference(references, x.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }


    /**
     * Resolves a list of {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} in a given
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} where each reference must point to an element of given
     * type. This also works when
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes} do not match exactly but content-wise, e.g.
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes#SUBMODEL_ELEMENT} vs
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes#PROPERTY}.
     * This is much better performance-wise than calling resolve(Reference, Object) multiple times.
     *
     * @param <T> return type of the element
     * @param references the reference to resolve
     * @param root the root element to resolve the reference in, e.g., Environment, Submodel, SubmodelElementCollection
     * @param type return type of the element
     * @return the resolved elements
     * @throws IllegalArgumentException if environment is null
     * @throws ResourceNotFoundException if reference cannot be resolved because element does not exist
     * @throws IllegalArgumentException if any resolved element does not match the return type
     */
    public static synchronized <T extends Referable> Map<Reference, T> resolve(Collection<Reference> references, Object root, Class<T> type) throws ResourceNotFoundException {
        Ensure.requireNonNull(type, "type must be non-null");
        return resolve(references, root).entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        x -> Optional.of(x.getValue())
                                .filter(type::isInstance)
                                .map(type::cast)
                                .orElseThrow(() -> new IllegalArgumentException(String.format(
                                        "unable to resolve reference as actual type does not match expected type (reference: %s, actual type: %s, expected type: %s)",
                                        ReferenceHelper.asString(x.getKey()),
                                        x.getValue().getClass(),
                                        type)))));
    }


    /**
     * Resolves a list of {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} in a given
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} where each reference must point to an element. This also
     * works when
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes} do not match exactly but content-wise, e.g.
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes#SUBMODEL_ELEMENT} vs
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes#PROPERTY}.
     * This is much better performance-wise than calling resolve(Reference, Object) multiple times.
     *
     * @param reference the reference to resolve
     * @param root the root element to resolve the reference in, e.g., Environment, Submodel, SubmodelElementCollection
     * @return the resolved element
     * @throws IllegalArgumentException if reference is null or empty
     * @throws IllegalArgumentException if environment is null
     * @throws IllegalArgumentException if returnType is null
     * @throws ResourceNotFoundException if reference cannot be resolved because element does not exist
     */
    public static Referable resolve(Reference reference, Object root) throws ResourceNotFoundException {
        return resolve(reference, root, Referable.class);
    }


    /**
     * Generates a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} for a
     * {{@link org.eclipse.digitaltwin.aas4j.v3.model.Referable} given an
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} as context.
     *
     * <p>
     * This method does not work in all cases as it tries to find the referable in the environment by equality. However,
     * this will fail if the is more
     * than one element in the environment that are equals, e.g. as part of different parents.
     *
     * @param referable the referable to generate the reference for
     * @param root the root element containing the referable
     * @return a reference pointing to the referable
     * @throws de.fraunhofer.iosb.ilt.faaast.service.model.exception.AmbiguousElementException if there are multiple
     *             matching elements in the
     *             environment
     * @throws IllegalArgumentException if referable or environment is null
     * @throws IllegalArgumentException if referable is not present in environment
     */
    public static Reference asReference(Referable referable, Object root) throws AmbiguousElementException {
        Ensure.requireNonNull(referable, "referable must be non-null");
        Ensure.requireNonNull(root, "root must be non-null");
        List<Reference> result = ReferenceCollector.collect(root).entrySet().stream()
                .filter(x -> Objects.equals(referable, x.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (result.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                    "referable not present in environment (idShort: %s)",
                    referable.getIdShort()));
        }
        if (result.size() > 1) {
            throw new AmbiguousElementException(referable, result);
        }
        return result.get(0);
    }
}
