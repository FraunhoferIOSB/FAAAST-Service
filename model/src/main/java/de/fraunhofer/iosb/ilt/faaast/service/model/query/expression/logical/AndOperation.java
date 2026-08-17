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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;

import java.util.List;


/**
 * The AAS Query Language {@code $and} operator, connecting two or more logical expressions through a logical AND.
 */
public class AndOperation extends AbstractLogicalOperation {
    /**
     * Creates a new AND operation.
     *
     * @param operands the logical expressions to combine
     */
    public AndOperation(List<LogicalExpression> operands) {
        super(operands);
    }


    @Override
    protected boolean neutralElement() {
        return true;
    }


    @Override
    protected AbstractLogicalOperation withOperands(List<LogicalExpression> operands) {
        return new AndOperation(operands);
    }

}
