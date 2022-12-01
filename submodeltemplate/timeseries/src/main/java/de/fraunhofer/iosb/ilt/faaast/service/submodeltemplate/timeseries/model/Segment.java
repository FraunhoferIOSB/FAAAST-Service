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
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.util.TimeUnitHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.AasHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.ModelingKind;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.builder.SubmodelElementCollectionBuilder;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


/**
 * Represents a segment according to SMT TimeSeries.
 */
public abstract class Segment extends DefaultSubmodelElementCollection {

    @JsonIgnore
    protected Optional<Long> recordCount;
    @JsonIgnore
    protected Optional<Long> samplingInterval;
    @JsonIgnore
    protected Optional<TimeUnit> samplingIntervalUnit;
    @JsonIgnore
    protected Optional<Long> samplingRate;
    @JsonIgnore
    protected Optional<TimeUnit> samplingRateUnit;
    @JsonIgnore
    protected String dataKind;
    @JsonIgnore
    protected List<Record> records;
    @JsonIgnore
    protected Optional<ZonedDateTime> start;
    @JsonIgnore
    protected Optional<ZonedDateTime> end;
    @JsonIgnore
    private boolean calculatePropertiesIfNotPresent;

    /**
     * Parses a given {@link io.adminshell.aas.v3.model.SubmodelElementCollection} into a {@link Segment}. Which
     * concrete {@link Segment} implementation will be used depends on the semanticId of the input.
     *
     * @param smc the {@link io.adminshell.aas.v3.model.SubmodelElementCollection} to parse
     * @return the parsed {@link io.adminshell.aas.v3.model.SubmodelElementCollection} as {@link Segment}, or null if
     *         input is null
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
     * Helper method to parse common elements of all segments from a given
     * {@link io.adminshell.aas.v3.model.SubmodelElementCollection}.
     *
     * @param <T> subtype of {@link Segment} expected
     * @param target target instance to update with parsed values
     * @param src {@link io.adminshell.aas.v3.model.SubmodelElementCollection} to parse
     */
    protected static <T extends Segment> void of(T target, SubmodelElementCollection src) {
        AasHelper.applyBasicProperties(src, target);
        for (var sme: src.getValues()) {
            if (Property.class.isAssignableFrom(sme.getClass())) {
                Property property = (Property) sme;
                switch (sme.getIdShort()) {
                    case Constants.SEGMENT_RECORD_COUNT_ID_SHORT: {
                        target.recordCount = Optional.ofNullable(Long.parseLong(property.getValue()));
                        break;
                    }
                    case Constants.SEGMENT_START_TIME_ID_SHORT: {
                        target.start = Optional.ofNullable(ZonedDateTime.parse(property.getValue()));
                        break;
                    }
                    case Constants.SEGMENT_END_TIME_ID_SHORT: {
                        target.end = Optional.ofNullable(ZonedDateTime.parse(property.getValue()));
                        break;
                    }
                    case Constants.SEGMENT_SAMPLING_INTERVAL_ID_SHORT: {
                        target.samplingInterval = Optional.of(Long.parseLong(property.getValue()));
                        target.samplingIntervalUnit = Optional.ofNullable(TimeUnitHelper.fromSemanticId(property.getSemanticId()));
                        break;
                    }
                    case Constants.SEGMENT_SAMPLING_RATE_ID_SHORT: {
                        target.samplingRate = Optional.of(Long.parseLong(property.getValue()));
                        target.samplingRateUnit = Optional.ofNullable(TimeUnitHelper.fromSemanticId(property.getSemanticId()));
                        break;
                    }
                    case Constants.SEGMENT_KIND_ID_SHORT: {
                        target.kind = ModelingKind.valueOf(property.getValue());
                        break;
                    }
                    default: {
                        target.values.add(sme);
                    }
                }
            }
            else {
                target.values.add(sme);
            }
        }
    }


    public Segment() {
        this.recordCount = Optional.empty();
        this.records = new ArrayList<>();
        this.start = Optional.empty();
        this.end = Optional.empty();
        this.samplingInterval = Optional.empty();
        this.samplingIntervalUnit = Optional.empty();
        this.samplingRate = Optional.empty();
        this.samplingRateUnit = Optional.empty();
        this.idShort = IdentifierHelper.randomId("Segment");
        this.calculatePropertiesIfNotPresent = true;
    }


    public Optional<Long> getSamplingInterval() {
        return samplingInterval;
    }


    public void setSamplingInterval(long samplingInterval) {
        this.samplingInterval = Optional.of(samplingInterval);
    }


    public long getRecordCount() {
        return recordCount.orElse(Long.valueOf(records.size()));
    }


    public void setRecordCount(long recordCount) {
        this.recordCount = Optional.ofNullable(recordCount);
    }


    public Optional<TimeUnit> getSamplingIntervalUnit() {
        return samplingIntervalUnit;
    }


    public void setSamplingIntervalUnit(TimeUnit samplingIntervalUnit) {
        this.samplingIntervalUnit = Optional.ofNullable(samplingIntervalUnit);
    }


    public Optional<Long> getSamplingRate() {
        return samplingRate;
    }


    public void setSamplingRate(long samplingRate) {
        this.samplingRate = Optional.of(samplingRate);
    }


    public Optional<TimeUnit> getSamplingRateUnit() {
        return samplingRateUnit;
    }


    public void setSamplingRateUnit(TimeUnit samplingRateUnit) {
        this.samplingRateUnit = Optional.ofNullable(samplingRateUnit);
    }


    public String getDataKind() {
        return dataKind;
    }


    public void setDataKind(String dataKind) {
        this.dataKind = dataKind;
    }


    public List<Record> getRecords() {
        return records;
    }


    public void setRecords(List<Record> records) {
        this.records = records;
    }


    public Optional<ZonedDateTime> getStart() {
        return start;
    }


    public void setStart(Optional<ZonedDateTime> start) {
        this.start = start;
    }


    public Optional<ZonedDateTime> getEnd() {
        return end;
    }


    public void setEnd(Optional<ZonedDateTime> end) {
        this.end = end;
    }


    public boolean getCalculatePropertiesIfNotPresent() {
        return calculatePropertiesIfNotPresent;
    }


    public void setCalculatePropertiesIfNotPresent(boolean calculatePropertiesIfNotPresent) {
        this.calculatePropertiesIfNotPresent = calculatePropertiesIfNotPresent;
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
            return super.equals(other)
                    // use getters as they may hide optional value
                    && Objects.equals(getRecordCount(), other.getRecordCount())
                    && Objects.equals(this.start, other.start)
                    && Objects.equals(this.end, other.end)
                    && Objects.equals(this.samplingInterval, other.samplingInterval)
                    && Objects.equals(this.samplingIntervalUnit, other.samplingIntervalUnit)
                    && Objects.equals(this.samplingRate, other.samplingRate)
                    && Objects.equals(this.samplingRateUnit, other.samplingRateUnit)
                    && Objects.equals(this.kind, other.kind)
                    && Objects.equals(this.records, other.records);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                getRecordCount(),
                this.start,
                this.end,
                this.samplingInterval,
                this.samplingIntervalUnit,
                this.samplingRate,
                this.samplingRateUnit,
                this.kind,
                this.records);
    }


    @Override
    public Collection<SubmodelElement> getValues() {
        List<SubmodelElement> result = new ArrayList<>();
        if (recordCount.isPresent() || calculatePropertiesIfNotPresent) {
            result.add(new DefaultProperty.Builder()
                    .idShort(Constants.SEGMENT_RECORD_COUNT_ID_SHORT)
                    .valueType(Datatype.LONG.getName())
                    .value(Long.toString(getRecordCount()))
                    .build());
        }
        if (start.isPresent() || (calculatePropertiesIfNotPresent && !records.isEmpty())) {
            result.add(new DefaultProperty.Builder()
                    .idShort(Constants.SEGMENT_START_TIME_ID_SHORT)
                    .semanticId(ReferenceHelper.globalReference(Constants.SEGMENT_START_TIME_ID_SHORT))
                    .valueType(Datatype.DATE_TIME.getName())
                    .value(Objects.toString(
                            start.orElse(records.stream()
                                    .map(x -> x.getTime())
                                    .min(new ZonedDateTimeComparator())
                                    .orElse(null))))
                    .build());
        }
        if (end.isPresent() || (calculatePropertiesIfNotPresent && !records.isEmpty())) {
            result.add(new DefaultProperty.Builder()
                    .idShort(Constants.SEGMENT_END_TIME_ID_SHORT)
                    .semanticId(ReferenceHelper.globalReference(Constants.SEGMENT_END_TIME_ID_SHORT))
                    .valueType(Datatype.DATE_TIME.getName())
                    .value(Objects.toString(
                            end.orElse(records.stream()
                                    .map(x -> x.getTime())
                                    .max(new ZonedDateTimeComparator())
                                    .orElse(null))))
                    .build());
        }
        if (samplingInterval.isPresent()) {
            result.add(new DefaultProperty.Builder()
                    .idShort(Constants.SEGMENT_SAMPLING_INTERVAL_ID_SHORT)
                    .semanticId(samplingIntervalUnit.isPresent() ? TimeUnitHelper.toSemanticId(samplingIntervalUnit.get()) : null)
                    .valueType(Datatype.LONG.getName())
                    .value(Long.toString(samplingInterval.get()))
                    .build());
        }
        if (samplingRate.isPresent()) {
            result.add(new DefaultProperty.Builder()
                    .idShort(Constants.SEGMENT_SAMPLING_RATE_ID_SHORT)
                    .semanticId(samplingIntervalUnit.isPresent() ? TimeUnitHelper.toSemanticId(samplingRateUnit.get()) : null)
                    .valueType(Datatype.LONG.getName())
                    .value(Long.toString(samplingRate.get()))
                    .build());
        }
        return result;
    }

    public abstract static class AbstractBuilder<T extends Segment, B extends AbstractBuilder<T, B>> extends SubmodelElementCollectionBuilder<T, B> {

        public B samplingInterval(long value) {
            getBuildingInstance().setSamplingInterval(value);
            return getSelf();
        }


        public B samplingRate(long value) {
            getBuildingInstance().setSamplingRate(value);
            return getSelf();
        }


        public B samplingRateUnit(TimeUnit value) {
            getBuildingInstance().setSamplingRateUnit(value);
            return getSelf();
        }


        public B samplingIntervalUnit(TimeUnit value) {
            getBuildingInstance().setSamplingIntervalUnit(value);
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
            getBuildingInstance().setStart(Optional.ofNullable(value));
            return getSelf();
        }


        public B end(ZonedDateTime value) {
            getBuildingInstance().setEnd(Optional.ofNullable(value));
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
