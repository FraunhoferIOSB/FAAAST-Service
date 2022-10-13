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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.environment.serializer;

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.EnvironmentSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SupportedDataformat;
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import io.adminshell.aas.v3.dataformat.aasx.InMemoryFile;
import io.adminshell.aas.v3.dataformat.i4aas.I4AASSerializer;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import java.nio.charset.Charset;
import java.util.Collection;


/**
 * OPC UA Nodeset serializer for {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}s and related
 * files.
 */
@SupportedDataformat(DataFormat.UANODESET)
public class OpcUaNodesetEnvironmentSerializer implements EnvironmentSerializer {

    private final I4AASSerializer serializer;

    public OpcUaNodesetEnvironmentSerializer() {
        this.serializer = new I4AASSerializer();
    }


    @Override
    public byte[] write(Charset charset, AssetAdministrationShellEnvironment environment, Collection<InMemoryFile> files) throws SerializationException {
        if (files != null && !files.isEmpty()) {
            throw new UnsupportedOperationException("serializing file content is not supported for data format OPC UA Nodeset");
        }
        try {
            return serializer.write(environment).getBytes(charset);
        }
        catch (io.adminshell.aas.v3.dataformat.SerializationException e) {
            throw new SerializationException("OPC UA Nodeset serialization failed", e);
        }
    }
}
