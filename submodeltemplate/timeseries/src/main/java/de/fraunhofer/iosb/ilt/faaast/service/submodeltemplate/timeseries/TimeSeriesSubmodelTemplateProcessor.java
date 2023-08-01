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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.config.LambdaOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.SubmodelTemplateProcessor;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.ExternalSegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.LinkedSegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.SegmentProvider;
import de.fraunhofer.iosb.ilt.faaast.service.util.AasHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Adds logic for submodel instances of template TimeSeries.
 */
public class TimeSeriesSubmodelTemplateProcessor implements SubmodelTemplateProcessor<TimeSeriesSubmodelTemplateProcessorConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesSubmodelTemplateProcessor.class);
    private TimeSeriesSubmodelTemplateProcessorConfig config;

    private Map<String, LinkedSegmentProvider> linkedSegmentProviders;
    private Map<String, ExternalSegmentProvider> externalSegmentProviders;
    private SegmentProvider internalSegmentProvider;

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
        linkedSegmentProviders = new HashMap<>();
        externalSegmentProviders = new HashMap<>();
        try {
            config.getLinkedSegmentProviders()
                    .forEach(LambdaExceptionHelper
                            .rethrowConsumer(x -> linkedSegmentProviders.put(x.getEndpoint(), (LinkedSegmentProvider) x.newInstance(coreConfig, serviceContext))));
            config.getExternalSegmentProviders()
                    .forEach(LambdaExceptionHelper
                            .rethrowConsumer(x -> externalSegmentProviders.put(x.toString(), (ExternalSegmentProvider) x.newInstance(coreConfig, serviceContext)))); //TODO: change toString 
            internalSegmentProvider = (SegmentProvider) config.getInternalSegmentProvider().newInstance(coreConfig, serviceContext);

        }
        catch (ConfigurationException e) {
            throw new ConfigurationInitializationException(e);
        }
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


    private void handleOperation(
                                 Submodel submodel,
                                 AssetConnectionManager assetConnectionManager,
                                 Supplier<Operation> operationSupplier,
                                 Supplier<LambdaOperationProviderConfig> operationProviderConfigSupplier)
            throws AssetConnectionException {
        Operation operation = operationSupplier.get();
        Reference reference = AasUtils.toReference(AasUtils.toReference(submodel), operation);
        if (!assetConnectionManager.hasOperationProvider(reference)) {
            assetConnectionManager.getLambdaAssetConnection().registerOperationProvider(reference, operationProviderConfigSupplier.get());
        }
    }


    @Override
    public boolean process(Submodel submodel, AssetConnectionManager assetConnectionManager) {
        try {
            handleOperation(submodel, assetConnectionManager,
                    () -> getOrCreateReadSegmentsOperation(submodel),
                    () -> LambdaOperationProviderConfig.builder()
                            .implementation(new ReadSegmentsOperationProvider(
                                    AasUtils.toReference(submodel),
                                    linkedSegmentProviders,
                                    externalSegmentProviders,
                                    internalSegmentProvider,
                                    config.isUseSegmentTimestamps()))
                            .build());
            handleOperation(submodel, assetConnectionManager,
                    () -> getOrCreateReadRecordsOperation(submodel),
                    () -> LambdaOperationProviderConfig.builder()
                            .implementation(new ReadRecordsOperationProvider(
                                    AasUtils.toReference(submodel),
                                    linkedSegmentProviders,
                                    externalSegmentProviders,
                                    internalSegmentProvider))
                            .build());
            return true;
        }
        catch (AssetConnectionException e) {
            LOGGER.trace("error processing SMT TimeSeries (submodel: {})",
                    AasUtils.asString(AasUtils.toReference(submodel)),
                    e);
            LOGGER.error("error processing SMT TimeSeries (submodel: {}, reason: {})",
                    AasUtils.asString(AasUtils.toReference(submodel)),
                    e.getMessage());
            return false;
        }
    }

}
