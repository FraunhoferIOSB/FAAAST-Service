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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Generic helper functions to convert Date type into String type.
 */
public class DateStringHelper {

    private DateStringHelper() {}

    /**
     * Convert Date object into stringr.
     *
     * @param date the Date object to covert
     * @return the corresponding string representation in format ("yyyy-MM-dd'T'HH:mm:ss")
     * @throws IllegalArgumentException if the value is not a valid date
     */
    public static String formateToString (Date date) {

        // ? Which timeformat is required - both with milisec & timzone and without are consitent with the spec
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        return formatter.format(date);
        
    }

}
