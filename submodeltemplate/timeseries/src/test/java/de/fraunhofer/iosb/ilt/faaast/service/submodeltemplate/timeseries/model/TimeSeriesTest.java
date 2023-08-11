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

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.TimeSeriesData;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.UtcTime;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.dataformat.SerializationException;
import io.adminshell.aas.v3.dataformat.json.JsonDeserializer;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.ModelingKind;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.Test;


public class TimeSeriesTest extends BaseModelTest {

    @Test
    public void testFromFile()
            throws ValueFormatException, DeserializationException, io.adminshell.aas.v3.dataformat.DeserializationException, IOException, SerializationException {
        TimeSeries actual = TimeSeries.of(new JsonDeserializer()
                .readReferable(
                        new String(
                                getClass().getClassLoader().getResourceAsStream("model-timeseries-internalsegment.json").readAllBytes(),
                                StandardCharsets.UTF_8),
                        Submodel.class));
        TimeSeries expected = TimeSeries.builder()
                .identification(actual.getIdentification())
                .idShort(actual.getIdShort())
                .metadata(TimeSeriesData.METADATA)
                .segment(InternalSegment.builder()
                        .record(Record.builder()
                                .times("Time00", new UtcTime("2022-01-01T00:00:00Z"))
                                .variable(TimeSeriesData.FIELD_1, TypedValueFactory.createSafe(Datatype.INT, "0"))
                                .variable(TimeSeriesData.FIELD_2, TypedValueFactory.createSafe(Datatype.DOUBLE, "0.0"))
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
                .identification(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.IRI)
                        .identifier(IdentifierHelper.randomId("TimeSeries"))
                        .build())
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
                .description(new LangString("foo", "en"))
                .description(new LangString("bar", "de"))
                .kind(ModelingKind.INSTANCE)
                .identification(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.IRI)
                        .identifier(IdentifierHelper.randomId("TimeSeries"))
                        .build())
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
                .submodelElement(ADDITIONAL_ELEMENT)
                .build();
        TimeSeries actual = TimeSeries.of(expected);
        assertAASHasElements(actual.getSubmodelElements(), ADDITIONAL_ELEMENT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testParseWithAdditionalElement() throws ValueFormatException {
        Submodel expected = new DefaultSubmodel.Builder()
                .idShort(Constants.TIMESERIES_SUBMODEL_ID_SHORT)
                .semanticId(ReferenceHelper.globalReference(Constants.TIMESERIES_SUBMODEL_SEMANTIC_ID))
                .submodelElement(new Metadata())
                .submodelElement(new DefaultSubmodelElementCollection.Builder()
                        .idShort(Constants.TIMESERIES_SEGMENTS_ID_SHORT)
                        .semanticId(ReferenceHelper.globalReference(Constants.SEGMENTS_SEMANTIC_ID))
                        .build())
                .submodelElement(ADDITIONAL_ELEMENT)
                .build();
        TimeSeries actual = TimeSeries.of(expected);
        assertAASEquals(expected, actual);
    }
}
