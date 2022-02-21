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

import de.fraunhofer.iosb.ilt.faaast.service.model.value.AnnotatedRelationshipElementValue;
import io.adminshell.aas.v3.model.AnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.stream.Collectors;


public class AnnotatedRelationshipElementValueMapper extends DataValueMapper<AnnotatedRelationshipElement, AnnotatedRelationshipElementValue> {

    @Override
    public AnnotatedRelationshipElementValue toValue(AnnotatedRelationshipElement submodelElement) {
        if (submodelElement == null) {
            return null;
        }
        AnnotatedRelationshipElementValue value = new AnnotatedRelationshipElementValue();
        value.setAnnotations(submodelElement.getAnnotations().stream().collect(Collectors.toMap(
                x -> x.getIdShort(),
                x -> ElementValueMapper.toValue(x))));
        value.setFirst(submodelElement.getFirst().getKeys());
        value.setSecond(submodelElement.getSecond().getKeys());
        return value;
    }


    @Override
    public AnnotatedRelationshipElement setValue(AnnotatedRelationshipElement submodelElement, AnnotatedRelationshipElementValue value) {
        if (submodelElement == null || value == null) {
            return null;
        }
        submodelElement.setFirst(new DefaultReference.Builder().keys(value.getFirst()).build());
        submodelElement.setSecond(new DefaultReference.Builder().keys(value.getSecond()).build());
        for (SubmodelElement element: submodelElement.getAnnotations()) {
            if (value.getAnnotations().containsKey(element.getIdShort())) {
                ElementValueMapper.setValue(element, value.getAnnotations().get(element.getIdShort()));
            }
        }
        return submodelElement;
    }
}
