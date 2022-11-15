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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.query.FluxTable;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.StringValue;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Data provider for linked segments referencing an InfluxDB datasource.
 */
public class InfluxLinkedSegmentProvider implements LinkedSegmentProvider<InfluxLinkedSegmentProviderConfig> {

    private InfluxLinkedSegmentProviderConfig config;

    @Override
    public InfluxLinkedSegmentProviderConfig asConfig() {
        return config;
    }


    @Override
    public List<Record> getRecords(Metadata metadata, LinkedSegment segment) {
        InfluxDBClient client;
        client = getInfluxDBClient(segment);

        assert client != null;
        List<FluxTable> tables = client.getQueryApi().query(segment.getQuery());
        List<Record> records = new ArrayList<>();
        tables.forEach(fluxTable -> fluxTable.getRecords()
                .forEach(fluxRecord -> {
                    //TODO: Which ZoneId? Add to config?
                    Record record = new Record(ZonedDateTime.ofInstant(fluxRecord.getTime(), ZoneId.systemDefault()));
                    record.getVariables().putIfAbsent(fluxRecord.getMeasurement(), new StringValue(fluxRecord.getValueByKey(config.getFieldKey()).toString()));
                    records.add(record);
                    record.setIdShort(UUID.randomUUID().toString());
                    System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
                }));
        client.close();
        return records;
    }


    private InfluxDBClient getInfluxDBClient(LinkedSegment segment) {

        if (config.getToken() != null && !config.getToken().equalsIgnoreCase("")) {
            //return  InfluxDBClientFactory.create(segment.getEndpoint(), config.getToken().toCharArray(), "IOSB", "test");
            return InfluxDBClientFactory.create(segment.getEndpoint(), config.getToken().toCharArray(), config.getOrg(), config.getBucket());
        }
        else if (config.getUsername() != null && !config.getUsername().equalsIgnoreCase("")
                && config.getPassword() != null && !config.getPassword().equalsIgnoreCase("")) {
            return InfluxDBClientFactory.create(segment.getEndpoint(), config.getUsername(), config.getPassword().toCharArray());
        }
        else {
            //ToDo throw ConfigException
        }
        return null;
    }


    @Override
    public void init(CoreConfig coreConfig, InfluxLinkedSegmentProviderConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
    }

}
