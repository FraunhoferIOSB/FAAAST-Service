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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Value provider that can be used from code with lambda expression.
 */
public class LambdaValueProvider implements AssetValueProvider {

    private Supplier<DataElementValue> reader;
    private Consumer<DataElementValue> writer;

    private LambdaValueProvider() {}


    /**
     * Checks if a reader is set.
     *
     * @return true if reader is set, false otherwise
     */
    public boolean hasReader() {
        return Objects.nonNull(reader);
    }


    /**
     * Checks if a writer is set.
     *
     * @return true if writer is set, false otherwise
     */
    public boolean hasWriter() {
        return Objects.nonNull(writer);
    }


    @Override
    public DataElementValue getValue() throws AssetConnectionException {
        if (Objects.nonNull(reader)) {
            try {
                return reader.get();
            }
            catch (Exception e) {
                throw new AssetConnectionException(e);
            }
        }
        return null;
    }


    @Override
    public void setValue(DataElementValue value) throws AssetConnectionException {
        if (Objects.nonNull(writer)) {
            try {
                writer.accept(value);
            }
            catch (Exception e) {
                throw new AssetConnectionException(e);
            }
        }
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ExtendableBuilder<LambdaValueProvider, Builder> {

        public Builder from(LambdaValueProvider provider) {
            if (Objects.nonNull(provider)) {
                read(provider.reader);
                write(provider.writer);
            }
            return this;
        }


        public Builder merge(LambdaValueProvider provider) {
            if (Objects.nonNull(provider)) {
                if (provider.hasReader()) {
                    read(provider.reader);
                }
                if (provider.hasWriter()) {
                    write(provider.writer);
                }
            }
            return this;
        }


        public Builder read(Supplier<DataElementValue> value) {
            getBuildingInstance().reader = value;
            return this;
        }


        public Builder write(Consumer<DataElementValue> value) {
            getBuildingInstance().writer = value;
            return this;
        }


        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected LambdaValueProvider newBuildingInstance() {
            return new LambdaValueProvider();
        }

    }

}
