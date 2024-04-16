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
package de.fraunhofer.iosb.ilt.faaast.service.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Describes the capabilities of a service in terms of implemented/supported service profiles.
 */
public class ServiceDescription {

    private List<ServiceSpecificationProfile> profiles;

    public ServiceDescription() {
        profiles = new ArrayList<>();
    }


    public ServiceDescription(ServiceSpecificationProfile... profiles) {
        this.profiles = Arrays.asList(profiles);
    }


    public List<ServiceSpecificationProfile> getProfiles() {
        return profiles;
    }


    public void setProfiles(List<ServiceSpecificationProfile> profiles) {
        this.profiles = profiles;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceDescription that = (ServiceDescription) o;
        return Objects.equals(profiles, that.profiles);
    }


    @Override
    public int hashCode() {
        return Objects.hash(profiles);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends ServiceDescription, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B profile(ServiceSpecificationProfile value) {
            getBuildingInstance().getProfiles().add(value);
            return getSelf();
        }


        public B profiles(List<ServiceSpecificationProfile> value) {
            getBuildingInstance().setProfiles(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<ServiceDescription, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected ServiceDescription newBuildingInstance() {
            return new ServiceDescription();
        }
    }
}
