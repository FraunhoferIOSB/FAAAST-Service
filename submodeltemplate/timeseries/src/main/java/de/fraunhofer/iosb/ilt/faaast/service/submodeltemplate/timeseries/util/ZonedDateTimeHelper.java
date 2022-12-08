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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.util;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import java.time.ZonedDateTime;
import java.util.Optional;


/**
 * Collection of utility methods for {@link java.time.ZonedDateTime}.
 */
public class ZonedDateTimeHelper {

    /**
     * Parses a given string to {@link java.time.ZonedDateTime} if possible.
     *
     * @param value the value to parse
     * @return {@link java.util.Optional} of {@link java.time.ZonedDateTime} containing the result
     */
    public static Optional<ZonedDateTime> tryParse(String value) {
        try {
            return Optional.ofNullable((ZonedDateTime) TypedValueFactory.create(Datatype.DATE_TIME, value).getValue());
        }
        catch (ValueFormatException ex) {
            return Optional.empty();
        }
    }


    private ZonedDateTimeHelper() {

    }
}
