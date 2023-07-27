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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.csv.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.ExternalSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Timespan;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.SegmentProviderException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.csv.AbstractCSVExternalSegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.csv.TimeSeriesTestData;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.Blob;
import io.adminshell.aas.v3.model.File;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultBlob;
import io.adminshell.aas.v3.model.impl.DefaultFile;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.Before;
import org.junit.Test;


public class CSVExternalSegmentProviderTest {

    private CSVExternalSegmentProviderConfig config;
    private ExternalSegment fileSegment;
    private ExternalSegment blobSegment;

    private final File dataFile = new DefaultFile.Builder()
            .value("src/test/resources/testCSV.csv")
            .mimeType("text/csv")
            .semanticId(ReferenceHelper.globalReference(Constants.FILE_SEMANTIC_ID))
            .idShort("Data")
            .build();

    //TODO move somewhere nicer?
    byte[] base64Blob = "dGltZSxmb28sYmFyLGV4dHJhCjIwMjItMDItMDFUMDA6MDA6MDBaLDAsMC4xLHRoaXMKMjAyMi0wMi0wMVQwMTowMDowMFosMSwwLjIsZG9lcwoyMDIyLTAyLTAxVDAyOjAwOjAwWiwyLDAuMSxub3QKMjAyMi0wMi0wMVQwMzowMDowMFosMywwLjMsZXhpc3QKMjAyMi0wMi0wMVQwNDowMDowMFosNCwwLjEsaW4KMjAyMi0wMi0wMlQwMTowMDowMFosNSwwLjQsdGhlCjIwMjItMDItMDJUMDI6MDA6MDBaLDYsMC4xLHNlZ21lbnRzCjIwMjItMDItMDJUMDM6MDA6MDBaLDcsMC41LG1ldGFkYXRhCjIwMjItMDItMDNUMDE6MDA6MDBaLDgsMC44LGlnbm9yZQoyMDIyLTAyLTAzVDAyOjAwOjAwWiw5LDAuOSxpdA=="
            .getBytes();
    private final Blob dataBlob = new DefaultBlob.Builder()
            .value(base64Blob)
            .mimeType("text/csv")
            .semanticId(ReferenceHelper.globalReference(Constants.FILE_SEMANTIC_ID))
            .idShort("BlobData")
            .build();

    @Before
    public void setUp() {
        fileSegment = ExternalSegment.builder().data(dataFile).start(ZonedDateTime.parse("2022-02-01T00:00:00Z"))
                .end(ZonedDateTime.parse("2022-02-03T02:00:00Z")).build();
        blobSegment = ExternalSegment.builder().data(dataBlob).start(ZonedDateTime.parse("2022-02-01T00:00:00Z"))
                .end(ZonedDateTime.parse("2022-02-03T02:00:00Z")).build();

        config = CSVExternalSegmentProviderConfig.builder().timeColumn("time").build();
    }


    @Test
    public void testExternalSegmentWithFile() throws SegmentProviderException, ConfigurationException {
        testRecords(fileSegment);
    }


    @Test
    public void testExternalSegmentWithBlob() throws SegmentProviderException, ConfigurationException {
        testRecords(blobSegment);
    }


    @Test
    public void testWithoutTimespanFile() throws SegmentProviderException, ConfigurationException {
        AbstractCSVExternalSegmentProvider<?> provider = (CSVExternalSegmentProvider) config.newInstance(CoreConfig.DEFAULT, mock(ServiceContext.class));
        // fetch all records
        assertEqualsIgnoringIdShort(
                TimeSeriesTestData.RECORDS,
                provider.getRecords(TimeSeriesTestData.METADATA, fileSegment));
        assertEqualsIgnoringIdShort(
                TimeSeriesTestData.RECORDS,
                provider.getRecords(TimeSeriesTestData.METADATA, blobSegment));

    }


    @Test
    public void testWithBaseDirInConfig() throws ConfigurationException, SegmentProviderException {
        this.config.setBaseDir("src/test/resources");
        File dataFileShort = new DefaultFile.Builder()
                .value("testCSV.csv")
                .mimeType("text/csv")
                .semanticId(ReferenceHelper.globalReference(Constants.FILE_SEMANTIC_ID))
                .idShort("Data")
                .build();

        ExternalSegment segment = ExternalSegment.builder().data(dataFileShort).start(ZonedDateTime.parse("2022-02-01T00:00:00Z"))
                .end(ZonedDateTime.parse("2022-02-03T02:00:00Z")).build();

        AbstractCSVExternalSegmentProvider<?> provider = (CSVExternalSegmentProvider) config.newInstance(CoreConfig.DEFAULT, mock(ServiceContext.class));

        assertEqualsIgnoringIdShort(
                TimeSeriesTestData.RECORDS,
                provider.getRecords(TimeSeriesTestData.METADATA, segment,
                        Timespan.EMPTY));
    }


    @Test
    public void testWithWrongMIMEType() throws ConfigurationException, SegmentProviderException {
        File dataFileShort = new DefaultFile.Builder()
                .value("src/test/resources/testCSV.csv")
                .mimeType("application/json")
                .semanticId(ReferenceHelper.globalReference(Constants.FILE_SEMANTIC_ID))
                .idShort("Data")
                .build();

        ExternalSegment segment = ExternalSegment.builder().data(dataFileShort).start(ZonedDateTime.parse("2022-02-01T00:00:00Z"))
                .end(ZonedDateTime.parse("2022-02-03T02:00:00Z")).build();

        AbstractCSVExternalSegmentProvider<?> provider = (CSVExternalSegmentProvider) config.newInstance(CoreConfig.DEFAULT, mock(ServiceContext.class));

        SegmentProviderException exc = assertThrows(SegmentProviderException.class, () -> provider.getRecords(TimeSeriesTestData.METADATA, segment, Timespan.EMPTY));
        assertEquals("Error reading from File (file: src/test/resources/testCSV.csv, expected type: text/csv, actual type: application/json)", exc.getMessage());
    }


    private void testRecords(ExternalSegment segmentUnderTest) throws SegmentProviderException, ConfigurationException {
        AbstractCSVExternalSegmentProvider<?> provider = (CSVExternalSegmentProvider) config.newInstance(CoreConfig.DEFAULT, mock(ServiceContext.class));
        // fetch all records
        assertEqualsIgnoringIdShort(
                TimeSeriesTestData.RECORDS,
                provider.getRecords(TimeSeriesTestData.METADATA, segmentUnderTest,
                        Timespan.EMPTY));
        // fetch exactly all records
        assertEqualsIgnoringIdShort(
                TimeSeriesTestData.RECORDS,
                provider.getRecords(TimeSeriesTestData.METADATA, segmentUnderTest, Timespan.of(
                        TimeSeriesTestData.RECORD_00.getTime(),
                        TimeSeriesTestData.RECORD_09.getTime())));
        // fetch nothing
        assertEqualsIgnoringIdShort(
                List.of(),
                provider.getRecords(TimeSeriesTestData.METADATA, segmentUnderTest, Timespan.of(
                        TimeSeriesTestData.RECORD_00.getTime().minusHours(1),
                        TimeSeriesTestData.RECORD_00.getTime().minusMinutes(1))));
        // fetch partially
        assertEqualsIgnoringIdShort(
                List.of(TimeSeriesTestData.RECORD_03, TimeSeriesTestData.RECORD_04),
                provider.getRecords(TimeSeriesTestData.METADATA, segmentUnderTest, Timespan.of(
                        TimeSeriesTestData.RECORD_03.getTime(),
                        TimeSeriesTestData.RECORD_04.getTime())));

    }


    private static void assertEqualsIgnoringIdShort(List<Record> expected, List<Record> actual) {
        List<SubmodelElement> expectedCopy = DeepCopyHelper.deepCopy(expected, SubmodelElement.class);
        expectedCopy.stream().forEach(x -> x.setIdShort(null));
        List<SubmodelElement> actualCopy = DeepCopyHelper.deepCopy(actual, SubmodelElement.class);
        actualCopy.stream().forEach(x -> x.setIdShort(null));
        assertEquals(expectedCopy, actualCopy);
    }

}
