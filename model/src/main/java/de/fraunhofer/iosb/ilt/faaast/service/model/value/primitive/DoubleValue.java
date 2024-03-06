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
package de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import jakarta.xml.bind.DatatypeConverter;
import org.apache.commons.lang3.StringUtils;


/**
 * A double value.
 */
public class DoubleValue extends TypedValue<Double> {

    public DoubleValue() {
        super();
    }


    public DoubleValue(Double value) {
        super(value);
    }


    @Override
    public String asString() {
        return DatatypeConverter.printDouble(value);
    }


    @Override
    public void fromString(String value) throws ValueFormatException {
        if (StringUtils.isAllBlank(value)) {
            this.setValue(null);
            return;
        }
        try {
            this.setValue(DatatypeConverter.parseDouble(value));
        }
        catch (NumberFormatException e) {
            throw new ValueFormatException(e);
        }
    }


    @Override
    public Datatype getDataType() {
        return Datatype.DOUBLE;
    }

}
