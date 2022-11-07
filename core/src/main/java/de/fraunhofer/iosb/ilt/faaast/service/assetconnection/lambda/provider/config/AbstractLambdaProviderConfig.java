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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetProvider;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.Objects;


/**
 * Abstract base class for code-backed provider configurations.
 *
 * @param <T> type of the provider
 */
public abstract class AbstractLambdaProviderConfig<T extends AssetProvider> {

    private T implementation;

    public T getImplementation() {
        return implementation;
    }


    public void setImplementation(T implementation) {
        this.implementation = implementation;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractLambdaProviderConfig<T> that = (AbstractLambdaProviderConfig<T>) o;
        return Objects.equals(implementation, that.implementation);
    }


    @Override
    public int hashCode() {
        return Objects.hash(implementation);
    }

    protected abstract static class AbstractBuilder<T extends AssetProvider, C extends AbstractLambdaProviderConfig<T>, B extends AbstractBuilder<T, C, B>>
            extends ExtendableBuilder<C, B> {

        public B implementation(T value) {
            getBuildingInstance().setImplementation(value);
            return getSelf();
        }
    }
}
