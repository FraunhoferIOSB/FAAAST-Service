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

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import io.adminshell.aas.v3.model.Reference;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for generating objects of type {@link TimeType} depending on a given semantic ID. Used for TimeSeries Submodel.
 */
public class TimeFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeFactory.class);

    private Map<String, Class<? extends TimeType>> supportedSemanticIDs = new HashMap<>(Map.of(
            Constants.TIME_UTC, UtcTime.class,
            Constants.TIME_TAI, TaiTime.class,
            Constants.TIME_RELATIVE_DURATION, RelativeTimeDuration.class,
            Constants.TIME_RELATIVE_POINT_IN_TIME, RelativePointInTime.class));

    private static TimeFactory timefactory; //TODO Enum singleton for threadsafe impl.?

    protected TimeFactory() {
        this.init();
    }


    /**
     * Get instance of the TimeFactory. First call of this method calls the init() method of this class to
     * create the list of supported semanticIDs.
     *
     * @return Current Instance of the {@link TimeFactory}.
     */
    public static TimeFactory getInstance() {
        if (timefactory == null) {
            timefactory = new TimeFactory();
        }
        return timefactory;
    }


    /**
     * Create a {@link TimeType} object depending on the given semantic ID, that contains the value given as timestamp.
     *
     * @param semanticID String containing the semantic ID determining the {@link TimeType} class to be created.
     * @param value String containing the time stamp.
     * @param valueType Optional {@link Datatype} name.
     * @return {@link TimeType} defined by the semanticID or {@link UnsupportedTime} if no matching class for semantic
     *         ID found.
     */
    public TimeType getTimeTypeFrom(String semanticID, String value, Optional<String> valueType) {
        for (Entry<String, Class<? extends TimeType>> entry: supportedSemanticIDs.entrySet()) {
            if (entry.getKey().equals(semanticID)) {
                return createInstanceOf(entry.getValue(), value, semanticID, valueType);
            }
        }
        LOGGER.info("TimeFactory: No class defined for semantic ID: {}", semanticID);
        return new UnsupportedTime(value, semanticID, valueType);
    }


    /**
     * Create a {@link TimeType} object depending on the given semantic ID, that contains the value given as timestamp.
     *
     * @param semanticID semantic ID {@link Reference} determining the {@link TimeType} class to be created.
     * @param value String containing the time stamp.
     * @param valueType Optional {@link Datatype} name.
     * @return {@link TimeType} defined by the semanticID or {@link UnsupportedTime} if no matching class for semantic
     *         ID found.
     */
    public TimeType getTimeTypeFrom(Reference semanticID, String value, Optional<String> valueType) {
        String stringID = semanticID.getKeys().get(0).getValue();
        return getTimeTypeFrom(stringID, value, valueType);
    }


    /**
     * Check, whether the TimeFactory has a class registered for the given semanticID.
     *
     * @param semanticID Semantic ID to test.
     * @return true if the TimeFactory has a class defined for the semantic ID.
     */
    public boolean hasClassFor(Reference semanticID) {
        //TODO if with init: create instance, init instance use instances supported IDs
        if (semanticID == null || semanticID.getKeys().isEmpty()) {
            return false;
        }
        String currentSemanticID = semanticID.getKeys().get(0).getValue();
        return supportedSemanticIDs.keySet().contains(currentSemanticID);
    }


    /**
     * Check for and register semanticID and class combinations, by searching for classes extending the {@link TimeType}
     * class in the package "de.fraunhofer.iosb.ilt.faaast.service".
     */
    public void init() {
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages("de.fraunhofer.iosb.ilt.faaast.service").scan()) { //TODO set which packages to accept
            ClassInfoList timeSubclasses = scanResult.getSubclasses(TimeType.class);
            for (ClassInfo classInfo: timeSubclasses) {
                Class<? extends TimeType> currentClass = (Class<? extends TimeType>) classInfo.loadClass();
                if (!this.supportedSemanticIDs.containsValue(currentClass)) { //TODO: check what if n classes classes for semanticID or n semanticIDs per class?
                    TimeType testType = createInstanceOf(currentClass, "00", "ToFind", Optional.empty()); //TODO: what if semanticID dependant on input?
                    if (testType instanceof UnsupportedTime) {
                        LOGGER.error("TimeFactory: Failed to add class {} to supported semantic IDs. Could not instantiate.", currentClass.getName());
                    }
                    else if (testType.getTimeSemanticID() == null || testType.getTimeSemanticID().equals("ToFind")) {
                        LOGGER.error(
                                "TimeFactory: Failed to add class {} to supported semantic IDs. Could not find or overwrote semanticID during init call of the class instance.",
                                currentClass.getName());
                    }
                    else {
                        this.supportedSemanticIDs.put(testType.getTimeSemanticID(), currentClass);
                    }
                }
            }
        }
    }


    private TimeType createInstanceOf(Class<? extends TimeType> timeClass, String value, String semanticID, Optional<String> valueType) {
        try {
            for (Constructor<?> constr: timeClass.getConstructors()) {
                if (constr.getParameterCount() == 1 && (constr.getParameterTypes()[0] == String.class)) {
                    return (TimeType) constr.newInstance(value);
                }
                else if (constr.getParameterCount() == 0) {
                    TimeType timeType = (TimeType) constr.newInstance();
                    String givenValueType = valueType.isPresent() ? valueType.get() : null;
                    timeType.init(value, semanticID, givenValueType);
                    return timeType;
                }
            }
            LOGGER.error("TimeFactory: No fitting constructor found in class [{}]. Should either have 1 String argument for the time value or none.",
                    timeClass.getName());
            return new UnsupportedTime(value, semanticID, valueType);
        }
        catch (InstantiationException e) {
            LOGGER.error("TimeFactory: Unable to instantiate class [{}].", timeClass.getName());
            return new UnsupportedTime(value, semanticID, valueType);
        }
        catch (IllegalAccessException e) {
            LOGGER.error("TimeFactory: Constructor of class [{}] not accessible.", timeClass.getName());
            return new UnsupportedTime(value, semanticID, valueType);
        }
        catch (IllegalArgumentException e) {
            LOGGER.error("TimeFactory: Constructor of class [{}] not in correct form. First parameter should be value as String.", timeClass.getName());
            return new UnsupportedTime(value, semanticID, valueType);
        }
        catch (InvocationTargetException e) {
            LOGGER.error("TimeFactory:Error constructing instance of class [{}]. Error: {}", timeClass.getName(), e.getMessage(), e);
            return new UnsupportedTime(value, semanticID, valueType);
        }
    }

}
