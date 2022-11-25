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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model;

import static de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.TimeSeriesData.FIELD_1;
import static de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.TimeSeriesData.FIELD_2;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.TimeSeriesData;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.ModelingKind;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import java.time.ZonedDateTime;
import org.junit.Assert;
import org.junit.Test;


public class RecordTest {

    @Test
    public void testConversionRoundTrip() {
        Record expected = TimeSeriesData.RECORD_01;
        Record actual = Record.of(expected);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testWithAdditionalProperties() {
        Record expected = Record.builder()
                .idShort("idShort")
                .category("category")
                .description(new LangString("foo", "en"))
                .description(new LangString("bar", "de"))
                .kind(ModelingKind.INSTANCE)
                .time(ZonedDateTime.parse("2021-01-01T00:00:00Z"))
                .variable(FIELD_1, TypedValueFactory.createSafe(Datatype.INT, "0"))
                .variable(FIELD_2, TypedValueFactory.createSafe(Datatype.DOUBLE, "0.1"))
                .build();
        Record actual = Record.of(expected);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testWithChildren() {
        Record expected = Record.builder()
                .time(ZonedDateTime.parse("2021-01-01T00:00:00Z"))
                .variable(FIELD_1, TypedValueFactory.createSafe(Datatype.INT, "0"))
                .variable(FIELD_2, TypedValueFactory.createSafe(Datatype.DOUBLE, "0.1"))
                .value(new DefaultProperty.Builder()
                        .idShort("idShort")
                        .category("category")
                        .description(new LangString("foo", "en"))
                        .description(new LangString("bar", "de"))
                        .kind(ModelingKind.INSTANCE)
                        .build())
                .build();
        Record actual = Record.of(expected);
        Assert.assertEquals(expected, actual);
    }
}
