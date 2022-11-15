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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.LinkedSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.Metadata;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.SegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Provides an implementation for SMT TimeSeries ReadRecords operation.
 */
public class ReadRecordsOperationProvider extends AbstractTimeSeriesOperationProvider {

    private Map<String, SegmentProvider> segmentProviders;

    public ReadRecordsOperationProvider(Submodel submodel, Map<String, SegmentProvider> segmentProviders) {
        super(Constants.READ_RECORDS_ID_SHORT, submodel);
        this.segmentProviders = segmentProviders;
    }


    @Override
    protected void validateInputParameters(OperationVariable[] input) {
        super.validateInputParameters(input);
        Ensure.require(input.length == 1, String.format("%s - found: %d argument(s), expected: %s",
                validationBaseErrorMessage,
                input.length,
                getExpectedInputParametersMessage()));
    }


    @Override
    public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
        //get Records from LinkedSegmentProvider
        validateInputParameters(input);
        Timespan timespan = getTimespanFromInput(input, Constants.READ_RECORDS_INPUT_TIMESPAN_ID_SHORT);
        List<SubmodelElementCollection> internalSegments = getSegments(true, false, false);
        List<LinkedSegment> linkedSegments = toLinkedSegment(getSegments(false, false, true));

        Metadata metadata = getMetadata();
        Map<LinkedSegment, List<Record>> allRecordsFromLinkedSegment = new HashMap<>();
        linkedSegments.forEach(x -> allRecordsFromLinkedSegment.put(x, segmentProviders.get(x.getEndpoint()).getRecords(metadata, x)));

        //convert records from linked segments to SMC internalSegments
        List<SubmodelElementCollection> linkedSegmentRecords = new ArrayList<>();
        allRecordsFromLinkedSegment.forEach((x, y) -> {
            linkedSegmentRecords.add(new DefaultSubmodelElementCollection.Builder()
                    .idShort(UUID.randomUUID().toString())
                    .description(new LangString(x.getEndpoint(), "en"))
                    .description(new LangString(x.getQuery(), "en"))
                    .values(recordToAas(y))
                    .build());
        });

        List<SubmodelElementCollection> allRecords = internalSegments.stream()
                .flatMap(x -> getRecords(x).stream())
                .collect(Collectors.toList());
        allRecords.addAll(linkedSegmentRecords);

        //Todo
        List<SubmodelElementCollection> relevantRecords = filterRecords(allRecords, timespan);
        return new OperationVariable[] {
                new DefaultOperationVariable.Builder()
                        .value(new DefaultSubmodelElementCollection.Builder()
                                .idShort(Constants.READ_RECORDS_OUTPUT_RECORDS_ID_SHORT)
                                .semanticId(ReferenceHelper.globalReference(Constants.READ_RECORDS_OUTPUT_RECORDS_SEMANTIC_ID))
                                .values(relevantRecords.stream().map(x -> (SubmodelElementCollection) x).collect(Collectors.toList()))
                                .build())
                        .build()
        };
    }


    private List<LinkedSegment> toLinkedSegment(List<SubmodelElementCollection> linkedSegments) {
        List<LinkedSegment> result = new ArrayList<>();
        linkedSegments.forEach(x -> {
            LinkedSegment linkedSegment = new LinkedSegment();
            linkedSegment.setEndpoint(findRecursive(x.getValues(), "Endpoint", Property.class).getValue());
            linkedSegment.setQuery(findRecursive(x.getValues(), "Query", Property.class).getValue());
            result.add(linkedSegment);
        });
        return result;
    }


    private Collection<SubmodelElement> recordToAas(List<Record> records) {
        Collection<SubmodelElement> result = new ArrayList<>();
        records.forEach(x -> result.add(recordToAas(x)));
        return result;
    }


    private SubmodelElement recordToAas(Record record) {
        return new DefaultSubmodelElementCollection.Builder()
                .idShort(record.getIdShort())
                .semanticId(ReferenceHelper.globalReference(Constants.RECORD_SEMANTIC_ID))
                .values(record.getValues().stream()
                        .map(value -> new DefaultProperty.Builder()
                                .idShort(UUID.randomUUID().toString())
                                .valueType("string")
                                .value(((Property) value).getValue())
                                .build())
                        .collect(Collectors.toList()))
                .value(new DefaultProperty.Builder()
                        .idShort(Constants.RECORD_TIME_ID_SHORT)
                        .valueType(Datatype.STRING.getName())
                        .value(record.getTime().toString())
                        .build())
                .build();
    }


    @Override
    protected String getExpectedInputParametersMessage() {
        return String.format("%s [%s, valueType: %s]",
                Constants.READ_RECORDS_INPUT_TIMESPAN_ID_SHORT,
                Range.class.getSimpleName(),
                Datatype.LONG.getName());
    }
}
