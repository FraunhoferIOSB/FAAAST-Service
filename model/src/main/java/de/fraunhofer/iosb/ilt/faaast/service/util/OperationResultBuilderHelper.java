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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.OperationResultBuilder;


/**
 * The OperationBuilderHelper extends the functionalities of aas4js OperationResultBuilder.
 */
public abstract class OperationResultBuilderHelper<T extends OperationResult, B extends OperationResultBuilderHelper<T, B>> extends OperationResultBuilder<T, B> {

    /**
     * Adds a message with the specified message type and message text.
     *
     * @param messageType the type of the message
     * @param messageText the text of the message
     * @return the current instance of the builder for method chaining
     */

    public B messages(MessageTypeEnum messageType, String messageText) {
        getBuildingInstance().getMessages().add(
                new Message.Builder()
                        .messageType(messageType)
                        .text(messageText)
                        .build());
        return getSelf();
    }

}
