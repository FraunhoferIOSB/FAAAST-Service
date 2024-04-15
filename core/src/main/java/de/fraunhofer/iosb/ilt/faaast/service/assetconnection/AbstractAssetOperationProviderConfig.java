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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection;

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Abstract base class for {@link AssetOperationProviderConfig}.
 */
public abstract class AbstractAssetOperationProviderConfig implements AssetOperationProviderConfig {

    protected ArgumentValidationMode inputValidationMode;
    protected ArgumentValidationMode inoutputValidationMode;
    protected ArgumentValidationMode outputValidationMode;

    protected AbstractAssetOperationProviderConfig() {
        this.inputValidationMode = ArgumentValidationMode.DEFAULT;
        this.inoutputValidationMode = ArgumentValidationMode.DEFAULT;
        this.outputValidationMode = ArgumentValidationMode.DEFAULT;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractAssetOperationProviderConfig that = (AbstractAssetOperationProviderConfig) o;
        return Objects.equals(inputValidationMode, that.inputValidationMode)
                && Objects.equals(inoutputValidationMode, that.inoutputValidationMode)
                && Objects.equals(outputValidationMode, that.outputValidationMode);
    }


    @Override
    public int hashCode() {
        return Objects.hash(inputValidationMode, inoutputValidationMode, outputValidationMode);
    }

    public abstract static class AbstractBuilder<T extends AbstractAssetOperationProviderConfig, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

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
    }
}
