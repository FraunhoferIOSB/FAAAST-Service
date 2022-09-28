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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Value class for AnnotatedRelationShipElement.
 */
public class AnnotatedRelationshipElementValue extends RelationshipElementValue {

    private Map<String, DataElementValue> annotations;

    public AnnotatedRelationshipElementValue() {
        this.annotations = new HashMap<>();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AnnotatedRelationshipElementValue that = (AnnotatedRelationshipElementValue) o;
        return super.equals(o)
                && Objects.equals(annotations, that.annotations);
    }


    public Map<String, DataElementValue> getAnnotations() {
        return annotations;
    }


    public void setAnnotations(Map<String, DataElementValue> annotations) {
        this.annotations = annotations;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), annotations);
    }

    public abstract static class AbstractBuilder<T extends AnnotatedRelationshipElementValue, B extends AbstractBuilder<T, B>>
            extends RelationshipElementValue.AbstractBuilder<T, B> {

        public B annotations(Map<String, DataElementValue> value) {
            getBuildingInstance().setAnnotations(value);
            return getSelf();
        }


        public B annotation(String name, DataElementValue value) {
            getBuildingInstance().getAnnotations().put(name, value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<AnnotatedRelationshipElementValue, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected AnnotatedRelationshipElementValue newBuildingInstance() {
            return new AnnotatedRelationshipElementValue();
        }
    }
}
