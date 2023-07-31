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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx.v2;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EndpointException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx.AbstractInfluxLinkedSegmentProviderTest;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx.AbstractInfluxLinkedSegmentProviderTest.InfluxInitializer;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx.InfluxServerConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx.util.ClientHelper;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;


public class InfluxV2LinkedSegmentProviderTest extends AbstractInfluxLinkedSegmentProviderTest implements InfluxInitializer {

    private static final String INFLUX_SERVER_VERSION = "2.5.1";

    @Override
    public void initialize(InfluxServerConfig serverConfig, String endpoint, String measurement, List<Record> records) {
        try (InfluxDBClient client = ClientHelper.createClient(
                endpoint,
                serverConfig.getBucket(),
                serverConfig.getOrganization(),
                serverConfig.getUsername(),
                serverConfig.getPassword(),
                serverConfig.getToken())) {
            client.getWriteApiBlocking().writePoints(records.stream().map(record -> Point
                    .measurement(measurement)
                    .time(record.getSingleTime().toEpochSecond(), WritePrecision.S)
                    .addFields(record.getVariables().entrySet().stream()
                            .collect(Collectors.toMap(
                                    x -> x.getKey(),
                                    x -> x.getValue().getValue()))))
                    .collect(Collectors.toList()));
        }
    }


    @Test
    public void testGetRecordsInfluxQLQueryWithToken() throws Exception {
        assertGetRecords(
                InfluxServerConfig.builder()
                        .authEnabled()
                        .bucket(BUCKET)
                        .organization(ORGANIZATION)
                        .token(TOKEN)
                        .version(INFLUX_SERVER_VERSION)
                        .build(),
                this,
                InfluxV2LinkedSegmentProviderConfig.builder()
                        .bucket(BUCKET)
                        .organization(ORGANIZATION)
                        .token(TOKEN)
                        .build(),
                MEASUREMENT,
                String.format("SELECT * FROM %s", MEASUREMENT));
    }


    @Test
    public void testGetRecordsWithToken() throws Exception {
        assertGetRecords(
                InfluxServerConfig.builder()
                        .authEnabled()
                        .bucket(BUCKET)
                        .organization(ORGANIZATION)
                        .token(TOKEN)
                        .version(INFLUX_SERVER_VERSION)
                        .build(),
                this,
                InfluxV2LinkedSegmentProviderConfig.builder()
                        .bucket(BUCKET)
                        .organization(ORGANIZATION)
                        .token(TOKEN)
                        .build(),
                MEASUREMENT,
                String.format("from(bucket:\"%s\") "
                        + "|> range(start: 0) "
                        + "|> filter(fn: (r) => r._measurement == \"%s\")",
                        BUCKET,
                        MEASUREMENT));
    }


    @Test
    public void testGetRecordsWithUsernamePassword() throws ConfigurationException, AssetConnectionException, MessageBusException, EndpointException, Exception {
        assertGetRecords(
                InfluxServerConfig.builder()
                        .authEnabled()
                        .bucket(BUCKET)
                        .organization(ORGANIZATION)
                        .username(USERNAME)
                        .password(PASSWORD)
                        .version(INFLUX_SERVER_VERSION)
                        .build(),
                this,
                InfluxV2LinkedSegmentProviderConfig.builder()
                        .bucket(BUCKET)
                        .organization(ORGANIZATION)
                        .username(USERNAME)
                        .password(PASSWORD)
                        .build(),
                MEASUREMENT,
                String.format("from(bucket:\"%s\") "
                        + "|> range(start: 0) "
                        + "|> filter(fn: (r) => r._measurement == \"%s\")",
                        BUCKET,
                        MEASUREMENT));
    }
}
