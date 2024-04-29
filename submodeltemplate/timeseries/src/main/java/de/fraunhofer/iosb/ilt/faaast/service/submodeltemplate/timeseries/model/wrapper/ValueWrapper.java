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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * Wraps a single value o an AAS {@link SubmodelElement}.
 *
 * @param <T> type of the value
 * @param <A> type of the AAS element
 */
public class ValueWrapper<T, A extends SubmodelElement> implements Wrapper<T, A> {

    private T value;
    private A aasElement;
    private final Class<A> aasType;
    private final Collection<SubmodelElement> parentValues;
    private final Function<T, A> convertToAAS;
    private final Predicate<A> canParseFromAAS;
    private final Function<A, T> parseFromAAS;
    private final boolean acceptsNullValue;

    public ValueWrapper(
            Collection<SubmodelElement> parentValues,
            T initialValue,
            boolean acceptsNullValue,
            Class<A> aasType,
            Function<T, A> convertToAAS,
            Predicate<A> canParseFromAAS,
            Function<A, T> parseFromAAS) {
        this.parentValues = parentValues;
        this.acceptsNullValue = acceptsNullValue;
        this.aasType = aasType;
        this.convertToAAS = convertToAAS;
        this.canParseFromAAS = canParseFromAAS;
        this.parseFromAAS = parseFromAAS;
        setValue(initialValue);
    }


    @Override
    public boolean canParse(A aasValue) {
        return canParseFromAAS.test(aasValue);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (obj == null) {
            return false;
        }
        else if (this.getClass() != obj.getClass()) {
            return false;
        }
        else {
            ValueWrapper other = (ValueWrapper) obj;
            return Objects.equals(this.value, other.value)
                    && Objects.equals(this.aasElement, other.aasElement)
                    && Objects.equals(this.aasType, other.aasType)
                    && Objects.equals(this.acceptsNullValue, other.acceptsNullValue)
                    && Objects.equals(this.parentValues, other.parentValues);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(value, aasElement, aasType, acceptsNullValue, parentValues);
    }


    @Override
    public Class<A> getAASType() {
        return aasType;
    }


    @Override
    public void parse(A aasValue) {
        setValue(parseFromAAS.apply(aasValue));
    }


    @Override
    public List<A> toAAS() {
        return List.of(convertToAAS.apply(value));
    }


    @Override
    public T getValue() {
        return value;
    }


    @Override
    public void setValue(T newValue) {
        A newAASElement = Objects.nonNull(newValue) || acceptsNullValue ? convertToAAS.apply(newValue) : null;
        if (!Objects.equals(aasElement, newAASElement)) {
            if (Objects.nonNull(aasElement)) {
                parentValues.remove(aasElement);
            }
            if (Objects.nonNull(newAASElement)) {
                parentValues.add(newAASElement);
            }
        }
        this.value = newValue;
        this.aasElement = newAASElement;
    }
}
