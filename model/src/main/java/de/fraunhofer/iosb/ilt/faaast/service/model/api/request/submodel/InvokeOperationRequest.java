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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractSubmodelInterfaceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.OutputModifierConstraints;
import de.fraunhofer.iosb.ilt.faaast.service.util.ObjectHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;


/**
 * Base class for invoke operation requests.
 *
 * @param <T> type of the corresponding response
 */
public abstract class InvokeOperationRequest<T extends Response> extends AbstractSubmodelInterfaceRequest<T> {

    private static final Duration DEFAULT_TIMEOUT = DatatypeFactory.newDefaultInstance().newDuration(3000);
    protected String path;
    protected List<OperationVariable> inputArguments;
    protected List<OperationVariable> inoutputArguments;
    protected Duration timeout;

    protected InvokeOperationRequest() {
        super(OutputModifierConstraints.builder()
                .supportsExtent(false)
                .supportsLevel(false)
                .supportedContentModifiers(Content.NORMAL, Content.VALUE)
                .build());
        this.path = "";
        this.inputArguments = new ArrayList<>();
        this.inoutputArguments = new ArrayList<>();
        this.timeout = DEFAULT_TIMEOUT;
    }


    public String getPath() {
        return path;
    }


    public void setPath(String path) {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InvokeOperationRequest<T> that = (InvokeOperationRequest<T>) o;
        return super.equals(that)
                && Objects.equals(path, that.path)
                && ObjectHelper.equalsIgnoreOrder(inputArguments, that.inputArguments)
                && ObjectHelper.equalsIgnoreOrder(inoutputArguments, that.inoutputArguments)
                && Objects.equals(timeout, that.timeout);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path, inputArguments, inoutputArguments, timeout);
    }


    public Duration getTimeout() {
        return timeout;
    }


    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public abstract static class AbstractBuilder<T extends InvokeOperationRequest, B extends AbstractBuilder<T, B>> extends AbstractSubmodelInterfaceRequest.AbstractBuilder<T, B> {

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


        public B timeout(Duration value) {
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


        public B path(String value) {
            getBuildingInstance().setPath(value);
            return getSelf();
        }
    }
}
