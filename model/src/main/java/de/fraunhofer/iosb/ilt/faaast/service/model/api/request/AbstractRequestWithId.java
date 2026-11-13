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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.DistinctResourceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.AccessObject;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.referable.REFERABLE_TYPES;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule.Right;

import java.util.Objects;


/**
 * Abstract baseclass for request with an Id.
 *
 * @param <T> type of the corresponding response
 */
public class AbstractRequestWithId<T extends Response> extends DistinctResourceRequest<T> {

    protected String id;

    protected AbstractRequestWithId(Right right, REFERABLE_TYPES type) {
        super(right, type);
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    @Override
    protected boolean checkType(AccessObject object) {
        return super.checkType(object)
                && ((object.isIdentifiable() && (object.asIdentifiable().getIdentifier().equals(id) || object.asIdentifiable().isWildcard())
                        || (object.isReferable() && (object.asReferable().getIdentifier().equals(id) || object.asReferable().isWildcard()))));
    }


    @Override
    protected boolean requestsCollection() {
        return false;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractRequestWithId<T> that = (AbstractRequestWithId<T>) o;
        return super.equals(that)
                && Objects.equals(id, that.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }

    public abstract static class AbstractBuilder<T extends AbstractRequestWithId, B extends AbstractBuilder<T, B>> extends Request.AbstractBuilder<T, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }
    }
}
