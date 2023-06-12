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

import de.fraunhofer.iosb.ilt.faaast.service.util.ScriptRunnerHelper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;


public class SelfSignedCertificateHandlerTest {
    public static final String RSA = "RSA";
    private static SelfSignedCertificateHandler certificateHandler;
    private static final String scriptPath = "C:\\Users\\cha35985\\IdeaProjects\\FAAAST\\scripts\\generate_certificate.bat" + File.separator;
    private static final String outputDirectory = "C:\\Users\\cha35985\\IdeaProjects\\FAAAST\\certs" + File.separator;

    private static final String PROPERTIES_FILE_PATH = "C:\\Users\\cha35985\\IdeaProjects\\FAAAST\\core\\src\\properties\\config_certificate.properties";
    private static Properties properties;

    @BeforeClass
    public static void setup() throws Exception {
        ScriptRunnerHelper bashScriptRunner = new ScriptRunnerHelper();
        bashScriptRunner.runBashScript(scriptPath, outputDirectory);
        certificateHandler = new SelfSignedCertificateHandler(certificateHandler);
        loadProperties();
    }


    @Test
    public void testGetAcceptedIssuers() {
        X509Certificate[] issuers = certificateHandler.getAcceptedIssuers();

        Assertions.assertNotNull(issuers);
        Assertions.assertEquals(0, issuers.length);
    }


    @Test
    public void testCreateTrustStoreWithCertificate() throws Exception {

        // setup
        certificateHandler.createTrustStoreWithCertificate();

        File truststoreFile = new File(getTruststorePath());
        Assertions.assertTrue(truststoreFile.exists());

        // Load the truststore
        KeyStore truststore = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream fis = new FileInputStream(truststoreFile);
        truststore.load(fis, getTruststorePassword());
        fis.close();

        // Verify the imported certificates
        for (String certFile: getCertsDirectory()) {
            try (FileInputStream fileInputStream = new FileInputStream(certFile);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
                Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(bufferedInputStream);
                if (certificate instanceof X509Certificate) {
                    try {
                        X509Certificate x509Certificate = (X509Certificate) certificate;
                        x509Certificate.checkValidity();
                        x509Certificate.verify(x509Certificate.getPublicKey()); // Verify the signature

                        // Check if the certificate is in the truststore
                        String alias = x509Certificate.getSubjectDN().getName().toString();
                        Assertions.assertNotNull(truststore.getCertificate(alias), "Certificate is not added to the truststore");
                    }
                    catch (Exception e) {
                        Assertions.fail("Certificate verification failed: " + e.getMessage());
                    }
                }
            }
        }
        // Clean up
        truststoreFile.delete();
    }

    private static void loadProperties() {
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
}
