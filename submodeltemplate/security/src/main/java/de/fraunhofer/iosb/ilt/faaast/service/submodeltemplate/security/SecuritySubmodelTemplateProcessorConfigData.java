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

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Configuration data for SMT Asset Interfaces Mapping Configuration processor.
 */
public class SecuritySubmodelTemplateProcessorConfigData {

    private String username;
    private String password;
    private long subscriptionInterval;

    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public long getSubscriptionInterval() {
        return subscriptionInterval;
    }


    public void setSubscriptionInterval(long subscriptionInterval) {
        this.subscriptionInterval = subscriptionInterval;
    }


    @Override
    public int hashCode() {
        return Objects.hash(username, password, subscriptionInterval);
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
        final de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.security.SecuritySubmodelTemplateProcessorConfigData other = (de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.security.SecuritySubmodelTemplateProcessorConfigData) obj;
        return super.equals(other)
                && Objects.equals(username, other.username)
                && Objects.equals(password, other.password)
                && Objects.equals(subscriptionInterval, other.getSubscriptionInterval());
    }

    protected abstract static class AbstractBuilder<C extends de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.security.SecuritySubmodelTemplateProcessorConfigData, B extends AbstractBuilder<C, B>>
            extends ExtendableBuilder<C, B> {

        public B username(String value) {
            getBuildingInstance().setUsername(value);
            return getSelf();
        }


        public B password(String value) {
            getBuildingInstance().setPassword(value);
            return getSelf();
        }


        public B subscriptionInterval(long value) {
            getBuildingInstance().setSubscriptionInterval(value);
            return getSelf();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.security.SecuritySubmodelTemplateProcessorConfigData, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.security.SecuritySubmodelTemplateProcessorConfigData newBuildingInstance() {
            return new de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.security.SecuritySubmodelTemplateProcessorConfigData();
        }
    }
}
