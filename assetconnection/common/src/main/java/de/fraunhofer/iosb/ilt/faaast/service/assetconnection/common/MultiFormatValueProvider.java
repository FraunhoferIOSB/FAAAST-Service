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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.Map;
import org.codehaus.plexus.util.StringUtils;


public abstract class MultiFormatValueProvider<T extends MultiFormatValueProviderConfig> implements AssetValueProvider {

    protected static final String DEFAULT_TEMPLATE = "${value}";

    protected final T config;

    protected MultiFormatValueProvider(T config) {
        Ensure.requireNonNull(config, "config must be non-null");
        this.config = config;
    }


    @Override
    public DataElementValue getValue() throws AssetConnectionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    protected abstract void setValue(byte[] value) throws AssetConnectionException;


    @Override
    public void setValue(DataElementValue value) throws AssetConnectionException {
        if (!(value instanceof PropertyValue)) {
            throw new AssetConnectionException(String.format("unsupported value (%s)", value.getClass().getSimpleName()));
        }
        setValue(TemplateHelper.replace(
                StringUtils.isBlank(config.getTemplate())
                        ? DEFAULT_TEMPLATE
                        : config.getTemplate(),
                Map.of("value",
                        FormatFactory
                                .create(config.getFormat())
                                .write(value)))
                .getBytes());
    }

}
