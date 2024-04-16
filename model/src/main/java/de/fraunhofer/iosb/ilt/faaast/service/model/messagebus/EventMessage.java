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
package de.fraunhofer.iosb.ilt.faaast.service.model.messagebus;

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Abstract base class for all messages that are sent via message bus.
 */
public abstract class EventMessage {

    private Reference element;

    public Reference getElement() {
        return element;
    }


    public void setElement(Reference element) {
        this.element = element;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventMessage that = (EventMessage) o;
        return Objects.equals(element, that.element);
    }


    @Override
    public int hashCode() {
        return Objects.hash(element);
    }

    public abstract static class AbstractBuilder<T extends EventMessage, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B element(Reference value) {
            getBuildingInstance().setElement(value);
            return getSelf();
        }


        public B element(Identifiable value) {
            getBuildingInstance().setElement(AasUtils.toReference(value));
            return getSelf();
        }
    }
}
