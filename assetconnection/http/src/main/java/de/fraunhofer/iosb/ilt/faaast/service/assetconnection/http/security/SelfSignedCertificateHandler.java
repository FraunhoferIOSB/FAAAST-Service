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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.security;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.HttpAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.util.KeyStoreHelper;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


/**
 * Handle self-signed certificates. Identify self-signed certificates by checking the given certificate against a list
 * of CA's and allow self-signed certificates to be authorized.
 */
public class SelfSignedCertificateHandler {

    private final List<X509Certificate> trustedCertificates = new ArrayList<>();

    /**
     * Creates a custom SSL context that accepts certificates defined in config.
     *
     * @param config the configuration
     * @return the SSL contenxt
     * @throws IOException if reading keyStore fails
     * @throws GeneralSecurityException if reading keyStore fails
     */
    public SSLContext createCustomSSLContext(HttpAssetConnectionConfig config) throws IOException, GeneralSecurityException {
        loadTrustedCertificates(config);

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, new TrustManager[] {
                new CustomTrustManager()
        }, new SecureRandom());
        return sslContext;
    }


    private void loadTrustedCertificates(HttpAssetConnectionConfig config) throws IOException, GeneralSecurityException {
        if (Objects.nonNull(config.getKeyStorePath()) && !config.getKeyStorePath().isEmpty()) {
            var keyStore = KeyStoreHelper.loadKeyStore(new File(config.getKeyStorePath()), config.getKeyStorePassword());
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                var alias = aliases.nextElement();
                var certificate = keyStore.getCertificate(alias);
                if (Objects.nonNull(certificate) && X509Certificate.class.isAssignableFrom(certificate.getClass())) {
                    trustedCertificates.add((X509Certificate) certificate);
                }
            }
        }
    }

    private class CustomTrustManager implements X509TrustManager {

        private final X509TrustManager defaultTrustManager;

        public CustomTrustManager() throws KeyStoreException, NoSuchAlgorithmException {
            TrustManagerFactory defaultFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            defaultFactory.init((KeyStore) null);
            defaultTrustManager = (X509TrustManager) Stream.of(defaultFactory.getTrustManagers())
                    .filter(x -> X509TrustManager.class.isAssignableFrom(x.getClass()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("unable to find default trust manager"));
        }


        @Override
        public void checkClientTrusted(X509Certificate[] chain, String s) throws CertificateException {
            defaultTrustManager.checkClientTrusted(chain, s);
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
            // TODO check if it is enough to here return also all trusted certificates
            return defaultTrustManager.getAcceptedIssuers();
        }
    }
}
