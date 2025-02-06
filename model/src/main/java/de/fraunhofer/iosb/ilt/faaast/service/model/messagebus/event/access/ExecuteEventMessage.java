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

import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import java.util.Objects;


/**
 * Abstract base class for all execute event messages that are sent via message bus.
 */
public abstract class ExecuteEventMessage extends AccessEventMessage {

    private String invocationId;

    public String getInvocationId() {
        return invocationId;
    }


    public void setInvocationId(String invocationId) {
        this.invocationId = invocationId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExecuteEventMessage that = (ExecuteEventMessage) o;
        return Objects.equals(invocationId, that.invocationId);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), invocationId);
    }

    public abstract static class AbstractBuilder<T extends ExecuteEventMessage, B extends AbstractBuilder<T, B>> extends EventMessage.AbstractBuilder<T, B> {
        public B invocationId(String value) {
            getBuildingInstance().setInvocationId(value);
            return getSelf();
        }
    }

}
