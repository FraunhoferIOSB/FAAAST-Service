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

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.TimeSeriesData;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.MissingInitialisationException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.TimeFactory;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.junit.Assert;
import org.junit.Test;


public class InternalSegmentTest extends BaseModelTest {

    private static final Property START_TIME = new DefaultProperty.Builder()
            .idShort(Constants.SEGMENT_START_TIME_ID_SHORT)
            .semanticId(ReferenceBuilder.global(Constants.TIME_UTC))
            .valueType(Datatype.DATE_TIME.getAas4jDatatype())
            .value(DateTimeFormatter.ISO_DATE_TIME.format(INTERNAL_SEGMENT_WITH_TIMES.getStart()))
            .build();

    private static final Property RECORD_COUNT = new DefaultProperty.Builder()
            .idShort(Constants.SEGMENT_RECORD_COUNT_ID_SHORT)
            .valueType(Datatype.LONG.getAas4jDatatype())
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
    public void testConversionRoundTripWithAutoCompleteProperties() throws ValueFormatException, MissingInitialisationException {
        InternalSegment expected = INTERNAL_SEGMENT_WITHOUT_TIMES;
        InternalSegment actual = InternalSegment.of(expected);
        actual.setCalculateProperties(true);
        assertAASNotEquals(expected, actual);
        Assert.assertNotEquals(expected, actual);
        Assert.assertNotEquals(null, actual.getStart());
        Assert.assertNotEquals(null, actual.getEnd());
        Assert.assertEquals(actual.getStart(), TimeFactory.getTimeFrom(TimeSeriesData.RECORD_00, expected.getStart(), expected.getStart(), null).getStart().get());
        Assert.assertEquals(actual.getEnd(), TimeFactory.getTimeFrom(TimeSeriesData.RECORD_09, expected.getStart(), expected.getStart(), null).getEnd().get());
        Assert.assertEquals(actual.getRecordCount().intValue(), TimeSeriesData.RECORDS.size());
    }


    @Test
    public void testWithAdditionalProperties() throws ValueFormatException {
        InternalSegment expected = InternalSegment.builder()
                .idShort("idShort")
                .category("category")
                .description(new DefaultLangStringTextType.Builder()
                        .language("en")
                        .text("foo")
                        .build())
                .description(new DefaultLangStringTextType.Builder()
                        .language("de")
                        .text("bar")
                        .build())
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
                .semanticId(ReferenceBuilder.global(Constants.INTERNAL_SEGMENT_SEMANTIC_ID))
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
