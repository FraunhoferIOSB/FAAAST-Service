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
import de.fraunhofer.iosb.ilt.faaast.service.util.AasHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.builder.SubmodelElementCollectionBuilder;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Represents a record according to SMT TimeSeries.
 */
public class Record extends DefaultSubmodelElementCollection {

    @JsonIgnore
    private ZonedDateTime time;
    @JsonIgnore
    private Map<String, TypedValue> variables;

    public Record() {
        this.variables = new HashMap<>();
        this.idShort = IdentifierHelper.randomId("Record");
        this.semanticId = ReferenceHelper.globalReference(Constants.RECORD_SEMANTIC_ID);
    }


    /**
     * Creates a new instance based on a {@link io.adminshell.aas.v3.model.SubmodelElementCollection}.
     *
     * @param smc the {@link io.adminshell.aas.v3.model.SubmodelElementCollection} to parse
     * @return the parsed {@link io.adminshell.aas.v3.model.SubmodelElementCollection} as {@link Record}, or null if
     *         input is null
     */
    public static Record of(SubmodelElementCollection smc) {
        if (smc == null) {
            return null;
        }
        Record result = new Record();
        AasHelper.applyBasicProperties(smc, result);
        for (var sme: smc.getValues()) {
            if (Property.class.isAssignableFrom(sme.getClass())) {
                Property property = (Property) sme;
                if (Objects.equals(ReferenceHelper.globalReference(Constants.TIME_UTC), sme.getSemanticId())) {
                    result.time = ZonedDateTime.parse(((Property) sme).getValue());
                }
                else {
                    try {
                        result.variables.put(
                                property.getIdShort(),
                                TypedValueFactory.create(
                                        property.getValueType(),
                                        property.getValue()));
                    }
                    catch (ValueFormatException e) {
                        // what to do?
                        throw new RuntimeException(e);
                    }
                }
            }
            else {
                result.values.add(sme);
            }
        }
        return result;
    }


    public ZonedDateTime getTime() {
        return time;
    }


    public void setTime(ZonedDateTime time) {
        this.time = time;
    }


    public Map<String, TypedValue> getVariables() {
        return variables;
    }


    public void setVariables(Map<String, TypedValue> variables) {
        this.variables = variables;
    }


    @Override
    public Collection<SubmodelElement> getValues() {
        List<SubmodelElement> result = variables.entrySet().stream()
                .map(x -> new DefaultProperty.Builder()
                        .idShort(x.getKey())
                        .valueType(x.getValue().getDataType().getName())
                        .value(x.getValue().asString())
                        .build())
                .collect(Collectors.toList());
        result.add(new DefaultProperty.Builder()
                .idShort("time")
                .semanticId(ReferenceHelper.globalReference(Constants.TIME_UTC))
                .valueType(Datatype.DATE_TIME.getName())
                .value(time.toString())
                .build());
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
            Record other = (Record) obj;
            return super.equals(other)
                    && Objects.equals(this.time, other.time)
                    && Objects.equals(this.variables, other.variables);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                this.time,
                this.variables);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends Record, B extends AbstractBuilder<T, B>> extends SubmodelElementCollectionBuilder<T, B> {

        public B time(ZonedDateTime value) {
            getBuildingInstance().setTime(value);
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
