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
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Handle self-signed certificates. Identify self-signed certificates by checking the given certificate against a list
 * of CA's and allow self-signed certificates to be authorized.
 */

public class SelfSignedCertificateHandler {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AssetConnectionManager.class);
    public static final String JKS = "JKS";
    private File trustStoreFile;

    /**
     * create custom SSL context.
     *
     * @return ssl context.
     */

    public SSLContext createCustomSSLContext(String trustStorePath, char[] password) throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustManagers = {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }


                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        List<X509Certificate> trustedCerts = getTrustedCertificates(trustStorePath, password);
                        var isNotTrusted = Arrays.stream(chain).noneMatch(c -> isSelfSigned(c) && trustedCerts.contains(c));
                        // Check if the certificate is in the trusted certificates list and if self-signed
                        if (isNotTrusted) {
                            throw new CertificateException("Certificate is not trusted.");
                        }
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, new SecureRandom());

        return sslContext;
    }


    private static List<X509Certificate> getTrustedCertificates(String trustStorePath, char[] password) {
        try {
            // Load the keystore file containing the trusted certificates
            KeyStore trustStore = KeyStore.getInstance(JKS);
            FileInputStream fileInputStream = new FileInputStream(trustStorePath);
            trustStore.load(fileInputStream, password);
            fileInputStream.close();

            // Iterate over the trusted certificates and add them to the list
            var trustedCerts = new ArrayList<X509Certificate>();
            var aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate cert = (X509Certificate) trustStore.getCertificate(alias);
                trustedCerts.add(cert);
            }

            return trustedCerts;
        } catch (Exception e) {
            //TODO- Handle the exception appropriately
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    private static boolean isSelfSigned(X509Certificate certificate) {
        try {
            certificate.verify(certificate.getPublicKey());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * delete trust store file.
     */
    private void deleteTruststore() throws IOException {
        if (trustStoreFile != null && trustStoreFile.exists()) {
            Files.delete(trustStoreFile.toPath());
        }
    }
}
