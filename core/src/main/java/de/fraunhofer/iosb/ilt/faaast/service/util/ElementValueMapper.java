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

import com.google.common.reflect.TypeToken;
import de.fraunhofer.iosb.ilt.faaast.service.model.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.MostSpecificClassComparator;
import de.fraunhofer.iosb.ilt.faaast.service.util.datavaluemapper.DataValueMapper;
import io.adminshell.aas.v3.dataformat.core.ReflectionHelper;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Finds available DataValue mappers and forward the request to the right class
 */
public class ElementValueMapper {

    private static Logger logger = LoggerFactory.getLogger(ElementValueMapper.class);
    private static Map<Class<? extends SubmodelElement>, ? extends DataValueMapper> mappers;

    private static void init() {
        if (mappers == null) {
            ScanResult scanResult = new ClassGraph()
                    .enableAllInfo()
                    .acceptPackages(ElementValueMapper.class.getPackageName())
                    .scan();

            mappers = scanResult.getSubclasses(DataValueMapper.class).loadClasses().stream()
                    .map(x -> (Class<? extends DataValueMapper>) x)
                    .collect(Collectors.toMap(
                            x -> (Class<? extends SubmodelElement>) TypeToken.of(x).resolveType(DataValueMapper.class.getTypeParameters()[0]).getRawType(),
                            x -> {
                                try {
                                    Constructor<? extends DataValueMapper> constructor = x.getConstructor();
                                    return constructor.newInstance();
                                }
                                catch (NoSuchMethodException | SecurityException ex) {
                                    logger.warn("element-value mapper implementation could not be loaded, "
                                            + "reason: missing constructor (implementation class: {})",
                                            x.getName());
                                }
                                catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                    logger.warn("element-value mapper implementation  could not be loaded, "
                                            + "reason: calling constructor failed (implementation class: {}",
                                            x.getName());
                                }
                                return null;
                            }));
        }
    }


    /**
     * Wraps the values of the SubmodelElement into a belonging ElementValue
     * instance
     *
     * @param submodelElement for which a ElementValue should be created
     * @param <I> type of the input SubmodelElement
     * @param <O> type of the output ElementValue
     * @return a DataElementValue for the given SubmodelElement
     */
    public static <I extends SubmodelElement, O extends ElementValue> O toValue(SubmodelElement submodelElement) {
        init();
        if (submodelElement == null) {
            throw new IllegalArgumentException("submodelElement must be non-null");
        }
        if (!mappers.containsKey(ReflectionHelper.getAasInterface(submodelElement.getClass()))) {
            throw new RuntimeException("no mapper defined for submodelElement type " + submodelElement.getClass().getSimpleName());
        }
        return (O) mappers.get(ReflectionHelper.getAasInterface(submodelElement.getClass())).toValue(submodelElement);
    }


    /**
     * Set the values of the ElementValue to the SubmodelElement Utility
     * function to determine equivalent ElementValue class for given
     * SubmodelElement class
     *
     * @param elementType SubmodelElement type
     * @return matching ElementValue class
     */
    public static Class<? extends ElementValue> getValueClass(Class<? extends SubmodelElement> elementType) {
        init();
        if (elementType == null) {
            throw new IllegalArgumentException("elementType must be non-null");
        }
        if (!mappers.containsKey(ReflectionHelper.getAasInterface(elementType))) {
            throw new RuntimeException("no mapper defined for elementType type " + elementType.getSimpleName());
        }
        return (Class<? extends ElementValue>) TypeToken.of(mappers.get(ReflectionHelper.getAasInterface(elementType)).getClass())
                .resolveType(DataValueMapper.class.getTypeParameters()[1])
                .getRawType();
    }


    public static Class<? extends SubmodelElement> getElementClass(Class<? extends ElementValue> valueType) {
        init();
        if (valueType == null) {
            throw new IllegalArgumentException("valueType must be non-null");
        }
        Class<?> aasValueType = ReflectionHelper.getAasInterface(valueType);
        Optional<?> result = mappers.values().stream()
                .map(x -> TypeToken.of(x.getClass()))
                .filter(x -> x.resolveType(DataValueMapper.class.getTypeParameters()[1]).getRawType().isAssignableFrom(aasValueType))
                .map(x -> (Class<? extends SubmodelElement>) x.resolveType(DataValueMapper.class.getTypeParameters()[0]).getRawType())
                .sorted(new MostSpecificClassComparator())
                .findFirst();

        if (!result.isPresent()) {
            throw new RuntimeException("no element class defined for value type  " + valueType.getSimpleName());
        }
        return (Class<? extends SubmodelElement>) result.get();
    }


    /**
     * Set the values of the DataElementValue to the SubmodelElement
     *
     * @param submodelElement for which the values will be set
     * @param elementValue which contains the values for the SubmodelElement
     * @param <I> type of the input/output SubmodelElement
     * @param <O> type of the input ElementValue
     * @return the SubmodelElement instance with the ElementValue values set
     */
    public static <I extends SubmodelElement, O extends ElementValue> I setValue(SubmodelElement submodelElement, ElementValue elementValue) {
        init();
        if (submodelElement == null) {
            throw new IllegalArgumentException("submodelElement must be non-null");
        }
        if (!mappers.containsKey(ReflectionHelper.getAasInterface(submodelElement.getClass()))) {
            throw new RuntimeException("no mapper defined for submodelElement type " + submodelElement.getClass().getSimpleName());
        }
        return (I) mappers.get(ReflectionHelper.getAasInterface(submodelElement.getClass())).setValue(submodelElement, elementValue);
    }

}
