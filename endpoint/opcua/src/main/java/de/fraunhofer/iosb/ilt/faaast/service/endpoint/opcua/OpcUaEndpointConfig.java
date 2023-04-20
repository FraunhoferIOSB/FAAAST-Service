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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


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
    private boolean allowAnonymous;
    private boolean enableCertificateAuthentication;
    private String discoveryServerUrl;
    private String serverCertificateBasePath;
    private String userCertificateBasePath;
    private boolean enableBasic256Sha256;
    private boolean enableAes128Sha256RsaOaep;
    private boolean enableAes256Sha256RsaPss;
    private boolean enableBasic256;
    private boolean enableBasic128Rsa15;

    public OpcUaEndpointConfig() {
        this.tcpPort = DEFAULT_PORT;
        this.secondsTillShutdown = DEFAULT_SECONDS_SHUTDOWN;
        this.allowAnonymous = true;
        this.enableCertificateAuthentication = true;
        this.discoveryServerUrl = "";
        this.userMap = new HashMap<>();
        this.serverCertificateBasePath = DEFAULT_SERVER_CERT_PATH;
        this.userCertificateBasePath = DEFAULT_USER_CERT_PATH;
        this.enableBasic256Sha256 = true;
        this.enableAes128Sha256RsaOaep = true;
        this.enableAes256Sha256RsaPss = true;
        this.enableBasic256 = false;
        this.enableBasic128Rsa15 = false;
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
                && Objects.equals(allowAnonymous, that.allowAnonymous)
                && Objects.equals(enableCertificateAuthentication, that.enableCertificateAuthentication)
                && Objects.equals(discoveryServerUrl, that.discoveryServerUrl)
                && Objects.equals(userMap, that.userMap)
                && Objects.equals(serverCertificateBasePath, that.serverCertificateBasePath)
                && Objects.equals(userCertificateBasePath, that.userCertificateBasePath)
                && Objects.equals(enableBasic256Sha256, that.enableBasic256Sha256)
                && Objects.equals(enableAes128Sha256RsaOaep, that.enableAes128Sha256RsaOaep)
                && Objects.equals(enableAes256Sha256RsaPss, that.enableAes256Sha256RsaPss)
                && Objects.equals(enableBasic256, that.enableBasic256)
                && Objects.equals(enableBasic128Rsa15, that.enableBasic128Rsa15);
    }


    @Override
    public int hashCode() {
        return Objects.hash(tcpPort, secondsTillShutdown, allowAnonymous, enableCertificateAuthentication, discoveryServerUrl, userMap, serverCertificateBasePath,
                userCertificateBasePath, enableBasic256Sha256, enableAes128Sha256RsaOaep, enableAes256Sha256RsaPss, enableBasic256, enableBasic128Rsa15);
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
     * Gets a value indicating whether Authentication with certificates is enabled.
     *
     * @return True if authentication with certificates is enabled, false otherwise.
     */
    public boolean getEnableCertificateAuthentication() {
        return enableCertificateAuthentication;
    }


    /**
     * Sets a value to enable or disable the Authentication with certificates.
     *
     * @param value True if authentication with certificates is enabled, false otherwise.
     */
    public void setEnableCertificateAuthentication(boolean value) {
        enableCertificateAuthentication = value;
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
     * Gets a value indicating, whether the Security Policy Basic256Sha256 is enabled or not.
     * 
     * @return True if Security Policy Basic256Sha256 is enabled, false otherwise.
     */
    public boolean getEnableBasic256Sha256() {
        return enableBasic256Sha256;
    }


    /**
     * Sets a value to enable or disable the Security Policy Basic256Sha256.
     * 
     * @param value True if Security Policy Basic256Sha256 should be enabled, false otherwise.
     */
    public void setEnableBasic256Sha256(boolean value) {
        enableBasic256Sha256 = value;
    }


    /**
     * Gets a value indicating, whether the Security Policy Aes128-Sha256-RsaOaep is enabled or not.
     * 
     * @return True if Security Policy Aes128-Sha256-RsaOaep is enabled, false otherwise.
     */
    public boolean getEnableAes128Sha256RsaOaep() {
        return enableAes128Sha256RsaOaep;
    }


    /**
     * Sets a value to enable or disable the Security Policy Aes128-Sha256-RsaOaep.
     * 
     * @param value True if Security Policy Aes128-Sha256-RsaOaep should be enabled, false otherwise.
     */
    public void setEnableAes128Sha256RsaOaep(boolean value) {
        enableAes128Sha256RsaOaep = value;
    }


    /**
     * Gets a value indicating, whether the Security Policy Aes256-Sha256-RsaPss is enabled or not.
     * 
     * @return True if Security Policy Aes256-Sha256-RsaPss is enabled, false otherwise.
     */
    public boolean getEnableAes256Sha256RsaPss() {
        return enableAes256Sha256RsaPss;
    }


    /**
     * Sets a value to enable or disable the Security Policy Aes256-Sha256-RsaPss.
     * 
     * @param value True if Security Policy Aes256-Sha256-RsaPss should be enabled, false otherwise.
     */
    public void setEnableAes256Sha256RsaPss(boolean value) {
        enableAes256Sha256RsaPss = value;
    }


    /**
     * Gets a value indicating, whether the Security Policy Basic256 is enabled or not.
     * Please be aware, that SecurityPolicy Basic256 is deprecated since the hash algorithm Sha-1 is not considered secure
     * anymore.
     *
     * @return True if Security Policy Basic256 is enabled, false otherwise.
     */
    @Deprecated
    public boolean getEnableBasic256() {
        return enableBasic256;
    }


    /**
     * Sets a value to enable or disable the Security Policy Basic256.
     * Please be aware, that SecurityPolicy Basic256 is deprecated since the hash algorithm Sha-1 is not considered secure
     * anymore.
     * 
     * @param value True if Security Policy Basic256 should be enabled, false otherwise.
     */
    @Deprecated
    public void setEnableBasic256(boolean value) {
        enableBasic256 = value;
    }


    /**
     * Gets a value indicating, whether the Security Policy Basic128Rsa15 is enabled or not.
     * Please be aware, that SecurityPolicy Basic128Rsa15 is deprecated since the hash algorithm Sha-1 is not considered
     * secure anymore.
     *
     * @return True if Security Policy Basic128Rsa15 is enabled, false otherwise.
     */
    @Deprecated
    public boolean getEnableBasic128Rsa15() {
        return enableBasic128Rsa15;
    }


    /**
     * Sets a value to enable or disable the Security Policy Basic256.
     * Please be aware, that SecurityPolicy Basic128Rsa15 is deprecated since the hash algorithm Sha-1 is not considered
     * secure anymore.
     * 
     * @param value True if Security Policy Basic128Rsa15 should be enabled, false otherwise.
     */
    @Deprecated
    public void setEnableBasic128Rsa15(boolean value) {
        enableBasic128Rsa15 = value;
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


        public B allowAnonymous(boolean value) {
            getBuildingInstance().setAllowAnonymous(value);
            return getSelf();
        }


        public B enableCertificateAuthentication(boolean value) {
            getBuildingInstance().setEnableCertificateAuthentication(value);
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


        public B enableBasic256Sha256(boolean value) {
            getBuildingInstance().setEnableBasic256Sha256(value);
            return getSelf();
        }


        public B enableAes128Sha256RsaOaep(boolean value) {
            getBuildingInstance().setEnableAes128Sha256RsaOaep(value);
            return getSelf();
        }


        public B enableAes256Sha256RsaPss(boolean value) {
            getBuildingInstance().setEnableAes256Sha256RsaPss(value);
            return getSelf();
        }


        @Deprecated
        public B enableBasic256(boolean value) {
            getBuildingInstance().setEnableBasic256(value);
            return getSelf();
        }


        @Deprecated
        public B enableBasic128Rsa15(boolean value) {
            getBuildingInstance().setEnableBasic128Rsa15(value);
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
