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
package de.fraunhofer.iosb.ilt.faaast.service.model.api;

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * A request represents a protocol-agnostic operation as defined in the AAS specification Part 2.
 *
 * @param <T> type of the corresponding response
 */
public abstract class Request<T extends Response> {

    private boolean internal;

    protected Request() {
        this.internal = false;
    }


    public boolean isInternal() {
        return internal;
    }


    public void setInternal(boolean internal) {
        this.internal = internal;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Request<?> that = (Request<?>) o;
        return Objects.equals(internal, that.internal);
    }


    @Override
    public int hashCode() {
        return Objects.hash(internal);
    }

    public abstract static class AbstractBuilder<T extends Request, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B internal(boolean value) {
            getBuildingInstance().setInternal(value);
            return getSelf();
        }


        public B internal() {
            getBuildingInstance().setInternal(true);
            return getSelf();
        }
    }
}
