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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.conversion;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;


/**
 * Converts values from OPC UA types to AAS types
 */
public interface OpcUaToAasValueConverter {

    /**
     * Converts a given OPC UA-based value to an AAS-based value.
     *
     * @param value OPC UA-based input value
     * @param targetType AAS-based target type
     * @return AAS-compliant value
     * @throws ValueConversionException if conversion fails
     */
    public TypedValue<?> convert(Variant value, Datatype targetType) throws ValueConversionException;
}
