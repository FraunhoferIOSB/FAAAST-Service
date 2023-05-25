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

import java.util.Date;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Model class representing a message.
 */
public class Message {

    private MessageType messageType;
    private String text;
    private String code;
    private Date timestamp;

    public Message() {
        this.messageType = MessageType.INFO;
        this.text = "";
        this.code = "";
        this.timestamp = new Date();
    }


    public MessageType getMessageType() {
        return messageType;
    }


    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }


    public String getText() {
        return text;
    }


    public void setText(String text) {
        this.text = text;
    }


    public String getCode() {
        return code;
    }


    public void setCode(String code) {
        this.code = code;
    }


    public Date getTimestamp() {
        return timestamp;
    }


    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Message message = (Message) o;
        return messageType == message.messageType
                && Objects.equals(text, message.text)
                && Objects.equals(code, message.code)
                && Objects.equals(timestamp, message.timestamp);
    }


    @Override
    public int hashCode() {
        return Objects.hash(messageType, text, code, timestamp);
    }


    public static Builder builder() {
        return new Builder();
    }

    private abstract static class AbstractBuilder<T extends Message, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B messageType(MessageType value) {
            getBuildingInstance().setMessageType(value);
            return getSelf();
        }


        public B text(String value) {
            getBuildingInstance().setText(value);
            return getSelf();
        }


        public B code(String value) {
            getBuildingInstance().setCode(value);
            return getSelf();
        }


        public B timestamp(Date value) {
            getBuildingInstance().setTimestamp(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<Message, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected Message newBuildingInstance() {
            return new Message();
        }
    }
}
