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
package de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper;

import com.google.common.reflect.TypeToken;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.MostSpecificClassComparator;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.util.ReflectionHelper;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Central class for bringing together submodel elements and their corresponding value representation, e.g. supports
 * converting submodel elements to their value representation and updating the value of a submodel element by providing
 * a value representation.
 */
public class ElementValueMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElementValueMapper.class);
    private static Map<Class<? extends SubmodelElement>, ? extends DataValueMapper> mappers;

    private ElementValueMapper() {}


    private static void init() {
        if (mappers == null) {
            ScanResult scanResult = new ClassGraph()
                    .enableAllInfo()
                    .acceptPackages(DataValueMapper.class.getPackageName())
                    .scan();
            mappers = scanResult.getClassesImplementing(DataValueMapper.class).loadClasses().stream()
                    .map(x -> (Class<? extends DataValueMapper>) x)
                    .collect(Collectors.toMap(
                            x -> (Class<? extends SubmodelElement>) TypeToken.of(x).resolveType(DataValueMapper.class.getTypeParameters()[0]).getRawType(),
                            x -> {
                                try {
                                    return ConstructorUtils.invokeConstructor(x);
                                }
                                catch (NoSuchMethodException | SecurityException e) {
                                    LOGGER.warn("element-value mapper implementation could not be loaded, "
                                            + "reason: missing constructor (implementation class: {})",
                                            x.getName(),
                                            e);
                                }
                                catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                                    LOGGER.warn("element-value mapper implementation  could not be loaded, "
                                            + "reason: calling constructor failed (implementation class: {}",
                                            x.getName(),
                                            e);
                                }
                                return null;
                            }));
        }
    }


    /**
     * Extracts the value of a {@link org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} into a corresponding
     * {@link ElementValue} instance.
     *
     * @param submodelElement for which a ElementValue should be created
     * @param <T> type of the input SubmodelElement
     * @return a value representation of the submodel element
     * @throws IllegalArgumentException if submodelElement is null
     * @throws ValueMappingException is no mapper for type of submodelElement can be found
     * @throws ValueMappingException if mapping fails
     */
    public static <T extends SubmodelElement> ElementValue toValue(T submodelElement) throws ValueMappingException {
        init();
        Ensure.requireNonNull(submodelElement, "submodelElement must be non-null");
        Class<?> aasInterface = ReflectionHelper.getAasInterface(submodelElement.getClass());
        if (!mappers.containsKey(aasInterface)) {
            throw new ValueMappingException("no mapper defined for AAS type " + aasInterface.getSimpleName());
        }
        return mappers.get(ReflectionHelper.getAasInterface(submodelElement.getClass())).toValue(submodelElement);
    }


    /**
     * Extracts the value of a {@link org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} into a corresponding
     * {@link ElementValue} instance.
     *
     *
     * @param submodelElement for which a ElementValue should be created
     * @param <I> type of the input SubmodelElement
     * @param <O> type of the output ElementValue
     * @param type expected result type
     * @return a value representation of the submodel element
     * @throws IllegalArgumentException if submodelElement is null
     * @throws ValueMappingException is no mapper for type of submodelElement can be found
     * @throws ValueMappingException if mapping fails
     */
    public static <I extends SubmodelElement, O extends ElementValue> O toValue(I submodelElement, Class<O> type) throws ValueMappingException {
        init();
        Ensure.requireNonNull(submodelElement, "submodelElement must be non-null");
        Ensure.requireNonNull(type, "type must be non-null");
        Class<?> aasInterface = ReflectionHelper.getAasInterface(submodelElement.getClass());
        if (!mappers.containsKey(aasInterface)) {
            throw new ValueMappingException(String.format("no mapper defined for AAS type %s", aasInterface.getSimpleName()));
        }
        ElementValue result = mappers.get(ReflectionHelper.getAasInterface(submodelElement.getClass())).toValue(submodelElement);
        if (Objects.nonNull(result) && !type.isAssignableFrom(result.getClass())) {
            throw new ValueMappingException(String.format("type mismatch (expected: %s but found: %s",
                    type.getSimpleName(),
                    result.getClass().getSimpleName()));
        }
        return (O) result;
    }


    /**
     * Find the correspondig value type for a given submodel element type.
     *
     * @param elementType submodel element type
     * @return corresponding value type
     * @throws IllegalArgumentException if submodelElement is null
     * @throws IllegalArgumentException is no corresppnding value type can be found
     */
    public static Class<? extends ElementValue> getValueClass(Class<? extends SubmodelElement> elementType) {
        init();
        Ensure.requireNonNull(elementType, "elementType must be non-null");
        if (!mappers.containsKey(ReflectionHelper.getAasInterface(elementType))) {
            throw new IllegalArgumentException("no mapper defined for elementType type " + elementType.getSimpleName());
        }
        return (Class<? extends ElementValue>) TypeToken.of(mappers.get(ReflectionHelper.getAasInterface(elementType)).getClass())
                .resolveType(DataValueMapper.class.getTypeParameters()[1])
                .getRawType();
    }


    /**
     * Find the correspondig submodel element type for a given value type.
     *
     * @param valueType value type
     * @return corresponding submodel element type
     * @throws IllegalArgumentException if valueType is null
     * @throws IllegalArgumentException is no corresppnding value type can be found
     */
    public static Class<? extends SubmodelElement> getElementClass(Class<? extends ElementValue> valueType) {
        init();
        Ensure.requireNonNull(valueType, "valueType must be non-null");
        Optional<?> result = mappers.values().stream()
                .map(x -> TypeToken.of(x.getClass()))
                .filter(x -> x.resolveType(DataValueMapper.class.getTypeParameters()[1]).getRawType().isAssignableFrom(valueType))
                .map(x -> (Class<? extends SubmodelElement>) x.resolveType(DataValueMapper.class.getTypeParameters()[0]).getRawType())
                .sorted(new MostSpecificClassComparator())
                .findFirst();
        if (!result.isPresent()) {
            throw new IllegalArgumentException("no element class defined for value type  " + valueType.getSimpleName());
        }
        return (Class<? extends SubmodelElement>) result.get();
    }


    /**
     * Sets the value of a submodel element to value provided as value representation.
     *
     * @param submodelElement for which the values will be set
     * @param elementValue which contains the values for the SubmodelElement
     * @param <T> type of the input/output SubmodelElement
     * @return the SubmodelElement instance with the ElementValue values set
     * @throws de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException if setting the value fails
     * @throws IllegalArgumentException if submodelElement is null
     * @throws IllegalArgumentException is no mapper for type of submodelElement
     */
    public static <T extends SubmodelElement> T setValue(T submodelElement, ElementValue elementValue) throws ValueMappingException {
        init();
        Ensure.requireNonNull(submodelElement, "submodelElement must be non-null");
        Ensure.requireNonNull(elementValue, "elementValue must be non-null");
        if (!mappers.containsKey(ReflectionHelper.getAasInterface(submodelElement.getClass()))) {
            throw new IllegalArgumentException("no mapper defined for submodelElement type " + submodelElement.getClass().getSimpleName());
        }
        return (T) mappers.get(ReflectionHelper.getAasInterface(submodelElement.getClass())).setValue(submodelElement, elementValue);
    }

}
