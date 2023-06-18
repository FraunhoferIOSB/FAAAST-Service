package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.util;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SelfSignedCertificateGenerator {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AssetConnectionManager.class);
    private String keystorePath; //"keystore.jks"
    private String directoryPath; //"assetconnection/http/src/test/resources/certificates"
    private String alias; //"selfsigned"
    private String keyPassword; //"password"
    private String commonName; //"localhost"
    private String organizationUnit; //"iosb"
    private String organization; //"fraunhofer"
    private String city; //"Karlsruhe"
    private String state; //"baden-württemberg"
    private String country; //"deutschland"
    private int validityDays; //365
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

