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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.util.AasHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.CollectionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Abstract base class for SMT TimeSeries operation provider implementations.
 */
public abstract class AbstractTimeSeriesOperationProvider implements AssetOperationProvider {

    protected final Submodel submodel;
    protected final String validationBaseErrorMessage;

    protected AbstractTimeSeriesOperationProvider(String name, Submodel submodel) {
        Ensure.requireNonNull(name, "name must be non-null");
        Ensure.requireNonNull(submodel, "submodel must be non-null");
        this.submodel = submodel;
        this.validationBaseErrorMessage = String.format("error validating input parameter(s) for operation '%s'", name);
    }


    /**
     * Safely parses a given string to long resp. returns a default value is string does not contain a valid long value.
     *
     * @param input the string to parse
     * @param defaultValue the default value to use
     * @return the string parsed as long or the default value if parsing failed
     */
    protected static long tryParseLong(String input, long defaultValue) {
        try {
            return Long.parseLong(input);
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }


    /**
     * Safely parses a given string to an {@code Optional<Long>}.
     *
     * @param input the string to parse
     * @return {@code Optional.empty()} if input is null or does not contain a valid long value, otherwise
     *         {@code Optional<Long>} containing the parsed value.
     */
    protected static Optional<Long> tryParseLong(String input) {
        if (input == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(input));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }


    /**
     * Gets a parameter by isShort and type from array of {@link OperationVariable} if present, otherwise throws
     * {@link IllegalArgumentException} with user-firendly message.
     *
     * @param <T> expected type of SubmodelElement
     * @param variables variables to get parameter from
     * @param idShort idShort of the parameter to get
     * @param type type of the parameter
     * @return the requested parameter, if present
     * @throws IllegalArgumentException if no such parameter is present
     */
    protected <T extends SubmodelElement> T getParameter(OperationVariable[] variables, String idShort, Class<T> type) {
        return (T) Stream.of(variables)
                .filter(x -> Objects.equals(idShort, x.getValue().getIdShort()))
                .filter(Objects::nonNull)
                .map(x -> x.getValue())
                .filter(Objects::nonNull)
                .filter(x -> type.isAssignableFrom(x.getClass()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("%s - missing input with idShort '%s' and type '%s'",
                        validationBaseErrorMessage,
                        idShort,
                        type.getName())));
    }


    /**
     * Gets the Segments collection of the SMT TimeSeries submodel.
     *
     * @return the Segments collection
     */
    protected SubmodelElementCollection getSegmentsCollection() {
        return AasHelper.getElementByIdShort(submodel.getSubmodelElements(), Constants.SEGMENTS_ID_SHORT, SubmodelElementCollection.class);
    }


    /**
     * Gets a list of segments of the SMT TimeSeries submodel.
     *
     * @param internalSegments if internalSegments should be returned
     * @param externalSegments if externalSegments should be returned
     * @param linkedSegments if linkedSegments should be returned
     * @return list of segments of selected type(s)
     */
    protected List<SubmodelElementCollection> getSegments(boolean internalSegments, boolean externalSegments, boolean linkedSegments) {
        SubmodelElementCollection segmentsCollection = getSegmentsCollection();
        return CollectionHelper.merge(
                internalSegments ? AasHelper.getElementsBySemanticId(segmentsCollection.getValues(), Constants.INTERNAL_SEGMENT_SEMANTIC_ID, SubmodelElementCollection.class)
                        : List.of(),
                externalSegments ? AasHelper.getElementsBySemanticId(segmentsCollection.getValues(), Constants.EXTERNAL_SEGMENT_SEMANTIC_ID, SubmodelElementCollection.class)
                        : List.of(),
                linkedSegments ? AasHelper.getElementsBySemanticId(segmentsCollection.getValues(), Constants.LINKED_SEGMENT_SEMANTIC_ID, SubmodelElementCollection.class)
                        : List.of());
    }


    /**
     * Gets the records of an InternalSegment.
     *
     * @param internalSegment the InternalSegment
     * @return the records the the InternalSegment
     */
    protected List<SubmodelElementCollection> getRecords(SubmodelElementCollection internalSegment) {
        return AasHelper.getElementsBySemanticId(
                AasHelper.getElementByIdShort(
                        internalSegment.getValues(),
                        Constants.INTERNAL_SEGMENT_RECORDS_ID_SHORT,
                        SubmodelElementCollection.class)
                        .getValues(),
                Constants.RECORD_SEMANTIC_ID,
                SubmodelElementCollection.class);
    }


    /**
     * Filters a given list of records by timespan.
     *
     * @param records the records to filter
     * @param timespan the timespan the record need to conform to
     * @return filtered list of records
     */
    protected List<SubmodelElementCollection> filterRecords(List<SubmodelElementCollection> records, Timespan timespan) {
        return records.stream()
                .filter(x -> timespan.includes(Long.parseLong(AasHelper.getElementByIdShort(x.getValues(), Constants.RECORD_TIME_ID_SHORT, Property.class).getValue())))
                .collect(Collectors.toList());
    }


    /**
     * Filters a given list of segments by timespan.
     *
     * @param segments the segments to filter
     * @param timespan the timespan the segments need to conform to
     * @param useSegmentTimestamps if start and end time of a segment should be used. If true, filtering by start and
     *            end time happens if at least one of the properties is set, otherwise its a fallback to record-level
     *            filtering.
     * @return filtered list of segments
     */
    protected List<SubmodelElementCollection> filterSegments(List<SubmodelElementCollection> segments, Timespan timespan, boolean useSegmentTimestamps) {
        return segments.stream()
                .filter(x -> {
                    if (useSegmentTimestamps) {
                        Timespan segmentTimestamp = new Timespan(
                                AasHelper.getElementByIdShort(x.getValues(), Constants.SEGMENT_START_TIME_ID_SHORT, Property.class),
                                AasHelper.getElementByIdShort(x.getValues(), Constants.SEGMENT_END_TIME_ID_SHORT, Property.class));
                        if (segmentTimestamp.getStart().isPresent() || segmentTimestamp.getEnd().isPresent()) {
                            return timespan.overlaps(segmentTimestamp);
                        }
                        // segment does have neither start or end timestamp -> check on record level                        
                    }
                    return !filterRecords(getRecords(x), timespan).isEmpty();
                })
                .collect(Collectors.toList());
    }


    /**
     * Extract Timespan parameter from input variables.
     *
     * @param input the input parameters
     * @param idShort idShort identifying the Timespan
     * @return the parsed Timespan
     * @throws IllegalArgumentException if Timespan parameter is not present or does not match requirements
     */
    protected Timespan getTimespanFromInput(OperationVariable[] input, String idShort) {
        Range timespan = getParameter(input, idShort, Range.class);
        if (!Datatype.LONG.getName().equals(timespan.getValueType())) {
            throw new IllegalArgumentException(String.format("%s - required valuedType for property with idShort '%s' is '%s' but found '%s'",
                    validationBaseErrorMessage,
                    idShort,
                    Datatype.LONG.getName(),
                    timespan.getValueType()));
        }
        return new Timespan(tryParseLong(timespan.getMin(), Long.MIN_VALUE), tryParseLong(timespan.getMax(), Long.MAX_VALUE));
    }


    /**
     * Validates input parameters. This method is not called in abstract class and should therefore be called manually
     * by inheriting classes.
     *
     * @param input the input parameters to check
     */
    protected void validateInputParameters(OperationVariable[] input) {
        Ensure.requireNonNull(input, String.format("%s - found: null, expected: %s", validationBaseErrorMessage, getExpectedInputParametersMessage()));
        Ensure.require(input.length >= 1, String.format("%s - found: no arguments, expected: %s", validationBaseErrorMessage, getExpectedInputParametersMessage()));
    }


    /**
     * Returns a description of expected input parameters of the operation. This is used only for providing meaningful
     * exceptions.
     *
     * @return description of expected input parameters of the operation
     */
    protected abstract String getExpectedInputParametersMessage();


    @Override
    public abstract OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException;

}
