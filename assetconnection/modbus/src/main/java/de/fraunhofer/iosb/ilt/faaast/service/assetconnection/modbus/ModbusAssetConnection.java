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
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AbstractAssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.ModbusOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.ModbusSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.ModbusValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


public class ModbusAssetConnection extends
        AbstractAssetConnection<ModbusAssetConnection, ModbusAssetConnectionConfig, ModbusValueProviderConfig, ModbusValueProvider, ModbusOperationProviderConfig, ModbusOperationProvider, ModbusSubscriptionProviderConfig, ModbusSubscriptionProvider> {

    ModbusTcpClient modbusTcpClient;

    @Override
    public void init(CoreConfig coreConfig, ModbusAssetConnectionConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        ModbusClientConfig modbusClientConfig = new ModbusClientConfig.Builder()
                .setRequestTimeout(config.getConnectTimeout()) // TODO should this be the same as request timeout?
                .build();

        CertificateConfig keyCertificateConfig = config.getKeyCertificateConfig();
        CertificateConfig trustCertificateConfig = config.getTrustCertificateConfig();
        KeyManagerFactory keyManagerFactory;
        TrustManagerFactory trustManagerFactory;
        try {
            // TODO this seems wrong. where to put path? Test with tls disabled for now.
            keyManagerFactory = KeyManagerFactory.getInstance(keyCertificateConfig.getKeyStoreType());
            trustManagerFactory = TrustManagerFactory.getInstance(trustCertificateConfig.getKeyStoreType());
        }
        catch (NoSuchAlgorithmException e) {
            // TODO
            throw new ConfigurationInitializationException(e);
        }

        NettyClientTransportConfig nettyConfig = new NettyClientTransportConfig.Builder()
                .setConnectTimeout(config.getConnectTimeout())
                .setPort(config.getPort())
                .setHostname(config.getHostname())
                .setConnectPersistent(config.isConnectPersistent())
                .setReconnectLazy(config.isReconnectLazy())
                .setTlsEnabled(config.isTlsEnabled())
                .setKeyManagerFactory(keyManagerFactory)
                .setTrustManagerFactory(trustManagerFactory)
                .build();

        modbusTcpClient = new ModbusTcpClient(modbusClientConfig, new NettyTcpClientTransport(nettyConfig));
    }


    @Override
    protected ModbusValueProvider createValueProvider(Reference reference, ModbusValueProviderConfig providerConfig) {
        return new ModbusValueProvider(serviceContext, reference, modbusTcpClient, config.getUnitId(), providerConfig);
    }


    @Override
    protected ModbusOperationProvider createOperationProvider(Reference reference, ModbusOperationProviderConfig providerConfig) throws AssetConnectionException {
        return new ModbusOperationProvider(serviceContext, reference, modbusTcpClient, asConfig().getUnitId(), providerConfig);
    }


    @Override
    protected ModbusSubscriptionProvider createSubscriptionProvider(Reference reference, ModbusSubscriptionProviderConfig providerConfig) {
        return new ModbusSubscriptionProvider(serviceContext, reference, modbusTcpClient, asConfig().getUnitId(), providerConfig);
    }


    @Override
    protected void doConnect() throws AssetConnectionException {
        try {
            modbusTcpClient.connect();
        }
        catch (ModbusExecutionException e) {
            throw new AssetConnectionException(e);
        }
    }


    @Override
    protected void doDisconnect() throws AssetConnectionException {
        try {
            modbusTcpClient.disconnect();
        }
        catch (ModbusExecutionException e) {
            throw new AssetConnectionException(e);
        }
    }


    @Override
    public String getEndpointInformation() {
        return config.getHostname();
    }
}
