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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.SubmodelTemplateProcessor;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.helper.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.helper.InterfaceData;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.helper.InterfaceDataHttp;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.helper.InterfaceDataMqtt;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.helper.MqttHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.RelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Adds logic for submodel instances of template Asset Interfaces Mapping Configuration.
 */
public class AimcSubmodelTemplateProcessor implements SubmodelTemplateProcessor<AimcSubmodelTemplateProcessorConfig> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AimcSubmodelTemplateProcessor.class);

    private AimcSubmodelTemplateProcessorConfig config;
    private ServiceContext serviceContext;
    private Submodel aimcSubmodel;
    private Map<Reference, InterfaceData> providerData;

    public AimcSubmodelTemplateProcessor() {
        providerData = new HashMap<>();
    }


    @Override
    public boolean accept(Submodel submodel) {
        return submodel != null
                && (Objects.equals(ReferenceBuilder.global(Constants.AIMC_SUBMODEL_SEMANTIC_ID), submodel.getSemanticId())
                        || Objects.equals(ReferenceBuilder.global(Constants.AID_SUBMODEL_SEMANTIC_ID), submodel.getSemanticId()));
    }


    @Override
    public boolean add(Submodel submodel, AssetConnectionManager assetConnectionManager) {
        Ensure.requireNonNull(submodel);
        Ensure.requireNonNull(assetConnectionManager);
        boolean retval;
        try {
            // in add we only process AIMC submodel
            if (Objects.equals(ReferenceBuilder.global(Constants.AIMC_SUBMODEL_SEMANTIC_ID), submodel.getSemanticId())) {
                LOGGER.atInfo().log("process submodel {} ({})", submodel.getIdShort(), AasUtils.asString(AasUtils.toReference(submodel)));
                processSubmodel(submodel, assetConnectionManager, ProcessingMode.ADD);
                aimcSubmodel = submodel;
            }

            retval = true;
        }
        catch (Exception ex) {
            LOGGER.error("error processing SMT AIMC (submodel: {})", AasUtils.asString(AasUtils.toReference(submodel)), ex);
            retval = false;
        }

        return retval;
    }


    @Override
    public void init(CoreConfig coreConfig, AimcSubmodelTemplateProcessorConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
        this.serviceContext = serviceContext;
    }


    @Override
    public AimcSubmodelTemplateProcessorConfig asConfig() {
        return config;
    }


    @Override
    public boolean update(Submodel submodel, AssetConnectionManager assetConnectionManager) {
        Ensure.requireNonNull(submodel);
        Ensure.requireNonNull(assetConnectionManager);
        boolean retval;

        try {
            LOGGER.atInfo().log("update submodel {} ({})", submodel.getIdShort(), AasUtils.asString(AasUtils.toReference(submodel)));
            Submodel updateSubmodel = submodel;
            // we always use AIMC submodel for processSubmodel
            if (Objects.equals(ReferenceBuilder.global(Constants.AID_SUBMODEL_SEMANTIC_ID), submodel.getSemanticId())) {
                updateSubmodel = aimcSubmodel;
            }
            processSubmodel(updateSubmodel, assetConnectionManager, ProcessingMode.UPDATE);
            retval = true;
        }
        catch (Exception ex) {
            LOGGER.error("error updating SMT AIMC (submodel: {})", AasUtils.asString(AasUtils.toReference(submodel)), ex);
            retval = false;
        }
        return retval;
    }


    @Override
    public boolean delete(Submodel submodel, AssetConnectionManager assetConnectionManager) {
        Ensure.requireNonNull(submodel);
        Ensure.requireNonNull(assetConnectionManager);
        boolean retval;
        try {
            // in delete we only process AIMC submodel
            if (Objects.equals(ReferenceBuilder.global(Constants.AIMC_SUBMODEL_SEMANTIC_ID), submodel.getSemanticId())) {
                LOGGER.atInfo().log("delete submodel {} ({})", submodel.getIdShort(), AasUtils.asString(AasUtils.toReference(submodel)));
                processSubmodel(submodel, assetConnectionManager, ProcessingMode.DELETE);
            }
            retval = true;
        }
        catch (Exception ex) {
            LOGGER.error("error deleting SMT AIMC (submodel: {})", AasUtils.asString(AasUtils.toReference(submodel)), ex);
            retval = false;
        }
        return retval;
    }


    private void processSubmodel(Submodel submodel, AssetConnectionManager assetConnectionManager, ProcessingMode mode)
            throws PersistenceException, ResourceNotFoundException, MalformedURLException, ConfigurationException, AssetConnectionException, URISyntaxException {
        SubmodelElementList mappingConfigurations = getMappingConfiguration(submodel);
        for (var c: mappingConfigurations.getValue()) {
            if (c instanceof SubmodelElementCollection configuration) {
                processConfiguration(configuration, assetConnectionManager, mode);
            }
            else {
                LOGGER.debug("processSubmodel: element {} not a Collection", c);
            }
        }
    }


    private void processConfiguration(SubmodelElementCollection configuration, AssetConnectionManager assetConnectionManager, ProcessingMode mode)
            throws PersistenceException, ResourceNotFoundException, MalformedURLException, ConfigurationException, AssetConnectionException, URISyntaxException {
        List<RelationshipElement> relations = getMappingRelations(configuration);
        processInterfaceReference(configuration, relations, assetConnectionManager, mode);
    }


    private void processInterfaceReference(SubmodelElementCollection configuration, List<RelationshipElement> relations, AssetConnectionManager assetConnectionManager,
                                           ProcessingMode mode)
            throws ResourceNotFoundException, ConfigurationException, PersistenceException, MalformedURLException, AssetConnectionException, IllegalArgumentException,
            URISyntaxException {
        Reference interfaceReferenceValue = getInterfaceReference(configuration).getValue();
        Referable referenceElement = EnvironmentHelper.resolve(interfaceReferenceValue, serviceContext.getAASEnvironment());
        if (referenceElement instanceof SubmodelElementCollection assetInterface) {
            if ((ReferenceBuilder.global(Constants.AID_INTERFACE_SEMANTIC_ID).equals(assetInterface.getSemanticId()))
                    && (assetInterface.getSupplementalSemanticIds() != null)) {
                AimcSubmodelTemplateProcessorConfigData configData;
                if (ReferenceHelper.containsSameReference(config.getInterfaceConfigurations(), interfaceReferenceValue)) {
                    configData = ReferenceHelper.getValueBySameReference(config.getInterfaceConfigurations(), interfaceReferenceValue);
                }
                else {
                    // if no configuration data is given, we create a new one with default values.
                    LOGGER.atDebug().log("processInterfaceReference: no config section found for Interface {}", ReferenceHelper.asString(interfaceReferenceValue));
                    configData = new AimcSubmodelTemplateProcessorConfigData();
                }
                InterfaceData interfaceData = null;
                if (ReferenceHelper.containsSameReference(providerData, interfaceReferenceValue)) {
                    interfaceData = ReferenceHelper.getValueBySameReference(providerData, interfaceReferenceValue);
                }

                if (assetInterface.getSupplementalSemanticIds().contains(ReferenceBuilder.global(Constants.AID_INTERFACE_SUPP_SEMANTIC_ID_HTTP))) {
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
                        http = new InterfaceDataHttp(configData);
                    }
                    HttpHelper.processInterface(serviceContext, http, assetInterface, relations, assetConnectionManager, mode);
                    providerData.put(interfaceReferenceValue, http);
                }
                else if (assetInterface.getSupplementalSemanticIds().contains(ReferenceBuilder.global(Constants.AID_INTERFACE_SUPP_SEMANTIC_ID_MQTT))) {
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
                        mqtt = new InterfaceDataMqtt(configData);
                    }
                    MqttHelper.processInterface(serviceContext, mqtt, assetInterface, relations, assetConnectionManager, mode);
                    providerData.put(interfaceReferenceValue, mqtt);
                }
            }
        }
        else {
            LOGGER.debug("processInterfaceReference: Interface not a SubmodelElementCollection");
        }
    }


    private static List<RelationshipElement> getRelationshipElements(List<SubmodelElement> relations) {
        List<RelationshipElement> retval = new ArrayList<>();
        for (var r: relations) {
            if (r instanceof RelationshipElement relationshipElement) {
                retval.add(relationshipElement);
            }
        }

        return retval;
    }


    private static SubmodelElementList getMappingConfiguration(Submodel submodel) {
        Optional<SubmodelElement> element = submodel.getSubmodelElements().stream().filter(s -> Constants.AIMC_MAPPING_CONFIGURATIONS.equals(s.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel invalid: MappingConfigurations not found.");
        }
        if (element.get() instanceof SubmodelElementList list) {
            return list;
        }
        else {
            throw new IllegalArgumentException("Submodel invalid: MappingConfigurations not a list.");
        }
    }


    private static List<RelationshipElement> getMappingRelations(SubmodelElementCollection configuration) {
        Optional<SubmodelElement> element = configuration.getValue().stream().filter(e -> Constants.AIMC_MAPPING_RELATIONS.equals(e.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel invalid: MappingSourceSinkRelations not found.");
        }
        List<RelationshipElement> relations = null;
        if (element.get() instanceof SubmodelElementList list) {
            relations = getRelationshipElements(list.getValue());
        }
        if (relations == null) {
            relations = new ArrayList<>();
        }
        return relations;
    }


    private static ReferenceElement getInterfaceReference(SubmodelElementCollection configuration) {
        Optional<SubmodelElement> element = configuration.getValue().stream().filter(e -> Constants.AIMC_INTERFACE_REFERENCE.equals(e.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel invalid: InterfaceReference not found.");
        }
        if (element.get() instanceof ReferenceElement interfaceReference) {
            return interfaceReference;
        }
        else {
            throw new IllegalArgumentException("Submodel invalid: InterfaceReference not a ReferenceElement.");
        }
    }
}
