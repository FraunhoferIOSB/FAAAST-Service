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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.util;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.MqttAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.provider.config.MqttSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config.BasicCredentials;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config.Credentials;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.model.RelationData;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
     * @param assetInterface The desired Asset Interface.
     * @param relations The list of rekations.
     * @param credentials The list of credentials.
     * @return The Asset Connection configuration from this interface.
     * @throws PersistenceException if a storage error occurs.
     * @throws ResourceNotFoundException if the resource dcesn't exist..
     */
    public static AssetConnectionConfig processInterface(ServiceContext serviceContext, SubmodelElementCollection assetInterface,
                                                         List<RelationshipElement> relations, Map<String, List<Credentials>> credentials)
            throws ResourceNotFoundException, PersistenceException {
        String title = Util.getInterfaceTitle(assetInterface);
        LOGGER.debug("process MQTT interface {} with {} relations", title, relations.size());

        // Endpoint Metadata
        SubmodelElementCollection metadata = Util.getEndpointMetadata(assetInterface);

        // base
        String base = Util.getBaseUrl(metadata);

        // contentType
        String contentType = Util.getContentType(metadata);

        Map<Reference, MqttSubscriptionProviderConfig> subscriptionProviders = new HashMap<>();

        processRelations(new RelationData(serviceContext, relations, contentType), subscriptionProviders);

        List<Credentials> serverCredentials = new ArrayList<>();
        if (credentials.containsKey(base)) {
            serverCredentials = credentials.get(base);
        }

        MqttAssetConnectionConfig.Builder assetConfigBuilder = MqttAssetConnectionConfig.builder().serverUri(base);

        // security
        Optional<SubmodelElement> element = metadata.getValue().stream().filter(e -> Util.semanticIdEquals(e, Constants.AID_METADATA_SECURITY_SEMANTIC_ID)).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel AID (MQTT) invalid: EndpointMetadata security not found.");
        }
        else if (element.get() instanceof SubmodelElementList securityList) {
            assetConfigBuilder = configureSecurity(serviceContext, securityList, assetConfigBuilder, serverCredentials);
        }

        LOGGER.debug("processInterface: add {} subscriptionProviders", subscriptionProviders.size());
        return assetConfigBuilder
                .subscriptionProviders(subscriptionProviders)
                .build();
    }


    private static MqttSubscriptionProviderConfig createSubscriptionProvider(SubmodelElementCollection property, RelationData data, Reference propertyReference)
            throws PersistenceException, ResourceNotFoundException {
        MqttSubscriptionProviderConfig retval;

        SubmodelElementCollection forms = Util.getPropertyForms(property, propertyReference, data);
        String contentType = Util.getContentType(data.getContentType(), forms);

        String href = Util.getFormsHref(forms);
        LOGGER.debug("createSubscriptionProvider: href: {}; contentType: {}", href, contentType);
        String jsonPath = Util.getJsonPath(property, propertyReference, data);
        MqttSubscriptionProviderConfig.Builder configBuilder = MqttSubscriptionProviderConfig.builder()
                .format(Util.getFormatFromContentType(contentType))
                .topic(href);
        if (!jsonPath.isEmpty()) {
            configBuilder.query(jsonPath);
        }
        retval = configBuilder.build();
        return retval;

    }


    private static void processRelations(RelationData data, Map<Reference, MqttSubscriptionProviderConfig> subscriptionProviders)
            throws PersistenceException, ResourceNotFoundException {
        for (var r: data.getRelations()) {
            if (EnvironmentHelper.resolve(r.getFirst(), data.getServiceContext().getAASEnvironment()) instanceof SubmodelElementCollection property) {
                LOGGER.atDebug().log("processRelations: createSubscriptionProvider for: {}", ReferenceHelper.asString(r.getSecond()));
                subscriptionProviders.put(r.getSecond(), createSubscriptionProvider(property, data, r.getFirst()));
            }
        }
    }


    private static MqttAssetConnectionConfig.Builder configureSecurity(ServiceContext serviceContext, SubmodelElementList securityList,
                                                                       MqttAssetConnectionConfig.Builder assetConfigBuilder, List<Credentials> credentials)
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
            Optional<BasicCredentials> basic = credentials.stream().filter(BasicCredentials.class::isInstance).map(c -> (BasicCredentials) c).findFirst();
            if (basic.isEmpty()) {
                LOGGER.warn("configureSecurity: basic security configured, but no username given");
            }
            else {
                retval = retval.username(basic.get().getUsername()).password(basic.get().getPassword());
            }
        }

        return retval;
    }

}
