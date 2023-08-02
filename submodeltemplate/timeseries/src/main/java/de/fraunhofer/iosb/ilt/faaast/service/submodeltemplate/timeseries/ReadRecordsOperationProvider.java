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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.ExternalSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.InternalSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.LinkedSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.TimeSeries;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Timespan;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.ExternalSegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.LinkedSegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.SegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.SegmentProviderException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Provides an implementation for SMT TimeSeries ReadRecords operation.
 */
public class ReadRecordsOperationProvider extends AbstractTimeSeriesOperationProvider {

    private final Map<String, LinkedSegmentProvider> linkedSegmentProviders;
    private final Map<String, ExternalSegmentProvider> externalSegmentProviders;
    private final SegmentProvider internalSegmentProvider;

    public ReadRecordsOperationProvider(Reference submodelRef, Map<String, LinkedSegmentProvider> linkedSegmentProviders,
            Map<String, ExternalSegmentProvider> externalSegmentProviders, SegmentProvider internalSegmentProvider) {
        super(Constants.READ_RECORDS_ID_SHORT, submodelRef);
        this.linkedSegmentProviders = linkedSegmentProviders;
        this.externalSegmentProviders = externalSegmentProviders;
        this.internalSegmentProvider = internalSegmentProvider;
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
        try {
            validateInputParameters(input);
            Timespan timespan = getTimespanFromInput(input, Constants.READ_RECORDS_INPUT_TIMESPAN_ID_SHORT);
            TimeSeries timeSeries = loadTimeSeries();
            List<Record> result = Stream.concat(Stream.concat(
                    timeSeries.getSegments(InternalSegment.class).stream()
                            .flatMap(LambdaExceptionHelper.rethrowFunction(
                                    x -> (Stream<Record>) internalSegmentProvider.getRecords(
                                            timeSeries.getMetadata(),
                                            x,
                                            timespan).stream())),
                    timeSeries.getSegments(LinkedSegment.class).stream()
                            .filter(x -> linkedSegmentProviders.containsKey(x.getEndpoint()))
                            .flatMap(LambdaExceptionHelper.rethrowFunction(
                                    x -> (Stream<Record>) linkedSegmentProviders.get(x.getEndpoint()).getRecords(
                                            timeSeries.getMetadata(),
                                            x,
                                            timespan).stream()))),
                    timeSeries.getSegments(ExternalSegment.class).stream()
                            .filter(x -> externalSegmentProviders.containsKey(x.getIdShort())) //TODO don't use toString, use different identifier for externalSegments
                            .flatMap(LambdaExceptionHelper.rethrowFunction(
                                    x -> (Stream<Record>) externalSegmentProviders.get(x.getIdShort()).getRecords( //TODO see above
                                            timeSeries.getMetadata(),
                                            x,
                                            timespan).stream())))
                    .collect(Collectors.toList());
            return new OperationVariable[] {
                    new DefaultOperationVariable.Builder()
                            .value(new DefaultSubmodelElementCollection.Builder()
                                    .idShort(Constants.READ_RECORDS_OUTPUT_RECORDS_ID_SHORT)
                                    .semanticId(ReferenceHelper.globalReference(Constants.READ_RECORDS_OUTPUT_RECORDS_SEMANTIC_ID))
                                    .values(result.stream().map(SubmodelElementCollection.class::cast).collect(Collectors.toList()))
                                    .build())
                            .build()
            };
        }
        catch (SegmentProviderException | ValueFormatException e) {
            throw new AssetConnectionException(e);
        }
    }


    @Override
    protected String getExpectedInputParametersMessage() {
        return String.format("%s [%s, valueType: %s]",
                Constants.READ_RECORDS_INPUT_TIMESPAN_ID_SHORT,
                Range.class.getSimpleName(),
                Datatype.LONG.getName());
    }
}
