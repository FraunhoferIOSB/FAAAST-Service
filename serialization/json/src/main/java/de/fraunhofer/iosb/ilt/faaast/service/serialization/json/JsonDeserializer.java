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
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.Deserializer;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.mixins.PropertyValueMixin;
import java.util.List;


/**
 * @author jab
 */
public class JsonDeserializer implements Deserializer {

    private final DeserializerWrapper wrapper;

    public JsonDeserializer() {
        this.wrapper = new DeserializerWrapper();
    }


    @Override
    public <T> T read(String json, Class<T> type) throws DeserializationException {
        try {
            return wrapper.getMapper().readValue(json, type);
        }
        catch (JsonProcessingException ex) {
            throw new DeserializationException("deserialization failed", ex);
        }
    }


    @Override
    public <T> List<T> readList(String json, Class<T> type) throws DeserializationException {
        try {
            return wrapper.getMapper().readValue(json, wrapper.getMapper().getTypeFactory().constructCollectionType(List.class, type));
        }
        catch (JsonProcessingException ex) {
            throw new DeserializationException("deserialization failed", ex);
        }
    }


    @Override
    public <T> void useImplementation(Class<T> interfaceType, Class<? extends T> implementationType) {
        wrapper.useImplementation(interfaceType, implementationType);
    }


    protected void modifyMapper(JsonMapper mapper) {
        mapper.addMixIn(PropertyValue.class, PropertyValueMixin.class);
    }

    private class DeserializerWrapper extends io.adminshell.aas.v3.dataformat.json.JsonDeserializer {

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
