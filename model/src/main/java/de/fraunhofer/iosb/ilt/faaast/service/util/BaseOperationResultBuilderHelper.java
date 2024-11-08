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

import org.eclipse.digitaltwin.aas4j.v3.model.BaseOperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.BaseOperationResultBuilder;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;



/**
 * The BaseOperationBuilderHelper extends the functionalities of aas4js BaseOperationBuilder
 */
public abstract class BaseOperationResultBuilderHelper<T extends BaseOperationResult, B extends BaseOperationResultBuilderHelper<T, B>> extends BaseOperationResultBuilder<T, B> {

    public B message(MessageTypeEnum messageType, String messageText) {
            getBuildingInstance().getMessages().add(
                    new Message.Builder()
                            .messageType(messageType)
                            .text(messageText)
                            .build());
            return getSelf();
        }

}