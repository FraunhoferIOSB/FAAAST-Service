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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.provider;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.provider.config.MultiFormatReadProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.util.MultiFormatReadWriteHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;


/**
 * Abstract base class for providers that support reading values using multiple formats.
 *
 * @param <T> type of matching configuration
 */
public abstract class AbstractMultiFormatReadProvider<T extends MultiFormatReadProviderConfig> extends AbstractMultiFormatProvider<T> implements MultiFormatReadProvider {

    protected AbstractMultiFormatReadProvider(T config) {
        super(config);
    }


    @Override
    public DataElementValue getValue() throws AssetConnectionException {
        return MultiFormatReadWriteHelper.convertForRead(config, getRawValue(), getTypeInfo());
    }


    /**
     * Gets type information about the element to read.
     *
     * @return the type information
     */
    protected abstract TypeInfo getTypeInfo();
}
