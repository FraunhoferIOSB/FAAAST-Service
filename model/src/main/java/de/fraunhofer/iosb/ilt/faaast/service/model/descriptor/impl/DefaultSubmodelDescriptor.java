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
package de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl;

import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;


/**
 * Registry Descriptor for default implementation Submodel.
 */
public class DefaultSubmodelDescriptor extends AbstractIdentifiableDescriptor implements SubmodelDescriptor {

    private Reference semanticId;
    private List<Reference> supplementalSemanticIds;

    public DefaultSubmodelDescriptor() {
        semanticId = null;
        supplementalSemanticIds = new ArrayList<>();
    }


    @Override
    public Reference getSemanticId() {
        return semanticId;
    }


    @Override
    public void setSemanticId(Reference semanticId) {
        this.semanticId = semanticId;
    }


    @Override
    public List<Reference> getSupplementalSemanticIds() {
        return supplementalSemanticIds;
    }


    @Override
    public void setSupplementalSemanticIds(List<Reference> suplementalSemanticIds) {
        this.supplementalSemanticIds = suplementalSemanticIds;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultSubmodelDescriptor that = (DefaultSubmodelDescriptor) o;
        return super.equals(that)
                && Objects.equals(semanticId, that.semanticId)
                && Objects.equals(supplementalSemanticIds, that.supplementalSemanticIds);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), semanticId, supplementalSemanticIds);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends DefaultSubmodelDescriptor, B extends AbstractBuilder<T, B>>
            extends AbstractIdentifiableDescriptor.AbstractBuilder<T, B> {

        public B semanticId(Reference value) {
            getBuildingInstance().setSemanticId(value);
            return getSelf();
        }


        public B supplementalSemanticIds(List<Reference> value) {
            getBuildingInstance().setSupplementalSemanticIds(value);
            return getSelf();
        }


        public B supplementalSemanticId(Reference value) {
            getBuildingInstance().getSupplementalSemanticIds().add(value);
            return getSelf();
        }


        public B from(SubmodelDescriptor other) {
            if (Objects.nonNull(other)) {
                idShort(other.getIdShort());
                endpoints(other.getEndpoints());
                administration(other.getAdministration());
                descriptions(other.getDescriptions());
                displayNames(other.getDisplayNames());
                id(other.getId());
                semanticId(other.getSemanticId());
                supplementalSemanticIds(other.getSupplementalSemanticIds());
            }
            return getSelf();
        }


        public B from(Submodel parent) {
            if (parent != null) {
                idShort(parent.getIdShort());
                id(parent.getId());
                administration(parent.getAdministration());
                descriptions(parent.getDescription());
                displayNames(parent.getDisplayName());
                semanticId(parent.getSemanticId());
                supplementalSemanticIds(parent.getSupplementalSemanticIds());
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<DefaultSubmodelDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected DefaultSubmodelDescriptor newBuildingInstance() {
            return new DefaultSubmodelDescriptor();
        }
    }
}
