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
package de.fraunhofer.iosb.ilt.faaast.service.model.api;

import com.google.common.net.MediaType;
import java.util.Objects;


/**
 * Abstract base class for protocol-agnostic responses containing payload.
 */
public abstract class BaseResponseWithPayload<T> extends BaseResponse {

    protected T payload;
    protected MediaType contentType = MediaType.ANY_TYPE;

    public T getPayload() {
        return payload;
    }


    public void setPayload(T payload) {
        this.payload = payload;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        BaseResponseWithPayload<?> that = (BaseResponseWithPayload<?>) o;
        return super.equals(that)
                && Objects.equals(payload, that.payload)
                && Objects.equals(contentType, that.contentType);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), payload, contentType);
    }


    public MediaType getContentType() {
        return contentType;
    }


    public void setContentType(MediaType contentType) {
        this.contentType = contentType;
    }

    public abstract static class AbstractBuilder<T, R extends BaseResponseWithPayload<T>, B extends AbstractBuilder<T, R, B>> extends BaseResponse.AbstractBuilder<R, B> {

        public B payload(T value) {
            getBuildingInstance().setPayload(value);
            return getSelf();
        }


        public B contentType(MediaType value) {
            getBuildingInstance().setContentType(value);
            return getSelf();
        }
    }

}
