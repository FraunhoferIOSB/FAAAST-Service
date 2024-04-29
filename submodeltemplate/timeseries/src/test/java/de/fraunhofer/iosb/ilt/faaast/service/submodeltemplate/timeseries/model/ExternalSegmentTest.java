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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultBlob;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultFile;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.junit.Before;
import org.junit.Test;


public class ExternalSegmentTest extends BaseModelTest {

    private File testFile;
    private Blob testBlob;
    private ExternalSegment testSegment;

    @Test
    public void testFromFile()
            throws ValueFormatException, DeserializationException, IOException, SerializationException, org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException {

        TimeSeries actualTS = TimeSeries.of(new JsonDeserializer()
                .read(new String(
                        getClass().getClassLoader().getResourceAsStream("model-timeseries-externalsegment.json").readAllBytes(),
                        StandardCharsets.UTF_8),
                        Submodel.class));

        ExternalSegment actual = ExternalSegment.of(actualTS.getSegments().get(0));

        File testFile = new DefaultFile.Builder()
                .value("./testCSVFile.csv")
                .contentType("text/csv")
                .semanticId(ReferenceBuilder.global(Constants.FILE_SEMANTIC_ID))
                .idShort("Data")
                .build();

        ExternalSegment expected = ExternalSegment.builder()
                .idShort(actual.getIdShort())
                .semanticId(actual.getSemanticId())
                .data(testFile)
                .build();

        assertEquals(expected, actual);
    }


    @Before
    public void setupTestObjects() {
        this.testFile = new DefaultFile.Builder()
                .value("./testFile.csv")
                .contentType("text/csv")
                .semanticId(ReferenceBuilder.global(Constants.FILE_SEMANTIC_ID))
                .build();;
        this.testBlob = new DefaultBlob.Builder()
                .value("thisIsATest".getBytes(StandardCharsets.UTF_8))
                .contentType("text")
                .semanticId(ReferenceBuilder.global(Constants.BLOB_SEMANTIC_ID))
                .build();

        this.testSegment = new ExternalSegment();
        this.testSegment.setSemanticId(ReferenceBuilder.global(Constants.EXTERNAL_SEGMENT_SEMANTIC_ID));
    }


    @Test
    public void testConversionRoundTrip() {
        ExternalSegment expected = testSegment;
        expected.setData(testBlob);

        ExternalSegment actual = ExternalSegment.of(expected);
        assertAASEquals(expected, actual);
        assertEquals(expected, actual);
    }


    @Test
    public void testWithAdditionalProperties() {
        ExternalSegment expected = ExternalSegment.builder()
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
                .semanticId(ReferenceBuilder.global(Constants.EXTERNAL_SEGMENT_SEMANTIC_ID))
                .data(testFile)
                .build();
        ExternalSegment actual = ExternalSegment.of(expected);
        assertEquals(expected, actual);
    }


    @Test
    public void testParseWithAdditionalElement() throws ValueFormatException {
        SubmodelElementCollection expected = new DefaultSubmodelElementCollection.Builder()
                .semanticId(ReferenceBuilder.global(Constants.EXTERNAL_SEGMENT_SEMANTIC_ID))
                .value(testFile)
                .value(ADDITIONAL_ELEMENT)
                .build();
        ExternalSegment actual = ExternalSegment.of(expected);
        assertAASEquals(expected, actual);
    }


    @Test
    public void testAddAdditionalElement() throws ValueFormatException {
        ExternalSegment expected = ExternalSegment.builder()
                .value(ADDITIONAL_ELEMENT)
                .build();
        ExternalSegment actual = ExternalSegment.of(expected);
        assertAASHasElements(actual, ADDITIONAL_ELEMENT);
        assertEquals(expected, actual);
    }


    @Test
    public void testSetGetDataFile() throws ClassCastException {
        testSegment.setData(testBlob);
        assertEquals(testBlob, (Blob) testSegment.getData());
        assertEquals(testBlob.getIdShort(), testSegment.getData().getIdShort());

        testSegment.setData(testFile);
        assertEquals(testFile, (File) testSegment.getData());
        assertEquals(testFile.getIdShort(), testSegment.getData().getIdShort());
    }


    @Test
    public void testBuilderWithFile() {
        ExternalSegment builderTestSegmentFile = ExternalSegment.builder().data(testFile).build();

        assertEquals(testFile, builderTestSegmentFile.getData());
        assertNotNull(builderTestSegmentFile.getData());
    }


    @Test
    public void testBuilderWithBlob() {
        ExternalSegment builderTestSegmentBlob = ExternalSegment.builder().data(testBlob).build();

        assertEquals(testBlob, builderTestSegmentBlob.getData());
        assertNotNull(builderTestSegmentBlob.getData());
    }


    @Test
    public void testBuilderWithDataBlob() throws ClassCastException {
        testSegment.setData(testBlob);
        ExternalSegment builderTestSegmentDataB = ExternalSegment.builder().data(testBlob).build();

        assertEquals(testBlob, builderTestSegmentDataB.getData());
        assertNotNull(builderTestSegmentDataB);

        Blob blobby = (Blob) builderTestSegmentDataB.getData();
        assertNotNull(blobby);
    }


    @Test
    public void testSwitchingToFileFromBlob() throws ClassCastException {
        testSegment.setData(testFile);
        testSegment.setData(testBlob);

        assertEquals(testBlob, testSegment.getData());

        testSegment.setData(testFile);
        assertEquals(testFile, testSegment.getData());
    }

}
