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
 * Authentication configuration for registry synchronization requests.
 */
public class RegistrySynchronizationAuthConfig {

    private RegistrySynchronizationHeaderAuthConfig header;

    public RegistrySynchronizationAuthConfig() {
        this.header = new RegistrySynchronizationHeaderAuthConfig();
    }


    public static Builder builder() {
        return new Builder();
    }


    /**
     * Gets static header authentication settings.
     *
     * @return header authentication configuration, never {@code null}
     */
    public RegistrySynchronizationHeaderAuthConfig getHeader() {
        if (header == null) {
            header = new RegistrySynchronizationHeaderAuthConfig();
        }
        return header;
    }


    /**
     * Sets static header authentication settings.
     *
     * @param header header authentication configuration; when {@code null} a default instance is used
     */
    public void setHeader(RegistrySynchronizationHeaderAuthConfig header) {
        this.header = header != null
                ? header
                : new RegistrySynchronizationHeaderAuthConfig();
    }


    @Override
    public int hashCode() {
        return Objects.hash(header);
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
        final RegistrySynchronizationAuthConfig other = (RegistrySynchronizationAuthConfig) obj;
        return Objects.equals(this.header, other.header);
    }

    public static class Builder extends ExtendableBuilder<RegistrySynchronizationAuthConfig, Builder> {

        public Builder header(RegistrySynchronizationHeaderAuthConfig value) {
            getBuildingInstance().setHeader(value);
            return getSelf();
        }


        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected RegistrySynchronizationAuthConfig newBuildingInstance() {
            return new RegistrySynchronizationAuthConfig();
        }
    }
}
