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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.fixtures.bar;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import java.util.Objects;


public class BarConnectionConfig extends AssetConnectionConfig<BarConnection, BarValueProviderConfig, BarOperationProviderConfig, BarSubscriptionProviderConfig> {

    private String property1;
    private int property2;

    public String getProperty1() {
        return property1;
    }


    public void setProperty1(String property1) {
        this.property1 = property1;
    }


    public int getProperty2() {
        return property2;
    }


    public void setProperty2(int property2) {
        this.property2 = property2;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BarConnectionConfig that = (BarConnectionConfig) obj;
        return super.equals(obj)
                && Objects.equals(property1, that.property1)
                && Objects.equals(property2, that.property2);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), property1, property2);
    }


    @Override
    public boolean equalsIgnoringProviders(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BarConnectionConfig that = (BarConnectionConfig) obj;
        return Objects.equals(property1, that.property1)
                && Objects.equals(property2, that.property2);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends BarConnectionConfig, B extends AbstractBuilder<T, B>>
            extends
            AssetConnectionConfig.AbstractBuilder<BarConnectionConfig, BarValueProviderConfig, BarValueProvider, BarOperationProviderConfig, BarOperationProvider, BarSubscriptionProviderConfig, BarSubscriptionProvider, BarConnection, B> {

        public B property1(String value) {
            getBuildingInstance().setProperty1(value);
            return getSelf();
        }


        public B property2(int value) {
            getBuildingInstance().setProperty2(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<BarConnectionConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected BarConnectionConfig newBuildingInstance() {
            return new BarConnectionConfig();
        }
    }

}
