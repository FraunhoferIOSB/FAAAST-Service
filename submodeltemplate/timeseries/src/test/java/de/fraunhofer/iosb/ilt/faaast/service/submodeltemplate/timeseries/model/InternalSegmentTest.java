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

import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.TimeSeriesData;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.ModelingKind;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import java.time.ZonedDateTime;
import org.junit.Assert;
import org.junit.Test;


public class InternalSegmentTest {

    public static final InternalSegment INTERNAL_SEGMENT = InternalSegment.builder()
            .start(ZonedDateTime.parse("2022-01-01T00:00:00Z"))
            .end(ZonedDateTime.parse("2022-01-04T00:00:00Z"))
            .records(TimeSeriesData.RECORDS)
            .build();

    public static final InternalSegment INTERNAL_SEGMENT_WITHOUT_TIMES = InternalSegment.builder()
            .records(TimeSeriesData.RECORDS)
            .build();

    @Test
    public void testConversionRoundTrip() {
        InternalSegment expected = INTERNAL_SEGMENT;
        InternalSegment actual = InternalSegment.of(expected);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testConversionRoundTrip_WithoutAutoCompleteProperties() {
        InternalSegment expected = INTERNAL_SEGMENT_WITHOUT_TIMES;
        expected.setCalculatePropertiesIfNotPresent(false);
        InternalSegment actual = InternalSegment.of(expected);
        expected.setCalculatePropertiesIfNotPresent(true);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testConversionRoundTrip_WithAutoCompleteProperties() {
        InternalSegment expected = INTERNAL_SEGMENT_WITHOUT_TIMES;
        InternalSegment actual = InternalSegment.of(expected);
        Assert.assertNotEquals(expected, actual);
    }


    @Test
    public void testWithAdditionalProperties() {
        InternalSegment expected = InternalSegment.builder()
                .idShort("idShort")
                .category("category")
                .description(new LangString("foo", "en"))
                .description(new LangString("bar", "de"))
                .kind(ModelingKind.INSTANCE)
                .start(ZonedDateTime.parse("2022-01-01T00:00:00Z"))
                .end(ZonedDateTime.parse("2022-01-02T00:00:00Z"))
                .record(TimeSeriesData.RECORD_00)
                .record(TimeSeriesData.RECORD_01)
                .build();
        InternalSegment actual = InternalSegment.of(expected);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testWithChildren() {
        InternalSegment expected = InternalSegment.builder()
                .value(new DefaultProperty.Builder()
                        .idShort("idShort")
                        .category("category")
                        .description(new LangString("foo", "en"))
                        .description(new LangString("bar", "de"))
                        .kind(ModelingKind.INSTANCE)
                        .build())
                .start(ZonedDateTime.parse("2022-01-01T00:00:00Z"))
                .end(ZonedDateTime.parse("2022-01-02T00:00:00Z"))
                .record(TimeSeriesData.RECORD_00)
                .record(TimeSeriesData.RECORD_01)
                .build();
        InternalSegment actual = InternalSegment.of(expected);
        Assert.assertEquals(expected, actual);
    }
}
