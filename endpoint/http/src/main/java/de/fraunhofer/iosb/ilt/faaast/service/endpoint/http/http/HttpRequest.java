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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class HttpRequest extends HttpMessage {

    private HttpMethod method;
    private String path;
    private Map<String, String> queryParameters;
    private List<String> pathElements;

    public static Builder builder() {
        return new Builder();
    }


    public HttpRequest() {
        method = HttpMethod.GET;
        queryParameters = new HashMap<>();
        pathElements = new ArrayList<>();
    }


    public HttpMethod getMethod() {
        return method;
    }


    public void setMethod(HttpMethod method) {
        this.method = method;
    }


    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        if (path.startsWith("/")) {
            this.path = path.substring(1);
        }
        else {
            this.path = path;
        }
        this.pathElements = Arrays.asList(this.path.split("/"));
    }


    public boolean hasQueryParameter(String parameter) {
        return queryParameters.containsKey(parameter);
    }


    public String getQueryParameter(String parameter) {
        return queryParameters.get(parameter);
    }


    public String getQueryParameterOrDefault(String parameter, String defaultValue) {
        return queryParameters.getOrDefault(parameter, defaultValue);
    }


    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }


    public void setQueryParameters(Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
    }


    public void setQueryParametersFromQueryString(String queryString) {
        this.queryParameters = queryString != null && queryString.contains("=")
                ? Arrays.asList(queryString.split("&")).stream()
                        .map(x -> x.split("=")).collect(Collectors.toMap(
                                x -> x[0],
                                x -> x[1]))
                : new HashMap<>();
    }


    public List<String> getPathElements() {
        return pathElements;
    }

    public static abstract class AbstractBuilder<T extends HttpRequest, B extends AbstractBuilder<T, B>> extends HttpMessage.AbstractBuilder<T, B> {

        public B method(HttpMethod value) {
            getBuildingInstance().setMethod(value);
            return getSelf();
        }


        public B path(String value) {
            getBuildingInstance().setPath(value);
            return getSelf();
        }


        public B queryParameters(Map<String, String> value) {
            getBuildingInstance().setQueryParameters(value);
            return getSelf();
        }


        public B query(String value) {
            getBuildingInstance().setQueryParametersFromQueryString(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<HttpRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected HttpRequest newBuildingInstance() {
            return new HttpRequest();
        }
    }
}
