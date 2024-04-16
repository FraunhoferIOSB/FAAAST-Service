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
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Reprensts an idShort path addressing a SubmodelElement within a submodel.
 */
public class IdShortPath {

    public static final IdShortPath EMPTY = IdShortPath.builder().build();

    private static final String ARRAY_INDEX_REGEX = "\\[\\d+\\]";
    private static final Pattern PATH_ELEMENT_PATTERN = Pattern.compile("[^\\.\\[\\]]+|" + ARRAY_INDEX_REGEX);
    private static final String SEPARATOR = ".";
    List<String> elements;

    public IdShortPath() {
        this.elements = new ArrayList<>();
    }


    public List<String> getElements() {
        return List.copyOf(elements);
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
        Ensure.require(Objects.nonNull(reference.getKeys()) && !reference.getKeys().isEmpty(), "reference must contain at least one keys");
        Ensure.require(Objects.equals(reference.getType(), ReferenceTypes.MODEL_REFERENCE), "reference must be a model reference");
        int startIndex = 0;
        if (ReferenceHelper.isKeyType(reference.getKeys().get(0), AssetAdministrationShell.class)) {
            startIndex = 1;
        }
        ReferenceHelper.ensureKeyType(reference.getKeys().get(startIndex), Submodel.class);
        IdShortPath.Builder builder = IdShortPath.builder();
        boolean inList = false;
        for (int i = startIndex + 1; i < reference.getKeys().size(); i++) {
            ReferenceHelper.ensureKeyType(reference.getKeys().get(i), SubmodelElement.class);
            if (inList) {
                builder.index(Long.parseUnsignedLong(reference.getKeys().get(i).getValue()));
            }
            else {
                builder.idShort(reference.getKeys().get(i).getValue());
            }
            inList = ReferenceHelper.isKeyType(reference.getKeys().get(i), SubmodelElementList.class);
        }
        return builder.build();
    }


    /**
     * Combines two idShort paths.
     *
     * @param parent the parent path
     * @param child the child path
     * @return the combined path or empty path if parent and child are null
     */
    public static IdShortPath combine(IdShortPath parent, IdShortPath child) {
        IdShortPath result = new IdShortPath();
        if (Objects.nonNull(parent)) {
            result.elements.addAll(parent.elements);
        }
        if (Objects.nonNull(child)) {
            result.elements.addAll(child.elements);
        }
        return result;
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
        return Objects.equals(elements, other.elements);
    }


    @Override
    public int hashCode() {
        return Objects.hash(elements);
    }


    @Override
    public String toString() {
        if (Objects.isNull(elements)) {
            return "";
        }
        String result = "";
        for (var element: elements) {
            if (!element.matches(ARRAY_INDEX_REGEX) && !StringHelper.isBlank(result)) {
                result += SEPARATOR;
            }
            result += element;
        }
        return result;
    }


    /**
     * Creates a reference representing this idShortParh. If it is possible to detect, the key type will be set to
     * {@link KeyTypes#SUBMODEL_ELEMENT_LIST} where appropriate, in all other occasions the key type will be set to
     * {@link KeyTypes#SUBMODEL_ELEMENT}.
     *
     * @return a reference representing this idShortPath
     */
    public Reference toReference() {
        ReferenceBuilder builder = new ReferenceBuilder();
        builder.type(ReferenceTypes.MODEL_REFERENCE);
        for (int i = 0; i < elements.size(); i++) {
            KeyTypes keyType = KeyTypes.SUBMODEL_ELEMENT;
            if (i < elements.size() - 1 && elements.get(i + 1).matches(ARRAY_INDEX_REGEX)) {
                keyType = KeyTypes.SUBMODEL_ELEMENT_LIST;
            }
            builder.element(
                    elements.get(i).matches(ARRAY_INDEX_REGEX)
                            ? elements.get(i).substring(1, elements.get(i).length() - 1)
                            : elements.get(i),
                    keyType);
        }
        return builder.build();
    }


    /**
     * Creates a new idShortPath pointing to the parent element by removing the last element in the path.
     *
     * @return idShortPath pointing to the parent element
     */
    public IdShortPath getParent() {
        IdShortPath result = new IdShortPath();
        result.elements = Objects.isNull(elements) || elements.size() <= 1
                ? List.of()
                : elements.subList(0, elements.size() - 1);
        return result;
    }


    /**
     * Checks if this idShortPath is empty, i.e. does not contain any elements.
     *
     * @return true if empty, otherwise false
     */
    public boolean isEmpty() {
        return Objects.isNull(elements) || elements.isEmpty();
    }


    /**
     * Parses an idShort from string.
     *
     * @param idShortPath the string representation of the idShortPath
     * @return the parsed idShortPath
     */
    public static IdShortPath parse(String idShortPath) {
        return IdShortPath.builder()
                .path(idShortPath)
                .build();
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends IdShortPath, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B from(IdShortPath value) {
            getBuildingInstance().elements = new ArrayList<>(value.elements);
            return getSelf();
        }


        public B idShort(String value) {
            getBuildingInstance().elements.add(value);
            return getSelf();
        }


        public B index(long value) {
            getBuildingInstance().elements.add("[" + value + "]");
            return getSelf();
        }


        public B index(String value) {
            getBuildingInstance().elements.add("[" + Long.parseUnsignedLong(value) + "]");
            return getSelf();
        }


        public B path(String value) {
            getBuildingInstance().elements = new ArrayList<>();
            pathSegment(value);
            return getSelf();
        }


        public B pathSegment(String value) {
            if (Objects.nonNull(value)) {
                var matcher = PATH_ELEMENT_PATTERN.matcher(value);
                while (matcher.find()) {
                    getBuildingInstance().elements.add(matcher.group());
                }
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
