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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.csv.impl;

import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.csv.AbstractCSVExternalSegmentProviderConfig;


/**
 * Configuration for external segment providers referencing a file or blob containing CSV data
 * ({@link CSVExternalSegmentProvider}).
 */
public class CSVExternalSegmentProviderConfig extends AbstractCSVExternalSegmentProviderConfig<CSVExternalSegmentProvider> {

    private String baseDir;
    private String timeColumn;

    public String getBaseDir() {
        return baseDir;
    }


    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }


    public String getTimeColumn() {
        return timeColumn;
    }


    public void setTimeColumn(String timeColumn) {
        this.timeColumn = timeColumn;
    }

    protected abstract static class AbstractBuilder<T extends CSVExternalSegmentProviderConfig, B extends AbstractBuilder<T, B>>
            extends AbstractCSVExternalSegmentProviderConfig.AbstractBuilder<T, B> {

        public B baseDir(String value) {
            getBuildingInstance().setBaseDir(value);
            return getSelf();
        }


        public B timeColumn(String value) {
            getBuildingInstance().setTimeColumn(value);
            return getSelf();
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<CSVExternalSegmentProviderConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected CSVExternalSegmentProviderConfig newBuildingInstance() {
            return new CSVExternalSegmentProviderConfig();
        }
    }
}
