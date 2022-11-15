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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import java.util.Collection;


/**
 * Represents a linked segment according to SMT TimeSeries.
 */
public class LinkedSegment extends Segment {

    private String endpoint;
    private String query;

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }


    public void setQuery(String query) {
        this.query = query;
    }


    @Override
    public Collection<SubmodelElement> getValues() {
        Collection<SubmodelElement> result = super.getValues();
        result.add(new DefaultProperty.Builder()
                .idShort("Endpoint")
                .valueType(Datatype.STRING.getName())
                .value(endpoint)
                .build());
        result.add(new DefaultProperty.Builder()
                .idShort("Query")
                .valueType(Datatype.STRING.getName())
                .value(query)
                .build());
        return result;
    }


    public String getEndpoint() {
        return endpoint;
    }


    public String getQuery() {
        return query;
    }
}
