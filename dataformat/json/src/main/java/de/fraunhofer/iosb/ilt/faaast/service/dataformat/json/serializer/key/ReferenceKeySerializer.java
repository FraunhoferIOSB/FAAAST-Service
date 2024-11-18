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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.key;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.io.IOException;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Serializer for Reference objects when used as key in lists.
 */
public class ReferenceKeySerializer extends StdKeySerializers.StringKeySerializer {
    @Override
    public void serialize(Object key, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (key instanceof Reference reference) {
            gen.writeFieldName(ReferenceHelper.asString(reference));
        }
        else {
            super.serialize(key, gen, provider);
        }
    }
}
