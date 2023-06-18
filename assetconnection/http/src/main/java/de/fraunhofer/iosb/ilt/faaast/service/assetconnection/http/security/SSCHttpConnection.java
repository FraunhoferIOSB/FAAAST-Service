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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.LoggerFactory;


/**
 * Handle self-signed certificates. Identify self-signed certificates by checking the given certificate against
 * a list of CA's and allow self-signed certificates to be authorized.
 */
public class SSCHttpConnection {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AssetConnectionManager.class);
    public static final String JKS = "JKS";
    private String trustStorePath = System.getProperty("java.io.tmpdir") + File.separator + "faaast.keystore";
    private static char[] trustStorePassword = "12345".toCharArray();

    /**
     * create custom SSL context.
     *
     * @return ssl context.
     */

    public SSLContext createCustomSSLContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] {
                    new SelfSignedTrustManager()
            }, new SecureRandom());
            return sslContext;
        }
        catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }


    private KeyStore getTrustedCertificates(X509Certificate[] chain) {
        try {
            KeyStore trustStore = KeyStore.getInstance(JKS);
            trustStore.load(null, null);

            for (X509Certificate certfile: chain) {
                trustStore.setCertificateEntry("certificate", certfile);
            }
            trustStore.store(new FileOutputStream(trustStorePath), trustStorePassword);

            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", String.valueOf(trustStorePassword));
            System.setProperty("javax.net.ssl.trustStoreType", JKS);
            return trustStore;
        }
        catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }


    public String getTrustStorePath() {
        return this.trustStorePath;
    }


    /**
     * Set the truststore password.
     *
     * @param trustStorePassword the truststore directory
     */
    public void setTrustStorePassword(char[] trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    private class SelfSignedTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String s) throws CertificateException {
            // Accept all client certificates
        }


        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            getTrustedCertificates(chain);
        }


        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
