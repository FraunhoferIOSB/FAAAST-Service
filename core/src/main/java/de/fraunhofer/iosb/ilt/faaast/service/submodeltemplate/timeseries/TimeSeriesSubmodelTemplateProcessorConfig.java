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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries;

import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.SubmodelTemplateProcessorConfig;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.Objects;


/**
 * Configuration for SMT TimeSeries Processor.
 */
public class TimeSeriesSubmodelTemplateProcessorConfig extends SubmodelTemplateProcessorConfig<TimeSeriesSubmodelTemplateProcessor> {
    private boolean useSegmentTimestamps;

    public boolean isUseSegmentTimestamps() {
        return useSegmentTimestamps;
    }


    public void setUseSegmentTimestamps(boolean useSegmentTimestamps) {
        this.useSegmentTimestamps = useSegmentTimestamps;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimeSeriesSubmodelTemplateProcessorConfig that = (TimeSeriesSubmodelTemplateProcessorConfig) o;
        return Objects.equals(useSegmentTimestamps, that.useSegmentTimestamps);
    }


    @Override
    public int hashCode() {
        return Objects.hash(useSegmentTimestamps);
    }

    protected abstract static class AbstractBuilder<C extends TimeSeriesSubmodelTemplateProcessorConfig, B extends AbstractBuilder<C, B>>
            extends ExtendableBuilder<C, B> {

        public B useSegmentTimestamps(boolean value) {
            getBuildingInstance().setUseSegmentTimestamps(value);
            return getSelf();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<TimeSeriesSubmodelTemplateProcessorConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected TimeSeriesSubmodelTemplateProcessorConfig newBuildingInstance() {
            return new TimeSeriesSubmodelTemplateProcessorConfig();
        }
    }

}
