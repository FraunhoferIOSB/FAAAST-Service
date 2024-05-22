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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AbstractAssetOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProviderConfig;
import java.util.Objects;
import java.util.function.BiFunction;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Operation provider that can be used from code with lambda expression.
 */
public class LambdaOperationProvider implements AssetOperationProvider {

    private BiFunction<OperationVariable[], OperationVariable[], OperationVariable[]> handler;

    private LambdaOperationProvider() {}


    @Override
    public AssetOperationProviderConfig getConfig() {
        return new AbstractAssetOperationProviderConfig() {};
    }


    @Override
    public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
        if (Objects.nonNull(handler)) {
            try {
                return handler.apply(input, inoutput);
            }
            catch (Exception e) {
                throw new AssetConnectionException(e);
            }
        }
        return null;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ExtendableBuilder<LambdaOperationProvider, Builder> {

        public Builder handle(BiFunction<OperationVariable[], OperationVariable[], OperationVariable[]> value) {
            getBuildingInstance().handler = value;
            return this;
        }


        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected LambdaOperationProvider newBuildingInstance() {
            return new LambdaOperationProvider();
        }

    }

}
