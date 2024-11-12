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

import com.prosysopc.ua.stack.core.UserTokenType;
import com.prosysopc.ua.stack.transport.security.SecurityPolicy;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.EndpointConfig;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * Class with Configuration information for the OPC UA Endpoint.
 */
public class OpcUaEndpointConfig extends EndpointConfig<OpcUaEndpoint> {

    public static final int DEFAULT_PORT = 4840;
    private static final int DEFAULT_SECONDS_SHUTDOWN = 2;
    private static final String DEFAULT_SERVER_CERT_PATH = "PKI/CA";
    private static final String DEFAULT_USER_CERT_PATH = "USERS_PKI/CA";
    private int tcpPort;
    private int secondsTillShutdown;
    private Map<String, String> userMap;
    private String discoveryServerUrl;
    private String serverCertificateBasePath;
    private String userCertificateBasePath;
    private Set<SecurityPolicy> supportedSecurityPolicies;
    private Set<UserTokenType> supportedAuthentications;

    public OpcUaEndpointConfig() {
        this.tcpPort = DEFAULT_PORT;
        this.secondsTillShutdown = DEFAULT_SECONDS_SHUTDOWN;
        this.discoveryServerUrl = "";
        this.userMap = new HashMap<>();
        this.serverCertificateBasePath = DEFAULT_SERVER_CERT_PATH;
        this.userCertificateBasePath = DEFAULT_USER_CERT_PATH;
        this.supportedSecurityPolicies = new HashSet<>(SecurityPolicy.ALL_SECURE_104);
        this.supportedSecurityPolicies.add(SecurityPolicy.NONE);
        this.supportedAuthentications = new HashSet<>(Arrays.asList(UserTokenType.Anonymous));
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
        return super.equals(o)
                && Objects.equals(tcpPort, that.tcpPort)
                && Objects.equals(secondsTillShutdown, that.secondsTillShutdown)
                && Objects.equals(discoveryServerUrl, that.discoveryServerUrl)
                && Objects.equals(userMap, that.userMap)
                && Objects.equals(serverCertificateBasePath, that.serverCertificateBasePath)
                && Objects.equals(userCertificateBasePath, that.userCertificateBasePath)
                && Objects.equals(supportedSecurityPolicies, that.supportedSecurityPolicies)
                && Objects.equals(supportedAuthentications, that.supportedAuthentications);
    }


    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                tcpPort,
                secondsTillShutdown,
                discoveryServerUrl,
                userMap,
                serverCertificateBasePath,
                userCertificateBasePath,
                supportedSecurityPolicies,
                supportedAuthentications);
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
     * Gets the URL of the discovery server.
     * If this value is null or empty, the discovery server registration is disabled.
     * 
     * @return The discovery server URL. Discovery registration is disabled if the value is null or empty
     */
    public String getDiscoveryServerUrl() {
        return discoveryServerUrl;
    }


    /**
     * Sets the URL of the discovery server.
     * If this value is null or an empty string, the discovery server registration is disabled.
     * 
     * @param value The discovery server URL. Discovery registration is disabled if the value is null or empty
     */
    public void setDiscoveryServerUrl(String value) {
        discoveryServerUrl = value;
    }


    /**
     * Gets the base path for the server certificates
     * 
     * @return The server certificate base path
     */
    public String getServerCertificateBasePath() {
        return serverCertificateBasePath;
    }


    /**
     * Sets the base path for the server certificates
     * 
     * @param value The server certificate base path
     */
    public void setServerCertificateBasePath(String value) {
        serverCertificateBasePath = value;
    }


    /**
     * Gets the base path for the user certificates
     * 
     * @return The user certificate base path
     */
    public String getUserCertificateBasePath() {
        return userCertificateBasePath;
    }


    /**
     * Sets the base path for the user certificatess
     * 
     * @param value The user certificate base path
     */
    public void setUserCertificateBasePath(String value) {
        userCertificateBasePath = value;
    }


    /**
     * Gets the set of supported Security Policies.
     *
     * @return The set of supported Security Policies.
     */
    public Set<SecurityPolicy> getSupportedSecurityPolicies() {
        return supportedSecurityPolicies;
    }


    /**
     * Sets the set of supported Security Policies.
     *
     * @param value The set of supported Security Policies.
     */
    public void setSupportedSecurityPolicies(Set<SecurityPolicy> value) {
        supportedSecurityPolicies = value;
    }


    /**
     * Gets the set of supported Authentication types (e.g. Anonymous, Username / Password, ...).
     *
     * @return The set of supported Authentication types.
     */
    public Set<UserTokenType> getSupportedAuthentications() {
        return supportedAuthentications;
    }


    /**
     * Sets the list of supported Authentication types.
     *
     * @param value The list of supported Authentication types.
     */
    public void setSupportedAuthentications(Set<UserTokenType> value) {
        supportedAuthentications = value;
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


        public B user(String username, String password) {
            getBuildingInstance().getUserMap().put(username, password);
            return getSelf();
        }


        public B userMap(Map<String, String> value) {
            getBuildingInstance().setUserMap(value);
            return getSelf();
        }


        public B discoveryServerUrl(String value) {
            getBuildingInstance().setDiscoveryServerUrl(value);
            return getSelf();
        }


        public B serverCertificateBasePath(String value) {
            getBuildingInstance().setServerCertificateBasePath(value);
            return getSelf();
        }


        public B userCertificateBasePath(String value) {
            getBuildingInstance().setUserCertificateBasePath(value);
            return getSelf();
        }


        public B supportedSecurityPolicies(Set<SecurityPolicy> value) {
            getBuildingInstance().setSupportedSecurityPolicies(value);
            return getSelf();
        }


        public B supportedSecurityPolicy(SecurityPolicy value) {
            getBuildingInstance().getSupportedSecurityPolicies().add(value);
            return getSelf();
        }


        public B supportedAuthentications(Set<UserTokenType> value) {
            getBuildingInstance().setSupportedAuthentications(value);
            return getSelf();
        }


        public B supportedAuthentication(UserTokenType value) {
            getBuildingInstance().getSupportedAuthentications().add(value);
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
