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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.helper;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.MqttAssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.MqttAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.config.MqttSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.AimcSubmodelTemplateProcessorConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.ProcessingMode;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.RelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class for MQTT connections.
 */
public class MqttHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttHelper.class);

    private MqttHelper() {}


    /**
     * Process a MQTT interface.
     *
     * @param serviceContext The service context.
     * @param config The current configuration.
     * @param assetInterface The desired Asset Interface.
     * @param relations The list of rekations.
     * @param assetConnectionManager The AssetConnectionManager.
     * @param mode The desired Processing Mode.
     * @throws PersistenceException if a storage error occurs.
     * @throws ResourceNotFoundException if the resource dcesn't exist..
     * @throws ConfigurationException if invalid configuration is provided.
     * @throws AssetConnectionException if there is an error in the Asset Connection.
     */
    public static void processInterfaceMqtt(ServiceContext serviceContext, AimcSubmodelTemplateProcessorConfig config, SubmodelElementCollection assetInterface,
                                            List<RelationshipElement> relations, AssetConnectionManager assetConnectionManager, ProcessingMode mode)
            throws ResourceNotFoundException, PersistenceException, ConfigurationException, AssetConnectionException {
        String title = Util.getInterfaceTitle(assetInterface);
        LOGGER.debug("process MQTT interface {} with {} relations", title, relations.size());

        // Endpoint Metadata
        SubmodelElementCollection metadata = Util.getEndpointMetadata(assetInterface);

        // base
        String base = Util.getBaseUrl(metadata);
        MqttAssetConnectionConfig.Builder assetConfigBuilder = MqttAssetConnectionConfig.builder().serverUri(base);

        // security
        Optional<SubmodelElement> element = metadata.getValue().stream().filter(e -> Constants.AID_METADATA_SECURITY.equals(e.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel AID (MQTT) invalid: EndpointMetadata security not found.");
        }
        else if (element.get() instanceof SubmodelElementList securityList) {
            assetConfigBuilder = configureSecurityMqtt(serviceContext, config, securityList, assetConfigBuilder);
        }

        // contentType
        String contentType = Util.getContentType(metadata);

        boolean removeAssetConnection = false;
        Map<Reference, MqttSubscriptionProviderConfig> subscriptionProviders = new HashMap<>();
        if ((mode == ProcessingMode.UPDATE) || (mode == ProcessingMode.DELETE)) {
            List<AssetConnection> connections = assetConnectionManager.getConnections();
            for (var c: connections) {
                if ((c instanceof MqttAssetConnection mac) && mac.asConfig().getServerUri().equals(base)) {
                    Set<Reference> currentSubscriptionProviders = mac.getSubscriptionProviders().keySet();

                    // search for removed providers
                    for (var k: currentSubscriptionProviders) {
                        if (((mode == ProcessingMode.UPDATE) && relations.stream().noneMatch(r -> r.getSecond().equals(k)))
                                || ((mode == ProcessingMode.DELETE) && relations.stream().anyMatch(r -> r.getSecond().equals(k)))) {
                            LOGGER.trace("processInterfaceMqtt: unregisterSubscriptionProvider: {}", AasUtils.asString(k));
                            mac.unregisterSubscriptionProvider(k);
                        }
                    }
                    if (mode != ProcessingMode.DELETE) {
                        updateRelationsMqtt(new RelationData(serviceContext, relations, contentType), subscriptionProviders, currentSubscriptionProviders);
                    }
                    else if (mac.getValueProviders().isEmpty() && mac.getSubscriptionProviders().isEmpty()
                            && mac.getOperationProviders().isEmpty()) {
                        removeAssetConnection = true;
                    }
                }
            }
        }
        else if (mode == ProcessingMode.ADD) {
            processRelationsMqtt(new RelationData(serviceContext, relations, contentType), subscriptionProviders);
        }

        if (!subscriptionProviders.isEmpty()) {
            LOGGER.debug("processInterfaceMqtt: add {} subscriptionProviders", subscriptionProviders.size());
            MqttAssetConnectionConfig assetConfig = assetConfigBuilder
                    .subscriptionProviders(subscriptionProviders)
                    .build();
            assetConnectionManager.add(assetConfig);
        }
        else if (removeAssetConnection) {
            // TODO: remove asset connection if mode DELETE and no more providers are available
            LOGGER.debug("processInterfaceMqtt: remove AssetConnection");
        }
    }


    private static MqttSubscriptionProviderConfig createSubscriptionProviderMqtt(SubmodelElementCollection property, String baseContentType) {
        MqttSubscriptionProviderConfig retval = null;

        SubmodelElementCollection forms = Util.getPropertyForms(property);
        String contentType = baseContentType;
        Optional<SubmodelElement> element = forms.getValue().stream().filter(e -> Constants.AID_FORMS_CONTENT_TYPE.equals(e.getIdShort())).findFirst();
        if (element.isPresent() && (element.get() instanceof Property prop)) {
            contentType = prop.getValue();
        }

        String href = Util.getFormsHref(forms);
        LOGGER.debug("createSubscriptionProviderMqtt: href: {}; contentType: {}", href, contentType);
        retval = MqttSubscriptionProviderConfig.builder()
                .format(Util.getFormatFromContentType(contentType))
                .topic(href)
                .build();

        return retval;
    }


    private static void processRelationsMqtt(RelationData data, Map<Reference, MqttSubscriptionProviderConfig> subscriptionProviders)
            throws PersistenceException, ResourceNotFoundException {
        for (var r: data.getRelations()) {
            if (EnvironmentHelper.resolve(r.getFirst(), data.getServiceContext().getAASEnvironment()) instanceof SubmodelElementCollection property) {
                subscriptionProviders.put(r.getSecond(), createSubscriptionProviderMqtt(property, data.getContentType()));
            }
        }
    }


    private static void updateRelationsMqtt(RelationData data, Map<Reference, MqttSubscriptionProviderConfig> subscriptionProviders, Set<Reference> currentSubscriptions)
            throws PersistenceException, ResourceNotFoundException {
        for (var r: data.getRelations()) {
            if ((EnvironmentHelper.resolve(r.getFirst(), data.getServiceContext().getAASEnvironment()) instanceof SubmodelElementCollection property)
                    && (!currentSubscriptions.contains(r.getSecond()))) {
                subscriptionProviders.put(r.getSecond(), createSubscriptionProviderMqtt(property, data.getContentType()));
            }
        }
    }


    private static MqttAssetConnectionConfig.Builder configureSecurityMqtt(ServiceContext serviceContext, AimcSubmodelTemplateProcessorConfig config,
                                                                           SubmodelElementList securityList, MqttAssetConnectionConfig.Builder assetConfigBuilder)
            throws ResourceNotFoundException, PersistenceException {
        MqttAssetConnectionConfig.Builder retval = assetConfigBuilder;
        List<String> supportedSecurity = Util.getSupportedSecurityList(serviceContext, securityList);

        if (supportedSecurity.contains(Constants.AID_SECURITY_NOSEC)) {
            // no security found. We choose that.
            LOGGER.trace("configureSecurity: use no security");
        }
        else if (supportedSecurity.contains(Constants.AID_SECURITY_BASIC)) {
            // use basic security. Username and password are used from the configuration.
            LOGGER.trace("configureSecurity: use basic security");
            if ((config.getUsername() == null) || config.getUsername().isEmpty()) {
                LOGGER.warn("configureSecurity: basic security configured, but no username given");
            }
            else {
                retval = retval.username(config.getUsername()).password(config.getPassword());
            }
        }

        return retval;
    }

}
