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
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Extension;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;


/**
 * Registry Descriptor default implementation for AssetAdministrationShell.
 */
public class DefaultAssetAdministrationShellDescriptor extends AbstractIdentifiableDescriptor implements AssetAdministrationShellDescriptor {

    private String globalAssetId;
    private List<SpecificAssetId> specificAssetIds;
    private List<SubmodelDescriptor> submodels;
    private List<Extension> extensions;
    private AssetKind assetKind;
    private String assetType;

    public DefaultAssetAdministrationShellDescriptor() {
        specificAssetIds = new ArrayList<>();
        submodels = new ArrayList<>();
        extensions = new ArrayList<>();
        assetKind = AssetKind.NOT_APPLICABLE;
    }


    @Override
    public AssetKind getAssetKind() {
        return assetKind;
    }


    @Override
    public void setAssetKind(AssetKind assetKind) {
        this.assetKind = assetKind;
    }


    @Override
    public String getAssetType() {
        return assetType;
    }


    @Override
    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }


    @Override
    public List<Extension> getExtensions() {
        return extensions;
    }


    @Override
    public void setExtensions(List<Extension> extensions) {
        this.extensions = extensions;
    }


    @Override
    public String getGlobalAssetId() {
        return globalAssetId;
    }


    @Override
    public void setGlobalAssetId(String globalAssetId) {
        this.globalAssetId = globalAssetId;
    }


    @Override
    public List<SpecificAssetId> getSpecificAssetIds() {
        return specificAssetIds;
    }


    @Override
    public void setSpecificAssetIds(List<SpecificAssetId> specificAssetIds) {
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
                && Objects.equals(submodels, that.submodels)
                && Objects.equals(assetType, that.assetType)
                && Objects.equals(extensions, that.extensions)
                && assetKind == that.assetKind;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), globalAssetId, specificAssetIds, submodels, assetKind, assetType, extensions);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends DefaultAssetAdministrationShellDescriptor, B extends AbstractBuilder<T, B>>
            extends AbstractIdentifiableDescriptor.AbstractBuilder<T, B> {

        public B globalAssetId(String value) {
            getBuildingInstance().setGlobalAssetId(value);
            return getSelf();
        }


        public B specificAssetIds(List<SpecificAssetId> value) {
            getBuildingInstance().setSpecificAssetIds(value);
            return getSelf();
        }


        public B specificAssetId(SpecificAssetId value) {
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


        public B assetType(String value) {
            getBuildingInstance().setAssetType(value);
            return getSelf();
        }


        public B assetkind(AssetKind value) {
            getBuildingInstance().setAssetKind(value);
            return getSelf();
        }


        public B extension(Extension value) {
            getBuildingInstance().getExtensions().add(value);
            return getSelf();
        }


        public B extensions(List<Extension> value) {
            getBuildingInstance().setExtensions(value);
            return getSelf();
        }


        public B from(AssetAdministrationShellDescriptor other) {
            if (Objects.nonNull(other)) {
                idShort(other.getIdShort());
                endpoints(other.getEndpoints());
                administration(other.getAdministration());
                descriptions(other.getDescriptions());
                displayNames(other.getDisplayNames());
                id(other.getId());
                globalAssetId(other.getGlobalAssetId());
                specificAssetIds(other.getSpecificAssetIds());
                submodels(other.getSubmodels());
                assetType(other.getAssetType());
                assetkind(other.getAssetKind());
                extensions(other.getExtensions());
            }
            return getSelf();
        }


        public B from(AssetAdministrationShell parent) {
            if (parent != null) {
                idShort(parent.getIdShort());
                administration(parent.getAdministration());
                descriptions(parent.getDescription());
                displayNames(parent.getDisplayName());
                id(parent.getId());
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
                        .filter(x -> !aas.getSubmodels().contains(ReferenceBuilder.forSubmodel(x)))
                        .collect(Collectors.toList());
                if (!submodelsNotPresentInAAS.isEmpty()) {
                    throw new IllegalArgumentException(String.format("Submodel(s) not found in AAS (id: %s) ",
                            submodelsNotPresentInAAS.stream()
                                    .map(x -> x.getId())
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
