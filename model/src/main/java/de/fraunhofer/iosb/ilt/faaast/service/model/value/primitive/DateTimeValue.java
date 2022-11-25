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
package de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A datetime value.
 */
public class DateTimeValue extends TypedValue<ZonedDateTime> {

    public static final ZoneId DEFAULT_TIMEZONE = ZoneOffset.UTC;

    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeValue.class);

    public DateTimeValue() {
        super();
    }


    public DateTimeValue(ZonedDateTime value) {
        super(value);
    }


    @Override
    public void fromString(String value) throws ValueFormatException {
        if (StringUtils.isAllBlank(value)) {
            this.value = null;
            return;
        }
        try {
            this.value = ZonedDateTime.parse(value);
        }
        catch (DateTimeParseException e) {
            // If the string can't be parsed, we try to interpret it as UTC (if the time zone is missing)
            LOGGER.trace("fromString: parse with time zone failed, try to parse with the default time zone");
            try {
                this.value = LocalDateTime.parse(value).atZone(DEFAULT_TIMEZONE);
            }
            catch (DateTimeParseException e2) {
                LOGGER.warn("fromString: no valid DateTime: {}", value);
                throw new ValueFormatException("no valid DateTime", e2);
            }
        }
    }


    @Override
    public Datatype getDataType() {
        return Datatype.DATE_TIME;
    }

}
