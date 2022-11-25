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

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Timespan;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.LinkedSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Metadata;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.LinkedSegmentProvider;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;


/**
 * Data provider for linked segments referencing an InfluxDB datasource.
 */
public class InfluxV1LinkedSegmentProvider implements LinkedSegmentProvider<InfluxV1LinkedSegmentProviderConfig> {

    private InfluxV1LinkedSegmentProviderConfig config;

    @Override
    public InfluxV1LinkedSegmentProviderConfig asConfig() {
        return config;
    }


    private TypedValue<?> parseValue(Object value, Datatype datatype) throws ValueFormatException {
        Object valuePreprocessed = value;
        switch (datatype) {
            case BYTE:
            case INT:
            case INTEGER:
            case SHORT: {
                if (value instanceof Number) {
                    valuePreprocessed = ((Number) value).intValue();
                }
            }

        }
        return TypedValueFactory.create(datatype, Objects.toString(valuePreprocessed));
    }


    private String updateTimeFilterOnQuery(String query, Timespan timespan) {
        String result = query;
        if (timespan == null) {
            return result;
        }
        if (timespan.getStart().isPresent()) {
            result = String.format("%s %s %s >= %s",
                    result,
                    result.toLowerCase().contains(" where ") ? "AND" : "WHERE",
                    config.getTimeField(),
                    timespan.getStart().get());
        }
        if (timespan.getEnd().isPresent()) {
            result = String.format("%s %s %s <= %s",
                    result,
                    result.toLowerCase().contains(" where ") ? "AND" : "WHERE",
                    config.getTimeField(),
                    timespan.getEnd().get());
        }
        return result;
    }


    @Override
    public List<Record> getRecords(Metadata metadata, LinkedSegment segment, Timespan timespan) {
        return getRecords(metadata, segment, updateTimeFilterOnQuery(segment.getQuery(), timespan));
    }


    private List<Record> getRecords(Metadata metadata, LinkedSegment segment, String query) {
        InfluxDB influxDB = InfluxDBFactory.connect(segment.getEndpoint());
        influxDB.setDatabase(config.getDatabase());
        QueryResult queryResult = influxDB.query(new Query(query));
        QueryResult.Series series = queryResult.getResults().get(0).getSeries().get(0);
        List<Record> result = new ArrayList<>();
        for (var values: series.getValues()) {
            Record record = new Record();
            for (int i = 0; i < values.size(); i++) {
                String fieldName = series.getColumns().get(i);
                Object fieldValue = values.get(i);
                if (config.getTimeField().equals(fieldName)) {
                    record.setTime(ZonedDateTime.parse(fieldValue.toString()));
                }
                else if (metadata.getRecordMetadata().containsKey(fieldName)) {
                    try {
                        record.getVariables().put(
                                fieldName,
                                parseValue(fieldValue, metadata.getRecordMetadata().get(fieldName)));
                    }
                    catch (ValueFormatException ex) {
                        Logger.getLogger(InfluxV1LinkedSegmentProvider.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            result.add(record);
        }
        return result;
    }


    @Override
    public List<Record> getRecords(Metadata metadata, LinkedSegment segment) {
        return getRecords(metadata, segment, segment.getQuery());
    }


    @Override
    public void init(CoreConfig coreConfig, InfluxV1LinkedSegmentProviderConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
    }

}
