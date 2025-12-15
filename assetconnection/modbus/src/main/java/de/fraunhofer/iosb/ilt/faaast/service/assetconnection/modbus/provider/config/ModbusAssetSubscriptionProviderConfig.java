package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProviderConfig;


public class ModbusAssetSubscriptionProviderConfig implements AssetSubscriptionProviderConfig {
    @Override
    public boolean sameAs(AssetProviderConfig other) {
        return false;
    }
}
