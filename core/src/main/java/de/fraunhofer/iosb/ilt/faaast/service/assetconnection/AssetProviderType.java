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

import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper.BiConsumerWithExceptions;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper.TriConsumerWithExceptions;
import java.util.Map;
import java.util.function.Function;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * This enum represents the different types of asset providers and offers methods to easily access them via
 * {@code AssetConnection} and {@code AssetConnectionConfig}.
 */
public enum AssetProviderType {
    VALUE(x -> ((AssetConnectionConfig) x.asConfig()).getValueProviders(),
            AssetConnectionConfig::getValueProviders,
            (connection, reference, config) -> connection.registerValueProvider(reference, (AssetValueProviderConfig) config),
            (connection, reference) -> connection.unregisterValueProvider(reference)),
    SUBSCRIPTION(x -> ((AssetConnectionConfig) x.asConfig()).getSubscriptionProviders(),
            AssetConnectionConfig::getSubscriptionProviders,
            (connection, reference, config) -> connection.registerSubscriptionProvider(reference, (AssetSubscriptionProviderConfig) config),
            (connection, reference) -> connection.unregisterSubscriptionProvider(reference)),
    OPERATION(x -> ((AssetConnectionConfig) x.asConfig()).getOperationProviders(),
            AssetConnectionConfig::getOperationProviders,
            (connection, reference, config) -> connection.registerOperationProvider(reference, (AssetOperationProviderConfig) config),
            (connection, reference) -> connection.unregisterOperationProvider(reference));

    private final Function<AssetConnection, Map<Reference, AssetProviderConfig>> providersFromConnectionAccessor;
    private final Function<AssetConnectionConfig, Map<Reference, AssetProviderConfig>> providersFromConfigAccessor;
    private final TriConsumerWithExceptions<AssetConnection, Reference, AssetProviderConfig, AssetConnectionException> registerProviderAccessor;
    private final BiConsumerWithExceptions<AssetConnection, Reference, AssetConnectionException> unregisterProviderAccessor;

    private AssetProviderType(Function<AssetConnection, Map<Reference, AssetProviderConfig>> providersFromConnectionAccessor,
            Function<AssetConnectionConfig, Map<Reference, AssetProviderConfig>> mapFromConnectionConfigAccessor,
            TriConsumerWithExceptions<AssetConnection, Reference, AssetProviderConfig, AssetConnectionException> registerProviderAccessor,
            BiConsumerWithExceptions<AssetConnection, Reference, AssetConnectionException> unregisterProviderAccessor) {
        this.providersFromConnectionAccessor = providersFromConnectionAccessor;
        this.providersFromConfigAccessor = mapFromConnectionConfigAccessor;
        this.registerProviderAccessor = registerProviderAccessor;
        this.unregisterProviderAccessor = unregisterProviderAccessor;
    }


    public Function<AssetConnection, Map<Reference, AssetProviderConfig>> getProvidersFromConnectionAccessor() {
        return providersFromConnectionAccessor;
    }


    public Function<AssetConnectionConfig, Map<Reference, AssetProviderConfig>> getProvidersFromConfigAccessor() {
        return providersFromConfigAccessor;
    }


    public TriConsumerWithExceptions<AssetConnection, Reference, AssetProviderConfig, AssetConnectionException> getRegisterProviderAccessor() {
        return registerProviderAccessor;
    }


    public BiConsumerWithExceptions<AssetConnection, Reference, AssetConnectionException> getUnregisterProviderAccessor() {
        return unregisterProviderAccessor;
    }

}
