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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ExtendableSubmodelElementCollection;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ValueWrapper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.Wrapper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.util.TimeUnitHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.SubmodelElementCollectionBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;


/**
 * Represents a segment according to SMT TimeSeries.
 */
public abstract class Segment extends ExtendableSubmodelElementCollection {

    @JsonIgnore
    private Wrapper<String, Property> duration = new ValueWrapper<>( //TODO: depending on semantic id property in ISO 8601 (duration) format in string or as long
            value,
            null,
            false,
            Property.class,
            x -> {
                String valueType;
                try {
                    Long.parseLong(x);
                    valueType = Datatype.LONG.getName();
                }
                catch (NumberFormatException e) {
                    valueType = Datatype.STRING.getName();
                }
                return new DefaultProperty.Builder()
                        .idShort(Constants.SEGMENT_DURATION_ID_SHORT)
                        .semanticId(ReferenceBuilder.global(Constants.SEGMENT_DURATION_SEMANTIC_ID))
                        .valueType(Datatype.valueOf(valueType).getAas4jDatatype())
                        .value(x)
                        .build();
            },
            x -> Objects.equals(Constants.SEGMENT_DURATION_ID_SHORT, x.getIdShort()),
            Property::getValue);

    @JsonIgnore
    private Wrapper<ZonedDateTime, Property> lastUpdate = new ValueWrapper<>(
            value,
            null,
            true,
            Property.class,
            x -> Objects.nonNull(x) // TODO: calculatePropertiesIfNotPresent
                    ? new DefaultProperty.Builder()
                            .idShort(Constants.SEGMENT_LAST_UPDATE_ID_SHORT)
                            .semanticId(ReferenceBuilder.global(Constants.TIME_UTC))
                            .valueType(Datatype.DATE_TIME.getAas4jDatatype())
                            .value(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(x)) //TODO: get better default value or def calculation in subclass
                            .build()
                    : null,
            x -> Objects.equals(Constants.SEGMENT_LAST_UPDATE_ID_SHORT, x.getIdShort()),
            x -> ZonedDateTime.parse(x.getValue(), DateTimeFormatter.ISO_ZONED_DATE_TIME));

    @JsonIgnore
    private Wrapper<String, Property> state = new ValueWrapper<>(
            value,
            null,
            false,
            Property.class,
            x -> {
                String stateSemanticID;
                if (x != null) {
                    stateSemanticID = x.equalsIgnoreCase("completed") ? Constants.SEGMENT_STATE_COMPLETED_SEMANTIC_ID : Constants.SEGMENT_STATE_IN_PROGRESS_SEMANTIC_ID;
                }
                else {
                    stateSemanticID = Constants.SEGMENT_STATE_SEMANTIC_ID;
                }
                return new DefaultProperty.Builder()
                        .idShort(Constants.SEGMENT_STATE_ID_SHORT)
                        .semanticId(ReferenceBuilder.global(stateSemanticID))
                        .valueType(Datatype.STRING.getAas4jDatatype())
                        .value(x)
                        .build();
            },
            x -> Objects.equals(Constants.SEGMENT_STATE_ID_SHORT, x.getIdShort()),
            Property::getValue);

    @JsonIgnore
    private final Wrapper<Long, Property> recordCount = new ValueWrapper<>(
            value,
            null,
            true,
            Property.class,
            x -> Objects.nonNull(x) // TODO: calculatePropertiesIfNotPresent
                    ? new DefaultProperty.Builder()
                            .idShort(Constants.SEGMENT_RECORD_COUNT_ID_SHORT)
                            .valueType(Datatype.LONG.getAas4jDatatype())
                            .value(Long.toString(x)) //TODO: get better default value
                            .build()
                    : null,
            x -> Objects.equals(Constants.SEGMENT_RECORD_COUNT_ID_SHORT, x.getIdShort()),
            x -> Long.parseLong(x.getValue()));

    @JsonIgnore
    private Wrapper<ZonedDateTime, Property> end = new ValueWrapper<>(
            value,
            null,
            true,
            Property.class,
            x -> Objects.nonNull(x) // TODO: calculatePropertiesIfNotPresent
                    ? new DefaultProperty.Builder()
                            .idShort(Constants.SEGMENT_END_TIME_ID_SHORT)
                            .semanticId(ReferenceBuilder.global(Constants.TIME_UTC))
                            .valueType(Datatype.DATE_TIME.getAas4jDatatype())
                            .value(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(x))
                            .build()
                    : null,
            x -> Objects.equals(Constants.SEGMENT_END_TIME_ID_SHORT, x.getIdShort()),
            x -> ZonedDateTime.parse(x.getValue(), DateTimeFormatter.ISO_ZONED_DATE_TIME));

    @JsonIgnore
    private final Wrapper<IntervalWithUnit, Property> samplingInterval = new ValueWrapper<>(
            value,
            null,
            false,
            Property.class,
            x -> new DefaultProperty.Builder()
                    .idShort(Constants.SEGMENT_SAMPLING_INTERVAL_ID_SHORT)
                    .semanticId(Objects.nonNull(x.getUnit()) ? TimeUnitHelper.toSemanticId(x.getUnit()) : null)
                    .valueType(Datatype.LONG.getAas4jDatatype())
                    .value(Long.toString(x.getInterval()))
                    .build(),
            x -> Objects.equals(Constants.SEGMENT_SAMPLING_INTERVAL_ID_SHORT, x.getIdShort()),
            x -> new IntervalWithUnit(Long.parseLong(x.getValue()), TimeUnitHelper.fromSemanticId(x.getSemanticId())));

    @JsonIgnore
    private final Wrapper<IntervalWithUnit, Property> samplingRate = new ValueWrapper<>(
            value,
            null,
            false,
            Property.class,
            x -> new DefaultProperty.Builder()
                    .idShort(Constants.SEGMENT_SAMPLING_RATE_ID_SHORT)
                    .semanticId(Objects.nonNull(x.getUnit()) ? TimeUnitHelper.toSemanticId(x.getUnit()) : null)
                    .valueType(Datatype.LONG.getAas4jDatatype())
                    .value(Long.toString(x.getInterval()))
                    .build(),
            x -> Objects.equals(Constants.SEGMENT_SAMPLING_RATE_ID_SHORT, x.getIdShort()),
            x -> new IntervalWithUnit(Long.parseLong(x.getValue()), TimeUnitHelper.fromSemanticId(x.getSemanticId())));

    @JsonIgnore
    private Wrapper<ZonedDateTime, Property> start = new ValueWrapper<>(
            value,
            null,
            true,
            Property.class,
            x -> Objects.nonNull(x) // TODO: calculatePropertiesIfNotPresent
                    ? new DefaultProperty.Builder()
                            .idShort(Constants.SEGMENT_START_TIME_ID_SHORT)
                            .semanticId(ReferenceBuilder.global(Constants.TIME_UTC))
                            .valueType(Datatype.DATE_TIME.getAas4jDatatype())
                            .value(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(x))
                            .build()
                    : null,
            x -> Objects.equals(Constants.SEGMENT_START_TIME_ID_SHORT, x.getIdShort()),
            x -> ZonedDateTime.parse(x.getValue(), DateTimeFormatter.ISO_ZONED_DATE_TIME));

    protected Segment() {
        withAdditionalValues(recordCount, start, end, duration, samplingInterval, samplingRate, state, lastUpdate);
        this.idShort = IdHelper.randomId("Segment");
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
                    && Objects.equals(this.duration, other.duration)
                    && Objects.equals(this.lastUpdate, other.lastUpdate)
                    && Objects.equals(this.state, other.state)
                    && Objects.equals(this.start, other.start)
                    && Objects.equals(this.end, other.end)
                    && Objects.equals(this.samplingInterval, other.samplingInterval)
                    && Objects.equals(this.samplingRate, other.samplingRate)
                    && Objects.equals(this.recordCount, other.recordCount);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), duration, lastUpdate, state, start, end, samplingInterval, samplingRate, recordCount);
    }


    public String getState() {
        return state.getValue();
    }


    /**
     * Sets the state of this segment.
     *
     * @param state the state to set
     */
    public void setState(String state) {
        this.state.setValue(state);
    }


    public ZonedDateTime getLastUpdate() {
        return lastUpdate.getValue();
    }


    /**
     * Sets the timepoint of the last update of this segment.
     *
     * @param lastUpdate the last timepoint the segment was updated
     */
    public void setLastUpdate(ZonedDateTime lastUpdate) {
        this.lastUpdate.setValue(lastUpdate);
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


    public String getDuration() {
        return duration.getValue();
    }


    /**
     * Sets the duration of the segment.
     *
     * @param duration the duration to set
     */
    public void setDuration(String duration) {
        this.duration.setValue(duration);
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
        if (Objects.equals(ReferenceBuilder.global(Constants.INTERNAL_SEGMENT_SEMANTIC_ID), smc.getSemanticId())) {
            return InternalSegment.of(smc);
        }
        if (Objects.equals(ReferenceBuilder.global(Constants.LINKED_SEGMENT_SEMANTIC_ID), smc.getSemanticId())) {
            return LinkedSegment.of(smc);
        }
        if (Objects.equals(ReferenceBuilder.global(Constants.EXTERNAL_SEGMENT_SEMANTIC_ID), smc.getSemanticId())) {
            return ExternalSegment.of(smc);
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
        return ExtendableSubmodelElementCollection.genericOf(target, smc);
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


        public B start(ZonedDateTime value) {
            getBuildingInstance().setStart(value);
            return getSelf();
        }


        public B end(ZonedDateTime value) {
            getBuildingInstance().setEnd(value);
            return getSelf();
        }


        public B recordCount(long value) {
            getBuildingInstance().setRecordCount(value);
            return getSelf();
        }


        public B duration(String value) {
            getBuildingInstance().setDuration(value);
            return getSelf();
        }


        public B lastUpdate(ZonedDateTime value) {
            getBuildingInstance().setLastUpdate(value);
            return getSelf();
        }


        public B state(String value) {
            getBuildingInstance().setState(value);
            return getSelf();
        }

    }
}
