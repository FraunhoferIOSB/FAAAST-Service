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

import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Registry Descriptor default implementation for AssetAdministrationShell.
 */
public class DefaultAssetAdministrationShellDescriptor extends AbstractIdentifiableDescriptor implements AssetAdministrationShellDescriptor {

    private Reference globalAssetId;
    private List<IdentifierKeyValuePair> specificAssetIds;
    private List<SubmodelDescriptor> submodels;

    public DefaultAssetAdministrationShellDescriptor() {
        globalAssetId = null;
        specificAssetIds = new ArrayList<>();
        submodels = new ArrayList<>();
    }


    @Override
    public Reference getGlobalAssetId() {
        return globalAssetId;
    }


    @Override
    public void setGlobalAssetId(Reference globalAssetId) {
        this.globalAssetId = globalAssetId;
    }


    @Override
    public List<IdentifierKeyValuePair> getSpecificAssetIds() {
        return specificAssetIds;
    }


    @Override
    public void setSpecificAssetIds(List<IdentifierKeyValuePair> specificAssetIds) {
        this.specificAssetIds = specificAssetIds;
    }


    @Override
    public List<SubmodelDescriptor> getSubmodels() {
        return submodels;
    }


    @Override
    public void setSubmodels(List<SubmodelDescriptor> submodels) {
        this.submodels = submodels;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultAssetAdministrationShellDescriptor that = (DefaultAssetAdministrationShellDescriptor) o;
        return super.equals(that)
                && Objects.equals(globalAssetId, that.globalAssetId)
                && Objects.equals(specificAssetIds, that.specificAssetIds)
                && Objects.equals(submodels, that.submodels);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), globalAssetId, specificAssetIds, submodels);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends DefaultAssetAdministrationShellDescriptor, B extends AbstractBuilder<T, B>>
            extends AbstractIdentifiableDescriptor.AbstractBuilder<T, B> {

        public B globalAssetId(Reference value) {
            getBuildingInstance().setGlobalAssetId(value);
            return getSelf();
        }


        public B specificAssetIds(List<IdentifierKeyValuePair> value) {
            getBuildingInstance().setSpecificAssetIds(value);
            return getSelf();
        }


        public B specificAssetId(IdentifierKeyValuePair value) {
            getBuildingInstance().getSpecificAssetIds().add(value);
            return getSelf();
        }


        public B submodels(List<SubmodelDescriptor> value) {
            getBuildingInstance().setSubmodels(value);
            return getSelf();
        }


        public B submodel(SubmodelDescriptor value) {
            getBuildingInstance().getSubmodels().add(value);
            return getSelf();
        }


        public B from(AssetAdministrationShell parent) {
            if (parent != null) {
                idShort(parent.getIdShort());
                administration(parent.getAdministration());
                descriptions(parent.getDescriptions());
                identification(parent.getIdentification());
                if (parent.getAssetInformation() != null) {
                    globalAssetId(parent.getAssetInformation().getGlobalAssetId());
                    specificAssetIds(parent.getAssetInformation().getSpecificAssetIds());
                }
            }
            return getSelf();
        }


        public B from(AssetAdministrationShell aas, List<Submodel> submodels) {
            if (Objects.isNull(aas)) {
                return getSelf();
            }
            from(aas);
            if (submodels != null) {
                List<Submodel> submodelsNotPresentInAAS = submodels.stream()
                        .filter(x -> !aas.getSubmodels().contains(ReferenceHelper.toReference(x.getIdentification(), Submodel.class)))
                        .collect(Collectors.toList());
                if (!submodelsNotPresentInAAS.isEmpty()) {
                    throw new IllegalArgumentException(String.format("Submodel(s) not found in AAS (id: %s) ",
                            submodelsNotPresentInAAS.stream()
                                    .map(x -> x.getIdentification().getIdentifier())
                                    .collect(Collectors.joining(", "))));
                }
                submodels(submodels.stream()
                        .map(x -> DefaultSubmodelDescriptor.builder()
                                .from(x)
                                .build())
                        .collect(Collectors.toList()));
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<DefaultAssetAdministrationShellDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected DefaultAssetAdministrationShellDescriptor newBuildingInstance() {
            return new DefaultAssetAdministrationShellDescriptor();
        }
    }
}
