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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.provider.config;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.ArgumentValidationMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Base class for AssetOperationProviderConfig supporting multiple data formats.
 */
public abstract class AbstractMultiFormatOperationProviderConfig extends AbstractMultiFormatProviderConfig implements MultiFormatOperationProviderConfig {

    protected ArgumentValidationMode inputValidationMode;
    protected ArgumentValidationMode inoutputValidationMode;
    protected ArgumentValidationMode outputValidationMode;
    protected Map<String, String> queries;

    protected AbstractMultiFormatOperationProviderConfig() {
        this.inputValidationMode = ArgumentValidationMode.DEFAULT;
        this.inoutputValidationMode = ArgumentValidationMode.DEFAULT;
        this.outputValidationMode = ArgumentValidationMode.DEFAULT;
        this.queries = new HashMap<>();
    }


    @Override
    public ArgumentValidationMode getInputValidationMode() {
        return inputValidationMode;
    }


    @Override
    public void setInputValidationMode(ArgumentValidationMode mode) {
        this.inputValidationMode = mode;
    }


    @Override
    public ArgumentValidationMode getInoutputValidationMode() {
        return inoutputValidationMode;
    }


    @Override
    public void setInoutputValidationMode(ArgumentValidationMode mode) {
        this.inoutputValidationMode = mode;
    }


    @Override
    public ArgumentValidationMode getOutputValidationMode() {
        return outputValidationMode;
    }


    @Override
    public void setOutputValidationMode(ArgumentValidationMode mode) {
        this.outputValidationMode = mode;
    }


    @Override
    public Map<String, String> getQueries() {
        return queries;
    }


    @Override
    public void setQueries(Map<String, String> queries) {
        this.queries = queries;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractMultiFormatOperationProviderConfig that = (AbstractMultiFormatOperationProviderConfig) o;
        return super.equals(that)
                && Objects.equals(inputValidationMode, that.inputValidationMode)
                && Objects.equals(inoutputValidationMode, that.inoutputValidationMode)
                && Objects.equals(outputValidationMode, that.outputValidationMode)
                && Objects.equals(queries, that.queries);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), inputValidationMode, inoutputValidationMode, outputValidationMode, queries);
    }

    protected abstract static class AbstractBuilder<T extends AbstractMultiFormatOperationProviderConfig, B extends AbstractBuilder<T, B>>
            extends AbstractMultiFormatProviderConfig.AbstractBuilder<T, B> {

        public B inputValidationMode(ArgumentValidationMode value) {
            getBuildingInstance().setInputValidationMode(value);
            return getSelf();
        }


        public B inoutputValidationMode(ArgumentValidationMode value) {
            getBuildingInstance().setInoutputValidationMode(value);
            return getSelf();
        }


        public B outputValidationMode(ArgumentValidationMode value) {
            getBuildingInstance().setOutputValidationMode(value);
            return getSelf();
        }


        public B query(String name, String query) {
            getBuildingInstance().getQueries().put(name, query);
            return getSelf();
        }


        public B queries(Map<String, String> value) {
            getBuildingInstance().setQueries(value);
            return getSelf();
        }
    }
}
