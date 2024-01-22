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

import jakarta.xml.bind.DatatypeConverter;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import org.apache.commons.lang3.StringUtils;


/**
 * A date value.
 */
public class DateValue extends TypedValue<OffsetDateTime> {

    private boolean isLocal = false;

    public DateValue() {
        super();
    }


    public DateValue(OffsetDateTime value) {
        super(value);
    }


    private static boolean isLocal(String value) {
        try {
            DateTimeFormatter.ISO_LOCAL_DATE.parse(value);
            return true;
        }
        catch (DateTimeParseException e) {
            return false;
        }
    }


    @Override
    public void fromString(String value) throws ValueFormatException {
        if (StringUtils.isAllBlank(value)) {
            this.setValue(null);
            return;
        }
        try {
            Calendar calendar = DatatypeConverter.parseDate(value);
            ZoneOffset offset = calendar.getTimeZone().toZoneId().getRules().getOffset(Instant.now());
            setValue(calendar.getTime().toInstant().atOffset(offset));
            isLocal = isLocal(value);
        }
        catch (IllegalArgumentException e) {
            throw new ValueFormatException(
                    String.format("unable to parse value (value: %s, type: %s)",
                            value, getDataType()),
                    e);
        }
    }


    @Override
    public String asString() {
        return DateTimeFormatter.ofPattern(isLocal ? "yyyy-MM-dd" : "yyyy-MM-ddXXX")
                .format(value);
    }


    @Override
    public Datatype getDataType() {
        return Datatype.DATE;
    }

}
