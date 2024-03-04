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
package de.fraunhofer.iosb.ilt.faaast.service.starter.model;

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Override information for a config property.
 */
public class ConfigOverride {

    private String originalKey;
    private String updatedKey;
    private String value;
    private ConfigOverrideSource source;

    public String getOriginalKey() {
        return originalKey;
    }


    public void setOriginalKey(String originalKey) {
        this.originalKey = originalKey;
    }


    public String getUpdatedKey() {
        return updatedKey;
    }


    public void setUpdatedKey(String updatedKey) {
        this.updatedKey = updatedKey;
    }


    public String getValue() {
        return value;
    }


    public void setValue(String value) {
        this.value = value;
    }


    public ConfigOverrideSource getSource() {
        return source;
    }


    public void setSource(ConfigOverrideSource source) {
        this.source = source;
    }


    public static Builder builder() {
        return new Builder();
    }


    @Override
    public int hashCode() {
        return Objects.hash(originalKey, updatedKey, value, source);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConfigOverride that = (ConfigOverride) o;
        return Objects.equals(originalKey, that.originalKey)
                && Objects.equals(updatedKey, that.updatedKey)
                && Objects.equals(value, that.value)
                && Objects.equals(source, that.source);
    }

    public abstract static class AbstractBuilder<T extends ConfigOverride, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B originalKey(String value) {
            getBuildingInstance().setOriginalKey(value);
            updatedKey(value);
            return getSelf();
        }


        public B updatedKey(String value) {
            getBuildingInstance().setUpdatedKey(value);
            return getSelf();
        }


        public B value(String value) {
            getBuildingInstance().setValue(value);
            return getSelf();
        }


        public B source(ConfigOverrideSource value) {
            getBuildingInstance().setSource(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<ConfigOverride, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected ConfigOverride newBuildingInstance() {
            return new ConfigOverride();
        }
    }

}
