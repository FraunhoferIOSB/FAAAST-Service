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
import de.fraunhofer.iosb.ilt.faaast.service.util.AasHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import java.util.Collection;


/**
 * Represents a linked segment according to SMT TimeSeries.
 */
public class LinkedSegment extends Segment {

    @JsonIgnore
    private String endpoint;
    @JsonIgnore
    private String query;

    /**
     * Creates a new instance based on a {@link io.adminshell.aas.v3.model.SubmodelElementCollection}.
     *
     * @param smc the {@link io.adminshell.aas.v3.model.SubmodelElementCollection} to parse
     * @return the parsed {@link io.adminshell.aas.v3.model.SubmodelElementCollection} as {@link LinkedSegment}, or null
     *         if input is null
     */
    public static LinkedSegment of(SubmodelElementCollection smc) {
        if (smc == null) {
            return null;
        }
        LinkedSegment result = new LinkedSegment();
        Segment.of(result, smc);
        Property endpoint = AasHelper.getElementByIdShort(result.values, Constants.LINKED_SEGMENT_ENDPOINT_ID_SHORT, Property.class);
        if (endpoint != null) {
            result.endpoint = endpoint.getValue();
            result.values.remove(endpoint);
        }
        Property query = AasHelper.getElementByIdShort(result.values, Constants.LINKED_SEGMENT_QUERY_ID_SHORT, Property.class);
        if (query != null) {
            result.query = query.getValue();
            result.values.remove(query);
        }
        return result;
    }


    public LinkedSegment() {
        super();
        this.idShort = IdentifierHelper.randomId("LinkedSegment");
        this.semanticId = ReferenceHelper.globalReference(Constants.LINKED_SEGMENT_SEMANTIC_ID);
    }


    public String getEndpoint() {
        return endpoint;
    }


    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }


    public String getQuery() {
        return query;
    }


    public void setQuery(String query) {
        this.query = query;
    }


    @Override
    public Collection<SubmodelElement> getValues() {
        Collection<SubmodelElement> result = super.getValues();
        result.add(new DefaultProperty.Builder()
                .idShort(Constants.LINKED_SEGMENT_ENDPOINT_ID_SHORT)
                .valueType(Datatype.STRING.getName())
                .value(endpoint)
                .build());
        result.add(new DefaultProperty.Builder()
                .idShort(Constants.LINKED_SEGMENT_QUERY_ID_SHORT)
                .valueType(Datatype.STRING.getName())
                .value(query)
                .build());
        return result;
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
