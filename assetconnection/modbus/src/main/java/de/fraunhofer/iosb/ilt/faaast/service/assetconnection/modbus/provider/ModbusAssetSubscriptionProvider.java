package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;


public class ModbusAssetSubscriptionProvider implements AssetSubscriptionProvider {
    @Override
    public void addNewDataListener(NewDataListener listener) throws AssetConnectionException {

    }


    @Override
    public void removeNewDataListener(NewDataListener listener) throws AssetConnectionException {

    }


    @Override
    public void unsubscribe() throws AssetConnectionException {

    }


    @Override
    public AssetProviderConfig asConfig() {
        return null;
    }
}
