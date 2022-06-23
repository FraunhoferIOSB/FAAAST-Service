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

import de.fraunhofer.iosb.ilt.faaast.service.model.value.RelationshipElementValue;
import io.adminshell.aas.v3.model.RelationshipElement;
import io.adminshell.aas.v3.model.impl.DefaultReference;


public class RelationshipElementValueMapper implements DataValueMapper<RelationshipElement, RelationshipElementValue> {

    @Override
    public RelationshipElementValue toValue(RelationshipElement submodelElement) {
        if (submodelElement == null) {
            return null;
        }
        RelationshipElementValue relationshipElementValue = new RelationshipElementValue();
        relationshipElementValue.setFirst(submodelElement.getFirst() != null ? submodelElement.getFirst().getKeys() : null);
        relationshipElementValue.setSecond(submodelElement.getSecond() != null ? submodelElement.getSecond().getKeys() : null);
        return relationshipElementValue;
    }


    @Override
    public RelationshipElement setValue(RelationshipElement submodelElement, RelationshipElementValue value) {
        DataValueMapper.super.setValue(submodelElement, value);
        submodelElement.setFirst(value.getFirst() != null ? new DefaultReference.Builder().keys(value.getFirst()).build() : null);
        submodelElement.setSecond(value.getSecond() != null ? new DefaultReference.Builder().keys(value.getSecond()).build() : null);
        return submodelElement;
    }
}
