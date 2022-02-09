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

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeContext;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;


public interface Deserializer {

    private static String readStream(InputStream src, Charset charset) {
        return new BufferedReader(
                new InputStreamReader(src, charset))
                        .lines()
                        .collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Default charset that will be used when no charset is specified
     */
    public Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public <T> T read(String json, Class<T> type) throws DeserializationException;


    public default <T> T read(InputStream src, Class<T> type) throws DeserializationException {
        return read(src, DEFAULT_CHARSET, type);
    }


    public default <T> T read(InputStream src, Charset charset, Class<T> type) throws DeserializationException {
        return read(readStream(src, charset), type);
    }


    public default <T> T read(File file, Charset charset, Class<T> type)
            throws FileNotFoundException, DeserializationException {
        return read(new FileInputStream(file), charset, type);
    }


    public default <T> T read(File file, Class<T> type) throws FileNotFoundException, DeserializationException {
        return read(file, DEFAULT_CHARSET, type);
    }


    public <T> List<T> readList(String json, Class<T> type) throws DeserializationException;


    public default <T> List<T> readList(InputStream src, Class<T> type) throws DeserializationException {
        return readList(src, DEFAULT_CHARSET, type);
    }


    public default <T> List<T> readList(InputStream src, Charset charset, Class<T> type) throws DeserializationException {
        return readList(readStream(src, charset), type);
    }


    public default <T> List<T> readList(File file, Charset charset, Class<T> type)
            throws FileNotFoundException, DeserializationException {
        return readList(new FileInputStream(file), charset, type);
    }


    public default <T> List<T> readList(File file, Class<T> type) throws FileNotFoundException, DeserializationException {
        return readList(file, DEFAULT_CHARSET, type);
    }


    public <T extends ElementValue> T readValue(String json, TypeContext context) throws DeserializationException;


    public default <T extends ElementValue> T readValue(String json, SubmodelElement submodelElement) throws DeserializationException {
        return readValue(json, TypeContext.fromElement(submodelElement));
    }


    public default <T extends ElementValue> T readValue(String json, Class<T> type, Datatype datatype) throws DeserializationException {
        if (type == null) {
            throw new IllegalArgumentException("type must be non-null");
        }
        if (!ElementValue.class.isAssignableFrom(type)) {
            throw new DeserializationException(String.format("invalid type '%s' - must be subtype of SubmodelElement", type.getSimpleName()));
        }
        return readValue(json, TypeContext.builder()
                .rootInfo(TypeInfo.builder()
                        .valueType(type)
                        .datatype(datatype)
                        .build())
                .build());
    }


    public default <T extends ElementValue> T readValue(String json, Class<T> type) throws DeserializationException {
        return readValue(json, type, Datatype.String);
    }


    public default <T extends ElementValue> T readValue(InputStream src, Charset charset, TypeContext context) throws DeserializationException {
        return readValue(readStream(src, charset), context);
    }


    public default <T extends ElementValue> T readValue(InputStream src, Charset charset, SubmodelElement submodelElement) throws DeserializationException {
        return readValue(readStream(src, charset), submodelElement);
    }


    public default <T extends ElementValue> T readValue(InputStream src, TypeContext context) throws DeserializationException {
        return readValue(readStream(src, DEFAULT_CHARSET), context);
    }


    public default <T extends ElementValue> T readValue(InputStream src, SubmodelElement submodelElement) throws DeserializationException {
        return readValue(readStream(src, DEFAULT_CHARSET), submodelElement);
    }


    public default <T extends ElementValue> T readValue(InputStream src, Charset charset, Class<T> type) throws DeserializationException {
        return readValue(readStream(src, charset), type);
    }


    public default <T extends ElementValue> T readValue(InputStream src, Charset charset, Class<T> type, Datatype datatype) throws DeserializationException {
        return readValue(readStream(src, charset), type, datatype);
    }


    public default <T extends ElementValue> T readValue(InputStream src, Class<T> type) throws DeserializationException {
        return readValue(readStream(src, DEFAULT_CHARSET), type);
    }


    public default <T extends ElementValue> T readValue(InputStream src, Class<T> type, Datatype datatype) throws DeserializationException {
        return readValue(readStream(src, DEFAULT_CHARSET), type, datatype);
    }


    public default <T extends ElementValue> T readValue(File file, Charset charset, Class<T> type) throws DeserializationException, FileNotFoundException {
        return readValue(new FileInputStream(file), charset, type);
    }


    public default <T extends ElementValue> T readValue(File file, Charset charset, Class<T> type, Datatype datatype) throws DeserializationException, FileNotFoundException {
        return readValue(new FileInputStream(file), charset, type, datatype);
    }


    public default <T extends ElementValue> T readValue(File file, Class<T> type) throws DeserializationException, FileNotFoundException {
        return readValue(new FileInputStream(file), DEFAULT_CHARSET, type);
    }


    public default <T extends ElementValue> T readValue(File file, TypeContext context) throws DeserializationException, FileNotFoundException {
        return readValue(new FileInputStream(file), DEFAULT_CHARSET, context);
    }


    public default <T extends ElementValue> T readValue(File file, SubmodelElement submodelElement) throws DeserializationException, FileNotFoundException {
        return readValue(new FileInputStream(file), DEFAULT_CHARSET, submodelElement);
    }


    public default <T extends ElementValue> T readValue(File file, Class<T> type, Datatype datatype) throws DeserializationException, FileNotFoundException {
        return readValue(new FileInputStream(file), DEFAULT_CHARSET, type, datatype);
    }


    public <T> void useImplementation(Class<T> interfaceType, Class<? extends T> implementationType);

}
