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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx;

import io.adminshell.aas.v3.model.builder.ExtendableBuilder;


public class InfluxServerConfig {

    private String adminPassword;
    private String adminUser;
    private boolean authEnabled = true;
    private String bucket;
    private String database;
    private String organization;
    private String password;
    private String token;
    private String username;
    private String version;

    public boolean getAuthEnabled() {
        return authEnabled;
    }


    public void setAuthEnabled(boolean authEnabled) {
        this.authEnabled = authEnabled;
    }


    public String getAdminPassword() {
        return adminPassword;
    }


    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }


    public String getAdminUser() {
        return adminUser;
    }


    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }


    public String getBucket() {
        return bucket;
    }


    public void setBucket(String bucket) {
        this.bucket = bucket;
    }


    public String getDatabase() {
        return database;
    }


    public void setDatabase(String database) {
        this.database = database;
    }


    public String getOrganization() {
        return organization;
    }


    public void setOrganization(String organization) {
        this.organization = organization;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public String getToken() {
        return token;
    }


    public void setToken(String token) {
        this.token = token;
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public String getVersion() {
        return version;
    }


    public void setVersion(String version) {
        this.version = version;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ExtendableBuilder<InfluxServerConfig, Builder> {

        public Builder authEnabled(boolean value) {
            getBuildingInstance().setAuthEnabled(value);
            return this;
        }


        public Builder authEnabled() {
            getBuildingInstance().setAuthEnabled(true);
            return this;
        }


        public Builder authDisabled() {
            getBuildingInstance().setAuthEnabled(false);
            return this;
        }


        public Builder adminPassword(String value) {
            getBuildingInstance().setAdminPassword(value);
            return this;
        }


        public Builder adminUser(String value) {
            getBuildingInstance().setAdminUser(value);
            return this;
        }


        public Builder bucket(String value) {
            getBuildingInstance().setBucket(value);
            return this;
        }


        public Builder database(String value) {
            getBuildingInstance().setDatabase(value);
            return this;
        }


        public Builder organization(String value) {
            getBuildingInstance().setOrganization(value);
            return this;
        }


        public Builder password(String value) {
            getBuildingInstance().setPassword(value);
            return this;
        }


        public Builder token(String value) {
            getBuildingInstance().setToken(value);
            return this;
        }


        public Builder username(String value) {
            getBuildingInstance().setUsername(value);
            return this;
        }


        public Builder version(String value) {
            getBuildingInstance().setVersion(value);
            return this;
        }


        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected InfluxServerConfig newBuildingInstance() {
            return new InfluxServerConfig();
        }

    }

}
