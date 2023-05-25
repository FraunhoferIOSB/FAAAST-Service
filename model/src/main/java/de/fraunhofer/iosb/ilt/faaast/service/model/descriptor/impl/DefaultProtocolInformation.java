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

import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.ProtocolInformation;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Registry Descriptor for ProtocolInformation.
 */
public class DefaultProtocolInformation implements ProtocolInformation {

    private String endpointAddress;
    private String endpointProtocol;
    private String endpointProtocolVersion;
    private String subprotocol;
    private String subprotocolBody;
    private String subprotocolBodyEncoding;

    public DefaultProtocolInformation() {

        endpointAddress = null;
        endpointProtocol = null;
        endpointProtocolVersion = null;
        subprotocol = null;
        subprotocolBody = null;
        subprotocolBodyEncoding = null;
    }


    @Override
    public String getEndpointAddress() {
        return endpointAddress;
    }


    @Override
    public void setEndpointAddress(String endpointAddress) {
        this.endpointAddress = endpointAddress;
    }


    @Override
    public String getEndpointProtocol() {
        return endpointProtocol;
    }


    @Override
    public void setEndpointProtocol(String endpointProtocol) {
        this.endpointProtocol = endpointProtocol;
    }


    @Override
    public String getEndpointProtocolVersion() {
        return endpointProtocolVersion;
    }


    @Override
    public void setEndpointProtocolVersion(String endpointProtocolVersion) {
        this.endpointProtocolVersion = endpointProtocolVersion;
    }


    @Override
    public String getSubprotocol() {
        return subprotocol;
    }


    @Override
    public void setSubprotocol(String subprotocol) {
        this.subprotocol = subprotocol;
    }


    @Override
    public String getSubprotocolBody() {
        return subprotocolBody;
    }


    @Override
    public void setSubprotocolBody(String subprotocolBody) {
        this.subprotocolBody = subprotocolBody;
    }


    @Override
    public String getSubprotocolBodyEncoding() {
        return subprotocolBodyEncoding;
    }


    @Override
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
        DefaultProtocolInformation that = (DefaultProtocolInformation) o;
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

    public abstract static class AbstractBuilder<T extends DefaultProtocolInformation, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B from(ProtocolInformation other) {
            if (Objects.nonNull(other)) {
                endpointAddress(other.getEndpointAddress());
                endpointProtocol(other.getEndpointProtocol());
                endpointProtocolVersion(other.getEndpointProtocolVersion());
                subprotocol(other.getSubprotocol());
                subprotocolBody(other.getSubprotocolBody());
                subprotocolBodyEncoding(other.getSubprotocolBodyEncoding());
            }
            return getSelf();
        }


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

    public static class Builder extends AbstractBuilder<DefaultProtocolInformation, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected DefaultProtocolInformation newBuildingInstance() {
            return new DefaultProtocolInformation();
        }
    }
}
