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

import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.AmbiguousElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.AssetAdministrationShellElementVisitor;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.DefaultAssetAdministrationShellElementSubtypeResolvingVisitor;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.ReferenceCollector;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.HasSemantics;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


/**
 * Helper class for working with {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment}.
 */
public class EnvironmentHelper {

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
     * @param environment the environment to resolve the reference in
     * @param returnType return type of the element
     * @return the resolved element
     * @throws IllegalArgumentException if reference is null or empty
     * @throws IllegalArgumentException if environment is null
     * @throws IllegalArgumentException if resolved element does not match the return type
     * @throws ResourceNotFoundException if reference cannot be resolved because element does not exist in environment
     */
    public static <T extends Referable> T resolve(Reference reference, Environment environment, Class<T> returnType) throws ResourceNotFoundException {
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.require(!reference.getKeys().isEmpty(), "reference must contain at least one key");
        Ensure.requireNonNull(environment, "environment must be non-null");
        Ensure.requireNonNull(returnType, "type must be non-null");
        Referable result = ReferenceCollector.collect(environment).entrySet().stream()
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
     * Resolves a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} in a given
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment}.
     *
     * @param reference the reference to resolve
     * @param environment the eenvironment to resolve the reference in
     * @return the resolved element
     * @throws IllegalArgumentException if reference is null or empty
     * @throws IllegalArgumentException if environment is null
     * @throws IllegalArgumentException if returnType is null
     * @throws ResourceNotFoundException if reference cannot be resolved because element does not exist in environment
     */
    public static Referable resolve(Reference reference, Environment environment) throws ResourceNotFoundException {
        return resolve(reference, environment, Referable.class);
    }


    /**
     * Generates a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} for a
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.Referable} given an
     * {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} as context.
     *
     * <p>This method does not work in all cases as it tries to find the referable in the environment by equality. However,
     * this will fail if the is more than one element in the environment that are equals, e.g. as part of different
     * parents.
     *
     * @param referable the referable to generate the reference for
     * @param environment the environment containing the referable
     * @return a reference pointing to the referable
     * @throws de.fraunhofer.iosb.ilt.faaast.service.model.exception.AmbiguousElementException if there are multiple
     *             matching elements in the environment
     * @throws IllegalArgumentException if referable or environment is null
     * @throws IllegalArgumentException if referable is not present in environment
     */
    public static Reference asReference(Referable referable, Environment environment) throws AmbiguousElementException {
        Ensure.requireNonNull(referable, "referable must be non-null");
        Ensure.requireNonNull(environment, "environment must be non-null");
        List<Reference> result = ReferenceCollector.collect(environment).entrySet().stream()
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


    /**
     * Resolves an idShortPath within a given submodel.
     *
     * @param <T> expected return type
     * @param idShortPath the idShortPath to resolve
     * @param submodel the submodle to resolve the idShortPath in
     * @param type the expected return type
     * @return the resolved {@link SubmodelElement}
     * @throws ResourceNotFoundException if path is not resolvable
     * @throws IllegalArgumentException if path does resolve to more than one element.
     */
    public static <T extends SubmodelElement> T resolveUniquePath(IdShortPath idShortPath, Submodel submodel, Class<T> type) throws ResourceNotFoundException {
        Ensure.requireNonNull(idShortPath, "idShortPath must be non-null");
        Ensure.requireNonNull(submodel, "submodel must be non-null");
        Ensure.requireNonNull(type, "type must be non-null");
        List<SubmodelElement> result = submodel.getSubmodelElements().stream()
                .flatMap(x -> resolvePathRecursive(idShortPath.getElements(), x, (id, element) -> Objects.equals(id, element.getIdShort())).stream())
                .collect(Collectors.toList());
        if (result.isEmpty()) {
            throw new ResourceNotFoundException(String.format("unable to resolve idShortPath on submodel (idShortPath: %s, submodel id: %s)",
                    idShortPath,
                    submodel.getId()));
        }
        if (result.size() > 1) {
            throw new IllegalArgumentException("idShortPath did resolve to more than one element");
        }
        return type.cast(result.get(0));
    }


    /**
     * Resolves an idShortPath within a given submodel element.
     *
     * @param <T> expected return type
     * @param idShortPath the idShortPath to resolve
     * @param element the submodle element to resolve the idShortPath in
     * @param type the expected return type
     * @return the resolved {@link SubmodelElement}
     * @throws ResourceNotFoundException if path is not resolvable
     * @throws IllegalArgumentException if path does resolve to more than one element.
     */
    public static <T extends SubmodelElement> T resolveUniquePath(IdShortPath idShortPath, SubmodelElement element, Class<T> type) throws ResourceNotFoundException {
        Ensure.requireNonNull(idShortPath, "idShortPath must be non-null");
        Ensure.requireNonNull(element, "root must be non-null");
        Ensure.requireNonNull(type, "type must be non-null");

        List<SubmodelElement> result = resolvePathRecursive(idShortPath.getElements(), element, (id, sme) -> Objects.equals(id, sme.getIdShort()));
        if (result.isEmpty()) {
            throw new ResourceNotFoundException(String.format("unable to resolve idShortPath on submodel element (idShortPath: %s, submodel element idShort: %s)",
                    idShortPath,
                    element.getIdShort()));
        }
        if (result.size() > 1) {
            throw new IllegalArgumentException("idShortPath did resolve to more than one element");
        }
        return type.cast(result.get(0));
    }


    /**
     * Resolves a semanticIdPath within a given submodel.
     *
     * @param <T> expected return type
     * @param semanticIdPath the semanticIdPath to resolve
     * @param submodel the submodle to resolve the semanticIdPath in
     * @param type the expected return type
     * @return the resolved {@link SubmodelElement}
     * @throws ResourceNotFoundException if path is not resolvable
     * @throws IllegalArgumentException if path does resolve to more than one element.
     */
    public static <T extends SubmodelElement> List<T> resolvePath(List<Reference> semanticIdPath, Submodel submodel, Class<T> type) throws ResourceNotFoundException {
        Ensure.requireNonNull(semanticIdPath, "semanticIdPath must be non-null");
        Ensure.requireNonNull(submodel, "submodel must be non-null");
        Ensure.requireNonNull(type, "type must be non-null");
        return submodel.getSubmodelElements().stream()
                .flatMap(x -> resolvePathRecursive(semanticIdPath, x,
                        (reference, element) -> ReferenceHelper.equals(reference, element.getSemanticId())).stream())
                .map(type::cast)
                .collect(Collectors.toList());
    }


    /**
     * Resolves a semanticIdPath within a given submodel.
     *
     * @param <T> expected return type
     * @param semanticIdPath the semanticIdPath to resolve
     * @param submodel the submodle to resolve the semanticIdPath in
     * @param type the expected return type
     * @return the resolved {@link SubmodelElement}
     * @throws ResourceNotFoundException if path is not resolvable
     * @throws IllegalArgumentException if path does resolve to more than one element.
     */
    public static <T extends SubmodelElement> T resolveUniquePath(List<Reference> semanticIdPath, Submodel submodel, Class<T> type) throws ResourceNotFoundException {
        List<T> result = resolvePath(semanticIdPath, submodel, type);
        if (result.isEmpty()) {
            throw new ResourceNotFoundException(String.format("unable to resolve semanticIdPath on submodel (semanticIdPath: %s, submodel id: %s)",
                    semanticIdPath.stream().map(ReferenceHelper::asString).collect(Collectors.joining(" -> ")),
                    submodel.getId()));
        }
        if (result.size() > 1) {
            throw new IllegalArgumentException("semanticIdPath did resolve to more than one element");
        }
        return result.get(0);
    }


    /**
     * Resolves an idShortPath within a given submodel element.
     *
     * @param <T> expected return type
     * @param semanticIdPath the semanticIdPath to resolve
     * @param submodelElement the submodel element to resolve the semanticIdPath in
     * @param type the expected return type
     * @return the resolved {@link SubmodelElement}
     * @throws ResourceNotFoundException if path is not resolvable
     * @throws IllegalArgumentException if path does resolve to more than one element.
     */
    public static <T extends SubmodelElement> T resolveUniquePath(List<Reference> semanticIdPath, SubmodelElement submodelElement, Class<T> type) throws ResourceNotFoundException {
        Ensure.requireNonNull(semanticIdPath, "semanticIdPath must be non-null");
        Ensure.requireNonNull(submodelElement, "submodelElement must be non-null");
        Ensure.requireNonNull(type, "type must be non-null");

        List<SubmodelElement> result = resolvePathRecursive(semanticIdPath, submodelElement,
                (reference, element) -> ReferenceHelper.equals(reference, element.getSemanticId()));
        if (result.isEmpty()) {
            throw new ResourceNotFoundException(String.format("unable to resolve semanticIdPath on submodelElement (semanticIdPath: %s, submodelElement id: %s)",
                    semanticIdPath.stream().map(ReferenceHelper::asString).collect(Collectors.joining(" -> ")),
                    submodelElement.getIdShort()));
        }
        if (result.size() > 1) {
            throw new IllegalArgumentException("semanticIdPath did resolve to more than one element");
        }
        return type.cast(result.get(0));
    }


    private static <T, U extends Referable & HasSemantics> List<U> resolvePathRecursive(List<T> path, U element, BiFunction<T, U, Boolean> equalsTester) {
        List<T> remainingPath = path.size() > 1 ? path.subList(1, path.size()) : List.of();
        if (path.isEmpty() || !equalsTester.apply(path.get(0), element)) {
            return List.of();
        }
        if (remainingPath.isEmpty()) {
            return List.of(element);
        }
        return getChildren(element).stream()
                .flatMap(x -> resolvePathRecursive(remainingPath, x, equalsTester).stream())
                .collect(Collectors.toList());
    }


    private static <T extends Referable & HasSemantics> List<T> getChildren(T parent) {
        final List<T> children = new ArrayList<>();
        AssetAdministrationShellElementVisitor visitor = new DefaultAssetAdministrationShellElementSubtypeResolvingVisitor() {
            @Override
            public void visit(Submodel submodel) {
                add(submodel.getSubmodelElements());
            }


            @Override
            public void visit(SubmodelElementCollection submodelElementCollection) {
                add(submodelElementCollection.getValue());
            }


            @Override
            public void visit(SubmodelElementList submodelElementList) {
                add(submodelElementList.getValue());
            }


            private void add(List<SubmodelElement> elements) {
                elements.forEach(x -> children.add((T) x));
            }
        };
        visitor.visit((Referable) parent);
        return children;
    }
}
