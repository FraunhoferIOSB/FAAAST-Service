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
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Builder class for self-signed certificates, based on Eclipse Milo (https://github.com/eclipse/milo).
 */
public class SelfSignedCertificateBuilder {

    /**
     * Signature Algorithm for SHA1 with RSA.
     *
     * <p>SHA1 was broken in 2017 and this algorithm should not be used.
     */
    public static final String SA_SHA1_RSA = "SHA1withRSA";

    /**
     * Signature Algorithm for SHA256 with RSA.
     */
    public static final String SA_SHA256_RSA = "SHA256withRSA";

    /**
     * Signature Algorithm for SHA256 with ECDSA.
     *
     * <p>May only be uses with EC-based KeyPairs and security profiles.
     */
    public static final String SA_SHA256_ECDSA = "SHA256withECDSA";

    private Period validityPeriod = Period.ofYears(3);

    private String commonName = "";
    private String organization = "";
    private String organizationalUnit = "";
    private String localityName = "";
    private String stateName = "";
    private String countryCode = "";

    private String applicationUri = "";
    private List<String> dnsNames = new ArrayList<>();
    private List<String> ipAddresses = new ArrayList<>();
    private String signatureAlgorithm = SA_SHA256_RSA;

    private final KeyPair keyPair;
    private final SelfSignedCertificateGenerator generator;

    public SelfSignedCertificateBuilder(KeyPair keyPair) {
        this(keyPair, new SelfSignedCertificateGenerator());
    }


    public SelfSignedCertificateBuilder(KeyPair keyPair, SelfSignedCertificateGenerator generator) {
        this.keyPair = keyPair;
        this.generator = generator;

        PublicKey publicKey = keyPair.getPublic();

        if (publicKey instanceof RSAPublicKey) {
            signatureAlgorithm = SA_SHA256_RSA;

            int bitLength = ((RSAPublicKey) keyPair.getPublic()).getModulus().bitLength();

            if (bitLength <= 1024) {
                Logger logger = LoggerFactory.getLogger(getClass());
                logger.warn("Using legacy key size: {}", bitLength);
            }
        }
        else if (keyPair.getPublic() instanceof ECPublicKey) {
            signatureAlgorithm = SA_SHA256_ECDSA;
        }
    }


    /**
     * Sets the validity period.
     *
     * @param validityPeriod the validity period to set.
     * @return the updated builder
     */
    public SelfSignedCertificateBuilder setValidityPeriod(Period validityPeriod) {
        this.validityPeriod = validityPeriod;
        return this;
    }


    /**
     * Sets the common name.
     *
     * @param commonName the common name to set.
     * @return the updated builder
     */
    public SelfSignedCertificateBuilder setCommonName(String commonName) {
        this.commonName = commonName;
        return this;
    }


    /**
     * Sets the organization.
     *
     * @param organization the organization to set.
     * @return the updated builder
     */
    public SelfSignedCertificateBuilder setOrganization(String organization) {
        this.organization = organization;
        return this;
    }


    /**
     * Sets the organizational unit.
     *
     * @param organizationalUnit the organizational unit to set.
     * @return the updated builder
     */
    public SelfSignedCertificateBuilder setOrganizationalUnit(String organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
        return this;
    }


    /**
     * Sets the locality name.
     *
     * @param localityName the locality name to set.
     * @return the updated builder
     */
    public SelfSignedCertificateBuilder setLocalityName(String localityName) {
        this.localityName = localityName;
        return this;
    }


    /**
     * Sets the state name.
     *
     * @param stateName the state name to set.
     * @return the updated builder
     */
    public SelfSignedCertificateBuilder setStateName(String stateName) {
        this.stateName = stateName;
        return this;
    }


    /**
     * Sets the country code.
     *
     * @param countryCode the country code to set.
     * @return the updated builder
     */
    public SelfSignedCertificateBuilder setCountryCode(String countryCode) {
        this.countryCode = countryCode;
        return this;
    }


    /**
     * Sets the application URI.
     *
     * @param applicationUri the application URI to set.
     * @return the updated builder
     */
    public SelfSignedCertificateBuilder setApplicationUri(String applicationUri) {
        this.applicationUri = applicationUri;
        return this;
    }


    /**
     * Sets the DNS name.
     *
     * @param dnsName the DNS name to set.
     * @return the updated builder
     */
    public SelfSignedCertificateBuilder addDnsName(String dnsName) {
        dnsNames.add(dnsName);
        return this;
    }


    /**
     * Sets the IP address.
     *
     * @param ipAddress the IP address to set.
     * @return the updated builder
     */
    public SelfSignedCertificateBuilder addIpAddress(String ipAddress) {
        ipAddresses.add(ipAddress);
        return this;
    }


    /**
     * Sets the signature algorithm.
     *
     * @param signatureAlgorithm the signature algorithm to set.
     * @return the updated builder
     */
    public SelfSignedCertificateBuilder setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
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
                organization,
                organizationalUnit,
                localityName,
                stateName,
                countryCode,
                applicationUri,
                dnsNames,
                ipAddresses,
                signatureAlgorithm);
    }

}
