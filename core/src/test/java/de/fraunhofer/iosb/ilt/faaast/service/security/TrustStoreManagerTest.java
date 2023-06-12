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
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;


public class TrustStoreManagerTest {
    private static final String scriptPath = "C:\\Users\\cha35985\\IdeaProjects\\FAAAST\\scripts\\generate_certificate.bat" + File.separator;
    private static final String outputDirectory = "C:\\Users\\cha35985\\IdeaProjects\\FAAAST\\certs" + File.separator;

    static TrustStoreManager trustStoreManager = new TrustStoreManager();

    @BeforeClass
    public static void setup() throws Exception {
        ScriptRunnerHelper bashScriptRunner = new ScriptRunnerHelper();
        bashScriptRunner.runBashScript(scriptPath, outputDirectory);
        trustStoreManager = new TrustStoreManager();
    }


    @Test
    public void testCreateTruststoreWithCertificate() throws Exception {

        trustStoreManager.createTruststoreWithCertificate();
        File truststoreFile = new File(TrustStoreManager.TRUSTSTORE_PATH);
        Assertions.assertTrue(truststoreFile.exists());

        // Load the truststore
        KeyStore truststore = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream fis = new FileInputStream(truststoreFile);
        truststore.load(fis, TrustStoreManager.TRUSTSTORE_PASSWORD);
        fis.close();

        // Verify the imported certificate(s)
        String alias = "selfSignedCert";
        Certificate certificate = truststore.getCertificate(alias);
        Assertions.assertNotNull(certificate, "Certificate was not imported into the truststore");

        // Perform additional checks on the self-signed certificate
        Assertions.assertTrue(certificate instanceof X509Certificate, "Certificate is not an instance of X509Certificate");
        X509Certificate x509Certificate = (X509Certificate) certificate;

        // Check certificate validity period
        x509Certificate.checkValidity();

        // Check if the certificate is self-signed
        try {
            x509Certificate.verify(x509Certificate.getPublicKey());
        }
        catch (Exception e) {
            Assertions.fail("Certificate is not self-signed: " + e.getMessage());
        }
    }
}
