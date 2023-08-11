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
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for generating objects of type {@link TimeType} depending on a given semantic ID. Used for TimeSeries Submodel.
 */
public class TimeFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeFactory.class);

    public static Map<String, String> supported_semanticIds = Map.of(
            Constants.TIME_UTC, UtcTime.class.getName(),
            Constants.TIME_TAI, TaiTime.class.getName(),
            Constants.TIME_RELATIVE_DURATION, RelativeTimeDuration.class.getName(),
            Constants.TIME_RELATIVE_POINT_IN_TIME, RelativePointInTime.class.getName(),
            Constants.TIME_UNIX, UnixTime.class.getName());

    /**
     * Create a {@link TimeType} object depending on the given semantic ID, that contains the value given as timestamp.
     *
     * @param semanticID String containing the semantic ID determining the {@link TimeType} class to be created.
     * @param value String containing the time stamp.
     * @param valueType Optional {@link Datatype} name.
     * @return {@link TimeType} defined by the semanticID or {@link UnsupportedTime} if no matching class for semantic
     *         ID found.
     */
    public static TimeType getTimeTypeFrom(String semanticID, String value, Optional<String> valueType) {
        //TODO: implement matching strategies for semantic ids
        switch (semanticID) {
            case Constants.TIME_UTC:
                return new UtcTime(value);
            case Constants.TIME_UNIX:
                return new UnixTime(value);
            case Constants.TIME_TAI:
                return new TaiTime(value);
            case Constants.TIME_RELATIVE_POINT_IN_TIME:
                return new RelativePointInTime(value);
            case Constants.TIME_RELATIVE_DURATION:
                return new RelativeTimeDuration(value);
            default:
                return new UnsupportedTime(value, semanticID, valueType);
        }
        //        for (Entry<String, String> entry: supported_semanticIds.entrySet()) {
        //            if (entry.getKey().equals(semanticID)) {
        //                try {
        //                    Class<?> timeClass = Class.forName(entry.getValue());
        //                    if (timeClass.getSuperclass().equals(TimeType.class)) {//(timeClass.isAssignableFrom(TimeType.class)) {
        //                        for (Constructor<?> constr: timeClass.getConstructors()) {
        //                            if (constr.getParameterCount() == 1) {
        //                                return (TimeType) constr.newInstance(value);
        //                            }
        //                            else if (constr.getParameterCount() == 0) {
        //                                TimeType tt = (TimeType) constr.newInstance();
        //                                tt.init(value, semanticID, (valueType.isPresent() ? valueType.get() : null));
        //                                return tt;
        //                            }
        //                        }
        //                        LOGGER.error(String.format("TimeFactory: No fitting constructor found in class [%s]. Should either have 1 String argument for value or none.",
        //                                entry.getKey(), entry.getValue()));
        //                        return new UnsupportedTime(value, semanticID, valueType);
        //                    }
        //                    else {
        //                        LOGGER.error(String.format("TimeFactory: Class [%s] found for semanticID [%s] should extend TimeType class.", entry.getKey(), entry.getValue()));
        //                    }
        //                    return new UnsupportedTime(value, semanticID, valueType);
        //
        //                }
        //                catch (ClassNotFoundException e) {
        //                    LOGGER.error(String.format("TimeFactory: No class [%s] found for semanticID [%s].", entry.getKey(), entry.getValue()));
        //                    return new UnsupportedTime(value, semanticID, valueType);
        //                }
        //                catch (InstantiationException e) {
        //                    LOGGER.error(String.format("TimeFactory: Unable to instantiate class [%s].", entry.getValue()));
        //                    return new UnsupportedTime(value, semanticID, valueType);
        //                }
        //                catch (IllegalAccessException e) {
        //                    LOGGER.error(String.format("TimeFactory: Constructor of class [%s] not accessible.", entry.getValue()));
        //                    return new UnsupportedTime(value, semanticID, valueType);
        //                }
        //                catch (IllegalArgumentException e) {
        //                    LOGGER.error(String.format("TimeFactory: Constructor of class [%s] not in correct form. First parameter should be value as String.", entry.getValue()));
        //                    return new UnsupportedTime(value, semanticID, valueType);
        //                }
        //                catch (InvocationTargetException e) {
        //                    LOGGER.error(String.format("TimeFactory:Error constructing instance of class [%s]. Error: %s", entry.getValue(), e.getMessage()));
        //                    return new UnsupportedTime(value, semanticID, valueType);
        //                }
        //            }
        //        }
        //        LOGGER.info(String.format("TimeFactory: No class defined for semantic ID: %s", semanticID));
        //        return new UnsupportedTime(value, semanticID, valueType);
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
    public static TimeType getTimeTypeFrom(Reference semanticID, String value, Optional<String> valueType) {
        String stringID = semanticID.getKeys().get(0).getValue();
        return getTimeTypeFrom(stringID, value, valueType);
    }


    /**
     * Check, whether the TimeFactory has a class registered for the given semanticID.
     *
     * @param semanticID Semantic ID to test.
     * @return true if the TimeFactory has a class defined for the semantic ID.
     */
    public static boolean hasClassFor(Reference semanticID) {
        //TODO if with init: create instance, init instance use instances supported IDs
        if (semanticID == null || semanticID.getKeys().isEmpty()) {
            return false;
        }
        String currentSemanticID = semanticID.getKeys().get(0).getValue();
        return supported_semanticIds.keySet().contains(currentSemanticID);
    }


    /**
     * Check for and register new semanticID and class combinations.
     */
    public void init() {
        // TODO: register user defined time options to the already implemented ones --> other methods non-static! Singleton?
    }

}
