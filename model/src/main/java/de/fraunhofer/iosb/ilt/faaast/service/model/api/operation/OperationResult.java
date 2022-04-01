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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * The result of invoking an {@link io.adminshell.aas.v3.model.Operation}
 */
public class OperationResult {

    private String requestId;
    private List<OperationVariable> outputArguments;
    private List<OperationVariable> inoutputArguments;
    private Result executionResult;
    private ExecutionState executionState;

    public OperationResult() {
        this.outputArguments = new ArrayList<>();
        this.inoutputArguments = new ArrayList<>();
    }


    public String getRequestId() {
        return requestId;
    }


    public void setRequestId(String requestId) {
        this.requestId = requestId;
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


    public Result getExecutionResult() {
        return executionResult;
    }


    public void setExecutionResult(Result executionResult) {
        this.executionResult = executionResult;
    }


    public ExecutionState getExecutionState() {
        return executionState;
    }


    public void setExecutionState(ExecutionState executionState) {
        this.executionState = executionState;
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
        return Objects.equals(requestId, that.requestId) && Objects.equals(outputArguments, that.outputArguments) && Objects.equals(inoutputArguments, that.inoutputArguments)
                && Objects.equals(executionResult, that.executionResult) && executionState == that.executionState;
    }


    @Override
    public int hashCode() {
        return Objects.hash(requestId, outputArguments, inoutputArguments, executionResult, executionState);
    }


    public static Builder builder() {
        return new Builder();
    }

    private abstract static class AbstractBuilder<T extends OperationResult, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B requestId(String value) {
            getBuildingInstance().setRequestId(value);
            return getSelf();
        }


        public B outputArguments(List<OperationVariable> value) {
            getBuildingInstance().setOutputArguments(value);
            return getSelf();
        }


        public B inoutputArguments(List<OperationVariable> value) {
            getBuildingInstance().setInoutputArguments(value);
            return getSelf();
        }


        public B executionResult(Result value) {
            getBuildingInstance().setExecutionResult(value);
            return getSelf();
        }


        public B executionState(ExecutionState value) {
            getBuildingInstance().setExecutionState(value);
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
