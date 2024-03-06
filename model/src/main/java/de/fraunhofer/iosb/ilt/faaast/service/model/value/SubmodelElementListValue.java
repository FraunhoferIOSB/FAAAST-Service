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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Value class for SubmodelElementList.
 */
public class SubmodelElementListValue extends DataElementValue {

    private List<ElementValue> values;

    public SubmodelElementListValue() {
        this.values = new ArrayList<>();
    }


    public List<ElementValue> getValues() {
        return values;
    }


    public void setValues(List<ElementValue> values) {
        this.values = values;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubmodelElementListValue other = (SubmodelElementListValue) o;
        return Objects.equals(values, other.values);
    }


    @Override
    public int hashCode() {
        return Objects.hash(values);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends SubmodelElementListValue, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B values(List<ElementValue> value) {
            getBuildingInstance().setValues(value);
            return getSelf();
        }


        public B value(ElementValue value) {
            getBuildingInstance().getValues().add(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<SubmodelElementListValue, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected SubmodelElementListValue newBuildingInstance() {
            return new SubmodelElementListValue();
        }
    }
}
