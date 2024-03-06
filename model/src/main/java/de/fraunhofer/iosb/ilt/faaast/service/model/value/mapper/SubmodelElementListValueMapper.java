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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementListValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


/**
 * Converts between {@link org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection} and
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementCollectionValue}.
 */
public class SubmodelElementListValueMapper implements DataValueMapper<SubmodelElementList, SubmodelElementListValue> {

    @Override
    public SubmodelElementListValue toValue(SubmodelElementList submodelElement) throws ValueMappingException {
        if (submodelElement == null) {
            return null;
        }
        SubmodelElementListValue value = SubmodelElementListValue.builder().build();
        if (submodelElement.getValue() != null && submodelElement.getValue().stream().noneMatch(Objects::isNull)) {
            value.setValues(submodelElement.getValue().stream()
                    .map(LambdaExceptionHelper.rethrowFunction(ElementValueMapper::toValue))
                    .map(ElementValue.class::cast)
                    .collect(Collectors.toList()));
        }
        else {
            value.setValues(null);
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
        if (elementSize < valueSize) {
            throw new ValueMappingException(String.format(
                    "Loss of information - setting a value with size %d to a SubmodelElementList of size %d results in loss of information",
                    valueSize,
                    elementSize));
        }

        for (int i = 0; i < Math.min(elementSize, valueSize); i++) {
            ElementValueMapper.setValue(submodelElement.getValue().get(i), value.getValues().get(i));
        }
        return submodelElement;
    }
}
