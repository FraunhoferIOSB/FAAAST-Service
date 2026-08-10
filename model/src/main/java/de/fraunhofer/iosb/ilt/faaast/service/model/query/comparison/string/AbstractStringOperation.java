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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.string;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.AbstractBinaryComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.Literal;

import java.util.List;


/**
 * $starts-with, $ends-with, $contains and $regex check, if the first given argument is part of the second argument or
 * if the first argument matches with the given REGEX.
 */
public abstract class AbstractStringOperation extends AbstractBinaryComparison {

    protected AbstractStringOperation(Operand left, Operand right) {
        super(left, right);
    }


    @Override
    protected void validate(List<? extends LogicalExpression> operands) throws IllegalArgumentException {
        super.validate(operands);
        if (!operands.stream().map(LogicalExpression::asLiteral).allMatch(Literal::isString)) {
            throw new IllegalArgumentException(String.format("operands to %s were not strings", this.getClass().getSimpleName()));
        }
    }
}
