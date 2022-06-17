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
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class RelationshipElementValue extends ElementValue {

    private List<Key> first;
    private List<Key> second;

    public RelationshipElementValue() {
        this.first = new ArrayList<>();
        this.second = new ArrayList<>();
    }


    public RelationshipElementValue(List<Key> first, List<Key> second) {
        this.first = first;
        this.second = second;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RelationshipElementValue that = (RelationshipElementValue) o;
        return Objects.equals(first, that.first) && Objects.equals(second, that.second);
    }


    public List<Key> getFirst() {
        return first;
    }


    public void setFirst(List<Key> first) {
        this.first = first;
    }


    public List<Key> getSecond() {
        return second;
    }


    public void setSecond(List<Key> second) {
        this.second = second;
    }


    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends RelationshipElementValue, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B first(List<Key> value) {
            getBuildingInstance().setFirst(value);
            return getSelf();
        }


        public B second(List<Key> value) {
            getBuildingInstance().setSecond(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<RelationshipElementValue, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected RelationshipElementValue newBuildingInstance() {
            return new RelationshipElementValue();
        }
    }
}
