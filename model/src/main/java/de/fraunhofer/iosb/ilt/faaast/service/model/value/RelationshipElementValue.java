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

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Value class for RelationShipElement.
 */
public class RelationshipElementValue extends ElementValue {

    private Reference first;
    private Reference second;

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


    public Reference getFirst() {
        return first;
    }


    public void setFirst(Reference first) {
        this.first = first;
    }


    public Reference getSecond() {
        return second;
    }


    public void setSecond(Reference second) {
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

        public B first(Reference value) {
            getBuildingInstance().setFirst(value);
            return getSelf();
        }


        public B second(Reference value) {
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
