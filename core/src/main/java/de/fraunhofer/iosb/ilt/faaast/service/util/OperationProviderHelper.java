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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
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

    private OperationProviderHelper() {}


    /**
     * Converts the given body to an AssetConnectionConfig.
     *
     * @param body The desired body.
     * @param operationReference The reference to the operation.
     * @return The converted AssetConnectionConfig.
     * @throws InvalidRequestException of conversion fails.
     */
    public static AssetConnectionConfig convertBodyToAssetConnectionConfig(String body, Reference operationReference)
            throws InvalidRequestException {
        try {
            JsonNode rootNode = MAPPER.readTree(body);
            JsonNode connection = rootNode.get("connection");
            if (connection == null) {
                throw new InvalidRequestException("invalid JSON: 'connection' field not found");
            }
            JsonNode provider = rootNode.get("provider");
            if (provider == null) {
                throw new InvalidRequestException("invalid JSON: 'provider' field not found");
            }
            ObjectNode resultNode = connection.deepCopy();
            ObjectNode operationProviders = MAPPER.createObjectNode();
            operationProviders.set(ReferenceHelper.asString(operationReference), provider);
            resultNode.set("operationProviders", operationProviders);
            LOGGER.atTrace().log("New JSON: {}", resultNode.toPrettyString());
            return MAPPER.treeToValue(resultNode, AssetConnectionConfig.class);
        }
        catch (JsonProcessingException e) {
            throw new InvalidRequestException("failed to parse asset connection config from JSON", e);
        }
    }

}
