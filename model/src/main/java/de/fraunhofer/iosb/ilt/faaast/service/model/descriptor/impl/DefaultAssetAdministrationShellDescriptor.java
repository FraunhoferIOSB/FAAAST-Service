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

import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AdministrationDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.AssetAdministrationShellDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.DescriptionDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.EndpointDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.IdentificationDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.IdentifierKeyValuePairDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.ReferenceDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Registry Descriptor default implementation for AssetAdministrationShell.
 */
public class DefaultAssetAdministrationShellDescriptor extends AbstractIdentifiableDescriptor implements AssetAdministrationShellDescriptor {

    private ReferenceDescriptor globalAssetId;
    private List<IdentifierKeyValuePairDescriptor> specificAssetIds;
    private List<SubmodelDescriptor> submodels;

    public DefaultAssetAdministrationShellDescriptor() {
        globalAssetId = null;
        specificAssetIds = new ArrayList<>();
        submodels = new ArrayList<>();
    }


    @Override
    public ReferenceDescriptor getGlobalAssetId() {
        return globalAssetId;
    }


    @Override
    public void setGlobalAssetId(ReferenceDescriptor globalAssetId) {
        this.globalAssetId = globalAssetId;
    }


    @Override
    public List<IdentifierKeyValuePairDescriptor> getSpecificAssetIds() {
        return specificAssetIds;
    }


    @Override
    public void setSpecificAssetIds(List<IdentifierKeyValuePairDescriptor> specificAssetIds) {
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

    public abstract static class AbstractBuilder<T extends DefaultAssetAdministrationShellDescriptor, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B idShort(String value) {
            getBuildingInstance().setIdShort(value);
            return getSelf();
        }


        public B endpoints(List<EndpointDescriptor> value) {
            getBuildingInstance().setEndpoints(value);
            return getSelf();
        }


        public B endpoint(EndpointDescriptor value) {
            getBuildingInstance().getEndpoints().add(value);
            return getSelf();
        }


        public B administration(AdministrationDescriptor value) {
            getBuildingInstance().setAdministration(value);
            return getSelf();
        }


        public B descriptions(List<DescriptionDescriptor> value) {
            getBuildingInstance().setDescriptions(value);
            return getSelf();
        }


        public B oneDescription(DescriptionDescriptor value) {
            getBuildingInstance().getDescriptions().add(value);
            return getSelf();
        }


        public B globalAssetId(ReferenceDescriptor value) {
            getBuildingInstance().setGlobalAssetId(value);
            return getSelf();
        }


        public B identification(IdentificationDescriptor value) {
            getBuildingInstance().setIdentification(value);
            return getSelf();
        }


        public B specificAssetIds(List<IdentifierKeyValuePairDescriptor> value) {
            getBuildingInstance().setSpecificAssetIds(value);
            return getSelf();
        }


        public B specificAssetId(IdentifierKeyValuePairDescriptor value) {
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


        public B from(AssetAdministrationShell assetAdministrationShell) {
            if (assetAdministrationShell != null) {
                getBuildingInstance().setIdShort(assetAdministrationShell.getIdShort());
                if (assetAdministrationShell.getAdministration() != null) {
                    getBuildingInstance().setAdministration(AdministrationDescriptor.builder().from(assetAdministrationShell.getAdministration()).build());
                }
                for (var langString: assetAdministrationShell.getDescriptions()) {
                    getBuildingInstance().getDescriptions().add(DefaultDescriptionDescriptor.builder().from(langString).build());
                }
                if (assetAdministrationShell.getIdentification() != null) {
                    getBuildingInstance().setIdentification(IdentificationDescriptor.builder().from(assetAdministrationShell.getIdentification()).build());
                }
                if (assetAdministrationShell.getAssetInformation() != null) {
                    if (assetAdministrationShell.getAssetInformation().getGlobalAssetId() != null) {
                        getBuildingInstance()
                                .setGlobalAssetId(DefaultReferenceDescriptor.builder().from(assetAdministrationShell.getAssetInformation().getGlobalAssetId()).build());
                    }
                    for (var specificAssetId: assetAdministrationShell.getAssetInformation().getSpecificAssetIds()) {
                        getBuildingInstance().getSpecificAssetIds().add(DefaultIdentifierKeyValuePairDescriptor.builder().from(specificAssetId).build());
                    }
                }
            }
            return getSelf();
        }


        public B from(AssetAdministrationShell assetAdministrationShell, List<Submodel> submodels) {
            from(assetAdministrationShell);
            if (assetAdministrationShell != null) {
                Map<Reference, Submodel> submodelMap = new HashMap<>();
                for (Submodel submodel: submodels) {
                    KeyType keyType = KeyType.valueOf(submodel.getIdentification().getIdType().name());
                    submodelMap.put(
                            new DefaultReference.Builder()
                                    .key(new DefaultKey.Builder().idType(keyType).type(KeyElements.SUBMODEL).value(submodel.getIdentification().getIdentifier()).build()).build(),
                            submodel);
                }
                for (var reference: assetAdministrationShell.getSubmodels()) {
                    if (!submodelMap.containsKey(reference)) {
                        throw new IllegalArgumentException("Submodel not found: " + AasUtils.asString(reference));
                    }
                    getBuildingInstance().getSubmodels().add(DefaultSubmodelDescriptor.builder().from(submodelMap.get(reference)).build());
                }
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
