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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config;

import java.util.Objects;


/**
 * Superclass for OPC UA provider config classes with Array.
 */
public abstract class AbstractOpcUaProviderWithArrayConfig extends AbstractOpcUaProviderConfig {

    protected String arrayIndex;

    public String getArrayIndex() {
        return arrayIndex;
    }


    public void setArrayIndex(String arrayIndex) {
        this.arrayIndex = arrayIndex;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractOpcUaProviderWithArrayConfig that = (AbstractOpcUaProviderWithArrayConfig) o;
        return super.equals(o)
                && Objects.equals(arrayIndex, that.arrayIndex);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), arrayIndex);
    }

    protected abstract static class AbstractBuilder<T extends AbstractOpcUaProviderWithArrayConfig, B extends AbstractBuilder<T, B>>
            extends AbstractOpcUaProviderConfig.AbstractBuilder<T, B> {

        public B arrayIndex(String arrayElementIndex) {
            getBuildingInstance().setArrayIndex(arrayElementIndex);
            return getSelf();
        }

    }
}
