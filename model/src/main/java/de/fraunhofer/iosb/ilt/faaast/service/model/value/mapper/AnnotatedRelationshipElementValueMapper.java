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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.model.AnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.Objects;
import java.util.stream.Collectors;


public class AnnotatedRelationshipElementValueMapper implements DataValueMapper<AnnotatedRelationshipElement, AnnotatedRelationshipElementValue> {

    @Override
    public AnnotatedRelationshipElementValue toValue(AnnotatedRelationshipElement submodelElement) throws ValueMappingException {
        if (submodelElement == null) {
            return null;
        }
        AnnotatedRelationshipElementValue value = new AnnotatedRelationshipElementValue();
        if (submodelElement.getAnnotations() != null && submodelElement.getAnnotations().stream().noneMatch(Objects::isNull)) {
            value.setAnnotations(submodelElement.getAnnotations().stream().collect(Collectors.toMap(
                    x -> x != null ? x.getIdShort() : null,
                    LambdaExceptionHelper.rethrowFunction(x -> x != null ? ElementValueMapper.toValue(x) : null))));
        }

        value.setFirst(submodelElement.getFirst() != null ? submodelElement.getFirst().getKeys() : null);
        value.setSecond(submodelElement.getSecond() != null ? submodelElement.getSecond().getKeys() : null);
        return value;
    }


    @Override
    public AnnotatedRelationshipElement setValue(AnnotatedRelationshipElement submodelElement, AnnotatedRelationshipElementValue value) {
        DataValueMapper.super.setValue(submodelElement, value);

        submodelElement.setFirst(value.getFirst() != null ? new DefaultReference.Builder().keys(value.getFirst()).build() : null);
        submodelElement.setSecond(value.getSecond() != null ? new DefaultReference.Builder().keys(value.getSecond()).build() : null);
        if (submodelElement.getAnnotations() != null) {
            for (SubmodelElement element: submodelElement.getAnnotations()) {
                if (element != null && value.getAnnotations().containsKey(element.getIdShort())) {
                    ElementValueMapper.setValue(element, value.getAnnotations().get(element.getIdShort()));
                }
            }
        }

        return submodelElement;
    }
}
