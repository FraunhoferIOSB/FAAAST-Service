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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.util;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.format.FormatFactory;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.provider.config.MultiFormatReadProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.provider.config.MultiFormatWriteProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;


/**
 * Helper to convert data for reading/writing.
 */
public class MultiFormatReadWriteHelper {

    public static final String DEFAULT_TEMPLATE = "${value}";

    private MultiFormatReadWriteHelper() {}


    /**
     * Converts byte[] value with additional configuration to AAS data model according to selected data format.
     *
     * @param config the configuration
     * @param value the value, typically raw payload received by transport protocol
     * @param typeinfo type information about target AAS type
     * @return value converted to AAS value
     * @throws AssetConnectionException if conversion fails
     */
    public static DataElementValue convertForRead(MultiFormatReadProviderConfig config, byte[] value, TypeInfo<?> typeinfo) throws AssetConnectionException {
        return FormatFactory
                .create(config.getFormat())
                .read(new String(value), config.getQuery(), typeinfo);
    }


    /**
     * Converts AAS value to byte[] to be forwarded to transport protocol of an asset connection.
     *
     * @param config the configuration
     * @param value the value, typically raw payload received by transport protocol
     * @return value as byte[]
     * @throws AssetConnectionException if conversion fails
     */
    public static byte[] convertForWrite(MultiFormatWriteProviderConfig config, DataElementValue value) throws AssetConnectionException {
        if (!(value instanceof PropertyValue)) {
            throw new AssetConnectionException(String.format("unsupported value (%s)", value.getClass().getSimpleName()));
        }
        return TemplateHelper.replace(
                StringUtils.isBlank(config.getTemplate())
                        ? DEFAULT_TEMPLATE
                        : config.getTemplate(),
                Map.of("value",
                        FormatFactory
                                .create(config.getFormat())
                                .write(value)))
                .getBytes();
    }
}
