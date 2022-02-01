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
package de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


public class TypedValueFactory {

    private TypedValueFactory() {}


    public static TypedValue<?> create(String datatypeName, String value) throws ValueFormatException {
        return create(Datatype.fromName(datatypeName), value);
    }


    public static TypedValue<?> create(Datatype datatype, String value) throws ValueFormatException {
        if (datatype == null) {
            throw new IllegalArgumentException("datatype most be non-null");
        }
        try {
            Constructor<? extends TypedValue> constructor = datatype.getImplementation().getConstructor();
            constructor.setAccessible(true);
            TypedValue<?> result = constructor.newInstance();
            result.fromString(value);
            return result;
        }
        catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(String.format("could not create typed value (datatype: %s, value: %s)", datatype.getName(), value, ex));
        }

    }

}
