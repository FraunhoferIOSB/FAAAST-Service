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

import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.IdentifierKeyValuePairDescriptor;
import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.ReferenceDescriptor;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.Objects;


/**
 * Registry Descriptor default implementation for IdentifierKeyValuePair.
 */
public class DefaultIdentifierKeyValuePairDescriptor implements IdentifierKeyValuePairDescriptor {

    private ReferenceDescriptor semanticId;
    private ReferenceDescriptor externalSubjectId;
    private String key;
    private String value;

    public DefaultIdentifierKeyValuePairDescriptor() {
        semanticId = null;
        externalSubjectId = null;
        key = null;
        value = null;
    }


    public DefaultIdentifierKeyValuePairDescriptor(IdentifierKeyValuePairDescriptor source) {
        semanticId = source.getSemanticId();
        externalSubjectId = source.getExternalSubjectId();
        key = source.getKey();
        value = source.getValue();
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
    public ReferenceDescriptor getExternalSubjectId() {
        return externalSubjectId;
    }


    @Override
    public void setExternalSubjectId(ReferenceDescriptor externalSubjectId) {
        this.externalSubjectId = externalSubjectId;
    }


    @Override
    public String getKey() {
        return key;
    }


    @Override
    public void setKey(String key) {
        this.key = key;
    }


    @Override
    public String getValue() {
        return value;
    }


    @Override
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
        DefaultIdentifierKeyValuePairDescriptor that = (DefaultIdentifierKeyValuePairDescriptor) o;
        return Objects.equals(semanticId, that.semanticId)
                && Objects.equals(externalSubjectId, that.externalSubjectId)
                && Objects.equals(key, that.key)
                && Objects.equals(value, that.value);
    }


    @Override
    public int hashCode() {
        return Objects.hash(semanticId, externalSubjectId, key, value);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends DefaultIdentifierKeyValuePairDescriptor, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

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
                    getBuildingInstance().setSemanticId(DefaultReferenceDescriptor.builder().from(identifierKeyValuePair.getSemanticId()).build());
                }
                if (identifierKeyValuePair.getExternalSubjectId() != null) {
                    getBuildingInstance().setExternalSubjectId(DefaultReferenceDescriptor.builder().from(identifierKeyValuePair.getExternalSubjectId()).build());
                }
                getBuildingInstance().setKey(identifierKeyValuePair.getKey());
                getBuildingInstance().setValue(identifierKeyValuePair.getValue());
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<DefaultIdentifierKeyValuePairDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected DefaultIdentifierKeyValuePairDescriptor newBuildingInstance() {
            return new DefaultIdentifierKeyValuePairDescriptor();
        }
    }
}
