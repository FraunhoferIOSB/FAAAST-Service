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
package de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl;

import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.KeyDescriptor;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.Objects;


/**
 * Registry Descriptor default implementation for Key.
 */
public class DefaultKeyDescriptor implements KeyDescriptor {

    private KeyType idType;
    private KeyElements type;
    private String value;

    public DefaultKeyDescriptor() {
        idType = null;
        type = null;
        value = null;
    }


    public DefaultKeyDescriptor(KeyDescriptor source) {
        idType = source.getIdType();
        type = source.getType();
        value = source.getValue();
    }


    @Override
    public KeyType getIdType() {
        return idType;
    }


    @Override
    public void setIdType(KeyType idType) {
        this.idType = idType;
    }


    @Override
    public KeyElements getType() {
        return type;
    }


    @Override
    public void setType(KeyElements type) {
        this.type = type;
    }


    @Override
    public String getValue() {
        return value;
    }


    @Override
    public void setValue(String value) {
        this.value = value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultKeyDescriptor that = (DefaultKeyDescriptor) o;
        return Objects.equals(idType, that.idType)
                && Objects.equals(type, that.type)
                && Objects.equals(value, that.value);
    }


    @Override
    public int hashCode() {
        return Objects.hash(idType, type, value);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends DefaultKeyDescriptor, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B idType(KeyType value) {
            getBuildingInstance().setIdType(value);
            return getSelf();
        }


        public B type(KeyElements value) {
            getBuildingInstance().setType(value);
            return getSelf();
        }


        public B value(String value) {
            getBuildingInstance().setValue(value);
            return getSelf();
        }


        public B from(Key key) {
            if (key != null) {
                getBuildingInstance().setIdType(key.getIdType());
                getBuildingInstance().setType(key.getType());
                getBuildingInstance().setValue(key.getValue());
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<DefaultKeyDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected DefaultKeyDescriptor newBuildingInstance() {
            return new DefaultKeyDescriptor();
        }
    }
}
