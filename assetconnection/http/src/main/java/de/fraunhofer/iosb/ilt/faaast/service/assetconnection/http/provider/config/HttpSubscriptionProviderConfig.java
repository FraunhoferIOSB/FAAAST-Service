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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.provider.config.AbstractMultiFormatSubscriptionProviderConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Config file for HTTP-based {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider}.
 */
public class HttpSubscriptionProviderConfig extends AbstractMultiFormatSubscriptionProviderConfig {

    private String path;
    private String method;
    private String payload;
    private long interval;
    private Map<String, String> headers;

    public HttpSubscriptionProviderConfig() {
        this.headers = new HashMap<>();
    }


    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }


    public String getMethod() {
        return method;
    }


    public void setMethod(String method) {
        this.method = method;
    }


    public long getInterval() {
        return interval;
    }


    public void setInterval(long interval) {
        this.interval = interval;
    }


    public String getPayload() {
        return payload;
    }


    public void setPayload(String payload) {
        this.payload = payload;
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
        HttpSubscriptionProviderConfig that = (HttpSubscriptionProviderConfig) o;
        return super.equals(that)
                && Objects.equals(path, that.path)
                && Objects.equals(method, that.method)
                && Objects.equals(payload, that.payload)
                && Objects.equals(interval, that.interval)
                && Objects.equals(headers, that.headers);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path, method, payload, interval, headers);
    }


    public static Builder builder() {
        return new Builder();
    }

    protected abstract static class AbstractBuilder<T extends HttpSubscriptionProviderConfig, B extends AbstractBuilder<T, B>>
            extends AbstractMultiFormatSubscriptionProviderConfig.AbstractBuilder<T, B> {

        public B path(String value) {
            getBuildingInstance().setPath(value);
            return getSelf();
        }


        public B method(String value) {
            getBuildingInstance().setMethod(value);
            return getSelf();
        }


        public B interval(long value) {
            getBuildingInstance().setInterval(value);
            return getSelf();
        }


        public B payload(String value) {
            getBuildingInstance().setPayload(value);
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

    public static class Builder extends AbstractBuilder<HttpSubscriptionProviderConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected HttpSubscriptionProviderConfig newBuildingInstance() {
            return new HttpSubscriptionProviderConfig();
        }
    }

}
