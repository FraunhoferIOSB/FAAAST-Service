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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.io.IOException;


/**
 * Deserializes an enum using the AAS naming strategy.
 *
 * @param <T> the enum type
 */
public class EnumDeserializer<T extends Enum> extends JsonDeserializer<T> {

    /**
     * Actual type of the enum to deserialize.
     */
    protected final Class<T> type;

    public EnumDeserializer(Class<T> type) {
        Ensure.requireNonNull(type, "type must be non-null");
        this.type = type;
    }


    @Override
    public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return (T) Enum.valueOf(type, org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.deserialization.EnumDeserializer.deserializeEnumName(parser.getText()));
    }
}
