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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Abstract base class for holding type information.
 *
 * @param <T> the actual type
 */
public abstract class TypeInfo<T> {

    protected Class<?> type;
    private Map<T, TypeInfo<?>> elements;

    protected TypeInfo() {
        this.elements = new HashMap<>();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TypeInfo<T> that = (TypeInfo<T>) o;
        return Objects.equals(elements, that.elements)
                && Objects.equals(type, that.type);
    }


    public Class<?> getType() {
        return type;
    }


    public void setType(Class<?> type) {
        this.type = type;
    }


    @Override
    public int hashCode() {
        return Objects.hash(elements, type);
    }

    public abstract static class AbstractBuilder<P, T extends TypeInfo<P>, B extends AbstractBuilder<P, T, B>> extends ExtendableBuilder<T, B> {

        public B type(Class<?> value) {
            getBuildingInstance().setType(value);
            return getSelf();
        }


        public B element(P key, TypeInfo<?> value) {
            getBuildingInstance().getElements().put(key, value);
            return getSelf();
        }


        public B elements(Map<P, TypeInfo<?>> value) {
            getBuildingInstance().setElements(value);
            return getSelf();
        }
    }

    public Map<T, TypeInfo<?>> getElements() {
        return elements;
    }


    public void setElements(Map<T, TypeInfo<?>> elements) {
        this.elements = elements;
    }

}
