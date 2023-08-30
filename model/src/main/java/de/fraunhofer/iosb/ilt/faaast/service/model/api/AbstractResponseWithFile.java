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

import de.fraunhofer.iosb.ilt.faaast.service.model.FileContent;
import java.util.Objects;


/**
 * Abstract base class for protocol-agnostic responses containing file.
 */
public abstract class AbstractResponseWithFile<T> extends AbstractResponse {

    protected FileContent payload;

    public FileContent getPayload() {
        return payload;
    }


    public void setPayload(FileContent payload) {
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
        AbstractResponseWithFile<T> that = (AbstractResponseWithFile<T>) o;
        return super.equals(o)
                && Objects.equals(payload, that.payload);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), payload);
    }

    public abstract static class AbstractBuilder<V, T extends AbstractResponseWithFile<V>, B extends AbstractBuilder<V, T, B>> extends AbstractResponse.AbstractBuilder<T, B> {

        public B payload(FileContent value) {
            getBuildingInstance().setPayload(value);
            return getSelf();
        }
    }

}
