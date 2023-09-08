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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonApiDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;
import jakarta.json.Json;
import jakarta.json.JsonMergePatch;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.http.entity.ContentType;


/**
 * Base class for mapping HTTP requests to protocol-agnostic requests.
 */
public abstract class AbstractRequestMapper {

    protected final ServiceContext serviceContext;
    protected final HttpJsonApiDeserializer deserializer;
    protected final HttpMethod method;
    protected String urlPattern;

    protected AbstractRequestMapper(ServiceContext serviceContext, HttpMethod method, String urlPattern) {
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        Ensure.requireNonNull(method, "method must be non-null");
        Ensure.requireNonNull(urlPattern, "urlPattern must be non-null");
        this.serviceContext = serviceContext;
        this.method = method;
        this.urlPattern = urlPattern;
        this.deserializer = new HttpJsonApiDeserializer();
        init();
    }


    /**
     * Utility method to create named regex groups used to represent URL path elements that are variable, e.g.
     * <i>/shells/[id]/aas/</i>.
     *
     * @param name the name of the regex group
     * @return a string representation of a named regex group with given {@code name}
     */
    protected static final String pathElement(String name) {
        return String.format("(?<%s>[^/$]*)", name);
    }


    private void init() {
        urlPattern = RegExHelper.ensureLineMatch(urlPattern);
    }


    public HttpMethod getMethod() {
        return method;
    }


    /**
     * Decides if a given HTTP request matches this concrete protocol-agnostic request.
     *
     * @param httpRequest the HTTP request to check
     * @return true if matches, otherwise false
     * @throws IllegalArgumentException if httpRequest is null
     */
    public boolean matchesUrl(HttpRequest httpRequest) {
        Ensure.requireNonNull(httpRequest, "httpRequest must be non-null");
        return httpRequest.getPath().matches(urlPattern);
    }


    /**
     * Decides if a given URL matches this concrete protocol-agnostic request.
     *
     * @param url the URL to check
     * @return true if matches, otherwise false
     * @throws IllegalArgumentException if url is null
     */
    public boolean matchesUrl(String url) {
        Ensure.requireNonNull(url, "url must be non-null");
        return matchesUrl(HttpRequest.builder()
                .path(url)
                .build());
    }


    /**
     * Converts the HTTP request to protocol-agnostic request.
     *
     * @param httpRequest the HTTP request to convert
     * @return the protocol-agnostic request
     * @throws InvalidRequestException if conversion fails
     * @throws IllegalArgumentException if httpRequest is null
     */
    public Request parse(HttpRequest httpRequest) throws InvalidRequestException {
        Ensure.requireNonNull(httpRequest, "httpRequest must be non-null");
        Matcher matcher = Pattern.compile(urlPattern).matcher(httpRequest.getPath());
        if (matcher.matches()) {
            return doParse(httpRequest, RegExHelper.getGroupValues(urlPattern, httpRequest.getPath()));
        }
        throw new IllegalStateException(String.format("request was matched but no suitable parser found (HTTP method: %s, URL pattern: %s", method, urlPattern));
    }


    /**
     * Converts the HTTP request to protocol-agnostic request.
     *
     * @param httpRequest the HTTP request to convert
     * @param urlParameters map of named regex groups and their values
     * @return parsed request
     * @throws InvalidRequestException if conversion fails
     * @throws IllegalArgumentException if httpRequest is null
     */
    public abstract Request doParse(HttpRequest httpRequest, Map<String, String> urlParameters) throws InvalidRequestException;


    /**
     * Deserializes HTTP body to given type.
     *
     * @param <T> expected type
     * @param httpRequest HTTP request
     * @param type expected type
     * @return deserialized payload
     * @throws InvalidRequestException if deserialization fails
     * @throws IllegalArgumentException if httpRequest is null
     */
    protected <T> T parseBody(HttpRequest httpRequest, Class<T> type) throws InvalidRequestException {
        Ensure.requireNonNull(httpRequest, "httpRequest must be non-null");
        try {
            return deserializer.read(httpRequest.getBody(), type);
        }
        catch (DeserializationException e) {
            throw new InvalidRequestException("error parsing body", e);
        }
    }


    /**
     * Deserializes HTTP body multipart form data.
     *
     * @param httpRequest HTTP request
     * @param contentType the multipart contentType containing the boundary
     * @return deserialized payload
     * @throws InvalidRequestException if deserialization fails
     * @throws IllegalArgumentException if httpRequest is null
     */
    protected Map<String, String> parseMultiPartBody(HttpRequest httpRequest, ContentType contentType) throws InvalidRequestException {
        Ensure.requireNonNull(httpRequest, "httpRequest must be non-null");
        Map<String, String> map = new HashMap<String, String>();
        try {
            MultipartStream multipartStream = new MultipartStream(
                    new ByteArrayInputStream(httpRequest.getBody().getBytes()),
                    contentType.getParameter("boundary").getBytes());
            boolean nextPart = multipartStream.skipPreamble();
            while (nextPart) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                String partHeaders = multipartStream.readHeaders();
                multipartStream.readBodyData(output);
                if (partHeaders.contains("text/plain")) {
                    map.put("fileName", output.toString());
                }
                else {
                    map.put("file", output.toString());
                }
                nextPart = multipartStream.readBoundary();
            }
        }
        catch (IOException e) {
            throw new InvalidRequestException("error parsing body", e);
        }
        return map;
    }


    /**
     * Deserializes HTTP body to a list of given type.
     *
     * @param <T> expected type
     * @param httpRequest HTTP request
     * @param type expected type
     * @return deserialized payload as list of given type
     * @throws InvalidRequestException if deserialization fails
     * @throws IllegalArgumentException if httpRequest is null
     */
    protected <T> List<T> parseBodyAsList(HttpRequest httpRequest, Class<T> type) throws InvalidRequestException {
        Ensure.requireNonNull(httpRequest, "httpRequest must be non-null");
        try {
            return deserializer.readList(httpRequest.getBody(), type);
        }
        catch (DeserializationException e) {
            throw new InvalidRequestException("error parsing body", e);
        }
    }


    /**
     * Parses a string to a JSON merge patch.
     *
     * @param json the JSON to parse
     * @return the parsed merge patch
     */
    protected JsonMergePatch parseMergePatch(String json) {
        return Json.createMergePatch(
                Json.createReader(
                        new ByteArrayInputStream(json.getBytes()))
                        .readValue());
    }


    @Override
    public int hashCode() {
        return Objects.hash(serviceContext, deserializer, method, urlPattern);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractRequestMapper other = (AbstractRequestMapper) obj;
        return Objects.equals(this.urlPattern, other.urlPattern)
                && Objects.equals(this.serviceContext, other.serviceContext)
                && Objects.equals(this.deserializer, other.deserializer)
                && Objects.equals(this.method, other.method);
    }

}
