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

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.TimeSeriesData;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.ModelingKind;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.time.ZonedDateTime;
import org.junit.Assert;
import org.junit.Test;


public class InternalSegmentTest extends BaseModelTest {

    private static final Property START_TIME = new DefaultProperty.Builder()
            .idShort(Constants.SEGMENT_START_TIME_ID_SHORT)
            .semanticId(ReferenceHelper.globalReference(Constants.TIME_UTC))
            .valueType(Datatype.DATE_TIME.getName())
            .value(INTERNAL_SEGMENT_WITH_TIMES.getStart().toString())
            .build();

    private static final Property RECORD_COUNT = new DefaultProperty.Builder()
            .idShort(Constants.SEGMENT_RECORD_COUNT_ID_SHORT)
            .valueType(Datatype.LONG.getName())
            .value("1")
            .build();

    private static final SubmodelElementCollection EMPTY_RECORDS = new DefaultSubmodelElementCollection.Builder()
            .idShort(Constants.INTERNAL_SEGMENT_RECORDS_ID_SHORT)
            .build();

    private static final SubmodelElementCollection RECORDS = new DefaultSubmodelElementCollection.Builder()
            .idShort(Constants.INTERNAL_SEGMENT_RECORDS_ID_SHORT)
            .value(TimeSeriesData.RECORD_00)
            .build();

    @Test
    public void testConversionRoundTrip() throws ValueFormatException {
        InternalSegment expected = INTERNAL_SEGMENT_WITH_TIMES;
        InternalSegment actual = InternalSegment.of(expected);
        assertAASEquals(expected, actual);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testConversionRoundTripWithoutAutoCompleteProperties() throws ValueFormatException {
        InternalSegment expected = INTERNAL_SEGMENT_WITHOUT_TIMES;
        InternalSegment actual = InternalSegment.of(expected);
        assertAASEquals(expected, actual);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testConversionRoundTripWithAutoCompleteProperties() throws ValueFormatException {
        InternalSegment expected = INTERNAL_SEGMENT_WITHOUT_TIMES;
        InternalSegment actual = InternalSegment.of(expected);
        actual.setCalculatePropertiesIfNotPresent(true);
        assertAASNotEquals(expected, actual);
        Assert.assertNotEquals(expected, actual);
    }


    @Test
    public void testWithAdditionalProperties() throws ValueFormatException {
        InternalSegment expected = InternalSegment.builder()
                .idShort("idShort")
                .category("category")
                .description(new LangString("foo", "en"))
                .description(new LangString("bar", "de"))
                .kind(ModelingKind.INSTANCE)
                .start(INTERNAL_SEGMENT_WITH_TIMES.getStart())
                .end(INTERNAL_SEGMENT_WITH_TIMES.getEnd())
                .record(TimeSeriesData.RECORD_00)
                .record(TimeSeriesData.RECORD_01)
                .build();
        InternalSegment actual = InternalSegment.of(expected);
        assertAASEquals(expected, actual);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testParseWithAdditionalElement() throws ValueFormatException {
        SubmodelElementCollection expected = new DefaultSubmodelElementCollection.Builder()
                .semanticId(ReferenceHelper.globalReference(Constants.INTERNAL_SEGMENT_SEMANTIC_ID))
                .value(new DefaultSubmodelElementCollection.Builder()
                        .idShort(Constants.INTERNAL_SEGMENT_RECORDS_ID_SHORT)
                        .value(TimeSeriesData.RECORD_00)
                        .build())
                .value(ADDITIONAL_ELEMENT)
                .build();
        InternalSegment actual = InternalSegment.of(expected);
        assertAASEquals(expected, actual);
    }


    @Test
    public void testAddAdditionalElement() throws ValueFormatException {
        InternalSegment expected = InternalSegment.builder()
                .value(ADDITIONAL_ELEMENT)
                .build();
        InternalSegment actual = InternalSegment.of(expected);
        assertAASHasElements(actual, ADDITIONAL_ELEMENT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testWithUpdatingElements() throws ValueFormatException {
        InternalSegment segment = new InternalSegment();
        assertAASElements(segment, EMPTY_RECORDS);

        segment.setStart(ZonedDateTime.parse(START_TIME.getValue()));
        assertAASElements(segment, EMPTY_RECORDS, START_TIME);

        segment.setRecordCount(Long.parseLong(RECORD_COUNT.getValue()));
        assertAASElements(segment, EMPTY_RECORDS, START_TIME, RECORD_COUNT);

        segment.getRecords().add(TimeSeriesData.RECORD_00);
        assertAASElements(segment, START_TIME, RECORD_COUNT, RECORDS);

        segment.setStart(null);
        assertAASElements(segment, RECORD_COUNT, RECORDS);

        segment.getRecords().remove(0);
        assertAASElements(segment, EMPTY_RECORDS, RECORD_COUNT);
    }
}
