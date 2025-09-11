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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config;

import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.model.Protocol;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.model.ProviderType;


public abstract class ExtendedConfiguration {
    private final Protocol protocol;
    private final ProviderType providerType;

    protected ExtendedConfiguration(Protocol protocol, ProviderType providerType) {
        this.protocol = protocol;
        this.providerType = providerType;
    }


    public Protocol getProtocol() {
        return protocol;
    }


    public ProviderType getProviderType() {
        return providerType;
    }
}
