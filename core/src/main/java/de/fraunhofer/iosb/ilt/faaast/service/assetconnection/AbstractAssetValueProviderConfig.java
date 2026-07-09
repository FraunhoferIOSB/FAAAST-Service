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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection;

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Abstract base class for asset value provider configs.
 */
public abstract class AbstractAssetValueProviderConfig implements AssetValueProviderConfig {
    public static final ReadWriteMode DEFAULT_READ_WRITE_MODE = ReadWriteMode.READ_WRITE;

    protected ReadWriteMode readWriteMode;

    protected AbstractAssetValueProviderConfig() {
        this.readWriteMode = DEFAULT_READ_WRITE_MODE;
    }


    @Override
    public ReadWriteMode getReadWriteMode() {
        return readWriteMode;
    }


    public void setReadWriteMode(ReadWriteMode readWriteMode) {
        this.readWriteMode = readWriteMode;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AbstractAssetValueProviderConfig that = (AbstractAssetValueProviderConfig) obj;
        return Objects.equals(readWriteMode, that.readWriteMode);
    }


    @Override
    public int hashCode() {
        return Objects.hash(readWriteMode);
    }

    public abstract static class AbstractBuilder<T extends AbstractAssetValueProviderConfig, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B readWriteMode(ReadWriteMode value) {
            getBuildingInstance().setReadWriteMode(value);
            return getSelf();
        }
    }

}
