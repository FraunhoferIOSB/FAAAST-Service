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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;

import java.util.function.BiFunction;


/**
 * The AAS Query Language {@code $ne} operator, comparing whether two operands are not identical.
 *
 * <p>This is an overloaded operator, i.e. it can deal with several input types (e.g. string and numeric values).
 */
public class NotEqualsOperation extends AbstractBinaryComparison {

    public NotEqualsOperation(Operand left, Operand right) {
        super(left, right);
    }


    @Override
    protected <T extends TypedValue<?>> BiFunction<T, T, Boolean> defaultOperation() {
        return (x, y) -> !x.equals(y);
    }


    @Override
    protected AbstractBinaryComparison withOperands(Operand left, Operand right) {
        return new NotEqualsOperation(left, right);
    }
}
