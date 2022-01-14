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
package de.fraunhofer.iosb.ilt.faaast.service.serialization.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.deser.impl.PropertyValue;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Content;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.Serializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.mixins.PropertyValueMixin;


/**
 * @author jab
 */
public class JsonSerializer implements Serializer {

    private final SerializerWrapper wrapper;

    public JsonSerializer() {
        this.wrapper = new SerializerWrapper();
    }


    @Override
    public String write(Object obj, Content content) throws SerializationException {
        try {
            return wrapper.getMapper().writeValueAsString(obj);
        }
        catch (JsonProcessingException ex) {
            throw new SerializationException("serialization failed", ex);
        }
    }


    protected void modifyMapper(JsonMapper mapper) {
        mapper.addMixIn(PropertyValue.class, PropertyValueMixin.class);
    }

    private class SerializerWrapper extends io.adminshell.aas.v3.dataformat.json.JsonSerializer {

        @Override
        protected void buildMapper() {
            super.buildMapper();
            modifyMapper(mapper);
        }


        protected JsonMapper getMapper() {
            return mapper;
        }
    }

}
