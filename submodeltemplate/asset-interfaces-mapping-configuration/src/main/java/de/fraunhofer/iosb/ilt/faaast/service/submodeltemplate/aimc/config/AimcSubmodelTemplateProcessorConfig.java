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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config;

import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.SubmodelTemplateProcessorConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.AimcSubmodelTemplateProcessor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Configuration for SMT Asset Interfaces Mapping Configuration processor.
 */
public class AimcSubmodelTemplateProcessorConfig extends SubmodelTemplateProcessorConfig<AimcSubmodelTemplateProcessor> {

    private Map<String, List<Credentials>> credentials;

    public AimcSubmodelTemplateProcessorConfig() {
        credentials = new HashMap<>();
    }


    public Map<String, List<Credentials>> getCredentials() {
        return credentials;
    }


    public void setCredentials(Map<String, List<Credentials>> credentials) {
        this.credentials = credentials;
    }


    @Override
    public int hashCode() {
        return Objects.hash(credentials);
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
        final AimcSubmodelTemplateProcessorConfig other = (AimcSubmodelTemplateProcessorConfig) obj;
        return Objects.equals(credentials, other.credentials);
    }

    protected abstract static class AbstractBuilder<C extends AimcSubmodelTemplateProcessorConfig, B extends AbstractBuilder<C, B>>
            extends ExtendableBuilder<C, B> {

        public B connectionLevelCredentials(Map<String, List<Credentials>> value) {
            getBuildingInstance().setCredentials(value);
            return getSelf();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<AimcSubmodelTemplateProcessorConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected AimcSubmodelTemplateProcessorConfig newBuildingInstance() {
            return new AimcSubmodelTemplateProcessorConfig();
        }
    }

}
