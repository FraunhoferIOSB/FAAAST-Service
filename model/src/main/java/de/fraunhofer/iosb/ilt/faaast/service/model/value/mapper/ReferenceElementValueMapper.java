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
package de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;


/**
 * Converts between {@link org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement} and
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue}.
 */
public class ReferenceElementValueMapper implements DataValueMapper<ReferenceElement, ReferenceElementValue> {

    @Override
    public ReferenceElementValue toValue(ReferenceElement submodelElement) {
        if (submodelElement == null) {
            return null;
        }
        ReferenceElementValue referenceElementValue = new ReferenceElementValue();
        referenceElementValue.setValue(submodelElement.getValue());
        return referenceElementValue;
    }


    @Override
    public ReferenceElement setValue(ReferenceElement submodelElement, ReferenceElementValue value) throws ValueMappingException {
        DataValueMapper.super.setValue(submodelElement, value);
        submodelElement.setValue(value.getValue());
        return submodelElement;
    }
}
