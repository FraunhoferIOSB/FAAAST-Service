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

import io.adminshell.aas.v3.dataformat.aasx.InMemoryFile;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;


/**
 * Serializer that writes a
 * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment} and
 * related files.
 */
public interface EnvironmentSerializer {

    public Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * Writes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     * and related files
     *
     * @param charset the charset to use
     * @param environment the environment to serialize
     * @param files the files to serialize
     * @return serialized versin of input
     * @throws SerializationException if serialization fails
     */
    public byte[] write(Charset charset, AssetAdministrationShellEnvironment environment, Collection<InMemoryFile> files) throws SerializationException;


    /**
     * Writes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     * and related files
     *
     * @param context the context to serialize
     * @return serialized versin of input
     * @throws SerializationException if serialization fails
     */
    public default byte[] write(EnvironmentContext context) throws SerializationException {
        return write(context.getEnvironment(), context.getFiles());
    }


    /**
     * Writes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     * and related files to an {@link OutputStream}
     *
     * @param out the {@link OutputStream} to write to
     * @param context the context to serialize
     * @throws SerializationException if serialization fails
     * @throws java.io.IOException if writing on the stream fails
     */
    public default void write(OutputStream out, EnvironmentContext context) throws SerializationException, IOException {
        write(out, context.getEnvironment(), context.getFiles());
    }


    /**
     * Writes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     * and related files to an {@link OutputStream}
     *
     * @param file the {@link File} to write to
     * @param context the context to serialize
     * @throws SerializationException if serialization fails
     * @throws java.io.IOException if writing on the stream fails
     */
    public default void write(File file, EnvironmentContext context) throws SerializationException, IOException {
        write(file, context.getEnvironment(), context.getFiles());
    }


    /**
     * Serializes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     * and related files as byte[]
     *
     * @param environment the environment to serialize
     * @param files related files to serialize
     * @return input serialized as byte[]
     * @throws SerializationException if serialization fails
     */
    public default byte[] write(AssetAdministrationShellEnvironment environment, Collection<InMemoryFile> files) throws SerializationException {
        return write(DEFAULT_CHARSET, environment, files);
    }


    /**
     * Writes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     * and related files to an {@link OutputStream}
     *
     * @param out the {@link OutputStream} to write to
     * @param environment the {@link AssetAdministrationShellEnvironment} to
     *            serialize
     * @param files related files to serialize
     * @throws SerializationException if serialization fails
     * @throws java.io.IOException if writing on the stream fails
     */
    public default void write(OutputStream out, AssetAdministrationShellEnvironment environment, Collection<InMemoryFile> files) throws SerializationException, IOException {
        out.write(write(environment, files));
    }


    /**
     * Writes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     * and related files to a {@link File}
     *
     * @param file the {@link File} to write to
     * @param environment the {@link AssetAdministrationShellEnvironment} to
     *            serialize
     * @param files related files to serialize
     * @throws SerializationException if serialization fails
     * @throws java.io.IOException if writing on the stream fails
     */
    public default void write(File file, AssetAdministrationShellEnvironment environment, Collection<InMemoryFile> files) throws SerializationException, IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            write(out, environment, files);
        }
    }


    /**
     * Serializes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment} as
     * byte[]
     *
     * @param environment the environment to serialize
     * @return input serialized as byte[]
     * @throws SerializationException if serialization fails
     */
    public default byte[] write(AssetAdministrationShellEnvironment environment) throws SerializationException {
        return write(environment, List.of());
    }


    /**
     * Writes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     *
     * @param out the {@link OutputStream} to write to
     * @param environment {@link AssetAdministrationShellEnvironment} to write
     *            to
     * @throws SerializationException if serialization fails
     * @throws java.io.IOException if writing on the stream fails
     */
    public default void write(OutputStream out, AssetAdministrationShellEnvironment environment) throws SerializationException, IOException {
        write(out, environment, List.of());
    }


    /**
     * Writes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment} to
     * a {@link File}
     *
     * @param file the {@link File} to write to
     * @param environment the {@link AssetAdministrationShellEnvironment} to
     *            serialize
     * @throws SerializationException if serialization fails
     * @throws java.io.IOException if writing on the stream fails
     */
    public default void write(File file, AssetAdministrationShellEnvironment environment) throws SerializationException, IOException {
        write(file, environment, List.of());
    }


    /**
     * Serializes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     * and related files as byte[]
     *
     * @param context the context to serialize
     * @param charset the charset to use
     * @return input serialized as byte[]
     * @throws SerializationException if serialization fails
     */
    public default byte[] write(Charset charset, EnvironmentContext context) throws SerializationException {
        return write(charset, context.getEnvironment(), context.getFiles());
    }


    /**
     * Writes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     * and related files to an {@link OutputStream}
     *
     * @param out the {@link OutputStream} to write to
     * @param charset the charset to use
     * @param context the context to serialize
     * @throws SerializationException if serialization fails
     * @throws java.io.IOException if writing on the stream fails
     */
    public default void write(OutputStream out, Charset charset, EnvironmentContext context) throws SerializationException, IOException {
        write(out, charset, context.getEnvironment(), context.getFiles());
    }


    /**
     * Writes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     * and related files to an {@link OutputStream}
     *
     * @param file the {@link File} to write to
     * @param charset the charset to use
     * @param context the context to serialize
     * @throws SerializationException if serialization fails
     * @throws java.io.IOException if writing on the stream fails
     */
    public default void write(File file, Charset charset, EnvironmentContext context) throws SerializationException, IOException {
        write(file, charset, context.getEnvironment(), context.getFiles());
    }


    /**
     * Writes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     * and related files to an {@link OutputStream}
     *
     * @param out the {@link OutputStream} to write to
     * @param charset the charset to use
     * @param environment the {@link AssetAdministrationShellEnvironment} to
     *            serialize
     * @param files related files to serialize
     * @throws SerializationException if serialization fails
     * @throws java.io.IOException if writing on the stream fails
     */
    public default void write(OutputStream out, Charset charset, AssetAdministrationShellEnvironment environment, Collection<InMemoryFile> files)
            throws SerializationException, IOException {
        out.write(write(charset, environment, files));
    }


    /**
     * Writes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     * and related files to a {@link File}
     *
     * @param file the {@link File} to write to
     * @param charset the charset to use
     * @param environment the {@link AssetAdministrationShellEnvironment} to
     *            serialize
     * @param files related files to serialize
     * @throws SerializationException if serialization fails
     * @throws java.io.IOException if writing on the stream fails
     */
    public default void write(File file, Charset charset, AssetAdministrationShellEnvironment environment, Collection<InMemoryFile> files)
            throws SerializationException, IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            write(out, charset, environment, files);
        }
    }


    /**
     * Serializes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment} as
     * byte[]
     *
     * @param charset the charset to use
     * @param environment the environment to serialize
     * @return input serialized as byte[]
     * @throws SerializationException if serialization fails
     */
    public default byte[] write(Charset charset, AssetAdministrationShellEnvironment environment) throws SerializationException {
        return write(charset, environment, List.of());
    }


    /**
     * Writes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
     *
     * @param out the {@link OutputStream} to write to
     * @param charset the charset to use
     * @param environment {@link AssetAdministrationShellEnvironment} to write
     *            to
     * @throws SerializationException if serialization fails
     * @throws java.io.IOException if writing on the stream fails
     */
    public default void write(OutputStream out, Charset charset, AssetAdministrationShellEnvironment environment) throws SerializationException, IOException {
        write(out, charset, environment, List.of());
    }


    /**
     * Writes a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment} to
     * a {@link File}
     *
     * @param file the {@link File} to write to
     * @param charset the charset to use
     * @param environment the {@link AssetAdministrationShellEnvironment} to
     *            serialize
     * @throws SerializationException if serialization fails
     * @throws java.io.IOException if writing on the stream fails
     */
    public default void write(File file, Charset charset, AssetAdministrationShellEnvironment environment) throws SerializationException, IOException {
        write(file, charset, environment, List.of());
    }

}
