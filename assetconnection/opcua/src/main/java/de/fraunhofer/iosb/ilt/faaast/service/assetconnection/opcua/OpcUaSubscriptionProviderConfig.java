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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProviderConfig;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.Objects;


/**
 * Config file for OPC UA-based
 * {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider}.
 */
public class OpcUaSubscriptionProviderConfig implements AssetSubscriptionProviderConfig {

    public String getNodeId() {
        return nodeId;
    }


    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    private String nodeId;

    public long getInterval() {
        return interval;
    }


    public void setInterval(long interval) {
        this.interval = interval;
    }

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
        return Objects.equals(nodeId, that.nodeId)
                && Objects.equals(interval, that.interval);
    }


    @Override
    public int hashCode() {
        return Objects.hash(nodeId, interval);
    }


    public static Builder builder() {
        return new Builder();
    }

    private static abstract class AbstractBuilder<T extends OpcUaSubscriptionProviderConfig, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B nodeId(String value) {
            getBuildingInstance().setNodeId(value);
            return getSelf();
        }


        public B interval(long value) {
            getBuildingInstance().setInterval(value);
            return getSelf();
        }

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
}
