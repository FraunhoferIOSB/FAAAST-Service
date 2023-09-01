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
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ExtendableSubmodelElementCollection;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.MapWrapper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.Wrapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.builder.SubmodelElementCollectionBuilder;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Represents a record according to SMT TimeSeries.
 */
public class Record extends ExtendableSubmodelElementCollection {

    @JsonIgnore
    private Wrapper<Map<String, Property>, Property> variablesAndTimes = new MapWrapper<>(
            values,
            new HashMap<>(),
            Property.class,
            x -> x.getValue(),
            x -> x instanceof Property,
            x -> new AbstractMap.SimpleEntry<>(x.getIdShort(), x));

    //    @JsonIgnore
    //    private Wrapper<Map<String, Time>, Property> times = new MapWrapper<>(
    //            values,
    //            new LinkedHashMap<>(),
    //            Property.class,
    //            x -> new DefaultProperty.Builder()
    //                    .idShort(x.getKey())
    //                    .valueType(x.getValue().getDataValueType())
    //                    .value(x.getValue().getTimestampString())
    //                    .semanticId(ReferenceHelper.globalReference(TimeFactory.getSemanticIDForClass(x.getValue().getClass())))
    //                    .build(),
    //            x -> TimeFactory.isParseable(x.getSemanticId(), x.getValue()),
    //            x -> new AbstractMap.SimpleEntry<>(x.getIdShort(), TimeFactory.getTimeTypeFrom(x.getSemanticId(), x.getValue()).get()));
    //
    //    @JsonIgnore
    //    private Wrapper<Map<String, TypedValue>, Property> variablesAndTimes = new MapWrapper<>(
    //            values,
    //            new HashMap<>(),
    //            Property.class,
    //            x -> new DefaultProperty.Builder()
    //                    .idShort(x.getKey())
    //                    .valueType(x.getValue().getDataType().getName())
    //                    .value(x.getValue().asString())
    //                    .build(),
    //            x -> !TimeFactory.isParseable(x.getSemanticId(), x.getValue()),
    //            x -> {
    //                try {
    //                    return new AbstractMap.SimpleEntry<>(x.getIdShort(), TypedValueFactory.create(x.getValueType(), x.getValue()));
    //                }
    //                catch (ValueFormatException e) {
    //                    throw new IllegalArgumentException(e);
    //                }
    //            });

    @JsonIgnore
    private Wrapper<Map<String, Property>, Property> propertyVariables = new MapWrapper<>(
            values,
            new HashMap<>(),
            Property.class,
            x -> x.getValue(),
            x -> x instanceof Property,
            x -> new AbstractMap.SimpleEntry<>(x.getIdShort(), x));

    //    @JsonIgnore
    //    private Wrapper<Map<String, ? extends DataElement>, ? extends DataElement> dataelementVariables = new MapWrapper<>(
    //            values,
    //            new HashMap<>(),
    //            DataElement.class,
    //            x -> ((DataElement) x.getValue()),
    //            x -> (x instanceof DataElement) && !(x instanceof Property),
    //            x -> new AbstractMap.SimpleEntry<>(x.getIdShort(), ((DataElement)x)));

    public Record() {
        withAdditionalValues(variablesAndTimes);
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
                    && Objects.equals(this.variablesAndTimes, other.variablesAndTimes);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), variablesAndTimes);
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


    public Map<String, Property> getTimesAndVariables() {
        return variablesAndTimes.getValue();
    }


    /**
     * Sets the variables.
     *
     * @param variables the variables to set
     */
    public void setTimesAndVariables(Map<String, Property> variables) {
        this.variablesAndTimes.setValue(variables);
    }


    /**
     * Add to the variables.
     *
     * @param variableName name of the variable
     * @param value {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue} of the variable
     */
    public void addVariables(String variableName, Property value) {
        this.variablesAndTimes.getValue().put(variableName, value);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends Record, B extends AbstractBuilder<T, B>> extends SubmodelElementCollectionBuilder<T, B> {

        public B timesAndVariables(Map<String, Property> value) {
            getBuildingInstance().setTimesAndVariables(value);
            return getSelf();
        }


        public B timeOrVariable(String key, Property value) {
            getBuildingInstance().getTimesAndVariables().put(key, value);
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
