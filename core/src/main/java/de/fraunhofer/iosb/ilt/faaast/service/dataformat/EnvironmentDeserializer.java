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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat;

import de.fraunhofer.iosb.ilt.faaast.service.model.EnvironmentContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * Deserializer that reads a {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} and related files.
 */
public interface EnvironmentDeserializer {

    public Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * reads a {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} and related files.
     *
     * @param in the InputStream to read
     * @param charset the charset to use
     * @return the read {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} and related files
     * @throws DeserializationException if deserialization fails
     */
    public EnvironmentContext read(InputStream in, Charset charset) throws DeserializationException;


    /**
     * reads a {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} and related files.
     *
     * @param in the InputStream to read
     * @return the read {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} and related files
     * @throws DeserializationException if deserialization fails
     */
    public default EnvironmentContext read(InputStream in) throws DeserializationException {
        return read(in, DEFAULT_CHARSET);
    }


    /**
     * reads a {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} and related files.
     *
     * @param file the File to read
     * @return the read {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} and related files
     * @throws DeserializationException if deserialization fails
     */
    public default EnvironmentContext read(File file) throws DeserializationException {
        return read(file, DEFAULT_CHARSET);
    }


    /**
     * reads a {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} and related files.
     *
     * @param file the File to read
     * @param charset the charset to use
     * @return the read {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} and related files
     * @throws DeserializationException if deserialization fails
     */
    public default EnvironmentContext read(File file, Charset charset) throws DeserializationException {
        try (InputStream in = new FileInputStream(file)) {
            return read(in, charset);
        }
        catch (IOException e) {
            throw new DeserializationException(String.format("error while deserializing - file not found (%s)", file), e);
        }
    }
}
