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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.BooleanValue;


/**
 * The AAS Query Language {@code bool(...)} cast operator, converting an operand to a boolean value (xs:boolean).
 */
public class CastToBoolean extends Cast<BooleanValue> {

    public CastToBoolean(Operand operand) {
        super(operand);
    }


    @Override
    protected Cast<BooleanValue> withOperand(Operand evaluated) {
        return new CastToBoolean(evaluated);
    }


    @Override
    protected BooleanValue instance() {
        return new BooleanValue();
    }
}
