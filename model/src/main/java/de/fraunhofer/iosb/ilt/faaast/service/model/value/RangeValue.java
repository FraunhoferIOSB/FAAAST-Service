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

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.Objects;


public class RangeValue<T> extends DataElementValue {

    private TypedValue<T> max;
    private TypedValue<T> min;

    public RangeValue() {}


    public RangeValue(TypedValue<T> min, TypedValue<T> max) {
        this.min = min;
        this.max = max;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RangeValue that = (RangeValue) o;
        return Objects.equals(that.min, min)
                && Objects.equals(that.max, max);
    }


    public TypedValue<T> getMax() {
        return max;
    }


    public void setMax(TypedValue<T> max) {
        this.max = max;
    }


    public TypedValue<T> getMin() {
        return min;
    }


    public void setMin(TypedValue<T> min) {
        this.min = min;
    }


    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends RangeValue, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B min(TypedValue value) {
            getBuildingInstance().setMin(value);
            return getSelf();
        }


        public B max(TypedValue value) {
            getBuildingInstance().setMax(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<RangeValue, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected RangeValue newBuildingInstance() {
            return new RangeValue();
        }
    }
}
