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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.util.List;
import java.util.Objects;


/**
 * Resolves type ids for event serialization; class name [...]EventMessage is equivalent to id [...]Event.
 */
public class EventMessageIdResolver extends TypeIdResolverBase {

    private static final String CLASS_SUFFIX = "Message";
    private JavaType superType;

    @Override
    public void init(JavaType baseType) {
        superType = baseType;
    }


    @Override
    public Id getMechanism() {
        return Id.NAME;
    }


    @Override
    public String idFromValue(Object obj) {
        return idFromValueAndType(obj, obj.getClass());
    }


    @Override
    public String idFromValueAndType(Object obj, Class<?> subType) {
        return removeSuffix(subType.getSimpleName());
    }


    private String removeSuffix(String name) {
        if (name.endsWith(CLASS_SUFFIX)) {
            return name.substring(0, name.length() - CLASS_SUFFIX.length());
        }
        return name;
    }


    private String appendSuffix(String name) {
        if (!name.endsWith(CLASS_SUFFIX)) {
            return name + CLASS_SUFFIX;
        }
        return name;
    }


    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
        String classname = appendSuffix(id);
        return context.constructSpecializedType(
                superType,
                getEventMessagesTypes().stream()
                        .filter(x -> Objects.equals(classname, x.getSimpleName()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(String.format(
                                "Unable to resolve id '%s' - not a valid event message type",
                                classname))));
    }


    private List<Class<EventMessage>> getEventMessagesTypes() {
        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages(EventMessage.class.getPackageName())
                .enableClassInfo().scan()) {
            return scanResult
                    .getSubclasses(EventMessage.class)
                    .filter(x -> !x.isAbstract())
                    .loadClasses(EventMessage.class);
        }
    }

}
