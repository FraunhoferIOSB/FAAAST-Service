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
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ValueWrapper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.Wrapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import java.util.Objects;


/**
 * Represents a linked segment according to SMT TimeSeries.
 */
public class LinkedSegment extends Segment {

    @JsonIgnore
    private final Wrapper<String, Property> endpoint = new ValueWrapper<String, Property>(
            values,
            null,
            true,
            Property.class,
            x -> new DefaultProperty.Builder()
                    .idShort(Constants.LINKED_SEGMENT_ENDPOINT_ID_SHORT)
                    .valueType(Datatype.STRING.getName())
                    .value(x)
                    .build(),
            x -> Objects.equals(Constants.LINKED_SEGMENT_ENDPOINT_ID_SHORT, x.getIdShort()),
            x -> x.getValue());

    @JsonIgnore
    private final Wrapper<String, Property> query = new ValueWrapper<String, Property>(
            values,
            null,
            true,
            Property.class,
            x -> new DefaultProperty.Builder()
                    .idShort(Constants.LINKED_SEGMENT_QUERY_ID_SHORT)
                    .valueType(Datatype.STRING.getName())
                    .value(x)
                    .build(),
            x -> Objects.equals(Constants.LINKED_SEGMENT_QUERY_ID_SHORT, x.getIdShort()),
            x -> x.getValue());

    /**
     * Creates a new instance based on a {@link io.adminshell.aas.v3.model.SubmodelElementCollection}.
     *
     * @param smc the {@link io.adminshell.aas.v3.model.SubmodelElementCollection} to parse
     * @return the parsed {@link io.adminshell.aas.v3.model.SubmodelElementCollection} as {@link LinkedSegment}, or null
     *         if input is null
     */
    public static LinkedSegment of(SubmodelElementCollection smc) {
        return Segment.of(new LinkedSegment(), smc);
    }


    public LinkedSegment() {
        this.idShort = IdentifierHelper.randomId("LinkedSegment");
        this.semanticId = ReferenceHelper.globalReference(Constants.LINKED_SEGMENT_SEMANTIC_ID);
        withAdditionalValues(endpoint, query);
    }


    public String getEndpoint() {
        return endpoint.getValue();
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
     * Sets the endpoint.
     *
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint.setValue(endpoint);
    }


    public String getQuery() {
        return query.getValue();
    }


    /**
     * Sets the quey.
     *
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query.setValue(query);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends LinkedSegment, B extends AbstractBuilder<T, B>> extends Segment.AbstractBuilder<T, B> {

        public B endpoint(String value) {
            getBuildingInstance().setEndpoint(value);
            return getSelf();
        }


        public B query(String value) {
            getBuildingInstance().setQuery(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<LinkedSegment, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected LinkedSegment newBuildingInstance() {
            return new LinkedSegment();
        }

    }
}
