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
package de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values;

import java.util.stream.Stream;


// TODO implement all data types
public enum Datatype {
    String("string", StringValue.class),
    Boolean("boolean", BooleanValue.class),
    Decimal("decimal", DecimalValue.class),
    Integer("integer", IntegerValue.class),
    Double("double", DoubleValue.class),
    Float("float", FloatValue.class),
    //    Date,
    //    Time,
    //    DateTime,
    //    DateTimeStamp,
    //    gYear,
    //    gMonth,
    //    gYearMonth,
    //    gMonthDay,
    //    Duration,
    //    YearMonthDuration,
    //    DayTimeDuration,
    Byte("byte", ByteValue.class),
    Short("short", ShortValue.class),
    Int("int", IntValue.class),
    Long("long", LongValue.class),
    //    UnsignedByte,
    //    UnsignedShort,
    //    UnsignedInt,
    //    UnsignedLong,
    //    PositiveInteger,
    //    NonNegativeInteger,
    //    NegativeInteger,
    //    NonPositiveInteger,
    //    HexBinary,
    //    Base64Binary,
    //    AnyURI,
    //    LangString
    ;

    public static final Datatype DEFAULT = Datatype.String;

    public static Datatype fromName(String name) {
        return Stream.of(Datatype.values())
                .filter(x -> x.getName().equals(name))
                .findAny()
                .orElse(Datatype.String);
    }

    private final Class<? extends TypedValue> implementation;
    private final String name;

    private Datatype(String name, Class<? extends TypedValue> implementation) {
        this.name = name;
        this.implementation = implementation;
    }


    public String getName() {
        return name;
    }


    protected Class<? extends TypedValue> getImplementation() {
        return implementation;
    }

}
