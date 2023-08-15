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
import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.HostnameUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    public static final String DEFAULT_ALIAS = "faaast";

    /**
     * Hide the implicit public constructor.
     */
    private KeyStoreHelper() {

    }


    /**
     * Create a key store of given type with the given certificate data.
     *
     * @param keyStoreType the type of key store to create
     * @param certificateData the certificate data to use
     * @param keyAlias the alias of the key; if null, default alias will be used
     * @param keyPassword the password for the key entry
     * @param keyStorePassword the password for the keyStore
     * @return a key store containing the given certificate data
     * @throws IOException if creation of the key store fails
     * @throws GeneralSecurityException if creation of the key store fails
     */
    public static KeyStore create(
                                  CertificateData certificateData,
                                  String keyStoreType,
                                  String keyAlias,
                                  String keyPassword,
                                  String keyStorePassword)
            throws IOException, GeneralSecurityException {
        KeyStore result = KeyStore.getInstance(keyStoreType);
        result.load(null, passwordToChar(keyStorePassword));
        String alias = Objects.nonNull(keyAlias) ? keyAlias : DEFAULT_ALIAS;
        result.setCertificateEntry(alias, certificateData.getCertificate());
        result.setKeyEntry(alias, certificateData.getKeyPair().getPrivate(), passwordToChar(keyPassword), certificateData.getCertificateChain());
        return result;
    }


    /**
     * Save the given file to the key store.
     *
     * @param keyStoreType the type of key store
     * @param file the file to write to
     * @param certificateData the certificate data
     * @param keyAlias the alias of the key; if null, default alias will be used
     * @param keyPassword the password for the key entry
     * @param keyStorePassword the password for the keyStore
     * @throws IOException if writing to the file fails
     * @throws GeneralSecurityException if generating the certificate fails
     */
    public static void save(
                            CertificateData certificateData,
                            File file,
                            String keyStoreType,
                            String keyAlias,
                            String keyPassword,
                            String keyStorePassword)
            throws IOException, GeneralSecurityException {
        save(KeyStoreHelper.create(certificateData, keyStoreType, keyAlias, keyPassword, keyStorePassword), file, keyStorePassword);
    }


    /**
     * Loads certificate data from a keystore.
     *
     * @param keyStoreType the type of the keyStore to load, e.g. PKCS12
     * @param file keystore file
     * @param keyStorePassword keystore password
     * @return loaded keyStore
     * @throws IOException if accessing the keystore fails
     * @throws GeneralSecurityException if reading/writing/generating certificate information fails
     */
    public static KeyStore load(File file, String keyStoreType, String keyStorePassword) throws IOException, GeneralSecurityException {
        try (InputStream inputStream = new FileInputStream(file)) {
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(inputStream, passwordToChar(keyStorePassword));
            return keyStore;
        }
    }


    /**
     * Save the given keyStore to file.
     *
     * @param file the file to write to
     * @param keyStore the key store to save
     * @param keyStorePassword the keyStore password
     * @throws IOException if writing to the file fails
     * @throws GeneralSecurityException if generating the certificate fails
     */
    public static void save(KeyStore keyStore, File file, String keyStorePassword) throws IOException, GeneralSecurityException {
        Ensure.requireNonNull(keyStore, "keyStore must be non-null");
        try (OutputStream out = new FileOutputStream(file)) {
            keyStore.store(out, passwordToChar(keyStorePassword));
        }
    }


    /**
     * Generates a self-signed certificate.
     *
     * @param certificateInformation the certificate informatino to use
     * @return a self-signed certificate
     * @throws KeyStoreException if generating certificate failed
     * @throws NoSuchAlgorithmException if generating private/public key pair fails due to missing algorithm
     */
    public static CertificateData generateSelfSigned(CertificateInformation certificateInformation) throws KeyStoreException, NoSuchAlgorithmException {
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
            builder.addDnsName(HostnameUtil.LOCALHOST);
            builder.addIpAddress(HostnameUtil.LOCALHOST_IP);
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
     * @param keyStoreType the keyStore type
     * @param keyPassword the password for the key entry
     * @param keyAlias the alias of the key; if null, default alias will be used
     * @param keyStorePassword the password for the keyStore
     * @param defaultValue certificate information used when creating a new key store
     * @return relevant information from the key store
     * @throws IOException if file access fails
     * @throws GeneralSecurityException if reading/writing/generating certificate information fails
     * @throws IllegalArgumentException if file or alias is null or file does not exist and certificateInformation is
     *             null
     */
    public static CertificateData loadOrCreateCertificateData(
                                                              File file,
                                                              String keyStoreType,
                                                              String keyAlias,
                                                              String keyPassword,
                                                              String keyStorePassword,
                                                              CertificateInformation defaultValue)
            throws IOException, GeneralSecurityException {
        Ensure.requireNonNull(file, "file must be non-null");
        if (file.exists()) {
            try (InputStream inputStream = new FileInputStream(file)) {
                return loadOrDefaultCertificateData(inputStream, keyStoreType, keyAlias, keyPassword, keyStorePassword, defaultValue);
            }
        }
        CertificateData result = loadOrDefaultCertificateData(null, keyStoreType, keyAlias, keyPassword, keyStorePassword, defaultValue);
        save(result, file, keyStoreType, keyAlias, keyPassword, keyStorePassword);
        return result;
    }


    /**
     * Loads relevant data for OPC UA from a key store or generates new one if it does not exist.
     *
     * @param certificate the certificate info
     * @param defaultValue certificate information used when creating a new key store
     * @return relevant information from the key store
     * @throws IOException if file access fails
     * @throws GeneralSecurityException if reading/writing/generating certificate information fails
     * @throws IllegalArgumentException if file or alias is null or file does not exist and certificateInformation is
     *             null
     */
    public static CertificateData loadOrCreateCertificateData(CertificateConfig certificate, CertificateInformation defaultValue)
            throws IOException, GeneralSecurityException {
        Ensure.requireNonNull(certificate, "certificate must be non-null");
        File file = new File(certificate.getKeyStorePath());
        if (file.exists()) {
            try (InputStream inputStream = new FileInputStream(file)) {
                return loadOrDefaultCertificateData(
                        inputStream,
                        certificate.getKeyStoreType(),
                        certificate.getKeyAlias(),
                        certificate.getKeyPassword(),
                        certificate.getKeyStorePassword(),
                        defaultValue);
            }
        }
        CertificateData result = loadOrDefaultCertificateData(
                null,
                certificate.getKeyStoreType(),
                certificate.getKeyAlias(),
                certificate.getKeyPassword(),
                certificate.getKeyStorePassword(),
                defaultValue);
        save(
                result,
                file,
                certificate.getKeyStoreType(),
                certificate.getKeyAlias(),
                certificate.getKeyPassword(),
                certificate.getKeyStorePassword());
        return result;
    }


    /**
     * Loads relevant data for OPC UA from a key store or generates new one if it does not exist.
     *
     * @param file the keystore file
     * @param keyStoreType the keyStore type
     * @param keyAlias the alias of the key; if null, default alias will be used
     * @param keyPassword the key password
     * @param keyStorePassword the password to use for the key entry and keyStore
     * @return relevant information from the key store
     * @throws IOException if file access fails
     * @throws GeneralSecurityException if reading/writing/generating certificate information fails
     * @throws IllegalArgumentException if file or alias is null or file does not exist and certificateInformation is
     *             null
     */
    public static CertificateData loadCertificateData(
                                                      File file,
                                                      String keyStoreType,
                                                      String keyAlias,
                                                      String keyPassword,
                                                      String keyStorePassword)
            throws IOException, GeneralSecurityException {
        Ensure.requireNonNull(file, "file must be non-null");
        try (InputStream inputStream = new FileInputStream(file)) {
            return KeyStoreHelper.loadCertificateData(inputStream, keyStoreType, keyAlias, keyPassword, keyStorePassword);
        }
    }


    /**
     * Loads relevant data for OPC UA from a key store or generates new one if it does not exist.
     *
     * @param certificate the certificate information
     * @return relevant information from the key store
     * @throws IOException if file access fails
     * @throws GeneralSecurityException if reading/writing/generating certificate information fails
     * @throws IllegalArgumentException if file or alias is null or file does not exist and certificateInformation is
     *             null
     */
    public static CertificateData loadCertificateData(CertificateConfig certificate)
            throws IOException, GeneralSecurityException {
        Ensure.requireNonNull(certificate.getKeyStorePath(), "file must be non-null");
        try (InputStream inputStream = new FileInputStream(certificate.getKeyStorePath())) {
            return KeyStoreHelper.loadCertificateData(
                    inputStream,
                    certificate.getKeyStoreType(),
                    certificate.getKeyAlias(),
                    certificate.getKeyPassword(),
                    certificate.getKeyStorePassword());
        }
    }


    /**
     * Loads certificate data from a keystore.
     *
     * @param keyStoreType the keyStoreType
     * @param keystoreInputStream input stream containing the keystore. If it is null, new certificate data willl be
     *            generated.
     * @param keyAlias the alias of the key; if null, first alias present in keyStore will be used
     * @param keyPassword the password for the key entry
     * @param keyStorePassword the password for the keyStore
     * @return certificate data contained in the keystore
     * @throws java.io.IOException if accessing the keystore fails
     * @throws java.security.GeneralSecurityException if reading/writing/generating certificate information fails
     * @throws IllegalArgumentException input stream of keystore is null
     */
    public static CertificateData loadCertificateData(
                                                      InputStream keystoreInputStream,
                                                      String keyStoreType,
                                                      String keyAlias,
                                                      String keyPassword,
                                                      String keyStorePassword)
            throws IOException, GeneralSecurityException {
        Ensure.requireNonNull(keystoreInputStream, "keystoreInputStream must be non-null");
        KeyStore keystore = KeyStore.getInstance(keyStoreType);
        keystore.load(keystoreInputStream, passwordToChar(keyStorePassword));
        String alias = keyAlias;
        if (Objects.isNull(alias)) {
            if (!keystore.aliases().hasMoreElements()) {
                throw new KeyStoreException("keystore must contain at least one alias (found: 0)");
            }
            alias = keystore.aliases().nextElement();
        }
        Key privateKey = keystore.getKey(alias, passwordToChar(keyPassword));
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
     * @param keyStoreType the keyStore type
     * @param keyPassword the password for the key entry
     * @param keyAlias the alias of the key; if null, first alias present in keyStore will be used
     * @param keyStorePassword the password for the keyStore
     * @param defaultValue certificate information used when creating a new key store
     * @return relevant information from the key store
     * @throws java.io.IOException if file access fails
     * @throws java.security.GeneralSecurityException if reading/writing/generating certificate information fails
     * @throws IllegalArgumentException if file or alias is null or file does not exist and certificateInformation is
     *             null
     */
    public static CertificateData loadOrDefaultCertificateData(
                                                               InputStream keystoreInputStream,
                                                               String keyStoreType,
                                                               String keyAlias,
                                                               String keyPassword,
                                                               String keyStorePassword,
                                                               CertificateInformation defaultValue)
            throws IOException,
            GeneralSecurityException {
        if (Objects.isNull(keystoreInputStream)) {
            return generateSelfSigned(defaultValue);
        }
        return KeyStoreHelper.loadCertificateData(keystoreInputStream, keyStoreType, keyAlias, keyPassword, keyStorePassword);
    }


    private static char[] passwordToChar(String password) {
        return Objects.nonNull(password) ? password.toCharArray() : new char[0];
    }
}
