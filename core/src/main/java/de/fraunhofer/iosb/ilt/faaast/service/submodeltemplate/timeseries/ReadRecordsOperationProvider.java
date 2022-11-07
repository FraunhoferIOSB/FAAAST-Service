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
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Provides an implementation for SMT TimeSeries ReadRecords operation.
 */
public class ReadRecordsOperationProvider extends AbstractTimeSeriesOperationProvider {

    public ReadRecordsOperationProvider(Submodel submodel) {
        super(Constants.READ_RECORDS_ID_SHORT, submodel);
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
        validateInputParameters(input);
        Timespan timespan = getTimespanFromInput(input, Constants.READ_RECORDS_INPUT_TIMESPAN_ID_SHORT);
        List<SubmodelElementCollection> internalSegments = getSegments(true, false, false);
        List<SubmodelElementCollection> allRecords = internalSegments.stream()
                .flatMap(x -> getRecords(x).stream())
                .collect(Collectors.toList());
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


    @Override
    protected String getExpectedInputParametersMessage() {
        return String.format("%s [%s, valueType: %s]",
                Constants.READ_RECORDS_INPUT_TIMESPAN_ID_SHORT,
                Range.class.getSimpleName(),
                Datatype.LONG.getName());
    }
}
