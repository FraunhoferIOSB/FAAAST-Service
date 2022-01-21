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
package de.fraunhofer.iosb.ilt.faaast.service.serialization.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.util.DataElementValueMapper;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.io.IOException;


public class SubmodelValueSerializer extends StdSerializer<Submodel> {

    public SubmodelValueSerializer() {
        this(null);
    }


    public SubmodelValueSerializer(Class<Submodel> type) {
        super(type);
    }


    @Override
    public void serialize(Submodel value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeStartObject();
        for (SubmodelElement element: value.getSubmodelElements()) {
            provider.defaultSerializeField(element.getIdShort(), DataElementValueMapper.toDataElement(element), generator);
        }
        generator.writeEndObject();
    }

}
