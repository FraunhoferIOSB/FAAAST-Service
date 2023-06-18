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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.util;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.LoggerFactory;


/**
 * Generate Self Signed Certificates.
 */
public class SelfSignedCertificateGenerator {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AssetConnectionManager.class);
    private String keystorePath;
    private String directoryPath;
    private String alias;
    private String keyPassword;
    private String commonName;
    private String organizationUnit;
    private String organization;
    private String city;
    private String state;
    private String country;
    private int validityDays;

    /**
     * constructor.
     *
     * @param keystorePath keystorePath
     * @param directoryPath directoryPath
     * @param alias alias
     * @param keyPassword keyPassword
     * @param commonName commonName
     * @param organizationUnit organizationUnit
     * @param organization organization
     * @param city city
     * @param state state
     * @param country country
     * @param validityDays validityDays
     */
    public SelfSignedCertificateGenerator(String keystorePath, String directoryPath, String alias, String keyPassword, String commonName, String organizationUnit,
            String organization, String city, String state, String country, int validityDays) throws IOException {
        this.keystorePath = keystorePath;
        this.directoryPath = directoryPath;
        this.alias = alias;
        this.keyPassword = keyPassword;
        this.commonName = commonName;
        this.organizationUnit = organizationUnit;
        this.organization = organization;
        this.city = city;
        this.state = state;
        this.country = country;
        this.validityDays = validityDays;
        generateSSCertificate();

    }


    private void generateSSCertificate() {
        try {
            Path directory = Paths.get(directoryPath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "keytool",
                    "-genkeypair",
                    "-alias", alias,
                    "-keyalg", "RSA",
                    "-keysize", "2048",
                    "-storetype", "PKCS12",
                    "-keystore", keystorePath,
                    "-storepass", keyPassword,
                    "-keypass", keyPassword,
                    "-dname", "CN=" + commonName + ", OU=" + organizationUnit +
                            ", O=" + organization + ", L=" + city + ", ST=" + state +
                            ", C=" + country,
                    "-validity", String.valueOf(validityDays),
                    "-ext", "SAN=dns:" + commonName);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                LOGGER.info("Self-signed certificate generated successfully.");
                LOGGER.debug("Keystore: " + keystorePath);
                LOGGER.debug("Alias: " + alias);
                LOGGER.debug("Key password: " + keyPassword);
            }
            else {
                LOGGER.warn("Failed to generate self-signed certificate. Exit code: " + exitCode);
            }
        }
        catch (IOException | InterruptedException e) {
            LOGGER.error("Error occurred while generating self-signed certificate: " + e.getMessage());
        }
    }
}
