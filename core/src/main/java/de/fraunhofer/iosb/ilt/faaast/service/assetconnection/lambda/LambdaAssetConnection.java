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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.LambdaOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.LambdaSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.LambdaValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Asset connection implementation that executes Java code.
 */
public class LambdaAssetConnection {

    private final Map<Reference, LambdaValueProvider> valueProviders;
    private final Map<Reference, LambdaSubscriptionProvider> subscriptionProviders;
    private final Map<Reference, LambdaOperationProvider> operationProviders;

    public LambdaAssetConnection() {
        this.valueProviders = new HashMap<>();
        this.subscriptionProviders = new HashMap<>();
        this.operationProviders = new HashMap<>();
    }


    /**
     * Register a {@link LambdaValueProvider}.
     *
     * @param reference the reference
     * @param provider the provider
     */
    public void registerValueProvider(Reference reference, LambdaValueProvider provider) {
        if (!ReferenceHelper.containsSameReference(valueProviders, reference)) {
            valueProviders.put(reference, provider);
            return;
        }
        Entry<Reference, LambdaValueProvider> existing = ReferenceHelper.getEntryBySameReference(valueProviders, reference);
        valueProviders.put(
                existing.getKey(),
                LambdaValueProvider.builder()
                        .from(existing.getValue())
                        .merge(provider)
                        .build());
    }


    /**
     * Unregister a {@link LambdaValueProvider}.
     *
     * @param reference the reference
     */
    public void unregisterValueProvider(Reference reference) {
        Reference actualReference = ReferenceHelper.findSameReference(valueProviders.keySet(), reference);
        if (Objects.nonNull(actualReference)) {
            valueProviders.remove(actualReference);
        }
    }


    /**
     * Register a {@link LambdaSubscriptionProvider}.
     *
     * @param reference the reference
     * @param provider the provider
     */
    public void registerSubscriptionProvider(Reference reference, LambdaSubscriptionProvider provider) {
        subscriptionProviders.put(reference, provider);
    }


    /**
     * Unregister a {@link LambdaSubscriptionProvider}.
     *
     * @param reference the reference
     */
    public void unregisterSubscriptionProvider(Reference reference) {
        Reference actualReference = ReferenceHelper.findSameReference(subscriptionProviders.keySet(), reference);
        if (Objects.nonNull(actualReference)) {
            subscriptionProviders.remove(actualReference);
        }
    }


    /**
     * Register a {@link LambdaOperationProvider}.
     *
     * @param reference the reference
     * @param provider the provider
     */
    public void registerOperationProvider(Reference reference, LambdaOperationProvider provider) {
        operationProviders.put(reference, provider);
    }


    /**
     * Unregister a {@link LambdaOperationProvider}.
     *
     * @param reference the reference
     */
    public void unregisterOperationProvider(Reference reference) {
        Reference actualReference = ReferenceHelper.findSameReference(operationProviders.keySet(), reference);
        if (Objects.nonNull(actualReference)) {
            operationProviders.remove(actualReference);
        }
    }


    /**
     * Returns whether there is a operation provider defined for the provided
     * AAS element or not.
     *
     * @param reference AAS element
     * @return true if there is a operation provider defined for the provided
     *         AAS element, otherwise false
     */
    public boolean hasOperationProvider(Reference reference) {
        return Objects.nonNull(getOperationProvider(reference));
    }


    /**
     * Returns whether there is a subscription provider defined for the provided
     * AAS element or not.
     *
     * @param reference AAS element
     * @return true if there is a subscription provider defined for the provided
     *         AAS element, otherwise false
     */
    public boolean hasSubscriptionProvider(Reference reference) {
        return Objects.nonNull(getSubscriptionProvider(reference));
    }


    /**
     * Returns whether there is a value provider defined for the provided AAS
     * element or not.
     *
     * @param reference AAS element
     * @return true if there is a value provider defined for the provided AAS
     *         element, otherwise false
     */
    public boolean hasValueProvider(Reference reference) {
        return Objects.nonNull(getValueProvider(reference));
    }


    /**
     * Gets the operation provider for the AAS element defined by reference.
     *
     * @param reference AAS element
     * @return operation provider for the AAS element defined by reference or
     *         null if there is none defined
     */
    public LambdaOperationProvider getOperationProvider(Reference reference) {
        return ReferenceHelper.getValueBySameReference(operationProviders, reference);
    }


    /**
     * Gets the subscription provider for the AAS element defined by reference.
     *
     * @param reference AAS element
     * @return subscription provider for the AAS element defined by reference or
     *         null if there is none defined
     */
    public LambdaSubscriptionProvider getSubscriptionProvider(Reference reference) {
        return ReferenceHelper.getValueBySameReference(subscriptionProviders, reference);
    }


    /**
     * Gets the value provider for the AAS element defined by reference.
     *
     * @param reference AAS element
     * @return value provider for the AAS element defined by reference or null
     *         if there is none defined
     */
    public LambdaValueProvider getValueProvider(Reference reference) {
        return ReferenceHelper.getValueBySameReference(valueProviders, reference);
    }


    /**
     * Starts the asset connection.
     */
    public void start() {
        subscriptionProviders.values().forEach(LambdaSubscriptionProvider::start);
    }


    /**
     * Stops the asset connection.
     */
    public void stop() {
        // intentionally empty
    }

}
