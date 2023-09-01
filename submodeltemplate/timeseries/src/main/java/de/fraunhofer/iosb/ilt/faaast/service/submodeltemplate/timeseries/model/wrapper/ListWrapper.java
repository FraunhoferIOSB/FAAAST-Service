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

import io.adminshell.aas.v3.model.SubmodelElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Wraps a {@link List} instance to an AAS {@link SubmodelElement}.
 *
 * @param <T> type of the list
 * @param <A> type of the AAS element
 */
public class ListWrapper<T, A extends SubmodelElement> implements List<T>, Wrapper<List<T>, A> {

    private List<T> value;
    private List<A> aasElements;
    private final Class<A> aasType;
    private final Collection<SubmodelElement> parentValues;
    private final Function<T, A> convertToAAS;
    private final Predicate<A> canParseFromAAS;
    private final Function<A, T> parseFromAAS;

    public ListWrapper(
            Collection<SubmodelElement> parentValues,
            List<T> initialValue,
            Class<A> aasType,
            Function<T, A> convertToAAS,
            Predicate<A> canParseFromAAS,
            Function<A, T> parseFromAAS) {
        this.parentValues = parentValues;
        this.aasType = aasType;
        this.convertToAAS = convertToAAS;
        this.canParseFromAAS = canParseFromAAS;
        this.parseFromAAS = parseFromAAS;
        this.aasElements = new ArrayList<>();
        setValue(initialValue);
    }


    @Override
    public boolean add(T e) {
        boolean result = value.add(e);
        if (result) {
            addInternal(e);
        }
        return result;
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
            ListWrapper other = (ListWrapper) obj;
            return Objects.equals(this.value, other.value)
                    && Objects.equals(this.aasElements, other.aasElements)
                    && Objects.equals(this.aasType, other.aasType)
                    && Objects.equals(this.parentValues, other.parentValues);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(value, aasElements, aasType, parentValues);
    }


    @Override
    public void add(int index, T element) {
        T old = value.get(index);
        value.add(index, element);
        if (!Objects.equals(old, element)) {
            addInternal(element);
        }
    }


    @Override
    public boolean addAll(Collection<? extends T> c) {
        if (Objects.nonNull(c)) {
            return c.stream().map(this::add).anyMatch(x -> Objects.equals(x, true));
        }
        return false;
    }


    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean result = value.addAll(index, c);
        if (result) {
            c.forEach(this::addInternal);
        }
        return result;
    }


    @Override
    public boolean contains(Object o) {
        return value.contains(o);
    }


    @Override
    public boolean containsAll(Collection<?> c) {
        return value.containsAll(c);
    }


    @Override
    public T get(int index) {
        return value.get(index);
    }


    @Override
    public Class<A> getAASType() {
        return aasType;
    }


    @Override
    public boolean canParse(A aasValue) {
        return canParseFromAAS.test(aasValue);
    }


    @Override
    public void clear() {
        value.clear();
        parentValues.removeAll(aasElements);
        aasElements.clear();
    }


    @Override
    public int indexOf(Object o) {
        return value.indexOf(o);
    }


    @Override
    public Iterator<T> iterator() {
        return value.iterator();
    }


    @Override
    public int lastIndexOf(Object o) {
        return value.lastIndexOf(o);
    }


    @Override
    public ListIterator<T> listIterator() {
        return value.listIterator();
    }


    @Override
    public ListIterator<T> listIterator(int index) {
        return value.listIterator(index);
    }


    @Override
    public void parse(A aasValue) {
        T newEntry = parseFromAAS.apply(aasValue);
        value.add(newEntry);
        addInternal(newEntry);
    }


    @Override
    public boolean remove(Object o) {
        boolean result = value.remove(o);
        if (result) {
            removeInternal((T) o);
        }
        return result;
    }


    @Override
    public T remove(int index) {
        T result = value.remove(index);
        if (Objects.nonNull(result)) {
            removeInternal(result);
        }
        return result;
    }


    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = value.removeAll(c);
        if (result) {
            c.forEach(x -> removeInternal((T) x));
        }
        return result;
    }


    @Override
    public boolean retainAll(Collection<?> c) {
        List<T> original = new ArrayList<>(value);
        boolean result = value.retainAll(c);
        if (result) {
            original.removeAll(value);
            original.forEach(this::removeInternal);
        }
        return result;
    }


    @Override
    public T set(int index, T element) {
        T result = value.set(index, element);
        if (!Objects.equals(result, element)) {
            removeInternal(result);
            addInternal(element);
        }
        return result;
    }


    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return value.subList(fromIndex, toIndex);
    }


    @Override
    public Object[] toArray() {
        return value.toArray();
    }


    @Override
    public <U> U[] toArray(U[] a) {
        return value.toArray(a);
    }


    private void addInternal(T value) {
        A aas = convertToAAS.apply(value);
        aasElements.add(aas);
        parentValues.add(aas);
    }


    private void removeInternal(T value) {
        A aas = convertToAAS.apply(value);
        aasElements.remove(aas);
        parentValues.remove(aas);
    }


    @Override
    public List<A> toAAS() {
        return value.stream()
                .map(convertToAAS::apply)
                .collect(Collectors.toList());
    }


    @Override
    public List<T> getValue() {
        return this;
    }


    @Override
    public void setValue(List<T> newValue) {
        parentValues.removeAll(aasElements);
        value = newValue;
        aasElements.clear();
        if (newValue != null) {
            aasElements = newValue.stream()
                    .map(convertToAAS::apply)
                    .collect(Collectors.toList());
            parentValues.addAll(aasElements);
        }
    }


    @Override
    public int size() {
        return value.size();
    }


    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

}
