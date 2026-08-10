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

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.Literal;


public abstract class Cast<O extends Literal> implements Operand {

    private final Operand operand;

    protected Cast(Operand operand) {
        this.operand = operand;
    }


    @Override
    public Operand evaluatePartially(EvaluationContext evaluationContext) {
        Operand evaluated = operand.evaluatePartially(evaluationContext);
        if (evaluated.isLiteral()) {
            try {
                return cast(evaluated.asLiteral());
            }
            catch (Exception e) {
                // TODO error handling?
                throw new RuntimeException(e);
            }
        }
        return evaluated == operand ? this : withOperand(evaluated);
    }


    protected abstract Cast<O> withOperand(Operand evaluated);


    protected abstract O cast(Literal input) throws Exception;
}
