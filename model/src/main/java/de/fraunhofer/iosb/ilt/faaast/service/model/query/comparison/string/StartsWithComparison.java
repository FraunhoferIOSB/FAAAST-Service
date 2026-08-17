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
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.StringValue;

import java.util.function.BiFunction;


/**
 * The AAS Query Language {@code $starts-with} operator, checking whether the left string operand starts with the right
 * string operand.
 */
public class StartsWithComparison extends AbstractStringComparison {

    public StartsWithComparison(Operand left, Operand right) {
        super(left, right);
    }


    @Override
    protected BiFunction<StringValue, StringValue, Boolean> stringOperation() {
        return (x, y) -> y.getValue().startsWith(x.getValue());
    }


    @Override
    protected AbstractBinaryComparison withOperands(Operand left, Operand right) {
        return new StartsWithComparison(left, right);
    }
}
