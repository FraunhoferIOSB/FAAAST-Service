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

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DateTimeValue;

import java.util.function.Function;


/**
 * The AAS Query Language {@code $dayOfWeek} operator, extracting the day of week from a date-time operand
 * ({@code <dateTimeToNum>}).
 */
public class DayOfWeekOperation extends TemporalOperation {

    public DayOfWeekOperation(Operand operand) {
        super(operand);
    }


    @Override
    protected Function<DateTimeValue, Integer> operation() {
        return dtv -> dtv.getValue().getDayOfWeek().getValue();
    }


    @Override
    protected DayOfWeekOperation withOperand(Operand operand) {
        return new DayOfWeekOperation(operand);
    }
}
