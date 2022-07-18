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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProviderConfig;
import java.util.Objects;


/**
 * Config file for OPC UA-based
 * {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider}.
 */
public class OpcUaSubscriptionProviderConfig extends AbstractOpcUaProviderConfig implements AssetSubscriptionProviderConfig {

    private long interval;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpcUaSubscriptionProviderConfig that = (OpcUaSubscriptionProviderConfig) o;
        return super.equals(o)
                && Objects.equals(interval, that.interval);
    }


    public long getInterval() {
        return interval;
    }


    public void setInterval(long interval) {
        this.interval = interval;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), interval);
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<OpcUaSubscriptionProviderConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected OpcUaSubscriptionProviderConfig newBuildingInstance() {
            return new OpcUaSubscriptionProviderConfig();
        }
    }

    private abstract static class AbstractBuilder<T extends OpcUaSubscriptionProviderConfig, B extends AbstractBuilder<T, B>>
            extends AbstractOpcUaProviderConfig.AbstractBuilder<T, B> {

        public B interval(long value) {
            getBuildingInstance().setInterval(value);
            return getSelf();
        }

    }
}
