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
package de.fraunhofer.iosb.ilt.faaast.service.serialization.core;

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Content;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public interface Serializer {

    public Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public String write(Object obj, Content content) throws SerializationException;


    public default String write(Object obj) throws SerializationException {
        return write(obj, Content.DEFAULT);
    }


    public default void write(OutputStream out, Object obj, Content content) throws IOException, SerializationException {
        write(out, DEFAULT_CHARSET, obj, content);
    }


    public default void write(OutputStream out, Object obj) throws IOException, SerializationException {
        write(out, obj, Content.DEFAULT);
    }


    public default void write(OutputStream out, Charset charset, Object obj, Content content) throws IOException, SerializationException {
        try (OutputStreamWriter writer = new OutputStreamWriter(out, charset)) {
            writer.write(write(obj, content));
        }
    }


    public default void write(OutputStream out, Charset charset, Object obj) throws IOException, SerializationException {
        write(out, charset, obj, Content.DEFAULT);
    }


    public default void write(File file, Charset charset, Object obj, Content content) throws FileNotFoundException, IOException, SerializationException {
        try (OutputStream out = new FileOutputStream(file)) {
            write(out, charset, obj, content);
        }
    }


    public default void write(File file, Charset charset, Object obj) throws FileNotFoundException, IOException, SerializationException {
        write(file, charset, obj, Content.DEFAULT);
    }


    public default void write(File file, Object obj, Content content) throws FileNotFoundException, IOException, SerializationException {
        write(file, DEFAULT_CHARSET, obj, content);
    }


    public default void write(File file, Object obj) throws FileNotFoundException, IOException, SerializationException {
        write(file, obj, Content.DEFAULT);
    }

}
