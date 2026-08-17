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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.operand;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.cast.CastToString;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.AbstractParser;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.StringValue;

import java.util.Objects;


public class StringValueToOperandParser extends AbstractParser<de.fraunhofer.iosb.ilt.faaast.service.model.query.json.StringValue, Operand> {

    private final AttributeItemToAttributeParser attributeParser = new AttributeItemToAttributeParser();
    private final ValueToOperandParser valueToOperandParser = new ValueToOperandParser();

    @Override
    public Operand parse(de.fraunhofer.iosb.ilt.faaast.service.model.query.json.StringValue value) {
        Objects.requireNonNull(value, "value must not be null");
        if (value.get$attribute() != null) {
            return attributeParser.parse(value.get$attribute());
        }
        if (value.get$field() != null) {
            return stringToFieldIdentifierParser.parse(value.get$field());
        }
        if (value.get$strVal() != null) {
            return new StringValue(value.get$strVal());
        }
        if (value.get$strCast() != null) {
            return new CastToString(valueToOperandParser.parse(value.get$strCast()));
        }
        throw new IllegalArgumentException(String.format("Unsupported string value: %s", value));
    }
}
