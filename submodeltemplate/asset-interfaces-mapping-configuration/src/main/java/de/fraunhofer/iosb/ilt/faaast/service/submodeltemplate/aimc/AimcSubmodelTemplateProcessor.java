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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.HttpAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.SubmodelTemplateProcessor;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
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

    @Override
    public boolean accept(Submodel submodel) {
        return submodel != null
                && Objects.equals(ReferenceBuilder.global(Constants.AIMC_SUBMODEL_SEMANTIC_ID), submodel.getSemanticId());
    }


    @Override
    public boolean process(Submodel submodel, AssetConnectionManager assetConnectionManager) {
        try {
            Ensure.requireNonNull(submodel);

            LOGGER.atInfo().log("process submodel {} ({})", submodel.getIdShort(), AasUtils.asString(AasUtils.toReference(submodel)));
            processSubmodel(submodel, assetConnectionManager);

            return true;
        }
        catch (Exception ex) {
            LOGGER.error("error processing SMT AIMC (submodel: {})", AasUtils.asString(AasUtils.toReference(submodel)), ex);
            return false;
        }
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


    private void processSubmodel(Submodel submodel, AssetConnectionManager assetConnectionManager)
            throws PersistenceException, ResourceNotFoundException, MalformedURLException, ConfigurationException, AssetConnectionException {
        Optional<SubmodelElement> element = submodel.getSubmodelElements().stream().filter(s -> Constants.AIMC_MAPPING_CONFIGURATIONS.equals(s.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel invalid: MappingConfigurations not found.");
        }
        if (element.get() instanceof SubmodelElementList mappingConfigurations) {
            for (var c: mappingConfigurations.getValue()) {
                if (c instanceof SubmodelElementCollection configuration) {
                    processConfiguration(configuration, assetConnectionManager);
                }
                else {
                    LOGGER.debug("processSubmodel: element {} not a Collection", c);
                }
            }
        }
        else {
            throw new IllegalArgumentException("Submodel invalid: MappingConfigurations not a list.");
        }
    }


    private void processConfiguration(SubmodelElementCollection configuration, AssetConnectionManager assetConnectionManager)
            throws PersistenceException, ResourceNotFoundException, MalformedURLException, ConfigurationException, AssetConnectionException {
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

        processInterfaceReference(configuration, relations, assetConnectionManager);
    }


    private void processInterfaceReference(SubmodelElementCollection configuration, List<RelationshipElement> relations, AssetConnectionManager assetConnectionManager)
            throws ResourceNotFoundException, ConfigurationException, PersistenceException, MalformedURLException, AssetConnectionException, IllegalArgumentException {
        Optional<SubmodelElement> element = configuration.getValue().stream().filter(e -> Constants.AIMC_INTERFACE_REFERENCE.equals(e.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel invalid: InterfaceReference not found.");
        }
        if (element.get() instanceof ReferenceElement interfaceReference) {
            Referable referenceElement = EnvironmentHelper.resolve(interfaceReference.getValue(), serviceContext.getAASEnvironment());
            if (referenceElement instanceof SubmodelElementCollection assetInterface) {
                if ((ReferenceBuilder.global(Constants.AID_INTERFACE_SEMANTIC_ID).equals(assetInterface.getSemanticId())) && ((assetInterface.getSupplementalSemanticIds() != null)
                        && (assetInterface.getSupplementalSemanticIds().contains(ReferenceBuilder.global(Constants.AID_INTERFACE_SUPP_SEMANTIC_ID_HTTP))))) {
                    // HTTP Interface
                    processHttpInterface(assetInterface, relations, assetConnectionManager);
                }
            }
            else {
                LOGGER.debug("processInterfaceReference: Interface not a SubmodelElementCollection");
            }
        }
        else {
            LOGGER.debug("processInterfaceReference: InterfaceReference not a ReferenceElement");
        }
    }


    private void processHttpInterface(SubmodelElementCollection assetInterface, List<RelationshipElement> relations, AssetConnectionManager assetConnectionManager)
            throws MalformedURLException, PersistenceException, ResourceNotFoundException, ConfigurationException, AssetConnectionException {
        Optional<SubmodelElement> element = assetInterface.getValue().stream().filter(p -> Constants.AID_INTERFACE_TITLE.equals(p.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel AID invalid: Interface Title not found.");
        }
        String title = ((Property) element.get()).getValue();
        LOGGER.debug("process HTTP interface {} with {} relations", title, relations.size());

        element = assetInterface.getValue().stream().filter(e -> Constants.AID_ENDPOINT_METADATA.equals(e.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel AID invalid: EndpointMetadata not found.");
        }
        if (element.get() instanceof SubmodelElementCollection metadata) {
            // Endpoint Metadata
            // base
            element = metadata.getValue().stream().filter(e -> Constants.AID_METADATA_BASE.equals(e.getIdShort())).findFirst();
            if (element.isEmpty()) {
                throw new IllegalArgumentException("Submodel AID invalid: EndpointMetadata base not found.");
            }
            String base = ((Property) element.get()).getValue();
            HttpAssetConnectionConfig.Builder assetConfigBuilder = HttpAssetConnectionConfig.builder().baseUrl(base);

            // security
            element = metadata.getValue().stream().filter(e -> Constants.AID_METADATA_SECURITY.equals(e.getIdShort())).findFirst();
            if (element.isEmpty()) {
                throw new IllegalArgumentException("Submodel AID invalid: EndpointMetadata security not found.");
            }
            else if (element.get() instanceof SubmodelElementList securityList) {
                configureSecurity(securityList, assetConfigBuilder);
            }

            // contentType
            String contentType = null;
            element = metadata.getValue().stream().filter(e -> Constants.AID_METADATA_CONTENT_TYPE.equals(e.getIdShort())).findFirst();
            if (element.isPresent() && (element.get() instanceof Property prop)) {
                contentType = prop.getValue();
            }

            Map<Reference, HttpValueProviderConfig> valueProviders = new HashMap<>();
            Map<Reference, HttpSubscriptionProviderConfig> subscriptionProviders = new HashMap<>();
            processRelations(relations, subscriptionProviders, base, contentType, valueProviders);

            HttpAssetConnectionConfig assetConfig = assetConfigBuilder
                    .valueProviders(valueProviders)
                    .subscriptionProviders(subscriptionProviders)
                    .build();
            assetConnectionManager.add(assetConfig);
        }
    }


    private void processRelations(List<RelationshipElement> relations, Map<Reference, HttpSubscriptionProviderConfig> subscriptionProviders, String base, String contentType,
                                  Map<Reference, HttpValueProviderConfig> valueProviders)
            throws PersistenceException, ResourceNotFoundException {
        Optional<SubmodelElement> element;
        for (var r: relations) {
            if (EnvironmentHelper.resolve(r.getFirst(), serviceContext.getAASEnvironment()) instanceof SubmodelElementCollection property) {
                boolean observable = false;
                element = property.getValue().stream().filter(e -> Constants.AID_PROPERTY_OBSERVABLE.equals(e.getIdShort())).findFirst();
                if (element.isPresent() && (element.get() instanceof Property prop)) {
                    String obsText = prop.getValue();
                    observable = Boolean.parseBoolean(obsText);
                }

                if (observable) {
                    subscriptionProviders.put(r.getSecond(), createSubscriptionProvider(property, base, contentType));
                }
                else {
                    valueProviders.put(r.getSecond(), createValueProvider(property, base, contentType));
                }
            }
        }
    }


    private static HttpValueProviderConfig createValueProvider(SubmodelElementCollection property, String baseUrl, String baseContentType) {
        HttpValueProviderConfig retval = null;

        Optional<SubmodelElement> element = property.getValue().stream().filter(e -> Constants.AID_PROPERTY_FORMS.equals(e.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel AID invalid: Property forms not found.");
        }

        if (element.get() instanceof SubmodelElementCollection forms) {
            String contentType = baseContentType;
            element = forms.getValue().stream().filter(e -> Constants.AID_FORMS_CONTENT_TYPE.equals(e.getIdShort())).findFirst();
            if (element.isPresent() && (element.get() instanceof Property prop)) {
                contentType = prop.getValue();
            }

            String href = getUrl(baseUrl, forms);
            Map<String, String> headers = getHeaders(forms);
            LOGGER.debug("createValueProvider: href: {}; contentType: {}", href, contentType);
            retval = HttpValueProviderConfig.builder()
                    .format(getFormatFromContentType(contentType))
                    .path(href)
                    .headers(headers)
                    .build();
        }

        return retval;
    }


    private static HttpSubscriptionProviderConfig createSubscriptionProvider(SubmodelElementCollection property, String baseUrl, String baseContentType) {
        HttpSubscriptionProviderConfig retval = null;

        Optional<SubmodelElement> element = property.getValue().stream().filter(e -> Constants.AID_PROPERTY_FORMS.equals(e.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel AID invalid: Property forms not found.");
        }

        if (element.get() instanceof SubmodelElementCollection forms) {
            String contentType = baseContentType;
            element = forms.getValue().stream().filter(e -> Constants.AID_FORMS_CONTENT_TYPE.equals(e.getIdShort())).findFirst();
            if (element.isPresent() && (element.get() instanceof Property prop)) {
                contentType = prop.getValue();
            }

            String href = getUrl(baseUrl, forms);
            Map<String, String> headers = getHeaders(forms);
            LOGGER.debug("createSubscriptionProvider: href: {}; contentType: {}", href, contentType);
            retval = HttpSubscriptionProviderConfig.builder()
                    .format(getFormatFromContentType(contentType))
                    .path(href)
                    .headers(headers)
                    .build();
        }

        return retval;
    }


    private static String getUrl(String baseUrl, SubmodelElementCollection forms) throws IllegalArgumentException {
        Optional<SubmodelElement> element = forms.getValue().stream().filter(e -> Constants.AID_FORMS_HREF.equals(e.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel AID invalid: Property href not found in forms.");
        }
        String href = ((Property) element.get()).getValue();
        if (!href.toLowerCase().startsWith("http")) {
            // create absolute URL from base URL
            href = URI.create(baseUrl).resolve(href).toString();
        }
        return href;
    }


    private static Map<String, String> getHeaders(SubmodelElementCollection forms) {
        Map<String, String> retval = new HashMap<>();
        Optional<SubmodelElement> element = forms.getValue().stream().filter(e -> Constants.AID_FORMS_HEADERS.equals(e.getIdShort())).findFirst();
        if (element.isPresent() && (element.get() instanceof SubmodelElementList list)) {
            for (var h: list.getValue()) {
                addHeader(retval, h);
            }
        }
        return retval;
    }


    private static void addHeader(Map<String, String> headers, SubmodelElement headerElement) {
        if (headerElement instanceof SubmodelElementCollection header) {
            Optional<SubmodelElement> nameElement = header.getValue().stream().filter(h -> Constants.AID_HEADER_FIELD_NAME.equals(h.getIdShort())).findFirst();
            Optional<SubmodelElement> valueElement = header.getValue().stream().filter(h -> Constants.AID_HEADER_FIELD_VALUE.equals(h.getIdShort())).findFirst();
            if (nameElement.isPresent() && valueElement.isPresent() && (nameElement.get() instanceof Property name) && (valueElement.get() instanceof Property value)) {
                headers.put(name.getValue(), value.getValue());
            }
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


    private static String getFormatFromContentType(String contentType) {
        Ensure.requireNonNull(contentType);
        switch (contentType) {
            case "application/xml", "text/xml":
                return "XML";

            case "application/json":
                return "JSON";

            default:
                throw new IllegalArgumentException("unsupported contentType: " + contentType);
        }
    }


    private void configureSecurity(SubmodelElementList securityList, HttpAssetConnectionConfig.Builder assetConfigBuilder) throws ResourceNotFoundException, PersistenceException {
        List<String> supportedSecurity = new ArrayList<>();
        for (SubmodelElement se: securityList.getValue()) {
            if (se instanceof ReferenceElement refElem) {
                Referable securityElement = EnvironmentHelper.resolve(refElem.getValue(), serviceContext.getAASEnvironment());
                if (Constants.AID_SECURITY_NOSEC.equals(securityElement.getIdShort()) || Constants.AID_SECURITY_BASIC.equals(securityElement.getIdShort())) {
                    supportedSecurity.add(securityElement.getIdShort());
                }

            }
        }

        if (supportedSecurity.contains(Constants.AID_SECURITY_NOSEC)) {
            // no security found. We choose that.
            LOGGER.trace("configureSecurity: use no security");
        }
        else if (supportedSecurity.contains(Constants.AID_SECURITY_BASIC)) {
            // use basic security. Username and password are userd from the configuration.
            LOGGER.trace("configureSecurity: use basic security");
            if ((config.getUsername() == null) || config.getUsername().isEmpty()) {
                LOGGER.warn("configureSecurity: basic security configured, but no username given");
            }
            else {
                assetConfigBuilder = assetConfigBuilder.username(config.getUsername()).password(config.getPassword());
            }
        }
    }
}
