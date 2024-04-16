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
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.ZonedDateTimeComparator;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.AbsoluteTime;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.MissingInitialisationException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.Time;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.TimeFactory;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ExtendableSubmodelElementCollection;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ListWrapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;


/**
 * Represents an internal segment according to SMT TimeSeries.
 */
public class InternalSegment extends Segment {

    @JsonIgnore
    private boolean calculateProperties;

    @JsonIgnore
    private ListWrapper<Record, SubmodelElementCollection> records;

    @JsonIgnore
    ExtendableSubmodelElementCollection recordsList;

    public InternalSegment() {
        recordsList = new ExtendableSubmodelElementCollection.Builder()
                .idShort(Constants.INTERNAL_SEGMENT_RECORDS_ID_SHORT)
                .build();
        records = new ListWrapper<>(
                recordsList.getValue(),
                new ArrayList<>(),
                SubmodelElementCollection.class,
                x -> x,
                x -> Objects.equals(ReferenceBuilder.global(Constants.RECORD_SEMANTIC_ID), x.getSemanticId()),
                Record::of);
        recordsList.withAdditionalValues(records);
        value.add(recordsList);

        this.idShort = IdHelper.randomId("InternalSegment");
        this.semanticId = ReferenceBuilder.global(Constants.INTERNAL_SEGMENT_SEMANTIC_ID);

        if (getCalculateProperties() && !records.isEmpty()) {
            setDerivedProperties();
        }
    }


    /**
     * Creates a new instance based on a {@link io.adminshell.aas.v3.model.SubmodelElementCollection}.
     *
     * @param smc the {@link io.adminshell.aas.v3.model.SubmodelElementCollection} to parse
     * @return the parsed {@link io.adminshell.aas.v3.model.SubmodelElementCollection} as {@link InternalSegment}, or
     *         null if input is null
     */
    public static InternalSegment of(SubmodelElementCollection smc) {
        InternalSegment target = new InternalSegment();
        Optional<SubmodelElementCollection> smcRecords = smc.getValue().stream()
                .filter(Objects::nonNull)
                .filter(x -> SubmodelElementCollection.class.isAssignableFrom(x.getClass()))
                .map(SubmodelElementCollection.class::cast)
                .filter(x -> Objects.equals(x.getIdShort(), Constants.INTERNAL_SEGMENT_RECORDS_ID_SHORT))
                .findFirst();
        SubmodelElementCollection toParse = smc;
        if (smcRecords.isPresent()) {
            target.recordsList = ExtendableSubmodelElementCollection.genericOf(target.recordsList, smcRecords.get());
            toParse = DeepCopyHelper.deepCopy(smc, SubmodelElementCollection.class);
            toParse.setValue(smc.getValue().stream()
                    .filter(x -> !Objects.equals(smcRecords.get(), x))
                    .collect(Collectors.toList()));
        }
        return Segment.of(target, toParse);
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
        else {
            return super.equals(obj);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }


    /**
     * Sets the records of this segment.
     *
     * @param records the records to set
     */
    public void setRecords(List<Record> records) {
        this.records.setValue(records);
    }


    public List<Record> getRecords() {
        return records.getValue();
    }


    /**
     * Sets whether properties should be calculated.
     *
     * @param value the boolean value to set
     */
    public void setCalculateProperties(boolean value) {
        this.calculateProperties = value;
    }


    public boolean getCalculateProperties() {
        return calculateProperties;
    }


    @Override
    public List<SubmodelElement> getValue() {
        if (this.getCalculateProperties()) {
            this.setDerivedProperties();
        }
        return super.getValue();
    }


    private void setDerivedProperties() {
        this.setRecordCount(records.getValue().size());
        if (this.getRecordCount() == 0) {
            return;
        }
        this.setStart(records.stream().map(e -> getStartTime(e)).min(new ZonedDateTimeComparator()).orElse(null));
        this.setEnd(records.stream().map(e -> getEndTime(e)).max(new ZonedDateTimeComparator()).orElse(null));
    }


    private ZonedDateTime getStartTime(Record rec) {
        Time time = TimeFactory.getTimeFrom(rec);
        if (time instanceof AbsoluteTime) {
            try {
                return ((AbsoluteTime) time).getStartAsUtcTime();
            }
            catch (MissingInitialisationException e) {
                return null;
            }
        }
        else {
            return null;
        }
    }


    private ZonedDateTime getEndTime(Record rec) {
        Time time = TimeFactory.getTimeFrom(rec);
        if (time instanceof AbsoluteTime) {
            try {
                return ((AbsoluteTime) time).getEndAsUtcTime();
            }
            catch (MissingInitialisationException e) {
                return null;
            }
        }
        else {
            return null;
        }
    }


    public static Builder builder() {
        return new Builder();

    }

    public abstract static class AbstractBuilder<T extends InternalSegment, B extends AbstractBuilder<T, B>> extends Segment.AbstractBuilder<T, B> {

        public B records(List<Record> value) {
            getBuildingInstance().setRecords(value);
            return getSelf();
        }


        public B record(Record value) {
            getBuildingInstance().getRecords().add(value);
            return getSelf();
        }


        public B calculateProperties(boolean value) {
            getBuildingInstance().setCalculateProperties(value);
            return getSelf();
        }


        public B dontCalculateProperties() {
            getBuildingInstance().setCalculateProperties(false);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<InternalSegment, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected InternalSegment newBuildingInstance() {
            return new InternalSegment();
        }

    }
}
