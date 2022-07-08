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
package de.fraunhofer.iosb.ilt.faaast.service.model.request;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public abstract class InvokeOperationRequest<T extends Response> extends AbstractInvokeOperationRequest<T> {

    private static final long DEFAULT_TIMEOUT = 1000;
    protected Identifier id;
    protected List<Key> path;
    protected List<OperationVariable> inputArguments;
    protected List<OperationVariable> inoutputArguments;
    protected long timeout;
    protected String requestId;

    protected InvokeOperationRequest() {
        this.path = new ArrayList<>();
        this.inputArguments = new ArrayList<>();
        this.inoutputArguments = new ArrayList<>();
        this.timeout = DEFAULT_TIMEOUT;
        this.requestId = UUID.randomUUID().toString();
    }


    public Identifier getId() {
        return id;
    }


    public void setId(Identifier id) {
        this.id = id;
    }


    public List<Key> getPath() {
        return path;
    }


    public void setPath(List<Key> path) {
        this.path = path;
    }


    public List<OperationVariable> getInputArguments() {
        return inputArguments;
    }


    public void setInputArguments(List<OperationVariable> inputArguments) {
        this.inputArguments = inputArguments;
    }


    public List<OperationVariable> getInoutputArguments() {
        return inoutputArguments;
    }


    public void setInoutputArguments(List<OperationVariable> inoutputArguments) {
        this.inoutputArguments = inoutputArguments;
    }


    public String getRequestId() {
        return requestId;
    }


    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InvokeOperationRequest that = (InvokeOperationRequest) o;
        return Objects.equals(id, that.id)
                && Objects.equals(path, that.path)
                && Objects.equals(inputArguments, that.inputArguments)
                && Objects.equals(inoutputArguments, that.inoutputArguments)
                && Objects.equals(timeout, that.timeout)
                && Objects.equals(requestId, that.requestId);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, path, inputArguments, inoutputArguments, timeout, requestId);
    }


    public long getTimeout() {
        return timeout;
    }


    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public abstract static class AbstractBuilder<T extends InvokeOperationRequest, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B id(Identifier value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B content(Content value) {
            OutputModifier.Builder builder = new OutputModifier.Builder();
            if (getBuildingInstance().outputModifier != null) {
                builder.level(getBuildingInstance().getOutputModifier().getLevel());
                builder.extend(getBuildingInstance().getOutputModifier().getExtent());
            }
            builder.content(value);
            getBuildingInstance().outputModifier = builder.build();
            return getSelf();
        }


        public B requestId(String value) {
            getBuildingInstance().setRequestId(value);
            return getSelf();
        }


        public B timeout(long value) {
            getBuildingInstance().setTimeout(value);
            return getSelf();
        }


        public B inoutputArgument(OperationVariable value) {
            getBuildingInstance().getInoutputArguments().add(value);
            return getSelf();
        }


        public B inoutputArguments(List<OperationVariable> value) {
            getBuildingInstance().setInoutputArguments(value);
            return getSelf();
        }


        public B inputArgument(OperationVariable value) {
            getBuildingInstance().getInputArguments().add(value);
            return getSelf();
        }


        public B inputArguments(List<OperationVariable> value) {
            getBuildingInstance().setInputArguments(value);
            return getSelf();
        }


        public B path(List<Key> value) {
            getBuildingInstance().setPath(value);
            return getSelf();
        }
    }
}
