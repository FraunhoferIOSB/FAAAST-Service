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

import java.util.Objects;


/**
 * Abstract base class for all read event messages that are sent via message bus.
 *
 * @param <T> value type
 */
public abstract class ReadEventMessage<T> extends AccessEventMessage {

    protected T value;

    public T getValue() {
        return value;
    }


    public void setValue(T value) {
        this.value = value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReadEventMessage<T> that = (ReadEventMessage<T>) o;
        return super.equals(o)
                && Objects.equals(value, that.value);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    public abstract static class AbstractBuilder<P, T extends ReadEventMessage<P>, B extends AbstractBuilder<P, T, B>> extends AccessEventMessage.AbstractBuilder<T, B> {

        public B value(P value) {
            getBuildingInstance().setValue(value);
            return getSelf();
        }
    }
}
