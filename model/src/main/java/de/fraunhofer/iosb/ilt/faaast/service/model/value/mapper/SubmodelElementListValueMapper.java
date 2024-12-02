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

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.submodeltemplate.Cardinality;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementListValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.SubmodelTemplateHelper;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Converts between {@link org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection} and
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementCollectionValue}.
 */
public class SubmodelElementListValueMapper implements DataValueMapper<SubmodelElementList, SubmodelElementListValue> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmodelElementListValueMapper.class);

    @Override
    public SubmodelElementListValue toValue(SubmodelElementList submodelElement) throws ValueMappingException {
        if (submodelElement == null) {
            return null;
        }
        SubmodelElementListValue value = SubmodelElementListValue.builder().build();
        if (Objects.nonNull(submodelElement.getValue())) {
            value.setValues(submodelElement.getValue().stream()
                    .filter(Objects::nonNull)
                    .filter(ElementValueHelper::isValueOnlySupported)
                    .map(LambdaExceptionHelper.rethrowFunction(ElementValueMapper::toValue))
                    .map(ElementValue.class::cast)
                    .collect(Collectors.toList()));
        }
        return value;
    }


    @Override
    public SubmodelElementList setValue(SubmodelElementList submodelElement, SubmodelElementListValue value) throws ValueMappingException {
        DataValueMapper.super.setValue(submodelElement, value);
        int elementSize = Objects.nonNull(submodelElement.getValue())
                ? submodelElement.getValue().size()
                : 0;
        int valueSize = Objects.nonNull(value.getValues())
                ? value.getValues().size()
                : 0;
        if (valueSize == 0) {
            if (Objects.nonNull(submodelElement.getValue())) {
                submodelElement.getValue().clear();
            }
            return submodelElement;
        }
        if (elementSize == 0) {
            throw new ValueMappingException(
                    "unable to set value for empty SubmodelElementList - SubmodelElementList should contain required number of elements or a single element with cardinality > 1");
        }
        Cardinality cardinality = SubmodelTemplateHelper.getCardinality(submodelElement.getValue().get(0));
        if (cardinality != Cardinality.UNDEFINED) {
            if (elementSize > 1) {
                throw new ValueMappingException(String.format(
                        "when using cardinality constraints inside SubmodelElementList, the list may only contain a single element (found: %d)",
                        elementSize));
            }
            if (cardinality.getAllowsMultipleValues()) {
                submodelElement.setValue(Stream.generate(() -> {
                    SubmodelElement temp = DeepCopyHelper.deepCopy(submodelElement.getValue().get(0));
                    SubmodelTemplateHelper.removeCardinalities(temp);
                    return temp;
                })
                        .limit(valueSize)
                        .collect(Collectors.toList()));
                elementSize = valueSize;
            }
        }
        if (elementSize < valueSize) {
            throw new ValueMappingException(String.format(
                    "unable to set value for SubmodelElementList - list must contain required number of elements or a single element with cardinality > 1 (required: %d, actual: %d)",
                    valueSize,
                    elementSize));
        }
        for (int i = 0; i < Math.min(elementSize, valueSize); i++) {
            ElementValueMapper.setValue(submodelElement.getValue().get(i), value.getValues().get(i));
        }
        submodelElement.setValue(submodelElement.getValue().subList(elementSize - valueSize, elementSize));
        return submodelElement;
    }
}
