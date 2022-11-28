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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Metadata;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import java.time.ZonedDateTime;
import java.util.List;


public class TimeSeriesData {

    public static final String FIELD_1 = "foo";
    public static final String FIELD_2 = "bar";
    public static final List<String> FIELDS = List.of(FIELD_1, FIELD_2);

    public static final Metadata METADATA = Metadata.builder()
            .recordMetadata(FIELD_1, Datatype.INT)
            .recordMetadata(FIELD_2, Datatype.DOUBLE)
            .build();

    public static final Record RECORD_00 = Record.builder()
            .time(ZonedDateTime.parse("2022-01-01T00:00:00Z"))
            .variable(FIELD_1, TypedValueFactory.createSafe(Datatype.INT, "0"))
            .variable(FIELD_2, TypedValueFactory.createSafe(Datatype.DOUBLE, "0.0"))
            .build();

    public static final Record RECORD_01 = Record.builder()
            .time(ZonedDateTime.parse("2022-01-01T01:00:00Z"))
            .variable(FIELD_1, TypedValueFactory.createSafe(Datatype.INT, "1"))
            .variable(FIELD_2, TypedValueFactory.createSafe(Datatype.DOUBLE, "0.1"))
            .build();

    public static final Record RECORD_02 = Record.builder()
            .time(ZonedDateTime.parse("2022-01-01T02:00:00Z"))
            .variable(FIELD_1, TypedValueFactory.createSafe(Datatype.INT, "2"))
            .variable(FIELD_2, TypedValueFactory.createSafe(Datatype.DOUBLE, "0.2"))
            .build();

    public static final Record RECORD_03 = Record.builder()
            .time(ZonedDateTime.parse("2022-01-01T03:00:00Z"))
            .variable(FIELD_1, TypedValueFactory.createSafe(Datatype.INT, "3"))
            .variable(FIELD_2, TypedValueFactory.createSafe(Datatype.DOUBLE, "0.3"))
            .build();

    public static final Record RECORD_04 = Record.builder()
            .time(ZonedDateTime.parse("2022-01-01T04:00:00Z"))
            .variable(FIELD_1, TypedValueFactory.createSafe(Datatype.INT, "4"))
            .variable(FIELD_2, TypedValueFactory.createSafe(Datatype.DOUBLE, "0.4"))
            .build();

    public static final Record RECORD_05 = Record.builder()
            .time(ZonedDateTime.parse("2022-01-01T05:00:00Z"))
            .variable(FIELD_1, TypedValueFactory.createSafe(Datatype.INT, "5"))
            .variable(FIELD_2, TypedValueFactory.createSafe(Datatype.DOUBLE, "0.5"))
            .build();

    public static final Record RECORD_06 = Record.builder()
            .time(ZonedDateTime.parse("2022-01-02T06:00:00Z"))
            .variable(FIELD_1, TypedValueFactory.createSafe(Datatype.INT, "6"))
            .variable(FIELD_2, TypedValueFactory.createSafe(Datatype.DOUBLE, "0.6"))
            .build();

    public static final Record RECORD_07 = Record.builder()
            .time(ZonedDateTime.parse("2022-01-02T07:00:00Z"))
            .variable(FIELD_1, TypedValueFactory.createSafe(Datatype.INT, "7"))
            .variable(FIELD_2, TypedValueFactory.createSafe(Datatype.DOUBLE, "0.7"))
            .build();

    public static final Record RECORD_08 = Record.builder()
            .time(ZonedDateTime.parse("2022-01-03T08:00:00Z"))
            .variable(FIELD_1, TypedValueFactory.createSafe(Datatype.INT, "8"))
            .variable(FIELD_2, TypedValueFactory.createSafe(Datatype.DOUBLE, "0.8"))
            .build();

    public static final Record RECORD_09 = Record.builder()
            .time(ZonedDateTime.parse("2022-01-03T09:00:00Z"))
            .variable(FIELD_1, TypedValueFactory.createSafe(Datatype.INT, "9"))
            .variable(FIELD_2, TypedValueFactory.createSafe(Datatype.DOUBLE, "0.9"))
            .build();

    public static final List<Record> RECORDS = List.of(
            RECORD_00,
            RECORD_01,
            RECORD_02,
            RECORD_03,
            RECORD_04,
            RECORD_05,
            RECORD_06,
            RECORD_07,
            RECORD_08,
            RECORD_09);

}
