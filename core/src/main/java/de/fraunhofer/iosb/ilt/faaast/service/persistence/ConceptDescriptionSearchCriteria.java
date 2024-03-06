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
 * Search criteria for finding {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription}.
 */
public class ConceptDescriptionSearchCriteria {

    public static final ConceptDescriptionSearchCriteria NONE = new ConceptDescriptionSearchCriteria();
    private static final String DEFAULT_ID_SHORT = null;
    private static final Reference DEFAULT_IS_CASE_OF = null;
    private static final Reference DEFAULT_DATA_SPECIFICATION = null;

    private String idShort;
    private Reference isCaseOf;
    private Reference dataSpecification;

    public ConceptDescriptionSearchCriteria() {
        this.idShort = DEFAULT_ID_SHORT;
        this.isCaseOf = DEFAULT_IS_CASE_OF;
        this.dataSpecification = DEFAULT_DATA_SPECIFICATION;
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


    public Reference getIsCaseOf() {
        return isCaseOf;
    }


    public void setIsCaseOf(Reference isCaseOf) {
        this.isCaseOf = isCaseOf;
    }


    public boolean isIsCaseOfSet() {
        return !Objects.equals(isCaseOf, DEFAULT_IS_CASE_OF);
    }


    public Reference getDataSpecification() {
        return dataSpecification;
    }


    public void setDataSpecification(Reference dataSpecification) {
        this.dataSpecification = dataSpecification;
    }


    public boolean isDataSpecificationSet() {
        return !Objects.equals(dataSpecification, DEFAULT_DATA_SPECIFICATION);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConceptDescriptionSearchCriteria other = (ConceptDescriptionSearchCriteria) o;
        return Objects.equals(idShort, other.idShort)
                && Objects.equals(isCaseOf, other.isCaseOf)
                && Objects.equals(dataSpecification, other.dataSpecification);
    }


    @Override
    public int hashCode() {
        return Objects.hash(idShort, isCaseOf, dataSpecification);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends ConceptDescriptionSearchCriteria, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B idShort(String value) {
            getBuildingInstance().setIdShort(value);
            return getSelf();
        }


        public B isCaseOf(Reference value) {
            getBuildingInstance().setIsCaseOf(value);
            return getSelf();
        }


        public B dataSpecification(Reference value) {
            getBuildingInstance().setDataSpecification(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<ConceptDescriptionSearchCriteria, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected ConceptDescriptionSearchCriteria newBuildingInstance() {
            return new ConceptDescriptionSearchCriteria();
        }
    }
}
