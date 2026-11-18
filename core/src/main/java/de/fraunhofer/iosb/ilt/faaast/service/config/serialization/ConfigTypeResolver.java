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
package de.fraunhofer.iosb.ilt.faaast.service.config.serialization;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.common.reflect.TypeToken;
import de.fraunhofer.iosb.ilt.faaast.service.config.Config;
import de.fraunhofer.iosb.ilt.faaast.service.config.Configurable;
import de.fraunhofer.iosb.ilt.faaast.service.util.ImplementationManager;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.jsontype.impl.TypeIdResolverBase;


/**
 * TypeResolver to de-/serialize config files.
 */
public class ConfigTypeResolver extends TypeIdResolverBase {

    @Override
    public void init(JavaType baseType) {
        // intentionally left empty
    }


    @Override
    public String idFromValue(DatabindContext ctxt, Object value) throws JacksonException {
        TypeToken<? extends Object> type = TypeToken.of(value.getClass());
        if (type.isSubtypeOf(Config.class)) {
            TypeToken<?> resolvedType = type.resolveType(Config.class.getTypeParameters()[0]);
            return resolvedType.getRawType().getName();
        }
        return null;
    }


    @Override
    public String idFromValueAndType(DatabindContext ctxt, Object value, Class<?> suggestedType) throws JacksonException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public JavaType typeFromId(DatabindContext context, String id) throws JacksonException {
        try {
            Class<?> type = Class.forName(id, true, ImplementationManager.getClassLoader());
            if (!Configurable.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException(
                        String.format("class '%s' must implement interface '%s' to be used with the dynamic configuration feature", id, Configurable.class.getSimpleName()));
            }
            TypeToken<?> resolvedType = TypeToken.of(type).resolveType(Configurable.class.getTypeParameters()[0]);
            return context.constructType(resolvedType.getRawType());
        }
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(String.format("class '%s' not found in classplath", id), e);
        }
    }


    @Override
    public JsonTypeInfo.Id getMechanism() {
        return Id.CUSTOM;
    }

}
