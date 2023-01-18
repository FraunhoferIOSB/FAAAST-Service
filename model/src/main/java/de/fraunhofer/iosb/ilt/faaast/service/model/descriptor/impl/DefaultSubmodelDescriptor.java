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
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.DescriptionDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.EndpointDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.IdentificationDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.ReferenceDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.SubmodelDescriptor;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Registry Descriptor for default implementation Submodel.
 */
public class DefaultSubmodelDescriptor implements SubmodelDescriptor {

    private String idShort;
    private List<EndpointDescriptor> endpoints;
    private AdministrationDescriptor administration;
    private List<DescriptionDescriptor> descriptions;
    private IdentificationDescriptor identification;
    private ReferenceDescriptor semanticId;

    public DefaultSubmodelDescriptor() {
        idShort = null;
        endpoints = new ArrayList<>();
        administration = null;
        descriptions = new ArrayList<>();
        identification = null;
        semanticId = null;
    }


    @Override
    public String getIdShort() {
        return idShort;
    }


    @Override
    public void setIdShort(String idShort) {
        this.idShort = idShort;
    }


    @Override
    public List<EndpointDescriptor> getEndpoints() {
        return endpoints;
    }


    @Override
    public void setEndpoints(List<EndpointDescriptor> endpoints) {
        this.endpoints = endpoints;
    }


    @Override
    public AdministrationDescriptor getAdministration() {
        return administration;
    }


    @Override
    public void setAdministration(AdministrationDescriptor administration) {
        this.administration = administration;
    }


    @Override
    public List<DescriptionDescriptor> getDescriptions() {
        return descriptions;
    }


    @Override
    public void setDescriptions(List<DescriptionDescriptor> descriptions) {
        this.descriptions = descriptions;
    }


    @Override
    public IdentificationDescriptor getIdentification() {
        return identification;
    }


    @Override
    public void setIdentification(IdentificationDescriptor identification) {
        this.identification = identification;
    }


    @Override
    public ReferenceDescriptor getSemanticId() {
        return semanticId;
    }


    @Override
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
        DefaultSubmodelDescriptor that = (DefaultSubmodelDescriptor) o;
        return Objects.equals(idShort, that.idShort)
                && Objects.equals(endpoints, that.endpoints)
                && Objects.equals(administration, that.administration)
                && Objects.equals(descriptions, that.descriptions)
                && Objects.equals(identification, that.identification)
                && Objects.equals(semanticId, that.semanticId);
    }


    @Override
    public int hashCode() {
        return Objects.hash(idShort, endpoints, administration, descriptions, identification, semanticId);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends DefaultSubmodelDescriptor, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

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


        public B from(Submodel submodel) {
            if (submodel != null) {
                getBuildingInstance().setIdShort(submodel.getIdShort());
                if (submodel.getIdentification() != null) {
                    getBuildingInstance().setIdentification(IdentificationDescriptor.builder().from(submodel.getIdentification()).build());
                }
                if (submodel.getAdministration() != null) {
                    getBuildingInstance().setAdministration(AdministrationDescriptor.builder().from(submodel.getAdministration()).build());
                }
                for (LangString langString: submodel.getDescriptions()) {
                    getBuildingInstance().getDescriptions().add(DefaultDescriptionDescriptor.builder().from(langString).build());
                }
                if (submodel.getSemanticId() != null) {
                    getBuildingInstance().setSemanticId(DefaultReferenceDescriptor.builder().from(submodel.getSemanticId()).build());
                }
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
