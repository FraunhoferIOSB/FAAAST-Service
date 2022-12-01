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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx.v2;

import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx.AbstractInfluxLinkedSegmentProviderConfig;


/**
 * Configuration for {@link InfluxV2LinkedSegmentProvider}.
 */
public class InfluxV2LinkedSegmentProviderConfig extends AbstractInfluxLinkedSegmentProviderConfig<InfluxV2LinkedSegmentProvider> {

    private String token;
    private String bucket;
    private String organization;

    public String getOrganization() {
        return organization;
    }


    public void setOrganization(String organization) {
        this.organization = organization;
    }


    public String getToken() {
        return token;
    }


    public String getBucket() {
        return bucket;
    }


    public void setToken(String token) {
        this.token = token;
    }


    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    protected abstract static class AbstractBuilder<C extends InfluxV2LinkedSegmentProviderConfig, B extends AbstractBuilder<C, B>>
            extends AbstractInfluxLinkedSegmentProviderConfig.AbstractBuilder<C, B> {

        public B token(String value) {
            getBuildingInstance().setToken(value);
            return getSelf();
        }


        public B bucket(String value) {
            getBuildingInstance().setBucket(value);
            return getSelf();
        }


        public B organization(String value) {
            getBuildingInstance().setOrganization(value);
            return getSelf();
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<InfluxV2LinkedSegmentProviderConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected InfluxV2LinkedSegmentProviderConfig newBuildingInstance() {
            return new InfluxV2LinkedSegmentProviderConfig();
        }
    }
}
