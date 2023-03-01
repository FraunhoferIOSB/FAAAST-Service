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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.ZonedDateTimeComparator;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ExtendableSubmodelElementCollection;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ListWrapper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ValueWrapper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.Wrapper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.util.TimeUnitHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.builder.SubmodelElementCollectionBuilder;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Represents a segment according to SMT TimeSeries.
 */
public abstract class Segment extends ExtendableSubmodelElementCollection {

    @JsonIgnore
    private boolean calculatePropertiesIfNotPresent;

    @JsonIgnore
    private ListWrapper<Record, SubmodelElementCollection> records;

    @JsonIgnore
    ExtendableSubmodelElementCollection recordsList;

    @JsonIgnore
    private Wrapper<String, Property> dataKind = new ValueWrapper<>(
            values,
            null,
            false,
            Property.class,
            x -> new DefaultProperty.Builder()
                    .idShort(Constants.SEGMENT_KIND_ID_SHORT)
                    //.semanticId("???")
                    .valueType(Datatype.STRING.getName())
                    .value(x)
                    .build(),
            x -> Objects.equals(Constants.SEGMENT_KIND_ID_SHORT, x.getIdShort()),
            Property::getValue);

    @JsonIgnore
    private final Wrapper<Long, Property> recordCount = new ValueWrapper<>(
            values,
            null,
            true,
            Property.class,
            x -> Objects.nonNull(x) || calculatePropertiesIfNotPresent
                    ? new DefaultProperty.Builder()
                            .idShort(Constants.SEGMENT_RECORD_COUNT_ID_SHORT)
                            .valueType(Datatype.LONG.getName())
                            .value(Long.toString(Optional
                                    .ofNullable(x)
                                    .orElse(Long.valueOf(records.getValue().size()))))
                            .build()
                    : null,
            x -> Objects.equals(Constants.SEGMENT_RECORD_COUNT_ID_SHORT, x.getIdShort()),
            x -> Long.parseLong(x.getValue()));

    @JsonIgnore
    private Wrapper<ZonedDateTime, Property> end = new ValueWrapper<>(
            values,
            null,
            true,
            Property.class,
            x -> Objects.nonNull(x) || (calculatePropertiesIfNotPresent && !records.isEmpty())
                    ? new DefaultProperty.Builder()
                            .idShort(Constants.SEGMENT_END_TIME_ID_SHORT)
                            .semanticId(ReferenceHelper.globalReference(Constants.TIME_UTC))
                            .valueType(Datatype.DATE_TIME.getName())
                            .value(Objects.toString(
                                    Optional.ofNullable(x).orElse(records.stream()
                                            .map(Record::getTime)
                                            .max(new ZonedDateTimeComparator())
                                            .orElse(null))))
                            .build()
                    : null,
            x -> Objects.equals(Constants.SEGMENT_END_TIME_ID_SHORT, x.getIdShort()),
            x -> ZonedDateTime.parse(x.getValue()));

    @JsonIgnore
    private final Wrapper<IntervalWithUnit, Property> samplingInterval = new ValueWrapper<>(
            values,
            null,
            false,
            Property.class,
            x -> new DefaultProperty.Builder()
                    .idShort(Constants.SEGMENT_SAMPLING_INTERVAL_ID_SHORT)
                    .semanticId(Objects.nonNull(x.getUnit()) ? TimeUnitHelper.toSemanticId(x.getUnit()) : null)
                    .valueType(Datatype.LONG.getName())
                    .value(Long.toString(x.getInterval()))
                    .build(),
            x -> Objects.equals(Constants.SEGMENT_SAMPLING_INTERVAL_ID_SHORT, x.getIdShort()),
            x -> new IntervalWithUnit(Long.parseLong(x.getValue()), TimeUnitHelper.fromSemanticId(x.getSemanticId())));

    @JsonIgnore
    private final Wrapper<IntervalWithUnit, Property> samplingRate = new ValueWrapper<>(
            values,
            null,
            false,
            Property.class,
            x -> new DefaultProperty.Builder()
                    .idShort(Constants.SEGMENT_SAMPLING_RATE_ID_SHORT)
                    .semanticId(Objects.nonNull(x.getUnit()) ? TimeUnitHelper.toSemanticId(x.getUnit()) : null)
                    .valueType(Datatype.LONG.getName())
                    .value(Long.toString(x.getInterval()))
                    .build(),
            x -> Objects.equals(Constants.SEGMENT_SAMPLING_RATE_ID_SHORT, x.getIdShort()),
            x -> new IntervalWithUnit(Long.parseLong(x.getValue()), TimeUnitHelper.fromSemanticId(x.getSemanticId())));

    @JsonIgnore
    private Wrapper<ZonedDateTime, Property> start = new ValueWrapper<>(
            values,
            null,
            true,
            Property.class,
            x -> Objects.nonNull(x) || (calculatePropertiesIfNotPresent && !records.isEmpty())
                    ? new DefaultProperty.Builder()
                            .idShort(Constants.SEGMENT_START_TIME_ID_SHORT)
                            .semanticId(ReferenceHelper.globalReference(Constants.TIME_UTC))
                            .valueType(Datatype.DATE_TIME.getName())
                            .value(Objects.toString(
                                    Optional.ofNullable(x)
                                            .orElse(records.stream()
                                                    .map(Record::getTime)
                                                    .min(new ZonedDateTimeComparator())
                                                    .orElse(null))))
                            .build()
                    : null,
            x -> Objects.equals(Constants.SEGMENT_START_TIME_ID_SHORT, x.getIdShort()),
            x -> ZonedDateTime.parse(x.getValue()));

    protected Segment() {
        withAdditionalValues(dataKind, recordCount, start, end, samplingInterval, samplingRate);
        recordsList = new ExtendableSubmodelElementCollection.Builder()
                .idShort(Constants.INTERNAL_SEGMENT_RECORDS_ID_SHORT)
                .build();
        records = new ListWrapper<>(
                recordsList.getValues(),
                new ArrayList<>(),
                SubmodelElementCollection.class,
                x -> x,
                x -> Objects.equals(ReferenceHelper.globalReference(Constants.RECORD_SEMANTIC_ID), x.getSemanticId()),
                Record::of);
        recordsList.withAdditionalValues(records);
        values.add(recordsList);
        this.idShort = IdentifierHelper.randomId("Segment");
        this.calculatePropertiesIfNotPresent = false;
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
            Segment other = (Segment) obj;
            return super.equals(obj)
                    && Objects.equals(this.calculatePropertiesIfNotPresent, other.calculatePropertiesIfNotPresent)
                    && Objects.equals(this.dataKind, other.dataKind);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), calculatePropertiesIfNotPresent, dataKind);
    }


    public boolean getCalculatePropertiesIfNotPresent() {
        return calculatePropertiesIfNotPresent;
    }


    public void setCalculatePropertiesIfNotPresent(boolean calculatePropertiesIfNotPresent) {
        this.calculatePropertiesIfNotPresent = calculatePropertiesIfNotPresent;
    }


    public String getDataKind() {
        return dataKind.getValue();
    }


    /**
     * Sets the data kind..
     *
     * @param dataKind the data kind to set
     */
    public void setDataKind(String dataKind) {
        this.dataKind.setValue(dataKind);
    }


    public ZonedDateTime getEnd() {
        return end.getValue();
    }


    /**
     * Sets the end of this segment.
     *
     * @param end the end to set
     */
    public void setEnd(ZonedDateTime end) {
        this.end.setValue(end);
    }


    public Long getRecordCount() {
        return recordCount.getValue();
    }


    /**
     * Sets the record count.
     *
     * @param recordCount the record count to set
     */
    public void setRecordCount(long recordCount) {
        this.recordCount.setValue(recordCount);
    }


    public List<Record> getRecords() {
        return records.getValue();
    }


    /**
     * Sets the records of this segment.
     *
     * @param records the records to set
     */
    public void setRecords(List<Record> records) {
        this.records.setValue(records);
    }


    public IntervalWithUnit getSamplingInterval() {
        return samplingInterval.getValue();
    }


    /**
     * Sets the sampling interval.
     *
     * @param samplingInterval the sampling interval to set
     */
    public void setSamplingInterval(IntervalWithUnit samplingInterval) {
        this.samplingInterval.setValue(samplingInterval);
    }


    public IntervalWithUnit getSamplingRate() {
        return samplingRate.getValue();
    }


    /**
     * Sets the sampling rate.
     *
     * @param samplingRate the sampling rate to set
     */
    public void setSamplingRate(IntervalWithUnit samplingRate) {
        this.samplingRate.setValue(samplingRate);
    }


    public ZonedDateTime getStart() {
        return start.getValue();
    }


    /**
     * Sets the start of this segment.
     *
     * @param start the start to set
     */
    public void setStart(ZonedDateTime start) {
        this.start.setValue(start);
    }


    @Override
    public Collection<SubmodelElement> getValues() {
        updateCalculatedProperties();
        return super.getValues();
    }


    private void updateCalculatedProperties() {
        start.setValue(start.getValue());
        end.setValue(end.getValue());
        recordCount.setValue(recordCount.getValue());
    }


    /**
     * Parses a given {@link io.adminshell.aas.v3.model.SubmodelElementCollection} into a {@link Segment}. Which
     * concrete {@link Segment} implementation will be used depends on the semanticId of the input.
     *
     * @param smc the {@link io.adminshell.aas.v3.model.SubmodelElementCollection} to parse
     * @return the parsed {@link io.adminshell.aas.v3.model.SubmodelElementCollection} as {@link Segment}, or null if
     *         input is null
     * @throws IllegalArgumentException if segment type is not supported
     */
    public static Segment of(SubmodelElementCollection smc) {
        if (smc == null) {
            return null;
        }
        if (Objects.equals(ReferenceHelper.globalReference(Constants.INTERNAL_SEGMENT_SEMANTIC_ID), smc.getSemanticId())) {
            return InternalSegment.of(smc);
        }
        if (Objects.equals(ReferenceHelper.globalReference(Constants.LINKED_SEGMENT_SEMANTIC_ID), smc.getSemanticId())) {
            return LinkedSegment.of(smc);
        }
        throw new IllegalArgumentException(String.format("unsupported segment type (semanticId: %s)", AasUtils.asString(smc.getSemanticId())));
    }


    /**
     * Parses a given {@link SubmodelElementCollection} into a segment.
     *
     * @param <T> actual type of segment
     * @param target (empty) instance of actual segment type to parse into; this object will be modified by this method!
     * @param smc SubmodelElementCollection to parse
     * @return the modified {@code target}
     */
    protected static <T extends Segment> T of(T target, SubmodelElementCollection smc) {
        Optional<SubmodelElementCollection> records = smc.getValues().stream()
                .filter(Objects::nonNull)
                .filter(x -> SubmodelElementCollection.class.isAssignableFrom(x.getClass()))
                .map(SubmodelElementCollection.class::cast)
                .filter(x -> Objects.equals(x.getIdShort(), Constants.INTERNAL_SEGMENT_RECORDS_ID_SHORT))
                .findFirst();
        SubmodelElementCollection toParse = smc;
        if (records.isPresent()) {
            target.recordsList = ExtendableSubmodelElementCollection.genericOf(target.recordsList, records.get());
            toParse = DeepCopyHelper.deepCopy(smc, SubmodelElementCollection.class);
            toParse.setValues(smc.getValues().stream()
                    .filter(x -> !Objects.equals(records.get(), x))
                    .collect(Collectors.toList()));
        }
        return ExtendableSubmodelElementCollection.genericOf(target, toParse);
    }

    public abstract static class AbstractBuilder<T extends Segment, B extends AbstractBuilder<T, B>> extends SubmodelElementCollectionBuilder<T, B> {

        public B samplingInterval(IntervalWithUnit value) {
            getBuildingInstance().setSamplingInterval(value);
            return getSelf();
        }


        public B samplingRate(IntervalWithUnit value) {
            getBuildingInstance().setSamplingRate(value);
            return getSelf();
        }


        public B records(List<Record> value) {
            getBuildingInstance().setRecords(value);
            return getSelf();
        }


        public B record(Record value) {
            getBuildingInstance().getRecords().add(value);
            return getSelf();
        }


        public B start(ZonedDateTime value) {
            getBuildingInstance().setStart(value);
            return getSelf();
        }


        public B end(ZonedDateTime value) {
            getBuildingInstance().setEnd(value);
            return getSelf();
        }


        public B dataKind(String value) {
            getBuildingInstance().setDataKind(value);
            return getSelf();
        }


        public B calculatePropertiesIfNotPresent(boolean value) {
            getBuildingInstance().setCalculatePropertiesIfNotPresent(value);
            return getSelf();
        }


        public B dontCalculatePropertiesIfNotPresent() {
            getBuildingInstance().setCalculatePropertiesIfNotPresent(false);
            return getSelf();
        }
    }
}
