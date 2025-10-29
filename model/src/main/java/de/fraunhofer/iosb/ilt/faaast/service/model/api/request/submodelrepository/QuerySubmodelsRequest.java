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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractRequestWithModifierAndPaging;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.QuerySubmodelsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Query;
import java.util.Objects;


/**
 * Request class for QuerySubmodels requests.
 */
public class QuerySubmodelsRequest extends AbstractRequestWithModifierAndPaging<QuerySubmodelsResponse> {

    private Query query;

    public Query getQuery() {
        return query;
    }


    public void setQuery(Query query) {
        this.query = query;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QuerySubmodelsRequest that = (QuerySubmodelsRequest) o;
        return super.equals(that)
                && Objects.equals(query, that.query);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), query);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends QuerySubmodelsRequest, B extends AbstractBuilder<T, B>> extends Request.AbstractBuilder<T, B> {

        public B query(Query value) {
            getBuildingInstance().setQuery(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<QuerySubmodelsRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected QuerySubmodelsRequest newBuildingInstance() {
            return new QuerySubmodelsRequest();
        }
    }
}
