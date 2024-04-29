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

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.TimeSeriesData;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.junit.Assert;
import org.junit.Test;


public class TimeSeriesTest extends BaseModelTest {

    @Test
    public void testFromFile()
            throws ValueFormatException, IOException, SerializationException, DeserializationException {
        TimeSeries actual = TimeSeries.of(new JsonDeserializer()
                .read(new String(
                        getClass().getClassLoader().getResourceAsStream("model-timeseries-internalsegment.json").readAllBytes(),
                        StandardCharsets.UTF_8),
                        Submodel.class));
        TimeSeries expected = TimeSeries.builder()
                .id(actual.getId())
                .idShort(actual.getIdShort())
                .metadata(TimeSeriesData.METADATA)
                .segment(InternalSegment.builder()
                        .record(Record.builder()
                                .timeOrVariable("Time00", TimeSeriesData.timeBuilder("2022-01-01T00:00:00Z"))
                                .timeOrVariable(TimeSeriesData.FIELD_1, TimeSeriesData.field01Builder(0))
                                .timeOrVariable(TimeSeriesData.FIELD_2, TimeSeriesData.field02Builder(0.0))
                                .idShort("Record01")
                                .build())
                        .idShort("InternalSegment01")
                        .build())
                .build();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testConversionRoundTrip() throws ValueFormatException {
        TimeSeries expected = TimeSeries.builder()
                .id(IdHelper.randomId("TimeSeries"))
                .metadata(TimeSeriesData.METADATA)
                .segment(INTERNAL_SEGMENT_WITH_TIMES)
                .segment(INTERNAL_SEGMENT_WITHOUT_TIMES)
                .segment(LINKED_SEGMENT)
                .build();
        TimeSeries actual = TimeSeries.of(expected);
        assertAASEquals(expected, actual);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testWithAdditionalProperties() throws ValueFormatException {
        TimeSeries expected = TimeSeries.builder()
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
                .id(IdHelper.randomId("TimeSeries"))
                .metadata(TimeSeriesData.METADATA)
                .segment(INTERNAL_SEGMENT_WITH_TIMES)
                .segment(INTERNAL_SEGMENT_WITH_TIMES)
                .build();
        TimeSeries actual = TimeSeries.of(expected);
        assertAASEquals(expected, actual);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testAddAdditionalElement() throws ValueFormatException {
        TimeSeries expected = TimeSeries.builder()
                .submodelElements(ADDITIONAL_ELEMENT)
                .build();
        TimeSeries actual = TimeSeries.of(expected);
        assertAASHasElements(actual.getSubmodelElements(), ADDITIONAL_ELEMENT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testParseWithAdditionalElement() throws ValueFormatException {
        Submodel expected = new DefaultSubmodel.Builder()
                .idShort(Constants.TIMESERIES_SUBMODEL_ID_SHORT)
                .semanticId(ReferenceBuilder.global(Constants.TIMESERIES_SUBMODEL_SEMANTIC_ID))
                .submodelElements(new Metadata())
                .submodelElements(new DefaultSubmodelElementCollection.Builder()
                        .idShort(Constants.TIMESERIES_SEGMENTS_ID_SHORT)
                        .semanticId(ReferenceBuilder.global(Constants.SEGMENTS_SEMANTIC_ID))
                        .build())
                .submodelElements(ADDITIONAL_ELEMENT)
                .build();
        TimeSeries actual = TimeSeries.of(expected);
        assertAASEquals(expected, actual);
    }
}
