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

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValueFactory;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;


/**
 * Converts between {@link org.eclipse.digitaltwin.aas4j.v3.model.Range} and
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.RangeValue}.
 */
public class RangeValueMapper implements DataValueMapper<Range, RangeValue> {

    @Override
    public RangeValue toValue(Range submodelElement) throws ValueMappingException {
        if (submodelElement == null) {
            return null;
        }
        RangeValue rangeValue = new RangeValue();
        try {
            rangeValue.setMin(TypedValueFactory.create(submodelElement.getValueType(), submodelElement.getMin()));
            rangeValue.setMax(TypedValueFactory.create(submodelElement.getValueType(), submodelElement.getMax()));
        }
        catch (ValueFormatException e) {
            throw new ValueMappingException("invalid data value", e);
        }
        return rangeValue;
    }


    @Override
    public Range setValue(Range submodelElement, RangeValue value) throws ValueMappingException {
        DataValueMapper.super.setValue(submodelElement, value);
        if (value.getMin() != null && value.getMin().getValue() != null) {
            submodelElement.setValueType(value.getMin().getDataType().getAas4jDatatype());
            submodelElement.setMin(value.getMin().asString());
        }
        else {
            submodelElement.setMin(null);
        }
        if (value.getMax() != null && value.getMax().getValue() != null) {
            submodelElement.setValueType(value.getMax().getDataType().getAas4jDatatype());
            submodelElement.setMax(value.getMax().asString());
        }
        else {
            submodelElement.setMax(null);
        }
        return submodelElement;
    }

}
