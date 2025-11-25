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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.fixtures.foo;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


public class FooValueProviderConfig implements AssetValueProviderConfig {

    private String property1;

    public String getProperty1() {
        return property1;
    }


    public void setProperty1(String property1) {
        this.property1 = property1;
    }


    @Override
    public int hashCode() {
        return Objects.hash(property1);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FooValueProviderConfig other = (FooValueProviderConfig) obj;
        return Objects.equals(this.property1, other.property1);
    }


    @Override
    public boolean sameAs(AssetProviderConfig other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        final FooValueProviderConfig that = (FooValueProviderConfig) other;
        return StringHelper.equalsNullOrEmpty(property1, that.property1);
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<FooValueProviderConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected FooValueProviderConfig newBuildingInstance() {
            return new FooValueProviderConfig();
        }
    }

    private abstract static class AbstractBuilder<T extends FooValueProviderConfig, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {
        public B property1(String value) {
            getBuildingInstance().setProperty1(value);
            return getSelf();
        }
    }
}
