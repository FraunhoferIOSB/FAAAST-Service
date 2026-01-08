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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.testutil;

import com.digitalpetri.modbus.client.ModbusTcpClient;
import com.digitalpetri.modbus.server.ModbusTcpServer;
import com.digitalpetri.modbus.server.ProcessImage;
import com.digitalpetri.modbus.server.ReadWriteModbusServices;
import com.digitalpetri.modbus.tcp.client.NettyClientTransportConfig;
import com.digitalpetri.modbus.tcp.client.NettyTcpClientTransport;
import com.digitalpetri.modbus.tcp.security.SecurityUtil;
import com.digitalpetri.modbus.tcp.server.NettyServerTransportConfig;
import com.digitalpetri.modbus.tcp.server.NettyTcpServerTransport;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.util.KeyStoreHelper;
import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Optional;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;


public class ModbusTestHelper {
    private ModbusTestHelper() {}


    public static ModbusTcpServer getServer(int port, boolean tls) throws GeneralSecurityException, IOException {
        var processImage = new ProcessImage();
        var modbusServices = new ReadWriteModbusServices() {
            @Override
            protected Optional<ProcessImage> getProcessImage(int unitId) {
                return Optional.of(processImage);
            }
        };
        var config = new NettyServerTransportConfig.Builder()
                .setBindAddress("0.0.0.0")
                .setPort(port);
        if (tls) {
            config.setTlsEnabled(true)
                    .setKeyManagerFactory(initializeKeyManagerFactory(CertificateConfig.builder()
                            .keyStorePath("src/test/resources/server.p12")
                            .keyStorePassword("123456")
                            .build()))
                    .setTrustManagerFactory(initializeTrustManagerFactory(CertificateConfig.builder()
                            .keyStorePath("src/test/resources/server-truststore.p12")
                            .keyStorePassword("123456")
                            .build()));
        }
        return ModbusTcpServer.create(
                new NettyTcpServerTransport(config.build()),
                modbusServices);
    }


    private static KeyManagerFactory initializeKeyManagerFactory(CertificateConfig keyCertificateConfig) throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStoreHelper.load(Path.of(keyCertificateConfig.getKeyStorePath()).toFile(), keyCertificateConfig.getKeyStoreType(),
                keyCertificateConfig.getKeyStorePassword());
        return SecurityUtil.createKeyManagerFactory(keyStore, keyCertificateConfig.getKeyStorePassword().toCharArray());

    }


    private static TrustManagerFactory initializeTrustManagerFactory(CertificateConfig trustCertificateConfig) throws GeneralSecurityException, IOException {
        KeyStore trustStore = KeyStoreHelper.load(Path.of(trustCertificateConfig.getKeyStorePath()).toFile(), trustCertificateConfig.getKeyStoreType(),
                trustCertificateConfig.getKeyStorePassword());
        return SecurityUtil.createTrustManagerFactory(trustStore);

    }


    public static ModbusTcpClient getClient(int port) {
        return ModbusTcpClient.create(new NettyTcpClientTransport(new NettyClientTransportConfig.Builder()
                .setHostname(InetAddress.getLoopbackAddress().getHostName())
                .setPort(port)
                .build()));
    }
}
