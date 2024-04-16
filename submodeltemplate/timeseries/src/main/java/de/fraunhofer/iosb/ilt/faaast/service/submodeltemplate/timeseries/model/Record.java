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
import de.fraunhofer.iosb.ilt.faaast.service.util.IdHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.SubmodelElementCollectionBuilder;


/**
 * Represents a record according to SMT TimeSeries.
 */
public class Record extends ExtendableSubmodelElementCollection {

    @JsonIgnore
    private Wrapper<Map<String, Property>, Property> timesAndVariables = new MapWrapper<>(
            value,
            new HashMap<>(),
            Property.class,
            x -> x.getValue(),
            x -> x instanceof Property,
            x -> new AbstractMap.SimpleEntry<>(x.getIdShort(), x));

    @JsonIgnore
    private Wrapper<Map<String, Property>, Property> propertyVariables = new MapWrapper<>(
            value,
            new HashMap<>(),
            Property.class,
            x -> x.getValue(),
            x -> x instanceof Property,
            x -> new AbstractMap.SimpleEntry<>(x.getIdShort(), x));

    public Record() {
        withAdditionalValues(timesAndVariables);
        this.idShort = IdHelper.randomId("Record");
        this.semanticId = ReferenceBuilder.global(Constants.RECORD_SEMANTIC_ID);
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
                    && Objects.equals(this.timesAndVariables, other.timesAndVariables);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), timesAndVariables);
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
        return timesAndVariables.getValue();
    }


    /**
     * Sets the variables.
     *
     * @param variables the variables to set
     */
    public void setTimesAndVariables(Map<String, Property> variables) {
        this.timesAndVariables.setValue(variables);
    }


    /**
     * Add to the variables.
     *
     * @param variableName name of the variable
     * @param value {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue} of the variable
     */
    public void addVariables(String variableName, Property value) {
        this.timesAndVariables.getValue().put(variableName, value);
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
