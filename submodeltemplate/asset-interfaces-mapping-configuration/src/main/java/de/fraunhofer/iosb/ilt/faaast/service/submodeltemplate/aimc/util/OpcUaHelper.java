/*
 * Copyright 2026 Fraunhofer IOSB.
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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.OpcUaAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config.AimcSubmodelTemplateProcessorConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config.BasicCredentials;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config.CertificateCredentials;
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
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class for OPC UA connections.
 */
public class OpcUaHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaHelper.class);

    private OpcUaHelper() {}


    /**
     * Process a OPC UA interface.
     *
     * @param serviceContext The service context.
     * @param assetInterface The desired Asset Interface.
     * @param relations The list of rekations.
     * @param config The configuration of the SubmodelTemplateProcessor.
     * @return The Asset Connection configuration from this interface.
     * @throws PersistenceException if storage error occurs
     * @throws ResourceNotFoundException if the resource dcesn't exist.
     */
    public static AssetConnectionConfig processInterface(ServiceContext serviceContext, SubmodelElementCollection assetInterface,
                                                         List<RelationshipElement> relations, AimcSubmodelTemplateProcessorConfig config)
            throws PersistenceException, ResourceNotFoundException {
        Map<String, List<Credentials>> credentials = config.getCredentials();
        String title = Util.getInterfaceTitle(assetInterface);
        LOGGER.debug("process OPC UA interface {} with {} relations", title, relations.size());

        // Endpoint Metadata
        SubmodelElementCollection metadata = Util.getEndpointMetadata(assetInterface);
        String base = Util.getBaseUrl(metadata);

        // contentType is not relevant here

        Map<Reference, OpcUaValueProviderConfig> valueProviders = new HashMap<>();
        Map<Reference, OpcUaSubscriptionProviderConfig> subscriptionProviders = new HashMap<>();

        processRelations(new RelationData(serviceContext, relations), subscriptionProviders, base, valueProviders);

        OpcUaAssetConnectionConfig.Builder assetConfigBuilder = OpcUaAssetConnectionConfig.builder().host(base);

        List<Credentials> serverCredentials = new ArrayList<>();
        if (credentials.containsKey(base)) {
            serverCredentials = credentials.get(base);
        }

        // security
        Optional<SubmodelElement> element = metadata.getValue().stream().filter(e -> Util.semanticIdEquals(e, Constants.AID_METADATA_SECURITY_SEMANTIC_ID)).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel AID (OPC UA) invalid: EndpointMetadata security not found.");
        }
        else if (element.get() instanceof SubmodelElementList securityList) {
            assetConfigBuilder = configureSecurity(serviceContext, securityList, assetConfigBuilder, serverCredentials);
        }

        if (config.getOpcuaSecurityBaseDir().containsKey(base)) {
            assetConfigBuilder.securityBaseDir(config.getOpcuaSecurityBaseDir().get(base));
        }
        return assetConfigBuilder
                .valueProviders(valueProviders)
                .subscriptionProviders(subscriptionProviders)
                .build();
    }


    private static void processRelations(RelationData data,
                                         Map<Reference, OpcUaSubscriptionProviderConfig> subscriptionProviders, String base,
                                         Map<Reference, OpcUaValueProviderConfig> valueProviders)
            throws PersistenceException, ResourceNotFoundException {
        for (var r: data.getRelations()) {
            if (EnvironmentHelper.resolve(r.getFirst(), data.getServiceContext().getPersistence().getEnvironment()) instanceof SubmodelElementCollection property) {
                if (Util.isObservable(property, data, r.getFirst())) {
                    LOGGER.atDebug().log("processRelations: createSubscriptionProvider for: {}", ReferenceHelper.asString(r.getSecond()));
                    subscriptionProviders.put(r.getSecond(), createSubscriptionProvider(property, data, r.getFirst()));
                }
                else {
                    LOGGER.atDebug().log("processRelations: createValueProvider for: {}", ReferenceHelper.asString(r.getSecond()));
                    valueProviders.put(r.getSecond(), createValueProvider(property, data, r.getFirst()));
                }
            }
        }
    }


    private static OpcUaAssetConnectionConfig.Builder configureSecurity(ServiceContext serviceContext, SubmodelElementList securityList,
                                                                        OpcUaAssetConnectionConfig.Builder assetConfigBuilder, List<Credentials> credentials)
            throws ResourceNotFoundException, PersistenceException {
        OpcUaAssetConnectionConfig.Builder retval = assetConfigBuilder;
        Map<String, SubmodelElement> supportedSecurity = Util.getSupportedSecurityList(serviceContext, securityList);

        if (supportedSecurity.containsKey(Constants.AID_SECURITY_OPCUA_CHANNEL)) {
            // use OPC UA Security Information
            if (supportedSecurity.get(Constants.AID_SECURITY_OPCUA_CHANNEL) instanceof SubmodelElementCollection smc) {
                retval.securityMode(MessageSecurityMode.valueOf(Util.getSecurityMode(smc)));
                retval.securityPolicy(SecurityPolicy.valueOf(Util.getSecurityPolicy(smc)));
            }
        }
        else if (supportedSecurity.containsKey(Constants.AID_SECURITY_NOSEC)) {
            // no security found. We choose that.
            LOGGER.trace("configureSecurity: use no security");
            retval.securityMode(MessageSecurityMode.None);
            retval.securityPolicy(SecurityPolicy.None);
        }

        if (supportedSecurity.containsKey(Constants.AID_SECURITY_OPCUA_AUTHENTICATION)) {
            if (supportedSecurity.get(Constants.AID_SECURITY_OPCUA_AUTHENTICATION) instanceof SubmodelElementCollection smc) {
                String tokenTxt = Util.getSecurityUserIdentity(smc);
                UserTokenType token = UserTokenType.valueOf(tokenTxt);
                switch (token) {
                    case Anonymous -> retval.userTokenType(token);

                    case UserName -> {
                        LOGGER.trace("configureSecurity: use OPC UA security with UserName");
                        Optional<BasicCredentials> basic = credentials.stream().filter(BasicCredentials.class::isInstance).map(c -> (BasicCredentials) c).findFirst();
                        if (basic.isEmpty()) {
                            LOGGER.warn("configureSecurity: OPC UA security with UserName configured, but no username given");
                        }
                        else {
                            retval = retval.userTokenType(token).username(basic.get().getUsername()).password(basic.get().getPassword());
                        }
                    }

                    case Certificate -> {
                        LOGGER.trace("configureSecurity: use OPC UA security with Certificate");
                        Optional<CertificateCredentials> cert = credentials.stream().filter(CertificateCredentials.class::isInstance).map(c -> (CertificateCredentials) c)
                                .findFirst();
                        if (cert.isEmpty()) {
                            LOGGER.warn("configureSecurity: OPC UA security with Certificate configured, but no certificate data given");
                        }
                        else {
                            retval = retval.userTokenType(token).authenticationCertificate(cert.get().getAuthenticationCertificate());
                        }
                    }

                    case IssuedToken -> LOGGER.warn("UserTokenType IssuedToken not supported");
                }
            }
        }

        return retval;
    }


    private static OpcUaSubscriptionProviderConfig createSubscriptionProvider(SubmodelElementCollection property, RelationData data, Reference propertyReference)
            throws PersistenceException, ResourceNotFoundException {

        SubmodelElementCollection forms = Util.getPropertyForms(property, propertyReference, data);
        String nodeid = getNodeId(forms);

        LOGGER.debug("createSubscriptionProvider: nodeId: {}", nodeid);
        OpcUaSubscriptionProviderConfig retval = OpcUaSubscriptionProviderConfig.builder()
                .nodeId(nodeid)
                .build();
        return retval;
    }


    private static OpcUaValueProviderConfig createValueProvider(SubmodelElementCollection property, RelationData data, Reference propertyReference)
            throws PersistenceException, ResourceNotFoundException {
        OpcUaValueProviderConfig retval;

        SubmodelElementCollection forms = Util.getPropertyForms(property, propertyReference, data);
        String nodeid = getNodeId(forms);

        LOGGER.debug("createValueProvider: nodeId: {}", nodeid);
        retval = OpcUaValueProviderConfig.builder()
                .nodeId(nodeid)
                .build();

        return retval;
    }


    private static String getNodeId(SubmodelElementCollection forms) {
        String href = Util.getFormsHref(forms);
        if (href.startsWith("/?id")) {
            href = href.substring(4).trim();
            if (href.startsWith("=")) {
                href = href.substring(1).trim();
                return href;
            }
            else {
                throw new IllegalArgumentException("illegal href value");
            }
        }
        throw new IllegalArgumentException("illegal href value");
    }
}
