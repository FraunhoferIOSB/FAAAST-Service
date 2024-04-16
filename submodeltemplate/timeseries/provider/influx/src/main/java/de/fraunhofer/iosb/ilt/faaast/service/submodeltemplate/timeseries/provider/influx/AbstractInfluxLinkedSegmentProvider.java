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
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.LinkedSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Metadata;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Timespan;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.LinkedSegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.SegmentProviderException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * Data provider for linked segments referencing an InfluxDB datasource.
 *
 * @param <T> type of corresponding config to use
 */
public abstract class AbstractInfluxLinkedSegmentProvider<T extends AbstractInfluxLinkedSegmentProviderConfig> implements LinkedSegmentProvider<T> {

    private static final String SELECT = "SELECT";
    public static final String TIME_FIELD = "time";
    public static final String FLUX_RANGE_START = "start";
    public static final String FLUX_RANGE_STOP = "stop";
    public static final String FLUX_RANGE = "range";
    public static final String FLUX_SEPERATOR = "|>";
    private T config;

    @Override
    public T asConfig() {
        return config;
    }


    /**
     * Fetch data from database and convert to
     * {@link de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record}.
     *
     * @param metadata the metadata of the segment containing fields and types to read
     * @param query the query to execute
     * @return fetched data
     * @throws de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.SegmentProviderException if
     *             fetching the data fails
     */
    protected abstract List<Record> getRecords(Metadata metadata, String query) throws SegmentProviderException;


    @Override
    public List<Record> getRecords(Metadata metadata, LinkedSegment segment, Timespan timespan) throws SegmentProviderException {
        return getRecords(metadata, withTimeFilter(segment.getQuery(), timespan));
    }


    @Override
    public List<Record> getRecords(Metadata metadata, LinkedSegment segment) throws SegmentProviderException {
        return getRecords(metadata, segment.getQuery());
    }


    @Override
    public void init(CoreConfig coreConfig, T config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
    }


    /**
     * Checks if a given query is written using InfluxQL. This check is extremely over-simplified and only checks if
     * query starts with 'SELECT'.
     *
     * @param query the query to check
     * @return true if query is written in InfluxQL, false otherwise
     */
    public static boolean isInfluxQL(String query) {
        return query.toUpperCase().startsWith(SELECT);
    }


    /**
     * Updates an InfluxQL query to include a time filter.
     *
     * @param query the query in InfluxQL syntax
     * @param timespan the timespan to filter
     * @return the updated query
     */
    protected static String withTimeFilterInfluxQL(String query, Timespan timespan) {
        String result = query;
        if (timespan == null) {
            return result;
        }
        if (timespan.getStart().isPresent()) {
            result = String.format("%s %s %s >= %s",
                    result,
                    result.toLowerCase().contains(" where ") ? "AND" : "WHERE",
                    TIME_FIELD,
                    TimeUnit.NANOSECONDS.convert(timespan.getStart().get().toInstant().toEpochMilli(), TimeUnit.MILLISECONDS));
        }
        if (timespan.getEnd().isPresent()) {
            result = String.format("%s %s %s <= %s",
                    result,
                    result.toLowerCase().contains(" where ") ? "AND" : "WHERE",
                    TIME_FIELD,
                    TimeUnit.NANOSECONDS.convert(timespan.getEnd().get().toInstant().toEpochMilli(), TimeUnit.MILLISECONDS));
        }
        return result;
    }


    /**
     * Updates a query to include a time filter. Works both on Flux and InfluxQL queries.
     *
     * @param query the query
     * @param timespan the timespan to filter
     * @return the updated query
     */
    public static String withTimeFilter(String query, Timespan timespan) {
        return isInfluxQL(query)
                ? withTimeFilterInfluxQL(query, timespan)
                : withTimeFilterFlux(query, timespan);
    }


    /**
     * Updates a Flux query to include a time filter.
     *
     * @param query the query in Flux syntax
     * @param timespan the timespan to filter
     * @return the updated query
     */
    protected static String withTimeFilterFlux(String query, Timespan timespan) {
        if (timespan.getStart().isPresent() || timespan.getEnd().isPresent()) {
            String filter = "";
            if (timespan.getStart().isPresent()) {
                filter = String.format("%s: %s", FLUX_RANGE_START,
                        timespan.getStart().get().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            }
            if (timespan.getEnd().isPresent()) {
                filter += String.format("%s%s: %s",
                        (filter.length() > 0 ? ", " : ""),
                        FLUX_RANGE_STOP,
                        timespan.getEnd().get().plusNanos(1).format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            }
            return String.format("%s %s %s(%s)",
                    query,
                    FLUX_SEPERATOR,
                    FLUX_RANGE,
                    filter);
        }
        return query;
    }


    /**
     * Parse a value to AAS {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue}.
     *
     * @param value the value to parse
     * @param datatype the datatype
     * @return the parse value
     * @throws ValueFormatException if parsign fails
     */
    protected static TypedValue parseValue(Object value, Datatype datatype) throws ValueFormatException {
        Object valuePreprocessed = value;
        switch (datatype) {
            case BYTE:
            case INT:
            case INTEGER:
            case SHORT: {
                if (value instanceof Number) {
                    valuePreprocessed = ((Number) value).intValue();
                }
                break;
            }
            default:
                // intentionally left empty
        }
        return TypedValueFactory.create(datatype, Objects.toString(valuePreprocessed));
    }
}
