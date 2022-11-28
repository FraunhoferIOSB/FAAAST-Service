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

import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.LinkedSegmentProviderConfig;
import io.apisense.embed.influx.InfluxServer;
import io.apisense.embed.influx.ServerAlreadyRunningException;
import io.apisense.embed.influx.ServerNotRunningException;
import io.apisense.embed.influx.configuration.InfluxConfigurationWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.junit.AfterClass;


public class InfluxV1LinkedSegmentProviderTest extends AbstractInfluxLinkedSegmentProviderTest {

    private static InfluxServer SERVER;

    public static Point toInfluxPoint(Record record, String measurement) {
        return Point
                .measurement(measurement)
                .time(record.getTime().toEpochSecond(), TimeUnit.SECONDS)
                .addField(TimeSeriesData.FIELD_1, (int) record.getVariables().get(TimeSeriesData.FIELD_1).getValue())
                .addField(TimeSeriesData.FIELD_2, (double) record.getVariables().get(TimeSeriesData.FIELD_2).getValue())
                .build();
    }


    @Override
    protected LinkedSegmentProviderConfig<?> getProviderConfig() {
        return InfluxV1LinkedSegmentProviderConfig.builder()
                .endpoint(endpoint)
                .database(DATABASE)
                .build();
    }


    @Override
    protected String getQuery() {
        return "SELECT * FROM " + MEASUREMENT;
    }


    @Override
    protected void initInflux(List<Record> records) {
        InfluxDB influxDB = InfluxDBFactory.connect(endpoint);
        influxDB.query(new Query("DROP DATABASE " + DATABASE));
        influxDB.query(new Query("CREATE DATABASE " + DATABASE));
        influxDB.setDatabase(DATABASE);
        String retentionPolicy = "forever";
        influxDB.query(new Query("CREATE RETENTION POLICY " + retentionPolicy + " ON " + DATABASE + " DURATION INF REPLICATION 1 DEFAULT"));
        influxDB.setRetentionPolicy(retentionPolicy);
        TimeSeriesData.RECORDS.forEach(x -> influxDB.write(toInfluxPoint(x, MEASUREMENT)));
    }


    @Override
    protected void startInflux(int port) throws IOException, ServerAlreadyRunningException {
        SERVER = new InfluxServer.Builder()
                .setInfluxConfiguration(new InfluxConfigurationWriter.Builder()
                        .setHttp(port)
                        .setBackupAndRestorePort(findFreePort())
                        .build())
                .build();
        SERVER.start();
    }


    @AfterClass
    public static void stopInflux() {
        if (SERVER != null) {
            try {
                SERVER.stop();
                SERVER.cleanup();
            }
            catch (ServerNotRunningException ex) {
                // ignore
            }
        }
    }

}
