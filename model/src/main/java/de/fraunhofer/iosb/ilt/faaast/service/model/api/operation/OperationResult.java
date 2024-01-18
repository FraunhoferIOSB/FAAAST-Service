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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;


/**
 * The result of invoking an {@link org.eclipse.digitaltwin.aas4j.v3.model.Operation}.
 */
public class OperationResult extends BaseOperationResult {

    private List<OperationVariable> outputArguments;
    private List<OperationVariable> inoutputArguments;

    public OperationResult() {
        this.outputArguments = new ArrayList<>();
        this.inoutputArguments = new ArrayList<>();
    }


    public List<OperationVariable> getOutputArguments() {
        return outputArguments;
    }


    public void setOutputArguments(List<OperationVariable> outputArguments) {
        this.outputArguments = outputArguments;
    }


    public List<OperationVariable> getInoutputArguments() {
        return inoutputArguments;
    }


    public void setInoutputArguments(List<OperationVariable> inoutputArguments) {
        this.inoutputArguments = inoutputArguments;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OperationResult that = (OperationResult) o;
        return super.equals(that)
                && Objects.equals(outputArguments, that.outputArguments)
                && Objects.equals(inoutputArguments, that.inoutputArguments);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), outputArguments, inoutputArguments);
    }

    protected abstract static class AbstractBuilder<T extends OperationResult, B extends AbstractBuilder<T, B>> extends BaseOperationResult.AbstractBuilder<T, B> {

        public B outputArguments(List<OperationVariable> value) {
            getBuildingInstance().setOutputArguments(value);
            return getSelf();
        }


        public B outputArgument(OperationVariable value) {
            getBuildingInstance().getOutputArguments().add(value);
            return getSelf();
        }


        public B inoutputArguments(List<OperationVariable> value) {
            getBuildingInstance().setInoutputArguments(value);
            return getSelf();
        }


        public B inoutputArgument(OperationVariable value) {
            getBuildingInstance().getInoutputArguments().add(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<OperationResult, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected OperationResult newBuildingInstance() {
            return new OperationResult();
        }
    }
}
