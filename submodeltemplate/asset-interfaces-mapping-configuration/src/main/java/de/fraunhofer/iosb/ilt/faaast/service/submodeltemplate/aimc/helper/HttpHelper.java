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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.HttpAssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.HttpAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.AimcSubmodelTemplateProcessorConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.ProcessingMode;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
     * @param config The current configuration.
     * @param assetInterface The desired Asset Interface.
     * @param relations The list of rekations.
     * @param assetConnectionManager The AssetConnectionManager.
     * @param mode The desired Processing Mode.
     * @throws MalformedURLException Invalif URL.
     * @throws PersistenceException if storage error occurs
     * @throws ResourceNotFoundException if the resource dcesn't exist.
     * @throws ConfigurationException if invalid configuration is provided.
     * @throws AssetConnectionException if there is an error in the Asset Connection.
     * @throws java.net.URISyntaxException
     */
    public static void processInterface(ServiceContext serviceContext, AimcSubmodelTemplateProcessorConfig config, SubmodelElementCollection assetInterface,
                                        List<RelationshipElement> relations, AssetConnectionManager assetConnectionManager, ProcessingMode mode)
            throws MalformedURLException, PersistenceException, ResourceNotFoundException, ConfigurationException, AssetConnectionException, URISyntaxException {
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
            updateAssetConnections(assetConnectionManager, base, mode, new RelationData(serviceContext, relations, contentType, config), subscriptionProviders, valueProviders,
                    assetConnectionsRemove);
        }
        else if (mode == ProcessingMode.ADD) {
            processRelations(new RelationData(serviceContext, relations, contentType, config), subscriptionProviders, base, valueProviders);
            // check if provider already exist and remove them if necessary
            valueProviders.entrySet().removeIf(e -> Util.hasValueProvider(e.getKey(), assetConnectionManager));
            subscriptionProviders.entrySet().removeIf(e -> Util.hasSubscriptionProvider(e.getKey(), assetConnectionManager));
        }

        if (!(subscriptionProviders.isEmpty() && valueProviders.isEmpty())) {
            LOGGER.debug("processInterfaceHttp: add {} valueProviders; {} subscriptionProviders", valueProviders.size(), subscriptionProviders.size());
            HttpAssetConnection assetConn = getAssetConnection(assetConnectionManager, base);
            if (assetConn == null) {
                HttpAssetConnectionConfig.Builder assetConfigBuilder = HttpAssetConnectionConfig.builder().baseUrl(base);

                // security
                Optional<SubmodelElement> element = metadata.getValue().stream().filter(e -> Constants.AID_METADATA_SECURITY.equals(e.getIdShort())).findFirst();
                if (element.isEmpty()) {
                    throw new IllegalArgumentException("Submodel AID (HTTP) invalid: EndpointMetadata security not found.");
                }
                else if (element.get() instanceof SubmodelElementList securityList) {
                    assetConfigBuilder = configureSecurity(serviceContext, config, securityList, assetConfigBuilder);
                }

                HttpAssetConnectionConfig assetConfig = assetConfigBuilder
                        .valueProviders(valueProviders)
                        .subscriptionProviders(subscriptionProviders)
                        .build();
                assetConnectionManager.add(assetConfig);
            }
            else {
                for (var p: valueProviders.entrySet()) {
                    assetConn.asConfig().getValueProviders().put(p.getKey(), p.getValue());
                }
                for (var s: subscriptionProviders.entrySet()) {
                    assetConn.asConfig().getSubscriptionProviders().put(s.getKey(), s.getValue());
                }
            }
        }
        else if (!assetConnectionsRemove.isEmpty()) {
            // remove asset connection if mode DELETE and no more providers are available
            LOGGER.debug("processInterfaceHttp: remove unused AssetConnections");
            for (var connection: assetConnectionsRemove) {
                assetConnectionManager.remove(connection);
            }
            assetConnectionsRemove.clear();
        }
    }


    private static void updateAssetConnections(AssetConnectionManager assetConnectionManager, String base, ProcessingMode mode, RelationData data,
                                               Map<Reference, HttpSubscriptionProviderConfig> subscriptionProviders,
                                               Map<Reference, HttpValueProviderConfig> valueProviders, List<AssetConnection> assetConnectionsRemove)
            throws PersistenceException, ResourceNotFoundException, URISyntaxException {
        HttpAssetConnection hac = getAssetConnection(assetConnectionManager, base);
        if (hac != null) {
            Set<Reference> currentValueProviders = hac.getValueProviders().keySet();
            Set<Reference> currentSubscriptionProviders = hac.getSubscriptionProviders().keySet();

            // search for removed providers
            for (var k: currentValueProviders) {
                if (((mode == ProcessingMode.UPDATE) && data.getRelations().stream().noneMatch(r -> r.getSecond().equals(k)))
                        || ((mode == ProcessingMode.DELETE) && data.getRelations().stream().anyMatch(r -> r.getSecond().equals(k)))) {
                    LOGGER.atTrace().log("processInterfaceHttp: unregisterValueProvider: {}", AasUtils.asString(k));
                    hac.unregisterValueProvider(k);
                }
            }
            for (var k: currentSubscriptionProviders) {
                if (((mode == ProcessingMode.UPDATE) && data.getRelations().stream().noneMatch(r -> r.getSecond().equals(k)))
                        || ((mode == ProcessingMode.DELETE) && data.getRelations().stream().anyMatch(r -> r.getSecond().equals(k)))) {
                    LOGGER.atTrace().log("processInterfaceHttp: unregisterSubscriptionProvider: {}", AasUtils.asString(k));
                    hac.unregisterSubscriptionProvider(k);
                }
            }
            if (mode != ProcessingMode.DELETE) {
                updateRelations(data, subscriptionProviders, valueProviders, base, hac);
            }
            else if (hac.getValueProviders().isEmpty() && hac.getSubscriptionProviders().isEmpty()
                    && hac.getOperationProviders().isEmpty()) {
                assetConnectionsRemove.add(hac);
            }
        }
        else {
            LOGGER.debug("updateAssetConnections: AssetConnection for URL '{}' not found", base);
        }
    }


    private static void processRelations(RelationData data,
                                         Map<Reference, HttpSubscriptionProviderConfig> subscriptionProviders, String base,
                                         Map<Reference, HttpValueProviderConfig> valueProviders)
            throws PersistenceException, ResourceNotFoundException {
        for (var r: data.getRelations()) {
            if (EnvironmentHelper.resolve(r.getFirst(), data.getServiceContext().getAASEnvironment()) instanceof SubmodelElementCollection property) {
                if (isObservable(property)) {
                    LOGGER.atDebug().log("processRelations: createSubscriptionProvider for: {}", ReferenceHelper.asString(r.getSecond()));
                    subscriptionProviders.put(r.getSecond(), HttpHelper.createSubscriptionProvider(property, base, data.getContentType(), data.getConfig()));
                }
                else {
                    LOGGER.atDebug().log("processRelations: createValueProvider for: {}", ReferenceHelper.asString(r.getSecond()));
                    valueProviders.put(r.getSecond(), createValueProvider(property, base, data.getContentType()));
                }
            }
        }
    }


    private static void updateRelations(RelationData data,
                                        Map<Reference, HttpSubscriptionProviderConfig> subscriptionProviders, Map<Reference, HttpValueProviderConfig> valueProviders,
                                        String base, HttpAssetConnection hac)
            throws PersistenceException, ResourceNotFoundException {
        Map<Reference, HttpValueProviderConfig> currentValues = hac.asConfig().getValueProviders();
        Map<Reference, HttpSubscriptionProviderConfig> currentSubscriptions = hac.asConfig().getSubscriptionProviders();
        for (var r: data.getRelations()) {
            if (EnvironmentHelper.resolve(r.getFirst(), data.getServiceContext().getAASEnvironment()) instanceof SubmodelElementCollection property) {
                if (isObservable(property)) {
                    if (currentSubscriptions.containsKey(r.getSecond())) {
                        // compare provider data
                        HttpSubscriptionProviderConfig config = currentSubscriptions.get(r.getSecond());
                        if (subscriptionProviderChanged(config, property, base, data.getContentType())) {
                            hac.unregisterSubscriptionProvider(r.getSecond());
                            subscriptionProviders.put(r.getSecond(), HttpHelper.createSubscriptionProvider(property, base, data.getContentType(), data.getConfig()));
                        }
                    }
                    else {
                        subscriptionProviders.put(r.getSecond(), HttpHelper.createSubscriptionProvider(property, base, data.getContentType(), data.getConfig()));
                    }
                }
                else if (currentValues.containsKey(r.getSecond())) {
                    // compare provider data
                    HttpValueProviderConfig config = currentValues.get(r.getSecond());
                    if (valueProviderChanged(config, property, base, data.getContentType())) {
                        hac.unregisterValueProvider(r.getSecond());
                        valueProviders.put(r.getSecond(), createValueProvider(property, base, data.getContentType()));
                    }
                }
                else {
                    valueProviders.put(r.getSecond(), createValueProvider(property, base, data.getContentType()));
                }
            }
        }
    }


    private static HttpSubscriptionProviderConfig createSubscriptionProvider(SubmodelElementCollection property, String baseUrl, String baseContentType,
                                                                             AimcSubmodelTemplateProcessorConfig config) {
        HttpSubscriptionProviderConfig retval = null;

        SubmodelElementCollection forms = Util.getPropertyForms(property);
        String contentType = Util.getContentType(baseContentType, forms);

        String href = getUrl(baseUrl, forms);
        Map<String, String> headers = getHeaders(forms);
        LOGGER.debug("createSubscriptionProvider: href: {}; contentType: {}", href, contentType);
        retval = HttpSubscriptionProviderConfig.builder()
                .format(Util.getFormatFromContentType(contentType))
                .path(href)
                .headers(headers)
                .interval(config.getSubscriptionInterval() <= 0 ? DEFAULT_INTERVAL : config.getSubscriptionInterval())
                .build();

        return retval;
    }


    private static boolean subscriptionProviderChanged(HttpSubscriptionProviderConfig config, SubmodelElementCollection property, String baseUrl, String baseContentType) {

        SubmodelElementCollection forms = Util.getPropertyForms(property);
        String format = Util.getFormatFromContentType(Util.getContentType(baseContentType, forms));
        if (!config.getFormat().equals(format)) {
            return true;
        }

        String href = getUrl(baseUrl, forms);
        if (!config.getPath().equals(href)) {
            return true;
        }

        return !config.getHeaders().equals(getHeaders(forms));
    }


    private static HttpValueProviderConfig createValueProvider(SubmodelElementCollection property, String baseUrl, String baseContentType) {
        HttpValueProviderConfig retval = null;

        SubmodelElementCollection forms = Util.getPropertyForms(property);
        String contentType = Util.getContentType(baseContentType, forms);

        String href = getUrl(baseUrl, forms);
        Map<String, String> headers = getHeaders(forms);
        LOGGER.debug("createValueProvider: href: {}; contentType: {}", href, contentType);
        retval = HttpValueProviderConfig.builder()
                .format(Util.getFormatFromContentType(contentType))
                .path(href)
                .headers(headers)
                .build();

        return retval;
    }


    private static boolean valueProviderChanged(HttpValueProviderConfig config, SubmodelElementCollection property, String baseUrl, String baseContentType) {

        SubmodelElementCollection forms = Util.getPropertyForms(property);
        String format = Util.getFormatFromContentType(Util.getContentType(baseContentType, forms));
        if (!config.getFormat().equals(format)) {
            return true;
        }

        String href = getUrl(baseUrl, forms);
        if (!config.getPath().equals(href)) {
            return true;
        }

        return !config.getHeaders().equals(getHeaders(forms));
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


    private static HttpAssetConnectionConfig.Builder configureSecurity(ServiceContext serviceContext, AimcSubmodelTemplateProcessorConfig config,
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


    private static boolean isObservable(SubmodelElementCollection property) {
        boolean retval = false;
        Optional<SubmodelElement> element = property.getValue().stream().filter(e -> Constants.AID_PROPERTY_OBSERVABLE.equals(e.getIdShort())).findFirst();
        if (element.isPresent() && (element.get() instanceof Property prop)) {
            String obsText = prop.getValue();
            retval = Boolean.parseBoolean(obsText);
        }
        return retval;
    }


    private static HttpAssetConnection getAssetConnection(AssetConnectionManager assetConnectionManager, String base) throws URISyntaxException {
        HttpAssetConnection retval = null;
        for (var c: assetConnectionManager.getConnections()) {
            if ((c instanceof HttpAssetConnection hac) && (new URI(base).equals(hac.asConfig().getBaseUrl().toURI()))) {
                retval = hac;
                break;
            }
        }

        return retval;
    }
}
