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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.server;

import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;


/**
 *
 * Enum representing supported OPC UA endpoint protocols.
 */
public enum Protocol {
    TCP,
    HTTPS,
    WEBSOCKET;

    public static Protocol from(TransportProfile transportProfile) {
        switch (transportProfile) {
            case TCP_UASC_UABINARY:
                return TCP;
            case HTTPS_UABINARY:
            case HTTPS_UAJSON:
            case HTTPS_UAXML:
                return HTTPS;
            case WSS_UASC_UABINARY:
            case WSS_UAJSON:
                return WEBSOCKET;
            default:
                throw new IllegalStateException(String.format("unsupported transport profile: %s", transportProfile));
        }
    }

}
