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
import org.eclipse.digitaltwin.aas4j.v3.dataformat.xml.XmlDeserializer;


/**
 * XML deserializer for {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment}s and related files.
 */
@SupportedDataformat(DataFormat.XML)
public class XmlEnvironmentDeserializer implements EnvironmentDeserializer {

    private final XmlDeserializer deserializer;

    public XmlEnvironmentDeserializer() {
        this.deserializer = new XmlDeserializer();
    }


    @Override
    public EnvironmentContext read(InputStream in, Charset charset) throws DeserializationException {
        try {
            return EnvironmentContext.builder()
                    .environment(deserializer.read(in, charset))
                    .build();
        }
        catch (org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException e) {
            throw new DeserializationException("XML deserialization failed", e);
        }
    }
}
