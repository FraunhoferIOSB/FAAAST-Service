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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;
import org.apache.commons.lang3.StringUtils;


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


    private String executeQuery(String value, String query) throws AssetConnectionException {
        List<String> results = query.startsWith("jq|")
                ? executeJqQuery(value, query.substring(3))
                : executeJsonPathQuery(value, query);
        if (results.isEmpty()) {
            throw new AssetConnectionException(String.format("Query expression did not return any value (JSON path: %s, JSON: %s)", query, value));
        }
        if (results.size() > 1) {
            throw new AssetConnectionException(String.format("Query expression returned more than one value (JSON path: %s, JSON: %s)", query, value));
        }
        return results.get(0);
    }


    private List<String> executeJqQuery(String value, String query) throws AssetConnectionException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode inputNode = mapper.readTree(value);
            Scope scope = Scope.newEmptyScope();
            BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_7, scope);
            JsonQuery q = JsonQuery.compile(query, Versions.JQ_1_7);

            final List<JsonNode> results = new ArrayList<>();
            q.apply(scope, inputNode, results::add);
            return results.stream()
                    .map(Object::toString)
                    .toList();
        }
        catch (JsonProcessingException e) {
            throw new AssetConnectionException(String.format("error executing JQ query (query: %s, JSON: %s)", query, value), e);
        }
    }


    private List<String> executeJsonPathQuery(String value, String query) throws AssetConnectionException {
        try {
            List<Object> results = JsonPath
                    .using(Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST))
                    .parse(value)
                    .read(query);
            return results.stream()
                    .map(Object::toString)
                    .toList();
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
                        actualValue = executeQuery(value, query);
                    }
                    try {
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
        catch (SerializationException | UnsupportedModifierException e) {
            throw new AssetConnectionException("serializing value to JSON failed", e);
        }
    }


    private static String escapeJson(String json) {
        return new String(JsonStringEncoder.getInstance().quoteAsString(json));
    }
}
