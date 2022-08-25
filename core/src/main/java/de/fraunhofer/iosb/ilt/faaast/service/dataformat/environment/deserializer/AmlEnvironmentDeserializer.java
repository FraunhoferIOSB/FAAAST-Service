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
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.EnvironmentContext;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.EnvironmentDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SupportedDataformat;
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import io.adminshell.aas.v3.dataformat.aml.AmlDeserializer;
import java.io.InputStream;
import java.nio.charset.Charset;


/**
 * AML deserializer for
 * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}s and
 * related files.
 */
@SupportedDataformat(DataFormat.AML)
public class AmlEnvironmentDeserializer implements EnvironmentDeserializer {

    private final AmlDeserializer deserializer;

    public AmlEnvironmentDeserializer() {
        this.deserializer = new AmlDeserializer();
    }


    @Override
    public EnvironmentContext read(InputStream in, Charset charset) throws DeserializationException {
        try {
            return EnvironmentContext.builder()
                    .environment(deserializer.read(in, charset))
                    .build();
        }
        catch (io.adminshell.aas.v3.dataformat.DeserializationException e) {
            throw new DeserializationException("AML deserialization failed", e);
        }
    }
}
