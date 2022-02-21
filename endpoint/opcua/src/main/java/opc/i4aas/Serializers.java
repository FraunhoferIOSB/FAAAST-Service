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
package opc.i4aas;

import com.prosysopc.ua.StructureSerializer;
import com.prosysopc.ua.stack.encoding.DecodingException;
import com.prosysopc.ua.stack.encoding.EncodingException;
import com.prosysopc.ua.stack.encoding.IDecoder;
import com.prosysopc.ua.stack.encoding.IEncodeable;
import com.prosysopc.ua.stack.encoding.IEncoder;
import com.prosysopc.ua.stack.encoding.binary.IEncodeableSerializer;
import java.util.ArrayList;
import java.util.List;


/**
 * Generated on 2022-02-08 12:58:54
 */
public class Serializers {
    public static final IEncodeableSerializer[] SERIALIZERS;

    static {
        List<IEncodeableSerializer> l = new ArrayList<IEncodeableSerializer>();
        l.add(new AASKeyDataTypeSerializer());
        SERIALIZERS = l.toArray(new IEncodeableSerializer[0]);
    }

    public static class AASKeyDataTypeSerializer extends StructureSerializer {
        public AASKeyDataTypeSerializer() {
            super(AASKeyDataType.class, AASKeyDataType.BINARY, AASKeyDataType.XML);
        }


        @Override
        public void calcEncodeable(IEncodeable encodeable, IEncoder calculator) throws EncodingException {
            super.calcEncodeable(encodeable, calculator);
            AASKeyDataType obj = (AASKeyDataType) encodeable;
            calculator.put(null, (obj == null) ? null : obj.getType(), AASKeyElementsDataType.class);
            calculator.put(null, (obj == null) ? null : obj.getValue(), String.class);
            calculator.put(null, (obj == null) ? null : obj.getIdType(), AASKeyTypeDataType.class);
        }


        @Override
        public void getEncodeable(IDecoder decoder, IEncodeable encodeable) throws DecodingException {
            AASKeyDataType result = (AASKeyDataType) encodeable;
            super.getEncodeable(decoder, result);
            result.setType(decoder.get("Type", AASKeyElementsDataType.class));
            result.setValue(decoder.get("Value", String.class));
            result.setIdType(decoder.get("IdType", AASKeyTypeDataType.class));
        }


        @Override
        public IEncodeable newEncodeable() {
            return new AASKeyDataType();
        }


        @Override
        public void putEncodeable(IEncodeable encodeable, IEncoder encoder) throws EncodingException {
            super.putEncodeable(encodeable, encoder);
            AASKeyDataType obj = (AASKeyDataType) encodeable;
            encoder.put("Type", (obj == null) ? null : obj.getType(), AASKeyElementsDataType.class);
            encoder.put("Value", (obj == null) ? null : obj.getValue(), String.class);
            encoder.put("IdType", (obj == null) ? null : obj.getIdType(), AASKeyTypeDataType.class);
        }
    }
}
