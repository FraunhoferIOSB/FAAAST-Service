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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.ServiceDescription;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import java.io.IOException;
import java.util.Objects;


/**
 * Serializer for {@link de.fraunhofer.iosb.ilt.faaast.service.model.ServiceDescription}.
 */
public class ServiceDescriptionSerializer extends StdSerializer<ServiceDescription> {

    private static final String FIELD_PROFILES = "profiles";

    public ServiceDescriptionSerializer() {
        this(null);
    }


    public ServiceDescriptionSerializer(Class<ServiceDescription> t) {
        super(t);
    }


    @Override
    public void serialize(ServiceDescription value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeStartObject();
        generator.writeFieldName(FIELD_PROFILES);
        generator.writeStartArray();
        if (Objects.nonNull(value) && Objects.nonNull(value.getProfiles())) {
            value.getProfiles().forEach(LambdaExceptionHelper.rethrowConsumer(x -> generator.writeString(x.getId())));
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }
}
