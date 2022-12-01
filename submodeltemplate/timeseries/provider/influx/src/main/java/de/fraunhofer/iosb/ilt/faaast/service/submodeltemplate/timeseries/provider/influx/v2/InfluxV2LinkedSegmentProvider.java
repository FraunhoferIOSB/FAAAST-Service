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
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.InfluxQLQuery;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.influxdb.query.InfluxQLQueryResult;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.LinkedSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Metadata;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Timespan;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx.AbstractInfluxLinkedSegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx.util.ClientHelper;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Data provider for linked segments referencing an InfluxDB datasource.
 */
public class InfluxV2LinkedSegmentProvider extends AbstractInfluxLinkedSegmentProvider<InfluxV2LinkedSegmentProviderConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxV2LinkedSegmentProvider.class);
    private InfluxV2LinkedSegmentProviderConfig config;

    @Override
    public InfluxV2LinkedSegmentProviderConfig asConfig() {
        return config;
    }


    @Override
    public List<Record> getRecords(Metadata metadata, LinkedSegment segment, Timespan timespan) {
        return getRecords(metadata, withTimeFilter(segment.getQuery(), timespan));
    }


    @Override
    public List<Record> getRecords(Metadata metadata, LinkedSegment segment) {
        return getRecords(metadata, segment.getQuery());
    }


    @Override
    protected List<Record> getRecords(Metadata metadata, String query) {
        return isInfluxQL(query)
                ? getRecordsInfluxQL(metadata, query)
                : getRecordsFlux(metadata, query);
    }


    private List<Record> getRecordsInfluxQL(Metadata metadata, String query) {
        InfluxDBClient client = ClientHelper.createClient(
                config.getEndpoint(),
                config.getBucket(),
                config.getOrganization(),
                config.getUsername(),
                config.getPassword(),
                config.getToken());
        InfluxQLQueryResult queryResults = client.getInfluxQLQueryApi().query(new InfluxQLQuery(query, config.getBucket()));
        List<Record> result = new ArrayList<>();
        if (queryResults == null) {
            return result;
        }
        for (var queryResult: queryResults.getResults()) {
            if (queryResult == null) {
                continue;
            }
            for (var series: queryResult.getSeries()) {
                if (series == null) {
                    continue;
                }
                for (var values: series.getValues()) {
                    Record record = new Record();
                    series.getColumns().forEach((fieldName, index) -> {
                        Object fieldValue = values.getValueByKey(fieldName);
                        if (fieldValue != null) {
                            if (TIME_FIELD.equals(fieldName)) {
                                record.setTime(ZonedDateTime.ofInstant(
                                        Instant.ofEpochMilli(
                                                TimeUnit.MILLISECONDS.convert(
                                                        Long.parseLong(fieldValue.toString()),
                                                        TimeUnit.NANOSECONDS)),
                                        ZoneOffset.UTC));
                            }
                            else if (metadata.getRecordMetadata().containsKey(fieldName)) {
                                try {
                                    record.getVariables().put(
                                            fieldName,
                                            parseValue(fieldValue, metadata.getRecordMetadata().get(fieldName)));
                                }
                                catch (ValueFormatException ex) {
                                    LOGGER.warn("Error reading from InfluxDB - conversion error", ex);
                                }
                            }
                        }
                    });
                    result.add(record);
                }
            }
        }
        return result;
    }


    private List<Record> getRecordsFlux(Metadata metadata, String query) {
        InfluxDBClient client = ClientHelper.createClient(
                config.getEndpoint(),
                config.getBucket(),
                config.getOrganization(),
                config.getUsername(),
                config.getPassword(),
                config.getToken());
        QueryApi queryApi = client.getQueryApi();
        List<FluxTable> tables = queryApi.query(query);
        int resultLength = tables.stream().mapToInt(x -> x.getRecords().size()).max().orElse(0);
        Record[] result = new Record[resultLength];
        for (int i = 0; i < tables.size(); i++) {
            FluxTable table = tables.get(i);
            for (int j = 0; j < table.getRecords().size(); j++) {
                FluxRecord record = table.getRecords().get(j);
                String fieldName = record.getField();
                if (metadata.getRecordMetadata().containsKey(fieldName)) {
                    Object fieldValue = record.getValue();
                    Record newRecord = result[j] == null ? new Record() : result[j];
                    try {
                        newRecord.getVariables().put(fieldName, parseValue(fieldValue, metadata.getRecordMetadata().get(fieldName)));
                    }
                    catch (ValueFormatException ex) {
                        LOGGER.warn("Error reading from InfluxDB - conversion error", ex);
                    }
                    newRecord.setTime(record.getTime().atZone(ZoneOffset.UTC));
                    result[j] = newRecord;
                }
            }
        }
        return Arrays.asList(result);
    }


    @Override
    public void init(CoreConfig coreConfig, InfluxV2LinkedSegmentProviderConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
    }

}
