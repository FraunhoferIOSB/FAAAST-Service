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
package de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.provider.config;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProviderConfig;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


public class CustomSubscriptionProviderConfig implements AssetSubscriptionProviderConfig {

    private static final long DEFAULT_INTERVAL = 1000;
    private long interval;

    public CustomSubscriptionProviderConfig() {
        this.interval = DEFAULT_INTERVAL;
    }


    public long getInterval() {
        return interval;
    }


    public void setInterval(long interval) {
        this.interval = interval;
    }

    public class Builder extends ExtendableBuilder<CustomSubscriptionProviderConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected CustomSubscriptionProviderConfig newBuildingInstance() {
            return new CustomSubscriptionProviderConfig();
        }


        public Builder interval(long value) {
            this.getBuildingInstance().setInterval(value);
            return getSelf();
        }

    }
}
