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
package de.fraunhofer.iosb.ilt.faaast.service.util.datavaluemapper;

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.AnnotatedRelationshipElementValue;
import io.adminshell.aas.v3.model.AnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.impl.DefaultReference;


public class AnnotatedRelationshipElementValueMapper extends DataValueMapper<AnnotatedRelationshipElement, AnnotatedRelationshipElementValue> {

    @Override
    public AnnotatedRelationshipElementValue toDataElementValue(AnnotatedRelationshipElement submodelElement) {
        if (submodelElement == null) {
            return null;
        }
        AnnotatedRelationshipElementValue annotatedRelationshipElementValue = new AnnotatedRelationshipElementValue();
        //TODO: Check type
        // annotatedRelationshipElementValue.setAnnotation(submodelElement.getAnnotations());
        annotatedRelationshipElementValue.setFirst(submodelElement.getFirst().getKeys());
        annotatedRelationshipElementValue.setSecond(submodelElement.getSecond().getKeys());
        return annotatedRelationshipElementValue;
    }


    @Override
    public AnnotatedRelationshipElement setDataElementValue(AnnotatedRelationshipElement submodelElement, AnnotatedRelationshipElementValue value) {
        if (submodelElement == null || value == null) {
            return null;
        }
        submodelElement.setFirst(new DefaultReference.Builder().keys(value.getFirst()).build());
        submodelElement.setSecond(new DefaultReference.Builder().keys(value.getSecond()).build());
        //TODO:
        //submodelElement.setAnnotations(value.getAnnotation());
        return submodelElement;
    }
}
