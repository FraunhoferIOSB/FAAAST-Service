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
package de.fraunhofer.iosb.ilt.faaast.service.certificate.util;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bouncycastle.cert.X509v3CertificateBuilder;


/**
 * Builder class for self-signed HTTPS certificates, based on Eclipse Milo (https://github.com/eclipse/milo).
 */
public class SelfSignedHttpsCertificateBuilder {

    /**
     * Signature Algorithm for SHA256 with RSA.
     */
    private static final String SA_SHA256_RSA = "SHA256withRSA";

    private final HttpsCertificateGenerator generator = new HttpsCertificateGenerator();

    private Period validityPeriod = Period.ofYears(3);
    private String commonName;
    private List<String> dnsNames = new ArrayList<>();
    private List<String> ipAddresses = new ArrayList<>();

    private final KeyPair keyPair;

    public SelfSignedHttpsCertificateBuilder(KeyPair keyPair) {
        this.keyPair = keyPair;
    }


    /**
     * Sets the validity period.
     *
     * @param validityPeriod the validity period to set.
     * @return the updated builder
     */
    public SelfSignedHttpsCertificateBuilder setValidityPeriod(Period validityPeriod) {
        this.validityPeriod = validityPeriod;
        return this;
    }


    /**
     * Set the Common Name, which should be the hostname when building an SSL/TLS certificate.
     *
     * @param commonName the hostname to place in the Common Name field.
     * @return {@code this} {@link SelfSignedHttpsCertificateBuilder}.
     */
    public SelfSignedHttpsCertificateBuilder setCommonName(String commonName) {
        this.commonName = commonName;
        return this;
    }


    /**
     * Sets the DNS name.
     *
     * @param dnsName the DNS name to set.
     * @return the updated builder
     */
    public SelfSignedHttpsCertificateBuilder addDnsName(String dnsName) {
        dnsNames.add(dnsName);
        return this;
    }


    /**
     * Sets the IP address.
     *
     * @param ipAddress the IP address to set.
     * @return the updated builder
     */
    public SelfSignedHttpsCertificateBuilder addIpAddress(String ipAddress) {
        ipAddresses.add(ipAddress);
        return this;
    }


    /**
     * Builds a new instance.
     *
     * @return thw new instance
     * @throws Exception if creating the certificate fails
     */
    public X509Certificate build() throws Exception {
        // Calculate start and end date based on validity period
        LocalDate now = LocalDate.now();
        LocalDate expiration = now.plus(validityPeriod);
        Date notBefore = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date notAfter = Date.from(expiration.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return generator.generateSelfSigned(
                keyPair,
                notBefore,
                notAfter,
                commonName,
                null,
                null,
                null,
                null,
                null,
                null,
                dnsNames,
                ipAddresses,
                SA_SHA256_RSA);
    }

    private static class HttpsCertificateGenerator extends SelfSignedCertificateGenerator {

        @Override
        protected void addKeyUsage(X509v3CertificateBuilder certificateBuilder) {
            // Don't set any KU fields
        }


        @Override
        protected void addExtendedKeyUsage(X509v3CertificateBuilder certificateBuilder) {
            // Don't set any EKU fields
        }

    }

}
