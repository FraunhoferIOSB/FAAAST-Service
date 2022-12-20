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
 * Registry Descriptor for Identification.
 */
public class IdentificationDescriptor implements Serializable {

    private String id;
    private String idType;

    public IdentificationDescriptor() {
        id = null;
        idType = null;
    }


    public IdentificationDescriptor(String id, String idType) {
        this.id = id;
        this.idType = idType;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getIdType() {
        return idType;
    }


    public void setIdType(String idType) {
        this.idType = idType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdentificationDescriptor that = (IdentificationDescriptor) o;
        return Objects.equals(id, that.id)
                && Objects.equals(idType, that.idType);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, idType);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends IdentificationDescriptor, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B idType(String value) {
            getBuildingInstance().setIdType(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<IdentificationDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected IdentificationDescriptor newBuildingInstance() {
            return new IdentificationDescriptor();
        }
    }
}
