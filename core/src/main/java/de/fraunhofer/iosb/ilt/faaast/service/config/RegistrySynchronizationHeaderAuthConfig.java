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
package de.fraunhofer.iosb.ilt.faaast.service.config;

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Header-based authentication config (e.g. Basic auth, API key).
 */
public class RegistrySynchronizationHeaderAuthConfig {

    private String name;
    private String value;

    public RegistrySynchronizationHeaderAuthConfig() {
        this.name = "";
        this.value = "";
    }


    public static Builder builder() {
        return new Builder();
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name != null
                ? name
                : "";
    }


    public String getValue() {
        return value;
    }


    public void setValue(String value) {
        this.value = value != null
                ? value
                : "";
    }


    public boolean isConfigured() {
        return name != null && !name.isBlank() && value != null && !value.isBlank();
    }


    @Override
    public int hashCode() {
        return Objects.hash(name, value);
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
        final RegistrySynchronizationHeaderAuthConfig other = (RegistrySynchronizationHeaderAuthConfig) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.value, other.value);
    }

    public static class Builder extends ExtendableBuilder<RegistrySynchronizationHeaderAuthConfig, Builder> {

        public Builder name(String value) {
            getBuildingInstance().setName(value);
            return getSelf();
        }


        public Builder value(String value) {
            getBuildingInstance().setValue(value);
            return getSelf();
        }


        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected RegistrySynchronizationHeaderAuthConfig newBuildingInstance() {
            return new RegistrySynchronizationHeaderAuthConfig();
        }
    }
}
