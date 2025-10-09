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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.SemanticIdPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.GetSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.GetSubmodelByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.SubmodelTemplateProcessor;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config.AimcSubmodelTemplateProcessorConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config.InterfaceConfiguration;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.model.InterfaceData;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.model.InterfaceDataHttp;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.model.InterfaceDataMqtt;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.model.RelationData;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.util.MqttHelper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.util.Util;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.RelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Adds logic for submodel instances of template Asset Interfaces Mapping Configuration.
 */
public class AimcSubmodelTemplateProcessor implements SubmodelTemplateProcessor<AimcSubmodelTemplateProcessorConfig> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AimcSubmodelTemplateProcessor.class);

    private AimcSubmodelTemplateProcessorConfig config;
    private ServiceContext serviceContext;
    // keeps track of relations between AID and AIMC submodels as changes to an AID submodel needs to trigger 
    // an update on all AIMC refering to/using that AID submodel.
    private Map<String, Set<String>> aidToAimcRelations;

    private Map<String, List<AssetConnectionConfig>> connectionsCurrent;

    public AimcSubmodelTemplateProcessor() {
        aidToAimcRelations = new HashMap<>();
        connectionsCurrent = new HashMap<>();
    }


    @Override
    public AimcSubmodelTemplateProcessorConfig asConfig() {
        return config;
    }


    @Override
    public void init(CoreConfig coreConfig, AimcSubmodelTemplateProcessorConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
        this.serviceContext = serviceContext;
    }


    @Override
    public boolean accept(Submodel submodel) {
        return Objects.nonNull(submodel)
                && (Util.semanticIdEquals(submodel, Constants.AIMC_SUBMODEL_SEMANTIC_ID)
                        || Util.semanticIdEquals(submodel, Constants.AID_SUBMODEL_SEMANTIC_ID));
    }


    @Override
    public boolean add(Submodel submodel, AssetConnectionManager assetConnectionManager) {
        Ensure.requireNonNull(submodel);
        Ensure.requireNonNull(assetConnectionManager);
        if (Util.semanticIdEquals(submodel, Constants.AID_SUBMODEL_SEMANTIC_ID)) {
            handleAidChange(submodel, assetConnectionManager, ProcessingMode.ADD);
            return false;
        }
        updateAidToAimcRelations(submodel, ProcessingMode.ADD);
        try {
            LOGGER.info("process submodel {} ({})", submodel.getIdShort(), ReferenceHelper.asString(ReferenceBuilder.forSubmodel(submodel)));
            processSubmodel(submodel, assetConnectionManager, ProcessingMode.ADD);
        }
        catch (Exception e) {
            LOGGER.error("error processing SMT AIMC (submodel: {})", ReferenceHelper.asString(ReferenceBuilder.forSubmodel(submodel)), e);
        }
        return false;
    }


    @Override
    public boolean update(Submodel submodel, AssetConnectionManager assetConnectionManager) {
        Ensure.requireNonNull(submodel);
        Ensure.requireNonNull(assetConnectionManager);
        if (Util.semanticIdEquals(submodel, Constants.AID_SUBMODEL_SEMANTIC_ID)) {
            handleAidChange(submodel, assetConnectionManager, ProcessingMode.UPDATE);
            return false;
        }
        updateAidToAimcRelations(submodel, ProcessingMode.UPDATE);
        try {
            LOGGER.info("update submodel {} ({})", submodel.getIdShort(), ReferenceHelper.asString(ReferenceBuilder.forSubmodel(submodel)));
            processSubmodel(submodel, assetConnectionManager, ProcessingMode.UPDATE);
        }
        catch (Exception e) {
            LOGGER.error("error updating SMT AIMC (submodel: {})", ReferenceHelper.asString(ReferenceBuilder.forSubmodel(submodel)), e);
        }
        return false;
    }


    @Override
    public boolean delete(Submodel submodel, AssetConnectionManager assetConnectionManager) {
        Ensure.requireNonNull(submodel);
        Ensure.requireNonNull(assetConnectionManager);
        if (Util.semanticIdEquals(submodel, Constants.AID_SUBMODEL_SEMANTIC_ID)) {
            handleAidChange(submodel, assetConnectionManager, ProcessingMode.DELETE);
            return false;
        }
        updateAidToAimcRelations(submodel, ProcessingMode.DELETE);
        try {
            LOGGER.info("delete submodel {} ({})", submodel.getIdShort(), ReferenceHelper.asString(ReferenceBuilder.forSubmodel(submodel)));
            processSubmodel(submodel, assetConnectionManager, ProcessingMode.DELETE);
        }
        catch (Exception e) {
            LOGGER.error("error deleting SMT AIMC (submodel: {})", ReferenceHelper.asString(ReferenceBuilder.forSubmodel(submodel)), e);
        }
        return false;
    }


    private void handleAidChange(Submodel submodel, AssetConnectionManager assetConnectionManager, ProcessingMode mode) {
        String aidSubmodelId = submodel.getId();
        if (!aidToAimcRelations.containsKey(aidSubmodelId)) {
            if (mode == ProcessingMode.ADD) {
                aidToAimcRelations.put(aidSubmodelId, new HashSet<>());
            }
            return;
        }
        aidToAimcRelations.get(aidSubmodelId).forEach(x -> {
            try {
                // handling too generic?
                // e.g. when add AID, we would know to only certain things, no delete, no update
                processSubmodel(getSubmodel(x), assetConnectionManager, mode);
            }
            catch (Exception e) {
                LOGGER.warn("Failed to update AIMC submodel (submodelId: {})", x);
            }
        });
    }


    private void updateAidToAimcRelations(Submodel submodel, ProcessingMode mode) {
        List<String> aidSubmodelIds = SemanticIdPath.builder()
                .globalReference(Constants.AIMC_MAPPING_CONFIGURATIONS_SEMANTIC_ID)
                .globalReference(Constants.AIMC_CONFIGURATION_SEMANTIC_ID)
                .globalReference(Constants.AIMC_INTERFACE_REFERENCE_SEMANTIC_ID)
                .build()
                .resolve(submodel, ReferenceElement.class)
                .stream()
                .map(ReferenceElement::getValue)
                .map(SubmodelElementIdentifier::fromReference)
                .map(SubmodelElementIdentifier::getSubmodelId).toList();
        String aimcSubmodelId = submodel.getId();
        aidSubmodelIds.forEach(x -> {
            if (mode == ProcessingMode.DELETE || mode == ProcessingMode.UPDATE) {
                if (aidToAimcRelations.containsKey(x)) {
                    aidToAimcRelations.get(x).remove(aimcSubmodelId);
                }
            }
            if (mode == ProcessingMode.ADD || mode == ProcessingMode.UPDATE) {
                if (!aidToAimcRelations.containsKey(x)) {
                    aidToAimcRelations.put(x, new HashSet<>());
                }
                aidToAimcRelations.get(x).add(aimcSubmodelId);
            }
        });
    }


    private void processSubmodel(Submodel submodel, AssetConnectionManager assetConnectionManager, ProcessingMode mode)
            throws PersistenceException, ResourceNotFoundException, MalformedURLException, ConfigurationException, AssetConnectionException, URISyntaxException {
        SemanticIdPath.builder()
                .globalReference(Constants.AIMC_MAPPING_CONFIGURATIONS_SEMANTIC_ID)
                .globalReference(Constants.AIMC_CONFIGURATION_SEMANTIC_ID)
                .build()
                .resolve(submodel, SubmodelElementCollection.class)
                .forEach(x -> processConfiguration(x, assetConnectionManager, mode));
    }


    private void processConfiguration(SubmodelElementCollection configuration, AssetConnectionManager assetConnectionManager, ProcessingMode mode)
            throws PersistenceException, ResourceNotFoundException, MalformedURLException, ConfigurationException, AssetConnectionException, URISyntaxException {
        List<RelationshipElement> relations = SemanticIdPath.builder()
                .globalReference(Constants.AIMC_MAPPING_RELATIONS_SEMANTIC_ID)
                .globalReference(Constants.AIMC_MAPPING_RELATION_SEMANTIC_ID)
                .build()
                .resolve(configuration, RelationshipElement.class);
        processInterfaceReference(configuration, relations, assetConnectionManager, mode);
    }


    private Referable getElement(Reference reference) {
        SubmodelElementIdentifier identifier = SubmodelElementIdentifier.fromReference(reference);
        // need to make sure serviceContext.execute already ready to receive requests!
        return ((GetSubmodelElementByPathResponse) serviceContext.execute(GetSubmodelElementByPathRequest.builder()
                .internal()
                .submodelId(identifier.getSubmodelId())
                .path(identifier.getIdShortPath().toString())
                .build())
                .getResult()).getPayload();
    }


    private Submodel getSubmodel(String submodelId) {
        return ((GetSubmodelByIdResponse) serviceContext.execute(GetSubmodelByIdRequest.builder()
                .internal()
                .id(submodelId)
                .build())
                .getResult()).getPayload();
    }


    private void processInterfaceReference(SubmodelElementCollection configuration, List<RelationshipElement> relations, AssetConnectionManager assetConnectionManager,
                                           ProcessingMode mode)
            throws ResourceNotFoundException, ConfigurationException, PersistenceException, MalformedURLException, AssetConnectionException, IllegalArgumentException,
            URISyntaxException {
        Reference interfaceReferenceValue = SemanticIdPath.builder()
                .globalReference(Constants.AIMC_INTERFACE_REFERENCE_SEMANTIC_ID)
                .build()
                .resolveUnique(configuration, ReferenceElement.class)
                .getValue();
        Referable referenceElement = getElement(interfaceReferenceValue);
        if (!(referenceElement instanceof SubmodelElementCollection)) {
            LOGGER.warn("Invalid AIMC configuration - target of InterfaceReference is not of type SubmodelElementCollection");
            return;
        }
        SubmodelElementCollection assetInterface = (SubmodelElementCollection) referenceElement;
        if (!Util.semanticIdEquals(assetInterface, Constants.AID_INTERFACE_SEMANTIC_ID)) {
            LOGGER.warn("Invalid AIMC configuration - target of InterfaceReference does not have correct semanticId (expected: {}, actual: {})",
                    Constants.AID_INTERFACE_SEMANTIC_ID,
                    ReferenceHelper.asString(assetInterface.getSemanticId()));
            return;
        }
        if (Objects.isNull(assetInterface.getSupplementalSemanticIds()) || assetInterface.getSupplementalSemanticIds().isEmpty()) {
            LOGGER.warn("Invalid AIMC configuration - target of InterfaceReference does not have any supplementalSemanticId, but at least one is required",
                    Constants.AID_INTERFACE_SEMANTIC_ID,
                    ReferenceHelper.asString(assetInterface.getSemanticId()));
            return;
        }

        InterfaceConfiguration interfaceConfig = ReferenceHelper.containsSameReference(config.getInterfaceConfigurations(), interfaceReferenceValue)
                ? ReferenceHelper.getValueBySameReference(config.getInterfaceConfigurations(), interfaceReferenceValue)
                : InterfaceConfiguration.builder()
                        .build();
        InterfaceData interfaceData = ReferenceHelper.containsSameReference(interfaceDataCache, interfaceReferenceValue)
                ? ReferenceHelper.getValueBySameReference(interfaceDataCache, interfaceReferenceValue)
                : null;

        if (Util.containsSupplementalSemanticId(assetInterface, Constants.AID_INTERFACE_SUPP_SEMANTIC_ID_HTTP)) {
            // HTTP Interface
            InterfaceDataHttp http;
            if (interfaceData != null) {
                if (interfaceData instanceof InterfaceDataHttp httpInterface) {
                    http = httpInterface;
                }
                else {
                    throw new IllegalArgumentException("wrong type: no InterfaceDataHttp");
                }
            }
            else {
                http = new InterfaceDataHttp(interfaceConfig);
            }
            HttpHelper.processInterface(serviceContext, http, assetInterface, relations, assetConnectionManager, mode);
            interfaceDataCache.put(interfaceReferenceValue, http);
        }
        else if (Util.containsSupplementalSemanticId(assetInterface, Constants.AID_INTERFACE_SUPP_SEMANTIC_ID_MQTT)) {
            // MQTT Interface
            InterfaceDataMqtt mqtt;
            if (interfaceData != null) {
                if (interfaceData instanceof InterfaceDataMqtt mqttInterface) {
                    mqtt = mqttInterface;
                }
                else {
                    throw new IllegalArgumentException("wrong type: no InterfaceDataHttp");
                }
            }
            else {
                mqtt = new InterfaceDataMqtt(interfaceConfig);
            }
            MqttHelper.processInterface(serviceContext, mqtt, assetInterface, relations, assetConnectionManager, mode);
            interfaceDataCache.put(interfaceReferenceValue, mqtt);
        }
    }


    private void processHttpMaping() {
        InterfaceDataHttp http;
        if (interfaceData != null) {
            if (interfaceData instanceof InterfaceDataHttp httpInterface) {
                http = httpInterface;
            }
            else {
                throw new IllegalArgumentException("wrong type: no InterfaceDataHttp");
            }
        }
        else {
            http = new InterfaceDataHttp(configData);
        }
        //HttpHelper.processInterface(serviceContext, http, assetInterface, relations, assetConnectionManager, mode);
        String title = Util.getInterfaceTitle(assetInterface);
        LOGGER.debug("process HTTP interface {} with {} relations", title, relations.size());

        // Endpoint Metadata
        SubmodelElementCollection metadata = Util.getEndpointMetadata(assetInterface);
        String base = Util.getBaseUrl(metadata);

        // contentType
        String contentType = Util.getContentType(metadata);

        List<AssetConnection> assetConnectionsRemove = new ArrayList<>();
        Map<Reference, HttpValueProviderConfig> valueProviders = new HashMap<>();
        Map<Reference, HttpSubscriptionProviderConfig> subscriptionProviders = new HashMap<>();
        if ((mode == ProcessingMode.UPDATE) || (mode == ProcessingMode.DELETE)) {
            updateAssetConnections(assetConnectionManager, base, mode, new RelationData(serviceContext, relations, contentType, interfaceData), subscriptionProviders,
                    valueProviders,
                    assetConnectionsRemove);
        }
        else if (mode == ProcessingMode.ADD) {
            addProvider(new RelationData(serviceContext, relations, contentType, interfaceData), base, subscriptionProviders, valueProviders, assetConnectionManager);
        }

        if (!(subscriptionProviders.isEmpty() && valueProviders.isEmpty())) {
            registerProviders(valueProviders, subscriptionProviders, assetConnectionManager, base, metadata, serviceContext, interfaceData);
        }
        if (!assetConnectionsRemove.isEmpty()) {
            // remove asset connection if no more providers are available
            LOGGER.debug("processInterface: remove unused AssetConnections");
            for (var connection: assetConnectionsRemove) {
                assetConnectionManager.remove(connection);
            }
            assetConnectionsRemove.clear();
        }
        interfaceDataCache.put(interfaceReferenceValue, http);
    }

}
