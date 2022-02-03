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
package de.fraunhofer.iosb.ilt.faaast.service.typing;

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.Datatype;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class TypeInfo {

    public static Builder builder() {
        return new Builder();
    }

    private Datatype datatype;

    private List<String> idShortPath;
    private Class<? extends ElementValue> valueType;

    public TypeInfo() {
        this.idShortPath = new ArrayList<>();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TypeInfo that = (TypeInfo) o;
        return Objects.equals(idShortPath, that.idShortPath)
                && Objects.equals(valueType, that.valueType)
                && Objects.equals(datatype, that.datatype);
    }


    public Datatype getDatatype() {
        return datatype;
    }


    public void setDatatype(Datatype datatype) {
        this.datatype = datatype;
    }


    public List<String> getIdShortPath() {
        return idShortPath;
    }


    public void setIdShortPath(List<String> idShortPath) {
        this.idShortPath = idShortPath;
    }


    public Class<? extends ElementValue> getValueType() {
        return valueType;
    }


    public void setValueType(Class<? extends ElementValue> valueType) {
        this.valueType = valueType;
    }


    @Override
    public int hashCode() {
        return Objects.hash(idShortPath, valueType, datatype);
    }

    public static abstract class AbstractBuilder<T extends TypeInfo, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B idShortPath(List<String> value) {
            getBuildingInstance().setIdShortPath(value);
            return getSelf();
        }


        public B idShortPath(String... value) {
            getBuildingInstance().setIdShortPath(Arrays.asList(value));
            return getSelf();
        }


        public B idShortPath(String value) {
            getBuildingInstance().getIdShortPath().add(value);
            return getSelf();
        }


        public B datatype(Datatype value) {
            getBuildingInstance().setDatatype(value);
            return getSelf();
        }


        public B valueType(Class<? extends ElementValue> value) {
            getBuildingInstance().setValueType(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<TypeInfo, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected TypeInfo newBuildingInstance() {
            return new TypeInfo();
        }
    }

}
