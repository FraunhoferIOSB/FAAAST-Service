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
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.LongTimespan;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Metadata;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.Time;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.TimeFactory;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.impl.UtcTime;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.SegmentProviderException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx.AbstractInfluxLinkedSegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx.util.ClientHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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
    public List<Record> getRecords(Metadata metadata, LinkedSegment segment, LongTimespan timespan) throws SegmentProviderException {
        return getRecords(metadata, withTimeFilter(segment.getQuery(), timespan));
    }


    @Override
    public List<Record> getRecords(Metadata metadata, LinkedSegment segment) throws SegmentProviderException {
        return getRecords(metadata, segment.getQuery());
    }


    @Override
    protected List<Record> getRecords(Metadata metadata, String query) throws SegmentProviderException {
        return isInfluxQL(query)
                ? getRecordsInfluxQL(metadata, query)
                : getRecordsFlux(metadata, query);
    }


    private List<Record> toRecords(Metadata metadata, InfluxQLQueryResult result) throws SegmentProviderException {
        if (result == null || result.getResults() == null) {
            return new ArrayList<>();
        }
        return result.getResults().stream()
                .flatMap(LambdaExceptionHelper.rethrowFunction(x -> toRecords(metadata, x).stream()))
                .collect(Collectors.toList());
    }


    private List<Record> toRecords(Metadata metadata, InfluxQLQueryResult.Result result) throws SegmentProviderException {
        if (result == null || result.getSeries() == null) {
            return new ArrayList<>();
        }
        return result.getSeries().stream()
                .flatMap(LambdaExceptionHelper.rethrowFunction(
                        x -> x.getValues().stream()
                                .map(LambdaExceptionHelper.rethrowFunction(
                                        y -> toRecord(metadata, x.getColumns(), y.getValues())))))
                .map(Record.class::cast)
                .collect(Collectors.toList());
    }


    private Record toRecord(Metadata metadata, Map<String, Integer> fields, Object[] values) throws SegmentProviderException {
        Record result = new Record();
        fields.forEach(LambdaExceptionHelper.rethrowBiConsumer(
                (fieldName, index) -> {
                    fieldName = getPropertyName(fieldName);
                    Object fieldValue = values[index];
                    if (fieldValue != null) {
                        if (TIME_FIELD.equals(fieldName)) {
                            UtcTime utcTime = new UtcTime(ZonedDateTime.ofInstant(
                                    Instant.ofEpochMilli(
                                            TimeUnit.MILLISECONDS.convert(
                                                    Long.parseLong(fieldValue.toString()),
                                                    TimeUnit.NANOSECONDS)),
                                    ZoneOffset.UTC).format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
                            result.getTimes().put(fieldName, utcTime);
                        }
                        else if (metadata.getRecordMetadataTime().containsKey(fieldName)) {
                            Time metaType = metadata.getRecordMetadataTime().get(fieldName);
                            result.getTimes().put(fieldName,
                                    TimeFactory.getTimeTypeFrom(metaType.getTimeSemanticID(), fieldValue.toString()).orElse(null));
                        }
                        else if (metadata.getRecordMetadataVariables().containsKey(fieldName)) {
                            try {
                                result.getVariables().put(
                                        fieldName,
                                        parseValue(fieldValue, metadata.getRecordMetadataVariables().get(fieldName).getDataType()));
                            }
                            catch (ValueFormatException e) {
                                throw new SegmentProviderException("Error reading from InfluxDB - conversion error", e);
                            }
                        }
                    }
                }));
        return result;
    }


    private List<Record> getRecordsInfluxQL(Metadata metadata, String query) throws SegmentProviderException {
        try (InfluxDBClient client = ClientHelper.createClient(
                config.getEndpoint(),
                config.getBucket(),
                config.getOrganization(),
                config.getUsername(),
                config.getPassword(),
                config.getToken())) {
            return toRecords(metadata, client.getInfluxQLQueryApi().query(new InfluxQLQuery(query, config.getBucket())));
        }
        catch (Exception e) {
            throw new SegmentProviderException("error reading data from influx", e);
        }
    }


    private List<Record> getRecordsFlux(Metadata metadata, String query) throws SegmentProviderException {
        try (InfluxDBClient client = ClientHelper.createClient(
                config.getEndpoint(),
                config.getBucket(),
                config.getOrganization(),
                config.getUsername(),
                config.getPassword(),
                config.getToken())) {
            QueryApi queryApi = client.getQueryApi();
            List<FluxTable> tables = queryApi.query(query);
            int resultLength = tables.stream().mapToInt(x -> x.getRecords().size()).max().orElse(0);
            Record[] result = new Record[resultLength];
            for (int i = 0; i < tables.size(); i++) {
                FluxTable table = tables.get(i);
                for (int j = 0; j < table.getRecords().size(); j++) {
                    FluxRecord record = table.getRecords().get(j);
                    String fieldName = getPropertyName(record.getField());
                    if (metadata.getRecordMetadataVariables().containsKey(fieldName)) {
                        Object fieldValue = record.getValue();
                        Record newRecord = result[j] == null ? new Record() : result[j];
                        try {
                            newRecord.getVariables().put(fieldName, parseValue(fieldValue, metadata.getRecordMetadataVariables().get(fieldName).getDataType()));
                        }
                        catch (ValueFormatException ex) {
                            LOGGER.warn("Error reading from InfluxDB - conversion error", ex);
                        }
                        newRecord.getTimes().put(TIME_FIELD, new UtcTime(record.getTime().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_ZONED_DATE_TIME)));
                        result[j] = newRecord;
                    }
                }
            }
            return Arrays.asList(result);
        }
        catch (Exception e) {
            throw new SegmentProviderException("error reading data from influx", e);
        }
    }


    private String getPropertyName(String columnName) {
        String variableName = config.getColumnNameToPropertyName().get(columnName);
        return variableName != null ? variableName : columnName;
    }


    @Override
    public void init(CoreConfig coreConfig, InfluxV2LinkedSegmentProviderConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
    }

}
