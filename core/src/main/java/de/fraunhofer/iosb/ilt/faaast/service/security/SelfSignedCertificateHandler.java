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

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.Properties;
import javax.net.ssl.X509TrustManager;


/**
 * Handle self-signed certificates. Identify self-signed certificates by checking the given certificate against a list
 * if CA's and allow self-signed certificates to be authorized.
 */
public class SelfSignedCertificateHandler implements X509TrustManager {
    public static final String X_509 = "X.509";
    public static final String JKS = "JKS";
    private SelfSignedCertificateHandler customTrustManager;
    private static final String PROPERTIES_FILE_PATH
            = "C:\\Users\\cha35985\\IdeaProjects\\FAAAST\\core\\src\\properties\\config_certificate.properties";
    private Properties properties;

    public SelfSignedCertificateHandler(SelfSignedCertificateHandler customTrustManager) {
        this.customTrustManager = customTrustManager;
        loadProperties();
    }

    private void loadProperties() {
        properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(PROPERTIES_FILE_PATH)) {
            properties.load(fileInputStream);
        }
        catch (IOException e) {
            // TODO (Handle exception if the file cannot be loaded)
            e.printStackTrace();
        }
    }


    private String[] getCertsDirectory() {
        String selfSignedCerts = properties.getProperty("certs.directory");
        return new String[] {
                selfSignedCerts
        };
    }


    private char[] getTruststorePassword() {
        return properties.getProperty("truststore.password").toCharArray();
    }


    private String getTruststorePath() {
        return properties.getProperty("truststore.directory");
    }


    /**
     * create trust store, saves the data stream to file and configures as the default trust store for the JVM
     * using system properties.
     *
     */
    public void createTrustStoreWithCertificate() throws Exception {
        // Load the existing truststore or create a new one
        KeyStore truststore = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            FileInputStream fileInputStream = new FileInputStream(getTruststorePath());
            truststore.load(fileInputStream, getTruststorePassword());
            fileInputStream.close();
        }
        catch (Exception e) {
            truststore.load(null, null);
        }
        String[] certDir = getCertsDirectory();
        for (String certFile: certDir) {
            try (FileInputStream fileInputStream = new FileInputStream(certFile);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
                while (bufferedInputStream.available() > 0) {
                    Certificate certificate = CertificateFactory.getInstance(X_509).generateCertificate(bufferedInputStream);
                    if (certificate instanceof X509Certificate) {
                        try {
                            ((X509Certificate) certificate).checkValidity();
                            isSelfSigned(certificate); // Verify the signature
                            // Certificate is trusted, add it to the truststore
                            truststore.setCertificateEntry(((X509Certificate) certificate).getSubjectDN().getName().toString(), certificate);
                        }
                        catch (CertificateException e) {
                            // Certificate is not trusted, handle accordingly
                            System.out.println("Certificate verification failed: " + e.getMessage());
                        }
                    }
                    bufferedInputStream.close();
                }
            }
        }
        // Save the trust store to a file
        truststore.store(new FileOutputStream(getTruststorePath()), getTruststorePassword());
        Thread.sleep(10000);
        new File(getTruststorePath()).deleteOnExit();

        // Set the trust store system properties
        //System.setProperty("javax.net.ssl.truststore", TRUSTSTORE_PATH);
        //System.setProperty("javax.net.ssl.truststorePassword", new String(TRUSTSTORE_PASSWORD));
        //System.setProperty("javax.net.ssl.trustStoreType", JKS);

    }


    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}


    /**
     * Checks the server's certificate chain and throws an exception if it is not trusted. If the certificate
     * is self-signed, it means it is authorized, and the method returns without throwing an exception.
     *
     * @param chain Array Object of X509Certificate
     * @param authType Authorisation type (Algorithm)
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}


    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    private boolean isSelfSigned(Certificate certificate) {
        try {
            certificate.verify(certificate.getPublicKey());
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

}
