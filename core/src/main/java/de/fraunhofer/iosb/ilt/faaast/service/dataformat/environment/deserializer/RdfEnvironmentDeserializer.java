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
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.apache.jena.riot.Lang;


/**
 * RDF deserializer for {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment}s and related files.
 */
@SupportedDataformat(DataFormat.RDF)
public class RdfEnvironmentDeserializer implements EnvironmentDeserializer {

    private static final String MSG_RDF_CURRENTLY_NOT_SUPPORTED = "Current version of AAS4j library does not support RDF de-/serialization";
    // private final Serializer deserializer;

    public RdfEnvironmentDeserializer() {
        // this.deserializer = new Serializer();
    }


    @Override
    public EnvironmentContext read(InputStream in, Charset charset) throws DeserializationException {
        throw new UnsupportedOperationException(MSG_RDF_CURRENTLY_NOT_SUPPORTED);
        // try {
        //     return EnvironmentContext.builder()
        //             .environment(deserializer.read(IOUtils.toString(in, charset)))
        //             .build();
        // }
        // catch (org.eclipse.digitaltwin.aas4j.v3.dataformat.DeserializationException | IOException e) {
        //     throw new DeserializationException("RDF deserialization failed", e);
        // }
    }


    /**
     * reads a {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} and related files.
     *
     * @param in the InputStream to read
     * @param charset the charset to use
     * @param rdfLanguage the RDF language to use
     * @return the read {@link EnvironmentContext}
     * @throws DeserializationException if deserialization fails
     */
    public EnvironmentContext read(InputStream in, Charset charset, Lang rdfLanguage) throws DeserializationException {
        throw new UnsupportedOperationException(MSG_RDF_CURRENTLY_NOT_SUPPORTED);
        // try {
        //     return EnvironmentContext.builder()
        //             .environment(deserializer.read(IOUtils.toString(in, charset), rdfLanguage))
        //             .build();
        // }
        // catch (org.eclipse.digitaltwin.aas4j.v3.dataformat.DeserializationException | IOException e) {
        //     throw new DeserializationException("RDF deserialization failed", e);
        // }
    }


    /**
     * reads a {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} and related files.
     *
     * @param file the file to read
     * @param charset the charset to use
     * @param rdfLanguage the RDF language to use
     * @return the read {@link EnvironmentContext}
     * @throws DeserializationException if deserialization fails
     */
    public EnvironmentContext read(File file, Charset charset, Lang rdfLanguage) throws DeserializationException {
        throw new UnsupportedOperationException(MSG_RDF_CURRENTLY_NOT_SUPPORTED);
        // try (InputStream in = new FileInputStream(file)) {
        //     return read(in, charset, rdfLanguage);
        // }
        // catch (IOException e) {
        //     throw new DeserializationException(String.format("error while deserializing - file not found (%s)", file), e);
        // }
    }


    /**
     * reads a {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} and related files.
     *
     * @param in the Inputstream to read
     * @param rdfLanguage the RDF language to use
     * @return the read {@link EnvironmentContext}
     * @throws DeserializationException if deserialization fails
     */
    public EnvironmentContext read(InputStream in, Lang rdfLanguage) throws DeserializationException {
        return read(in, DEFAULT_CHARSET, rdfLanguage);
    }


    /**
     * reads a {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment} and related files.
     *
     * @param file the File to read
     * @param rdfLanguage the RDF language to use
     * @return the read {@link EnvironmentContext}
     * @throws DeserializationException if deserialization fails
     */
    public EnvironmentContext read(File file, Lang rdfLanguage) throws DeserializationException {
        return read(file, DEFAULT_CHARSET, rdfLanguage);
    }
}
