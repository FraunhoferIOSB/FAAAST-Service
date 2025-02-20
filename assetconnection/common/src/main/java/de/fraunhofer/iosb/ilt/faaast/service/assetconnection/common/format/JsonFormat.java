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

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.ElementInfo;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.UnsupportedModifierException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;


/**
 * Implementation of JSON format for asset connection.
 */
@Dataformat(key = JsonFormat.KEY)
public class JsonFormat implements Format {

    public static final String KEY = "JSON";
    private static final String MIME_TYPE = "application/json";
    private final JsonApiSerializer serializer;
    private final JsonApiDeserializer deserializer;

    public JsonFormat() {
        this.serializer = new JsonApiSerializer();
        this.deserializer = new JsonApiDeserializer();
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
                    boolean shouldThrowException = true;
                    if (StringHelper.isBlank(query)) {
                        shouldThrowException = false;
                        query = elements.size() == 1 ? "$" : String.format("$.%s", x.getKey());
                    }
                    try {
                        List<Object> jsonPathResult = JsonPath
                                .using(Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST))
                                .parse(value)
                                .read(query);
                        if (jsonPathResult.isEmpty()) {
                            return handleException(String.format("JSONPath expression did not return any value (JSON path: %s, JSON: %s)", query, value), null,
                                    shouldThrowException);
                        }
                        if (jsonPathResult.size() > 1) {
                            return handleException(String.format("JSONPath expression returned more than one value (JSON path: %s, JSON: %s)", query, value), null,
                                    shouldThrowException);
                        }
                        actualValue = jsonPathResult.get(0).toString();
                        TypeInfo<?> typeInfo = x.getValue().getTypeInfo();
                        // if datatype is string, we need to escape and wrap it with additional quotes
                        if (typeInfo != null
                                && ElementValueTypeInfo.class.isAssignableFrom(typeInfo.getClass())
                                && ((ElementValueTypeInfo) typeInfo).getDatatype() == Datatype.STRING
                                && !actualValue.startsWith("\"")
                                && !actualValue.endsWith("\"")) {
                            actualValue = String.format("\"%s\"", escapeJson(actualValue));
                        }
                        return deserializer.readValue(actualValue, x.getValue().getTypeInfo());
                    }
                    catch (PathNotFoundException e) {
                        return handleException(String.format("value addressed by JSONPath not found (JSON path: %s, JSON: %s)", query, value), e, shouldThrowException);
                    }
                    catch (InvalidPathException e) {
                        return handleException(String.format("invalid JSONPath (JSON path: %s)", query), e, shouldThrowException);
                    }
                    catch (JsonPathException e) {
                        return handleException(String.format("error resolving JSONPath (JSON path: %s, JSON: %s)", query, value), e, shouldThrowException);
                    }
                    catch (DeserializationException e) {
                        return handleException(String.format("JSON deserialization failed (json: %S)", actualValue), e, shouldThrowException);
                    }
                    catch (Exception e) {
                        return handleException("unhandled exception", e, shouldThrowException);
                    }
                })));
    }


    private static DataElementValue handleException(String message, Exception e, boolean shouldThrowException) throws AssetConnectionException {
        if (shouldThrowException) {
            throw new AssetConnectionException(message, e);
        }
        return null;
    }


    @Override
    public String write(DataElementValue value) throws AssetConnectionException {
        try {
            return serializer.write(value);
        }
        catch (SerializationException | UnsupportedModifierException e) {
            throw new AssetConnectionException("serializing value to JSON failed", e);
        }
    }


    private static String escapeJson(String json) {
        return new String(JsonStringEncoder.getInstance().quoteAsString(json));
    }
}
