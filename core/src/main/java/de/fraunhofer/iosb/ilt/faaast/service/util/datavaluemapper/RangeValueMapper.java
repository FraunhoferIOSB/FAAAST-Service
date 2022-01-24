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
package de.fraunhofer.iosb.ilt.faaast.service.util.datavaluemapper;

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.ValueFormatException;
import io.adminshell.aas.v3.model.Range;


public class RangeValueMapper extends DataValueMapper<Range, RangeValue> {

    @Override
    public RangeValue toValue(Range submodelElement) {
        if (submodelElement == null) {
            return null;
        }
        RangeValue rangeValue = new RangeValue();
        try {
            rangeValue.setMin(TypedValueFactory.create(submodelElement.getValueType(), submodelElement.getMin()));
            rangeValue.setMax(TypedValueFactory.create(submodelElement.getValueType(), submodelElement.getMax()));
        }
        catch (ValueFormatException ex) {
            // TODO properly throw?
            throw new RuntimeException("invalid data value");
        }
        return rangeValue;
    }


    @Override
    public Range setValue(Range submodelElement, RangeValue value) {
        if (submodelElement == null || value == null) {
            return null;
        }
        submodelElement.setValueType(value.getMin().getDataType().getName());
        submodelElement.setMin(value.getMin().asString());
        submodelElement.setMax(value.getMax().asString());
        return submodelElement;
    }

}
