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
package de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive;

import java.util.stream.Stream;


/**
 * Datatypes defined in AAS specification
 */
public enum Datatype {
    STRING("string", StringValue.class),
    BOOLEAN("boolean", BooleanValue.class),
    DECIMAL("decimal", DecimalValue.class),
    INTEGER("integer", IntegerValue.class),
    DOUBLE("double", DoubleValue.class),
    FLOAT("float", FloatValue.class),
    // TODO implement all data types
    //    Date,
    //    Time,
    DATE_TIME("datetime", DateTimeValue.class),
    //    DateTimeStamp,
    //    gYear,
    //    gMonth,
    //    gYearMonth,
    //    gMonthDay,
    //    Duration,
    //    YearMonthDuration,
    //    DayTimeDuration,
    BYTE("byte", ByteValue.class),
    SHORT("short", ShortValue.class),
    INT("int", IntValue.class),
    LONG("long", LongValue.class), //    UnsignedByte,
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

    public static final Datatype DEFAULT = Datatype.STRING;
    private final Class<? extends TypedValue> implementation;
    private final String name;

    /**
     * Finds datatype from string. Matching is case-sensitive. If no match is
     * found, {@link Datatype#DEFAULT} is returned.
     *
     * @param name name of datatype as defined in AAS specification
     * @return matching datatype if found, else {@link Datatype#DEFAULT}
     */
    public static Datatype fromName(String name) {
        return Stream.of(Datatype.values())
                .filter(x -> x.getName().equals(name))
                .findAny()
                .orElse(DEFAULT);
    }


    /**
     * Checks if a given string is a valid (i.e. supported) datatype.
     *
     * @param name name of the datatype
     * @return true is it is a valid datatype, otherwise false
     */
    public static boolean isValid(String name) {
        return Stream.of(Datatype.values())
                .anyMatch(x -> x.getName().equals(name));
    }


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
