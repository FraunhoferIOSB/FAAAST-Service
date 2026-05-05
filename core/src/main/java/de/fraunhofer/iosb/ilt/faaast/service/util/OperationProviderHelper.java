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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class for Body conversions.
 */
public class OperationProviderHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationProviderHelper.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    /**
     * Converts the given body to an AssetConnectionConfig.
     *
     * @param body The desired body.
     * @param operationReference The reference to the operation.
     * @return The converted AssetConnectionConfig.
     * @throws JsonProcessingException error serializing or deserializing data.
     * @throws InvalidRequestException of conversion fails.
     */
    public static AssetConnectionConfig convertBodyToAssetConnectionConfig(String body, Reference operationReference) throws JsonProcessingException, InvalidRequestException {
        JsonNode rootNode = MAPPER.readTree(body);
        //if (json.getNodeType() == JsonNodeType.OBJECT) {
        //ObjectNode rootNode = (ObjectNode) json;
        JsonNode connection = rootNode.get("connection");
        if (connection == null) {
            throw new InvalidRequestException("invalid JSON: connection not found");
        }
        //var iterator = connection.values();
        //while (iterator.hasNext()) {
        //    LOGGER.info("value: {}", iterator.next());
        //}
        //connection.propertyStream().forEach(x -> LOGGER.info("Key: ", x.getKey()));
        //for (var entry: connection.properties()) {
        //    LOGGER.info("Key: {}; value: {}", entry.getKey(), entry.getValue());
        //}
        //createConfig();
        ObjectNode newNode = (ObjectNode) MAPPER.createObjectNode();
        //if (connection.getNodeType() == JsonNodeType.OBJECT) {
        //    ObjectNode connObj = (ObjectNode) connection;
        Map<String, JsonNode> map = new HashMap<>();
        connection.properties().stream().forEach(x -> map.put(x.getKey(), x.getValue()));
        newNode.setAll(map);

        JsonNode op = rootNode.get("provider");
        ObjectNode operationProviders = (ObjectNode) MAPPER.createObjectNode();

        operationProviders.set(ReferenceHelper.asString(operationReference), op);
        newNode.set("operationProviders", operationProviders);
        //rootNode.remove("connection");
        String txt = newNode.toPrettyString();
        LOGGER.info("New JSON: {}", txt);
        AssetConnectionConfig result = MAPPER.readValue(txt, AssetConnectionConfig.class);
        return result;
        //connObj.setA
        //}
        //}
        //return null;
    }


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
