package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config.ModbusAssetOperationProviderConfig;


public class ModbusAssetOperationProvider implements AssetOperationProvider<ModbusAssetOperationProviderConfig> {
    @Override
    public ModbusAssetOperationProviderConfig getConfig() {
        return null;
    }


    @Override
    public AssetProviderConfig asConfig() {
        return null;
    }
}
