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
package de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.util;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.concurrent.ThreadLocalRandom;


public class RandomValueGenerator {

    private RandomValueGenerator() {}


    public static Object generateRandomValue(Datatype datatype) {
        switch (datatype) {
            case BOOLEAN:
                return ThreadLocalRandom.current().nextBoolean();
            case STRING:
                // uses 'a' to 'z' with random length of 1 to 50 characters
                return ThreadLocalRandom.current().ints(97, 123)
                        .limit(ThreadLocalRandom.current().nextInt(49) + 1)
                        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                        .toString();
            case INT:
            case INTEGER:
                return ThreadLocalRandom.current().nextInt();
            case DECIMAL:
            case DOUBLE:
                return ThreadLocalRandom.current().nextDouble();
            case SHORT:
                return (short) ThreadLocalRandom.current().nextInt(Short.MAX_VALUE + 1);
            case LONG:
                return ThreadLocalRandom.current().nextLong();
            case BYTE:
                return (byte) (Math.random() * 255);
            case FLOAT:
                return ThreadLocalRandom.current().nextFloat();
            case DATE_TIME:
                return OffsetDateTime.ofInstant(
                        Instant.ofEpochMilli(ThreadLocalRandom.current().nextLong()),
                        ZoneId.of(ZoneId.getAvailableZoneIds().stream().skip(ThreadLocalRandom.current().nextLong(ZoneId.getAvailableZoneIds().size())).findFirst().get()));
            default:
                throw new IllegalArgumentException(String.format("unknown datatype: %s", datatype));
        }
    }
}
