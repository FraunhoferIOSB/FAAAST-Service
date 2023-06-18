package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.util;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    public SelfSignedCertificateGenerator(String keystorePath,String directoryPath, String alias, String keyPassword, String commonName, String organizationUnit, String organization, String city, String state,String country, int validityDays) throws IOException {
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

    private void generateSSCertificate(){
        try {
            Path directory = Paths.get(directoryPath);
            if(!Files.exists(directory)) {
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
                    "-ext", "SAN=dns:" + commonName
            );
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                LOGGER.info("Self-signed certificate generated successfully.");
                LOGGER.debug("Keystore: " + keystorePath);
                LOGGER.debug("Alias: " + alias);
                LOGGER.debug("Key password: " + keyPassword);
            } else {
                LOGGER.warn("Failed to generate self-signed certificate. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error occurred while generating self-signed certificate: " + e.getMessage());
        }
    }
}

