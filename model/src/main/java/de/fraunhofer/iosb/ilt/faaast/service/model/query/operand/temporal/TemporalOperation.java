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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.temporal;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DateTimeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.IntValue;

import java.util.function.Function;


/**
 * An operation that extracts a temporal component from a date-time operand.
 */
public abstract class TemporalOperation implements Operand {

    private final Operand operand;

    protected TemporalOperation(Operand operand) {
        this.operand = operand;
    }


    @Override
    public Operand evaluatePartially(EvaluationContext evaluationContext) {
        Operand evaluated = operand.evaluatePartially(evaluationContext);

        if (!evaluated.isTypedValue()) {
            return this;
        }

        return new IntValue(operation().apply(((DateTimeValue) operand.asTypedValue())));
    }


    /**
     * Returns the function that extracts the temporal component from a date-time value.
     *
     * @return the temporal extraction function
     */
    protected abstract Function<DateTimeValue, Integer> operation();


    /**
     * Returns a new temporal operation wrapping the given operand.
     *
     * @param operand the operand to wrap
     * @return the new temporal operation
     */
    protected abstract TemporalOperation withOperand(Operand operand);
}
