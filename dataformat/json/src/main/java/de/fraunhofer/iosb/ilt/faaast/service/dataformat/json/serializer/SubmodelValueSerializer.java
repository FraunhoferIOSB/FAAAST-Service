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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import java.io.IOException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.util.ReflectionHelper;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Serializer for {@link org.eclipse.digitaltwin.aas4j.v3.model.Submodel}. Serializes a submodel as map of idShort and
 * value of all
 * its elements.
 */
public class SubmodelValueSerializer extends StdSerializer<Submodel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmodelValueSerializer.class);

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
            if (ElementValueHelper.isValueOnlySupported(element)) {
                try {
                    provider.defaultSerializeField(element.getIdShort(), ElementValueMapper.toValue(element), generator);
                }
                catch (ValueMappingException e) {
                    provider.reportMappingProblem(e, "error mapping submodel element to value");
                }
            }
            else {
                LOGGER.trace("skipping element for value serialization as it is not supported (idShort: {}, entity type: {})",
                        element.getIdShort(),
                        ReflectionHelper.getModelType(element.getClass()));
            }
        }
        generator.writeEndObject();
    }

}
