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
package de.fraunhofer.iosb.ilt.faaast.service.model.descriptor;

import io.adminshell.aas.v3.model.AdministrativeInformation;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.io.Serializable;
import java.util.Objects;


/**
 * Registry Descriptor for Administration.
 */
public class AdministrationDescriptor implements Serializable {

    private String version;
    private String revision;

    public AdministrationDescriptor() {
        version = null;
        revision = null;
    }


    public String getVersion() {
        return version;
    }


    public void setVersion(String version) {
        this.version = version;
    }


    public String getRevision() {
        return revision;
    }


    public void setRevision(String revision) {
        this.revision = revision;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdministrationDescriptor that = (AdministrationDescriptor) o;
        return Objects.equals(version, that.version)
                && Objects.equals(revision, that.revision);
    }


    @Override
    public int hashCode() {
        return Objects.hash(version, revision);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends AdministrationDescriptor, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B version(String value) {
            getBuildingInstance().setVersion(value);
            return getSelf();
        }


        public B revision(String value) {
            getBuildingInstance().setRevision(value);
            return getSelf();
        }


        public B from(AdministrativeInformation administrativeInformation) {
            if (administrativeInformation != null) {
                getBuildingInstance().setVersion(administrativeInformation.getVersion());
                getBuildingInstance().setRevision(administrativeInformation.getRevision());
            }
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<AdministrationDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected AdministrationDescriptor newBuildingInstance() {
            return new AdministrationDescriptor();
        }
    }
}
