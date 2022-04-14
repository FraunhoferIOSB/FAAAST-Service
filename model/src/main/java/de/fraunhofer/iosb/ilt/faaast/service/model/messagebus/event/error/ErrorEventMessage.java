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
package de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.error;

import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import java.util.Objects;


/**
 * Event message indicating that an error has occured
 */
public class ErrorEventMessage extends EventMessage {

    private Exception exception;

    private Class<?> throwingSource;

    private ErrorLevel errorLevel;

    /**
     * Default Constructor creating ErrorEventMessage with errorLevel = Info
     */
    public ErrorEventMessage() {
        this.errorLevel = ErrorLevel.INFO;
    }


    public Exception getException() {
        return exception;
    }


    public void setException(Exception exception) {
        this.exception = exception;
    }


    public Class getThrowingSource() {
        return throwingSource;
    }


    public void setThrowingSource(Class<?> throwingSource) {
        this.throwingSource = throwingSource;
    }


    public ErrorLevel getErrorLevel() {
        return errorLevel;
    }


    public void setErrorLevel(ErrorLevel errorLevel) {
        this.errorLevel = errorLevel;
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
        ErrorEventMessage that = (ErrorEventMessage) o;
        return Objects.equals(exception, that.exception)
                && Objects.equals(throwingSource, that.throwingSource)
                && Objects.equals(errorLevel, that.errorLevel);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), exception, throwingSource, errorLevel);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends ErrorEventMessage, B extends AbstractBuilder<T, B>> extends EventMessage.AbstractBuilder<T, B> {

        public B exception(Exception value) {
            getBuildingInstance().setException(value);
            return getSelf();
        }


        public B source(Class<?> value) {
            getBuildingInstance().setThrowingSource(value);
            return getSelf();
        }


        public B level(ErrorLevel value) {
            getBuildingInstance().setErrorLevel(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<ErrorEventMessage, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected ErrorEventMessage newBuildingInstance() {
            return new ErrorEventMessage();
        }
    }
}
