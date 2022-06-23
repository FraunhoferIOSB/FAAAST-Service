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
package de.fraunhofer.iosb.ilt.faaast.service.model.value;

import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class ReferenceElementValue extends DataElementValue {

    private List<Key> keys;

    public ReferenceElementValue() {
        this.keys = new ArrayList<>();
    }


    public ReferenceElementValue(List<Key> keys) {
        this.keys = keys;
    }


    public ReferenceElementValue(Key... keys) {
        this.keys = Arrays.asList(keys);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReferenceElementValue that = (ReferenceElementValue) o;
        return Objects.equals(keys, that.keys);
    }


    public List<Key> getKeys() {
        return keys;
    }


    public void setKeys(List<Key> keys) {
        this.keys = keys;
    }


    @Override
    public int hashCode() {
        return Objects.hash(keys);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends ReferenceElementValue, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B keys(List<Key> value) {
            getBuildingInstance().setKeys(value);
            return getSelf();
        }


        public B key(Key value) {
            getBuildingInstance().getKeys().add(value);
            return getSelf();
        }


        public B key(KeyType idType, KeyElements type, String value) {
            getBuildingInstance().getKeys().add(new DefaultKey.Builder()
                    .idType(idType)
                    .type(type)
                    .value(value)
                    .build());
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<ReferenceElementValue, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected ReferenceElementValue newBuildingInstance() {
            return new ReferenceElementValue();
        }
    }
}
