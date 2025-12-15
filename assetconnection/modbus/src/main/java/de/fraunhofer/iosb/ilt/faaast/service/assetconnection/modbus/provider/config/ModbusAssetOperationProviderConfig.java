package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.config;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.ArgumentValidationMode;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetProviderConfig;


public class ModbusAssetOperationProviderConfig implements AssetOperationProviderConfig {
    @Override
    public ArgumentValidationMode getInputValidationMode() {
        return null;
    }


    @Override
    public void setInputValidationMode(ArgumentValidationMode mode) {

    }


    @Override
    public ArgumentValidationMode getInoutputValidationMode() {
        return null;
    }


    @Override
    public void setInoutputValidationMode(ArgumentValidationMode mode) {

    }


    @Override
    public ArgumentValidationMode getOutputValidationMode() {
        return null;
    }


    @Override
    public void setOutputValidationMode(ArgumentValidationMode mode) {

    }


    @Override
    public boolean sameAs(AssetProviderConfig other) {
        return false;
    }
}
