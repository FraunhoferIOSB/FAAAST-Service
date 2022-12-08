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
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ExtendableSubmodelElementCollection;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.MapWrapper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ValueWrapper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.Wrapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.builder.SubmodelElementCollectionBuilder;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Represents metadata according to SMT TimeSeries.
 */
public class Metadata extends ExtendableSubmodelElementCollection {

    @JsonIgnore
    private ValueWrapper<RecordMetadata, SubmodelElementCollection> recordMetadata = new ValueWrapper<>(
            values,
            new RecordMetadata(),
            false,
            SubmodelElementCollection.class,
            x -> new DefaultSubmodelElementCollection.Builder()
                    .idShort(Constants.METADATA_RECORD_METADATA_ID_SHORT)
                    .values(x.getValues())
                    .build(),
            x -> Objects.equals(Constants.METADATA_RECORD_METADATA_ID_SHORT, x.getIdShort()),
            x -> ExtendableSubmodelElementCollection.genericOf(new RecordMetadata(), x));

    public Metadata() {
        withAdditionalValues(recordMetadata);
        this.idShort = Constants.TIMESERIES_METADATA_ID_SHORT;
        this.semanticId = ReferenceHelper.globalReference(Constants.METADATA_SEMANTIC_ID);
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
        return super.equals(obj);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }


    /**
     * Creates a new instance based on a {@link io.adminshell.aas.v3.model.SubmodelElementCollection}.
     *
     * @param smc the {@link io.adminshell.aas.v3.model.SubmodelElementCollection} to parse
     * @return the parsed {@link io.adminshell.aas.v3.model.SubmodelElementCollection} as {@link Metadata}, or null if
     *         input is null
     */
    public static Metadata of(SubmodelElementCollection smc) {
        return ExtendableSubmodelElementCollection.genericOf(new Metadata(), smc);
    }


    public Map<String, Datatype> getRecordMetadata() {
        return this.recordMetadata.getValue().variables.getValue();
    }


    /**
     * Sets the record metadata.
     *
     * @param recordMetadata the record metadata
     */
    public void setRecordMetadata(Map<String, Datatype> recordMetadata) {
        this.recordMetadata.getValue().variables.setValue(recordMetadata);
    }

    private static class RecordMetadata extends ExtendableSubmodelElementCollection {

        @JsonIgnore
        Wrapper<Map<String, Datatype>, Property> variables = new MapWrapper<>(
                values,
                new HashMap<>(),
                Property.class,
                x -> new DefaultProperty.Builder()
                        .idShort(x.getKey())
                        .valueType(x.getValue().getName())
                        .build(),
                x -> !Objects.equals(ReferenceHelper.globalReference(Constants.TIME_UTC), x.getSemanticId()),
                x -> new AbstractMap.SimpleEntry<>(
                        x.getIdShort(),
                        Datatype.fromName(x.getValueType())));

        RecordMetadata() {
            withAdditionalValues(variables);
            this.idShort = Constants.METADATA_RECORD_METADATA_ID_SHORT;
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
            return super.equals(obj);
        }


        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends Metadata, B extends AbstractBuilder<T, B>> extends SubmodelElementCollectionBuilder<T, B> {

        public B recordMetadata(Map<String, Datatype> value) {
            getBuildingInstance().setRecordMetadata(value);
            return getSelf();
        }


        public B recordMetadata(String name, Datatype value) {
            getBuildingInstance().getRecordMetadata().put(name, value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<Metadata, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected Metadata newBuildingInstance() {
            return new Metadata();
        }
    }
}
