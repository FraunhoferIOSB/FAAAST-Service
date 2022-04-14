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
package de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Event message indicating that an operation has finished execution
 */
public class OperationFinishEventMessage extends ExecuteEventMessage {

    private List<ElementValue> output;
    private List<ElementValue> inoutput;

    public OperationFinishEventMessage() {
        this.output = new ArrayList<>();
        this.inoutput = new ArrayList<>();
    }


    public List<ElementValue> getOutput() {
        return output;
    }


    public void setOutput(List<ElementValue> output) {
        this.output = output;
    }


    public List<ElementValue> getInoutput() {
        return inoutput;
    }


    public void setInoutput(List<ElementValue> inoutput) {
        this.inoutput = inoutput;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        OperationFinishEventMessage that = (OperationFinishEventMessage) o;
        return Objects.equals(output, that.output) && Objects.equals(inoutput, that.inoutput);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), output, inoutput);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends OperationFinishEventMessage, B extends AbstractBuilder<T, B>> extends ExecuteEventMessage.AbstractBuilder<T, B> {

        public B output(List<ElementValue> value) {
            getBuildingInstance().setOutput(value);
            return getSelf();
        }


        public B inoutput(List<ElementValue> value) {
            getBuildingInstance().setInoutput(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<OperationFinishEventMessage, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected OperationFinishEventMessage newBuildingInstance() {
            return new OperationFinishEventMessage();
        }
    }
}
