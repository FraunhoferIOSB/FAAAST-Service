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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EndpointException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Timespan;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.LinkedSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.LinkedSegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.LinkedSegmentProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.apisense.embed.influx.InfluxServer;
import io.apisense.embed.influx.ServerAlreadyRunningException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public abstract class AbstractInfluxLinkedSegmentProviderTest {

    protected static InfluxServer server;
    protected static String endpoint;
    protected static final String DATABASE = "TestDatabase";
    protected static final String MEASUREMENT = "TestMeasurement";
    private boolean initialized = false;

    protected LinkedSegment linkedSegment = LinkedSegment.builder()
            .semanticId(ReferenceHelper.globalReference(Constants.LINKED_SEGMENT_SEMANTIC_ID))
            .build();

    protected static int findFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Assert.assertNotNull(serverSocket);
            Assert.assertTrue(serverSocket.getLocalPort() > 0);
            return serverSocket.getLocalPort();
        }
    }


    protected abstract void startInflux(int port) throws Exception;


    protected abstract void initInflux(List<Record> records);


    protected abstract String getQuery();


    protected String buildEndpointUrl(int port) {
        return "http://localhost:" + port;
    }


    @Before
    public void setup() throws IOException, ServerAlreadyRunningException, Exception {
        if (!initialized) {
            int port = findFreePort();
            endpoint = buildEndpointUrl(port);
            linkedSegment.setEndpoint(endpoint);
            linkedSegment.setQuery(getQuery());
            startInflux(port);
            initInflux(TimeSeriesData.RECORDS);
            initialized = true;
        }
    }


    protected abstract LinkedSegmentProviderConfig<?> getProviderConfig();


    @Test
    public void testGetRecords() throws ConfigurationException, AssetConnectionException, MessageBusException, EndpointException {
        LinkedSegmentProvider<?> provider = (LinkedSegmentProvider<?>) getProviderConfig().newInstance(CoreConfig.DEFAULT, mock(ServiceContext.class));
        // fetch all records
        assertEqualsIgnoringIdShort(
                TimeSeriesData.RECORDS,
                provider.getRecords(TimeSeriesData.METADATA, linkedSegment,
                        Timespan.EMPTY));
        // fetch exactly all records
        assertEqualsIgnoringIdShort(
                TimeSeriesData.RECORDS,
                provider.getRecords(TimeSeriesData.METADATA, linkedSegment, Timespan.of(
                        TimeSeriesData.RECORD_00.getTime(),
                        TimeSeriesData.RECORD_09.getTime())));
        // fetch nothing
        assertEqualsIgnoringIdShort(
                List.of(),
                provider.getRecords(TimeSeriesData.METADATA, linkedSegment, Timespan.of(
                        TimeSeriesData.RECORD_00.getTime().minusHours(1),
                        TimeSeriesData.RECORD_00.getTime().minusMinutes(1))));
        // fetch partially
        assertEqualsIgnoringIdShort(
                List.of(TimeSeriesData.RECORD_03, TimeSeriesData.RECORD_04),
                provider.getRecords(TimeSeriesData.METADATA, linkedSegment, Timespan.of(
                        TimeSeriesData.RECORD_03.getTime(),
                        TimeSeriesData.RECORD_04.getTime())));
    }


    protected static void assertEqualsIgnoringIdShort(List<Record> expected, List<Record> actual) {
        List<SubmodelElement> expectedCopy = DeepCopyHelper.deepCopy(expected, SubmodelElement.class);
        expectedCopy.stream().forEach(x -> x.setIdShort(null));
        List<SubmodelElement> actualCopy = DeepCopyHelper.deepCopy(actual, SubmodelElement.class);
        actualCopy.stream().forEach(x -> x.setIdShort(null));
        assertEquals(expectedCopy, actualCopy);
    }

}
