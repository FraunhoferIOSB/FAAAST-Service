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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.ExternalSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Metadata;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Timespan;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.SegmentProviderException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.csv.AbstractCSVExternalSegmentProvider;
import io.adminshell.aas.v3.model.Blob;
import io.adminshell.aas.v3.model.File;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Data provider for external segments referencing CSV data in file or BLOB.
 */
public class CSVExternalSegmentProvider extends AbstractCSVExternalSegmentProvider<CSVExternalSegmentProviderConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVExternalSegmentProvider.class);
    private CSVExternalSegmentProviderConfig config;

    @Override
    public List<Record> getRecords(Metadata metadata, ExternalSegment segment) throws SegmentProviderException {
        List<Record> resultRecords = new ArrayList<>();
        if (segment.getData() instanceof File) {
            resultRecords = getRecordFromFile(metadata, (File) segment.getData(), null);
        }
        else if (segment.getData() instanceof Blob) {
            resultRecords = getRecordFromBlob(metadata, (Blob) segment.getData(), null);
        }
        return resultRecords;
    }


    @Override
    public List<Record> getRecords(Metadata metadata, ExternalSegment segment, Timespan timespan) throws SegmentProviderException {
        if (!isInTimeRange(segment, timespan)) {
            return new ArrayList<Record>();
        }

        List<Record> resultRecords = new ArrayList<>();
        if (segment.getData() instanceof File) {
            resultRecords = getRecordFromFile(metadata, (File) segment.getData(), timespan);
        }
        else if (segment.getData() instanceof Blob) {
            resultRecords = getRecordFromBlob(metadata, (Blob) segment.getData(), timespan);
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
        Timespan segmentTimespan = new Timespan(segment.getStart(), segment.getEnd());
        if (timespan.overlaps(segmentTimespan)) {
            return true;
        }
        return false;
    }


    private List<Record> getRecordFromFile(Metadata metadata, File data, Timespan timespan) throws SegmentProviderException {
        if (!data.getMimeType().equals(ACCEPTED_MIMETYPE)) {
            String message = String.format("Error reading from File (file: %s, expected type: %s, actual type: %s)",
                    data.getValue(),
                    ACCEPTED_MIMETYPE,
                    data.getMimeType());
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
            recordRows = readCsvToRecords(metadata, reader, timespan);
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


    private List<Record> getRecordFromBlob(Metadata metadata, Blob data, Timespan timespan) throws SegmentProviderException {
        if (!data.getMimeType().equals(ACCEPTED_MIMETYPE)) {
            String message = String.format("Error reading from Blob (Blob ShortID: %s, expected type: %s, actual type: %s)",
                    data.getIdShort(),
                    ACCEPTED_MIMETYPE,
                    data.getMimeType());
            LOGGER.debug(message);
            throw new SegmentProviderException(message);
        }

        List<Record> recordRows = new ArrayList<>();
        String blobData = new String(Base64.getDecoder().decode(data.getValue())); //TODO check correctness
        StringReader reader = new StringReader(blobData);
        recordRows = readCsvToRecords(metadata, reader, timespan);

        return recordRows;
    }


    private List<Record> readCsvToRecords(Metadata metadata, Reader inputReader, Timespan timespan) throws SegmentProviderException {
        ArrayList<Record> recordRows = new ArrayList<>();
        try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(inputReader)) {
            Map<String, String> values;
            while ((values = reader.readMap()) != null) {
                ZonedDateTime timestamp = ZonedDateTime.parse(values.get(config.getTimeColumn()));

                if (timespan != null) {
                    if (timespan.getStart().isPresent() && timespan.getStart().get().isAfter(timestamp)) {
                        continue;
                    }
                    else if (timespan.getEnd().isPresent() && timespan.getEnd().get().isBefore(timestamp)) {
                        break;
                    }
                    else {
                        recordRows.add(toRecord(metadata, values));
                    }
                }
                else {
                    recordRows.add(toRecord(metadata, values));
                }
            }
        }
        catch (IOException e) {
            String message = String.format("Error reading from CSV File: Header not parsable: %s",
                    e.getMessage());
            LOGGER.debug(message);
            throw new SegmentProviderException(message);
        }
        catch (CsvValidationException e) {
            String message = String.format("Error reading from CSV File: csv not valid: %s",
                    e.getMessage());
            LOGGER.debug(message);
            throw new SegmentProviderException(message);
        }

        return recordRows;
    }


    private Record toRecord(Metadata metadata, Map<String, String> row) throws SegmentProviderException {
        Record record = new Record();
        for (Entry<String, String> columnEntry: row.entrySet()) {
            String columnName = columnEntry.getKey().trim();
            if (columnName.equalsIgnoreCase(config.getTimeColumn())) {
                record.setTime(ZonedDateTime.parse(columnEntry.getValue()));
            }
            else if (metadata.getRecordMetadata().containsKey(columnName)) {
                try {
                    record.getVariables().put(columnName,
                            parseValue(columnEntry.getValue(), metadata.getRecordMetadata().get(columnName)));
                }
                catch (ValueFormatException e) {
                    LOGGER.debug("Error reading from CSV - conversion error: " + e.getMessage());
                    throw new SegmentProviderException("Error reading from CSV - conversion error", e);
                }
            }
        }
        return record;
    }

}
