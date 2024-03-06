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
package de.fraunhofer.iosb.ilt.faaast.service.certificate;

import de.fraunhofer.iosb.ilt.faaast.service.util.HostnameUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.AbstractBuilder;


/**
 * Information required to create a certificate for OPC UA.
 */
public class CertificateInformation {

    private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private String commonName;
    private String organization;
    private String organizationUnit;
    private String localityName;
    private String countryCode;
    private String applicationUri;
    private List<String> dnsNames;
    private List<String> ipAddresses;

    public CertificateInformation() {
        this.dnsNames = new ArrayList<>();
        this.ipAddresses = new ArrayList<>();
    }


    /**
     * Detects all available IP addresses and DNS names and adds them.
     */
    public void autodetectDnsAndIp() {
        ipAddresses.clear();
        dnsNames.clear();
        for (var entry: HostnameUtil.getHostnames("0.0.0.0")) {
            if (IP_ADDRESS_PATTERN.matcher(entry).matches()) {
                ipAddresses.add(entry);
            }
            else {
                dnsNames.add(entry);
            }
        }
    }


    public String getCommonName() {
        return commonName;
    }


    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }


    public String getOrganization() {
        return organization;
    }


    public void setOrganization(String organization) {
        this.organization = organization;
    }


    public String getOrganizationUnit() {
        return organizationUnit;
    }


    public void setOrganizationUnit(String organizationUnit) {
        this.organizationUnit = organizationUnit;
    }


    public String getLocalityName() {
        return localityName;
    }


    public void setLocalityName(String localityName) {
        this.localityName = localityName;
    }


    public String getCountryCode() {
        return countryCode;
    }


    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }


    public String getApplicationUri() {
        return applicationUri;
    }


    public void setApplicationUri(String applicationUri) {
        this.applicationUri = applicationUri;
    }


    public List<String> getDnsNames() {
        return dnsNames;
    }


    public void setDnsNames(List<String> dnsNames) {
        this.dnsNames = dnsNames;
    }


    public List<String> getIpAddresses() {
        return ipAddresses;
    }


    public void setIpAddresses(List<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CertificateInformation that = (CertificateInformation) o;
        return Objects.equals(commonName, that.commonName)
                && Objects.equals(organization, that.organization)
                && Objects.equals(organizationUnit, that.organizationUnit)
                && Objects.equals(localityName, that.localityName)
                && Objects.equals(countryCode, that.countryCode)
                && Objects.equals(applicationUri, that.applicationUri)
                && Objects.equals(dnsNames, that.dnsNames)
                && Objects.equals(ipAddresses, that.ipAddresses);
    }


    @Override
    public int hashCode() {
        return Objects.hash(commonName,
                organization,
                organizationUnit,
                localityName,
                countryCode,
                applicationUri,
                dnsNames,
                ipAddresses);
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<CertificateInformation> {

        public Builder commonName(String value) {
            getBuildingInstance().setCommonName(value);
            return this;
        }


        public Builder organization(String value) {
            getBuildingInstance().setOrganization(value);
            return this;
        }


        public Builder organizationUnit(String value) {
            getBuildingInstance().setOrganizationUnit(value);
            return this;
        }


        public Builder localityName(String value) {
            getBuildingInstance().setLocalityName(value);
            return this;
        }


        public Builder countryCode(String value) {
            getBuildingInstance().setCountryCode(value);
            return this;
        }


        public Builder applicationUri(String value) {
            getBuildingInstance().setApplicationUri(value);
            return this;
        }


        public Builder dnsNames(List<String> value) {
            getBuildingInstance().setDnsNames(value);
            return this;
        }


        public Builder dnsName(String value) {
            getBuildingInstance().getDnsNames().add(value);
            return this;
        }


        public Builder ipAddresses(List<String> value) {
            getBuildingInstance().setIpAddresses(value);
            return this;
        }


        public Builder ipAddress(String value) {
            getBuildingInstance().getIpAddresses().add(value);
            return this;
        }


        public Builder autodetectDnsAndIp() {
            getBuildingInstance().autodetectDnsAndIp();
            return this;
        }


        @Override
        protected CertificateInformation newBuildingInstance() {
            return new CertificateInformation();
        }
    }
}
