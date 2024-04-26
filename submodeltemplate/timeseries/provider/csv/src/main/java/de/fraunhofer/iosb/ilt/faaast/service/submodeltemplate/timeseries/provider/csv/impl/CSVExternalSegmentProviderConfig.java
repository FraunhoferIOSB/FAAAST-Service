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

import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.ExternalSegmentProviderConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Configuration for external segment providers referencing a file or blob containing CSV data
 * ({@link CSVExternalSegmentProvider}).
 */
public class CSVExternalSegmentProviderConfig extends ExternalSegmentProviderConfig<CSVExternalSegmentProvider> {

    private List<String> timeColumns;

    private Map<String, String> columnToVariableNames = new HashMap<>();

    public List<String> getTimeColumns() {
        return timeColumns;
    }


    public void setTimeColumns(List<String> timeColumns) {
        this.timeColumns = timeColumns;
    }


    public Map<String, String> getColumnToVariableNames() {
        return columnToVariableNames;
    }


    public void setColumnToVariableNames(Map<String, String> columnToVariableNames) {
        this.columnToVariableNames = columnToVariableNames;
    }

    public abstract static class AbstractBuilder<T extends CSVExternalSegmentProviderConfig, B extends AbstractBuilder<T, B>>
            extends ExternalSegmentProviderConfig.AbstractBuilder<T, B> {

        public B timeColumns(List<String> value) {
            getBuildingInstance().setTimeColumns(value);
            return getSelf();
        }


        public B columnToVariableNames(Map<String, String> value) {
            getBuildingInstance().setColumnToVariableNames(value);
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
