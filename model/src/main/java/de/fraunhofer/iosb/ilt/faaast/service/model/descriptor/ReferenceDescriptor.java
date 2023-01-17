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
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Registry Descriptor for Reference.
 */
public class ReferenceDescriptor implements Serializable {

    @JsonIgnore
    private String id;
    private List<KeyDescriptor> keys;

    public ReferenceDescriptor() {
        id = null;
        keys = new ArrayList<>();
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public List<KeyDescriptor> getKeys() {
        return keys;
    }


    public void setKeys(List<KeyDescriptor> keys) {
        this.keys = keys;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReferenceDescriptor that = (ReferenceDescriptor) o;
        return Objects.equals(id, that.id)
                && Objects.equals(keys, that.keys);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, keys);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends ReferenceDescriptor, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B keys(List<KeyDescriptor> value) {
            getBuildingInstance().setKeys(value);
            return getSelf();
        }


        public B key(KeyDescriptor value) {
            getBuildingInstance().getKeys().add(value);
            return getSelf();
        }


        public B from(Reference reference) {
            if (reference != null) {
                for (Key key: reference.getKeys()) {
                    getBuildingInstance().getKeys().add(KeyDescriptor.builder().from(key).build());
                }
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<ReferenceDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected ReferenceDescriptor newBuildingInstance() {
            return new ReferenceDescriptor();
        }
    }
}
