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

import java.io.File;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Class to store certificate information in the config.
 */
public class CertificateConfig {

    private static final String DEFAULT_KEYSTORE_TYPE = "PKCS12";

    private String keyStoreType;
    private String keyStorePath;
    private String keyStorePassword;
    private String keyAlias;
    private String keyPassword;

    public CertificateConfig() {
        this.keyStoreType = DEFAULT_KEYSTORE_TYPE;
    }


    public String getKeyStoreType() {
        return keyStoreType;
    }


    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }


    public String getKeyStorePath() {
        return keyStorePath;
    }


    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }


    public String getKeyStorePassword() {
        return keyStorePassword;
    }


    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }


    public String getKeyAlias() {
        return keyAlias;
    }


    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }


    public String getKeyPassword() {
        return keyPassword;
    }


    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }


    @Override
    public int hashCode() {
        return Objects.hash(keyStoreType, keyStorePath, keyStorePassword, keyAlias, keyPassword);
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
        final CertificateConfig other = (CertificateConfig) obj;
        return Objects.equals(this.keyStoreType, other.keyStoreType)
                && Objects.equals(this.keyStorePath, other.keyStorePath)
                && Objects.equals(this.keyStorePassword, other.keyStorePassword)
                && Objects.equals(this.keyAlias, other.keyAlias)
                && Objects.equals(this.keyPassword, other.keyPassword);
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ExtendableBuilder<CertificateConfig, Builder> {

        public Builder keyStoreType(String value) {
            getBuildingInstance().setKeyStoreType(value);
            return getSelf();
        }


        public Builder keyStorePath(String value) {
            getBuildingInstance().setKeyStorePath(value);
            return getSelf();
        }


        public Builder keyStorePath(File value) {
            getBuildingInstance().setKeyStorePath(value.getAbsolutePath());
            return getSelf();
        }


        public Builder keyPassword(String value) {
            getBuildingInstance().setKeyPassword(value);
            return getSelf();
        }


        public Builder keyStorePassword(String value) {
            getBuildingInstance().setKeyStorePassword(value);
            return getSelf();
        }


        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected CertificateConfig newBuildingInstance() {
            return new CertificateConfig();
        }

    }
}
