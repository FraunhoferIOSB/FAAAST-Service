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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.provider.config.AbstractMultiFormatValueProviderConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * * Config file for HTTP-based {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider}.
 */
public class HttpValueProviderConfig extends AbstractMultiFormatValueProviderConfig {

    private String path;
    private String writeMethod;
    private Map<String, String> headers;

    public HttpValueProviderConfig() {
        this.headers = new HashMap<>();
    }


    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }


    public String getWriteMethod() {
        return writeMethod;
    }


    public void setWriteMethod(String writeMethod) {
        this.writeMethod = writeMethod;
    }


    public Map<String, String> getHeaders() {
        return headers;
    }


    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HttpValueProviderConfig that = (HttpValueProviderConfig) o;
        return super.equals(that)
                && Objects.equals(path, that.path)
                && Objects.equals(writeMethod, that.writeMethod)
                && Objects.equals(headers, that.headers);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path, writeMethod, headers);
    }


    public static Builder builder() {
        return new Builder();
    }

    protected abstract static class AbstractBuilder<T extends HttpValueProviderConfig, B extends AbstractBuilder<T, B>>
            extends AbstractMultiFormatValueProviderConfig.AbstractBuilder<T, B> {

        public B path(String value) {
            getBuildingInstance().setPath(value);
            return getSelf();
        }


        public B writeMethod(String value) {
            getBuildingInstance().setWriteMethod(value);
            return getSelf();
        }


        public B headers(Map<String, String> value) {
            getBuildingInstance().setHeaders(value);
            return getSelf();
        }


        public B header(String name, String value) {
            getBuildingInstance().getHeaders().put(name, value);
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
