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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.LambdaOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.LambdaSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.LambdaValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.config.LambdaOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.config.LambdaSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.config.LambdaValueProviderConfig;


/**
 * Configuration for {@link AssetConnectionConfig}.
 */
public class LambdaAssetConnectionConfig
        extends AssetConnectionConfig<LambdaAssetConnection, LambdaValueProviderConfig, LambdaOperationProviderConfig, LambdaSubscriptionProviderConfig> {

    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends LambdaAssetConnectionConfig, B extends AbstractBuilder<T, B>>
            extends
            AssetConnectionConfig.AbstractBuilder<LambdaAssetConnectionConfig, LambdaValueProviderConfig, LambdaValueProvider, LambdaOperationProviderConfig, LambdaOperationProvider, LambdaSubscriptionProviderConfig, LambdaSubscriptionProvider, LambdaAssetConnection, B> {}

    public static class Builder extends AbstractBuilder<LambdaAssetConnectionConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected LambdaAssetConnectionConfig newBuildingInstance() {
            return new LambdaAssetConnectionConfig();
        }

    }
}
