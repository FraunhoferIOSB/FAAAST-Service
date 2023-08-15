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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.OpcUaOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.OpcUaSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.OpcUaValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import java.nio.file.Path;
import java.util.Objects;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType;


/**
 * Config file for {@link OpcUaAssetConnection}.
 */
public class OpcUaAssetConnectionConfig
        extends AssetConnectionConfig<OpcUaAssetConnection, OpcUaValueProviderConfig, OpcUaOperationProviderConfig, OpcUaSubscriptionProviderConfig> {

    public static final int DEFAULT_REQUEST_TIMEOUT = 3000;
    public static final int DEFAULT_ACKNOWLEDGE_TIMEOUT = 10000;
    public static final int DEFAULT_RETRIES = 1;
    public static final Path DEFAULT_SECURITY_BASEDIR = Path.of(".");
    public static final SecurityPolicy DEFAULT_SECURITY_POLICY = SecurityPolicy.None;
    public static final MessageSecurityMode DEFAULT_SECURITY_MODE = MessageSecurityMode.None;
    public static final String DEFAULT_APPLICATION_CERTIFICATE_FILE = "application.p12";
    public static final String DEFAULT_AUTHENTICATION_CERTIFICATE_FILE = "authentication.p12";
    public static final TransportProfile DEFAULT_TRANSPORT_PROFILE = TransportProfile.TCP_UASC_UABINARY;
    public static final UserTokenType DEFAULT_USER_TOKEN = UserTokenType.Anonymous;

    private String host;
    private String username;
    private String password;
    private int requestTimeout;
    private int acknowledgeTimeout;
    private int retries;
    private Path securityBaseDir;
    private SecurityPolicy securityPolicy;
    private MessageSecurityMode securityMode;
    private CertificateConfig applicationCertificate;
    private CertificateConfig authenticationCertificate;
    private TransportProfile transportProfile;
    private UserTokenType userTokenType;

    public OpcUaAssetConnectionConfig() {
        this.requestTimeout = DEFAULT_REQUEST_TIMEOUT;
        this.acknowledgeTimeout = DEFAULT_ACKNOWLEDGE_TIMEOUT;
        this.retries = DEFAULT_RETRIES;
        this.securityBaseDir = DEFAULT_SECURITY_BASEDIR;
        this.securityPolicy = DEFAULT_SECURITY_POLICY;
        this.securityMode = DEFAULT_SECURITY_MODE;
        this.applicationCertificate = CertificateConfig.builder()
                .keyStorePath(DEFAULT_APPLICATION_CERTIFICATE_FILE)
                .build();
        this.authenticationCertificate = CertificateConfig.builder()
                .keyStorePath(DEFAULT_AUTHENTICATION_CERTIFICATE_FILE)
                .build();
        this.transportProfile = DEFAULT_TRANSPORT_PROFILE;
        this.userTokenType = DEFAULT_USER_TOKEN;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpcUaAssetConnectionConfig that = (OpcUaAssetConnectionConfig) o;
        return super.equals(that)
                && Objects.equals(host, that.host)
                && Objects.equals(username, that.username)
                && Objects.equals(password, that.password)
                && Objects.equals(requestTimeout, that.requestTimeout)
                && Objects.equals(acknowledgeTimeout, that.acknowledgeTimeout)
                && Objects.equals(retries, that.retries)
                && Objects.equals(securityBaseDir, that.securityBaseDir)
                && Objects.equals(securityPolicy, that.securityPolicy)
                && Objects.equals(securityMode, that.securityMode)
                && Objects.equals(applicationCertificate, that.applicationCertificate)
                && Objects.equals(authenticationCertificate, that.authenticationCertificate)
                && Objects.equals(transportProfile, that.transportProfile)
                && Objects.equals(userTokenType, that.userTokenType);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                host,
                username,
                password,
                requestTimeout,
                acknowledgeTimeout,
                retries,
                securityBaseDir,
                securityPolicy,
                securityMode,
                applicationCertificate,
                authenticationCertificate,
                transportProfile,
                userTokenType);
    }


    public String getHost() {
        return host;
    }


    public void setHost(String host) {
        this.host = host;
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public int getRequestTimeout() {
        return requestTimeout;
    }


    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }


    public int getAcknowledgeTimeout() {
        return acknowledgeTimeout;
    }


    public void setAcknowledgeTimeout(int acknowledgeTimeout) {
        this.acknowledgeTimeout = acknowledgeTimeout;
    }


    public int getRetries() {
        return retries;
    }


    public void setRetries(int retries) {
        this.retries = retries;
    }


    public Path getSecurityBaseDir() {
        return securityBaseDir;
    }


    public void setSecurityBaseDir(Path securityBaseDir) {
        this.securityBaseDir = securityBaseDir;
    }


    public SecurityPolicy getSecurityPolicy() {
        return securityPolicy;
    }


    public void setSecurityPolicy(SecurityPolicy securityPolicy) {
        this.securityPolicy = securityPolicy;
    }


    public MessageSecurityMode getSecurityMode() {
        return securityMode;
    }


    public void setSecurityMode(MessageSecurityMode securityMode) {
        this.securityMode = securityMode;
    }


    public CertificateConfig getApplicationCertificate() {
        return applicationCertificate;
    }


    public void setApplicationCertificate(CertificateConfig applicationCertificate) {
        this.applicationCertificate = applicationCertificate;
    }


    public CertificateConfig getAuthenticationCertificate() {
        return authenticationCertificate;
    }


    public void setAuthenticationCertificate(CertificateConfig authenticationCertificate) {
        this.authenticationCertificate = authenticationCertificate;
    }


    public TransportProfile getTransportProfile() {
        return transportProfile;
    }


    public void setTransportProfile(TransportProfile transportProfile) {
        this.transportProfile = transportProfile;
    }


    public UserTokenType getUserTokenType() {
        return userTokenType;
    }


    public void setUserTokenType(UserTokenType userTokenType) {
        this.userTokenType = userTokenType;
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends OpcUaAssetConnectionConfig, B extends AbstractBuilder<T, B>>
            extends
            AssetConnectionConfig.AbstractBuilder<OpcUaAssetConnectionConfig, OpcUaValueProviderConfig, OpcUaValueProvider, OpcUaOperationProviderConfig, OpcUaOperationProvider, OpcUaSubscriptionProviderConfig, OpcUaSubscriptionProvider, OpcUaAssetConnection, B> {

        @Override
        public B of(OpcUaAssetConnectionConfig other) {
            super.of(other);
            acknowledgeTimeout(other.acknowledgeTimeout);
            applicationCertificate(other.applicationCertificate);
            authenticationCertificate(other.authenticationCertificate);
            host(other.host);
            password(other.password);
            requestTimeout(other.requestTimeout);
            retries(other.retries);
            securityBaseDir(other.securityBaseDir);
            securityMode(other.securityMode);
            securityPolicy(other.securityPolicy);
            username(other.username);
            transportProfile(other.transportProfile);
            userTokenType(other.userTokenType);
            return getSelf();
        }


        public B host(String value) {
            getBuildingInstance().setHost(value);
            return getSelf();
        }


        public B username(String value) {
            getBuildingInstance().setUsername(value);
            return getSelf();
        }


        public B password(String value) {
            getBuildingInstance().setPassword(value);
            return getSelf();
        }


        public B requestTimeout(int value) {
            getBuildingInstance().setRequestTimeout(value);
            return getSelf();
        }


        public B acknowledgeTimeout(int value) {
            getBuildingInstance().setAcknowledgeTimeout(value);
            return getSelf();
        }


        public B retries(int value) {
            getBuildingInstance().setRetries(value);
            return getSelf();
        }


        public B securityBaseDir(Path value) {
            getBuildingInstance().setSecurityBaseDir(value);
            return getSelf();
        }


        public B securityPolicy(SecurityPolicy value) {
            getBuildingInstance().setSecurityPolicy(value);
            return getSelf();
        }


        public B securityMode(MessageSecurityMode value) {
            getBuildingInstance().setSecurityMode(value);
            return getSelf();
        }


        public B applicationCertificate(CertificateConfig value) {
            getBuildingInstance().setApplicationCertificate(value);
            return getSelf();
        }


        public B authenticationCertificate(CertificateConfig value) {
            getBuildingInstance().setAuthenticationCertificate(value);
            return getSelf();
        }


        public B transportProfile(TransportProfile value) {
            getBuildingInstance().setTransportProfile(value);
            return getSelf();
        }


        public B userTokenType(UserTokenType value) {
            getBuildingInstance().setUserTokenType(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<OpcUaAssetConnectionConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected OpcUaAssetConnectionConfig newBuildingInstance() {
            return new OpcUaAssetConnectionConfig();
        }
    }

}
