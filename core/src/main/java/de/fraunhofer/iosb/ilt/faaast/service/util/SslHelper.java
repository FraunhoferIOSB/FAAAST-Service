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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import de.fraunhofer.iosb.ilt.faaast.service.certificate.util.KeyStoreHelper;
import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


/**
 * Helper class for working with HTTPS connections.
 */
public class SslHelper {

    private static final String PROTOCOL_TLS = "TLSv1.2";
    private static final String KEY_DISABLE_HOSTNAME_VERIFICATION = "jdk.internal.httpclient.disableHostnameVerification";

    private SslHelper() {}


    /**
     * Create a new {@link HttpClient} that accepts all certificates and with disabled hostname verification.
     *
     * @return the new {@link HttpClient}
     *
     * @throws KeyManagementException if creating a {@link TrustManager} fails
     * @throws NoSuchAlgorithmException if accessing the {@link SSLContext} fails
     */
    public static HttpClient newClientAcceptingAllCertificates() throws KeyManagementException, NoSuchAlgorithmException {
        return disableHostnameVerification(HttpClient.newBuilder().sslContext(newContextAcceptingAllCertificates()));
    }


    /**
     * Creates a new {@link HttpClient} with hostname verification disabled.
     *
     * @param builder the {@link HttpClient.Builder} to use
     * @return the new {@link HttpClient}
     */
    public static HttpClient disableHostnameVerification(HttpClient.Builder builder) {
        // setting custom HostnameVerifier not possible (see https://bugs.openjdk.java.net/browse/JDK-8213309), 
        // therefore, this is the least invasive way to to this
        Properties properties = System.getProperties();
        Boolean temp = Boolean.parseBoolean(properties.getProperty(KEY_DISABLE_HOSTNAME_VERIFICATION));
        properties.setProperty(KEY_DISABLE_HOSTNAME_VERIFICATION, Boolean.TRUE.toString());
        HttpClient result = builder.build();
        properties.setProperty(KEY_DISABLE_HOSTNAME_VERIFICATION, temp.toString());
        return result;
    }


    /**
     * Create a new {@link SSLContext} that accepts all certificates and with disabled hostname verification.
     *
     * @return the new {@link HttpClient}
     *
     * @throws KeyManagementException if creating a {@link TrustManager} fails
     * @throws NoSuchAlgorithmException if accessing the {@link SSLContext} fails
     */
    public static SSLContext newContextAcceptingAllCertificates() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getInstance(PROTOCOL_TLS);
        sslContext.init(null, new TrustManager[] {
                new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        // intentionally left empty
                    }


                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        // intentionally left empty
                    }


                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                }
        }, new SecureRandom());
        return sslContext;
    }


    /**
     * Create a new {@link HttpClient} that accepts certificates stored in the given keyStore.All other certificates a
     * resolved regularly.
     *
     * @param keyStorePath the path to the keyStore
     * @param keyStoreType the type of the keyStore, e.g. PKCS12
     * @param keyStorePassword the password of the keystore
     * @return the new {@link HttpClient}
     *
     * @throws GeneralSecurityException if creating a {@link TrustManager} or loading the keyStore file fails
     * @throws IOException if loading the keyStore file fails
     */
    public static HttpClient newClientAcceptingCertificates(File keyStorePath, String keyStoreType, String keyStorePassword) throws GeneralSecurityException, IOException {
        return HttpClient.newBuilder()
                .sslContext(newContextAcceptingCertificates(keyStorePath, keyStoreType, keyStorePassword))
                .build();
    }


    /**
     * Create a new {@link javax.net.ssl.SSLContext} that accepts certificates stored in the given keyStore.All other
     * certificates a
     * resolved regularly.
     *
     * @param keyStorePath the path to the keyStore
     * @param keyStoreType the type of the keyStore, e.g. PKCS12
     * @param keyStorePassword the password of the keystore
     * @return the new {@link HttpClient}
     *
     * @throws GeneralSecurityException if creating a {@link TrustManager} or loading the keyStore file fails
     * @throws IOException if loading the keyStore file fails
     */
    public static SSLContext newContextAcceptingCertificates(File keyStorePath, String keyStoreType, String keyStorePassword) throws GeneralSecurityException, IOException {
        List<X509Certificate> trustedCertificates = loadCertificatesFromKeyStore(keyStorePath, keyStoreType, keyStorePassword);
        SSLContext sslContext = SSLContext.getInstance(PROTOCOL_TLS);
        TrustManagerFactory defaultFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        defaultFactory.init((KeyStore) null);
        X509TrustManager defaultTrustManager = (X509TrustManager) Stream.of(defaultFactory.getTrustManagers())
                .filter(x -> X509TrustManager.class.isAssignableFrom(x.getClass()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unable to find default trust manager"));
        sslContext.init(null, new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        defaultTrustManager.checkClientTrusted(chain, authType);
                    }


                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        if (chain.length == 1 && trustedCertificates.contains(chain[0])) {
                            return;
                        }
                        defaultTrustManager.checkServerTrusted(chain, authType);
                    }


                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return defaultTrustManager.getAcceptedIssuers();
                    }
                }
        }, new SecureRandom());
        return sslContext;
    }


    /**
     * Create a new {@link javax.net.ssl.SSLContext} that accepts certificates stored in the given keyStore.All other
     * certificates a
     * resolved regularly.
     *
     * @param config the certificate config
     * @return the new {@link java.net.http.HttpClient}
     *
     * @throws GeneralSecurityException if creating a {@link TrustManager} or loading the keyStore file fails
     * @throws IOException if loading the keyStore file fails
     */
    public static SSLContext newContextAcceptingCertificates(CertificateConfig config) throws GeneralSecurityException, IOException {
        if (Objects.isNull(config)
                || Objects.isNull(config.getKeyStorePath())
                || StringHelper.isEmpty(config.getKeyStorePath())) {
            return SSLContext.getDefault();
        }
        return newContextAcceptingCertificates(new File(config.getKeyStorePath()), config.getKeyStoreType(), config.getKeyStorePassword());
    }


    private static List<X509Certificate> loadCertificatesFromKeyStore(File keyStorePath, String keyStoreType, String keyStorePassword)
            throws IOException, GeneralSecurityException {
        List<X509Certificate> result = new ArrayList<>();
        if (Objects.nonNull(keyStorePath)) {
            var keyStore = KeyStoreHelper.load(
                    keyStorePath,
                    keyStoreType,
                    keyStorePassword);
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                var alias = aliases.nextElement();
                var certificate = keyStore.getCertificate(alias);
                if (Objects.nonNull(certificate) && X509Certificate.class.isAssignableFrom(certificate.getClass())) {
                    result.add((X509Certificate) certificate);
                }
            }
        }
        return result;
    }
}
