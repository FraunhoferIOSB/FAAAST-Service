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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.AbsoluteTime;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.RelativeTime;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.Time;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.TimeFactory;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ExtendableSubmodelElementCollection;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.MapWrapper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.Wrapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.builder.SubmodelElementCollectionBuilder;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;


/**
 * Represents a record according to SMT TimeSeries.
 */
public class Record extends ExtendableSubmodelElementCollection {

    @JsonIgnore
    private Wrapper<Map<String, Time>, Property> times = new MapWrapper<>(
            values,
            new LinkedHashMap<>(),
            Property.class,
            x -> new DefaultProperty.Builder()
                    .idShort(x.getKey())
                    .valueType(x.getValue().getDataValueType())
                    .value(x.getValue().getTimestampString())
                    .semanticId(ReferenceHelper.globalReference(TimeFactory.getSemanticIDForClass(x.getValue().getClass())))
                    .build(),
            x -> TimeFactory.isParseable(x.getSemanticId(), x.getValue()),
            x -> new AbstractMap.SimpleEntry<>(x.getIdShort(), TimeFactory.getTimeTypeFrom(x.getSemanticId(), x.getValue()).get()));

    @JsonIgnore
    private Wrapper<Map<String, TypedValue>, Property> variablesAndTimes = new MapWrapper<>(
            values,
            new HashMap<>(),
            Property.class,
            x -> new DefaultProperty.Builder()
                    .idShort(x.getKey())
                    .valueType(x.getValue().getDataType().getName())
                    .value(x.getValue().asString())
                    .build(),
            x -> !TimeFactory.isParseable(x.getSemanticId(), x.getValue()),
            x -> {
                try {
                    return new AbstractMap.SimpleEntry<>(x.getIdShort(), TypedValueFactory.create(x.getValueType(), x.getValue()));
                }
                catch (ValueFormatException e) {
                    throw new IllegalArgumentException(e);
                }
            });

    public Record() {
        withAdditionalValues(times, variablesAndTimes);
        this.idShort = IdentifierHelper.randomId("Record");
        this.semanticId = ReferenceHelper.globalReference(Constants.RECORD_SEMANTIC_ID);
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
            Record other = (Record) obj;
            return super.equals(obj)
                    && Objects.equals(this.times, other.times)
                    && Objects.equals(this.variablesAndTimes, other.variablesAndTimes);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), times, variablesAndTimes);
    }


    /**
     * Creates a new instance based on a {@link io.adminshell.aas.v3.model.SubmodelElementCollection}.
     *
     * @param smc the {@link io.adminshell.aas.v3.model.SubmodelElementCollection} to parse
     * @return the parsed {@link io.adminshell.aas.v3.model.SubmodelElementCollection} as {@link Record}, or null if
     *         input is null
     */
    public static Record of(SubmodelElementCollection smc) {
        return ExtendableSubmodelElementCollection.genericOf(new Record(), smc);
    }


    public Map<String, Time> getTimes() {
        return times.getValue();
    }


    /**
     * Get a single timestamp from the timestamps of the record. Prefers supported and absolute timestamps.
     *
     * @return timestamp of the record or null, if no time is set.
     */
    @JsonIgnore
    public Time getSingleTime() {
        Optional<Entry<String, Time>> timeOpt = this.times.getValue().entrySet().stream().filter(e -> e.getValue() instanceof AbsoluteTime).findFirst();

        if (timeOpt.isEmpty()) {
            timeOpt = this.times.getValue().entrySet().stream().filter(e -> e.getValue() instanceof RelativeTime && !(((RelativeTime) e.getValue()).isIncrementalToPrevious()))
                    .findFirst();
            if (timeOpt.isEmpty()) {
                timeOpt = this.times.getValue().entrySet().stream().findFirst();
            }
        }
        return timeOpt.isPresent() ? timeOpt.get().getValue() : null;

    }


    /**
     * Get a single absolute timestamp from the timestamps of the record.
     *
     * @return timestamp of the record or null, if no absolute time is set.
     */
    @JsonIgnore
    public AbsoluteTime getAbsoluteTime() {
        Optional<Entry<String, Time>> timeOpt = this.times.getValue().entrySet().stream().filter(e -> e.getValue() instanceof AbsoluteTime).findFirst();
        return timeOpt.isPresent() ? (AbsoluteTime) timeOpt.get().getValue() : null;
    }


    /**
     * Get a single relative timestamp from the timestamps of the record.
     *
     * @return timestamp of the record or null, if no relative time is set.
     */
    @JsonIgnore
    public RelativeTime getRelativeTime() {
        Optional<Entry<String, Time>> timeOpt = this.times.getValue().entrySet().stream().filter(e -> e.getValue() instanceof RelativeTime).findFirst();
        return timeOpt.isPresent() ? (RelativeTime) timeOpt.get().getValue() : null;
    }


    /**
     * Sets the time.
     *
     * @param time the time to set
     */
    public void setTimes(Map<String, Time> time) {
        this.times.setValue(time);
    }


    public Map<String, TypedValue> getVariables() {
        return variablesAndTimes.getValue();
    }


    /**
     * Sets the variables.
     *
     * @param variables the variables to set
     */
    public void setVariables(Map<String, TypedValue> variables) {
        this.variablesAndTimes.setValue(variables);
    }


    /**
     * Add to the variables.
     *
     * @param variableName name of the variable
     * @param value {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue} of the variable
     */
    public void addVariables(String variableName, TypedValue value) {
        this.variablesAndTimes.getValue().put(variableName, value);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends Record, B extends AbstractBuilder<T, B>> extends SubmodelElementCollectionBuilder<T, B> {

        public B times(Map<String, Time> value) {
            getBuildingInstance().setTimes(value);
            return getSelf();
        }


        public B times(String key, Time value) {
            getBuildingInstance().getTimes().put(key, value);
            return getSelf();
        }


        public B variables(Map<String, TypedValue> value) {
            getBuildingInstance().setVariables(value);
            return getSelf();
        }


        public B variable(String key, TypedValue value) {
            getBuildingInstance().getVariables().put(key, value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<Record, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected Record newBuildingInstance() {
            return new Record();
        }

    }

}