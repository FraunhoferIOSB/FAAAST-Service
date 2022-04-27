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
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.path.IdShortPathElementWalker;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;


/**
 * Serializer for content=path
 */
public class PathJsonSerializer {

    private final SerializerWrapper wrapper;

    public PathJsonSerializer() {
        this.wrapper = new SerializerWrapper(this::modifyMapper);
    }


    public JsonMapper getMapper() {
        return wrapper.getMapper();
    }


    public String write(Object obj) throws SerializationException {
        return write(obj, Level.DEFAULT);
    }


    /**
     * Serializes a given object with given level. If obj if not a AAS element
     * subject to serialization, result will be empty JSON array.
     *
     * @param obj object to serialize
     * @param level level of serialization
     * @return JSON array of all idShort paths subject to serialization
     *         according to specification.
     * @throws SerializationException if serialization fails
     */
    public String write(Object obj, Level level) throws SerializationException {
        IdShortPathElementWalker walker = new IdShortPathElementWalker(level);
        walker.walk(obj);
        try {
            return wrapper.getMapper().writeValueAsString(walker.getIdShortPaths());
        }
        catch (JsonProcessingException e) {
            throw new SerializationException("serialization failed", e);
        }
    }


    /**
     * Extension point. Override this method in subclasses to modify mapper.
     *
     * @param mapper current mapper
     * @return new mapper to use
     */
    protected JsonMapper modifyMapper(JsonMapper mapper) {
        return mapper;
    }

}
