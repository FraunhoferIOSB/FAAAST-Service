package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.ModbusAssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.ModbusAssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.ModbusAssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusAssetSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusAssetValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import java.util.Map;


public class ModbusAssetConnection implements
        AssetConnection<ModbusAssetConnectionConfig, ModbusAssetValueProviderConfig, ModbusAssetValueProvider, ModbusOperationProviderConfig, ModbusAssetOperationProvider,
                ModbusAssetSubscriptionProviderConfig, ModbusAssetSubscriptionProvider> {

    @Override
    public void disconnect() throws AssetConnectionException {

    }


    @Override
    public void connect() throws AssetConnectionException {

    }


    @Override
    public boolean isConnected() {
        return false;
    }


    @Override
    public Map<Reference, ModbusAssetOperationProvider> getOperationProviders() {
        return Map.of();
    }


    @Override
    public Map<Reference, ModbusAssetSubscriptionProvider> getSubscriptionProviders() {
        return Map.of();
    }


    @Override
    public Map<Reference, ModbusAssetValueProvider> getValueProviders() {
        return Map.of();
    }


    @Override
    public String getEndpointInformation() {
        return "";
    }


    @Override
    public void registerOperationProvider(Reference reference, ModbusOperationProviderConfig providerConfig) throws AssetConnectionException {

    }


    @Override
    public void registerSubscriptionProvider(Reference reference, ModbusAssetSubscriptionProviderConfig providerConfig) throws AssetConnectionException {

    }


    @Override
    public void registerValueProvider(Reference reference, ModbusAssetValueProviderConfig providerConfig) throws AssetConnectionException {

    }


    @Override
    public void unregisterOperationProvider(Reference reference) throws AssetConnectionException {

    }


    @Override
    public void unregisterSubscriptionProvider(Reference reference) throws AssetConnectionException {

    }


    @Override
    public void unregisterValueProvider(Reference reference) throws AssetConnectionException {

    }


    @Override
    public void stop() {

    }


    @Override
    public void init(CoreConfig coreConfig, ModbusAssetConnectionConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {

    }


    @Override
    public ModbusAssetConnectionConfig asConfig() {
        return null;
    }
}
