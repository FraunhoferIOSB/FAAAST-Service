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

/**
 * Abstraction of concrete HTTP response.
 */
public class HttpResponse extends HttpMessage {

    private String url;
    private int status;
    private String message;

    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }


    public int getStatus() {
        return status;
    }


    public void setStatus(int status) {
        this.status = status;
    }


    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends HttpResponse, B extends AbstractBuilder<T, B>> extends HttpMessage.AbstractBuilder<T, B> {

        public B message(String value) {
            getBuildingInstance().setMessage(value);
            return getSelf();
        }


        public B status(int value) {
            getBuildingInstance().setStatus(value);
            return getSelf();
        }


        public B url(String value) {
            getBuildingInstance().setUrl(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<HttpResponse, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected HttpResponse newBuildingInstance() {
            return new HttpResponse();
        }
    }
}
