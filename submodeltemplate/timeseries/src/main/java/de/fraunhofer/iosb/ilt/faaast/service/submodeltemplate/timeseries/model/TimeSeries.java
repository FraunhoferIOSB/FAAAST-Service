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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.util.AasHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.builder.SubmodelBuilder;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Represents a time series according to SMT TimeSeries.
 */
public class TimeSeries extends DefaultSubmodel {

    @JsonIgnore
    private List<Segment> segments;
    @JsonIgnore
    private Metadata metadata;

    /**
     * Creates a new instance based on a {@link io.adminshell.aas.v3.model.Submodel}.
     *
     * @param submodel the {@link io.adminshell.aas.v3.model.Submodel} to parse
     * @return the parsed {@link io.adminshell.aas.v3.model.Submodel} as {@link TimeSeries}, or null if input is null
     */
    public static TimeSeries of(Submodel submodel) {
        if (submodel == null) {
            return null;
        }
        TimeSeries result = new TimeSeries();
        AasHelper.applyBasicProperties(submodel, result);
        List<SubmodelElement> elements = submodel.getSubmodelElements();
        SubmodelElementCollection segments = AasHelper.getElementByIdShort(elements, Constants.TIMESERIES_SEGMENTS_ID_SHORT, SubmodelElementCollection.class);
        if (segments != null) {
            result.segments = segments.getValues().stream()
                    .filter(Objects::nonNull)
                    .filter(x -> SubmodelElementCollection.class.isAssignableFrom(x.getClass()))
                    .map(x -> Segment.of((SubmodelElementCollection) x))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            elements.remove(segments);
        }
        SubmodelElementCollection metadataSMC = AasHelper.getElementByIdShort(elements, Constants.TIMESERIES_METADATA_ID_SHORT, SubmodelElementCollection.class);
        if (metadataSMC != null) {
            result.metadata = Metadata.of(metadataSMC);
            elements.remove(metadataSMC);
        }
        result.submodelElements = elements;
        return result;
    }


    public TimeSeries() {
        this.segments = new ArrayList<>();
        this.idShort = Constants.TIMESERIES_SUBMODEL_ID_SHORT;
        this.semanticId = ReferenceHelper.globalReference(Constants.TIMESERIES_SUBMODEL_SEMANTIC_ID);
    }


    /**
     * Gets all contained segments of given type.
     *
     * @param <T> the expected type
     * @param type the expected type
     * @return list of all contained segments of that type
     */
    public <T extends Segment> List<T> getSegments(Class<T> type) {
        return segments.stream()
                .filter(Objects::nonNull)
                .filter(x -> type.isAssignableFrom(x.getClass()))
                .map(x -> (T) x)
                .collect(Collectors.toList());
    }


    public List<Segment> getSegments() {
        return segments;
    }


    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }


    public Metadata getMetadata() {
        return metadata;
    }


    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }


    @Override
    public List<SubmodelElement> getSubmodelElements() {
        List<SubmodelElement> result = new ArrayList<>(super.getSubmodelElements());
        result.add(new DefaultSubmodelElementCollection.Builder()
                .idShort(Constants.TIMESERIES_SEGMENTS_ID_SHORT)
                .semanticId(ReferenceHelper.globalReference(Constants.SEGMENTS_SEMANTIC_ID))
                .values(segments.stream().map(x -> (SubmodelElement) x).collect(Collectors.toList()))
                .build());
        result.add(metadata);
        return result;
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends TimeSeries, B extends AbstractBuilder<T, B>> extends SubmodelBuilder<T, B> {

        public B segments(List<Segment> value) {
            getBuildingInstance().setSegments(value);
            return getSelf();
        }


        public B segment(Segment value) {
            getBuildingInstance().getSegments().add(value);
            return getSelf();
        }


        public B metadata(Metadata value) {
            getBuildingInstance().setMetadata(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<TimeSeries, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected TimeSeries newBuildingInstance() {
            return new TimeSeries();
        }

    }

}
