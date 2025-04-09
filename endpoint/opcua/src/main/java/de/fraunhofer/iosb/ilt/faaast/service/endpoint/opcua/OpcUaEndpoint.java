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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.AbstractEndpoint;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EndpointException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.PatchSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.InvokeOperationSyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValueParser;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for the OPC UA endpoint
 */
public class OpcUaEndpoint extends AbstractEndpoint<OpcUaEndpointConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaEndpoint.class);
    private static final String CALL_OPERATION_ERROR_TXT = "callOperation: Operation {} error executing operation: {}";

    private Environment aasEnvironment;
    private Server server;

    /**
     * Creates a new instance of OpcUaEndpoint
     */
    public OpcUaEndpoint() {
        aasEnvironment = null;
        config = null;
        server = null;
    }


    /**
     * Gets the MessageBus
     *
     * @return The MessageBus
     */
    public MessageBus<?> getMessageBus() {
        return serviceContext.getMessageBus();
    }


    @Override
    public void start() throws EndpointException {
        if (server != null && server.isRunning()) {
            LOGGER.debug("OPC UA Endpoint already started");
            return;
        }

        try {
            aasEnvironment = serviceContext.getAASEnvironment();
            Ensure.requireNonNull(aasEnvironment, "aasEnvironment must not be null");
            server = new Server(config.getTcpPort(), aasEnvironment, this);
            server.startup();
            LOGGER.debug("server started");
        }
        catch (Exception e) {
            throw new EndpointException("OPC UA server could not be started", e);
        }
    }


    @Override
    public void stop() {
        try {
            if (server != null) {
                LOGGER.debug("stop server. Currently running: {}", server.isRunning());
                server.shutdown(config.getSecondsTillShutdown());
            }
        }
        catch (Exception e) {
            LOGGER.error("Error stopping OPC UA Server", e);
        }
    }


    @Override
    public OpcUaEndpointConfig asConfig() {
        return config;
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
        Ensure.requireNonNull(element, "element must not be null");
        Ensure.requireNonNull(submodel, "submodel must not be null");

        try {
            String path = ReferenceHelper.toPath(refElement);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("writeValue: Reference {}; Path {}", ReferenceHelper.toString(refElement), path);
            }
            PatchSubmodelElementValueByPathRequest request = new PatchSubmodelElementValueByPathRequest();

            request.setSubmodelId(submodel.getId());
            request.setPath(path);
            request.setValueParser(ElementValueParser.DEFAULT);
            if ((element instanceof MultiLanguageProperty mlp) && ((mlp.getValue() != null) && (mlp.getValue().size() > 1))) {
                for (int i = 0; i < mlp.getValue().size(); i++) {
                    LOGGER.trace("writeValue: MLP {}: {}", i, mlp.getValue().get(i).getText());
                }
            }

            request.setRawValue(ElementValueMapper.toValue(element));

            if ((request.getRawValue() instanceof MultiLanguagePropertyValue mlpv) && ((mlpv.getLangStringSet() != null) && (mlpv.getLangStringSet().size() > 1))) {
                for (int i = 0; i < mlpv.getLangStringSet().size(); i++) {
                    LOGGER.trace("writeValue: MLPV {}: {}", i, mlpv.getLangStringSet().toArray()[i]);
                }
            }

            Response response = serviceContext.execute(this, request);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("writeValue: Submodel {}; Element {} (Path {}); Status: {}", submodel.getId(), element.getIdShort(), ReferenceHelper.toPath(refElement),
                        response.getStatusCode());
            }
            if (response.getStatusCode().isSuccess()) {
                retval = true;
            }
        }
        catch (Exception e) {
            LOGGER.error("writeValue error", e);
        }

        return retval;
    }


    /**
     * Reads the value of the desired SubmodelElement from the service.
     *
     * @param submodelId The ID of the desired Submodel.
     * @param refElement The reference to the element.
     * @return The value of the desired SubmodelElement, null if the read failed.
     */
    public SubmodelElement readValue(String submodelId, Reference refElement) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("readValue: Submodel: {}; Ref: {}", submodelId, ReferenceHelper.toString(refElement));
        }
        SubmodelElement retval = null;
        GetSubmodelElementByPathRequest request = new GetSubmodelElementByPathRequest.Builder().submodelId(submodelId).path(ReferenceHelper.toPath(refElement)).build();
        Response response = serviceContext.execute(this, request);
        if ((response.getStatusCode() == StatusCode.SUCCESS) && (GetSubmodelElementByPathResponse.class.isAssignableFrom(response.getClass()))) {
            retval = ((GetSubmodelElementByPathResponse) response).getPayload();
        }

        return retval;
    }


    /**
     * Checks if the referenced element has a Value Provider.
     *
     * @param refElement The reference to the element.
     * @return True if it has a Value Provider, false otherwise.
     */
    public boolean hasValueProvider(Reference refElement) {
        return serviceContext.hasValueProvider(refElement);
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
        InvokeOperationSyncRequest request = new InvokeOperationSyncRequest();

        request.setSubmodelId(submodel.getId());
        request.setPath(ReferenceHelper.toPath(refElement));
        request.setInputArguments(inputVariables);

        // execute method
        InvokeOperationSyncResponse response = serviceContext.execute(this, request);
        if (response.getStatusCode().isSuccess()) {
            if (response.getPayload().getExecutionState() == ExecutionState.COMPLETED) {
                LOGGER.debug("callOperation: Operation {} executed successfully", operation.getIdShort());
            }
            else {
                LOGGER.warn(CALL_OPERATION_ERROR_TXT, operation.getIdShort(), response.getPayload().getExecutionState());
                throw new StatusException(StatusCodes.Bad_UnexpectedError);
            }
        }
        else if (response.getStatusCode() == StatusCode.CLIENT_METHOD_NOT_ALLOWED) {
            LOGGER.warn(CALL_OPERATION_ERROR_TXT, operation.getIdShort(), response.getStatusCode());
            throw new StatusException(StatusCodes.Bad_NotExecutable);
        }
        else {
            LOGGER.warn(CALL_OPERATION_ERROR_TXT, operation.getIdShort(), response.getStatusCode());
            throw new StatusException(StatusCodes.Bad_UnexpectedError);
        }

        outputArguments = response.getPayload().getOutputArguments();

        return outputArguments;
    }


    /**
     * Read the current environment from the service.
     *
     * @return The current environment.
     * @throws PersistenceException if accessing the environment fails
     */
    public Environment getAASEnvironment() throws PersistenceException {
        return serviceContext.getAASEnvironment();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpcUaEndpoint that = (OpcUaEndpoint) o;
        return super.equals(o)
                && Objects.equals(aasEnvironment, that.aasEnvironment)
                && Objects.equals(server, that.server);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), aasEnvironment, server);
    }
}
