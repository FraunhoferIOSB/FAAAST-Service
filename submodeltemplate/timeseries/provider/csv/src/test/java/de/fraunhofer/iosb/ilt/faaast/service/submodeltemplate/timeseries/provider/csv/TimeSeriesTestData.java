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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.csv;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Metadata;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;


public class TimeSeriesTestData {
    public static final String FIELD_1 = "foo";
    public static final String FIELD_2 = "bar";
    public static final List<String> FIELDS = List.of(FIELD_1, FIELD_2);

    public static final Metadata METADATA = Metadata.builder()
            .recordMetadataTimeOrVariable("Time00", time00Builder("0000-01-01T00:00:00Z"))
            .recordMetadataTimeOrVariable("Time01", time01Builder("0000-01-01T00:00:00Z"))
            .recordMetadataTimeOrVariable(FIELD_1, field01Builder(0))
            .recordMetadataTimeOrVariable(FIELD_2, field02Builder(0.0))
            .build();

    public static final Record RECORD_00 = Record.builder()
            .timeOrVariable("Time00", time00Builder("2022-02-01T00:00:00Z"))
            .timeOrVariable("Time01", time01Builder("2022-02-01T01:00:00Z"))
            .timeOrVariable(FIELD_1, field01Builder(0))
            .timeOrVariable(FIELD_2, field02Builder(0.1))
            .build();

    public static final Record RECORD_01 = Record.builder()
            .timeOrVariable("Time00", time00Builder("2022-02-01T01:00:00Z"))
            .timeOrVariable("Time01", time01Builder("2022-02-01T02:00:00Z"))
            .timeOrVariable(FIELD_1, field01Builder(1))
            .timeOrVariable(FIELD_2, field02Builder(0.2))
            .build();

    public static final Record RECORD_02 = Record.builder()
            .timeOrVariable("Time00", time00Builder("2022-02-01T02:00:00Z"))
            .timeOrVariable("Time01", time01Builder("2022-02-01T03:00:00Z"))
            .timeOrVariable(FIELD_1, field01Builder(2))
            .timeOrVariable(FIELD_2, field02Builder(0.1))
            .build();

    public static final Record RECORD_03 = Record.builder()
            .timeOrVariable("Time00", time00Builder("2022-02-01T03:00:00Z"))
            .timeOrVariable("Time01", time01Builder("2022-02-01T04:00:00Z"))
            .timeOrVariable(FIELD_1, field01Builder(3))
            .timeOrVariable(FIELD_2, field02Builder(0.3))
            .build();

    public static final Record RECORD_04 = Record.builder()
            .timeOrVariable("Time00", time00Builder("2022-02-01T04:00:00Z"))
            .timeOrVariable("Time01", time01Builder("2022-02-01T05:00:00Z"))
            .timeOrVariable(FIELD_1, field01Builder(4))
            .timeOrVariable(FIELD_2, field02Builder(0.1))
            .build();

    public static final Record RECORD_05 = Record.builder()
            .timeOrVariable("Time00", time00Builder("2022-02-02T01:00:00Z"))
            .timeOrVariable("Time01", time01Builder("2022-02-02T02:00:00Z"))
            .timeOrVariable(FIELD_1, field01Builder(5))
            .timeOrVariable(FIELD_2, field02Builder(0.4))
            .build();

    public static final Record RECORD_06 = Record.builder()
            .timeOrVariable("Time00", time00Builder("2022-02-02T02:00:00Z"))
            .timeOrVariable("Time01", time01Builder("2022-02-02T03:00:00Z"))
            .timeOrVariable(FIELD_1, field01Builder(6))
            .timeOrVariable(FIELD_2, field02Builder(0.1))
            .build();

    public static final Record RECORD_07 = Record.builder()
            .timeOrVariable("Time00", time00Builder("2022-02-02T03:00:00Z"))
            .timeOrVariable("Time01", time01Builder("2022-02-02T04:00:00Z"))
            .timeOrVariable(FIELD_1, field01Builder(7))
            .timeOrVariable(FIELD_2, field02Builder(0.5))
            .build();

    public static final Record RECORD_08 = Record.builder()
            .timeOrVariable("Time00", time00Builder("2022-02-03T01:00:00Z"))
            .timeOrVariable("Time01", time01Builder("2022-02-03T02:00:00Z"))
            .timeOrVariable(FIELD_1, field01Builder(8))
            .timeOrVariable(FIELD_2, field02Builder(0.8))
            .build();

    public static final Record RECORD_09 = Record.builder()
            .timeOrVariable("Time00", time00Builder("2022-02-03T02:00:00Z"))
            .timeOrVariable("Time01", time01Builder("2022-02-03T03:00:00Z"))
            .timeOrVariable(FIELD_1, field01Builder(9))
            .timeOrVariable(FIELD_2, field02Builder(0.9))
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

    public static Property field01Builder(int value) {
        return new DefaultProperty.Builder()
                .idShort(FIELD_1)
                .value(Integer.toString(value))
                .valueType(Datatype.INT.getAas4jDatatype())
                .build();
    }


    public static Property field02Builder(double value) {
        return new DefaultProperty.Builder()
                .idShort(FIELD_2)
                .value(Double.toString(value))
                .valueType(Datatype.DOUBLE.getAas4jDatatype())
                .build();
    }


    public static Property time00Builder(String value) {
        return new DefaultProperty.Builder()
                .idShort("Time00")
                .value(value)
                .semanticId(ReferenceBuilder.global(Constants.TIME_UTC))
                .valueType(Datatype.DATE_TIME.getAas4jDatatype())
                .build();
    }


    public static Property time01Builder(String value) {
        return new DefaultProperty.Builder()
                .idShort("Time00")
                .value(value)
                .semanticId(ReferenceBuilder.global(Constants.TIME_UTC))
                .valueType(Datatype.DATE_TIME.getAas4jDatatype())
                .build();
    }
}
