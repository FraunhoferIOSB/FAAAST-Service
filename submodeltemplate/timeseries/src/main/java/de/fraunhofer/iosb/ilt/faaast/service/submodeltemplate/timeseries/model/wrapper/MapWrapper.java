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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Wraps a {@link Map} instance to an AAS {@link SubmodelElement}.
 *
 * @param <K> key type of the map
 * @param <V> value type of the map
 * @param <A> type of the AAS element
 */
public class MapWrapper<K, V, A extends SubmodelElement> implements Map<K, V>, Wrapper<Map<K, V>, A> {

    private Map<K, V> value;
    private List<A> aasElements;
    private final Class<A> aasType;
    private final Collection<SubmodelElement> parentValues;
    private final Function<Map.Entry<K, V>, A> convertToAAS;
    private final Predicate<A> canParseFromAAS;
    private final Function<A, Map.Entry<K, V>> parseFromAAS;

    public MapWrapper(
            Collection<SubmodelElement> parentValues,
            Map<K, V> initialValue,
            Class<A> aasType,
            Function<Map.Entry<K, V>, A> convertToAAS,
            Predicate<A> canParseFromAAS,
            Function<A, Map.Entry<K, V>> parseFromAAS) {
        this.parentValues = parentValues;
        this.aasType = aasType;
        this.convertToAAS = convertToAAS;
        this.canParseFromAAS = canParseFromAAS;
        this.parseFromAAS = parseFromAAS;
        this.aasElements = new ArrayList<>();
        setValue(initialValue);
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
            MapWrapper other = (MapWrapper) obj;
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
    public void parse(A aasValue) {
        Entry<K, V> newEntry = parseFromAAS.apply(aasValue);
        value.put(newEntry.getKey(), newEntry.getValue());
        addInternal(newEntry.getKey(), newEntry.getValue());
    }


    private void addInternal(K key, V value) {
        A aas = convertToAAS.apply(new AbstractMap.SimpleEntry<>(key, value));
        aasElements.add(aas);
        parentValues.add(aas);
    }


    private void removeInternal(K key, V value) {
        A aas = convertToAAS.apply(new AbstractMap.SimpleEntry<>(key, value));
        aasElements.remove(aas);
        parentValues.remove(aas);
    }


    @Override
    public V put(K newKey, V newValue) {
        V oldValue = value.put(newKey, newValue);
        if (Objects.nonNull(oldValue)) {
            if (Objects.equals(oldValue, newValue)) {
                return newValue;
            }
            removeInternal(newKey, oldValue);
        }
        addInternal(newKey, newValue);
        return newValue;
    }


    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m != null) {
            m.forEach((k, v) -> put(k, v));
        }
    }


    @Override
    public V remove(Object o) {
        V result = value.remove(o);
        if (Objects.nonNull(result)) {
            removeInternal((K) o, result);
        }
        return result;
    }


    @Override
    public List<A> toAAS() {
        return value.entrySet().stream()
                .map(x -> convertToAAS.apply(x))
                .collect(Collectors.toList());
    }


    @Override
    public Map<K, V> getValue() {
        return this;
    }


    @Override
    public void setValue(Map<K, V> newValue) {
        parentValues.removeAll(aasElements);
        value = newValue;
        aasElements.clear();
        if (newValue != null) {
            aasElements = newValue.entrySet().stream()
                    .map(x -> convertToAAS.apply(x))
                    .collect(Collectors.toList());
            parentValues.addAll(aasElements);
        }
    }


    @Override
    public int size() {
        return value.size();
    }


    @Override
    public Collection<V> values() {
        return value.values();
    }


    @Override
    public boolean containsKey(Object o) {
        return value.containsKey(o);
    }


    @Override
    public boolean containsValue(Object o) {
        return value.containsValue(o);
    }


    @Override
    public Set<Entry<K, V>> entrySet() {
        return value.entrySet();
    }


    @Override
    public V get(Object o) {
        return value.get(o);
    }


    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }


    @Override
    public Set<K> keySet() {
        return value.keySet();
    }
}
