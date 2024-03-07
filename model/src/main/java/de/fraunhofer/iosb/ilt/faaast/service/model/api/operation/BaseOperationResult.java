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
import java.util.Objects;


/**
 * The result of invoking an {@link org.eclipse.digitaltwin.aas4j.v3.model.Operation}.
 */
public class BaseOperationResult extends Result {

    private ExecutionState executionState;
    private boolean success;

    public boolean getSuccess() {
        return success;
    }


    public void setSuccess(boolean success) {
        this.success = success;
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
        BaseOperationResult that = (BaseOperationResult) o;
        return super.equals(that)
                && Objects.equals(success, that.success)
                && executionState == that.executionState;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), success, executionState);
    }

    protected abstract static class AbstractBuilder<T extends BaseOperationResult, B extends AbstractBuilder<T, B>> extends Result.AbstractBuilder<T, B> {

        public B success(boolean value) {
            getBuildingInstance().setSuccess(value);
            return getSelf();
        }


        public B executionState(ExecutionState value) {
            getBuildingInstance().setExecutionState(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<BaseOperationResult, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected BaseOperationResult newBuildingInstance() {
            return new BaseOperationResult();
        }
    }
}
