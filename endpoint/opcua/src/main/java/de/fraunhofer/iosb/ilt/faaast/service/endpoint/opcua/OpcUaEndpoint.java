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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.stack.core.StatusCodes;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.Endpoint;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EndpointException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.InvokeOperationSyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.SetSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.MultiLanguageProperty;
import io.adminshell.aas.v3.model.Operation;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for the OPC UA endpoint
 *
 * @author Tino Bischoff
 */
@SuppressWarnings("java:S2139")
public class OpcUaEndpoint implements Endpoint<OpcUaEndpointConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaEndpoint.class);

    private ServiceContext service;
    private AssetAdministrationShellEnvironment aasEnvironment;
    private MessageBus<?> messageBus;
    private OpcUaEndpointConfig currentConfig;
    private Server server;
    private int requestCounter;

    /**
     * Creates a new instance of OpcUaEndpoint
     */
    public OpcUaEndpoint() {
        aasEnvironment = null;
        messageBus = null;
        currentConfig = null;
        server = null;
    }


    /**
     * Gets the MessageBus
     *
     * @return The MessageBus
     */
    @SuppressWarnings("java:S1452")
    public MessageBus<?> getMessageBus() {
        return messageBus;
    }


    /**
     * Initializes the OPC UA Endpoint with the given Configurations.
     * This is the first call.
     *
     * @param core The desired Core Configuration
     * @param config The desired OPC UA Configuration
     * @param context The desired ServiceContext
     */
    @Override
    public void init(CoreConfig core, OpcUaEndpointConfig config, ServiceContext context) {
        currentConfig = config;
        service = context;
        messageBus = service.getMessageBus();
        if (messageBus == null) {
            throw new IllegalArgumentException("MessageBus is null");
        }
    }


    /**
     * Starts the Endpoint.
     * This is the third call.
     */
    @Override
    public void start() throws EndpointException {
        if (server != null && server.isRunning()) {
            throw new IllegalStateException("OPC UA Endpoint cannot be started because it is already running");
        }
        else if (currentConfig == null) {
            throw new IllegalStateException("OPC UA Endpoint cannot be started because no configuration is available");
        }

        aasEnvironment = service.getAASEnvironment();
        if (aasEnvironment == null) {
            throw new IllegalArgumentException("AASEnvironment is null");
        }

        try {
            server = new Server(currentConfig.getTcpPort(), aasEnvironment, this);
            server.startup();
            LOGGER.info("server started");
        }
        catch (Exception e) {
            LOGGER.error("Error starting OPC UA Server", e);
            throw new EndpointException("OPC UA server could not be started", e);
        }
    }


    /**
     * Stops the Endpoint.
     */
    @Override
    public void stop() {
        if (currentConfig == null) {
            throw new IllegalStateException("OPC UA Endpoint cannot be stopped because no configuration is available");
        }

        try {
            if (server != null) {
                LOGGER.info("stop server. Currently running: {}", server.isRunning());
                server.shutdown(currentConfig.getSecondsTillShutdown());
            }
        }
        catch (Exception e) {
            LOGGER.error("Error stopping OPC UA Server", e);
        }
    }


    /**
     * Retrieves the Endpoint Configuration.
     *
     * @return The current Configuration.
     */
    @Override
    public OpcUaEndpointConfig asConfig() {
        return currentConfig;
    }


    /**
     * Writes the Value of the given SubmodelElement into the service.
     *
     * @param element The desired SubmodelElement including the new value
     * @param submodel The corresponding submodel
     * @param refElement The reference to the SubmodelElement
     * @return True if the write succeeded, false otherwise
     */
    public boolean writeValue(SubmodelElement element, Submodel submodel, Reference refElement) {
        boolean retval = false;
        if (element == null) {
            throw new IllegalArgumentException("element == null");
        }
        else if (submodel == null) {
            throw new IllegalArgumentException("submodel == null");
        }

        try {
            SetSubmodelElementValueByPathRequest request = new SetSubmodelElementValueByPathRequest();

            List<Key> path = new ArrayList<>();
            path.addAll(refElement.getKeys());

            request.setId(submodel.getIdentification());
            request.setPath(path);
            request.setValueParser(new OpcUaElementValueParser());
            if (element instanceof MultiLanguageProperty) {
                MultiLanguageProperty mlp = (MultiLanguageProperty) element;
                if ((mlp.getValues() != null) && (mlp.getValues().size() > 1)) {
                    for (int i = 0; i < mlp.getValues().size(); i++) {
                        LOGGER.info("writeValue: MLP {}: {}", i, mlp.getValues().get(i).getValue());
                    }
                }
            }

            request.setRawValue(ElementValueMapper.toValue(element));

            if (request.getRawValue() instanceof MultiLanguagePropertyValue) {
                MultiLanguagePropertyValue mlpv = (MultiLanguagePropertyValue) request.getRawValue();
                if ((mlpv.getLangStringSet() != null) && (mlpv.getLangStringSet().size() > 1)) {
                    for (int i = 0; i < mlpv.getLangStringSet().size(); i++) {
                        LOGGER.info("writeValue: MLPV {}: {}", i, mlpv.getLangStringSet().toArray()[i]);
                    }
                }
            }

            Response response = service.execute(request);
            LOGGER.info("writeValue: Submodel {}; Element {}; Status: {}", submodel.getIdentification().getIdentifier(), element.getIdShort(), response.getStatusCode());
            if (isSuccess(response.getStatusCode())) {
                retval = true;
            }
        }
        catch (Exception e) {
            LOGGER.error("writeValue error", e);
        }

        return retval;
    }


    /**
     * Calls the desired operation in the service.
     *
     * @param operation The desired operation
     * @param inputVariables The input arguments
     * @param submodel The corresponding submodel
     * @param refElement The reference to the SubmodelElement
     * @return The OutputArguments The output arguments returned from the operation call
     * @throws StatusException If the operation fails
     */
    public List<OperationVariable> callOperation(Operation operation, List<OperationVariable> inputVariables, Submodel submodel, Reference refElement) throws StatusException {
        List<OperationVariable> outputArguments;
        try {
            InvokeOperationSyncRequest request = new InvokeOperationSyncRequest();

            List<Key> path = new ArrayList<>();
            path.addAll(refElement.getKeys());

            request.setId(submodel.getIdentification());
            request.setPath(path);
            request.setInputArguments(inputVariables);

            requestCounter++;
            request.setRequestId(Integer.toString(requestCounter));

            // execute method
            InvokeOperationSyncResponse response = (InvokeOperationSyncResponse) service.execute(request);
            if (isSuccess(response.getStatusCode())) {
                LOGGER.info("callOperation: Operation {} executed successfully", operation.getIdShort());
            }
            else if (response.getStatusCode() == StatusCode.CLIENT_METHOD_NOT_ALLOWED) {
                LOGGER.warn("callOperation: Operation {} error executing operation: {}", operation.getIdShort(), response.getStatusCode());
                throw new StatusException(StatusCodes.Bad_NotExecutable);
            }
            else {
                LOGGER.warn("callOperation: Operation {} error executing operation: {}", operation.getIdShort(), response.getStatusCode());
                throw new StatusException(StatusCodes.Bad_UnexpectedError);
            }

            outputArguments = response.getPayload().getOutputArguments();
        }
        catch (Exception e) {
            LOGGER.error("callOperation error", e);
            throw e;
        }

        return outputArguments;
    }


    /**
     * Returns a value indicating whether the given StatusCode is a success
     * 
     * @param code The desired StatusCode
     * @return True if the StatusCode is a success, false otherweise
     */
    private static boolean isSuccess(StatusCode code) {
        boolean retval = false;
        if ((code == StatusCode.SUCCESS) || (code == StatusCode.SUCCESS_CREATED) || (code == StatusCode.SUCCESS_NO_CONTENT)) {
            retval = true;
        }

        return retval;
    }
}
