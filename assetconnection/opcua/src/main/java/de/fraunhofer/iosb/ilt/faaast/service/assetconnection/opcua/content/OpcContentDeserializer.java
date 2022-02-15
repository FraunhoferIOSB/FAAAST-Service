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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.content;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.*;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeContext;
import java.math.BigDecimal;
import java.math.BigInteger;


public class OpcContentDeserializer implements ContentDeserializer {

    public OpcContentDeserializer() {

    }


    @Override
    public DataElementValue read(Object raw, TypeContext typeContext) throws AssetConnectionException {
        if (PropertyValue.class.isAssignableFrom(typeContext.getRootInfo().getValueType())) {
            PropertyValue result = new PropertyValue();
            //todo: need to use existing value mapper?
            switch (typeContext.getRootInfo().getDatatype().name()) {
                case "String":
                    result.setValue(new StringValue((String) raw));
                    break;
                case "Boolean":
                    result.setValue(new BooleanValue((Boolean) raw));
                    break;
                case "Decimal":
                    result.setValue(new DecimalValue((BigDecimal) raw));
                    break;
                case "Integer":
                    result.setValue(new IntegerValue((BigInteger) raw));
                    break;
                case "Double":
                    result.setValue(new DoubleValue((Double) raw));
                    break;
                case "Float":
                    result.setValue(new FloatValue((Float) raw));
                    break;
                case "Byte":
                    result.setValue(new ByteValue((Byte) raw));
                    break;
                case "Short":
                    result.setValue(new ShortValue((Short) raw));
                    break;
                case "Int":
                    result.setValue(new IntValue((Integer) raw));
                    break;
                case "Long":
                    result.setValue(new LongValue((Long) raw));
                    break;
            }
            return result;
        }
        throw new UnsupportedOperationException(String.format("error parsing value - unsupported element type (%s)", typeContext.getRootInfo().getValueType().getSimpleName()));
    }
}
