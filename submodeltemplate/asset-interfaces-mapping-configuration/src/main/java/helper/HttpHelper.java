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
package helper;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.HttpAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.AimcSubmodelTemplateProcessorConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.RelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class for HTTP connections.
 */
public class HttpHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpHelper.class);

    /**
     * Process a HTTP interface.
     *
     * @param serviceContext The service context.
     * @param config The current configuration.
     * @param assetInterface The desired Asset Interface.
     * @param relations The list of rekations.
     * @param assetConnectionManager The AssetConnectionManager.
     * @throws MalformedURLException Invalif URL.
     * @throws PersistenceException if storage error occurs
     * @throws ResourceNotFoundException if the resource dcesn't exist.
     * @throws ConfigurationException if invalid configuration is provided.
     * @throws AssetConnectionException if there is an error in the Asset Connection.
     */
    public static void processInterfaceHttp(ServiceContext serviceContext, AimcSubmodelTemplateProcessorConfig config, SubmodelElementCollection assetInterface,
                                            List<RelationshipElement> relations, AssetConnectionManager assetConnectionManager)
            throws MalformedURLException, PersistenceException, ResourceNotFoundException, ConfigurationException, AssetConnectionException {
        String title = Util.getInterfaceTitle(assetInterface);
        LOGGER.debug("process HTTP interface {} with {} relations", title, relations.size());

        // Endpoint Metadata
        SubmodelElementCollection metadata = Util.getEndpointMetadata(assetInterface);

        // base
        String base = Util.getBaseUrl(metadata);
        HttpAssetConnectionConfig.Builder assetConfigBuilder = HttpAssetConnectionConfig.builder().baseUrl(base);

        // security
        Optional<SubmodelElement> element = metadata.getValue().stream().filter(e -> Constants.AID_METADATA_SECURITY.equals(e.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel AID (HTTP) invalid: EndpointMetadata security not found.");
        }
        else if (element.get() instanceof SubmodelElementList securityList) {
            assetConfigBuilder = configureSecurityHttp(serviceContext, config, securityList, assetConfigBuilder);
        }

        // contentType
        String contentType = Util.getContentType(metadata);

        Map<Reference, HttpValueProviderConfig> valueProviders = new HashMap<>();
        Map<Reference, HttpSubscriptionProviderConfig> subscriptionProviders = new HashMap<>();
        processRelationsHttp(serviceContext, relations, subscriptionProviders, base, contentType, valueProviders);

        HttpAssetConnectionConfig assetConfig = assetConfigBuilder
                .valueProviders(valueProviders)
                .subscriptionProviders(subscriptionProviders)
                .build();
        assetConnectionManager.add(assetConfig);
    }


    private static void processRelationsHttp(ServiceContext serviceContext, List<RelationshipElement> relations,
                                             Map<Reference, HttpSubscriptionProviderConfig> subscriptionProviders, String base, String contentType,
                                             Map<Reference, HttpValueProviderConfig> valueProviders)
            throws PersistenceException, ResourceNotFoundException {
        for (var r: relations) {
            if (EnvironmentHelper.resolve(r.getFirst(), serviceContext.getAASEnvironment()) instanceof SubmodelElementCollection property) {
                boolean observable = false;
                Optional<SubmodelElement> element = property.getValue().stream().filter(e -> Constants.AID_PROPERTY_OBSERVABLE.equals(e.getIdShort())).findFirst();
                if (element.isPresent() && (element.get() instanceof Property prop)) {
                    String obsText = prop.getValue();
                    observable = Boolean.parseBoolean(obsText);
                }

                if (observable) {
                    subscriptionProviders.put(r.getSecond(), HttpHelper.createSubscriptionProviderHttp(property, base, contentType));
                }
                else {
                    valueProviders.put(r.getSecond(), createValueProviderHttp(property, base, contentType));
                }
            }
        }
    }


    private static HttpSubscriptionProviderConfig createSubscriptionProviderHttp(SubmodelElementCollection property, String baseUrl, String baseContentType) {
        HttpSubscriptionProviderConfig retval = null;

        SubmodelElementCollection forms = Util.getPropertyForms(property);
        String contentType = baseContentType;
        Optional<SubmodelElement> element = forms.getValue().stream().filter(e -> Constants.AID_FORMS_CONTENT_TYPE.equals(e.getIdShort())).findFirst();
        if (element.isPresent() && (element.get() instanceof Property prop)) {
            contentType = prop.getValue();
        }

        String href = getUrl(baseUrl, forms);
        Map<String, String> headers = getHeaders(forms);
        LOGGER.debug("createSubscriptionProviderHttp: href: {}; contentType: {}", href, contentType);
        retval = HttpSubscriptionProviderConfig.builder()
                .format(Util.getFormatFromContentType(contentType))
                .path(href)
                .headers(headers)
                .build();
        //}

        return retval;
    }


    private static HttpValueProviderConfig createValueProviderHttp(SubmodelElementCollection property, String baseUrl, String baseContentType) {
        HttpValueProviderConfig retval = null;

        Optional<SubmodelElement> element = property.getValue().stream().filter(e -> Constants.AID_PROPERTY_FORMS.equals(e.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel AID (HTTP) invalid: Property forms not found.");
        }

        if (element.get() instanceof SubmodelElementCollection forms) {
            String contentType = baseContentType;
            element = forms.getValue().stream().filter(e -> Constants.AID_FORMS_CONTENT_TYPE.equals(e.getIdShort())).findFirst();
            if (element.isPresent() && (element.get() instanceof Property prop)) {
                contentType = prop.getValue();
            }

            String href = getUrl(baseUrl, forms);
            Map<String, String> headers = getHeaders(forms);
            LOGGER.debug("createValueProviderHttp: href: {}; contentType: {}", href, contentType);
            retval = HttpValueProviderConfig.builder()
                    .format(Util.getFormatFromContentType(contentType))
                    .path(href)
                    .headers(headers)
                    .build();
        }

        return retval;
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


    private static String getUrl(String baseUrl, SubmodelElementCollection forms) throws IllegalArgumentException {
        String href = Util.getFormsHref(forms);
        if (!href.toLowerCase().startsWith("http")) {
            // create absolute URL from base URL
            href = URI.create(baseUrl).resolve(href).toString();
        }
        return href;
    }


    private static HttpAssetConnectionConfig.Builder configureSecurityHttp(ServiceContext serviceContext, AimcSubmodelTemplateProcessorConfig config,
                                                                           SubmodelElementList securityList, HttpAssetConnectionConfig.Builder assetConfigBuilder)
            throws ResourceNotFoundException, PersistenceException {
        HttpAssetConnectionConfig.Builder retval = assetConfigBuilder;
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
