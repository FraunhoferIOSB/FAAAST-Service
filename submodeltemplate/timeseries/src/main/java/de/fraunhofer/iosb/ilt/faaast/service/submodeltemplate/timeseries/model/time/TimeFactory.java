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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time;

import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Metadata;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Timespan;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.impl.RelativePointInTime;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.impl.RelativeTimeDuration;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.impl.TaiTime;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.impl.UtcTime;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Qualifier;
import io.adminshell.aas.v3.model.Reference;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for generating objects of type {@link TimeType} depending on a given semantic ID. Used for TimeSeries Submodel.
 */
public class TimeFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeFactory.class);

    private static Map<String, Class<? extends Time>> supportedSemanticIDs = new HashMap<>(Map.of(
            Constants.TIME_UTC, UtcTime.class,
            Constants.TIME_TAI, TaiTime.class,
            Constants.TIME_RELATIVE_DURATION, RelativeTimeDuration.class,
            Constants.TIME_RELATIVE_POINT_IN_TIME, RelativePointInTime.class));

    private static boolean isInitialized = false;

    private TimeFactory() {}


    /**
     * Extract an object with {@link Time} interface from the given record or returns null, if there was none to be found.
     * Properties must have a semantic ID supported by the TimeFactory and should begin with "time" in their shortID.
     *
     * @param record {@link Record} whose Time property should be extracted
     * @return {@link Time} of the record or null, if none found
     */
    public static Time getTimeFrom(Record record) {
        init();
        Optional<Property> timeProperty = record.getTimesAndVariables().entrySet().stream()
                .filter(entr -> entr.getKey().toLowerCase().startsWith("time"))
                .filter(entr -> supportedSemanticIDs.containsKey(entr.getValue().getSemanticId().getKeys().get(0).getValue()))
                .map(entr -> entr.getValue())
                .findFirst();
        if (timeProperty.isEmpty()) {
            timeProperty = record.getTimesAndVariables().entrySet().stream()
                    .filter(entr -> supportedSemanticIDs.containsKey(entr.getValue().getSemanticId().getKeys().get(0).getValue()))
                    .map(entr -> entr.getValue())
                    .findFirst();
        }
        if (timeProperty.isPresent()) {
            Optional<Time> time = getTimeTypeFrom(timeProperty.get().getSemanticId(), timeProperty.get().getValue());
            return time.isPresent() ? time.get() : null;
        }
        else {
            return null;
        }
    }


    /**
     * Create timespan for the given record by finding a property within, which has a name beginning with "Time" and /
     * or has a semantic ID that is supported by the {@link TimeFactory}. Depending on whether the time is a relative
     * incremental time, a relative absolute, or absolute time, either the startTime of the segment, the end time of the
     * previous segment or neither is used, to calculate the timespan.
     *
     * @param record {@link Record} whose timespan is to be extracted
     * @param absoluteStarttime StartTime of the segment used for absolute releative times
     * @param previousEndtime End time of the previous record used for incremental relative times
     * @param metadata metadata of {@link Metadata} of the records used for checking for qualifiers
     * @return Timespan of the record or null, if no time property was found or was parseable.
     */
    public static Timespan getTimeFrom(Record record, ZonedDateTime absoluteStarttime, ZonedDateTime previousEndtime, Metadata metadata) {
        init();
        Optional<Property> timeProperty = record.getTimesAndVariables().entrySet().stream()
                .filter(entr -> entr.getKey().toLowerCase().startsWith("time"))
                .filter(entr -> supportedSemanticIDs.containsKey(entr.getValue().getSemanticId().getKeys().get(0).getValue()))
                .map(entr -> entr.getValue())
                .findFirst();
        if (timeProperty.isEmpty()) {
            timeProperty = record.getTimesAndVariables().entrySet().stream()
                    .filter(entr -> supportedSemanticIDs.containsKey(entr.getValue().getSemanticId().getKeys().get(0).getValue()))
                    .map(entr -> entr.getValue())
                    .findFirst();
        }
        if (timeProperty.isPresent()) {
            Optional<Time> time = getTimeTypeFrom(timeProperty.get().getSemanticId(), timeProperty.get().getValue());
            return time.isPresent() ? extractTimespan(time.get(), timeProperty.get().getIdShort(), absoluteStarttime, previousEndtime, metadata) : null;
        }
        return null;
    }


    private static Timespan extractTimespan(Time time, String propertyShortID, ZonedDateTime absoluteStarttime, ZonedDateTime previousEndtime, Metadata metadata) {
        try {
            if (time instanceof AbsoluteTime) {
                AbsoluteTime absolute = (AbsoluteTime) time;
                return new Timespan(absolute.getStartAsUtcTime(), absolute.getEndAsUtcTime());

            }
            else if (time instanceof RelativeTime) {
                RelativeTime relative = (RelativeTime) time;

                Optional<Qualifier> qualifier = metadata.getMetadataRecordVariables().get(propertyShortID).getQualifiers().stream()
                        .filter(constr -> constr instanceof Qualifier
                                && ((Qualifier) constr).getSemanticId().getKeys().get(0).getValue().startsWith(Constants.MEASUREMENT_MODEL_QUALIFIER_SEMANTIC_ID))
                        .map(constr -> (Qualifier) constr)
                        .findFirst();

                boolean isIncremental = qualifier.isPresent() ? qualifier.get().getValue().equals("incremental") : relative.isIncrementalToPrevious();
                if (isIncremental) {
                    return new Timespan(relative.getStartAsUtcTime(previousEndtime),
                            relative.getEndAsEpochMillis(previousEndtime));
                }
                else {
                    return new Timespan(relative.getStartAsUtcTime(absoluteStarttime),
                            relative.getEndAsEpochMillis(absoluteStarttime));
                }
            }
        }
        catch (MissingInitialisationException e) {
            LOGGER.warn("Error reading time with shortID {}. Either value not parseable or given relative startpoint {} or {} was null ", propertyShortID, absoluteStarttime,
                    previousEndtime);
        }
        return null;
    }


    /**
     * Create a {@link TimeType} object depending on the given semantic ID, that contains the value given as timestamp.
     *
     * @param semanticID String containing the semantic ID determining the {@link TimeType} class to be created.
     * @param value String containing the time stamp.
     * @return Object implementing {@link Time} defined by the semanticID or empty {@link Optional} if no matching class
     *         for semantic
     *         ID is found.
     */
    public static Optional<Time> getTimeTypeFrom(String semanticID, String value) {
        init();
        Class<? extends Time> timeClass = supportedSemanticIDs.get(semanticID);

        if (timeClass != null) {
            if (value == null) {
                return Optional.ofNullable(createUninitializedInstantOf(timeClass));
            }
            else {
                return Optional.ofNullable(createInstanceOf(timeClass, value));
            }
        }
        LOGGER.info("TimeFactory: No class defined for semantic ID: {}", semanticID);
        return Optional.empty();
    }


    /**
     * Create an object implementing {@link Time} depending on the given semantic ID, that contains the value given as
     * timestamp.
     *
     * @param semanticID semantic ID {@link Reference} determining the {@link TimeType} class to be created.
     * @param value String containing the time stamp.
     * @return Object implementing {@link Time} defined by the semanticID or empty {@link Optional} if no matching class
     *         for semanticID is found.
     */
    public static Optional<Time> getTimeTypeFrom(Reference semanticID, String value) {
        String stringID = semanticID.getKeys().get(0).getValue();
        return getTimeTypeFrom(stringID, value);
    }


    /**
     * Check, whether the TimeFactory has a class registered for the given semanticID.
     *
     * @param semanticID Semantic ID to test.
     * @return true if the TimeFactory has a class defined for the semantic ID.
     */
    public static boolean hasClassFor(Reference semanticID) {
        init();
        if (semanticID == null || semanticID.getKeys().isEmpty()) {
            return false;
        }
        String currentSemanticID = semanticID.getKeys().get(0).getValue();
        return supportedSemanticIDs.keySet().contains(currentSemanticID);
    }


    /**
     * Check, whether the TimeFactory has a class registered for the given semanticID and whether the registered class
     * can parse the value string. Also returns true, if there is a class for the semantic id, bur the value given is
     * null.
     *
     * @param semanticID Semantic ID to test.
     * @param value timestamp value to test.
     * @return true if there is a registered class and when this class can parse the value
     */
    public static boolean isParseable(Reference semanticID, String value) {
        init();
        if (hasClassFor(semanticID)) {
            if (value == null) {
                return true;
            }
            String currentSemanticID = semanticID.getKeys().get(0).getValue();
            Time testTime = createInstanceOf(supportedSemanticIDs.get(currentSemanticID), value);
            return testTime != null ? true : false;
        }
        return false;
    }


    /**
     * Given the class of an Object, return the semantic ID registered in the TimeFactory for this class.
     *
     * @param timeClass class extending {@link Time}
     * @return semanticID belonging to the class
     */
    public static String getSemanticIDForClass(Class<? extends Time> timeClass) {
        init();
        return supportedSemanticIDs.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue(), timeClass))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }


    /**
     * Check for and register semanticID and class combinations, by searching for classes extending the {@link Time}
     * class in the package "de.fraunhofer.iosb.ilt.faaast.service".
     */
    public static void init() {
        if (isInitialized) {
            return;
        }
        try (ScanResult scanResult = new ClassGraph().enableClassInfo().enableAnnotationInfo().scan()) {
            for (ClassInfo classInfo: scanResult.getClassesWithAnnotation(SupportedSemanticID.class)) {
                String semanticID = ((SupportedSemanticID) classInfo.getAnnotationInfo(SupportedSemanticID.class).loadClassAndInstantiate()).value();
                if (!supportedSemanticIDs.containsKey(semanticID)) {
                    if (classInfo.implementsInterface(Time.class)) {
                        Class<? extends Time> currentClass = (Class<? extends Time>) classInfo.loadClass();
                        supportedSemanticIDs.put(semanticID, currentClass);
                    }
                    else {
                        LOGGER.warn("TimeFactory: Class {} has SupportedSemanticID annotation but does not implement a Time interface", classInfo.getName());
                    }
                }
            }
        }
        isInitialized = true;
    }


    private static Time createInstanceOf(Class<? extends Time> timeClass, String value) {
        try {
            for (Constructor<?> constr: timeClass.getConstructors()) {
                if (constr.getParameterCount() == 0) {
                    Time timeType = (Time) constr.newInstance();
                    return timeType.init(value) ? timeType : null;
                }
            }
            LOGGER.error("TimeFactory: No fitting constructor found in class [{}]. Should either have 1 String argument for the time value or none.",
                    timeClass.getName());
            return null;
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.error("TimeFactory: Unable to instantiate class [{}].", timeClass.getName(), e);
            return null;
        }
    }


    private static Time createUninitializedInstantOf(Class<? extends Time> timeClass) {
        try {
            for (Constructor<?> constr: timeClass.getConstructors()) {
                if (constr.getParameterCount() == 0) {
                    Time timeType = (Time) constr.newInstance();
                    return timeType;
                }
            }
            LOGGER.error("TimeFactory: No fitting constructor found in class [{}]. Should have a constructor without arguments.",
                    timeClass.getName());
            return null;
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.error("TimeFactory: Unable to instantiate class [{}].", timeClass.getName(), e);
            return null;
        }
    }

}
