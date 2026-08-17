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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.cast;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;


/**
 * A cast operation that converts an operand to a specific typed value.
 *
 * @param <O> the target typed value type
 */
public abstract class Cast<O extends TypedValue<?>> implements Operand {

    private final Operand operand;

    protected Cast(Operand operand) {
        this.operand = operand;
    }


    @Override
    public Operand evaluatePartially(EvaluationContext evaluationContext) {
        Operand evaluated = operand.evaluatePartially(evaluationContext);
        if (evaluated.isTypedValue()) {
            return cast(evaluated.asTypedValue());
        }
        return evaluated == operand ? this : withOperand(evaluated);
    }


    /**
     * Returns a new cast operation wrapping the given evaluated operand.
     *
     * @param evaluated the evaluated operand to wrap
     * @return the new cast operation
     */
    protected abstract Cast<O> withOperand(Operand evaluated);


    /**
     * Casts the given typed value to the target type.
     *
     * @param input the typed value to cast
     * @return the cast result
     */
    protected O cast(TypedValue<?> input) {
        O o = instance();
        try {
            o.fromString(input.asString());
        }
        catch (ValueFormatException e) {
            throw new IllegalStateException(String.format("Could not parse %s to %s", input, this.getClass().getSimpleName()), e);
        }
        return o;
    }


    /**
     * Returns a new instance of the target typed value.
     *
     * @return a new target typed value instance
     */
    protected abstract O instance();
}
