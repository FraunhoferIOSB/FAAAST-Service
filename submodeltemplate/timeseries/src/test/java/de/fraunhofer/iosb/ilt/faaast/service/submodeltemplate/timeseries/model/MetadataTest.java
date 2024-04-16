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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.TimeSeriesData;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.time.format.DateTimeFormatter;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.junit.Assert;
import org.junit.Test;


public class MetadataTest extends BaseModelTest {

    private static final SubmodelElementCollection METADATA_RECORD_EMPTY = new DefaultSubmodelElementCollection.Builder()
            .idShort(Constants.METADATA_RECORD_METADATA_ID_SHORT)
            .semanticId(ReferenceBuilder.global(Constants.RECORD_SEMANTIC_ID))
            .build();

    private static final SubmodelElementCollection METADATA_RECORD_FIELD1 = new DefaultSubmodelElementCollection.Builder()
            .idShort(Constants.METADATA_RECORD_METADATA_ID_SHORT)
            .semanticId(ReferenceBuilder.global(Constants.RECORD_SEMANTIC_ID))
            .value(new DefaultProperty.Builder()
                    .idShort(Constants.RECORD_TIME_ID_SHORT)
                    .valueType(Datatype.DATE_TIME.getAas4jDatatype())
                    .value("2021-01-01T00:00:00Z")
                    .semanticId(ReferenceBuilder.global(Constants.TIME_UTC))
                    .build())
            .value(new DefaultProperty.Builder()
                    .idShort(TimeSeriesData.FIELD_1)
                    .valueType(Datatype.INT.getAas4jDatatype())
                    .build())
            .build();

    private static final SubmodelElementCollection METADATA_RECORD_FIELD2 = new DefaultSubmodelElementCollection.Builder()
            .idShort(Constants.METADATA_RECORD_METADATA_ID_SHORT)
            .semanticId(ReferenceBuilder.global(Constants.RECORD_SEMANTIC_ID))
            .value(new DefaultProperty.Builder()
                    .idShort(Constants.RECORD_TIME_ID_SHORT)
                    .valueType(Datatype.DATE_TIME.getAas4jDatatype())
                    .value("2021-01-01T00:00:00Z")
                    .semanticId(ReferenceBuilder.global(Constants.TIME_UTC))
                    .build())
            .value(new DefaultProperty.Builder()
                    .idShort(TimeSeriesData.FIELD_2)
                    .valueType(Datatype.DOUBLE.getAas4jDatatype())
                    .build())
            .build();

    private static final SubmodelElementCollection METADATA_RECORD_FIELD1_FIELD2 = new DefaultSubmodelElementCollection.Builder()
            .idShort(Constants.METADATA_RECORD_METADATA_ID_SHORT)
            .semanticId(ReferenceBuilder.global(Constants.RECORD_SEMANTIC_ID))
            .value(new DefaultProperty.Builder()
                    .idShort(Constants.RECORD_TIME_ID_SHORT)
                    .valueType(Datatype.DATE_TIME.getAas4jDatatype())
                    .value("2021-01-01T00:00:00Z")
                    .semanticId(ReferenceBuilder.global(Constants.TIME_UTC))
                    .build())
            .value(new DefaultProperty.Builder()
                    .idShort(TimeSeriesData.FIELD_1)
                    .valueType(Datatype.INT.getAas4jDatatype())
                    .build())
            .value(new DefaultProperty.Builder()
                    .idShort(TimeSeriesData.FIELD_2)
                    .valueType(Datatype.DOUBLE.getAas4jDatatype())
                    .build())
            .build();

    private static Property field_01_property = new DefaultProperty.Builder()
            .idShort(TimeSeriesData.FIELD_1)
            .valueType(Datatype.INT.getAas4jDatatype())
            .build();

    private static Property field_02_property = new DefaultProperty.Builder()
            .idShort(TimeSeriesData.FIELD_2)
            .valueType(Datatype.DOUBLE.getAas4jDatatype())
            .build();

    @Test
    public void testConversionRoundTrip() throws ValueFormatException {
        Metadata expected = Metadata.builder()
                .recordMetadataTimeOrVariable(TimeSeriesData.FIELD_1, field_01_property)
                .recordMetadataTimeOrVariable(TimeSeriesData.FIELD_2, field_02_property)
                .recordMetadataTimeOrVariable(Constants.RECORD_TIME_ID_SHORT, TimeSeriesData.timeBuilder(TIME.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)))
                .build();
        Metadata actual = Metadata.of(expected);
        assertAASEquals(expected, actual);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testParseRecords() throws ValueFormatException, SerializationException {
        Metadata expected = Metadata.builder()
                .recordMetadataTimeOrVariable(TimeSeriesData.FIELD_1, field_01_property)
                .recordMetadataTimeOrVariable(TimeSeriesData.FIELD_2, field_02_property)
                .build();
        Metadata actual = Metadata.of(expected);
        assertAASEquals(expected, actual);
    }


    @Test
    public void testWithAdditionalProperties() throws ValueFormatException {
        Metadata expected = Metadata.builder()
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
                .recordMetadataTimeOrVariable(TimeSeriesData.FIELD_1, field_01_property)
                .recordMetadataTimeOrVariable(TimeSeriesData.FIELD_2, field_02_property)
                .build();
        Metadata actual = Metadata.of(expected);
        assertAASEquals(expected, actual);
    }


    @Test
    public void testAddAdditionalElement() throws ValueFormatException {
        Metadata expected = Metadata.builder()
                .value(ADDITIONAL_ELEMENT)
                .build();
        Metadata actual = Metadata.of(expected);
        assertAASHasElements(actual, ADDITIONAL_ELEMENT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testParseSMC() throws ValueFormatException {
        SubmodelElementCollection expected = new DefaultSubmodelElementCollection.Builder()
                .value(METADATA_RECORD_EMPTY)
                .value(ADDITIONAL_ELEMENT)
                .build();
        Metadata actual = Metadata.of(expected);
        assertAASEquals(expected, actual);
    }


    @Test
    public void testParseWithAdditionalElement() throws ValueFormatException {
        SubmodelElementCollection expected = new DefaultSubmodelElementCollection.Builder()
                .value(METADATA_RECORD_FIELD1_FIELD2)
                .value(ADDITIONAL_ELEMENT)
                .build();
        Metadata actual = Metadata.of(expected);
        assertAASEquals(expected, actual);
    }


    @Test
    public void testWithUpdatingElements() throws ValueFormatException {
        Metadata metadata = new Metadata();
        assertAASElements(metadata, METADATA_RECORD_EMPTY);

        metadata.getMetadataRecordVariables().put(Constants.RECORD_TIME_ID_SHORT, TimeSeriesData.timeBuilder(TIME.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)));

        metadata.getRecordMetadata().addVariables(TimeSeriesData.FIELD_1, field_01_property);
        assertAASElements(metadata, METADATA_RECORD_FIELD1);

        metadata.getRecordMetadata().getTimesAndVariables().put(TimeSeriesData.FIELD_2, field_02_property);
        assertAASElements(metadata, METADATA_RECORD_FIELD1_FIELD2);

        metadata.getRecordMetadata().getTimesAndVariables().remove(TimeSeriesData.FIELD_1);
        assertAASElements(metadata, METADATA_RECORD_FIELD2);

        metadata.getRecordMetadata().getTimesAndVariables().clear();
        assertAASElements(metadata, METADATA_RECORD_EMPTY);
    }
}
