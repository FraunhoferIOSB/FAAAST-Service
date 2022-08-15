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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua;

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.EndpointConfig;
import java.util.Map;
import java.util.Objects;


/**
 * Class with Configuration information for the OPC UA Endpoint.
 */
public class OpcUaEndpointConfig extends EndpointConfig<OpcUaEndpoint> {

    public static final int DEFAULT_PORT = 4840;
    private int tcpPort;
    private int secondsTillShutdown;
    private Map<String, String> userMap;
    private boolean allowAnonymous;
    private boolean registerWithDiscoveryServer;

    public OpcUaEndpointConfig() {
        this.tcpPort = DEFAULT_PORT;
        this.allowAnonymous = true;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpcUaEndpointConfig that = (OpcUaEndpointConfig) o;
        return Objects.equals(tcpPort, that.tcpPort)
                && Objects.equals(secondsTillShutdown, that.secondsTillShutdown)
                && Objects.equals(allowAnonymous, that.allowAnonymous);
    }


    @Override
    public int hashCode() {
        return Objects.hash(tcpPort, secondsTillShutdown, allowAnonymous);
    }


    /**
     * Gets the desired port for the OPC.TCP Endpoint
     *
     * @return The desired port for the OPC.TCP Endpoint
     */
    public int getTcpPort() {
        return tcpPort;
    }


    /**
     * Sets the given port for the OPC.TCP Endpoint
     *
     * @param tcpPort The desired port for the OPC.TCP Endpoint
     */
    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }


    /**
     * Gets the number of seconds until the server stops on shutdown
     *
     * @return The desired number of seconds
     */
    public int getSecondsTillShutdown() {
        return secondsTillShutdown;
    }


    /**
     * Sets the number of seconds until the server stops on shutdown
     *
     * @param value The desired number of seconds
     */
    public void setSecondsTillShutdown(int value) {
        secondsTillShutdown = value;
    }


    /**
     * Gets the user names (Key) and passwords (Value)
     * 
     * @return The desired user names (Key) and passwords (Value)
     */
    public Map<String, String> getUserMap() {
        return userMap;
    }


    /**
     * Sets the user names (Key) and passwords (Value)
     * 
     * @param value The desired user names (Key) and passwords (Value)
     */
    public void setUserMap(Map<String, String> value) {
        userMap = value;
    }


    /**
     * Gets a value indicating whether anonymous access to the server is allowed
     * 
     * @return True if anonymous access is allowed, false otherwise
     */
    public boolean getAllowAnonymous() {
        return allowAnonymous;
    }


    /**
     * Sets a value indicating whether anonymous access to the server is allowed
     * 
     * @param value True if anonymous access is allowed, false otherwise
     */
    public void setAllowAnonymous(boolean value) {
        allowAnonymous = value;
    }


    /**
     * Gets a value indicating whether the server should register with the discovery server
     * 
     * @return True if the server should register with the discovery server, false otherwise
     */
    public boolean getRegisterWithDiscoveryServer() {
        return registerWithDiscoveryServer;
    }


    /**
     * Sets a value indicating whether the server should register with the discovery server
     * 
     * @param value True if the server should register with the discovery server, false otherwise
     */
    public void setRegisterWithDiscoveryServer(boolean value) {
        registerWithDiscoveryServer = value;
    }


    public static Builder builder() {
        return new Builder();
    }

    private abstract static class AbstractBuilder<T extends OpcUaEndpointConfig, B extends AbstractBuilder<T, B>> extends EndpointConfig.AbstractBuilder<OpcUaEndpoint, T, B> {

        public B tcpPort(int value) {
            getBuildingInstance().setTcpPort(value);
            return getSelf();
        }


        public B secondsTillShutdown(int value) {
            getBuildingInstance().setSecondsTillShutdown(value);
            return getSelf();
        }


        public B userMap(Map<String, String> value) {
            getBuildingInstance().setUserMap(value);
            return getSelf();
        }


        public B allowAnonymous(boolean value) {
            getBuildingInstance().setAllowAnonymous(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<OpcUaEndpointConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected OpcUaEndpointConfig newBuildingInstance() {
            return new OpcUaEndpointConfig();
        }
    }
}
