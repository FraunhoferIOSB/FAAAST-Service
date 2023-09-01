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
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Metadata;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Segment;
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
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Provides an implementation for SMT TimeSeries ReadSegments operation.
 */
public class ReadSegmentsOperationProvider extends AbstractTimeSeriesOperationProvider {

    private final Map<String, LinkedSegmentProvider> linkedSegmentProviders;
    private final Map<String, ExternalSegmentProvider> externalSegmentProviders;
    private final SegmentProvider internalSegmentProvider;
    private final boolean useSegmentTimestamps;

    public ReadSegmentsOperationProvider(Reference submodelRef, Map<String, LinkedSegmentProvider> linkedSegmentProviders,
            Map<String, ExternalSegmentProvider> externalSegmentProviders, SegmentProvider internalSegmentProvider,
            boolean useSegmentTimestamps) {
        super(Constants.READ_SEGMENTS_ID_SHORT, submodelRef);
        this.linkedSegmentProviders = linkedSegmentProviders;
        this.externalSegmentProviders = externalSegmentProviders;
        this.internalSegmentProvider = internalSegmentProvider;
        this.useSegmentTimestamps = useSegmentTimestamps;
    }


    @Override
    protected void validateInputParameters(OperationVariable[] input) {
        super.validateInputParameters(input);
        Ensure.require(input.length == 1, String.format("%s - found: %d argument(s), expected: %s",
                validationBaseErrorMessage,
                input.length,
                getExpectedInputParametersMessage()));
    }


    private <T extends Segment> Predicate<T> segmentTimeFilter(Timespan timespan, Metadata metadata, Function<T, SegmentProvider> segmentProviderSupplier)
            throws SegmentProviderException {
        return LambdaExceptionHelper.rethrowPredicate(x -> (useSegmentTimestamps && (Objects.nonNull(x.getStart()) || Objects.nonNull(x.getEnd())))
                ? timespan.overlaps(new Timespan(x.getStart(), x.getEnd()))
                : !segmentProviderSupplier.apply(x).getRecords(metadata, x, timespan).isEmpty());
    }


    @Override
    public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
        try {
            validateInputParameters(input);
            Timespan timespan = getTimespanFromInput(input, Constants.READ_SEGMENTS_INPUT_TIMESPAN_ID_SHORT);
            TimeSeries timeSeries = loadTimeSeries();
            List<Segment> result = Stream.concat(Stream.concat(
                    timeSeries.getSegments(InternalSegment.class).stream()
                            .filter(segmentTimeFilter(timespan, timeSeries.getMetadata(), x -> internalSegmentProvider)),
                    timeSeries.getSegments(LinkedSegment.class).stream()
                            .filter(segmentTimeFilter(timespan, timeSeries.getMetadata(),
                                    x -> linkedSegmentProviders.get(x.getEndpoint())))),
                    timeSeries.getSegments(ExternalSegment.class).stream()
                            .filter(segmentTimeFilter(timespan, timeSeries.getMetadata(),
                                    x -> externalSegmentProviders.get(x.getIdShort()))))
                    .collect(Collectors.toList());
            return new OperationVariable[] {
                    new DefaultOperationVariable.Builder()
                            .value(new DefaultSubmodelElementCollection.Builder()
                                    .idShort(Constants.READ_SEGMENTS_OUTPUT_SEGMENTS_ID_SHORT)
                                    .semanticId(ReferenceHelper.globalReference(Constants.READ_SEGMENTS_OUTPUT_SEGMENTS_SEMANTIC_ID))
                                    .values(result.stream().map(SubmodelElement.class::cast).collect(Collectors.toList()))
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
                Constants.READ_SEGMENTS_INPUT_TIMESPAN_ID_SHORT,
                Range.class.getSimpleName(),
                Datatype.LONG.getName());
    }
}
