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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.model;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Class with data for HTTP interfaces.
 */
public class InterfaceDataHttp extends InterfaceData {

    private Map<Reference, HttpValueProviderConfig> valueProvider;
    private Map<Reference, HttpSubscriptionProviderConfig> subscriptionProvider;

    public InterfaceDataHttp() {
        valueProvider = new HashMap<>();
        subscriptionProvider = new HashMap<>();
    }


    /**
     * Gets the value providers.
     *
     * @return The value providers.
     */
    public Map<Reference, HttpValueProviderConfig> getValueProvider() {
        return valueProvider;
    }


    /**
     * Sets the value providers.
     *
     * @param value The value providers.
     */
    public void setValueProvider(Map<Reference, HttpValueProviderConfig> value) {
        valueProvider = value;
    }


    /**
     * Adds the given list of value provider.
     *
     * @param value The desired value providers.
     */
    public void addValueProvider(Map<Reference, HttpValueProviderConfig> value) {
        valueProvider.putAll(value);
    }


    /**
     * Gets the subscription providers.
     *
     * @return The subscription providers.
     */
    public Map<Reference, HttpSubscriptionProviderConfig> getSubscriptionProvider() {
        return subscriptionProvider;
    }


    /**
     * Sets the subscription providers.
     *
     * @param value The subscription providers.
     */
    public void setSubscriptionProvider(Map<Reference, HttpSubscriptionProviderConfig> value) {
        subscriptionProvider = value;
    }


    /**
     * Adds the fiven subscription providers.
     *
     * @param value The desired subscription providers.
     */
    public void addSubscriptionProviders(Map<Reference, HttpSubscriptionProviderConfig> value) {
        subscriptionProvider.putAll(value);
    }
}
