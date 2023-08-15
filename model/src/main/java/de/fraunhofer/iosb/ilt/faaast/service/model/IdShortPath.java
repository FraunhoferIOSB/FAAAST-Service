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

import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Reprensts an idShort path addressing a SubmodelElement.
 */
public class IdShortPath {

    private String submodelId;
    private List<String> elements;

    public IdShortPath() {
        this.elements = new ArrayList<>();
    }


    public String getSubmodelId() {
        return submodelId;
    }


    public void setSubmodelId(String submodelId) {
        this.submodelId = submodelId;
    }


    public List<String> getElements() {
        return elements;
    }


    public void setElements(List<String> elements) {
        this.elements = elements;
    }


    /**
     * Creates a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} equivalent to the idShort path.
     *
     * @return the reference
     * @throws IllegalArgumentException if submodelId is null
     * @throws IllegalArgumentException if elements is null
     */
    public Reference toReference() {
        Ensure.requireNonNull(submodelId, "submodelId must be non-null");
        Ensure.requireNonNull(elements, "elements must be non-null");
        return new ReferenceBuilder()
                .submodel(submodelId)
                .elements(elements.toArray(new String[0]))
                .build();
    }


    /**
     * Creates an idShort path equaivalent to the reference. The reference can either be of the form AAS -> Submodel ->
     * SubmodelElements* or Submodel -> SubmodelElements*. The key types must be of the expected type or any
     * suitable/related type, e.g. the key type for a SubmodelElement may be
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.KeyType#SUBMODEL_ELEMENT} or
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.KeyType#PROPERTY} or of any other subclass of SubmodelElement.
     *
     * <p>If the reference starts with an AAS key, the AAS key is discarded.
     *
     * @param reference the reference
     * @return the idShort path
     * @throws IllegalArgumentException if reference is null or does not contain the required elements
     * @throws IllegalArgumentException if the key types do not match the stated requirements
     */
    public static IdShortPath fromReference(Reference reference) {
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.require(Objects.nonNull(reference.getKeys()) && reference.getKeys().size() >= 1, "reference must contain at least one keys");
        Ensure.require(Objects.equals(reference.getType(), ReferenceTypes.MODEL_REFERENCE), "reference must be a model reference");
        int startIndex = 0;
        if (isKeyType(reference.getKeys().get(0), AssetAdministrationShell.class)) {
            startIndex = 1;
        }
        ensureKeyType(reference.getKeys().get(startIndex), Submodel.class);
        IdShortPath.Builder builder = IdShortPath.builder();
        builder.submodelId(reference.getKeys().get(startIndex).getValue());
        for (int i = startIndex + 1; i < reference.getKeys().size(); i++) {
            ensureKeyType(reference.getKeys().get(i), SubmodelElement.class);
            builder.element(reference.getKeys().get(i).getValue());
        }
        return builder.build();
    }


    private static boolean isKeyType(Key key, Class<?> type) {
        if (Objects.isNull(key) || Objects.isNull(type)) {
            return false;
        }
        return type.isAssignableFrom(AasUtils.keyTypeToClass(key.getType()));
    }


    private static void ensureKeyType(Key key, Class<?> type) {
        Ensure.requireNonNull(key, "key must be non-null");
        Class<?> keyClass = AasUtils.keyTypeToClass(key.getType());
        Ensure.requireNonNull(keyClass, String.format("unsupported key type '%s'", key.getType()));
        Ensure.require(
                type.isAssignableFrom(keyClass),
                String.format("key must be compatible to %s (found: %s)", type, keyClass));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdShortPath other = (IdShortPath) o;
        return Objects.equals(submodelId, other.submodelId)
                && Objects.equals(elements, other.elements);
    }


    @Override
    public int hashCode() {
        return Objects.hash(submodelId, elements);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends IdShortPath, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B submodelId(String value) {
            getBuildingInstance().setSubmodelId(value);
            return getSelf();
        }


        public B elements(List<String> value) {
            getBuildingInstance().setElements(value);
            return getSelf();
        }


        public B element(String value) {
            getBuildingInstance().getElements().add(value);
            return getSelf();
        }


        public B path(String value) {
            if (Objects.nonNull(value)) {
                elements(new ArrayList<>(Arrays.asList(value.split("\\."))));
            }
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<IdShortPath, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected IdShortPath newBuildingInstance() {
            return new IdShortPath();
        }
    }
}
