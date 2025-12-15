package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProviderConfig;


public class ModbusAssetValueProviderConfig implements AssetValueProviderConfig {
    @Override
    public boolean sameAs(AssetProviderConfig other) {
        return false;
    }
}
