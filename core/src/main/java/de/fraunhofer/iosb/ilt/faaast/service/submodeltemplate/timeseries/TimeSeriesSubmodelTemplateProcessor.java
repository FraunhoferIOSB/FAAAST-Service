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

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.LambdaAssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.config.LambdaOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.SubmodelTemplateProcessor;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.SegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.util.AasHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.Operation;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.impl.DefaultOperation;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import io.adminshell.aas.v3.model.impl.DefaultRange;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Adds logic for submodel instances of template TimeSeries.
 */
public class TimeSeriesSubmodelTemplateProcessor implements SubmodelTemplateProcessor<TimeSeriesSubmodelTemplateProcessorConfig> {

    private TimeSeriesSubmodelTemplateProcessorConfig config;

    private Map<String, SegmentProvider> segmentProviders;

    @Override
    public boolean accept(Submodel submodel) {
        return submodel != null
                && Objects.equals(ReferenceHelper.globalReference(Constants.TIMESERIES_SUBMODEL_SEMANTIC_ID), submodel.getSemanticId());
    }


    @Override
    public TimeSeriesSubmodelTemplateProcessorConfig asConfig() {
        return config;
    }


    @Override
    public void init(CoreConfig coreConfig, TimeSeriesSubmodelTemplateProcessorConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
        segmentProviders = new HashMap<>();
        config.getSegmentProviders().forEach(x -> {
            try {
                segmentProviders.put(x.getEndpoint(), (SegmentProvider) x.newInstance(coreConfig, serviceContext));
            }
            catch (ConfigurationException e) {
                throw new RuntimeException(e);
            }
        });
    }


    private Operation newReadSegmentsOperation() {
        return new DefaultOperation.Builder()
                .idShort(Constants.READ_SEGMENTS_ID_SHORT)
                .semanticId(ReferenceHelper.globalReference(Constants.READ_SEGMENTS_SEMANTIC_ID))
                .inputVariable(new DefaultOperationVariable.Builder()
                        .value(new DefaultRange.Builder()
                                .idShort(Constants.READ_SEGMENTS_INPUT_TIMESPAN_ID_SHORT)
                                .displayName(new LangString("Zeitspanne@de"))
                                .displayName(new LangString("Timespan@en"))
                                .description(new LangString("Der valueType der übergebenen Zeitspanne muss mit dem valueType der Time Properties der Segemente übereinstimmen@de"))
                                .description(new LangString("The valueType of the given timespan must match the valueType of the time properties of the Segments@en"))
                                .valueType(Datatype.STRING.getName())
                                .build())
                        .build())
                .outputVariable(new DefaultOperationVariable.Builder()
                        .value(new DefaultSubmodelElementCollection.Builder()
                                .idShort(Constants.READ_SEGMENTS_OUTPUT_SEGMENTS_ID_SHORT)
                                .displayName(new LangString("Segments@de"))
                                .displayName(new LangString("Segments@en"))
                                .description(new LangString("Segmente, die sich zumindest teilweise mit der übergebenen Periode überschneiden@de"))
                                .description(new LangString("Segments that at least partially overlap with the@en"))
                                .build())
                        .build())
                .build();
    }


    private Operation newReadRecordsOperation() {
        return new DefaultOperation.Builder()
                .idShort(Constants.READ_RECORDS_ID_SHORT)
                .semanticId(ReferenceHelper.globalReference(Constants.READ_RECORDS_SEMANTIC_ID))
                .inputVariable(new DefaultOperationVariable.Builder()
                        .value(new DefaultRange.Builder()
                                .idShort(Constants.READ_RECORDS_INPUT_TIMESPAN_ID_SHORT)
                                .displayName(new LangString("Zeitspanne@de"))
                                .displayName(new LangString("Timespan@en"))
                                .description(new LangString("Der valueType der übergebenen Zeitspanne muss mit dem valueType der Time Properties der Records übereinstimmen@de"))
                                .description(new LangString("The valueType of the given timespan must match the valueType of the time properties of the Records@en"))
                                .valueType(Datatype.STRING.getName())
                                .build())
                        .build())
                .outputVariable(new DefaultOperationVariable.Builder()
                        .value(new DefaultSubmodelElementCollection.Builder()
                                .idShort(Constants.READ_RECORDS_OUTPUT_RECORDS_ID_SHORT)
                                .displayName(new LangString("Records@de"))
                                .displayName(new LangString("Records@en"))
                                .description(new LangString("Records, die innerhalb der übergebenen Zeitspanne liegen@de"))
                                .description(new LangString("Segments that at least partially overlap with the passed period@en"))
                                .build())
                        .build())
                .build();
    }


    private Operation getOrCreateReadSegmentsOperation(Submodel submodel) {
        Operation result = AasHelper.getElementByIdShort(submodel.getSubmodelElements(), Constants.READ_SEGMENTS_ID_SHORT, Operation.class);
        if (result == null) {
            result = newReadSegmentsOperation();
            submodel.getSubmodelElements().add(result);
        }
        return result;
    }


    private Operation getOrCreateReadRecordsOperation(Submodel submodel) {
        Operation result = AasHelper.getElementByIdShort(submodel.getSubmodelElements(), Constants.READ_RECORDS_ID_SHORT, Operation.class);
        if (result == null) {
            result = newReadRecordsOperation();
            submodel.getSubmodelElements().add(result);
        }
        return result;
    }


    private LambdaAssetConnection getOrCreateLambdaAssetConnection(AssetConnectionManager assetConnectionManager) {
        return assetConnectionManager.getConnections().stream()
                .filter(x -> LambdaAssetConnection.class.isAssignableFrom(x.getClass()))
                .map(x -> (LambdaAssetConnection) x)
                .findFirst()
                .orElseGet(() -> {
                    LambdaAssetConnection result = new LambdaAssetConnection();
                    assetConnectionManager.getConnections().add(result);
                    return result;
                });
    }


    private void handleOperation(
                                 Submodel submodel,
                                 AssetConnectionManager assetConnectionManager,
                                 LambdaAssetConnection assetConnection,
                                 Supplier<Operation> operationSupplier,
                                 Supplier<LambdaOperationProviderConfig> operationProviderConfigSupplier)
            throws AssetConnectionException {
        Operation operation = operationSupplier.get();
        Reference reference = AasUtils.toReference(AasUtils.toReference(submodel), operation);
        if (!assetConnectionManager.hasOperationProvider(reference)) {
            assetConnection.registerOperationProvider(reference, operationProviderConfigSupplier.get());
        }
    }


    @Override
    public boolean process(Submodel submodel, AssetConnectionManager assetConnectionManager) {
        LambdaAssetConnection assetConnection = getOrCreateLambdaAssetConnection(assetConnectionManager);
        try {
            handleOperation(submodel, assetConnectionManager, assetConnection,
                    () -> getOrCreateReadSegmentsOperation(submodel),
                    () -> LambdaOperationProviderConfig.builder()
                            .implementation(new ReadSegmentsOperationProvider(submodel, config.isUseSegmentTimestamps()))
                            .build());
            handleOperation(submodel, assetConnectionManager, assetConnection,
                    () -> getOrCreateReadRecordsOperation(submodel),
                    () -> LambdaOperationProviderConfig.builder()
                            .implementation(new ReadRecordsOperationProvider(submodel, segmentProviders))
                            .build());
            return true;
        }
        catch (AssetConnectionException ex) {
            Logger.getLogger(TimeSeriesSubmodelTemplateProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

}
