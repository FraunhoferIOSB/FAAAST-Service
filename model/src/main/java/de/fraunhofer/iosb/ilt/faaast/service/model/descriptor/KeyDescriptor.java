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
package de.fraunhofer.iosb.ilt.faaast.service.model.descriptor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.io.Serializable;
import java.util.Objects;


/**
 * Registry Descriptor for Key.
 */
public class KeyDescriptor implements Serializable {

    @JsonIgnore
    private String id;
    private KeyType idType;
    private KeyElements type;
    private String value;

    public KeyDescriptor() {
        id = null;
        idType = null;
        type = null;
        value = null;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public KeyType getIdType() {
        return idType;
    }


    public void setIdType(KeyType idType) {
        this.idType = idType;
    }


    public KeyElements getType() {
        return type;
    }


    public void setType(KeyElements type) {
        this.type = type;
    }


    public String getValue() {
        return value;
    }


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
        KeyDescriptor that = (KeyDescriptor) o;
        return Objects.equals(id, that.id)
                && Objects.equals(idType, that.idType)
                && Objects.equals(type, that.type)
                && Objects.equals(value, that.value);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, idType, type, value);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends KeyDescriptor, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


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

    public static class Builder extends AbstractBuilder<KeyDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected KeyDescriptor newBuildingInstance() {
            return new KeyDescriptor();
        }
    }
}
