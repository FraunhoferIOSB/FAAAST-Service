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
package de.fraunhofer.iosb.ilt.faaast.service.model.registry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Registry Descriptor for AssetAdministrationShell.
 */
public class AssetAdministrationShellDescriptor {

    @JsonIgnore
    private String id;

    private String idShort;

    private List<EndpointDescriptor> endpoints;

    private AdministrationDescriptor administration;

    private List<DescriptionDescriptor> descriptions;

    //private Reference globalAssetId;

    //private Identification identification;

    //private List<IdentifierKeyValuePair> specificAssetIds;

    //@Singular
    //private List<Submodel> submodelDescriptors;

    public AssetAdministrationShellDescriptor() {
        id = null;
        idShort = null;
        endpoints = new ArrayList<>();
        administration = null;
        descriptions = new ArrayList<>();
    }


    public AssetAdministrationShellDescriptor(String id, String idShort, List<EndpointDescriptor> endpoints, AdministrationDescriptor administration,
            List<DescriptionDescriptor> descriptions) {
        this.id = id;
        this.idShort = idShort;
        if (endpoints == null) {
            this.endpoints = new ArrayList<>();
        }
        else {
            this.endpoints = endpoints;
        }
        this.administration = administration;
        if (descriptions == null) {
            this.descriptions = new ArrayList<>();
        }
        else {
            this.descriptions = descriptions;
        }
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getIdShort() {
        return idShort;
    }


    public void setIdShort(String idShort) {
        this.idShort = idShort;
    }


    public List<EndpointDescriptor> getEndpoints() {
        return endpoints;
    }


    public void setEndpoints(List<EndpointDescriptor> endpoints) {
        this.endpoints = endpoints;
    }


    public AdministrationDescriptor getAdministration() {
        return administration;
    }


    public void setAdministrationDescriptor(AdministrationDescriptor administration) {
        this.administration = administration;
    }


    public List<DescriptionDescriptor> getDescriptions() {
        return descriptions;
    }


    public void setDescriptions(List<DescriptionDescriptor> descriptions) {
        this.descriptions = descriptions;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AssetAdministrationShellDescriptor that = (AssetAdministrationShellDescriptor) o;
        return Objects.equals(id, that.id)
                && Objects.equals(idShort, that.idShort)
                && Objects.equals(endpoints, that.endpoints)
                && Objects.equals(administration, that.administration)
                && Objects.equals(descriptions, that.descriptions);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, idShort, endpoints, administration, descriptions);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends AssetAdministrationShellDescriptor, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


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
            getBuildingInstance().setAdministrationDescriptor(value);
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
    }

    public static class Builder extends AbstractBuilder<AssetAdministrationShellDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected AssetAdministrationShellDescriptor newBuildingInstance() {
            return new AssetAdministrationShellDescriptor();
        }
    }
}
