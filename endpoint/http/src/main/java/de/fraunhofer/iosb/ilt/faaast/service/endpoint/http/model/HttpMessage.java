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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model;

import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.HashMap;
import java.util.Map;


/**
 * High-level representation of an HTTP message. This class provides abstraction
 * of concrete HTTP library used.
 */
public abstract class HttpMessage {

    protected String body;
    protected Map<String, String> headers;

    protected HttpMessage() {
        this.headers = new HashMap<>();
    }


    public String getBody() {
        return body;
    }


    public Map<String, String> getHeaders() {
        return headers;
    }


    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }


    public void setBody(String body) {
        this.body = body;
    }

    public abstract static class AbstractBuilder<T extends HttpMessage, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B body(String value) {
            getBuildingInstance().setBody(value);
            return getSelf();
        }


        public B header(String key, String value) {
            getBuildingInstance().getHeaders().put(key, value);
            return getSelf();
        }


        public B headers(Map<String, String> value) {
            getBuildingInstance().setHeaders(value);
            return getSelf();
        }
    }
}
