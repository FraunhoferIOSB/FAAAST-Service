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
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.LinkedSegmentProviderConfig;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Configuration for SMT TimeSeries Processor.
 */
public class TimeSeriesSubmodelTemplateProcessorConfig extends SubmodelTemplateProcessorConfig<TimeSeriesSubmodelTemplateProcessor> {

    private boolean useSegmentTimestamps;
    private List<LinkedSegmentProviderConfig> segmentProviders;

    public TimeSeriesSubmodelTemplateProcessorConfig() {
        this.segmentProviders = new ArrayList<>();
    }


    public boolean isUseSegmentTimestamps() {
        return useSegmentTimestamps;
    }


    public void setUseSegmentTimestamps(boolean useSegmentTimestamps) {
        this.useSegmentTimestamps = useSegmentTimestamps;
    }


    public List<LinkedSegmentProviderConfig> getSegmentProviders() {
        return segmentProviders;
    }


    public void setSegmentProviders(List<LinkedSegmentProviderConfig> segmentProviders) {
        this.segmentProviders = segmentProviders;
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
        return Objects.equals(useSegmentTimestamps, that.useSegmentTimestamps)
                && Objects.equals(segmentProviders, that.segmentProviders);
    }


    @Override
    public int hashCode() {
        return Objects.hash(useSegmentTimestamps, segmentProviders);
    }

    protected abstract static class AbstractBuilder<C extends TimeSeriesSubmodelTemplateProcessorConfig, B extends AbstractBuilder<C, B>>
            extends ExtendableBuilder<C, B> {

        public B useSegmentTimestamps(boolean value) {
            getBuildingInstance().setUseSegmentTimestamps(value);
            return getSelf();
        }


        public B withSegmentTimestamps() {
            getBuildingInstance().setUseSegmentTimestamps(true);
            return getSelf();
        }


        public B withoutSegmentTimestamps() {
            getBuildingInstance().setUseSegmentTimestamps(false);
            return getSelf();
        }


        public B segmentProvider(LinkedSegmentProviderConfig value) {
            getBuildingInstance().getSegmentProviders().add(value);
            return getSelf();
        }


        public B segmentProviders(List<LinkedSegmentProviderConfig> value) {
            getBuildingInstance().setSegmentProviders(value);
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
