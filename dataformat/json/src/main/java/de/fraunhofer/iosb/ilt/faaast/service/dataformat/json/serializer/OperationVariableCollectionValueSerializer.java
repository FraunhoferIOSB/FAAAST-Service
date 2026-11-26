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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import java.util.Collection;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;


/**
 * Serializer for a collection of {@link org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable}s.
 */
public class OperationVariableCollectionValueSerializer extends StdSerializer<Collection<OperationVariable>> {

    public OperationVariableCollectionValueSerializer() {
        this(null);
    }


    public OperationVariableCollectionValueSerializer(Class<Collection<OperationVariable>> type) {
        super(type);
    }


    @Override
    public void serialize(Collection<OperationVariable> values, JsonGenerator generator, SerializationContext context) {
        if (Objects.nonNull(values)) {
            generator.writeStartObject();
            for (var value: values) {
                if (Objects.nonNull(value.getValue())) {
                    try {
                        context.defaultSerializeProperty(value.getValue().getIdShort(), ElementValueMapper.toValue(value.getValue()), generator);
                    }
                    catch (ValueMappingException e) {
                        context.reportMappingProblem(e, "error serializing OperationVariable as valueOnly", generator);
                    }
                }
            }
            generator.writeEndObject();
        }
    }
}
