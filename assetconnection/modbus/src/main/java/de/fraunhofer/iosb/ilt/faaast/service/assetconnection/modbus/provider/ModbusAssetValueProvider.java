package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;


public class ModbusAssetValueProvider implements AssetValueProvider {
    @Override
    public DataElementValue getValue() throws AssetConnectionException {
        return null;
    }


    @Override
    public void setValue(DataElementValue value) throws AssetConnectionException {

    }


    @Override
    public AssetProviderConfig asConfig() {
        return null;
    }
}
