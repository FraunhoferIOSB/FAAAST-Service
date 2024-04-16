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

import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.util.ReflectionHelper;
import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.AnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.BasicEventElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.Capability;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.RelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class for working with {@link SubmodelElementList}.
 */
public class SubmodelElementListHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmodelElementListHelper.class);

    private SubmodelElementListHelper() {}


    /**
     * Gets the type of elements inside this {@link SubmodelElementList}. If the property is set on the list, it is
     * returned as-is. If the property is not present, it is automatically determined.
     *
     * @param submodelElementList the list to get the element type for
     * @return the element type of this {@link SubmodelElementList} as Java class or null if the input is null
     * @throws IllegalArgumentException if element type is not present and cannot be automatically be determined, e.g.,
     *             because the list contains different types of elements which is not a valid state
     */
    public static Class<? extends SubmodelElement> getElementType(SubmodelElementList submodelElementList) {
        if (Objects.isNull(submodelElementList)) {
            return null;
        }
        if (Objects.nonNull(submodelElementList.getTypeValueListElement())) {
            return submodelElementTypeToClass(submodelElementList.getTypeValueListElement());
        }
        return determineElementType(submodelElementList);
    }


    /**
     * Gets the datatype of elements inside this {@link SubmodelElementList}. If the property is set on the list, it is
     * returned as-is. If the property is not present, it is automatically determined.
     *
     * @param submodelElementList the list to get the element type for
     * @return the datatype of this {@link SubmodelElementList} or null if the input is null. If no datatype is defined
     *         within the elements, {@link Datatype#DEFAULT} is returned.
     * @throws IllegalArgumentException if datatype is not present and cannot be automatically be determined, e.g.,
     *             because the list contains different datatypes which is not a valid state
     */
    public static Datatype getDatatype(SubmodelElementList submodelElementList) {
        if (Objects.isNull(submodelElementList)) {
            return null;
        }
        if (Objects.nonNull(submodelElementList.getValueTypeListElement())) {
            return Datatype.fromAas4jDatatype(submodelElementList.getValueTypeListElement());
        }
        return determineDatatype(submodelElementList);
    }


    /**
     * Converts an {@link AasSubmodelElements} type to the corresponding Java class.
     *
     * @param submodelElementType the type to convert
     * @return the corresponding Java class
     * @throws IllegalArgumentException if the type is not a valid subtype of SubmodelElement.
     */
    public static Class<? extends SubmodelElement> submodelElementTypeToClass(AasSubmodelElements submodelElementType) {
        switch (submodelElementType) {
            case ANNOTATED_RELATIONSHIP_ELEMENT:
                return AnnotatedRelationshipElement.class;
            case BASIC_EVENT_ELEMENT:
                return BasicEventElement.class;
            case BLOB:
                return Blob.class;
            case CAPABILITY:
                return Capability.class;
            case ENTITY:
                return Entity.class;
            case FILE:
                return File.class;
            case MULTI_LANGUAGE_PROPERTY:
                return MultiLanguageProperty.class;
            case OPERATION:
                return Operation.class;
            case PROPERTY:
                return Property.class;
            case RANGE:
                return Range.class;
            case REFERENCE_ELEMENT:
                return ReferenceElement.class;
            case RELATIONSHIP_ELEMENT:
                return RelationshipElement.class;
            case SUBMODEL_ELEMENT_COLLECTION:
                return SubmodelElementCollection.class;
            case SUBMODEL_ELEMENT_LIST:
                return SubmodelElementList.class;
            default:
                throw new IllegalArgumentException(String.format(
                        "Found unsupported element type for SubmodelElement (%s)",
                        submodelElementType));
        }
    }


    private static Class<? extends SubmodelElement> determineElementType(SubmodelElementList submodelElementList) {
        if (Objects.isNull(submodelElementList.getValue())
                || submodelElementList.getValue().isEmpty()
                || submodelElementList.getValue().stream().allMatch(Objects::isNull)) {
            return submodelElementTypeToClass(submodelElementList.getTypeValueListElement());
        }
        Set<Class<? extends SubmodelElement>> typesPresent = submodelElementList.getValue().stream()
                .filter(Objects::nonNull)
                .map(Object::getClass)
                .map(ReflectionHelper::getAasInterface)
                .filter(SubmodelElement.class::isAssignableFrom)
                .map(x -> (Class<? extends SubmodelElement>) x)
                .collect(Collectors.toSet());
        if (typesPresent.size() > 1) {
            throw new IllegalArgumentException(String.format(
                    "Could not determine element type for elements in SubmodelElementList because it contains multiple different types (%s)",
                    typesPresent.stream().map(Class::getSimpleName).collect(Collectors.joining(", "))));
        }
        return typesPresent.iterator().next();
    }


    private static Datatype determineDatatype(SubmodelElementList submodelElementList) {
        Datatype defaultResult = Objects.nonNull(submodelElementList.getValueTypeListElement())
                ? Datatype.fromAas4jDatatype(submodelElementList.getValueTypeListElement())
                : null;
        if (Objects.isNull(submodelElementList.getValue())
                || submodelElementList.getValue().isEmpty()
                || submodelElementList.getValue().stream().allMatch(Objects::isNull)) {
            return defaultResult;
        }
        Class<? extends Object> elementType = getElementType(submodelElementList);
        Function<Object, DataTypeDefXsd> extractor = null;
        if (Property.class.isAssignableFrom(elementType)) {
            extractor = x -> ((Property) x).getValueType();
        }
        else if (Range.class.isAssignableFrom(elementType)) {
            extractor = x -> ((Range) x).getValueType();
        }
        if (extractor == null) {
            return defaultResult;
        }
        Set<Datatype> datatypesPresent = submodelElementList.getValue().stream()
                .filter(Objects::nonNull)
                .map(extractor)
                .filter(Objects::nonNull)
                .map(Datatype::fromAas4jDatatype)
                .collect(Collectors.toSet());
        if (datatypesPresent.isEmpty()) {
            LOGGER.debug("Could not determine datatype for elements in SubmodelElementList because no datatype information available - using default ({})",
                    Datatype.DEFAULT);
            return Datatype.DEFAULT;
        }
        if (datatypesPresent.size() > 1) {
            throw new IllegalArgumentException(String.format(
                    "Could not determine datatype for elements in SubmodelElementList because it contains multiple different datatypes (%s)",
                    datatypesPresent.stream().map(Enum::name).collect(Collectors.joining(", "))));
        }
        return datatypesPresent.iterator().next();
    }
}
