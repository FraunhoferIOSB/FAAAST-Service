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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.managers.referable;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;


public class Util {

    public static Method getGetListMethod(Class clazz, Object parent) {
        for (Method m: parent.getClass().getMethods()) {
            Type type = m.getGenericReturnType();
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;

                if (pt.getActualTypeArguments().length == 1) {
                    Type t = pt.getActualTypeArguments()[0];
                    if (Arrays.stream(clazz.getInterfaces()).anyMatch(x -> x.getName().equalsIgnoreCase(t.getTypeName()))) {
                        return m;
                    }
                    if (t.getTypeName().equalsIgnoreCase(clazz.getName())) {
                        return m;
                    }
                }
            }
            else if (m.getGenericParameterTypes().length > 0 && m.getGenericParameterTypes()[0].getTypeName().equalsIgnoreCase(clazz.getName())) {
                return m;
            }
        }
        return null;
    }
}
