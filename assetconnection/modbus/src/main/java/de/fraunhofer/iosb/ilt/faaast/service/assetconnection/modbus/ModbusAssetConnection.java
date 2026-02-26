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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus;

import com.digitalpetri.modbus.client.ModbusClientConfig;
import com.digitalpetri.modbus.client.ModbusTcpClient;
import com.digitalpetri.modbus.exceptions.ModbusExecutionException;
import com.digitalpetri.modbus.tcp.client.NettyClientTransportConfig;
import com.digitalpetri.modbus.tcp.client.NettyTcpClientTransport;
import com.digitalpetri.modbus.tcp.security.SecurityUtil;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AbstractAssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.ModbusOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.ModbusSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.ModbusValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.util.KeyStoreHelper;
import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.time.Duration;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Modbus asset connection.
 */
public class ModbusAssetConnection extends
        AbstractAssetConnection<ModbusAssetConnection, ModbusAssetConnectionConfig, ModbusValueProviderConfig, ModbusValueProvider, ModbusOperationProviderConfig, ModbusOperationProvider, ModbusSubscriptionProviderConfig, ModbusSubscriptionProvider> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusAssetConnection.class);

    ModbusTcpClient client;

    @Override
    public void init(CoreConfig coreConfig, ModbusAssetConnectionConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        super.init(coreConfig, config, serviceContext);
        LOGGER.debug("Creating modbus client for {}", getEndpointInformation());

        String hostname = config.getHostname();
        int port = config.getPort();

        KeyManagerFactory keyManagerFactory = null;
        TrustManagerFactory trustManagerFactory = null;

        if (config.isTlsEnabled()) {
            try {
                keyManagerFactory = initializeKeyManagerFactory(config.getKeyCertificateConfig());
                trustManagerFactory = initializeTrustManagerFactory(config.getTrustCertificateConfig());
            }
            catch (GeneralSecurityException | IOException e) {
                throw new ConfigurationInitializationException(e);
            }
        }

        NettyClientTransportConfig nettyConfig = new NettyClientTransportConfig.Builder()
                .setHostname(hostname)
                .setPort(port)
                .setConnectTimeout(Duration.ofMillis(config.getConnectTimeoutMillis()))
                .setConnectPersistent(config.isConnectPersistent())
                .setReconnectLazy(config.isReconnectLazy())
                .setTlsEnabled(config.isTlsEnabled())
                .setKeyManagerFactory(keyManagerFactory)
                .setTrustManagerFactory(trustManagerFactory)
                .build();

        ModbusClientConfig modbusClientConfig = new ModbusClientConfig.Builder()
                .setRequestTimeout(Duration.ofMillis(config.getRequestTimeoutMillis()))
                .build();

        client = new ModbusTcpClient(modbusClientConfig, new NettyTcpClientTransport(nettyConfig));
        LOGGER.debug("Created modbus client for {}", getEndpointInformation());
    }


    @Override
    public String getEndpointInformation() {
        return "Modbus TCP Server(hostname: " + config.getHostname() + ", port: " + config.getPort() + ", tls enabled: " + config.isTlsEnabled() + ")";
    }


    @Override
    protected ModbusValueProvider createValueProvider(Reference reference, ModbusValueProviderConfig providerConfig) throws AssetConnectionException {
        return new ModbusValueProvider(serviceContext, reference, client, providerConfig);
    }


    @Override
    protected ModbusOperationProvider createOperationProvider(Reference reference, ModbusOperationProviderConfig providerConfig) throws AssetConnectionException {
        return new ModbusOperationProvider(serviceContext, reference, client, providerConfig);
    }


    @Override
    protected ModbusSubscriptionProvider createSubscriptionProvider(Reference reference, ModbusSubscriptionProviderConfig providerConfig) throws AssetConnectionException {
        return new ModbusSubscriptionProvider(serviceContext, reference, client, providerConfig);
    }


    @Override
    public boolean isConnected() {
        return client.isConnected();
    }


    @Override
    protected void doConnect() throws AssetConnectionException {
        if (client.isConnected()) {
            return;
        }

        LOGGER.debug("Attempting to connect to {}.", getEndpointInformation());

        try {
            client.connect();
        }
        catch (ModbusExecutionException e) {
            throw new AssetConnectionException(e);
        }
        LOGGER.debug("Successfully connected to {}.", getEndpointInformation());
    }


    @Override
    protected void doDisconnect() throws AssetConnectionException {
        if (!client.isConnected()) {
            return;
        }
        LOGGER.debug("Attempting to disconnect from {}.", getEndpointInformation());
        try {
            client.disconnect();
        }
        catch (ModbusExecutionException e) {
            throw new AssetConnectionException(e);
        }
        LOGGER.debug("Successfully disconnected from {}.", getEndpointInformation());
    }


    private KeyManagerFactory initializeKeyManagerFactory(CertificateConfig keyCertificateConfig) throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStoreHelper.load(Path.of(keyCertificateConfig.getKeyStorePath()).toFile(), keyCertificateConfig.getKeyStoreType(),
                keyCertificateConfig.getKeyStorePassword());
        return SecurityUtil.createKeyManagerFactory(keyStore, keyCertificateConfig.getKeyStorePassword().toCharArray());

    }


    private TrustManagerFactory initializeTrustManagerFactory(CertificateConfig trustCertificateConfig) throws GeneralSecurityException, IOException {
        KeyStore trustStore = KeyStoreHelper.load(Path.of(trustCertificateConfig.getKeyStorePath()).toFile(), trustCertificateConfig.getKeyStoreType(),
                trustCertificateConfig.getKeyStorePassword());
        return SecurityUtil.createTrustManagerFactory(trustStore);

    }
}
