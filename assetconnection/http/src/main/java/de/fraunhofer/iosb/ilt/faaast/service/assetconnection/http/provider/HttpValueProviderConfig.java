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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.content.ContentFormat;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;


/**
 * * Config file for HTTP-based
 * {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider}.
 */
public class HttpValueProviderConfig implements AssetValueProviderConfig {

    private ContentFormat contentFormat;
    private String path;
    private String query;
    private String method;

    public String getMethod() {
        return method;
    }


    public void setMethod(String method) {
        this.method = method;
    }


    public HttpValueProviderConfig() {
        this.contentFormat = ContentFormat.DEFAULT;
    }


    public ContentFormat getContentFormat() {
        return contentFormat;
    }


    public void setContentFormat(ContentFormat contentFormat) {
        this.contentFormat = contentFormat;
    }


    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }


    public String getQuery() {
        return query;
    }


    public void setQuery(String query) {
        this.query = query;
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends HttpValueProviderConfig, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B query(String value) {
            getBuildingInstance().setQuery(value);
            return getSelf();
        }


        public B path(String value) {
            getBuildingInstance().setPath(value);
            return getSelf();
        }


        public B method(String value) {
            getBuildingInstance().setMethod(value);
            return getSelf();
        }


        public B contentFormat(ContentFormat value) {
            getBuildingInstance().setContentFormat(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<HttpValueProviderConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected HttpValueProviderConfig newBuildingInstance() {
            return new HttpValueProviderConfig();
        }
    }
}
