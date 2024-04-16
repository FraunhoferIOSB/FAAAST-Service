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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType;


/**
 *
 * Describes the security configuration of an endpoint.
 */
public class EndpointSecurityConfiguration {

    private static final List<UserTokenType> SUPPORTED_USER_TOKEN_POLICIES = List.of(UserTokenType.Anonymous, UserTokenType.UserName, UserTokenType.Certificate);

    public static final EndpointSecurityConfiguration NO_SECURITY_ANONYMOUS = new EndpointSecurityConfiguration(
            SecurityPolicy.None,
            MessageSecurityMode.None,
            Protocol.TCP,
            UserTokenType.Anonymous);

    public static final EndpointSecurityConfiguration NONE_NONE_TCP = new EndpointSecurityConfiguration(
            SecurityPolicy.None,
            MessageSecurityMode.None,
            Protocol.TCP);

    public static final EndpointSecurityConfiguration NONE_NONE_HTTPS = new EndpointSecurityConfiguration(
            SecurityPolicy.None,
            MessageSecurityMode.None,
            Protocol.HTTPS);

    public static final EndpointSecurityConfiguration BASIC256_SIGN_TCP = new EndpointSecurityConfiguration(
            SecurityPolicy.Basic256,
            MessageSecurityMode.Sign,
            Protocol.TCP);

    public static final EndpointSecurityConfiguration BASIC256_SIGN_HTTPS = new EndpointSecurityConfiguration(
            SecurityPolicy.Basic256,
            MessageSecurityMode.Sign,
            Protocol.HTTPS);

    public static final EndpointSecurityConfiguration BASIC256_SIGN_ENCRYPT_TCP = new EndpointSecurityConfiguration(
            SecurityPolicy.Basic256,
            MessageSecurityMode.SignAndEncrypt,
            Protocol.TCP);
    public static final EndpointSecurityConfiguration BASIC256_SIGN_ENCRYPT_HTTPS = new EndpointSecurityConfiguration(
            SecurityPolicy.Basic256,
            MessageSecurityMode.SignAndEncrypt,
            Protocol.HTTPS);

    public static final EndpointSecurityConfiguration BASIC256SHA256_SIGN_TCP = new EndpointSecurityConfiguration(
            SecurityPolicy.Basic256Sha256,
            MessageSecurityMode.Sign,
            Protocol.TCP);
    public static final EndpointSecurityConfiguration BASIC256SHA256_SIGN_HTTPS = new EndpointSecurityConfiguration(
            SecurityPolicy.Basic256Sha256,
            MessageSecurityMode.Sign,
            Protocol.HTTPS);
    public static final EndpointSecurityConfiguration BASIC256SHA256_SIGN_ENCRYPT_TCP = new EndpointSecurityConfiguration(
            SecurityPolicy.Basic256Sha256,
            MessageSecurityMode.SignAndEncrypt,
            Protocol.TCP);

    public static final EndpointSecurityConfiguration BASIC256SHA256_SIGN_ENCRYPT_HTTPS = new EndpointSecurityConfiguration(
            SecurityPolicy.Basic256Sha256,
            MessageSecurityMode.SignAndEncrypt,
            Protocol.HTTPS);

    public static final EndpointSecurityConfiguration AES256_SHA256_RSAPSS_SIGN_TCP = new EndpointSecurityConfiguration(
            SecurityPolicy.Aes256_Sha256_RsaPss,
            MessageSecurityMode.Sign,
            Protocol.TCP);
    public static final EndpointSecurityConfiguration AES256_SHA256_RSAPSS_SIGN_HTTPS = new EndpointSecurityConfiguration(
            SecurityPolicy.Aes256_Sha256_RsaPss,
            MessageSecurityMode.Sign,
            Protocol.HTTPS);

    public static final EndpointSecurityConfiguration AES256_SHA256_RSAPSS_SIGN_ENCRYPT_TCP = new EndpointSecurityConfiguration(
            SecurityPolicy.Aes256_Sha256_RsaPss,
            MessageSecurityMode.SignAndEncrypt,
            Protocol.TCP);

    public static final EndpointSecurityConfiguration AES256_SHA256_RSAPSS_SIGN_ENCRYPT_HTTPS = new EndpointSecurityConfiguration(
            SecurityPolicy.Aes256_Sha256_RsaPss,
            MessageSecurityMode.SignAndEncrypt,
            Protocol.HTTPS);

    public static final EndpointSecurityConfiguration AES128_SHA256_RSAOAEP_SIGN_TCP = new EndpointSecurityConfiguration(
            SecurityPolicy.Aes128_Sha256_RsaOaep,
            MessageSecurityMode.Sign,
            Protocol.TCP);

    public static final EndpointSecurityConfiguration AES128_SHA256_RSAOAEP_SIGN_HTTPS = new EndpointSecurityConfiguration(
            SecurityPolicy.Aes128_Sha256_RsaOaep,
            MessageSecurityMode.Sign,
            Protocol.HTTPS);

    public static final EndpointSecurityConfiguration AES128_SHA256_RSAOAEP_SIGN_ENCRYPT_TCP = new EndpointSecurityConfiguration(
            SecurityPolicy.Aes128_Sha256_RsaOaep,
            MessageSecurityMode.SignAndEncrypt,
            Protocol.TCP);

    public static final EndpointSecurityConfiguration AES128_SHA256_RSAOAEP_SIGN_ENCRYPT_HTTPS = new EndpointSecurityConfiguration(
            SecurityPolicy.Aes128_Sha256_RsaOaep,
            MessageSecurityMode.SignAndEncrypt,
            Protocol.HTTPS);

    public static final EndpointSecurityConfiguration BASIC128_RSA15_SIGN_TCP = new EndpointSecurityConfiguration(
            SecurityPolicy.Basic128Rsa15,
            MessageSecurityMode.Sign,
            Protocol.TCP);

    public static final EndpointSecurityConfiguration BASIC128_RSA15_SIGN_HTTPS = new EndpointSecurityConfiguration(
            SecurityPolicy.Basic128Rsa15,
            MessageSecurityMode.Sign,
            Protocol.HTTPS);

    public static final EndpointSecurityConfiguration BASIC128_RSA15_SIGN_ENCRYPT_TCP = new EndpointSecurityConfiguration(
            SecurityPolicy.Basic128Rsa15,
            MessageSecurityMode.SignAndEncrypt,
            Protocol.TCP);

    public static final EndpointSecurityConfiguration BASIC128_RSA15_SIGN_ENCRYPT_HTTPS = new EndpointSecurityConfiguration(
            SecurityPolicy.Basic128Rsa15,
            MessageSecurityMode.SignAndEncrypt,
            Protocol.HTTPS);

    public static final List<EndpointSecurityConfiguration> POLICY_NONE = List.of(
            NONE_NONE_TCP,
            NONE_NONE_HTTPS);

    public static final List<EndpointSecurityConfiguration> POLICY_BASIC256 = List.of(
            BASIC256_SIGN_TCP,
            BASIC256_SIGN_HTTPS,
            BASIC256_SIGN_ENCRYPT_TCP,
            BASIC256_SIGN_ENCRYPT_HTTPS);

    public static final List<EndpointSecurityConfiguration> POLICY_BASIC256SHA256 = List.of(
            BASIC256SHA256_SIGN_TCP,
            BASIC256SHA256_SIGN_HTTPS,
            BASIC256SHA256_SIGN_ENCRYPT_TCP,
            BASIC256SHA256_SIGN_ENCRYPT_HTTPS);

    public static final List<EndpointSecurityConfiguration> POLICY_AES256_SHA256_RSAPSS = List.of(
            AES256_SHA256_RSAPSS_SIGN_TCP,
            AES256_SHA256_RSAPSS_SIGN_HTTPS,
            AES256_SHA256_RSAPSS_SIGN_ENCRYPT_TCP,
            AES256_SHA256_RSAPSS_SIGN_ENCRYPT_HTTPS);

    public static final List<EndpointSecurityConfiguration> POLICY_AES128_SHA256_RSAOAEP = List.of(
            AES128_SHA256_RSAOAEP_SIGN_TCP,
            AES128_SHA256_RSAOAEP_SIGN_HTTPS,
            AES128_SHA256_RSAOAEP_SIGN_ENCRYPT_TCP,
            AES128_SHA256_RSAOAEP_SIGN_ENCRYPT_HTTPS);

    public static final List<EndpointSecurityConfiguration> POLICY_BASIC128_RSA15 = List.of(
            BASIC128_RSA15_SIGN_TCP,
            BASIC128_RSA15_SIGN_HTTPS,
            BASIC128_RSA15_SIGN_ENCRYPT_TCP,
            BASIC128_RSA15_SIGN_ENCRYPT_HTTPS);

    public static final List<EndpointSecurityConfiguration> ALL = List.of(
            NONE_NONE_TCP,
            NONE_NONE_HTTPS,
            BASIC256_SIGN_TCP,
            BASIC256_SIGN_HTTPS,
            BASIC256_SIGN_ENCRYPT_TCP,
            BASIC256_SIGN_ENCRYPT_HTTPS,
            BASIC256SHA256_SIGN_TCP,
            BASIC256SHA256_SIGN_HTTPS,
            BASIC256SHA256_SIGN_ENCRYPT_TCP,
            BASIC256SHA256_SIGN_ENCRYPT_HTTPS,
            AES256_SHA256_RSAPSS_SIGN_TCP,
            AES256_SHA256_RSAPSS_SIGN_HTTPS,
            AES256_SHA256_RSAPSS_SIGN_ENCRYPT_TCP,
            AES256_SHA256_RSAPSS_SIGN_ENCRYPT_HTTPS,
            AES128_SHA256_RSAOAEP_SIGN_TCP,
            AES128_SHA256_RSAOAEP_SIGN_HTTPS,
            AES128_SHA256_RSAOAEP_SIGN_ENCRYPT_TCP,
            AES128_SHA256_RSAOAEP_SIGN_ENCRYPT_HTTPS,
            BASIC128_RSA15_SIGN_TCP,
            BASIC128_RSA15_SIGN_HTTPS,
            BASIC128_RSA15_SIGN_ENCRYPT_TCP,
            BASIC128_RSA15_SIGN_ENCRYPT_HTTPS);

    private static final SecurityPolicy DEFAULT_POLICY = SecurityPolicy.None;
    private static final MessageSecurityMode DEFAULT_SECURITY_MODE = MessageSecurityMode.None;
    private static final Protocol DEFAULT_PROTOCOL = Protocol.TCP;
    private static final Set<UserTokenType> DEFAULT_TOKEN_POLICIES = Collections.singleton(UserTokenType.Anonymous);

    private SecurityPolicy policy;
    private MessageSecurityMode securityMode;
    private Protocol protocol;
    private Set<UserTokenType> tokenPolicies;

    public EndpointSecurityConfiguration() {
        this.policy = DEFAULT_POLICY;
        this.securityMode = DEFAULT_SECURITY_MODE;
        this.protocol = DEFAULT_PROTOCOL;
        this.tokenPolicies = new HashSet<>(DEFAULT_TOKEN_POLICIES);
    }


    public EndpointSecurityConfiguration(SecurityPolicy policy, MessageSecurityMode securityMode, Protocol protocol, UserTokenType... tokenPolicies) {
        this.policy = policy;
        this.securityMode = securityMode;
        this.protocol = protocol;
        this.tokenPolicies = new HashSet<>(Objects.nonNull(tokenPolicies) ? Arrays.asList(tokenPolicies) : SUPPORTED_USER_TOKEN_POLICIES);
    }


    public SecurityPolicy getPolicy() {
        return policy;
    }


    public MessageSecurityMode getSecurityMode() {
        return securityMode;
    }


    public Protocol getProtocol() {
        return protocol;
    }


    public Set<UserTokenType> getTokenPolicies() {
        return tokenPolicies;
    }


    @Override
    public int hashCode() {
        return Objects.hash(policy, securityMode, protocol, tokenPolicies);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EndpointSecurityConfiguration that = (EndpointSecurityConfiguration) o;
        return super.equals(that)
                && Objects.equals(policy, that.policy)
                && Objects.equals(securityMode, that.securityMode)
                && Objects.equals(protocol, that.protocol)
                && Objects.equals(tokenPolicies, that.tokenPolicies);
    }


    @Override
    public String toString() {
        return String.format("policy: %s, securityMode: %s, protocol: %s, tokenPolicies: %s",
                policy,
                securityMode,
                protocol,
                tokenPolicies.stream()
                        .map(x -> x.name())
                        .collect(Collectors.joining(", ")));
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ExtendableBuilder<EndpointSecurityConfiguration, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected EndpointSecurityConfiguration newBuildingInstance() {
            return new EndpointSecurityConfiguration();
        }


        public Builder policy(SecurityPolicy value) {
            getBuildingInstance().policy = value;
            return getSelf();
        }


        public Builder securityMode(MessageSecurityMode value) {
            getBuildingInstance().securityMode = value;
            return getSelf();
        }


        public Builder protocol(Protocol value) {
            getBuildingInstance().protocol = value;
            return getSelf();
        }


        public Builder tokenPolicies(Set<UserTokenType> value) {
            getBuildingInstance().tokenPolicies = value;
            return getSelf();
        }


        public Builder tokenPolicy(UserTokenType value) {
            getBuildingInstance().tokenPolicies.add(value);
            return getSelf();
        }
    }

}
