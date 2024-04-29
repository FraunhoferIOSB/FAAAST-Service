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

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.LinkedSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Metadata;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Timespan;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.SegmentProviderException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx.AbstractInfluxLinkedSegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Data provider for linked segments referencing an InfluxDB datasource.
 */
public class InfluxV1LinkedSegmentProvider extends AbstractInfluxLinkedSegmentProvider<InfluxV1LinkedSegmentProviderConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxV1LinkedSegmentProvider.class);
    private InfluxV1LinkedSegmentProviderConfig config;

    @Override
    public InfluxV1LinkedSegmentProviderConfig asConfig() {
        return config;
    }


    @Override
    public List<Record> getRecords(Metadata metadata, LinkedSegment segment, Timespan timespan) throws SegmentProviderException {
        return getRecords(metadata, withTimeFilter(segment.getQuery(), timespan));
    }


    @Override
    public List<Record> getRecords(Metadata metadata, LinkedSegment segment) throws SegmentProviderException {
        return getRecords(metadata, segment.getQuery());
    }


    private List<Record> toRecords(Metadata metadata, String query, QueryResult result) throws SegmentProviderException {
        if (result == null || result.getResults() == null) {
            return new ArrayList<>();
        }
        if (result.hasError()) {
            String message = String.format("Error reading from InfluxDB (database: %s, query: %s, error: %s)",
                    config.getDatabase(),
                    query,
                    result.getError());
            LOGGER.debug(message);
            throw new SegmentProviderException(message);
        }
        return result.getResults().stream()
                .flatMap(LambdaExceptionHelper.rethrowFunction(x -> toRecords(metadata, x).stream()))
                .collect(Collectors.toList());
    }


    private List<Record> toRecords(Metadata metadata, QueryResult.Result result) {
        if (result == null || result.getSeries() == null) {
            return new ArrayList<>();
        }
        return result.getSeries().stream()
                .flatMap(LambdaExceptionHelper.rethrowFunction(
                        x -> x.getValues().stream()
                                .map(LambdaExceptionHelper.rethrowFunction(
                                        y -> toRecord(metadata, x.getColumns(), y)))))
                .map(Record.class::cast)
                .collect(Collectors.toList());
    }


    private Record toRecord(Metadata metadata, List<String> fields, List<Object> values) {
        Record result = new Record();
        for (int i = 0; i < values.size(); i++) {
            String fieldName = getPropertyName(fields.get(i));
            Object fieldValue = values.get(i);
            if (metadata.getMetadataRecordVariables().containsKey(fieldName)) {
                Property newProperty = DeepCopyHelper.deepCopy(metadata.getMetadataRecordVariables().get(fieldName), Property.class);
                try {
                    if (Datatype.fromAas4jDatatype(newProperty.getValueType()).equals(Datatype.DATE_TIME)) {
                        newProperty.setValue(fieldValue.toString());
                    }
                    else {
                        newProperty.setValue(parseValue(fieldValue, Datatype.fromAas4jDatatype(newProperty.getValueType())).asString());
                    }
                    result.getTimesAndVariables().put(fieldName, newProperty);
                }
                catch (ValueFormatException e) {
                    LOGGER.warn("InfluxDB reader: Failed to add variable {} - could not parse value", fieldName);
                }
            }
        }
        return result;
    }


    private String getPropertyName(String columnName) {
        String variableName = config.getColumnNameToPropertyName().get(columnName);
        return variableName != null ? variableName : columnName;
    }


    @Override
    protected List<Record> getRecords(Metadata metadata, String query) throws SegmentProviderException {
        return toRecords(metadata, query, executeQuery(query));
    }


    private QueryResult executeQuery(String query) throws SegmentProviderException {
        try {
            InfluxDB influxDB = StringHelper.isBlank(config.getUsername())
                    ? InfluxDBFactory.connect(config.getEndpoint())
                    : InfluxDBFactory.connect(config.getEndpoint(), config.getUsername(), config.getPassword());
            influxDB.setDatabase(config.getDatabase());
            return influxDB.query(new Query(query));
        }
        catch (Exception e) {
            throw new SegmentProviderException(String.format(
                    "error fetching data from database for SMT Time Series Data (endpoint: %s, database: %s, user: %s, password: %s, query: %s)",
                    config.getEndpoint(),
                    config.getDatabase(),
                    config.getUsername(),
                    config.getPassword(),
                    query),
                    e);
        }
    }


    @Override
    public void init(CoreConfig coreConfig, InfluxV1LinkedSegmentProviderConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
    }

}
