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
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.ReferenceDescriptor;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Registry Descriptor default implementation for Reference.
 */
public class DefaultReferenceDescriptor implements ReferenceDescriptor {

    private List<KeyDescriptor> keys;

    public DefaultReferenceDescriptor() {
        keys = new ArrayList<>();
    }


    @Override
    public List<KeyDescriptor> getKeys() {
        return keys;
    }


    @Override
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
        DefaultReferenceDescriptor that = (DefaultReferenceDescriptor) o;
        return Objects.equals(keys, that.keys);
    }


    @Override
    public int hashCode() {
        return Objects.hash(keys);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends DefaultReferenceDescriptor, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

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
                    getBuildingInstance().getKeys().add(DefaultKeyDescriptor.builder().from(key).build());
                }
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<DefaultReferenceDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected DefaultReferenceDescriptor newBuildingInstance() {
            return new DefaultReferenceDescriptor();
        }
    }
}
