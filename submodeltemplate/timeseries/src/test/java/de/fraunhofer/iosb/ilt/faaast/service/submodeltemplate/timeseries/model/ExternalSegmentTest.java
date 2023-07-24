package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.File;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.ModelingKind;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.Blob;
import io.adminshell.aas.v3.model.impl.DefaultBlob;
import io.adminshell.aas.v3.model.impl.DefaultFile;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;


public class ExternalSegmentTest extends BaseModelTest {

    private File testFile;
    private Blob testBlob;
    private ExternalSegment testSegment;

    @Before
    public void setupTestObjects() {
        this.testFile = new DefaultFile();
        this.testBlob = new DefaultBlob();

        this.testSegment = new ExternalSegment();
        this.testSegment.setSemanticId(ReferenceHelper.globalReference(Constants.EXTERNAL_SEGMENT_SEMANTIC_ID));
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
                .description(new LangString("foo", "en"))
                .description(new LangString("bar", "de"))
                .kind(ModelingKind.INSTANCE)
                .semanticId(ReferenceHelper.globalReference(Constants.EXTERNAL_SEGMENT_SEMANTIC_ID))
                .data(testFile)
                .build();
        ExternalSegment actual = ExternalSegment.of(expected);
        assertEquals(expected, actual);
    }
    
    @Test
    public void testParseWithAdditionalElement() throws ValueFormatException {
        SubmodelElementCollection expected = new DefaultSubmodelElementCollection.Builder()
                .semanticId(ReferenceHelper.globalReference(Constants.EXTERNAL_SEGMENT_SEMANTIC_ID))
                .value(new DefaultFile())
                .value(ADDITIONAL_ELEMENT)
                .build();
        Record actual = Record.of(expected);
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
        ExternalSegment builderTestSegmentFile = ExternalSegment.builder().file(testFile).build();

        assertEquals(testFile, builderTestSegmentFile.getData());
        assertNotNull(builderTestSegmentFile.getData());
    }


    @Test
    public void testBuilderWithBlob() {
        ExternalSegment builderTestSegmentBlob = ExternalSegment.builder().blob(testBlob).build();

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
