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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.assetconnection;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.request.assetconnection.PostOperationProviderByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.response.assetconnection.PostOperationProviderByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.util.OperationProviderHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.request.assetconnection.PostOperationProviderByPathRequest} in the
 * service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.response.assetconnection.PostOperationProviderByPathResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class PostOperationProviderByPathRequestHandler extends AbstractSubmodelInterfaceRequestHandler<PostOperationProviderByPathRequest, PostOperationProviderByPathResponse> {

    //private static final Logger LOGGER = LoggerFactory.getLogger(PostOperationProviderByPathRequestHandler.class);
    //private static final ObjectMapper MAPPER = new ObjectMapper()
    //.enable(SerializationFeature.INDENT_OUTPUT)
    //        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    //.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    @Override
    protected PostOperationProviderByPathResponse doProcess(PostOperationProviderByPathRequest request, RequestExecutionContext context) throws Exception {
        Reference reference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(request.getPath())
                .build();
        if (context.getAssetConnectionManager().hasOperationProvider(reference)) {
            throw new IllegalArgumentException(String.format(
                    "operation provider already defined for reference '%s'",
                    ReferenceHelper.toString(reference)));
        }

        AssetConnectionConfig config = OperationProviderHelper.convertBodyToAssetConnectionConfig(request.getBody(), reference);
        OperationProviderHelper.logMessages(context.getAssetConnectionManager().updateConnections(new ArrayList<>(), List.of(config)));
        return PostOperationProviderByPathResponse.builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
    }

    //    private AssetConnectionConfig convertBody(String body, Reference operationReference) throws JsonProcessingException, InvalidRequestException {
    //        JsonNode rootNode = MAPPER.readTree(body);
    //        //if (json.getNodeType() == JsonNodeType.OBJECT) {
    //        //ObjectNode rootNode = (ObjectNode) json;
    //        JsonNode connection = rootNode.get("connection");
    //        if (connection == null) {
    //            throw new InvalidRequestException("invalid JSON: connection not found");
    //        }
    //        //var iterator = connection.values();
    //        //while (iterator.hasNext()) {
    //        //    LOGGER.info("value: {}", iterator.next());
    //        //}
    //        //connection.propertyStream().forEach(x -> LOGGER.info("Key: ", x.getKey()));
    //        //for (var entry: connection.properties()) {
    //        //    LOGGER.info("Key: {}; value: {}", entry.getKey(), entry.getValue());
    //        //}
    //        //createConfig();
    //        ObjectNode newNode = (ObjectNode) MAPPER.createObjectNode();
    //        //if (connection.getNodeType() == JsonNodeType.OBJECT) {
    //        //    ObjectNode connObj = (ObjectNode) connection;
    //        Map<String, JsonNode> map = new HashMap<>();
    //        connection.properties().stream().forEach(x -> map.put(x.getKey(), x.getValue()));
    //        newNode.setAll(map);
    //
    //        JsonNode op = rootNode.get("provider");
    //        ObjectNode operationProviders = (ObjectNode) MAPPER.createObjectNode();
    //
    //        operationProviders.set(ReferenceHelper.asString(operationReference), op);
    //        newNode.set("operationProviders", operationProviders);
    //        //rootNode.remove("connection");
    //        String txt = newNode.toPrettyString();
    //        LOGGER.info("New JSON: {}", txt);
    //        AssetConnectionConfig result = MAPPER.readValue(txt, AssetConnectionConfig.class);
    //        return result;
    //        //connObj.setA
    //        //}
    //        //}
    //        //return null;
    //    }

    //    private void createConfig() throws JsonProcessingException {
    //        String body = """
    //                {
    //                    "@class": "de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.OpcUaAssetConnection",
    //                    "host": "opc.tcp://localhost:12686/milo",
    //                    "operationProviders": {
    //                        "(Submodel)urn:aas:id:example:demo:submodel:1, (Operation)add_opcua": {
    //                            "nodeId": "nsu=info:faaast:demo;s=Add",
    //                            "inputArgumentMapping": [
    //                                {
    //                                    "idShort": "in1",
    //                                    "argumentName": "Input1"
    //                                },
    //                                {
    //                                    "idShort": "in2",
    //                                    "argumentName": "Input2"
    //                                }
    //                            ],
    //                            "outputArgumentMapping": [
    //                                {
    //                                    "idShort": "result",
    //                                    "argumentName": "Output"
    //                                }
    //                            ]
    //                        }
    //                    }
    //                }
    //                """;
    //        JsonNode json = MAPPER.readTree(body);
    //        JsonNode op = json.get("operationProviders");
    //        op.propertyStream().forEach(x -> LOGGER.info("Key: ", x.getKey()));
    //        for (var entry: op.properties()) {
    //            LOGGER.info("Key: {}; value: {}", entry.getKey(), entry.getValue());
    //        }
    //    }

    //    private void log(List<Message> messages) {
    //        for (var message: messages) {
    //            switch (message.getMessageType()) {
    //                case ERROR -> LOGGER.error(message.getText());
    //                case EXCEPTION -> LOGGER.error(message.getText());
    //                case INFO -> LOGGER.info(message.getText());
    //                case WARNING -> LOGGER.warn(message.getText());
    //                default -> LOGGER.debug(message.getText());
    //            }
    //        }
    //    }
}
