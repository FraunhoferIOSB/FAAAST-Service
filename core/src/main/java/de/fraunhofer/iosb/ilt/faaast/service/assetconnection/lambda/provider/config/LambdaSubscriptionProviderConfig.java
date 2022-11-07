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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.config;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProviderConfig;


/**
 * Configuration for code-backed subscription provider.
 */
public class LambdaSubscriptionProviderConfig extends AbstractLambdaProviderConfig<AssetSubscriptionProvider> implements AssetSubscriptionProviderConfig {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractLambdaProviderConfig.AbstractBuilder<AssetSubscriptionProvider, LambdaSubscriptionProviderConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected LambdaSubscriptionProviderConfig newBuildingInstance() {
            return new LambdaSubscriptionProviderConfig();
        }
    }

}
