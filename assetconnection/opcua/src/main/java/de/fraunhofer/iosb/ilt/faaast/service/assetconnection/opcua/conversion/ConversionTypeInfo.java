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
import java.util.Objects;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;


/**
 * Utility class to store conversion type information.
 */
public class ConversionTypeInfo {

    private final Datatype aasDatatype;
    private final NodeId opcUaDatatype;

    public ConversionTypeInfo(Datatype aasDatatype, NodeId opcUaDatatype) {
        this.aasDatatype = aasDatatype;
        this.opcUaDatatype = opcUaDatatype;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConversionTypeInfo that = (ConversionTypeInfo) o;
        return Objects.equals(aasDatatype, that.aasDatatype)
                && Objects.equals(opcUaDatatype, that.opcUaDatatype);
    }


    @Override
    public int hashCode() {
        return Objects.hash(aasDatatype, opcUaDatatype);
    }
}
