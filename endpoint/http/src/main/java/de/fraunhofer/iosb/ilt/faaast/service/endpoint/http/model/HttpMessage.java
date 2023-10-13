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

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * High-level representation of an HTTP message. This class provides abstraction of concrete HTTP library used.
 */
public abstract class HttpMessage {

    protected byte[] body;
    protected Charset charset;
    protected Map<String, String> headers;

    protected HttpMessage() {
        this.headers = new HashMap<>();
        this.charset = Charset.defaultCharset();
    }


    public byte[] getBody() {
        return body;
    }


    public String getBodyAsString() {
        return new String(body, charset);
    }


    /**
     * Returns the body as string using given charset.
     *
     * @param charset the charset to use
     * @return the body as string using given charset
     */
    public String getBodyAsString(Charset charset) {
        return new String(body, charset);
    }


    public Map<String, String> getHeaders() {
        return headers;
    }


    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }


    public void setBody(byte[] body) {
        this.body = body;
    }


    public void setBody(String body) {
        this.body = body.getBytes(charset);
    }


    public void setCharset(Charset charset) {
        this.charset = charset;
    }


    /**
     * Sets the charset.
     *
     * @param charset the charset to set
     */
    public void setCharset(String charset) {
        try {
            this.charset = Charset.forName(charset);
        }
        catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            throw e;
        }
        catch (IllegalArgumentException e) {
            this.charset = Charset.defaultCharset();
        }
    }


    /**
     * Sets the body to a string encoded using the provided charset.
     *
     * @param body the body to set
     * @param charset the charset to use
     */
    public void setBody(String body, Charset charset) {
        this.charset = charset;
        this.body = body.getBytes(charset);
    }

    public abstract static class AbstractBuilder<T extends HttpMessage, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B body(byte[] value) {
            getBuildingInstance().setBody(value);
            return getSelf();
        }


        public B body(String value) {
            getBuildingInstance().setBody(value);
            return getSelf();
        }


        public B body(String value, Charset charset) {
            getBuildingInstance().setBody(value, charset);
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


        public B charset(Charset value) {
            getBuildingInstance().setCharset(value);
            return getSelf();
        }


        public B charset(String value) {
            getBuildingInstance().setCharset(value);
            return getSelf();
        }
    }
}
