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
 * Configuration for registry synchronization (descriptor registration).
 */
public class RegistrySynchronizationConfig {

    public static final RegistrySynchronizationConfig DEFAULT = builder().build();

    private RegistrySynchronizationAuthConfig auth;

    public RegistrySynchronizationConfig() {
        this.auth = new RegistrySynchronizationAuthConfig();
    }


    public static Builder builder() {
        return new Builder();
    }


    /**
     * Gets authentication settings for registry synchronization requests.
     *
     * @return authentication configuration, never {@code null}
     */
    public RegistrySynchronizationAuthConfig getAuth() {
        if (auth == null) {
            auth = new RegistrySynchronizationAuthConfig();
        }
        return auth;
    }


    /**
     * Sets authentication settings for registry synchronization requests.
     *
     * @param auth authentication configuration; when {@code null} a default instance is used
     */
    public void setAuth(RegistrySynchronizationAuthConfig auth) {
        this.auth = auth != null
                ? auth
                : new RegistrySynchronizationAuthConfig();
    }


    @Override
    public int hashCode() {
        return Objects.hash(auth);
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
        final RegistrySynchronizationConfig other = (RegistrySynchronizationConfig) obj;
        return Objects.equals(this.auth, other.auth);
    }

    public static class Builder extends ExtendableBuilder<RegistrySynchronizationConfig, Builder> {

        public Builder auth(RegistrySynchronizationAuthConfig value) {
            getBuildingInstance().setAuth(value);
            return getSelf();
        }


        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected RegistrySynchronizationConfig newBuildingInstance() {
            return new RegistrySynchronizationConfig();
        }
    }
}
