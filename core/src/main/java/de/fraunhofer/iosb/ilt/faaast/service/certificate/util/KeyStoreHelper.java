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

import de.fraunhofer.iosb.ilt.faaast.service.certificate.CertificateData;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.CertificateInformation;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Objects;


/**
 * Helper class for reading PCKS12 keystores.
 */
public class KeyStoreHelper {

    public static final String KEYSTORE_TYPE = "PKCS12";
    public static final String DEFAULT_ALIAS = "faaast";
    public static final String LOCALHOST = "localhost";
    public static final String LOCALHOST_IP = "127.0.0.1";

    /**
     * Hide the implicit public constructor.
     */
    private KeyStoreHelper() {

    }


    /**
     * Save the given file to the key store.
     *
     * @param file the file to write to
     * @param certificateData the certificate data
     * @param password the password to set
     * @throws IOException if writing to the file fails
     * @throws GeneralSecurityException if generating the certificate fails
     */
    public static void save(File file, CertificateData certificateData, String password) throws IOException, GeneralSecurityException {
        KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);
        keystore.load(null, passwordToChar(password));
        keystore.setCertificateEntry(DEFAULT_ALIAS, certificateData.getCertificate());
        keystore.setKeyEntry(DEFAULT_ALIAS, certificateData.getKeyPair().getPrivate(), passwordToChar(password), certificateData.getCertificateChain());
        try (OutputStream out = new FileOutputStream(file)) {
            keystore.store(out, passwordToChar(password));
        }
    }


    private static char[] passwordToChar(String password) {
        return StringHelper.isEmpty(password) ? new char[0] : password.toCharArray();
    }


    /**
     * Generates a self-signed certificate.
     *
     * @param certificateInformation the certificate informatino to use
     * @return a self-signed certificate
     * @throws KeyStoreException if generating certificate failed
     * @throws NoSuchAlgorithmException if generating private/public key pair fails due to missing algorithm
     * @throws UnknownHostException if localhost cannot be resolved
     */
    public static CertificateData generateSelfSigned(CertificateInformation certificateInformation) throws KeyStoreException, NoSuchAlgorithmException, UnknownHostException {
        Ensure.requireNonNull(certificateInformation, "certificateInformation must be non-null when key store does not exist");
        CertificateData result = new CertificateData();
        result.setKeyPair(SelfSignedCertificateGenerator.generateRsaKeyPair(2048));
        SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(result.getKeyPair())
                .setCommonName(certificateInformation.getCommonName())
                .setOrganization(certificateInformation.getOrganization())
                .setOrganizationalUnit(certificateInformation.getOrganizationUnit())
                .setLocalityName(certificateInformation.getLocalityName())
                .setCountryCode(certificateInformation.getCountryCode())
                .setApplicationUri(certificateInformation.getApplicationUri());
        // if no DNS & IP info available use localhost & 127.0.0.1
        if (certificateInformation.getDnsNames().isEmpty() && certificateInformation.getIpAddresses().isEmpty()) {
            certificateInformation.autodetectDnsAndIp();
            builder.addDnsName(LOCALHOST);
            builder.addIpAddress(LOCALHOST_IP);
        }
        certificateInformation.getDnsNames().forEach(builder::addDnsName);
        certificateInformation.getIpAddresses().forEach(builder::addIpAddress);
        try {
            X509Certificate certificate = builder.build();
            result.setCertificate(certificate);
            result.setCertificateChain(new X509Certificate[] {
                    certificate
            });
        }
        catch (Exception e) {
            throw new KeyStoreException("generating certificate failed", e);
        }
        return result;
    }


    /**
     * Loads relevant data for OPC UA from a key store or generates new one if it does not exist.
     *
     * @param file the keystore file
     * @param password the password to use
     * @param certificateInformation certificate information used when creating a new key store
     * @return relevant information from the key store
     * @throws java.io.IOException if file access fails
     * @throws java.security.GeneralSecurityException if reading/writing/generating certificate information fails
     * @throws IllegalArgumentException if file or alias is null or file does not exist and certificateInformation is
     *             null
     */
    public static CertificateData loadOrCreate(File file, String password, CertificateInformation certificateInformation)
            throws IOException,
            GeneralSecurityException {
        Ensure.requireNonNull(file, "file must be non-null");
        if (file.exists()) {
            try (InputStream inputStream = new FileInputStream(file)) {
                return loadOrDefault(inputStream, password, certificateInformation);
            }
        }
        CertificateData result = loadOrDefault(null, password, certificateInformation);
        save(file, result, password);
        return result;
    }


    /**
     * Loads relevant data for OPC UA from a key store or generates new one if it does not exist.
     *
     * @param file the keystore file
     * @param password the password to use
     * @return relevant information from the key store
     * @throws java.io.IOException if file access fails
     * @throws java.security.GeneralSecurityException if reading/writing/generating certificate information fails
     * @throws IllegalArgumentException if file or alias is null or file does not exist and certificateInformation is
     *             null
     */
    public static CertificateData load(File file, String password)
            throws IOException,
            GeneralSecurityException {
        Ensure.requireNonNull(file, "file must be non-null");
        try (InputStream inputStream = new FileInputStream(file)) {
            return load(inputStream, password);
        }
    }


    /**
     * Loads certificate data from a PKCS12 keystore.
     *
     * @param keystoreInputStream input stream containing the keystore. If it is null, new certificate data willl be
     *            generated.
     * @param password the password to use
     * @return certificate data contained in the keystore
     * @throws java.io.IOException if accessing the keystore fails
     * @throws java.security.GeneralSecurityException if reading/writing/generating certificate information fails
     * @throws IllegalArgumentException input stream of keystore is null
     */
    public static CertificateData load(InputStream keystoreInputStream, String password)
            throws IOException,
            GeneralSecurityException {
        Ensure.requireNonNull(keystoreInputStream, "keystoreInputStream must be non-null");
        KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);
        keystore.load(keystoreInputStream, passwordToChar(password));
        if (!keystore.aliases().hasMoreElements()) {
            throw new KeyStoreException("keystore must contain exactly one alias (found: 0)");
        }
        String alias = keystore.aliases().nextElement();
        Key privateKey = keystore.getKey(alias, passwordToChar(password));
        if (Objects.isNull(privateKey) || !PrivateKey.class.isAssignableFrom(privateKey.getClass())) {
            throw new KeyStoreException("keystore must contain private key");
        }
        Certificate certificate = keystore.getCertificate(alias);
        if (Objects.isNull(certificate) || !X509Certificate.class.isAssignableFrom(certificate.getClass())) {
            throw new KeyStoreException("keystore must contain X509 certificate");
        }
        PublicKey publicKey = certificate.getPublicKey();
        if (Objects.isNull(publicKey)) {
            throw new KeyStoreException("X509 certificate must contain public key");
        }
        return CertificateData.builder()
                .certificate((X509Certificate) certificate)
                .certificateChain(Arrays.stream(keystore.getCertificateChain(alias))
                        .map(X509Certificate.class::cast)
                        .toArray(X509Certificate[]::new))
                .keyPair(publicKey, (PrivateKey) privateKey)
                .build();
    }


    /**
     * Gets relevant data for OPC UA from a key store or generates new one if it does not exist.
     *
     * @param keystoreInputStream input stream containing the keystore. If it is null, new certificate data willl be
     *            generated.
     * @param password the password to use
     * @param certificateInformation certificate information used when creating a new key store
     * @return relevant information from the key store
     * @throws java.io.IOException if file access fails
     * @throws java.security.GeneralSecurityException if reading/writing/generating certificate information fails
     * @throws IllegalArgumentException if file or alias is null or file does not exist and certificateInformation is
     *             null
     */
    public static CertificateData loadOrDefault(InputStream keystoreInputStream, String password, CertificateInformation certificateInformation)
            throws IOException,
            GeneralSecurityException {
        if (Objects.isNull(keystoreInputStream)) {
            return generateSelfSigned(certificateInformation);
        }
        return load(keystoreInputStream, password);
    }
}
