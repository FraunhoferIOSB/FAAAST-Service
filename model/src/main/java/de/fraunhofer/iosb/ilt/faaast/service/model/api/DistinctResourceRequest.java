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
package de.fraunhofer.iosb.ilt.faaast.service.model.api;

import static de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule.Right.ALL;

import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.AccessObject;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.referable.REFERABLE_TYPES;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule.Right;
import java.util.List;
import java.util.Objects;


/**
 * A request pertaining to a single resource type.
 *
 * @param <T> type of the corresponding response
 */
public abstract class DistinctResourceRequest<T extends Response> extends Request<T> {

    private final Right right;
    private final REFERABLE_TYPES type;

    protected DistinctResourceRequest(Right right, REFERABLE_TYPES type) {
        this.right = right;
        this.type = type;
    }


    public List<AccessPermissionRule> getFilteredRules() {
        // need TODO route checking here since all access objects are a big OR. If we check ROUTE in HTTP filters, we cannot know which rules are here because they passed ROUTE
        //          vs. they are here because they could not be removed yet. --> Call getFilteredRules() in the mapper, where HTTP request path is available?

        // TODO also, $sme don't have wildcards in the idShortPath part. This means, for e.g., GetAllSubmodelElements a specific submodel ID or submodel Wildcard must be present.
        return getRules().stream()
                .filter(rule -> rule.rule().rights().stream().anyMatch(right -> getRight() == right || ALL == right))
                .filter(rule -> rule.objects().stream().anyMatch(this::checkType))
                .filter(rule -> rule.objects().stream()
                        .anyMatch(object -> !requestsCollection() || checkType(object) && (object.isIdentifiable() && object.asIdentifiable().isWildcard()
                                || object.isReferable() && object.asReferable().isWildcard())))
                .toList();
    }


    /**
     * Check if the type of this access object matches the type of this request.
     * 
     * @param accessObject The access object containing the type.
     * @return True if the types match, either for an IdentifiableObject or a ReferableObject.
     */
    protected boolean checkType(AccessObject accessObject) {
        return accessObject.isIdentifiable() && this.getType() == accessObject.asIdentifiable().getType()
                || accessObject.isReferable() && this.getType() == accessObject.asReferable().getType();
    }


    /**
     * Return whether this request shall return a collection of AAS, Submodel, ConceptDescripion, SubmodelElement, or a
     * single item or a subset of items of them.
     * 
     * @return True if this request returns a collection of AAS, Submodel, ConceptDescripion, SubmodelElement, or a single
     *         item or a subset of items of them.
     */
    protected abstract boolean requestsCollection();


    public Right getRight() {
        return right;
    }


    public REFERABLE_TYPES getType() {
        return type;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DistinctResourceRequest<?> that = (DistinctResourceRequest<?>) o;
        return super.equals(that) &&
                Objects.equals(right, that.right) &&
                Objects.equals(type, that.type);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), right, type);
    }
}
