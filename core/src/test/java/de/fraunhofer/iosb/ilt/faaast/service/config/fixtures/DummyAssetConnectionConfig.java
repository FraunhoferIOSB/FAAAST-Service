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
package de.fraunhofer.iosb.ilt.faaast.service.config.fixtures;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;


public class DummyAssetConnectionConfig
        extends AssetConnectionConfig<DummyAssetConnection, DummyNodeBasedProviderConfig, DummyNodeBasedProviderConfig, DummySubscriptionBasedProviderConfig> {

    private String host;
    private int port;

    public String getHost() {
        return host;
    }


    public void setHost(String host) {
        this.host = host;
    }


    public int getPort() {
        return port;
    }


    public void setPort(int port) {
        this.port = port;
    }

}
