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

import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.io.Serializable;
import java.util.Objects;


/**
 * Registry Descriptor for ProtocolInformation.
 */
public class ProtocolInformationDescriptor implements Serializable {

    private String endpointAddress;
    private String endpointProtocol;
    private String endpointProtocolVersion;
    private String subprotocol;
    private String subprotocolBody;
    private String subprotocolBodyEncoding;

    public ProtocolInformationDescriptor() {

        endpointAddress = null;
        endpointProtocol = null;
        endpointProtocolVersion = null;
        subprotocol = null;
        subprotocolBody = null;
        subprotocolBodyEncoding = null;
    }


    public String getEndpointAddress() {
        return endpointAddress;
    }


    public void setEndpointAddress(String endpointAddress) {
        this.endpointAddress = endpointAddress;
    }


    public String getEndpointProtocol() {
        return endpointProtocol;
    }


    public void setEndpointProtocol(String endpointProtocol) {
        this.endpointProtocol = endpointProtocol;
    }


    public String getEndpointProtocolVersion() {
        return endpointProtocolVersion;
    }


    public void setEndpointProtocolVersion(String endpointProtocolVersion) {
        this.endpointProtocolVersion = endpointProtocolVersion;
    }


    public String getSubprotocol() {
        return subprotocol;
    }


    public void setSubprotocol(String subprotocol) {
        this.subprotocol = subprotocol;
    }


    public String getSubprotocolBody() {
        return subprotocolBody;
    }


    public void setSubprotocolBody(String subprotocolBody) {
        this.subprotocolBody = subprotocolBody;
    }


    public String getSubprotocolBodyEncoding() {
        return subprotocolBodyEncoding;
    }


    public void setSubprotocolBodyEncoding(String subprotocolBodyEncoding) {
        this.subprotocolBodyEncoding = subprotocolBodyEncoding;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProtocolInformationDescriptor that = (ProtocolInformationDescriptor) o;
        return Objects.equals(endpointAddress, that.endpointAddress)
                && Objects.equals(endpointProtocol, that.endpointProtocol)
                && Objects.equals(endpointProtocolVersion, that.endpointProtocolVersion)
                && Objects.equals(subprotocol, that.subprotocol)
                && Objects.equals(subprotocolBody, that.subprotocolBody)
                && Objects.equals(subprotocolBodyEncoding, that.subprotocolBodyEncoding);
    }


    @Override
    public int hashCode() {
        return Objects.hash(endpointAddress, endpointProtocol, endpointProtocolVersion, subprotocol, subprotocolBody, subprotocolBodyEncoding);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends ProtocolInformationDescriptor, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B endpointAddress(String value) {
            getBuildingInstance().setEndpointAddress(value);
            return getSelf();
        }


        public B endpointProtocol(String value) {
            getBuildingInstance().setEndpointProtocol(value);
            return getSelf();
        }


        public B endpointProtocolVersion(String value) {
            getBuildingInstance().setEndpointProtocolVersion(value);
            return getSelf();
        }


        public B subprotocol(String value) {
            getBuildingInstance().setSubprotocol(value);
            return getSelf();
        }


        public B subprotocolBody(String value) {
            getBuildingInstance().setSubprotocolBody(value);
            return getSelf();
        }


        public B subprotocolBodyEncoding(String value) {
            getBuildingInstance().setSubprotocolBodyEncoding(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<ProtocolInformationDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected ProtocolInformationDescriptor newBuildingInstance() {
            return new ProtocolInformationDescriptor();
        }
    }
}
