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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.postgres;

import de.fraunhofer.iosb.ilt.faaast.service.persistence.PersistenceConfig;
import java.util.Objects;


/**
 * Configuration for a persistence with a Postgres database.
 */
public class PersistencePostgresConfig extends PersistenceConfig<PersistencePostgres> {

    private String jdbcUrl = "jdbc:postgresql://localhost:5432/faaast";
    private String username = "postgres";
    private String password = "";
    private boolean override = false;

    public String getJdbcUrl() {
        return jdbcUrl;
    }


    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public boolean getOverride() {
        return override;
    }


    public void setOverride(boolean override) {
        this.override = override;
    }


    @Override
    public int hashCode() {
        return Objects.hash(jdbcUrl,
                username,
                password,
                override);
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
        final PersistencePostgresConfig other = (PersistencePostgresConfig) obj;
        return Objects.equals(this.jdbcUrl, other.jdbcUrl)
                && Objects.equals(this.username, other.username)
                && Objects.equals(this.password, other.password)
                && Objects.equals(this.override, other.override);
    }


    public static Builder builder() {
        return new Builder();
    }

    private abstract static class AbstractBuilder<T extends PersistencePostgresConfig, B extends AbstractBuilder<T, B>>
            extends PersistenceConfig.AbstractBuilder<PersistencePostgres, T, B> {
        public B jdbc(String value) {
            getBuildingInstance().setJdbcUrl(value);
            return getSelf();
        }


        public B username(String value) {
            getBuildingInstance().setUsername(value);
            return getSelf();
        }


        public B password(String value) {
            getBuildingInstance().setPassword(value);
            return getSelf();
        }


        public B override(boolean value) {
            getBuildingInstance().setOverride(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<PersistencePostgresConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected PersistencePostgresConfig newBuildingInstance() {
            return new PersistencePostgresConfig();
        }
    }
}
