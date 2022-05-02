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

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.Objects;


public class PropertyValue extends DataElementValue {

    private TypedValue value;

    public static PropertyValue of(String datatype, String value) throws ValueFormatException {
        return new PropertyValue(TypedValueFactory.create(datatype, value));
    }


    public static PropertyValue of(Datatype datatype, String value) throws ValueFormatException {
        return new PropertyValue(TypedValueFactory.create(datatype, value));
    }


    public PropertyValue() {}


    public PropertyValue(TypedValue value) {
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
        PropertyValue that = (PropertyValue) o;
        return Objects.equals(value, that.value);
    }


    public TypedValue getValue() {
        return value;
    }


    public void setValue(TypedValue value) {
        this.value = value;
    }


    @Override
    public int hashCode() {
        return Objects.hash(value);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends PropertyValue, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B value(TypedValue value) {
            getBuildingInstance().setValue(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<PropertyValue, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected PropertyValue newBuildingInstance() {
            return new PropertyValue();
        }
    }
}
