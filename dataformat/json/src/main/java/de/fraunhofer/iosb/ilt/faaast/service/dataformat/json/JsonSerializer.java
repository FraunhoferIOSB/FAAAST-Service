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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.Serializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.EnumSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.ModifierAwareSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReflectionHelper;
import io.adminshell.aas.v3.dataformat.json.modeltype.ModelTypeProcessor;
import java.util.List;


/**
 * JSON serializer for FA³ST supporting different output modifier as defined by
 * specification.
 * <p>
 * Currently supports only content=value.
 */
public class JsonSerializer implements Serializer {

    private final PathJsonSerializer pathSerializer;
    private final ValueOnlyJsonSerializer valueOnlySerializer;
    private final SerializerWrapper wrapper;

    public JsonSerializer() {
        this.wrapper = new SerializerWrapper(this::modifyMapper);
        this.pathSerializer = new PathJsonSerializer();
        this.valueOnlySerializer = new ValueOnlyJsonSerializer();
    }


    /**
     * Modifies Jackson JsonMapper
     *
     * @param mapper mapper to modify
     */
    protected void modifyMapper(JsonMapper mapper) {
        SimpleModule module = new SimpleModule();
        ReflectionHelper.ENUMS.forEach(x -> module.addSerializer(x, new EnumSerializer()));
        mapper.registerModule(module);
    }


    @Override
    public String write(Object obj, OutputModifier modifier) throws SerializationException {
        if (modifier != null && modifier.getContent() == Content.VALUE) {
            return valueOnlySerializer.write(obj, modifier.getLevel(), modifier.getExtent());
        }
        if (modifier != null && modifier.getContent() == Content.PATH) {
            return pathSerializer.write(obj, modifier.getLevel());
        }
        if (obj != null && ElementValue.class.isAssignableFrom(obj.getClass())) {
            return valueOnlySerializer.write(obj, modifier.getLevel(), modifier.getExtent());
        }
        try {
            JsonMapper mapper = wrapper.getMapper();
            if (obj != null && List.class.isAssignableFrom(obj.getClass()) && !((List) obj).isEmpty()) {
                ObjectWriter objectWriter = mapper.writerFor(mapper.getTypeFactory()
                        .constructCollectionType(List.class, ((List<Object>) obj).get(0).getClass()))
                        .withAttribute(ModifierAwareSerializer.LEVEL, modifier);
                return mapper.writeValueAsString(ModelTypeProcessor.postprocess(
                        mapper.readTree(objectWriter.writeValueAsString(obj))));
            }
            else {
                return mapper.writer()
                        .withAttribute(ModifierAwareSerializer.LEVEL, modifier)
                        .writeValueAsString(ModelTypeProcessor.postprocess(wrapper.getMapper().valueToTree(obj)));
            }
        }
        catch (JsonProcessingException e) {
            throw new SerializationException("serialization failed", e);
        }
    }

}
