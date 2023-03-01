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
import io.adminshell.aas.v3.dataformat.SerializationException;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.ModelingKind;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import org.junit.Assert;
import org.junit.Test;


public class MetadataTest extends BaseModelTest {

    private static final SubmodelElementCollection METADATA_RECORD_EMPTY = new DefaultSubmodelElementCollection.Builder()
            .idShort(Constants.METADATA_RECORD_METADATA_ID_SHORT)
            .semanticId(ReferenceHelper.globalReference(Constants.RECORD_SEMANTIC_ID))
            .build();

    private static final SubmodelElementCollection METADATA_RECORD_FIELD1 = new DefaultSubmodelElementCollection.Builder()
            .idShort(Constants.METADATA_RECORD_METADATA_ID_SHORT)
            .semanticId(ReferenceHelper.globalReference(Constants.RECORD_SEMANTIC_ID))
            .value(new DefaultProperty.Builder()
                    .idShort(TimeSeriesData.FIELD_1)
                    .valueType(Datatype.INT.getName())
                    .build())
            .build();

    private static final SubmodelElementCollection METADATA_RECORD_FIELD2 = new DefaultSubmodelElementCollection.Builder()
            .idShort(Constants.METADATA_RECORD_METADATA_ID_SHORT)
            .semanticId(ReferenceHelper.globalReference(Constants.RECORD_SEMANTIC_ID))
            .value(new DefaultProperty.Builder()
                    .idShort(TimeSeriesData.FIELD_2)
                    .valueType(Datatype.DOUBLE.getName())
                    .build())
            .build();

    private static final SubmodelElementCollection METADATA_RECORD_FIELD1_FIELD2 = new DefaultSubmodelElementCollection.Builder()
            .idShort(Constants.METADATA_RECORD_METADATA_ID_SHORT)
            .semanticId(ReferenceHelper.globalReference(Constants.RECORD_SEMANTIC_ID))
            .value(new DefaultProperty.Builder()
                    .idShort(TimeSeriesData.FIELD_1)
                    .valueType(Datatype.INT.getName())
                    .build())
            .value(new DefaultProperty.Builder()
                    .idShort(TimeSeriesData.FIELD_2)
                    .valueType(Datatype.DOUBLE.getName())
                    .build())
            .build();

    @Test
    public void testConversionRoundTrip() throws ValueFormatException {
        Metadata expected = Metadata.builder()
                .recordMetadata(TimeSeriesData.FIELD_1, Datatype.INT)
                .recordMetadata(TimeSeriesData.FIELD_2, Datatype.DOUBLE)
                .build();
        Metadata actual = Metadata.of(expected);
        assertAASEquals(expected, actual);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testParseRecords() throws ValueFormatException, SerializationException {
        Metadata expected = Metadata.builder()
                .recordMetadata(TimeSeriesData.FIELD_1, Datatype.INT)
                .recordMetadata(TimeSeriesData.FIELD_2, Datatype.DOUBLE)
                .build();
        Metadata actual = Metadata.of(expected);
        assertAASEquals(expected, actual);
    }


    @Test
    public void testWithAdditionalProperties() throws ValueFormatException {
        Metadata expected = Metadata.builder()
                .idShort("idShort")
                .category("category")
                .description(new LangString("foo", "en"))
                .description(new LangString("bar", "de"))
                .kind(ModelingKind.INSTANCE)
                .recordMetadata(TimeSeriesData.FIELD_1, Datatype.INT)
                .recordMetadata(TimeSeriesData.FIELD_2, Datatype.DOUBLE)
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

        metadata.getRecordMetadata().put(TimeSeriesData.FIELD_1, Datatype.INT);
        assertAASElements(metadata, METADATA_RECORD_FIELD1);

        metadata.getRecordMetadata().put(TimeSeriesData.FIELD_2, Datatype.DOUBLE);
        assertAASElements(metadata, METADATA_RECORD_FIELD1_FIELD2);

        metadata.getRecordMetadata().remove(TimeSeriesData.FIELD_1);
        assertAASElements(metadata, METADATA_RECORD_FIELD2);

        metadata.getRecordMetadata().clear();
        assertAASElements(metadata, METADATA_RECORD_EMPTY);
    }
}
