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
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import java.util.Objects;


/**
 * Registry Descriptor for default implementation Submodel.
 */
public class DefaultSubmodelDescriptor extends AbstractIdentifiableDescriptor implements SubmodelDescriptor {

    private Reference semanticId;

    public DefaultSubmodelDescriptor() {
        semanticId = null;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultSubmodelDescriptor that = (DefaultSubmodelDescriptor) o;
        return super.equals(that)
                && Objects.equals(semanticId, that.semanticId);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), semanticId);
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


        public B from(SubmodelDescriptor other) {
            if (Objects.nonNull(other)) {
                idShort(other.getIdShort());
                endpoints(other.getEndpoints());
                administration(other.getAdministration());
                descriptions(other.getDescriptions());
                displayNames(other.getDisplayNames());
                identification(other.getIdentification());
                semanticId(other.getSemanticId());
            }
            return getSelf();
        }


        public B from(Submodel parent) {
            if (parent != null) {
                idShort(parent.getIdShort());
                identification(parent.getIdentification());
                administration(parent.getAdministration());
                descriptions(parent.getDescriptions());
                displayNames(parent.getDisplayNames());
                semanticId(parent.getSemanticId());
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
