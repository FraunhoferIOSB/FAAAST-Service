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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.prosys;

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.Endpoint;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.SetSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.util.DataElementValueMapper;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Operation;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for the OPC UA endpoint
 *
 * @author Tino Bischoff
 */
public class OpcUaEndpoint implements Endpoint<OpcUaEndpointConfig> {

    private static final Logger logger = LoggerFactory.getLogger(OpcUaEndpoint.class);

    private Service service;
    private AssetAdministrationShellEnvironment aasEnvironment;
    private MessageBus messageBus;
    private OpcUaEndpointConfig currentConfig;
    private Server server;
    private int requestCounter;

    /**
     * Constructs a new OpcUaEndpoint
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
    public MessageBus getMessageBus() {
        return messageBus;
    }


    /**
     * Initializes the OPC UA Endpoint with the given Configurations. This is
     * the first call.
     *
     * @param core The desired Core Configuration
     * @param config The desired OPC UA Configuration
     */
    @Override
    public void init(CoreConfig core, OpcUaEndpointConfig config) {
        currentConfig = config;
    }


    /**
     * Sets the given Service. This is the second call.
     *
     * @param service The current Service
     */
    @Override
    public void setService(Service service) {
        this.service = service;
        this.aasEnvironment = service.getEnvironment();
        this.messageBus = service.getMessageBus();
    }


    /**
     * Starts the Endpoint. This is the third call.
     */
    @Override
    public void start() {
        if (server != null && server.isRunning()) {
            throw new IllegalStateException("OPC UA Endpoint cannot be started because it is already running");
        }

        try {
            server = new Server(currentConfig.getTcpPort(), aasEnvironment, this, currentConfig.getHttpsPort());
            server.startup();
            logger.info("server started");
        }
        catch (Exception ex) {
            logger.error("Error starting OPC UA Server", ex);
            throw new RuntimeException("OPC UA server could not be started", ex);
        }
    }


    /**
     * Stops the Endpoint.
     */
    @Override
    public void stop() {
        try {
            if (server != null) {
                logger.info("stop server");
                server.shutdown();
            }
        }
        catch (Exception ex) {
            logger.error("Error stopping OPC UA Server", ex);
            throw new RuntimeException("OPC UA server could not be stopped", ex);
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
     * Writes the Value of the given Property into the service.
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
            //Reference ref = AasUtils.toReference(AasUtils.toReference(submodel), element);
            path.addAll(refElement.getKeys());

            request.setId(submodel.getIdentification());
            request.setPath(path);
            request.setValueParser(new OpcUaElementValueParser());
            request.setRawValue(DataElementValueMapper.toDataElement(element));

            Response response = service.execute(request);
            logger.info("writeValue: Submodel " + submodel.getIdentification().getIdentifier() + "; Element " + element.getIdShort() + "; Status: " + response.getStatusCode());
            if (isSuccess(response.getStatusCode())) {
                retval = true;
            }
        }
        catch (Exception ex) {
            logger.error("writeValue error", ex);
        }

        return retval;
    }


    /**
     * Calls the desired operation in the service.
     *
     * @param operation The desired operation
     * @param inputVariables The input arguments
     */
    public void callOperation(Operation operation, List<OperationVariable> inputVariables) {
        try {
            InvokeOperationSyncRequest request = new InvokeOperationSyncRequest();

            List<Key> path = new ArrayList<>();
            path.add(new DefaultKey.Builder().idType(KeyType.ID_SHORT).type(KeyElements.OPERATION).value(operation.getIdShort()).build());
            request.setPath(path);

            request.setInputArguments(inputVariables);

            requestCounter++;
            request.setRequestId(Integer.toString(requestCounter));

            // TODO Method in Service not yet implemented
        }
        catch (Exception ex) {
            logger.error("callOperation error", ex);
        }
    }


    /**
     * Returns a value indicating whether the given StatusCode is a success
     * 
     * @param code The desired StatusCode
     * @return True if the StatusCode is a success, false otherweise
     */
    private static boolean isSuccess(StatusCode code) {
        boolean retval = false;
        if ((code == StatusCode.Success) || (code == StatusCode.SuccessCreated) || (code == StatusCode.SuccessNoContent)) {
            retval = true;
        }

        return retval;
    }
}
