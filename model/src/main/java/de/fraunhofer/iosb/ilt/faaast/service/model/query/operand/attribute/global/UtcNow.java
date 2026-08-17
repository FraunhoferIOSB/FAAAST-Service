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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global;

import static java.time.ZoneOffset.UTC;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DateTimeValue;
import java.time.LocalDateTime;


public class UtcNow implements GlobalAttribute {

    @Override
    public DateTimeValue evaluatePartially(EvaluationContext evaluationContext) {
        return new DateTimeValue(LocalDateTime.now().atOffset(UTC));
    }


    @Override
    public boolean isUtcNow() {
        return true;
    }


    @Override
    public UtcNow asUtcNow() {
        return this;
    }


    @Override
    public boolean equals(Object o) {
        return o != null && getClass() == o.getClass();
    }


    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
