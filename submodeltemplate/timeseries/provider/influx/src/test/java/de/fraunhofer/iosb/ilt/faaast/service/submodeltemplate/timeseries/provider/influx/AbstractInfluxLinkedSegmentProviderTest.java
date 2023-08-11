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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.LinkedSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Timespan;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.util.List;
import java.util.Optional;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.InfluxDBContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;


public abstract class AbstractInfluxLinkedSegmentProviderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInfluxLinkedSegmentProviderTest.class);
    protected static final String ADMIN_USER = "testadmin";
    protected static final String ADMIN_PASSWORD = "testadminpassword";
    protected static final String TOKEN = "testtoken";
    protected static final String ORGANIZATION = "testorg";
    protected static final String BUCKET = "testbucket";
    protected static final String MEASUREMENT = "testmeasurement";
    protected static final String USERNAME = "testuser";
    protected static final String PASSWORD = "testpassword";
    protected static final String DATABASE = "testbucket";

    protected InfluxDBContainer<?> createInfluxContainer(InfluxServerConfig config) throws Exception {
        InfluxDBContainer<?> result = new InfluxDBContainer<>(DockerImageName.parse("influxdb:" + config.getVersion()))
                .withLogConsumer(new Slf4jLogConsumer(LOGGER))
                .withAuthEnabled(config.getAuthEnabled());
        if (config.getAdminUser() != null) {
            result.withAdmin(config.getAdminUser());
        }
        if (config.getAdminPassword() != null) {
            result.withAdminPassword(config.getAdminPassword());
        }
        if (config.getBucket() != null) {
            result.withBucket(config.getBucket());
        }
        if (config.getToken() != null) {
            result.withAdminToken(config.getToken());
        }
        if (config.getOrganization() != null) {
            result.withOrganization(config.getOrganization());
        }
        if (config.getUsername() != null) {
            result.withUsername(config.getUsername());
        }
        if (config.getPassword() != null) {
            result.withPassword(config.getPassword());
        }
        if (config.getDatabase() != null) {
            result.withDatabase(config.getDatabase());
        }
        return result;
    }


    @BeforeClass
    public static void ensureDockerAvailable() {
        Assume.assumeTrue("Docker is not available on this environment", DockerClientFactory.instance().isDockerAvailable());
    }

    public static interface InfluxInitializer {

        public void initialize(InfluxServerConfig serverConfig, String endpoint, String measurement, List<Record> records);
    }

    public void assertGetRecords(
                                 InfluxServerConfig serverConfig,
                                 InfluxInitializer initializer,
                                 AbstractInfluxLinkedSegmentProviderConfig<?> providerConfig,
                                 String measurement,
                                 String query)
            throws Exception {
        InfluxDBContainer<?> server = createInfluxContainer(serverConfig);
        server.start();
        String endpoint = "http://localhost:" + server.getMappedPort(8086);
        initializer.initialize(serverConfig, endpoint, measurement, TimeSeriesData.RECORDS);
        LinkedSegment linkedSegment = LinkedSegment.builder()
                .semanticId(ReferenceHelper.globalReference(Constants.LINKED_SEGMENT_SEMANTIC_ID))
                .endpoint(endpoint)
                .query(query)
                .build();
        providerConfig.setEndpoint(endpoint);
        AbstractInfluxLinkedSegmentProvider<?> provider = (AbstractInfluxLinkedSegmentProvider<?>) providerConfig.newInstance(CoreConfig.DEFAULT, mock(ServiceContext.class));
        try {
            // fetch all records
            assertEqualsIgnoringIdShort(
                    TimeSeriesData.RECORDS,
                    provider.getRecords(TimeSeriesData.METADATA, linkedSegment,
                            Timespan.EMPTY));
            // fetch exactly all records
            assertEqualsIgnoringIdShort(
                    TimeSeriesData.RECORDS,
                    provider.getRecords(TimeSeriesData.METADATA, linkedSegment, Timespan.of(
                            TimeSeriesData.RECORD_00.getSingleTime().getStartAsZonedDateTime(Optional.empty()),
                            TimeSeriesData.RECORD_09.getSingleTime().getEndAsZonedDateTime(Optional.empty()))));
            // fetch nothing
            assertEqualsIgnoringIdShort(
                    List.of(),
                    provider.getRecords(TimeSeriesData.METADATA, linkedSegment, Timespan.of(
                            TimeSeriesData.RECORD_00.getSingleTime().getStartAsZonedDateTime(Optional.empty()).minusHours(1),
                            TimeSeriesData.RECORD_00.getSingleTime().getStartAsZonedDateTime(Optional.empty()).minusMinutes(1))));
            // fetch partially
            assertEqualsIgnoringIdShort(
                    List.of(TimeSeriesData.RECORD_03, TimeSeriesData.RECORD_04),
                    provider.getRecords(TimeSeriesData.METADATA, linkedSegment, Timespan.of(
                            TimeSeriesData.RECORD_03.getSingleTime().getStartAsZonedDateTime(Optional.empty()),
                            TimeSeriesData.RECORD_04.getSingleTime().getEndAsZonedDateTime(Optional.empty()))));
        }
        finally {
            server.stop();
        }
    }


    protected static void assertEqualsIgnoringIdShort(List<Record> expected, List<Record> actual) {
        List<SubmodelElement> expectedCopy = DeepCopyHelper.deepCopy(expected, SubmodelElement.class);
        expectedCopy.stream().forEach(x -> x.setIdShort(null));
        List<SubmodelElement> actualCopy = DeepCopyHelper.deepCopy(actual, SubmodelElement.class);
        actualCopy.stream().forEach(x -> x.setIdShort(null));
        assertEquals(expectedCopy, actualCopy);
    }

}
