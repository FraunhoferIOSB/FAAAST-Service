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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.Attribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.path.FieldPath;

import java.util.Objects;


public abstract class FieldIdentifierAttribute extends Attribute {
    protected final FieldPath fieldPath;

    public FieldIdentifierAttribute(FieldPath fieldPath) {
        this.fieldPath = fieldPath;
    }


    protected abstract String getScopeSyntax();


    @Override
    public Operand evaluatePartially(EvaluationContext evaluationContext) {
        return this;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        FieldIdentifierAttribute other = (FieldIdentifierAttribute) obj;
        return Objects.equals(fieldPath, other.fieldPath);
    }


    @Override
    public int hashCode() {
        return Objects.hash(getClass(), fieldPath);
    }


    @Override
    public String toString() {
        return getScopeSyntax() + "#" + fieldPath;
    }
}
