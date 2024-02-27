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

import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Search criteria for finding {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}.
 */
public class SubmodelElementSearchCriteria {

    public static final SubmodelElementSearchCriteria NONE = new SubmodelElementSearchCriteria();
    private static final SubmodelElementIdentifier DEFAULT_PARENT = null;
    private static final Reference DEFAULT_SEMANTIC_ID = null;
    private static final boolean DEFAULT_VALUE_ONLY = false;

    private SubmodelElementIdentifier parent;
    private Reference semanticId;
    private boolean valueOnly;

    public SubmodelElementSearchCriteria() {
        this.parent = DEFAULT_PARENT;
        this.semanticId = DEFAULT_SEMANTIC_ID;
        this.valueOnly = DEFAULT_VALUE_ONLY;
    }


    public SubmodelElementIdentifier getParent() {
        return parent;
    }


    public void setParent(SubmodelElementIdentifier parent) {
        this.parent = parent;
    }


    public boolean isParentSet() {
        return !Objects.equals(parent, DEFAULT_PARENT);
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


    public boolean getValueOnly() {
        return valueOnly;
    }


    public void setValueOnly(boolean valueOnly) {
        this.valueOnly = valueOnly;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubmodelElementSearchCriteria other = (SubmodelElementSearchCriteria) o;
        return Objects.equals(parent, other.parent)
                && Objects.equals(semanticId, other.semanticId)
                && Objects.equals(valueOnly, other.valueOnly);
    }


    @Override
    public int hashCode() {
        return Objects.hash(parent, semanticId, valueOnly);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends SubmodelElementSearchCriteria, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B parent(SubmodelElementIdentifier value) {
            getBuildingInstance().setParent(value);
            return getSelf();
        }


        public B semanticId(Reference value) {
            getBuildingInstance().setSemanticId(value);
            return getSelf();
        }


        public B valueOnly(boolean value) {
            getBuildingInstance().setValueOnly(value);
            return getSelf();
        }


        public B valueOnly() {
            getBuildingInstance().setValueOnly(true);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<SubmodelElementSearchCriteria, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected SubmodelElementSearchCriteria newBuildingInstance() {
            return new SubmodelElementSearchCriteria();
        }
    }
}
