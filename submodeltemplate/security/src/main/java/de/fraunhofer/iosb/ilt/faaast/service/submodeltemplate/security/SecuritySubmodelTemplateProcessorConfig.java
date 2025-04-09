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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.security;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.fraunhofer.iosb.ilt.faaast.service.config.serialization.ReferenceDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.config.serialization.ReferenceSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.SubmodelTemplateProcessorConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Configuration for SMT Asset Interfaces Mapping Configuration processor.
 */
public class SecuritySubmodelTemplateProcessorConfig extends SubmodelTemplateProcessorConfig<SecuritySubmodelTemplateProcessor> {

    @JsonSerialize(keyUsing = ReferenceSerializer.class)
    @JsonDeserialize(keyUsing = ReferenceDeserializer.class)
    private Map<Reference, SecuritySubmodelTemplateProcessorConfigData> interfaceConfigurations;

    public SecuritySubmodelTemplateProcessorConfig() {
        interfaceConfigurations = new HashMap<>();
    }


    public Map<Reference, SecuritySubmodelTemplateProcessorConfigData> getInterfaceConfigurations() {
        return interfaceConfigurations;
    }


    public void setInterfaceConfigurations(Map<Reference, SecuritySubmodelTemplateProcessorConfigData> value) {
        interfaceConfigurations = value;
    }


    @Override
    public int hashCode() {
        return Objects.hash(interfaceConfigurations);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.security.SecuritySubmodelTemplateProcessorConfig other = (de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.security.SecuritySubmodelTemplateProcessorConfig) obj;
        return super.equals(other)
                && Objects.equals(interfaceConfigurations, other.interfaceConfigurations);
    }

    protected abstract static class AbstractBuilder<C extends de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.security.SecuritySubmodelTemplateProcessorConfig, B extends AbstractBuilder<C, B>>
            extends ExtendableBuilder<C, B> {

        public B interfaceConfigurations(Map<Reference, SecuritySubmodelTemplateProcessorConfigData> value) {
            getBuildingInstance().setInterfaceConfigurations(value);
            return getSelf();
        }


        public B interfaceConfiguration(Reference key, SecuritySubmodelTemplateProcessorConfigData value) {
            getBuildingInstance().getInterfaceConfigurations().put(key, value);
            return getSelf();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.security.SecuritySubmodelTemplateProcessorConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.security.SecuritySubmodelTemplateProcessorConfig newBuildingInstance() {
            return new de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.security.SecuritySubmodelTemplateProcessorConfig();
        }
    }
}
