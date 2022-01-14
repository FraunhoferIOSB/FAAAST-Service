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
package de.fraunhofer.iosb.ilt.faaast.service.model.v3.api;

import java.util.Date;
import java.util.List;
import java.util.Objects;


public class Message {
    private MessageType messageType;
    private String text;
    private List<String> code;
    private List<Date> timestamp;

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


    public List<String> getCode() {
        return code;
    }


    public void setCode(List<String> code) {
        this.code = code;
    }


    public List<Date> getTimestamp() {
        return timestamp;
    }


    public void setTimestamp(List<Date> timestamp) {
        this.timestamp = timestamp;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Message message = (Message) o;
        return messageType == message.messageType && Objects.equals(text, message.text) && Objects.equals(code, message.code) && Objects.equals(timestamp, message.timestamp);
    }


    @Override
    public int hashCode() {
        return Objects.hash(messageType, text, code, timestamp);
    }
}
