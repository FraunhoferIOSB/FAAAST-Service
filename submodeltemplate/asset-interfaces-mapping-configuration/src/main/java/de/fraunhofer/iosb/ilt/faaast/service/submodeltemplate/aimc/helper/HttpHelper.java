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
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.AimcSubmodelTemplateProcessorConfigData;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.ProcessingMode;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
     * @param interfaceData The current interface configuration.
     * @param assetInterface The desired Asset Interface.
     * @param relations The list of rekations.
     * @param assetConnectionManager The AssetConnectionManager.
     * @param mode The desired Processing Mode.
     * @throws MalformedURLException Invalif URL.
     * @throws PersistenceException if storage error occurs
     * @throws ResourceNotFoundException if the resource dcesn't exist.
     * @throws ConfigurationException if invalid configuration is provided.
     * @throws AssetConnectionException if there is an error in the Asset
     *             Connection.
     * @throws java.net.URISyntaxException
     */
    public static void processInterface(ServiceContext serviceContext, InterfaceDataHttp interfaceData, SubmodelElementCollection assetInterface,
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
    }


    private static void registerProviders(Map<Reference, HttpValueProviderConfig> valueProviders, Map<Reference, HttpSubscriptionProviderConfig> subscriptionProviders,
                                          AssetConnectionManager assetConnectionManager, String base, SubmodelElementCollection metadata, ServiceContext serviceContext,
                                          InterfaceDataHttp interfaceData)
            throws IllegalArgumentException, ResourceNotFoundException, MalformedURLException, AssetConnectionException, ConfigurationException, URISyntaxException,
            PersistenceException {
        LOGGER.debug("registerProviders: add {} valueProviders; {} subscriptionProviders", valueProviders.size(), subscriptionProviders.size());
        HttpAssetConnection assetConn = getAssetConnection(assetConnectionManager, base);
        if (assetConn == null) {
            HttpAssetConnectionConfig.Builder assetConfigBuilder = HttpAssetConnectionConfig.builder().baseUrl(base);

            // security
            Optional<SubmodelElement> element = metadata.getValue().stream().filter(e -> Constants.AID_METADATA_SECURITY.equals(e.getIdShort())).findFirst();
            if (element.isEmpty()) {
                throw new IllegalArgumentException("Submodel AID (HTTP) invalid: EndpointMetadata security not found.");
            }
            else if (element.get() instanceof SubmodelElementList securityList) {
                assetConfigBuilder = configureSecurity(serviceContext, interfaceData.getConfigData(), securityList, assetConfigBuilder);
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
                if (assetConn.isConnected()) {
                    assetConn.registerValueProvider(p.getKey(), p.getValue());
                }
            }
            for (var s: subscriptionProviders.entrySet()) {
                assetConn.asConfig().getSubscriptionProviders().put(s.getKey(), s.getValue());
                if (assetConn.isConnected()) {
                    assetConn.registerSubscriptionProvider(s.getKey(), s.getValue());
                }
            }
        }
        if (!valueProviders.isEmpty()) {
            interfaceData.addValueProvider(valueProviders);
        }
        if (!subscriptionProviders.isEmpty()) {
            interfaceData.addSubscriptionProviders(subscriptionProviders);
        }
    }


    private static void addProvider(RelationData data, String base, Map<Reference, HttpSubscriptionProviderConfig> subscriptionProviders,
                                    Map<Reference, HttpValueProviderConfig> valueProviders, AssetConnectionManager assetConnectionManager)
            throws ResourceNotFoundException, PersistenceException {
        // save baseUrl
        data.getInterfaceData().setBaseUrl(base);
        processRelations(data, subscriptionProviders, base, valueProviders);
        // check if provider already exist and remove them if necessary
        List<Reference> doubleList = valueProviders.keySet().stream().filter(k -> Util.hasValueProvider(k, assetConnectionManager)).toList();
        for (Reference r: doubleList) {
            LOGGER.atWarn().log("addProvider: ValueProvider for '{}' already configured - entry is ignored", ReferenceHelper.asString(r));
            valueProviders.remove(r);
        }
        doubleList = subscriptionProviders.keySet().stream().filter(k -> Util.hasSubscriptionProvider(k, assetConnectionManager)).toList();
        for (Reference r: doubleList) {
            LOGGER.atWarn().log("addProvider: SubscriptionProvider for '{}' already configured - entry is ignored", ReferenceHelper.asString(r));
            subscriptionProviders.remove(r);
        }
    }


    private static void updateAssetConnections(AssetConnectionManager assetConnectionManager, String base, ProcessingMode mode, RelationData data,
                                               Map<Reference, HttpSubscriptionProviderConfig> subscriptionProviders,
                                               Map<Reference, HttpValueProviderConfig> valueProviders, List<AssetConnection> assetConnectionsRemove)
            throws PersistenceException, ResourceNotFoundException, URISyntaxException {
        if (!base.equals(data.getInterfaceData().getBaseUrl())) {
            deleteOldProviders(assetConnectionManager, data, assetConnectionsRemove);
        }
        HttpAssetConnection hac = getAssetConnection(assetConnectionManager, base);
        if (hac != null) {
            Iterator<Reference> valueIter = data.getInterfaceDataHttp().getValueProvider().keySet().iterator();
            Iterator<Reference> subscriptionIter = data.getInterfaceDataHttp().getSubscriptionProvider().keySet().iterator();

            checkRemovedProviders(valueIter, mode, data, hac, subscriptionIter);
            if (mode != ProcessingMode.DELETE) {
                updateRelations(data, subscriptionProviders, valueProviders, base, hac, assetConnectionManager);
            }
            else if (hac.getValueProviders().isEmpty() && hac.getSubscriptionProviders().isEmpty()
                    && hac.getOperationProviders().isEmpty()) {
                assetConnectionsRemove.add(hac);
            }
        }
        else {
            // create new Asset Connection
            addProvider(data, base, subscriptionProviders, valueProviders, assetConnectionManager);
        }
    }


    private static void checkRemovedProviders(Iterator<Reference> valueIter, ProcessingMode mode, RelationData data, HttpAssetConnection hac,
                                              Iterator<Reference> subscriptionIter) {
        // search for removed providers
        while (valueIter.hasNext()) {
            Reference ref = valueIter.next();
            if (((mode == ProcessingMode.UPDATE) && data.getRelations().stream().noneMatch(r -> r.getSecond().equals(ref)))
                    || ((mode == ProcessingMode.DELETE) && data.getRelations().stream().anyMatch(r -> r.getSecond().equals(ref)))) {
                LOGGER.atTrace().log("checkRemovedProviders: unregisterValueProvider: {}", AasUtils.asString(ref));
                hac.unregisterValueProvider(ref);
                hac.asConfig().getValueProviders().remove(ref);
                valueIter.remove();
            }
        }
        while (subscriptionIter.hasNext()) {
            Reference ref = subscriptionIter.next();
            if (((mode == ProcessingMode.UPDATE) && data.getRelations().stream().noneMatch(r -> r.getSecond().equals(ref)))
                    || ((mode == ProcessingMode.DELETE) && data.getRelations().stream().anyMatch(r -> r.getSecond().equals(ref)))) {
                LOGGER.atTrace().log("checkRemovedProviders: unregisterSubscriptionProvider: {}", AasUtils.asString(ref));
                hac.unregisterSubscriptionProvider(ref);
                hac.asConfig().getSubscriptionProviders().remove(ref);
                subscriptionIter.remove();
            }
        }
    }


    private static void deleteOldProviders(AssetConnectionManager assetConnectionManager, RelationData data, List<AssetConnection> assetConnectionsRemove)
            throws URISyntaxException {
        // delete providers in the old Asset Connection
        HttpAssetConnection hacOld = getAssetConnection(assetConnectionManager, data.getInterfaceData().getBaseUrl());
        if (hacOld != null) {
            for (Reference ref: data.getInterfaceDataHttp().getValueProvider().keySet()) {
                hacOld.unregisterValueProvider(ref);
                hacOld.asConfig().getValueProviders().remove(ref);
            }
            data.getInterfaceDataHttp().getValueProvider().clear();
            for (Reference ref: data.getInterfaceDataHttp().getSubscriptionProvider().keySet()) {
                hacOld.unregisterSubscriptionProvider(ref);
                hacOld.asConfig().getSubscriptionProviders().remove(ref);
            }
            data.getInterfaceDataHttp().getSubscriptionProvider().clear();
            if (hacOld.getValueProviders().isEmpty() && hacOld.getSubscriptionProviders().isEmpty()
                    && hacOld.getOperationProviders().isEmpty()) {
                assetConnectionsRemove.add(hacOld);
            }
        }
        else {
            LOGGER.debug("deleteOldProviders: AssetConnection for old URL '{}' not found", data.getInterfaceData().getBaseUrl());
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
                    subscriptionProviders.put(r.getSecond(), createSubscriptionProvider(property, base, data, r.getFirst()));
                }
                else {
                    LOGGER.atDebug().log("processRelations: createValueProvider for: {}", ReferenceHelper.asString(r.getSecond()));
                    valueProviders.put(r.getSecond(), createValueProvider(property, base, data, r.getFirst()));
                }
            }
        }
    }


    private static void updateRelations(RelationData data,
                                        Map<Reference, HttpSubscriptionProviderConfig> subscriptionProviders, Map<Reference, HttpValueProviderConfig> valueProviders,
                                        String base, HttpAssetConnection hac, AssetConnectionManager assetConnectionManager)
            throws PersistenceException, ResourceNotFoundException {
        var currentValues = data.getInterfaceDataHttp().getValueProvider();
        var currentSubscriptions = data.getInterfaceDataHttp().getSubscriptionProvider();
        for (var r: data.getRelations()) {
            if (EnvironmentHelper.resolve(r.getFirst(), data.getServiceContext().getAASEnvironment()) instanceof SubmodelElementCollection property) {
                if (isObservable(property)) {
                    if (currentSubscriptions.containsKey(r.getSecond())) {
                        // compare provider data
                        HttpSubscriptionProviderConfig config = ReferenceHelper.getValueBySameReference(currentSubscriptions, r.getSecond());
                        if (subscriptionProviderChanged(config, property, base, data, r.getFirst())) {
                            hac.unregisterSubscriptionProvider(r.getSecond());
                            subscriptionProviders.put(r.getSecond(),
                                    createSubscriptionProvider(property, base, data, r.getFirst()));
                        }
                    }
                    // skip items already available in the config
                    else if (!Util.hasSubscriptionProvider(r.getSecond(), assetConnectionManager)) {
                        subscriptionProviders.put(r.getSecond(),
                                createSubscriptionProvider(property, base, data, r.getFirst()));
                    }
                }
                else if (currentValues.containsKey(r.getSecond())) {
                    // compare provider data
                    HttpValueProviderConfig config = ReferenceHelper.getValueBySameReference(currentValues, r.getSecond());
                    if (valueProviderChanged(config, property, base, data, r.getFirst())) {
                        hac.unregisterValueProvider(r.getSecond());
                        valueProviders.put(r.getSecond(), createValueProvider(property, base, data, r.getFirst()));
                    }
                }
                else if (!Util.hasValueProvider(r.getSecond(), assetConnectionManager)) {
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
                .headers(headers)
                .interval(data.getInterfaceData().getConfigData().getSubscriptionInterval() <= 0 ? DEFAULT_INTERVAL
                        : data.getInterfaceData().getConfigData().getSubscriptionInterval());
        if (!jsonPath.isEmpty()) {
            configBuilder.query(jsonPath);
        }
        retval = configBuilder.build();
        return retval;
    }


    private static boolean subscriptionProviderChanged(HttpSubscriptionProviderConfig config, SubmodelElementCollection property, String baseUrl, RelationData data,
                                                       Reference propertyReference)
            throws PersistenceException, ResourceNotFoundException {

        SubmodelElementCollection forms = Util.getPropertyForms(property, propertyReference, data);
        String format = Util.getFormatFromContentType(Util.getContentType(data.getContentType(), forms));
        if (!config.getFormat().equals(format)) {
            return true;
        }

        String path = getPath(baseUrl, forms);
        if (!config.getPath().equals(path)) {
            return true;
        }

        String jsonPath = Util.getJsonPath(property, propertyReference, data);
        if (!jsonPath.equals(config.getQuery())) {
            return true;
        }

        return !config.getHeaders().equals(getHeaders(forms));
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


    private static boolean valueProviderChanged(HttpValueProviderConfig config, SubmodelElementCollection property, String baseUrl, RelationData data, Reference propertyReference)
            throws PersistenceException, ResourceNotFoundException {

        SubmodelElementCollection forms = Util.getPropertyForms(property, propertyReference, data);
        String format = Util.getFormatFromContentType(Util.getContentType(data.getContentType(), forms));
        if (!config.getFormat().equals(format)) {
            return true;
        }

        String path = getPath(baseUrl, forms);
        if (!config.getPath().equals(path)) {
            return true;
        }

        String jsonPath = Util.getJsonPath(property, propertyReference, data);
        if (!jsonPath.equals(config.getQuery())) {
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


    private static String getPath(String baseUrl, SubmodelElementCollection forms) throws IllegalArgumentException {
        String retval = Util.getFormsHref(forms);
        // make path relative to baseUrl
        if (retval.startsWith(baseUrl)) {
            retval = retval.substring(0, baseUrl.length());
        }
        return retval;
    }


    private static HttpAssetConnectionConfig.Builder configureSecurity(ServiceContext serviceContext, AimcSubmodelTemplateProcessorConfigData config,
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
        // only available in the root object
        Optional<SubmodelElement> element = property.getValue().stream().filter(e -> Util.semanticIdEquals(e, Constants.AID_PROPERTY_OBSERVABLE_SEMANTIC_ID)).findFirst();
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
