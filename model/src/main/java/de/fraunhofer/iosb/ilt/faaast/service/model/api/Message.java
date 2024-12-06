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

import de.fraunhofer.iosb.ilt.faaast.service.util.TimeFormatHelper;
import java.util.Date;
import org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.MessageBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMessage;


/**
 * Model class representing a message and is extending the aas4j defaultMessage implementation.
 */
public class Message extends DefaultMessage {

    // enshure variables are set to the default values as required by the implementation 
    public Message() {
        Date date = new Date();
        this.timestamp = TimeFormatHelper.asISO8601(date);
        this.text = "";
        this.code = "";
        this.messageType = MessageTypeEnum.INFO;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends MessageBuilder<Message, Builder> {

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
