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

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DoubleValue;


/**
 * The AAS Query Language {@code num(...)} cast operator, converting an operand to a numeric value (xs:integer).
 */
public class CastToNumber extends Cast<DoubleValue> {

    public CastToNumber(Operand operand) {
        super(operand);
    }


    @Override
    protected Cast<DoubleValue> withOperand(Operand evaluated) {
        return new CastToNumber(evaluated);
    }


    @Override
    protected DoubleValue instance() {
        return new DoubleValue();
    }
}
