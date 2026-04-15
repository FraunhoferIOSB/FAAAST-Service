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
package de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.provider.config;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AbstractAssetValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.util.Objects;


public class CustomValueProviderConfig extends AbstractAssetValueProviderConfig {

    private static final String DEFAULT_NOTE = "-";
    private String note;

    public CustomValueProviderConfig() {
        this.note = DEFAULT_NOTE;
    }


    public String getNote() {
        return note;
    }


    public void setNote(String note) {
        this.note = note;
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        final CustomValueProviderConfig that = (CustomValueProviderConfig) other;
        return Objects.equals(note, that.note);
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
        final CustomValueProviderConfig that = (CustomValueProviderConfig) other;
        return StringHelper.equalsNullOrEmpty(note, that.note);
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(note);
    }

    public class Builder extends AbstractAssetValueProviderConfig.AbstractBuilder<CustomValueProviderConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected CustomValueProviderConfig newBuildingInstance() {
            return new CustomValueProviderConfig();
        }


        public Builder note(String value) {
            this.getBuildingInstance().setNote(value);
            return getSelf();
        }

    }

}
