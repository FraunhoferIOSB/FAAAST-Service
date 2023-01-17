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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.io.Serializable;
import java.util.Objects;


/**
 * Registry Descriptor for IdentifierKeyValuePair.
 */
public class IdentifierKeyValuePairDescriptor implements Serializable {

    @JsonIgnore
    private String id;
    private ReferenceDescriptor semanticId;
    private ReferenceDescriptor externalSubjectId;
    private String key;
    private String value;

    public IdentifierKeyValuePairDescriptor() {
        id = null;
        semanticId = null;
        externalSubjectId = null;
        key = null;
        value = null;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public ReferenceDescriptor getSemanticId() {
        return semanticId;
    }


    public void setSemanticId(ReferenceDescriptor semanticId) {
        this.semanticId = semanticId;
    }


    public ReferenceDescriptor getExternalSubjectId() {
        return externalSubjectId;
    }


    public void setExternalSubjectId(ReferenceDescriptor externalSubjectId) {
        this.externalSubjectId = externalSubjectId;
    }


    public String getKey() {
        return key;
    }


    public void setKey(String key) {
        this.key = key;
    }


    public String getValue() {
        return value;
    }


    public void setValue(String value) {
        this.value = value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdentifierKeyValuePairDescriptor that = (IdentifierKeyValuePairDescriptor) o;
        return Objects.equals(id, that.id)
                && Objects.equals(semanticId, that.semanticId)
                && Objects.equals(externalSubjectId, that.externalSubjectId)
                && Objects.equals(key, that.key)
                && Objects.equals(value, that.value);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, semanticId, externalSubjectId, key, value);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends IdentifierKeyValuePairDescriptor, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B semanticId(ReferenceDescriptor value) {
            getBuildingInstance().setSemanticId(value);
            return getSelf();
        }


        public B externalSubjectId(ReferenceDescriptor value) {
            getBuildingInstance().setExternalSubjectId(value);
            return getSelf();
        }


        public B key(String value) {
            getBuildingInstance().setKey(value);
            return getSelf();
        }


        public B value(String value) {
            getBuildingInstance().setValue(value);
            return getSelf();
        }


        public B from(IdentifierKeyValuePair identifierKeyValuePair) {
            if (identifierKeyValuePair != null) {
                if (identifierKeyValuePair.getSemanticId() != null) {
                    getBuildingInstance().setSemanticId(ReferenceDescriptor.builder().from(identifierKeyValuePair.getSemanticId()).build());
                }
                if (identifierKeyValuePair.getExternalSubjectId() != null) {
                    getBuildingInstance().setExternalSubjectId(ReferenceDescriptor.builder().from(identifierKeyValuePair.getExternalSubjectId()).build());
                }
                getBuildingInstance().setKey(identifierKeyValuePair.getKey());
                getBuildingInstance().setValue(identifierKeyValuePair.getValue());
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<IdentifierKeyValuePairDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected IdentifierKeyValuePairDescriptor newBuildingInstance() {
            return new IdentifierKeyValuePairDescriptor();
        }
    }
}
