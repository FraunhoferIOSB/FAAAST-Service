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

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Value;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.cast.CastToBoolean;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.cast.CastToDateTime;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.cast.CastToHex;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.cast.CastToNumber;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.cast.CastToString;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.cast.CastToTime;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.temporal.DayOfMonthOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.temporal.DayOfWeekOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.temporal.MonthOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.temporal.YearOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.AbstractParser;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.BooleanValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DateTimeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DoubleValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.HexBinaryValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.IntValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.StringValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TimeValue;

import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;


public class ValueToOperandParser extends AbstractParser<Value, Operand> {

    private final AttributeItemToAttributeParser attributeParser = new AttributeItemToAttributeParser();
    private final StringToFieldIdentifierParser stringToFieldIdentifierParser = new StringToFieldIdentifierParser();

    @Override
    public Operand parse(Value value) {
        if (value.get$attribute() != null) {
            return attributeParser.parse(value.get$attribute());
        }
        if (value.get$field() != null) {
            return stringToFieldIdentifierParser.parse(value.get$field());
        }
        if (value.get$strVal() != null) {
            return new StringValue(value.get$strVal());
        }
        if (value.get$numVal() != null) {
            Double d = value.get$numVal();
            return d.intValue() == d ? new IntValue(d.intValue()) : new DoubleValue(d);
        }
        if (value.get$hexVal() != null) {
            try {
                TypedValue<byte[]> hex = new HexBinaryValue();
                hex.fromString(value.get$hexVal());
                return hex;
            }
            catch (ValueFormatException valueFormatException) {
                throw new IllegalArgumentException(String.format("Cannot parse %s as hex", value.get$hexVal()), valueFormatException);
            }
        }
        if (value.get$dateTimeVal() != null) {
            return asDateTimeValue(value.get$dateTimeVal());
        }
        if (value.get$timeVal() != null) {
            try {
                TypedValue<OffsetTime> time = new TimeValue();
                time.fromString(value.get$timeVal());
                return time;
            }
            catch (ValueFormatException valueFormatException) {
                throw new IllegalArgumentException(String.format("Cannot parse %s as time", value.get$hexVal()), valueFormatException);
            }
        }
        if (value.get$boolean() != null) {
            return new BooleanValue(value.get$boolean());
        }
        if (value.get$strCast() != null) {
            return new CastToString(parse(value.get$strCast()));
        }
        if (value.get$numCast() != null) {
            return new CastToNumber(parse(value.get$numCast()));
        }
        if (value.get$hexCast() != null) {
            return new CastToHex(parse(value.get$hexCast()));
        }
        if (value.get$boolCast() != null) {
            return new CastToBoolean(parse(value.get$boolCast()));
        }
        if (value.get$dateTimeCast() != null) {
            return new CastToDateTime(parse(value.get$dateTimeCast()));
        }
        if (value.get$timeCast() != null) {
            return new CastToTime(parse(value.get$timeCast()));
        }
        if (value.get$dayOfWeek() != null) {
            return new DayOfWeekOperation(asDateTimeValue(value.get$dayOfWeek()));
        }
        if (value.get$dayOfMonth() != null) {
            return new DayOfMonthOperation(asDateTimeValue(value.get$dayOfMonth()));
        }
        if (value.get$month() != null) {
            return new MonthOperation(asDateTimeValue(value.get$month()));
        }
        if (value.get$year() != null) {
            return new YearOperation(asDateTimeValue(value.get$year()));
        }
        throw new IllegalArgumentException(String.format("Unsupported value: %s", value));
    }


    private DateTimeValue asDateTimeValue(Date from) {
        return new DateTimeValue(from.toInstant().atOffset(ZoneOffset.UTC));
    }


    private static ZonedDateTime toZonedDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault());
    }

}
