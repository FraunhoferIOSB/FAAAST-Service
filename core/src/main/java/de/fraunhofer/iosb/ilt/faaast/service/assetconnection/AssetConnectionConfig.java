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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.fraunhofer.iosb.ilt.faaast.service.config.Config;
import de.fraunhofer.iosb.ilt.faaast.service.config.serialization.ReferenceDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.config.serialization.ReferenceSerializer;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Base config class for all AssetConnection implementations/subclasses.
 *
 * @param <T> type of the corresponding asset connection
 * @param <V> type of the value providers of the corresponding asset connection
 * @param <O> type of the operation providers of the corresponding asset
 *            connection
 * @param <S> type of the subscription providers of the corresponding asset
 *            connection
 */
public class AssetConnectionConfig<T extends AssetConnection, V extends AssetValueProviderConfig, O extends AssetOperationProviderConfig, S extends AssetSubscriptionProviderConfig>
        extends Config<T> {

    @JsonSerialize(keyUsing = ReferenceSerializer.class)
    @JsonDeserialize(keyUsing = ReferenceDeserializer.class)
    protected Map<Reference, O> operationProviders;

    @JsonSerialize(keyUsing = ReferenceSerializer.class)
    @JsonDeserialize(keyUsing = ReferenceDeserializer.class)
    protected Map<Reference, S> subscriptionProviders;

    @JsonSerialize(keyUsing = ReferenceSerializer.class)
    @JsonDeserialize(keyUsing = ReferenceDeserializer.class)
    protected Map<Reference, V> valueProviders;

    public AssetConnectionConfig() {
        operationProviders = new HashMap<>();
        subscriptionProviders = new HashMap<>();
        valueProviders = new HashMap<>();
    }


    /**
     * Gets a map of all operation providers the references they are mapped to
     *
     * @return a map of all operation providers the references they are mapped
     *         to
     */
    public Map<Reference, O> getOperationProviders() {
        return operationProviders;
    }


    /**
     * Sets a map of all operation providers the references they are mapped to
     *
     * @param operationProviders map of all operation providers the references
     *            they are mapped to
     */
    public void setOperationProviders(Map<Reference, O> operationProviders) {
        this.operationProviders = operationProviders;
    }


    /**
     * Gets a map of all subscription providers the references they are mapped
     * to
     *
     * @return a map of all subscription providers the references they are
     *         mapped to
     */
    public Map<Reference, S> getSubscriptionProviders() {
        return subscriptionProviders;
    }


    /**
     * Sets a map of all subscription providers the references they are mapped
     * to
     *
     * @param subscriptionProviders map of all subscription providers the
     *            references they are mapped to
     */
    public void setSubscriptionProviders(Map<Reference, S> subscriptionProviders) {
        this.subscriptionProviders = subscriptionProviders;
    }


    /**
     * Gets a map of all value providers the references they are mapped to
     *
     * @return a map of all value providers the references they are mapped to
     */
    public Map<Reference, V> getValueProviders() {
        return valueProviders;
    }


    /**
     * Sets a map of all value providers the references they are mapped to
     *
     * @param valueProviders map of all value providers the references they are
     *            mapped to
     */
    public void setValueProviders(Map<Reference, V> valueProviders) {
        this.valueProviders = valueProviders;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AssetConnectionConfig<?, ?, ?, ?> that = (AssetConnectionConfig<?, ?, ?, ?>) o;
        return Objects.equals(valueProviders, that.valueProviders)
                && Objects.equals(operationProviders, that.operationProviders)
                && Objects.equals(subscriptionProviders, that.subscriptionProviders);
    }


    @Override
    public int hashCode() {
        return Objects.hash(valueProviders, operationProviders, subscriptionProviders);
    }

    /**
     * Abstract builder class that should be used for builders of inheriting
     * classes.
     *
     * @param <T> type of the asset connection of the config to build
     * @param <V> type of the value providers of the corresponding asset
     *            connection
     * @param <O> type of the operation providers of the corresponding asset
     *            connection
     * @param <S> type of the subscription providers of the corresponding asset
     *            connection
     * @param <C> type of the asset connection
     * @param <B> type of this builder, needed for inheritance builder pattern
     */
    public abstract static class AbstractBuilder<T extends AssetConnectionConfig, V extends AssetValueProviderConfig, O extends AssetOperationProviderConfig, S extends AssetSubscriptionProviderConfig, C extends AssetConnection<T, V, O, S>, B extends AbstractBuilder<T, V, O, S, C, B>>
            extends ExtendableBuilder<T, B> {

        public B operationProviders(Map<Reference, O> value) {
            getBuildingInstance().setOperationProviders(value);
            return getSelf();
        }


        public B operationProvider(Reference key, O value) {
            getBuildingInstance().getOperationProviders().put(key, value);
            return getSelf();
        }


        public B valueProviders(Map<Reference, V> value) {
            getBuildingInstance().setValueProviders(value);
            return getSelf();
        }


        public B valueProvider(Reference key, V value) {
            getBuildingInstance().getValueProviders().put(key, value);
            return getSelf();
        }


        public B subscriptionProviders(Map<Reference, S> value) {
            getBuildingInstance().setSubscriptionProviders(value);
            return getSelf();
        }


        public B subscriptionProvider(Reference key, S value) {
            getBuildingInstance().getSubscriptionProviders().put(key, value);
            return getSelf();
        }

    }

    /**
     * Builder for AssetConnectionConfig class.
     *
     * @param <C> type of the asset connection of the config to build
     * @param <V> type of the value providers of the corresponding asset
     *            connection
     * @param <O> type of the operation providers of the corresponding asset
     *            connection
     * @param <S> type of the subscription providers of the corresponding asset
     *            connection
     */
    public static class Builder<V extends AssetValueProviderConfig, O extends AssetOperationProviderConfig, S extends AssetSubscriptionProviderConfig, C extends AssetConnection<AssetConnectionConfig, V, O, S>>
            extends AbstractBuilder<AssetConnectionConfig, V, O, S, C, Builder<V, O, S, C>> {

        @Override
        protected Builder<V, O, S, C> getSelf() {
            return this;
        }


        @Override
        protected AssetConnectionConfig<AssetConnection, V, O, S> newBuildingInstance() {
            return new AssetConnectionConfig<>();
        }

    }

}
