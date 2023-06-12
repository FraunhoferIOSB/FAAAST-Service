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
package de.fraunhofer.iosb.ilt.faaast.service.security;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;


/**
 * test class.
 */

public class TrustStoreManager {
    public static final String TRUSTSTORE_PATH = System.getProperty("java.io.tmpdir") + File.separator + "faaast.keystore";
    public static final char[] TRUSTSTORE_PASSWORD = "changeit".toCharArray();
    private static final String[] SELF_SIGNED_CRTS = {
            "C:\\Users\\cha35985\\IdeaProjects\\FAAAST\\certs" + File.separator
    };

    /**
     * tests.
     */
    public TrustStoreManager() {}


    /**
     * test.
     */
    public void createTruststoreWithCertificate() throws Exception {
        // Load the existing truststore or create a new one
        KeyStore truststore = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            FileInputStream fis = new FileInputStream(TRUSTSTORE_PATH);
            truststore.load(fis, TRUSTSTORE_PASSWORD);
            fis.close();
        }
        catch (Exception e) {
            truststore.load(null, null);
        }

        // Create a custom TrustManager that accepts self-signed certificates
        TrustManager[] trustManagers = {
                new SelfSignedTrustManager()
        };

        // Create an SSLContext with the custom TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, null);

        // Iterate over the self-signed certificates and import them into the truststore
        for (String certFile: SELF_SIGNED_CRTS) {
            try (FileInputStream fileInputStream = new FileInputStream(certFile);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
                while (bufferedInputStream.available() > 0) {
                    Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(bufferedInputStream);
                    truststore.setCertificateEntry(certFile, certificate);
                }
            }
        }

        // Save the truststore
        truststore.store(new FileOutputStream(TRUSTSTORE_PATH), TRUSTSTORE_PASSWORD);
    }

    private static class SelfSignedTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}


        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            // Custom server certificate validation, including self-signed certificates
            for (X509Certificate cert: x509Certificates) {
                try {
                    cert.checkValidity();
                    // Additional checks if needed
                }
                catch (Exception e) {
                    throw new CertificateException("Invalid server certificate: " + e.getMessage());
                }
            }
        }


        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
