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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.environment.deserializer;

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.EnvironmentDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SupportedDataformat;
import de.fraunhofer.iosb.ilt.faaast.service.model.EnvironmentContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import java.io.InputStream;
import java.nio.charset.Charset;


/**
 * JSON-LD deserializer for {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment}s and related files.
 */
@SupportedDataformat(DataFormat.JSONLD)
public class JsonLDEnvironmentDeserializer implements EnvironmentDeserializer {

    //private final Serializer deserializer;

    public JsonLDEnvironmentDeserializer() {
        //this.deserializer = new Serializer();        
    }


    @Override
    public EnvironmentContext read(InputStream in, Charset charset) throws DeserializationException {
        throw new UnsupportedOperationException("Current version of AAS4j library does not support RDF/JSON-LD de-/serialization");
        // try {
        //     return EnvironmentContext.builder()
        //             .environment(deserializer.read(IOUtils.toString(in, charset), Lang.JSONLD))
        //             .build();
        // }
        // catch (org.eclipse.digitaltwin.aas4j.v3.dataformat.DeserializationException | IOException e) {
        //     throw new DeserializationException("JSON deserialization failed", e);
        // }
    }

}
