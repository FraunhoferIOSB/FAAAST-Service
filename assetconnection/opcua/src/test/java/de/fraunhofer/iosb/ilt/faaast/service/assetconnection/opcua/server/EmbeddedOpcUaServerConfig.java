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

import de.fraunhofer.iosb.ilt.faaast.service.certificate.CertificateData;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;


public class EmbeddedOpcUaServerConfig {

    private static final String DEFAULT_PATH = "/test";

    private Path securityBaseDir;
    private CertificateData applicationCertificate;
    private List<X509Certificate> allowedClientCertificates;
    private String path;
    private List<EndpointSecurityConfiguration> endpointSecurityConfigurations;
    private CertificateData httpsCertificate;
    private Map<String, String> allowedCredentials;
    private Map<Protocol, Integer> protocolPorts;

    public EmbeddedOpcUaServerConfig() {
        this.path = DEFAULT_PATH;
        this.allowedClientCertificates = Collections.synchronizedList(new ArrayList<>());
        this.endpointSecurityConfigurations = new ArrayList<>();
        this.protocolPorts = new HashMap<>();
        this.allowedCredentials = new HashMap<>();
        // for discovery it's necessary that TCP is always enabled
        this.protocolPorts.put(Protocol.TCP, PortHelper.findFreePort());
        try {
            this.securityBaseDir = Files.createTempDirectory("embedded-opcua-server");
        }
        catch (IOException ex) {
            throw new IllegalArgumentException("unable to create temp directory for server");
        }
    }


    private void assignFreePorts() {
        protocolPorts.entrySet().stream()
                .filter(x -> Objects.isNull(x.getValue()))
                .forEach(x -> protocolPorts.put(x.getKey(), PortHelper.findFreePort()));
    }


    public Path getSecurityBaseDir() {
        return securityBaseDir;
    }


    public CertificateData getApplicationCertificate() {
        return applicationCertificate;
    }


    public List<X509Certificate> getAllowedClientCertificates() {
        return allowedClientCertificates;
    }


    public String getPath() {
        return path;
    }


    public List<EndpointSecurityConfiguration> getEndpointSecurityConfigurations() {
        return endpointSecurityConfigurations;
    }


    public CertificateData getHttpsCertificate() {
        return httpsCertificate;
    }


    public Map<Protocol, Integer> getProtocolPorts() {
        return protocolPorts;
    }


    public void setSecurityBaseDir(Path securityBaseDir) {
        this.securityBaseDir = securityBaseDir;
    }


    public void setApplicationCertificate(CertificateData applicationCertificate) {
        this.applicationCertificate = applicationCertificate;
    }


    public void setAllowedClientCertificates(List<X509Certificate> allowedClientCertificates) {
        this.allowedClientCertificates = allowedClientCertificates;
    }


    public void setPath(String path) {
        this.path = path;
    }


    public void setEndpointSecurityConfigurations(List<EndpointSecurityConfiguration> endpointSecurityConfigurations) {
        this.endpointSecurityConfigurations = endpointSecurityConfigurations;
    }


    public void setHttpsCertificate(CertificateData httpsCertificate) {
        this.httpsCertificate = httpsCertificate;
    }


    public Map<String, String> getAllowedCredentials() {
        return allowedCredentials;
    }


    public void setAllowedCredentials(Map<String, String> allowedCredentials) {
        this.allowedCredentials = allowedCredentials;
    }


    public void setProtocolPorts(Map<Protocol, Integer> value) {
        this.protocolPorts = value;
        assignFreePorts();
    }


    private void updateProtocolPorts() {
        endpointSecurityConfigurations.stream()
                .map(x -> x.getProtocol())
                .distinct()
                .filter(x -> !protocolPorts.containsKey(x))
                .forEach(x -> protocolPorts.put(x, PortHelper.findFreePort()));
    }


    @Override
    public int hashCode() {
        return Objects.hash(securityBaseDir,
                applicationCertificate,
                allowedClientCertificates,
                path,
                endpointSecurityConfigurations,
                httpsCertificate,
                allowedCredentials,
                protocolPorts);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EmbeddedOpcUaServerConfig that = (EmbeddedOpcUaServerConfig) o;
        return super.equals(that)
                && Objects.equals(securityBaseDir, that.securityBaseDir)
                && Objects.equals(applicationCertificate, that.applicationCertificate)
                && Objects.equals(allowedClientCertificates, that.allowedClientCertificates)
                && Objects.equals(path, that.path)
                && Objects.equals(endpointSecurityConfigurations, that.endpointSecurityConfigurations)
                && Objects.equals(httpsCertificate, that.httpsCertificate)
                && Objects.equals(allowedCredentials, that.allowedCredentials)
                && Objects.equals(protocolPorts, that.protocolPorts);
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ExtendableBuilder<EmbeddedOpcUaServerConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected EmbeddedOpcUaServerConfig newBuildingInstance() {
            return new EmbeddedOpcUaServerConfig();
        }


        public Builder securityBaseDir(Path value) {
            getBuildingInstance().setSecurityBaseDir(value);
            return getSelf();
        }


        public Builder applicationCertificate(CertificateData value) {
            getBuildingInstance().setApplicationCertificate(value);
            return getSelf();
        }


        public Builder allowedClientCertificates(List<X509Certificate> value) {
            getBuildingInstance().setAllowedClientCertificates(value);
            return getSelf();
        }


        public Builder allowClient(X509Certificate value) {
            getBuildingInstance().getAllowedClientCertificates().add(value);
            return getSelf();
        }


        public Builder path(String value) {
            getBuildingInstance().setPath(value);
            return getSelf();
        }


        public Builder httpsCertificate(CertificateData value) {
            getBuildingInstance().setHttpsCertificate(value);
            return getSelf();
        }


        public Builder endpointSecurityConfigurations(List<EndpointSecurityConfiguration> value) {
            getBuildingInstance().setEndpointSecurityConfigurations(value);
            getBuildingInstance().updateProtocolPorts();
            return getSelf();
        }


        public Builder endpointSecurityConfiguration(EndpointSecurityConfiguration value) {
            getBuildingInstance().getEndpointSecurityConfigurations().add(value);
            getBuildingInstance().updateProtocolPorts();
            return getSelf();
        }


        public Builder allowedCredentials(Map<String, String> value) {
            getBuildingInstance().setAllowedCredentials(value);
            return getSelf();
        }


        public Builder allowedCredential(String username, String password) {
            getBuildingInstance().getAllowedCredentials().put(username, password);
            return getSelf();
        }


        public Builder protocol(TransportProfile value) {
            getBuildingInstance().getProtocolPorts().put(Protocol.from(value), PortHelper.findFreePort());
            return getSelf();
        }


        public Builder protocol(Protocol value) {
            getBuildingInstance().getProtocolPorts().put(value, PortHelper.findFreePort());
            return getSelf();
        }


        public Builder protocolPort(TransportProfile value, int port) {
            getBuildingInstance().getProtocolPorts().put(Protocol.from(value), port);
            return getSelf();
        }


        public Builder protocolPort(Protocol value, int port) {
            getBuildingInstance().getProtocolPorts().put(value, port);
            return getSelf();
        }


        public Builder protocolPorts(Map<Protocol, Integer> value) {
            getBuildingInstance().setProtocolPorts(value);
            return getSelf();
        }
    }

}
