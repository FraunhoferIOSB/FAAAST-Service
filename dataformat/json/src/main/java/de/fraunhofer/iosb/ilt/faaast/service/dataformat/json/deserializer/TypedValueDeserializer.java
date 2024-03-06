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

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import java.io.IOException;


/**
 * Deserializer for {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue}.
 */
public class TypedValueDeserializer extends StdDeserializer<TypedValue> {

    public TypedValueDeserializer() {
        this(null);
    }


    public TypedValueDeserializer(Class<TypedValue> type) {
        super(type);
    }


    @Override
    public TypedValue deserialize(JsonParser parser, DeserializationContext context) throws IOException, JacksonException {
        TypeInfo typeInfo = ContextAwareElementValueDeserializer.getTypeInfo(context);
        if (typeInfo == null || !ElementValueTypeInfo.class.isAssignableFrom(typeInfo.getClass())) {
            throw new IllegalArgumentException("missing datatype information");
        }
        Datatype datatype = ((ElementValueTypeInfo) typeInfo).getDatatype();
        try {
            return TypedValueFactory.create(datatype, parser.getValueAsString());
        }
        catch (ValueFormatException e) {
            throw new IOException(String.format("error deserializing typed value (datatype: %s, value %s", datatype.getName(), parser.getValueAsString()), e);
        }
    }

}
