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

import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


public class TypeContext {

    public static Builder builder() {
        return new Builder();
    }


    public static TypeContext fromElement(SubmodelElement element) {
        return TypeExtractor.getTypeContext(element);
    }

    private TypeInfo rootInfo;
    private List<TypeInfo> typeInfos;

    public TypeContext() {
        this.typeInfos = new ArrayList();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TypeContext that = (TypeContext) o;
        return Objects.equals(typeInfos, that.typeInfos) && Objects.equals(rootInfo, that.rootInfo);
    }


    public TypeInfo getRootInfo() {
        return rootInfo;
    }


    public void setRootInfo(TypeInfo rootInfo) {
        this.rootInfo = rootInfo;
    }


    public TypeInfo getTypeInfoByPath(Collection<String> idShortPath) {
        if (typeInfos == null) {
            return null;
        }
        return typeInfos.stream()
                .filter(x -> Objects.equals(idShortPath, x.getIdShortPath()))
                .findFirst()
                .orElse(null);
    }


    public List<TypeInfo> getTypeInfos() {
        return typeInfos;
    }


    public void setTypeInfos(List<TypeInfo> typeInfos) {
        this.typeInfos = typeInfos;
    }


    @Override
    public int hashCode() {
        return Objects.hash(typeInfos, rootInfo);
    }

    public static abstract class AbstractBuilder<T extends TypeContext, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B typeInfo(TypeInfo value) {
            getBuildingInstance().getTypeInfos().add(value);
            return getSelf();
        }


        public B rootInfo(TypeInfo value) {
            getBuildingInstance().setRootInfo(value);
            return getSelf();
        }


        public B typeInfos(List<TypeInfo> value) {
            getBuildingInstance().setTypeInfos(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<TypeContext, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected TypeContext newBuildingInstance() {
            return new TypeContext();
        }
    }

}
