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
package de.fraunhofer.iosb.ilt.faaast.service.model;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.AssetAdministrationShellElementVisitor;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.DefaultAssetAdministrationShellElementSubtypeResolvingVisitor;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.HasSemantics;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Reprensts a semanticIdPath path addressing one or more SubmodelElement within a submodel.
 */
public class SemanticIdPath {

    List<Reference> elements;

    public SemanticIdPath() {
        this.elements = new ArrayList<>();
    }


    public List<Reference> getElements() {
        return List.copyOf(elements);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SemanticIdPath other = (SemanticIdPath) o;
        if (elements.size() != other.elements.size()) {
            return false;
        }
        for (int i = 0; i < elements.size(); i++) {
            if (!ReferenceHelper.equals(elements.get(i), other.elements.get(i))) {
                return false;
            }
        }
        return true;
    }


    @Override
    public int hashCode() {
        return Objects.hash(elements);
    }


    @Override
    public String toString() {
        return elements.stream()
                .map(ReferenceHelper::asString)
                .collect(Collectors.joining(" --> "));
    }


    /**
     * Creates a new idShortPath without the first/parent segment.
     *
     * @return idShortPath without the first/parent segment
     */
    public SemanticIdPath withoutParent() {
        SemanticIdPath result = new SemanticIdPath();
        result.elements = Objects.isNull(elements) || elements.size() <= 1
                ? List.of()
                : elements.subList(1, elements.size());
        return result;
    }


    /**
     * Resolves a semanticIdPath.
     *
     * @param <T> type of the root element
     * @param root the root to resolve the path in
     * @return a list of references to elements that match the semanticIdPath
     */
    public <T extends Referable & HasSemantics> List<Reference> resolve(T root) {
        Ensure.requireNonNull(root, "root must be non-null");
        return resolveRecursive(root, this, null);
    }


    /**
     * Uniquely resolves a semanticIdPath inside a given root element.
     *
     * @param <T> the type of the root
     * @param root the root to resolve the path in
     * @return the reference to element that matches the semanticIdPath
     * @throws ResourceNotFoundException if path does not resolve to an element
     * @throws IllegalArgumentException if the semanticIdPath resolves to more than one element
     */
    public <T extends Referable & HasSemantics> Reference resolveUnique(T root) throws ResourceNotFoundException {
        Ensure.requireNonNull(root, "root must be non-null");
        List<Reference> result = resolveRecursive(root, this, null);
        if (result.isEmpty()) {
            throw new ResourceNotFoundException("semanticIdPath did resolve to an element");
        }
        if (result.size() > 1) {
            throw new IllegalArgumentException("semanticIdPath did resolve to more than one element");
        }
        return result.get(0);
    }


    /**
     * Uniquely resolves a semanticIdPath inside a given root element.
     *
     * @param <T> the type of the root
     * @param root the root to resolve the path in
     * @param type the expected effective type of the reference
     * @return the reference to element that matches the semanticIdPath
     * @throws ResourceNotFoundException if path does not resolve to an element
     * @throws IllegalArgumentException if the semanticIdPath resolves to more than one element
     */
    public <T extends Referable & HasSemantics> Reference resolveUnique(T root, KeyTypes type) throws ResourceNotFoundException {
        Ensure.requireNonNull(type, "type must be non-null");
        Reference result = resolveUnique(root);
        KeyTypes actualType = ReferenceHelper.getEffectiveKeyType(result);
        if (!Objects.equals(type, actualType)) {
            throw new IllegalArgumentException(String.format(
                    "semanticIdPath does not resolve to correct type of element (expected: %s, actual: %s)",
                    type,
                    actualType));
        }
        return result;
    }


    /**
     * Uniquely resolves a semanticIdPath inside a given root element.
     *
     * @param <I> type of the root element
     * @param <T> type of the target elements
     * @param root the root to resolve the path in
     * @param type the type of the target elements
     * @return the element that matches the semanticIdPath if present, otherwise Optional.empty()
     * @throws IllegalArgumentException if the semanticIdPath resolves to more than one element
     */
    public <I extends Referable & HasSemantics, T extends Referable & HasSemantics> Optional<T> resolveOptional(I root, Class<T> type) {
        Ensure.requireNonNull(type, "type must be non-null");
        try {
            return Optional.ofNullable(EnvironmentHelper.resolve(resolveUnique(root), root, type));
        }
        catch (ResourceNotFoundException e) {
            return Optional.empty();
        }
    }


    /**
     * Resolves a semanticIdPath to elements of a given type. Elements matching the semanticIdPath having a different type
     * are ignored.
     *
     * @param <I> type of the root element
     * @param <T> type of the target elements
     * @param root the root to resolve the path in
     * @param type the type of the target elements
     * @return a list of references to elements that match the semanticIdPath and type
     */
    public <I extends Referable & HasSemantics, T extends Referable & HasSemantics> List<T> resolve(I root, Class<T> type) {
        Ensure.requireNonNull(root, "root must be non-null");
        return resolveRecursive(root, this, null).stream()
                .map(x -> {
                    try {
                        return EnvironmentHelper.resolve(x, root);
                    }
                    catch (ResourceNotFoundException ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
    }


    private static <T extends Referable & HasSemantics> List<Reference> resolveRecursive(T current, SemanticIdPath remainingPath, Reference parentRef) {
        Ensure.requireNonNull(current, "current must be non-null");
        Ensure.requireNonNull(remainingPath, "path must be non-null");
        Reference currentRef = ReferenceHelper.combine(
                parentRef,
                new ReferenceBuilder()
                        .element(
                                Identifiable.class.isInstance(current)
                                        ? ((Identifiable) current).getId()
                                        : current.getIdShort(),
                                ReferenceHelper.toKeyType(current.getClass()))
                        .build());
        if (remainingPath.isEmpty()) {
            return List.of(currentRef);
        }
        if (SubmodelElementList.class.isInstance(current)) {
            List<SubmodelElement> children = ((SubmodelElementList) current).getValue();
            List<Reference> result = new ArrayList<>();
            for (int i = 0; i < children.size(); i++) {
                if (ReferenceHelper.equals(children.get(i).getSemanticId(), remainingPath.getElements().get(0))) {
                    final String newChildId = Integer.toString(i);
                    List<Reference> childRefs = resolveRecursive(children.get(i), remainingPath.withoutParent(), currentRef);
                    result.addAll(childRefs.stream()
                            .map(x -> {
                                x.getKeys().get(currentRef.getKeys().size()).setValue(newChildId);
                                return x;
                            })
                            .toList());
                }
            }
            return result;
        }
        return getChildren(current).stream()
                .filter(x -> ReferenceHelper.equals(x.getSemanticId(), remainingPath.getElements().get(0)))
                .flatMap(x -> resolveRecursive(x, remainingPath.withoutParent(), currentRef).stream())
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


    /**
     * Checks if this path is empty, i.e. does not contain any elements.
     *
     * @return true if empty, otherwise false
     */
    public boolean isEmpty() {
        return Objects.isNull(elements) || elements.isEmpty();
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends SemanticIdPath, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B from(SemanticIdPath value) {
            getBuildingInstance().elements = new ArrayList<>(value.elements);
            return getSelf();
        }


        public B semanticId(Reference value) {
            getBuildingInstance().elements.add(value);
            return getSelf();
        }


        public B globalReference(String value) {
            getBuildingInstance().elements.add(ReferenceBuilder.global(value));
            return getSelf();
        }


        public B semanticIds(Reference... value) {
            getBuildingInstance().elements.addAll(Arrays.asList(value));
            return getSelf();
        }


        public B semanticIds(List<Reference> value) {
            getBuildingInstance().elements.addAll(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<SemanticIdPath, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected SemanticIdPath newBuildingInstance() {
            return new SemanticIdPath();
        }
    }
}
