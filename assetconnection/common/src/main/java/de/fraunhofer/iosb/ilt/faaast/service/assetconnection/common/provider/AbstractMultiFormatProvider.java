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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.provider.config.MultiFormatProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.Objects;


/**
 * Abstract base class for providers that support reading values using multiple formats.
 *
 * @param <T> type of matching configuration
 */
public abstract class AbstractMultiFormatProvider<T extends MultiFormatProviderConfig> {

    protected final T config;

    protected AbstractMultiFormatProvider(T config) {
        Ensure.requireNonNull(config, "config must be non-null");
        this.config = config;
    }


    @Override
    public int hashCode() {
        return Objects.hash(config);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractMultiFormatProvider)) {
            return false;
        }
        final AbstractMultiFormatProvider<?> that = (AbstractMultiFormatProvider<?>) obj;
        return Objects.equals(config, that.config);
    }
}
