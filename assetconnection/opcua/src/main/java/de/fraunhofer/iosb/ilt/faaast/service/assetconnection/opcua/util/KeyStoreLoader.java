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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.util;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.OpcUaAssetConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages a KeyStore.
 */
public class KeyStoreLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyStoreLoader.class);
    private static final Pattern IP_ADDR_PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private static final String CLIENT_ALIAS = "client-fa3st";
    private static final String PW = "";

    private X509Certificate[] clientCertificateChain;
    private X509Certificate clientCertificate;
    private KeyPair clientKeyPair;

    /**
     * Loads the KeyStore from the givrn Path.
     *
     * @param baseDir The desired Path.
     * @return The KeyStore.
     * @throws AssetConnectionException When an error occurs.
     */
    public KeyStoreLoader load(Path baseDir) throws AssetConnectionException {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            Path serverKeyStore = baseDir.resolve("fa3st-client.pfx");
            LOGGER.trace("Loading KeyStore at {}", serverKeyStore);

            if (!Files.exists(serverKeyStore)) {
                keyStore.load(null, PW.toCharArray());

                KeyPair keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);

                SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair)
                        .setCommonName("Fraunhofer IOSB AAS Service")
                        .setOrganization("Fraunhofer IOSB")
                        .setOrganizationalUnit("")
                        .setLocalityName("Karlsruhe")
                        .setCountryCode("DE")
                        .setApplicationUri(OpcUaAssetConnection.APPLICATION_URI)
                        .addDnsName("localhost")
                        .addIpAddress("127.0.0.1");

                // Get as many hostnames and IP addresses as we can listed in the certificate.
                for (String hostname: HostnameUtil.getHostnames("0.0.0.0")) {
                    if (IP_ADDR_PATTERN.matcher(hostname).matches()) {
                        builder.addIpAddress(hostname);
                    }
                    else {
                        builder.addDnsName(hostname);
                    }
                }

                X509Certificate certificate = builder.build();
                keyStore.setKeyEntry(CLIENT_ALIAS, keyPair.getPrivate(), PW.toCharArray(), new X509Certificate[] {
                        certificate
                });
                try (OutputStream out = Files.newOutputStream(serverKeyStore)) {
                    keyStore.store(out, PW.toCharArray());
                }
            }
            else {
                try (InputStream in = Files.newInputStream(serverKeyStore)) {
                    keyStore.load(in, PW.toCharArray());
                }
            }

            Key clientPrivateKey = keyStore.getKey(CLIENT_ALIAS, PW.toCharArray());
            if (clientPrivateKey instanceof PrivateKey) {
                clientCertificate = (X509Certificate) keyStore.getCertificate(CLIENT_ALIAS);

                clientCertificateChain = Arrays.stream(keyStore.getCertificateChain(CLIENT_ALIAS))
                        .map(X509Certificate.class::cast)
                        .toArray(X509Certificate[]::new);

                PublicKey serverPublicKey = clientCertificate.getPublicKey();
                clientKeyPair = new KeyPair(serverPublicKey, (PrivateKey) clientPrivateKey);
            }

            return this;
        }
        catch (Exception ex) {
            throw new AssetConnectionException(ex);
        }
    }


    public X509Certificate getClientCertificate() {
        return clientCertificate;
    }


    public X509Certificate[] getClientCertificateChain() {
        return clientCertificateChain;
    }


    public KeyPair getClientKeyPair() {
        return clientKeyPair;
    }

}
