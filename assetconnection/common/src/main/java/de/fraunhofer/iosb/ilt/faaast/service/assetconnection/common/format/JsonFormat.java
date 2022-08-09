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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.format;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.PathNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.ElementInfo;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.codehaus.plexus.util.StringUtils;


/**
 * Implementation of JSON format for asset connection
 */
@Dataformat(key = JsonFormat.KEY)
public class JsonFormat implements Format {

    public static final String KEY = "JSON";
    private static final String MIME_TYPE = "application/json";
    private final JsonSerializer serializer;
    private final JsonDeserializer deserializer;

    public JsonFormat() {
        this.serializer = new JsonSerializer();
        this.deserializer = new JsonDeserializer();
    }


    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }


    @Override
    public Map<String, DataElementValue> read(String value, Map<String, ElementInfo> elements) throws AssetConnectionException {
        if (elements == null) {
            return Map.of();
        }
        if (value == null) {
            return elements.keySet().stream().collect(Collectors.toMap(x -> x, x -> null));
        }
        return elements.entrySet().stream().collect(Collectors.toMap(
                Entry::getKey,
                LambdaExceptionHelper.rethrowFunction(x -> {
                    String query = x.getValue().getQuery();
                    String actualValue = value;
                    if (!StringUtils.isBlank(query)) {
                        try {
                            actualValue = JsonPath.read(value, query).toString();
                        }
                        catch (PathNotFoundException e) {
                            throw new AssetConnectionException(String.format("value addressed by JSONPath not found (JSON path: %s, JSON: %s)", query, value), e);
                        }
                        catch (InvalidPathException e) {
                            throw new AssetConnectionException(String.format("invalid JSONPath (JSON path: %s)", query), e);
                        }
                        catch (JsonPathException e) {
                            throw new AssetConnectionException(String.format("error resolving JSONPath (JSON path: %s, JSON: %s)", query, value), e);
                        }
                    }
                    try {
                        TypeInfo<?> typeInfo = x.getValue().getTypeInfo();
                        // if datatype is string, we need to wrap it with additional quotes
                        if (typeInfo != null
                                && ElementValueTypeInfo.class.isAssignableFrom(typeInfo.getClass())
                                && ((ElementValueTypeInfo) typeInfo).getDatatype() == Datatype.STRING
                                && !actualValue.startsWith("\"")
                                && !actualValue.endsWith("\"")) {
                            actualValue = String.format("\"%s\"", actualValue);
                        }
                        return deserializer.readValue(actualValue, x.getValue().getTypeInfo());
                    }
                    catch (DeserializationException e) {
                        throw new AssetConnectionException(String.format("JSON deserialization failed (json: %S)", actualValue), e);
                    }
                })));
    }


    @Override
    public String write(DataElementValue value) throws AssetConnectionException {
        try {
            return serializer.write(value);
        }
        catch (SerializationException e) {
            throw new AssetConnectionException("serializing value to JSON failed", e);
        }
    }
}
