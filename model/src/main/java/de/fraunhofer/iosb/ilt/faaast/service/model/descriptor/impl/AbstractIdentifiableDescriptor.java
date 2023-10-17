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

import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.Endpoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Methods for Identifiable.
 */
public abstract class AbstractIdentifiableDescriptor {

    private String idShort;
    private List<Endpoint> endpoints;
    private AdministrativeInformation administration;
    private List<LangStringTextType> descriptions;
    private List<LangStringNameType> displayNames;
    private String id;

    protected AbstractIdentifiableDescriptor() {
        idShort = null;
        endpoints = new ArrayList<>();
        administration = null;
        descriptions = new ArrayList<>();
        displayNames = new ArrayList<>();
    }


    protected AbstractIdentifiableDescriptor(
            String idShort,
            List<Endpoint> endpoints,
            AdministrativeInformation administration,
            List<LangStringTextType> descriptions,
            String identification) {
        this.idShort = idShort;
        this.endpoints = endpoints;
        this.administration = administration;
        this.descriptions = descriptions;
        this.id = identification;
    }


    public String getIdShort() {
        return idShort;
    }


    public void setIdShort(String idShort) {
        this.idShort = idShort;
    }


    public List<Endpoint> getEndpoints() {
        return endpoints;
    }


    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }


    public AdministrativeInformation getAdministration() {
        return administration;
    }


    public void setAdministration(AdministrativeInformation administration) {
        this.administration = administration;
    }


    public List<LangStringTextType> getDescriptions() {
        return descriptions;
    }


    public void setDescriptions(List<LangStringTextType> descriptions) {
        this.descriptions = descriptions;
    }


    public List<LangStringNameType> getDisplayNames() {
        return displayNames;
    }


    public void setDisplayNames(List<LangStringNameType> displayNames) {
        this.displayNames = displayNames;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractIdentifiableDescriptor that = (AbstractIdentifiableDescriptor) o;
        return Objects.equals(idShort, that.idShort)
                && Objects.equals(endpoints, that.endpoints)
                && Objects.equals(administration, that.administration)
                && Objects.equals(descriptions, that.descriptions)
                && Objects.equals(displayNames, that.displayNames)
                && Objects.equals(id, that.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(idShort, endpoints, administration, descriptions, displayNames, id);
    }

    public abstract static class AbstractBuilder<T extends AbstractIdentifiableDescriptor, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B idShort(String value) {
            getBuildingInstance().setIdShort(value);
            return getSelf();
        }


        public B endpoints(List<Endpoint> value) {
            getBuildingInstance().setEndpoints(value);
            return getSelf();
        }


        public B endpoint(Endpoint value) {
            getBuildingInstance().getEndpoints().add(value);
            return getSelf();
        }


        public B administration(AdministrativeInformation value) {
            getBuildingInstance().setAdministration(value);
            return getSelf();
        }


        public B descriptions(List<LangStringTextType> value) {
            getBuildingInstance().setDescriptions(value);
            return getSelf();
        }


        public B description(LangStringTextType value) {
            getBuildingInstance().getDescriptions().add(value);
            return getSelf();
        }


        public B displayNames(List<LangStringNameType> value) {
            getBuildingInstance().setDisplayNames(value);
            return getSelf();
        }


        public B displayName(LangStringNameType value) {
            getBuildingInstance().getDisplayNames().add(value);
            return getSelf();
        }


        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }
    }
}
