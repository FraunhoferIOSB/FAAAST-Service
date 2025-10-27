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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.HttpAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config.BasicCredentials;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config.Credentials;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.model.RelationData;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.net.MalformedURLException;
import java.util.ArrayList;
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

    public static final long DEFAULT_INTERVAL = 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpHelper.class);

    private HttpHelper() {}


    /**
     * Process a HTTP interface.
     *
     * @param serviceContext The service context.
     * @param assetInterface The desired Asset Interface.
     * @param relations The list of rekations.
     * @param credentials The list of credentials.
     * @return The Asset Connection configuration from this interface.
     * @throws MalformedURLException Invalif URL.
     * @throws PersistenceException if storage error occurs
     * @throws ResourceNotFoundException if the resource dcesn't exist.
     */
    public static AssetConnectionConfig processInterface(ServiceContext serviceContext, SubmodelElementCollection assetInterface,
                                                         List<RelationshipElement> relations, Map<String, List<Credentials>> credentials)
            throws MalformedURLException, PersistenceException, ResourceNotFoundException {
        String title = Util.getInterfaceTitle(assetInterface);
        LOGGER.debug("process HTTP interface {} with {} relations", title, relations.size());

        // Endpoint Metadata
        SubmodelElementCollection metadata = Util.getEndpointMetadata(assetInterface);
        String base = Util.getBaseUrl(metadata);

        // contentType
        String contentType = Util.getContentType(metadata);

        Map<Reference, HttpValueProviderConfig> valueProviders = new HashMap<>();
        Map<Reference, HttpSubscriptionProviderConfig> subscriptionProviders = new HashMap<>();

        processRelations(new RelationData(serviceContext, relations, contentType), subscriptionProviders, base, valueProviders);

        HttpAssetConnectionConfig.Builder assetConfigBuilder = HttpAssetConnectionConfig.builder().baseUrl(base);

        List<Credentials> serverCredentials = new ArrayList<>();
        if (credentials.containsKey(base)) {
            serverCredentials = credentials.get(base);
        }

        // security
        Optional<SubmodelElement> element = metadata.getValue().stream().filter(e -> Util.semanticIdEquals(e, Constants.AID_METADATA_SECURITY_SEMANTIC_ID)).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel AID (HTTP) invalid: EndpointMetadata security not found.");
        }
        else if (element.get() instanceof SubmodelElementList securityList) {
            assetConfigBuilder = configureSecurity(serviceContext, securityList, assetConfigBuilder, serverCredentials);
        }

        return assetConfigBuilder
                .valueProviders(valueProviders)
                .subscriptionProviders(subscriptionProviders)
                .build();
    }


    private static void processRelations(RelationData data,
                                         Map<Reference, HttpSubscriptionProviderConfig> subscriptionProviders, String base,
                                         Map<Reference, HttpValueProviderConfig> valueProviders)
            throws PersistenceException, ResourceNotFoundException {
        for (var r: data.getRelations()) {
            if (EnvironmentHelper.resolve(r.getFirst(), data.getServiceContext().getAASEnvironment()) instanceof SubmodelElementCollection property) {
                if (isObservable(property)) {
                    LOGGER.atDebug().log("processRelations: createSubscriptionProvider for: {}", ReferenceHelper.asString(r.getSecond()));
                    subscriptionProviders.put(r.getSecond(), createSubscriptionProvider(property, base, data, r.getFirst()));
                }
                else {
                    LOGGER.atDebug().log("processRelations: createValueProvider for: {}", ReferenceHelper.asString(r.getSecond()));
                    valueProviders.put(r.getSecond(), createValueProvider(property, base, data, r.getFirst()));
                }
            }
        }
    }


    private static HttpSubscriptionProviderConfig createSubscriptionProvider(SubmodelElementCollection property, String baseUrl,
                                                                             RelationData data, Reference propertyReference)
            throws PersistenceException, ResourceNotFoundException {
        HttpSubscriptionProviderConfig retval;

        SubmodelElementCollection forms = Util.getPropertyForms(property, propertyReference, data);
        String contentType = Util.getContentType(data.getContentType(), forms);

        String path = getPath(baseUrl, forms);
        Map<String, String> headers = getHeaders(forms);
        LOGGER.debug("createSubscriptionProvider: href: {}; contentType: {}", path, contentType);
        String jsonPath = Util.getJsonPath(property, propertyReference, data);
        HttpSubscriptionProviderConfig.Builder configBuilder = HttpSubscriptionProviderConfig.builder()
                .format(Util.getFormatFromContentType(contentType))
                .path(path)
                .headers(headers);
        if (!jsonPath.isEmpty()) {
            configBuilder.query(jsonPath);
        }
        retval = configBuilder.build();
        return retval;
    }


    private static HttpValueProviderConfig createValueProvider(SubmodelElementCollection property, String baseUrl, RelationData data, Reference propertyReference)
            throws PersistenceException, ResourceNotFoundException {
        HttpValueProviderConfig retval;

        SubmodelElementCollection forms = Util.getPropertyForms(property, propertyReference, data);
        String contentType = Util.getContentType(data.getContentType(), forms);

        String path = getPath(baseUrl, forms);
        Map<String, String> headers = getHeaders(forms);
        LOGGER.debug("createValueProvider: href: {}; contentType: {}", path, contentType);
        String jsonPath = Util.getJsonPath(property, propertyReference, data);
        HttpValueProviderConfig.Builder configBuilder = HttpValueProviderConfig.builder()
                .format(Util.getFormatFromContentType(contentType))
                .path(path)
                .headers(headers);
        if (!jsonPath.isEmpty()) {
            configBuilder.query(jsonPath);
        }
        retval = configBuilder.build();

        return retval;
    }


    private static Map<String, String> getHeaders(SubmodelElementCollection forms) {
        Map<String, String> retval = new HashMap<>();
        Optional<SubmodelElement> element = forms.getValue().stream().filter(e -> Util.semanticIdEquals(e, Constants.AID_FORMS_HEADERS_SEMANTIC_ID)).findFirst();
        if (element.isPresent() && (element.get() instanceof SubmodelElementList list)) {
            for (var h: list.getValue()) {
                addHeader(retval, h);
            }
        }
        return retval;
    }


    private static void addHeader(Map<String, String> headers, SubmodelElement headerElement) {
        if (headerElement instanceof SubmodelElementCollection header) {
            Optional<SubmodelElement> nameElement = header.getValue().stream().filter(h -> Util.semanticIdEquals(h, Constants.AID_HEADER_FIELD_NAME_SEMANTIC_ID)).findFirst();
            Optional<SubmodelElement> valueElement = header.getValue().stream().filter(h -> Util.semanticIdEquals(h, Constants.AID_HEADER_FIELD_VALUE_SEMANTIC_ID)).findFirst();
            if (nameElement.isPresent() && valueElement.isPresent() && (nameElement.get() instanceof Property name) && (valueElement.get() instanceof Property value)) {
                headers.put(name.getValue(), value.getValue());
            }
        }
    }


    private static String getPath(String baseUrl, SubmodelElementCollection forms) throws IllegalArgumentException {
        String retval = Util.getFormsHref(forms);
        // make path relative to baseUrl
        if (retval.startsWith(baseUrl)) {
            retval = retval.substring(0, baseUrl.length());
        }
        return retval;
    }


    //private static HttpAssetConnectionConfig.Builder configureSecurity(ServiceContext serviceContext, InterfaceConfiguration config,
    private static HttpAssetConnectionConfig.Builder configureSecurity(ServiceContext serviceContext, SubmodelElementList securityList,
                                                                       HttpAssetConnectionConfig.Builder assetConfigBuilder, List<Credentials> credentials)
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


    private static boolean isObservable(SubmodelElementCollection property) {
        boolean retval = false;
        // only available in the root object
        Optional<SubmodelElement> element = property.getValue().stream().filter(e -> Util.semanticIdEquals(e, Constants.AID_PROPERTY_OBSERVABLE_SEMANTIC_ID)).findFirst();
        if (element.isPresent() && (element.get() instanceof Property prop)) {
            String obsText = prop.getValue();
            retval = Boolean.parseBoolean(obsText);
        }
        return retval;
    }

}
