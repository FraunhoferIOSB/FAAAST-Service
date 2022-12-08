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
import java.util.Objects;


/**
 * Registry Descriptor for Endpoint.
 */
public class EndpointDescriptor {

    @JsonIgnore
    private String id;

    private String interfaceInformation;

    private ProtocolInformationDescriptor protocolInformation;

    public EndpointDescriptor() {

        id = null;
        interfaceInformation = null;
        protocolInformation = null;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getInterfaceInformation() {
        return interfaceInformation;
    }


    public void setInterfaceInformation(String interfaceInformation) {
        this.interfaceInformation = interfaceInformation;
    }


    public ProtocolInformationDescriptor getProtocolInformation() {
        return protocolInformation;
    }


    public void setProtocolInformation(ProtocolInformationDescriptor protocolInformation) {
        this.protocolInformation = protocolInformation;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EndpointDescriptor that = (EndpointDescriptor) o;
        return Objects.equals(id, that.id)
                && Objects.equals(interfaceInformation, that.interfaceInformation)
                && Objects.equals(protocolInformation, that.protocolInformation);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, interfaceInformation, protocolInformation);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends EndpointDescriptor, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B interfaceInformation(String value) {
            getBuildingInstance().setInterfaceInformation(value);
            return getSelf();
        }


        public B protocolInformation(ProtocolInformationDescriptor value) {
            getBuildingInstance().setProtocolInformation(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<EndpointDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected EndpointDescriptor newBuildingInstance() {
            return new EndpointDescriptor();
        }
    }
}
