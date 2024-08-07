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
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.ProtocolInformation;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Registry Descriptor default implementation for endpoint.
 */
public class DefaultEndpoint implements Endpoint {

    private String _interface;
    private ProtocolInformation protocolInformation;

    public DefaultEndpoint() {
        _interface = null;
        protocolInformation = null;
    }


    @Override
    public String getInterface() {
        return _interface;
    }


    @Override
    public void setInterface(String _interface) {
        this._interface = _interface;
    }


    @Override
    public ProtocolInformation getProtocolInformation() {
        return protocolInformation;
    }


    @Override
    public void setProtocolInformation(ProtocolInformation protocolInformation) {
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
        DefaultEndpoint that = (DefaultEndpoint) o;
        return Objects.equals(_interface, that._interface)
                && Objects.equals(protocolInformation, that.protocolInformation);
    }


    @Override
    public int hashCode() {
        return Objects.hash(_interface, protocolInformation);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends DefaultEndpoint, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B from(Endpoint other) {
            if (Objects.nonNull(other)) {
                _interface(other.getInterface());
                protocolInformation(other.getProtocolInformation());
            }
            return getSelf();
        }


        public B _interface(String value) {
            getBuildingInstance().setInterface(value);
            return getSelf();
        }


        public B protocolInformation(ProtocolInformation value) {
            getBuildingInstance().setProtocolInformation(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<DefaultEndpoint, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected DefaultEndpoint newBuildingInstance() {
            return new DefaultEndpoint();
        }
    }
}
