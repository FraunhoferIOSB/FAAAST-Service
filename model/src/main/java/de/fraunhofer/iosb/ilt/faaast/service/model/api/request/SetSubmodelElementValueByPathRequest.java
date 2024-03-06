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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.SetSubmodelElementValueByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValueParser;
import java.util.Objects;


/**
 * Request class for SetSubmodelElementValueByPath requests.
 */
public class SetSubmodelElementValueByPathRequest<T> extends AbstractSubmodelInterfaceRequest<SetSubmodelElementValueByPathResponse> {

    private String path;
    private T rawValue;
    private ElementValueParser valueParser;
    private boolean syncWithAsset;

    public SetSubmodelElementValueByPathRequest() {
        this.syncWithAsset = true;
        this.path = "";
        this.valueParser = ElementValueParser.DEFAULT;
    }


    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }


    public T getRawValue() {
        return rawValue;
    }


    public void setRawValue(T rawValue) {
        this.rawValue = rawValue;
    }


    public ElementValueParser getValueParser() {
        return valueParser;
    }


    public void setValueParser(ElementValueParser valueParser) {
        this.valueParser = valueParser;
    }


    public boolean isSyncWithAsset() {
        return syncWithAsset;
    }


    protected void setSyncWithAsset(boolean syncWithAsset) {
        this.syncWithAsset = syncWithAsset;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetSubmodelElementValueByPathRequest<T> that = (SetSubmodelElementValueByPathRequest<T>) o;
        return super.equals(that)
                && Objects.equals(path, that.path)
                && Objects.equals(rawValue, that.rawValue);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path, rawValue, syncWithAsset);
    }


    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public abstract static class AbstractBuilder<U, T extends SetSubmodelElementValueByPathRequest<U>, B extends AbstractBuilder<U, T, B>>
            extends AbstractSubmodelInterfaceRequest.AbstractBuilder<T, B> {

        public B path(String value) {
            getBuildingInstance().setPath(value);
            return getSelf();
        }


        public B value(U value) {
            getBuildingInstance().setRawValue(value);
            return getSelf();
        }


        public B valueParser(ElementValueParser<U> value) {
            getBuildingInstance().setValueParser(value);
            return getSelf();
        }


        public B disableSyncWithAsset() {
            getBuildingInstance().setSyncWithAsset(false);
            return getSelf();
        }
    }

    public static class Builder<T> extends AbstractBuilder<T, SetSubmodelElementValueByPathRequest<T>, Builder<T>> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected SetSubmodelElementValueByPathRequest<T> newBuildingInstance() {
            return new SetSubmodelElementValueByPathRequest<>();
        }
    }
}
