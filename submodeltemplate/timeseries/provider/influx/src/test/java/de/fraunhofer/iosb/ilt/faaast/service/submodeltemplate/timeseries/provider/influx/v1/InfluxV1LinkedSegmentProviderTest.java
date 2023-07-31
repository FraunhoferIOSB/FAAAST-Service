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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx.v1;

import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx.AbstractInfluxLinkedSegmentProviderTest;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx.AbstractInfluxLinkedSegmentProviderTest.InfluxInitializer;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx.InfluxServerConfig;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.junit.Test;


public class InfluxV1LinkedSegmentProviderTest extends AbstractInfluxLinkedSegmentProviderTest implements InfluxInitializer {

    private static final String INFLUX_SERVER_VERSION = "1.7.6";

    @Override
    public void initialize(InfluxServerConfig serverConfig, String endpoint, String measurement, List<Record> records) {
        InfluxDB influxDB = InfluxDBFactory.connect(endpoint, serverConfig.getAdminUser(), serverConfig.getAdminPassword());
        influxDB.query(new Query(String.format("DROP DATABASE \"%s\"", serverConfig.getDatabase())));
        influxDB.query(new Query(String.format("CREATE DATABASE \"%s\"", serverConfig.getDatabase())));
        influxDB.setDatabase(serverConfig.getDatabase());
        String retentionPolicy = "forever";
        influxDB.query(new Query(String.format("CREATE RETENTION POLICY \"%s\" ON \"%s\" DURATION INF REPLICATION 1 DEFAULT",
                retentionPolicy,
                serverConfig.getDatabase())));
        influxDB.query(new Query(String.format("GRANT ALL PRIVILEGES TO \"%s\"", serverConfig.getUsername())));
        influxDB.setRetentionPolicy(retentionPolicy);
        influxDB.write(BatchPoints
                .database(serverConfig.getDatabase())
                .points(records.stream().map(record -> Point
                        .measurement(measurement)
                        .time(record.getSingleTime().toEpochSecond(), TimeUnit.SECONDS)
                        .fields(record.getVariables().entrySet().stream()
                                .collect(Collectors.toMap(
                                        x -> x.getKey(),
                                        x -> x.getValue().getValue())))
                        .build())
                        .collect(Collectors.toList()))
                .build());
    }


    @Test
    public void testGetRecords() throws Exception {
        assertGetRecords(
                InfluxServerConfig.builder()
                        .authEnabled()
                        .adminUser(ADMIN_USER)
                        .adminPassword(ADMIN_PASSWORD)
                        .bucket(BUCKET)
                        .database(DATABASE)
                        .organization(ORGANIZATION)
                        .username(USERNAME)
                        .password(PASSWORD)
                        .version(INFLUX_SERVER_VERSION)
                        .build(),
                this,
                InfluxV1LinkedSegmentProviderConfig.builder()
                        .database(DATABASE)
                        .username(USERNAME)
                        .password(PASSWORD)
                        .build(),
                MEASUREMENT,
                String.format("SELECT * FROM %s", MEASUREMENT));
    }

}
