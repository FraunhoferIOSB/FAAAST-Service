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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Model class for a result.
 */
public class Result {

    private List<Message> messages;

    public Result() {
        this.messages = new ArrayList<>();
    }


    public List<Message> getMessages() {
        return messages;
    }


    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Result result = (Result) o;
        return Objects.equals(messages, result.messages);
    }


    @Override
    public int hashCode() {
        return Objects.hash(messages);
    }


    public static Builder builder() {
        return new Builder();
    }

    protected abstract static class AbstractBuilder<T extends Result, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B messages(List<Message> value) {
            getBuildingInstance().setMessages(value);
            return getSelf();
        }


        public B message(Message value) {
            getBuildingInstance().getMessages().add(value);
            return getSelf();
        }


        public B message(MessageType messageType, String messageText) {
            getBuildingInstance().getMessages().add(
                    Message.builder()
                            .messageType(messageType)
                            .text(messageText)
                            .build());
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<Result, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected Result newBuildingInstance() {
            return new Result();
        }
    }
}
