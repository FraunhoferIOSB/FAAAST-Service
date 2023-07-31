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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ExtendableSubmodelElementCollection;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.MapWrapper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.Wrapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.builder.SubmodelElementCollectionBuilder;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


/**
 * Represents a record according to SMT TimeSeries.
 */
public class Record extends ExtendableSubmodelElementCollection {

    @JsonIgnore
    private Wrapper<Map<String, ZonedDateTime>, Property> time = new MapWrapper<>(
            values,
            new LinkedHashMap<>(),
            Property.class,
            x -> new DefaultProperty.Builder()
                    .idShort(x.getKey())
                    .valueType(Datatype.DATE_TIME.getName())
                    .value(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(x.getValue()))
                    .semanticId(ReferenceHelper.globalReference(Constants.TIME_UTC))
                    .build(),
            x -> Objects.equals(ReferenceHelper.globalReference(Constants.TIME_UTC), x.getSemanticId()),
            x -> new AbstractMap.SimpleEntry<>(x.getIdShort(), ZonedDateTime.parse(x.getValue(), DateTimeFormatter.ISO_ZONED_DATE_TIME)));

    @JsonIgnore
    private Wrapper<Map<String, TypedValue>, Property> variables = new MapWrapper<>(
            values,
            new HashMap<>(),
            Property.class,
            x -> new DefaultProperty.Builder()
                    .idShort(x.getKey())
                    .valueType(x.getValue().getDataType().getName())
                    .value(x.getValue().asString())
                    .build(),
            x -> !Objects.equals(ReferenceHelper.globalReference(Constants.TIME_UTC), x.getSemanticId()),
            x -> {
                try {
                    return new AbstractMap.SimpleEntry<>(x.getIdShort(), TypedValueFactory.create(x.getValueType(), x.getValue()));
                }
                catch (ValueFormatException e) {
                    throw new IllegalArgumentException(e);
                }
            });

    public Record() {
        withAdditionalValues(time, variables);
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
                    && Objects.equals(this.time, other.time)
                    && Objects.equals(this.variables, other.variables);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), time, variables);
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


    public Map<String, ZonedDateTime> getTime() {
        return time.getValue();
    }


    /**
     * Get a single timestamp from the timestamps of the record.
     *
     * @return timestamp of the record or null, if no time is set
     */
    public ZonedDateTime getSingleTime() {
        Optional<String> timeName = this.time.getValue().keySet().stream().findFirst();
        if (timeName.isPresent()) {
            return this.time.getValue().get(timeName.get());
        }
        else {
            return null;
        }

    }


    /**
     * Sets the time.
     *
     * @param time the time to set
     */
    public void setTime(Map<String, ZonedDateTime> time) {
        this.time.setValue(time);
    }


    public Map<String, TypedValue> getVariables() {
        return variables.getValue();
    }


    /**
     * Sets the variables.
     *
     * @param variables the variables to set
     */
    public void setVariables(Map<String, TypedValue> variables) {
        this.variables.setValue(variables);
    }


    /**
     * Add to the variables.
     *
     * @param variableName name of the variable
     * @param value {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue} of the variable
     */
    public void addVariables(String variableName, TypedValue value) {
        this.variables.getValue().put(variableName, value);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends Record, B extends AbstractBuilder<T, B>> extends SubmodelElementCollectionBuilder<T, B> {

        public B time(Map<String, ZonedDateTime> value) {
            getBuildingInstance().setTime(value);
            return getSelf();
        }


        public B time(String key, ZonedDateTime value) {
            getBuildingInstance().getTime().put(key, value);
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
