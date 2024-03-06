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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeExtractor;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * Deseriliazer for API calls. Deserializes not only whole {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment}
 * but also other elements like
 * {@link org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell},
 * {@link org.eclipse.digitaltwin.aas4j.v3.model.Submodel} or
 * {@link org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}.
 */
public interface ApiDeserializer {

    /**
     * Default charset that will be used when no charset is specified.
     */
    public Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * Deserializes a JSON string as provided type.
     *
     * @param <T> expected type
     * @param json JSON input string to deserialize
     * @param type expectect type
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T> T read(String json, Class<T> type) throws DeserializationException {
        return read(json, toTypeReference(type));
    }


    /**
     * Deserializes a JSON string as provided type.
     *
     * @param <T> expected type
     * @param json JSON input string to deserialize
     * @param type expectect type
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T> T read(String json, TypeReference<T> type) throws DeserializationException {
        return read(json, TypeFactory.defaultInstance().constructType(type));
    }


    /**
     * Deserializes a JSON string as provided type.
     *
     * @param <T> expected type
     * @param json JSON input string to deserialize
     * @param type expectect type
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public <T> T read(String json, JavaType type) throws DeserializationException;


    /**
     * Deserializes a JSON string from input stream as provided type using UTF-8 as charset.
     *
     * @param <T> expected type
     * @param src input stream containing JSON string
     * @param type expectect type
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T> T read(InputStream src, TypeReference<T> type) throws DeserializationException {
        return read(src, DEFAULT_CHARSET, type);
    }


    /**
     * Deserializes a JSON string from input stream as provided type using UTF-8 as charset.
     *
     * @param <T> expected type
     * @param src input stream containing JSON string
     * @param type expectect type
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T> T read(InputStream src, JavaType type) throws DeserializationException {
        return read(src, DEFAULT_CHARSET, type);
    }


    /**
     * Deserializes a JSON string from input stream as provided type using UTF-8 as charset.
     *
     * @param <T> expected type
     * @param src input stream containing JSON string
     * @param type expectect type
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T> T read(InputStream src, Class<T> type) throws DeserializationException {
        return read(src, DEFAULT_CHARSET, type);
    }


    /**
     * Deserializes a JSON string from input stream as provided type using provided charset.
     *
     * @param <T> expected type
     * @param src input stream containing JSON string
     * @param charset charset used in input stream
     * @param type expectect type
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T> T read(InputStream src, Charset charset, Class<T> type) throws DeserializationException {
        return read(readStream(src, charset), type);
    }


    /**
     * Deserializes a JSON string from input stream as provided type using provided charset.
     *
     * @param <T> expected type
     * @param src input stream containing JSON string
     * @param charset charset used in input stream
     * @param type expectect type
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T> T read(InputStream src, Charset charset, TypeReference<T> type) throws DeserializationException {
        return read(readStream(src, charset), type);
    }


    /**
     * Deserializes a JSON string from input stream as provided type using provided charset.
     *
     * @param <T> expected type
     * @param src input stream containing JSON string
     * @param charset charset used in input stream
     * @param type expectect type
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T> T read(InputStream src, Charset charset, JavaType type) throws DeserializationException {
        return read(readStream(src, charset), type);
    }


    /**
     * Deserializes a JSON string from file as provided type using provided charset.
     *
     * @param <T> expected type
     * @param file file containing JSON string
     * @param charset charset used in file
     * @param type expectect type
     * @return deserializes JSON object
     * @throws FileNotFoundException is file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T> T read(File file, Charset charset, Class<T> type) throws FileNotFoundException, DeserializationException {
        return read(new FileInputStream(file), charset, type);
    }


    /**
     * Deserializes a JSON string from file as provided type using provided charset.
     *
     * @param <T> expected type
     * @param file file containing JSON string
     * @param charset charset used in file
     * @param type expectect type
     * @return deserializes JSON object
     * @throws FileNotFoundException is file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T> T read(File file, Charset charset, TypeReference<T> type) throws FileNotFoundException, DeserializationException {
        return read(new FileInputStream(file), charset, type);
    }


    /**
     * Deserializes a JSON string from file as provided type using provided charset.
     *
     * @param <T> expected type
     * @param file file containing JSON string
     * @param charset charset used in file
     * @param type expectect type
     * @return deserializes JSON object
     * @throws FileNotFoundException is file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T> T read(File file, Charset charset, JavaType type) throws FileNotFoundException, DeserializationException {
        return read(new FileInputStream(file), charset, type);
    }


    /**
     * Deserializes a JSON string from file as provided type using UTF-8 charset.
     *
     * @param <T> expected type
     * @param file file containing JSON string
     * @param type expectect type
     * @return deserialized JSON object
     * @throws FileNotFoundException is file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T> T read(File file, Class<T> type) throws FileNotFoundException, DeserializationException {
        return read(file, DEFAULT_CHARSET, type);
    }


    /**
     * Deserializes a JSON string from file as provided type using UTF-8 charset.
     *
     * @param <T> expected type
     * @param file file containing JSON string
     * @param type expectect type
     * @return deserialized JSON object
     * @throws FileNotFoundException is file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T> T read(File file, TypeReference<T> type) throws FileNotFoundException, DeserializationException {
        return read(file, DEFAULT_CHARSET, type);
    }


    /**
     * Deserializes a JSON string from file as provided type using UTF-8 charset.
     *
     * @param <T> expected type
     * @param file file containing JSON string
     * @param type expectect type
     * @return deserialized JSON object
     * @throws FileNotFoundException is file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T> T read(File file, JavaType type) throws FileNotFoundException, DeserializationException {
        return read(file, DEFAULT_CHARSET, type);
    }


    /**
     * Deserializes a JSON string as list of provided type.
     *
     * @param <T> expected type
     * @param json JSON input string to deserialize
     * @param type expectect type
     * @return list of deserialized JSON objects
     * @throws DeserializationException if deserialization fails
     */
    public default <T> List<T> readList(String json, Class<T> type) throws DeserializationException {
        return readList(json, toTypeReference(type));
    }


    /**
     * Deserializes a JSON string as list of provided type.
     *
     * @param <T> expected type
     * @param json JSON input string to deserialize
     * @param type expectect type
     * @return list of deserialized JSON objects
     * @throws DeserializationException if deserialization fails
     */
    public default <T> List<T> readList(String json, TypeReference<T> type) throws DeserializationException {
        return readList(json, TypeFactory.defaultInstance().constructType(type));
    }


    /**
     * Deserializes a JSON string as list of provided type.
     *
     * @param <T> expected type
     * @param json JSON input string to deserialize
     * @param type expectect type
     * @return list of deserialized JSON objects
     * @throws DeserializationException if deserialization fails
     */
    public <T> List<T> readList(String json, JavaType type) throws DeserializationException;


    /**
     * Deserializes a JSON string from input stream as list of provided type using UTF-8 charset.
     *
     * @param <T> expected type
     * @param src input stream containing JSON string
     * @param type expectect type
     * @return list of deserialized JSON objects
     * @throws DeserializationException if deserialization fails
     */
    public default <T> List<T> readList(InputStream src, Class<T> type) throws DeserializationException {
        return readList(src, DEFAULT_CHARSET, type);
    }


    /**
     * Deserializes a JSON string from input stream as list of provided type using UTF-8 charset.
     *
     * @param <T> expected type
     * @param src input stream containing JSON string
     * @param type expectect type
     * @return list of deserialized JSON objects
     * @throws DeserializationException if deserialization fails
     */
    public default <T> List<T> readList(InputStream src, TypeReference<T> type) throws DeserializationException {
        return readList(src, DEFAULT_CHARSET, type);
    }


    /**
     * Deserializes a JSON string from input stream as list of provided type using UTF-8 charset.
     *
     * @param <T> expected type
     * @param src input stream containing JSON string
     * @param type expectect type
     * @return list of deserialized JSON objects
     * @throws DeserializationException if deserialization fails
     */
    public default <T> List<T> readList(InputStream src, JavaType type) throws DeserializationException {
        return readList(src, DEFAULT_CHARSET, type);
    }


    /**
     * Deserializes a JSON string from input stream as list of provided type using provided charset.
     *
     * @param <T> expected type
     * @param src input stream containing JSON string
     * @param charset charset used in input stream
     * @param type expectect type
     * @return list of deserialized JSON objects
     * @throws DeserializationException if deserialization fails
     */
    public default <T> List<T> readList(InputStream src, Charset charset, Class<T> type) throws DeserializationException {
        return readList(readStream(src, charset), type);
    }


    /**
     * Deserializes a JSON string from input stream as list of provided type using provided charset.
     *
     * @param <T> expected type
     * @param src input stream containing JSON string
     * @param charset charset used in input stream
     * @param type expectect type
     * @return list of deserialized JSON objects
     * @throws DeserializationException if deserialization fails
     */
    public default <T> List<T> readList(InputStream src, Charset charset, TypeReference<T> type) throws DeserializationException {
        return readList(readStream(src, charset), type);
    }


    /**
     * Deserializes a JSON string from input stream as list of provided type using provided charset.
     *
     * @param <T> expected type
     * @param src input stream containing JSON string
     * @param charset charset used in input stream
     * @param type expectect type
     * @return list of deserialized JSON objects
     * @throws DeserializationException if deserialization fails
     */
    public default <T> List<T> readList(InputStream src, Charset charset, JavaType type) throws DeserializationException {
        return readList(readStream(src, charset), type);
    }


    /**
     * Deserializes a JSON string from file as list of provided type using provided charset.
     *
     * @param <T> expected type
     * @param file file containing JSON string
     * @param charset charset used in input stream
     * @param type expectect type
     * @return list of deserialized JSON objects
     * @throws FileNotFoundException if file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T> List<T> readList(File file, Charset charset, Class<T> type) throws FileNotFoundException, DeserializationException {
        return readList(new FileInputStream(file), charset, type);
    }


    /**
     * Deserializes a JSON string from file as list of provided type using provided charset.
     *
     * @param <T> expected type
     * @param file file containing JSON string
     * @param charset charset used in input stream
     * @param type expectect type
     * @return list of deserialized JSON objects
     * @throws FileNotFoundException if file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T> List<T> readList(File file, Charset charset, TypeReference<T> type) throws FileNotFoundException, DeserializationException {
        return readList(new FileInputStream(file), charset, type);
    }


    /**
     * Deserializes a JSON string from file as list of provided type using provided charset.
     *
     * @param <T> expected type
     * @param file file containing JSON string
     * @param charset charset used in input stream
     * @param type expectect type
     * @return list of deserialized JSON objects
     * @throws FileNotFoundException if file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T> List<T> readList(File file, Charset charset, JavaType type) throws FileNotFoundException, DeserializationException {
        return readList(new FileInputStream(file), charset, type);
    }


    /**
     * Deserializes a JSON string from file as list of provided type using UTF-8 charset.
     *
     * @param <T> expected type
     * @param file file containing JSON string
     * @param type expectect type
     * @return list of deserialized JSON objects
     * @throws FileNotFoundException if file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T> List<T> readList(File file, Class<T> type) throws FileNotFoundException, DeserializationException {
        return readList(file, DEFAULT_CHARSET, type);
    }


    /**
     * Deserializes a JSON string from file as list of provided type using UTF-8 charset.
     *
     * @param <T> expected type
     * @param file file containing JSON string
     * @param type expectect type
     * @return list of deserialized JSON objects
     * @throws FileNotFoundException if file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T> List<T> readList(File file, TypeReference<T> type) throws FileNotFoundException, DeserializationException {
        return readList(file, DEFAULT_CHARSET, type);
    }


    /**
     * Deserializes a JSON string from file as list of provided type using UTF-8 charset.
     *
     * @param <T> expected type
     * @param file file containing JSON string
     * @param type expectect type
     * @return list of deserialized JSON objects
     * @throws FileNotFoundException if file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T> List<T> readList(File file, JavaType type) throws FileNotFoundException, DeserializationException {
        return readList(file, DEFAULT_CHARSET, type);
    }


    /**
     * Deserializes a JSON string as provided value type according to provided typeInfo.
     *
     * @param <T> expected value type
     * @param json JSON input string to deserialize
     * @param typeInfo detailed type information for deserialization
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public <T extends ElementValue> T readValue(String json, TypeInfo typeInfo) throws DeserializationException;


    /**
     * Deserializes a JSON string representing a {@link SubmodelElement} as provided value type according to provided
     * typeInfo.
     *
     * @param json JSON input string to deserialize
     * @param type type of the submodel element contained in JSON
     * @param typeInfo detailed type information for deserialization
     * @return deserialized JSON object
     * @throws DeserializationException if type does not have a value representation in AAS model
     *             * @throws DeserializationException if deserialization fails
     */
    public default ElementValue readValue(String json, Class<? extends SubmodelElement> type, TypeInfo typeInfo) throws DeserializationException {
        if (ElementValue.class.isAssignableFrom(type)) {
            return readValue(json, typeInfo);
        }
        if (!ElementValueHelper.isValueOnlySupported(type)) {
            throw new DeserializationException("not a value type");
        }
        try {
            return ElementValueMapper.toValue(read(json, type));
        }
        catch (ValueMappingException e) {
            throw new DeserializationException("deserialization failed because of invalid value mapping", e);
        }
    }


    /**
     * Deserializes a JSON string as provided value type according to provided typeInfo.
     *
     * @param <T> expected value type
     * @param json JSON input string to deserialize
     * @param submodelElement submodel element class to extract type information from
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T readValue(String json, SubmodelElement submodelElement) throws DeserializationException {
        return readValue(json, TypeExtractor.extractTypeInfo(submodelElement));
    }


    /**
     * Deserializes a JSON string as provided value type with given datatype.
     *
     * @param <T> expected value type
     * @param json JSON input string to deserialize
     * @param type expected value type
     * @param datatype datatype of the value
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T readValue(String json, Class<T> type, Datatype datatype) throws DeserializationException {
        if (type == null) {
            throw new IllegalArgumentException("type must be non-null");
        }
        if (!ElementValue.class.isAssignableFrom(type)) {
            throw new DeserializationException(String.format("invalid type '%s' - must be subtype of SubmodelElement", type.getSimpleName()));
        }
        return readValue(json, ElementValueTypeInfo.builder()
                .type(type)
                .datatype(datatype)
                .build());
    }


    /**
     * Deserializes a JSON string as provided value type with default datatype (string).
     *
     * @param <T> expected value type
     * @param json JSON input string to deserialize
     * @param type expected value type
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T readValue(String json, Class<T> type) throws DeserializationException {
        return readValue(json, type, Datatype.STRING);
    }


    /**
     * Deserializes a JSON string from input stream as provided value type according to provided typeInfo.
     *
     * @param <T> expected value type
     * @param src input stream containing JSON string
     * @param charset charset used in input stream
     * @param typeInfo detailed type information for deserialization
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T readValue(InputStream src, Charset charset, TypeInfo typeInfo) throws DeserializationException {
        return readValue(readStream(src, charset), typeInfo);
    }


    /**
     * Deserializes a JSON string from input stream as provided value type according to provided typeInfo.
     *
     * @param <T> expected value type
     * @param src input stream containing JSON string
     * @param charset charset used in input stream
     * @param submodelElement submodel element class to extract type information from
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T readValue(InputStream src, Charset charset, SubmodelElement submodelElement) throws DeserializationException {
        return readValue(readStream(src, charset), submodelElement);
    }


    /**
     * Deserializes a JSON string from input stream as provided value type according to provided typeInfo.
     *
     * @param <T> expected value type
     * @param src input stream containing JSON string
     * @param typeInfo detailed type information for deserialization
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T readValue(InputStream src, TypeInfo typeInfo) throws DeserializationException {
        return readValue(readStream(src, DEFAULT_CHARSET), typeInfo);
    }


    /**
     * Deserializes a JSON string from input stream as provided value type.
     *
     * @param <T> expected value type
     * @param src input stream containing JSON string
     * @param submodelElement submodel element class to extract type information from
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T readValue(InputStream src, SubmodelElement submodelElement) throws DeserializationException {
        return readValue(readStream(src, DEFAULT_CHARSET), submodelElement);
    }


    /**
     * Deserializes a JSON string from input stream as provided value type with default datatype (string).
     *
     * @param <T> expected value type
     * @param src input stream containing JSON string
     * @param charset charset used in input stream
     * @param type expected value type
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T readValue(InputStream src, Charset charset, Class<T> type) throws DeserializationException {
        return readValue(readStream(src, charset), type);
    }


    /**
     * Deserializes a JSON string from input stream as provided value type with given datatype.
     *
     * @param <T> expected value type
     * @param src input stream containing JSON string
     * @param charset charset used in input stream
     * @param type expected value type
     * @param datatype datatype of the value
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T readValue(InputStream src, Charset charset, Class<T> type, Datatype datatype) throws DeserializationException {
        return readValue(readStream(src, charset), type, datatype);
    }


    /**
     * Deserializes a JSON string from input stream as provided value type with default datatype (string).
     *
     * @param <T> expected value type
     * @param src input stream containing JSON string
     * @param type expected value type
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T readValue(InputStream src, Class<T> type) throws DeserializationException {
        return readValue(readStream(src, DEFAULT_CHARSET), type);
    }


    /**
     * Deserializes a JSON string from input stream as provided value type with given datatype using UTF-8 charset.
     *
     * @param <T> expected value type
     * @param src input stream containing JSON string
     * @param type expected value type
     * @param datatype datatype of the value
     * @return deserialized JSON object
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T readValue(InputStream src, Class<T> type, Datatype datatype) throws DeserializationException {
        return readValue(readStream(src, DEFAULT_CHARSET), type, datatype);
    }


    /**
     * Deserializes a JSON string from file as provided value type with default datatype (string).
     *
     * @param <T> expected value type
     * @param file file containing JSON string
     * @param charset charset used in input stream
     * @param type expected value type
     * @return deserialized JSON object
     * @throws FileNotFoundException if file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T readValue(File file, Charset charset, Class<T> type) throws DeserializationException, FileNotFoundException {
        return readValue(new FileInputStream(file), charset, type);
    }


    /**
     * Deserializes a JSON string from file as provided value type with given datatype.
     *
     * @param <T> expected value type
     * @param file file containing JSON string
     * @param charset charset used in input stream
     * @param type expected value type
     * @param datatype datatype of the value
     * @return deserialized JSON object
     * @throws FileNotFoundException if file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T readValue(File file, Charset charset, Class<T> type, Datatype datatype) throws DeserializationException, FileNotFoundException {
        return readValue(new FileInputStream(file), charset, type, datatype);
    }


    /**
     * Deserializes a JSON string from file as provided value type with default datatype (string).
     *
     * @param <T> expected value type
     * @param file file containing JSON string
     * @param type expected value type
     * @return deserialized JSON object
     * @throws FileNotFoundException if file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T readValue(File file, Class<T> type) throws DeserializationException, FileNotFoundException {
        return readValue(new FileInputStream(file), DEFAULT_CHARSET, type);
    }


    /**
     * Deserializes a JSON string from file as provided value type according to provided typeInfo.
     *
     * @param <T> expected value type
     * @param file file containing JSON string
     * @param typeInfo detailed type information for deserialization
     * @return deserialized JSON object
     * @throws FileNotFoundException if file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T readValue(File file, TypeInfo typeInfo) throws DeserializationException, FileNotFoundException {
        return readValue(new FileInputStream(file), DEFAULT_CHARSET, typeInfo);
    }


    /**
     * Deserializes a JSON string from file as provided value type.
     *
     * @param <T> expected value type
     * @param file file containing JSON string
     * @param submodelElement submodel element class to extract type information from
     * @return deserialized JSON object
     * @throws FileNotFoundException if file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T readValue(File file, SubmodelElement submodelElement) throws DeserializationException, FileNotFoundException {
        return readValue(new FileInputStream(file), DEFAULT_CHARSET, submodelElement);
    }


    /**
     * Deserializes a JSON string from file as provided value type with given datatype using UTF-8 charset.
     *
     * @param <T> expected value type
     * @param file file containing JSON string
     * @param type expected value type
     * @param datatype datatype of the value
     * @return deserialized JSON object
     * @throws FileNotFoundException if file is not found
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T readValue(File file, Class<T> type, Datatype datatype) throws DeserializationException, FileNotFoundException {
        return readValue(new FileInputStream(file), DEFAULT_CHARSET, type, datatype);
    }


    /**
     * Deserializes a JSON containg an array of {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue}.
     *
     * @param <T> expected value type
     * @param json JSON input string to deserialize
     * @param typeInfo detailed type information for deserialization
     * @return an array of element values
     * @throws DeserializationException if deserialization fails
     */
    public <T extends ElementValue> T[] readValueArray(String json, TypeInfo typeInfo) throws DeserializationException;


    /**
     * Deserializes a JSON containg a list of {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue}.
     *
     * @param <T> expected value type
     * @param json JSON input string to deserialize
     * @param submodelElement submodel element class to extract type information from
     * @return a list of element values
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> T[] readValueArray(String json, SubmodelElement submodelElement) throws DeserializationException {
        return readValueArray(json, TypeExtractor.extractTypeInfo(submodelElement));
    }


    /**
     * Deserializes a JSON containg a list of {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue}.
     *
     * @param <T> expected value type
     * @param json JSON input string to deserialize
     * @param typeInfo detailed type information for deserialization
     * @return a list of element values
     * @throws DeserializationException if deserialization fails
     */
    public <T extends ElementValue> List<T> readValueList(String json, TypeInfo typeInfo) throws DeserializationException;


    /**
     * Deserializes a JSON containg a {@code Page} of
     * {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue}.
     *
     * @param <T> expected value type
     * @param json JSON input string to deserialize
     * @param typeInfo detailed type information for deserialization
     * @return a {@code Page} of element values
     * @throws DeserializationException if deserialization fails
     */
    public <T extends ElementValue> Page<T> readValuePage(String json, TypeInfo typeInfo) throws DeserializationException;


    /**
     * Deserializes a JSON containg a list of {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue}.
     *
     * @param <T> expected value type
     * @param json JSON input string to deserialize
     * @param submodelElement the submodel element to extract the type information from
     * @return a list of element values
     * @throws DeserializationException if deserialization fails
     */
    public default <T extends ElementValue> List<T> readValueList(String json, SubmodelElement submodelElement) throws DeserializationException {
        return readValueList(json, TypeExtractor.extractTypeInfo(submodelElement));
    }


    /**
     * Deserializes a JSON string containg a map of
     * {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue}.
     *
     * @param <K> expected key type of returned map
     * @param <V> expected value type of returned map
     * @param json JSON input string to deserialize
     * @param typeInfo detailed type information for deserialization
     * @return a map of element values
     * @throws DeserializationException if deserialization fails
     */
    public <K, V extends ElementValue> Map<K, V> readValueMap(String json, TypeInfo typeInfo) throws DeserializationException;


    /**
     * Deserializes a JSON node a map of {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue}.
     *
     * @param <K> expected key type of returned map
     * @param <V> expected value type of returned map
     * @param json JSON node to deserialize
     * @param typeInfo detailed type information for deserialization
     * @return a map of element values
     * @throws DeserializationException if deserialization fails
     */
    public <K, V extends ElementValue> Map<K, V> readValueMap(JsonNode json, TypeInfo typeInfo) throws DeserializationException;


    /**
     * Deserializes a JSON containg a map of {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue}.
     *
     * @param <K> expected key type of returned map
     * @param <V> expected value type of returned map
     * @param json JSON input string to deserialize
     * @param submodelElement submodel element class to extract type information from
     * @return a map of element values
     * @throws DeserializationException if deserialization fails
     */
    public default <K, V extends ElementValue> Map<K, V> readValueMap(String json, SubmodelElement submodelElement) throws DeserializationException {
        return readValueMap(json, TypeExtractor.extractTypeInfo(submodelElement));
    }


    /**
     * Register custom implementation of AAS model classes to be used for deserialization.
     *
     * @param <T> type of AAS interface
     * @param interfaceType type of AAS interface
     * @param implementationType concrete implementation of AAS interface to use upon deserialization
     */
    public <T> void useImplementation(Class<T> interfaceType, Class<? extends T> implementationType);


    /**
     * Deserializes an {@link InvokeOperationRequest} with valueOnly format.For this to work, the implementation first
     * discoveres the operation identified by the operationIdentifier using the serviceContext and extracts the type
     * information necessary for deserialization.
     *
     * @param <T> the actual type of the request (sync or async)
     * @param file the file containing the JSON
     * @param type the actual type of the request (sync or async)
     * @param serviceContext the service context
     * @param operationIdentifier the identifier of the corresponding operation
     * @return the parse InvokeOperationRequest
     * @throws de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException when deserialization fails
     * @throws java.io.IOException when accessing the file fails
     */
    public <T extends InvokeOperationRequest> T readValueOperationRequest(File file, Class<T> type, ServiceContext serviceContext, SubmodelElementIdentifier operationIdentifier)
            throws DeserializationException, IOException;


    /**
     * Deserializes an {@link InvokeOperationRequest} with valueOnly format.For this to work, the implementation first
     * discoveres the operation identified by the operationIdentifier using the serviceContext and extracts the type
     * information necessary for deserialization.
     *
     * @param <T> the actual type of the request (sync or async)
     * @param json the json to parse
     * @param type the actual type of the request (sync or async)
     * @param serviceContext the service context
     * @param operationIdentifier the identifier of the corresponding operation
     * @return the parse InvokeOperationRequest
     * @throws de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException when deserialization fails
     */
    public <T extends InvokeOperationRequest> T readValueOperationRequest(String json, Class<T> type, ServiceContext serviceContext, SubmodelElementIdentifier operationIdentifier)
            throws DeserializationException;


    private static String readStream(InputStream src, Charset charset) {
        return new BufferedReader(
                new InputStreamReader(src, charset))
                        .lines()
                        .collect(Collectors.joining(System.lineSeparator()));
    }


    private static <T> TypeReference<T> toTypeReference(Class<T> type) {
        return new TypeReference<T>() {
            @Override
            public Type getType() {
                return type;
            }
        };
    }
}
