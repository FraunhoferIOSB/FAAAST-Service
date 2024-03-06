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
package de.fraunhofer.iosb.ilt.faaast.service.model.value;

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Value class for ReferenceElement.
 */
public class ReferenceElementValue extends DataElementValue {

    private Reference value;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReferenceElementValue that = (ReferenceElementValue) o;
        return Objects.equals(value, that.value);
    }


    public Reference getValue() {
        return value;
    }


    public void setValue(Reference value) {
        this.value = value;
    }


    @Override
    public int hashCode() {
        return Objects.hash(value);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends ReferenceElementValue, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B value(Reference value) {
            getBuildingInstance().setValue(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<ReferenceElementValue, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected ReferenceElementValue newBuildingInstance() {
            return new ReferenceElementValue();
        }
    }
}
