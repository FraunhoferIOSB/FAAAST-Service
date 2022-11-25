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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx;

import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.LinkedSegmentProviderConfig;


/**
 * Configuration for {@link InfluxV1LinkedSegmentProvider}.
 */
public class InfluxV1LinkedSegmentProviderConfig extends LinkedSegmentProviderConfig<InfluxV1LinkedSegmentProvider> {

    private static final String DEFAULT_TIME_FIELD = "time";

    private String database;
    private String timeField;

    public InfluxV1LinkedSegmentProviderConfig() {
        this.timeField = DEFAULT_TIME_FIELD;
    }


    public String getDatabase() {
        return database;
    }


    public void setDatabase(String database) {
        this.database = database;
    }


    public String getTimeField() {
        return timeField;
    }


    public void setTimeField(String timeField) {
        this.timeField = timeField;
    }

    protected abstract static class AbstractBuilder<T extends InfluxV1LinkedSegmentProviderConfig, B extends AbstractBuilder<T, B>>
            extends LinkedSegmentProviderConfig.AbstractBuilder<T, B> {

        public B database(String value) {
            getBuildingInstance().setDatabase(value);
            return getSelf();
        }


        public B timeField(String value) {
            getBuildingInstance().setTimeField(value);
            return getSelf();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<InfluxV1LinkedSegmentProviderConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected InfluxV1LinkedSegmentProviderConfig newBuildingInstance() {
            return new InfluxV1LinkedSegmentProviderConfig();
        }
    }
}
