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
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ExtendableSubmodel;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ExtendableSubmodelElementCollection;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ListWrapper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ValueWrapper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.Wrapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.ModellingKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.SubmodelBuilder;


/**
 * Represents a time series according to SMT TimeSeries.
 */
public class TimeSeries extends ExtendableSubmodel {

    @JsonIgnore
    private Wrapper<Metadata, SubmodelElementCollection> metadata = new ValueWrapper<>(
            submodelElements,
            new Metadata(),
            true,
            SubmodelElementCollection.class,
            x -> Objects.nonNull(x) ? x : new Metadata(),
            x -> Objects.equals(Constants.TIMESERIES_METADATA_ID_SHORT, x.getIdShort()),
            Metadata::of);

    @JsonIgnore
    private ListWrapper<Segment, SubmodelElementCollection> segments;

    @JsonIgnore
    ExtendableSubmodelElementCollection segmentsList;

    /**
     * Creates a new instance based on a {@link io.adminshell.aas.v3.model.Submodel}.
     *
     * @param submodel the {@link io.adminshell.aas.v3.model.Submodel} to parse
     * @return the parsed {@link io.adminshell.aas.v3.model.Submodel} as {@link TimeSeries}, or null if input is null
     */
    public static TimeSeries of(Submodel submodel) {
        TimeSeries result = new TimeSeries();
        Optional<SubmodelElementCollection> segments = submodel.getSubmodelElements().stream()
                .filter(Objects::nonNull)
                .filter(x -> SubmodelElementCollection.class.isAssignableFrom(x.getClass()))
                .map(SubmodelElementCollection.class::cast)
                .filter(x -> Objects.equals(x.getIdShort(), Constants.TIMESERIES_SEGMENTS_ID_SHORT))
                .findFirst();
        Submodel toParse = submodel;
        if (segments.isPresent()) {
            result.segmentsList = ExtendableSubmodelElementCollection.genericOf(result.segmentsList, segments.get());
            toParse = DeepCopyHelper.deepCopy(submodel, Submodel.class);
            toParse.setSubmodelElements(submodel.getSubmodelElements().stream()
                    .filter(x -> !Objects.equals(segments.get(), x))
                    .collect(Collectors.toList()));
        }
        return ExtendableSubmodel.genericOf(result, toParse);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (obj == null) {
            return false;
        }
        else if (this.getClass() != obj.getClass()) {
            return false;
        }
        return super.equals(obj);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }


    public TimeSeries() {
        withAdditionalValues(metadata);
        segmentsList = new ExtendableSubmodelElementCollection.Builder()
                .idShort(Constants.TIMESERIES_SEGMENTS_ID_SHORT)
                .build();
        segments = new ListWrapper<>(
                segmentsList.getValue(),
                new ArrayList<>(),
                SubmodelElementCollection.class,
                x -> x,
                x -> Constants.SEGMENTS_SEMANTIC_IDS.contains(x.getSemanticId()),
                Segment::of);
        segmentsList.withAdditionalValues(segments);
        submodelElements.add(segmentsList);
        this.idShort = Constants.TIMESERIES_SUBMODEL_ID_SHORT;
        this.kind = ModellingKind.INSTANCE;
        this.semanticId = ReferenceBuilder.global(Constants.TIMESERIES_SUBMODEL_SEMANTIC_ID);
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
        return segments.getValue();
    }


    /**
     * Sets the segments of this time seris.
     *
     * @param segments the segments to set
     */
    public void setSegments(List<Segment> segments) {
        this.segments.setValue(segments);
    }


    public Metadata getMetadata() {
        return metadata.getValue();
    }


    /**
     * Sets the metadata.
     *
     * @param metadata the metadata to set
     */
    public void setMetadata(Metadata metadata) {
        this.metadata.setValue(metadata);
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
