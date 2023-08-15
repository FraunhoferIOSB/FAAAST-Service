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
package de.fraunhofer.iosb.ilt.faaast.service.persistence;

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Search criteria for finding {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel}.
 */
public class SubmodelSearchCriteria {

    public static final SubmodelSearchCriteria NONE = new SubmodelSearchCriteria();
    private static final String DEFAULT_ID_SHORT = null;
    private static final Reference DEFAULT_SEMANTIC_ID = null;

    private String idShort;
    private Reference semanticId;

    public SubmodelSearchCriteria() {
        this.idShort = DEFAULT_ID_SHORT;
        this.semanticId = DEFAULT_SEMANTIC_ID;
    }


    public String getIdShort() {
        return idShort;
    }


    public void setIdShort(String idShort) {
        this.idShort = idShort;
    }


    public boolean isIdShortSet() {
        return !Objects.equals(idShort, DEFAULT_ID_SHORT);
    }


    public Reference getSemanticId() {
        return semanticId;
    }


    public void setSemanticId(Reference semanticId) {
        this.semanticId = semanticId;
    }


    public boolean isSemanticIdSet() {
        return !Objects.equals(semanticId, DEFAULT_SEMANTIC_ID);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubmodelSearchCriteria other = (SubmodelSearchCriteria) o;
        return Objects.equals(idShort, other.idShort)
                && Objects.equals(semanticId, other.semanticId);
    }


    @Override
    public int hashCode() {
        return Objects.hash(idShort, semanticId);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends SubmodelSearchCriteria, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B idShort(String value) {
            getBuildingInstance().setIdShort(value);
            return getSelf();
        }


        public B semanticId(Reference value) {
            getBuildingInstance().setSemanticId(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<SubmodelSearchCriteria, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected SubmodelSearchCriteria newBuildingInstance() {
            return new SubmodelSearchCriteria();
        }
    }
}
