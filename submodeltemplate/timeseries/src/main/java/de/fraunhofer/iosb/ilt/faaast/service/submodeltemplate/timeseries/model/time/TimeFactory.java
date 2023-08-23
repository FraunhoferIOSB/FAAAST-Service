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
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.impl.RelativePointInTime;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.impl.RelativeTimeDuration;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.impl.TaiTime;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time.impl.UtcTime;
import io.adminshell.aas.v3.model.Reference;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
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
     * Check for and register semanticID and class combinations, by searching for classes extending the {@link Time}
     * class in the package "de.fraunhofer.iosb.ilt.faaast.service".
     */
    public static void init() {
        if (isInitialized) {
            return;
        }
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
            ClassInfoList timeSubclasses = scanResult.getClassesImplementing(Time.class).getStandardClasses();
            for (ClassInfo classInfo: timeSubclasses) {
                if (classInfo.isAbstract()) {
                    continue;
                }
                Class<? extends Time> currentClass = (Class<? extends Time>) classInfo.loadClass();
                if (!supportedSemanticIDs.containsValue(currentClass)) { //TODO: check what if n classes classes for semanticID or n semanticIDs per class?
                    Time testType = createUninitializedInstantOf(currentClass); //TODO: what if semanticID dependant on input?
                    if (testType == null) {
                        LOGGER.error("TimeFactory: Failed to add class {} to supported semantic IDs. Could not instantiate.", currentClass.getName());
                    }
                    else if (testType.getTimeSemanticID() == null) {
                        LOGGER.error(
                                "TimeFactory: Failed to add class {} to supported semantic IDs. Could not find semantic ID.",
                                currentClass.getName());
                    }
                    else {
                        supportedSemanticIDs.put(testType.getTimeSemanticID(), currentClass);
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
