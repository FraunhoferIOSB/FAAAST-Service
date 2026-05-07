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

import static org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum.ERROR;
import static org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum.EXCEPTION;
import static org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum.INFO;
import static org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum.WARNING;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class for Loggings.
 */
public class LogHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogHelper.class);

    private LogHelper() {}


    /**
     * Logs the given messages.
     *
     * @param messages The desired messages.
     */
    public static void logMessages(List<Message> messages) {
        for (var message: messages) {
            switch (message.getMessageType()) {
                case ERROR -> LOGGER.error(message.getText());
                case EXCEPTION -> LOGGER.error(message.getText());
                case INFO -> LOGGER.info(message.getText());
                case WARNING -> LOGGER.warn(message.getText());
                default -> LOGGER.debug(message.getText());
            }
        }
    }

}
