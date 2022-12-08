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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.AbstractLambdaOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Metadata;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.TimeSeries;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Timespan;
import de.fraunhofer.iosb.ilt.faaast.service.util.AasHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Abstract base class for SMT TimeSeries operation provider implementations.
 */
public abstract class AbstractTimeSeriesOperationProvider extends AbstractLambdaOperationProvider {

    protected final Reference submodelRef;
    protected final String validationBaseErrorMessage;

    protected AbstractTimeSeriesOperationProvider(String name, Reference submodelRef) {
        Ensure.requireNonNull(name, "name must be non-null");
        Ensure.requireNonNull(submodelRef, "submodelRef must be non-null");
        this.submodelRef = submodelRef;
        this.validationBaseErrorMessage = String.format("error validating input parameter(s) for operation '%s'", name);
    }


    /**
     * Loads the related time series. The TimeSeries might change over time, therefore it should be reloaded for each
     * operation call.
     *
     * @return the time series
     * @throws de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException if parsing values fails
     */
    protected TimeSeries loadTimeSeries() throws ValueFormatException {
        return TimeSeries.of(AasUtils.resolve(submodelRef, serviceContext.getAASEnvironment(), Submodel.class));
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
     * Gets the metadata element of the underlying submodel.
     *
     * @return the metadata element
     * @throws de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException if parsing values fails
     */
    protected Metadata getMetadata() throws ValueFormatException {
        SubmodelElementCollection metadata = AasHelper.getElementByIdShort(loadTimeSeries().getSubmodelElements(), Constants.TIMESERIES_METADATA_ID_SHORT,
                SubmodelElementCollection.class);
        return Metadata.builder()
                .recordMetadata(AasHelper.getElementByIdShort(metadata.getValues(), Constants.METADATA_RECORD_METADATA_ID_SHORT, SubmodelElementCollection.class)
                        .getValues().stream()
                        .collect(Collectors.toMap(
                                x -> x.getIdShort(),
                                x -> Datatype.fromName(((Property) x).getValueType()))))
                .build();
    }


    /**
     * Gets the Segments collection of the SMT TimeSeries submodel.
     *
     * @return the Segments collection
     * @throws de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException if parsing values fails
     */
    protected SubmodelElementCollection getSegmentsCollection() throws ValueFormatException {
        return AasHelper.getElementByIdShort(loadTimeSeries().getSubmodelElements(), Constants.TIMESERIES_SEGMENTS_ID_SHORT, SubmodelElementCollection.class);
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
     * Searches given set of submodel elements for element with given idShort recursive.
     *
     * @param <T> type of submodel element
     * @param collection submodel elements to search
     * @param idShort idShort of element to find
     * @param clazz type of element to find
     * @return found element or null if not present
     */
    protected <T extends SubmodelElement> T findRecursive(Collection<SubmodelElement> collection, String idShort, Class<T> clazz) {
        for (SubmodelElement submodelElement: collection) {
            if (SubmodelElementCollection.class.isAssignableFrom(submodelElement.getClass())) {
                T result = findRecursive(((SubmodelElementCollection) submodelElement).getValues(), idShort, clazz);
                if (result != null) {
                    return result;
                }
            }
            if (submodelElement.getIdShort().equalsIgnoreCase(idShort) && clazz.isAssignableFrom(submodelElement.getClass())) {
                return (T) submodelElement;
            }
        }
        return null;
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
        if (!Datatype.DATE_TIME.getName().equals(timespan.getValueType())) {
            throw new IllegalArgumentException(String.format("%s - required valuedType for property with idShort '%s' is '%s' but found '%s'",
                    validationBaseErrorMessage,
                    idShort,
                    Datatype.DATE_TIME.getName(),
                    timespan.getValueType()));
        }
        return Timespan.fromString(timespan.getMin(), timespan.getMax());
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
