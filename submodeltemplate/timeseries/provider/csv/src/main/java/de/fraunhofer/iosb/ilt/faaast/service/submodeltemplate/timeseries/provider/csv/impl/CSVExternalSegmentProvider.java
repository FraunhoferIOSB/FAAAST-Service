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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.csv.impl;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.ExternalSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Metadata;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Timespan;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.AbsoluteTime;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.MissingInitialisationException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.RelativeTime;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.Time;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.TimeFactory;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.ExternalSegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.SegmentProviderException;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Data provider for external segments referencing CSV data in file or BLOB.
 */
public class CSVExternalSegmentProvider implements ExternalSegmentProvider<CSVExternalSegmentProviderConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVExternalSegmentProvider.class);
    private static final String ACCEPTED_MIMETYPE = "text/csv";

    private CSVExternalSegmentProviderConfig config;

    @Override
    public List<Record> getRecords(Metadata metadata, ExternalSegment segment) throws SegmentProviderException {
        List<Record> resultRecords = new ArrayList<>();
        if (segment.getData() instanceof File file) {
            resultRecords = getRecordFromFile(metadata, file, null, segment.getStart());
        } else if (segment.getData() instanceof Blob blob) {
            resultRecords = getRecordFromBlob(metadata, blob, null, segment.getStart());
        }
        return resultRecords;
    }


    @Override
    public List<Record> getRecords(Metadata metadata, ExternalSegment segment, Timespan timespan) throws SegmentProviderException {
        if (!isInTimeRange(segment, timespan)) {
            return new ArrayList<>();
        }

        List<Record> resultRecords = new ArrayList<>();
        if (segment.getData() instanceof File file) {
            resultRecords = getRecordFromFile(metadata, file, timespan, segment.getStart());
        } else if (segment.getData() instanceof Blob blob) {
            resultRecords = getRecordFromBlob(metadata, blob, timespan, segment.getStart());
        }
        return resultRecords;
    }


    @Override
    public void init(CoreConfig coreConfig, CSVExternalSegmentProviderConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
    }


    @Override
    public CSVExternalSegmentProviderConfig asConfig() {
        return config;
    }


    private boolean isInTimeRange(ExternalSegment segment, Timespan timespan) {
        return timespan.overlaps(new Timespan(segment.getStart(), segment.getEnd()));
    }


    private List<Record> getRecordFromFile(Metadata metadata, File data, Timespan timespan, ZonedDateTime startTime) throws SegmentProviderException {
        if (!data.getContentType().equals(ACCEPTED_MIMETYPE)) {
            String message = String.format("Error reading from File (file: %s, expected type: %s, actual type: %s)",
                    data.getValue(),
                    ACCEPTED_MIMETYPE,
                    data.getContentType());
            LOGGER.debug(message);
            throw new SegmentProviderException(message);
        }

        List<Record> recordRows = new ArrayList<>();
        java.io.File csvFile;
        if (config.getBaseDir() != null) {
            csvFile = new java.io.File(config.getBaseDir(), data.getValue());
        }
        else {
            csvFile = new java.io.File(data.getValue());
        }
        try (FileReader reader = new FileReader(csvFile)) {
            recordRows = readCsvToRecords(metadata, reader, timespan, startTime);
        }
        catch (FileNotFoundException e) {
            String message = String.format("Error reading from File (file: %s, full path: %s): FileNotFoundException: %s",
                    data.getValue(),
                    csvFile.getAbsolutePath(),
                    e.getMessage());
            LOGGER.debug(message);
            throw new SegmentProviderException(message);
        }
        catch (IOException e1) {
            String message = String.format("Error reading from File (file: %s, full path: %s): Error closing reader: %s",
                    data.getValue(),
                    csvFile.getAbsolutePath(),
                    e1.getMessage());
            LOGGER.debug(message);
            throw new SegmentProviderException(message);
        }

        return recordRows;
    }


    private List<Record> getRecordFromBlob(Metadata metadata, Blob data, Timespan timespan, ZonedDateTime startTime) throws SegmentProviderException {
        if (!data.getContentType().equals(ACCEPTED_MIMETYPE)) {
            String message = String.format("Error reading from Blob (Blob ShortID: %s, expected type: %s, actual type: %s)",
                    data.getIdShort(),
                    ACCEPTED_MIMETYPE,
                    data.getContentType());
            LOGGER.debug(message);
            throw new SegmentProviderException(message);
        }

        List<Record> recordRows = new ArrayList<>();
        try {
            String blobData = new String(Base64.getDecoder().decode(data.getValue())); //TODO check correctness
            StringReader reader = new StringReader(blobData);
            recordRows = readCsvToRecords(metadata, reader, timespan, startTime);
        }
        catch (IllegalArgumentException e) {
            String message = String.format("Error reading from Blob (Blob ShortID: %s): Not a Base64 encoded scheme",
                    data.getIdShort());
            LOGGER.debug(message);
            throw new SegmentProviderException(message);
        }

        return recordRows;
    }


    private List<Record> readCsvToRecords(Metadata metadata, Reader inputReader, Timespan timespan, ZonedDateTime startTime) throws SegmentProviderException {
        List<Record> recordRows = new ArrayList<>();
        try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(inputReader)) {
            String timeColumnForFilter = "";
            Time timeColumnsTimeObject = null;

            for (String currentColumn: config.getTimeColumns()) {
                if (metadata.getMetadataRecordVariables().get(getVariableName(currentColumn)) != null) {
                    timeColumnForFilter = currentColumn;
                    Property timeProperty = metadata.getMetadataRecordVariables().get(getVariableName(currentColumn));
                    timeColumnsTimeObject = TimeFactory.getTimeTypeFrom(timeProperty.getSemanticId(), timeProperty.getValue()).orElse(null);
                    break;
                }
            }
            if (timeColumnsTimeObject == null) {
                LOGGER.error(
                        "NO TIMESTAMP FOUND IN CSV: no time columns defined by the config matched in metadata. Time-Columns in metadata: {} /n Columns in config: {} /n Mapping: {}",
                        metadata.getMetadataRecordVariables().keySet(), config.getTimeColumns(), config.getColumnToVariableNames().entrySet());
                return new ArrayList<>();
            }

            recordRows = readAndExtractRecords(reader, metadata, timeColumnsTimeObject, timeColumnForFilter, startTime, timespan);
        }
        catch (IOException e) {
            String message = String.format("Error reading from CSV File: Header not parsable: %s", e.getMessage());
            LOGGER.error(message, e);
            throw new SegmentProviderException(message);
        }
        return recordRows;
    }


    private List<Record> readAndExtractRecords(CSVReaderHeaderAware reader, Metadata metadata, Time timetype, String timecolumnName, ZonedDateTime startTime, Timespan timespan)
            throws SegmentProviderException {
        ArrayList<Record> recordRows = new ArrayList<>();
        Map<String, String> values;

        boolean absoluteType = AbsoluteTime.class.isAssignableFrom(timetype.getClass());

        try {
            ZonedDateTime currentStartTime = startTime;
            while ((values = reader.readMap()) != null) {
                Optional<Time> currentTime = TimeFactory.getTimeTypeFrom(TimeFactory.getSemanticIDForClass(timetype.getClass()), values.get(timecolumnName));
                if (currentTime.isEmpty()) {
                    continue;
                }

                ZonedDateTime timeStart;
                try {
                    if (absoluteType) {
                        timeStart = ((AbsoluteTime) currentTime.get()).getStartAsUtcTime();
                    }
                    else {
                        timeStart = ((RelativeTime) currentTime.get()).getStartAsUtcTime(currentStartTime);

                        if (((RelativeTime) currentTime.get()).isIncrementalToPrevious()) {
                            currentStartTime = ((RelativeTime) currentTime.get()).getEndAsEpochMillis(currentStartTime);
                        }
                    }
                }
                catch (MissingInitialisationException e) {
                    LOGGER.warn("Error in reading CSV: Could not determine time range of record.");
                    continue;
                }

                if (timespan != null) {
                    if (timespan.getEnd().isPresent() && timespan.getEnd().get().isBefore(timeStart)) {
                        break;
                    }
                    else if (!(timespan.getStart().isPresent() && timespan.getStart().get().isAfter(timeStart))) {
                        recordRows.add(toRecord(metadata, values));
                    }
                }
                else {
                    recordRows.add(toRecord(metadata, values));
                }
            }
        }
        catch (IOException e) {
            String message = String.format("Error reading from CSV File: Number of header items does not match number of columns: %s",
                    e.getMessage());
            LOGGER.error(message);
            throw new SegmentProviderException(message);
        }
        catch (CsvValidationException e) {
            String message = String.format("Error reading from CSV File: CSV not valid: %s",
                    e.getMessage());
            LOGGER.error(message);
            throw new SegmentProviderException(message);
        }
        return recordRows;
    }


    private Record toRecord(Metadata metadata, Map<String, String> row) {
        Record newRecord = new Record();
        for (Entry<String, String> columnEntry: row.entrySet()) {
            String columnName = columnEntry.getKey().trim();
            if (metadata.getMetadataRecordVariables().containsKey(getVariableName(columnName))) {
                Property newProperty = DeepCopyHelper.deepCopy(metadata.getMetadataRecordVariables().get(getVariableName(columnName)), Property.class);
                newProperty.setValue(columnEntry.getValue());
                newRecord.getTimesAndVariables().put(getVariableName(columnName), newProperty);
            }
        }
        return newRecord;
    }


    private String getVariableName(String columnName) {
        String equivalent = this.config.getColumnToVariableNames().get(columnName);
        return equivalent != null ? equivalent : columnName;
    }

}
