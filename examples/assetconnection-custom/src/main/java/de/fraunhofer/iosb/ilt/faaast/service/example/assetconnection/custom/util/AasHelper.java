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
package de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.util;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.ReflectionHelper;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


public class AasHelper {

    private AasHelper() {}


    public static Datatype getDatatype(Reference reference, ServiceContext serviceContext) throws ValueMappingException {
        TypeInfo typeInfo = serviceContext.getTypeInfo(reference);
        if (!ElementValueTypeInfo.class.isAssignableFrom(typeInfo.getClass())) {
            throw new IllegalArgumentException(String.format("type info does not provide datatype (type info: %s)", typeInfo.getClass()));
        }
        return ((ElementValueTypeInfo) typeInfo).getDatatype();
    }


    public static void ensureType(Reference reference, Class<?> type, ServiceContext serviceContext) {
        Referable element = EnvironmentHelper.resolve(reference, serviceContext.getAASEnvironment());
        if (element == null) {
            throw new IllegalArgumentException(String.format("element could not be resolved (reference: %s)", AasUtils.asString(reference)));
        }
        if (!type.isAssignableFrom(element.getClass())) {
            throw new IllegalArgumentException(String.format("unsupported element type (expected: %s, found: %s)",
                    type.getName(),
                    ReflectionHelper.getAasInterface(element.getClass()).getName()));
        }
    }
}
