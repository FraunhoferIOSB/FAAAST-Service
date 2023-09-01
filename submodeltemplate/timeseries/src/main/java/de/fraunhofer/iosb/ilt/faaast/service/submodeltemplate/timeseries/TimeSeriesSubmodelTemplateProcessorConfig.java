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
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.ExternalSegmentProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.LinkedSegmentProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.SegmentProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.aas.DefaultInternalSegmentProviderConfig;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Configuration for SMT TimeSeries Processor.
 */
public class TimeSeriesSubmodelTemplateProcessorConfig extends SubmodelTemplateProcessorConfig<TimeSeriesSubmodelTemplateProcessor> {

    private boolean useSegmentTimestamps;
    private List<LinkedSegmentProviderConfig> linkedSegmentProviders;
    private List<ExternalSegmentProviderConfig> externalSegmentProviders;
    private SegmentProviderConfig internalSegmentProvider;

    public TimeSeriesSubmodelTemplateProcessorConfig() {
        this.linkedSegmentProviders = new ArrayList<>();
        this.externalSegmentProviders = new ArrayList<>();
        this.internalSegmentProvider = new DefaultInternalSegmentProviderConfig();
    }


    public boolean isUseSegmentTimestamps() {
        return useSegmentTimestamps;
    }


    public void setUseSegmentTimestamps(boolean useSegmentTimestamps) {
        this.useSegmentTimestamps = useSegmentTimestamps;
    }


    public List<LinkedSegmentProviderConfig> getLinkedSegmentProviders() {
        return linkedSegmentProviders;
    }


    public void setLinkedSegmentProviders(List<LinkedSegmentProviderConfig> linkedSegmentProviders) {
        this.linkedSegmentProviders = linkedSegmentProviders;
    }


    public List<ExternalSegmentProviderConfig> getExternalSegmentProviders() {
        return externalSegmentProviders;
    }


    public void setExternalSegmentProviders(List<ExternalSegmentProviderConfig> externalSegmentProviders) {
        this.externalSegmentProviders = externalSegmentProviders;
    }


    public SegmentProviderConfig getInternalSegmentProvider() {
        return internalSegmentProvider;
    }


    public void setInternalSegmentProvider(SegmentProviderConfig internalSegmentProvider) {
        this.internalSegmentProvider = internalSegmentProvider;
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
                && Objects.equals(linkedSegmentProviders, that.linkedSegmentProviders)
                && Objects.equals(externalSegmentProviders, that.externalSegmentProviders)
                && Objects.equals(internalSegmentProvider, that.internalSegmentProvider);
    }


    @Override
    public int hashCode() {
        return Objects.hash(useSegmentTimestamps, linkedSegmentProviders, externalSegmentProviders, internalSegmentProvider);
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


        public B linkedSegmentProvider(LinkedSegmentProviderConfig value) {
            getBuildingInstance().getLinkedSegmentProviders().add(value);
            return getSelf();
        }


        public B linkedSegmentProvider(List<LinkedSegmentProviderConfig> value) {
            getBuildingInstance().setLinkedSegmentProviders(value);
            return getSelf();
        }


        public B externalSegmentProvider(ExternalSegmentProviderConfig value) {
            getBuildingInstance().getExternalSegmentProviders().add(value);
            return getSelf();
        }


        public B externalSegmentProvider(List<ExternalSegmentProviderConfig> value) {
            getBuildingInstance().setExternalSegmentProviders(value);
            return getSelf();
        }


        public B internalSegmentProvider(SegmentProviderConfig value) {
            getBuildingInstance().setInternalSegmentProvider(value);
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
