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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Registry Descriptor for Submodel.
 */
public class SubmodelDescriptor implements Serializable {

    @JsonIgnore
    private String id;
    private String idShort;
    @JsonIgnore
    private String identifier;
    private List<EndpointDescriptor> endpoints;
    private AdministrationDescriptor administration;
    private List<DescriptionDescriptor> descriptions;
    private IdentificationDescriptor identification;
    private ReferenceDescriptor semanticId;

    public SubmodelDescriptor() {
        id = null;
        idShort = null;
        identifier = null;
        endpoints = new ArrayList<>();
        administration = null;
        descriptions = new ArrayList<>();
        identification = null;
        semanticId = null;
    }


    public SubmodelDescriptor(String id, String idShort, String identifier, List<EndpointDescriptor> endpoints, AdministrationDescriptor administration,
            List<DescriptionDescriptor> descriptions, IdentificationDescriptor identification, ReferenceDescriptor semanticId) {
        this.id = id;
        this.idShort = idShort;
        this.identifier = identifier;
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
        this.identification = identification;
        this.semanticId = semanticId;
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


    public String getIdentifier() {
        return identifier;
    }


    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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


    public void setAdministration(AdministrationDescriptor administration) {
        this.administration = administration;
    }


    public List<DescriptionDescriptor> getDescriptions() {
        return descriptions;
    }


    public void setDescriptions(List<DescriptionDescriptor> descriptions) {
        this.descriptions = descriptions;
    }


    public IdentificationDescriptor getIdentification() {
        return identification;
    }


    public void setIdentification(IdentificationDescriptor identification) {
        this.identification = identification;
    }


    public ReferenceDescriptor getSemanticId() {
        return semanticId;
    }


    public void setSemanticId(ReferenceDescriptor semanticId) {
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
        SubmodelDescriptor that = (SubmodelDescriptor) o;
        return Objects.equals(id, that.id)
                && Objects.equals(idShort, that.idShort)
                && Objects.equals(identifier, that.identifier)
                && Objects.equals(endpoints, that.endpoints)
                && Objects.equals(administration, that.administration)
                && Objects.equals(descriptions, that.descriptions)
                && Objects.equals(identification, that.identification)
                && Objects.equals(semanticId, that.semanticId);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, idShort, identifier, endpoints, administration, descriptions, identification, semanticId);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends SubmodelDescriptor, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B idShort(String value) {
            getBuildingInstance().setIdShort(value);
            return getSelf();
        }


        public B identifier(String value) {
            getBuildingInstance().setIdentifier(value);
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


        public B description(DescriptionDescriptor value) {
            getBuildingInstance().getDescriptions().add(value);
            return getSelf();
        }


        public B identification(IdentificationDescriptor value) {
            getBuildingInstance().setIdentification(value);
            return getSelf();
        }


        public B semanticId(ReferenceDescriptor value) {
            getBuildingInstance().setSemanticId(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<SubmodelDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected SubmodelDescriptor newBuildingInstance() {
            return new SubmodelDescriptor();
        }
    }
}
