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
package de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import java.util.Objects;


/**
 * Event message indicating that the value of an element has been changed.
 */
public class ValueChangeEventMessage extends ChangeEventMessage {

    private ElementValue oldValue;
    private ElementValue newValue;

    public ElementValue getOldValue() {
        return oldValue;
    }


    public void setOldValue(ElementValue oldValue) {
        this.oldValue = oldValue;
    }


    public ElementValue getNewValue() {
        return newValue;
    }


    public void setNewValue(ElementValue newValue) {
        this.newValue = newValue;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ValueChangeEventMessage that = (ValueChangeEventMessage) o;
        return super.equals(o)
                && Objects.equals(oldValue, that.oldValue)
                && Objects.equals(newValue, that.newValue);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), oldValue, newValue);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends ValueChangeEventMessage, B extends AbstractBuilder<T, B>> extends ChangeEventMessage.AbstractBuilder<T, B> {

        public B oldValue(ElementValue value) {
            getBuildingInstance().setOldValue(value);
            return getSelf();
        }


        public B newValue(ElementValue value) {
            getBuildingInstance().setNewValue(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<ValueChangeEventMessage, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected ValueChangeEventMessage newBuildingInstance() {
            return new ValueChangeEventMessage();
        }
    }
}
