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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.listener;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaMethod;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.server.CallableListener;
import com.prosysopc.ua.server.ServiceContext;
import com.prosysopc.ua.stack.builtintypes.DiagnosticInfo;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.StatusCode;
import com.prosysopc.ua.stack.builtintypes.Variant;
import com.prosysopc.ua.stack.core.StatusCodes;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.OpcUaEndpoint;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for listening method calls from a UaCallable node.
 */
public class AasServiceMethodManagerListener implements CallableListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AasServiceMethodManagerListener.class);

    private final OpcUaEndpoint endpoint;
    private final AasServiceNodeManager nodeManager;

    /**
     * Creates a new instance of AasServiceMethodManagerListener
     *
     * @param endpoint the associated endpoint
     * @param nodeManager the associated NodeManager
     */
    public AasServiceMethodManagerListener(OpcUaEndpoint endpoint, AasServiceNodeManager nodeManager) {
        this.endpoint = endpoint;
        Ensure.requireNonNull(endpoint, "endpoint must not be null");
        this.nodeManager = nodeManager;
    }


    @Override
    public boolean onCall(ServiceContext serviceContext, NodeId objectId, UaNode object, NodeId methodId, UaMethod method, Variant[] inputArguments,
                          StatusCode[] inputArgumentResults, DiagnosticInfo[] inputArgumentDiagnosticInfos, Variant[] outputs)
            throws StatusException {

        boolean retval = false;

        // Note that the outputs array is already allocated
        LOGGER.trace("onCall: method {}: called. InputArguments: {}", methodId, inputArguments);

        try {
            SubmodelElementData data = nodeManager.getAasData(objectId);
            Operation aasOper = (Operation) data.getSubmodelElement();
            if (aasOper != null) {
                List<OperationVariable> inputVariables = aasOper.getInputVariables();
                ValueConverter.setOperationValues(inputVariables, inputArguments);
                List<OperationVariable> outputVariables = endpoint.callOperation(aasOper, inputVariables, data.getSubmodel(), data.getReference());

                ValueConverter.setOutputArguments(outputVariables, outputs);
                retval = true;
            }
            else {
                LOGGER.debug("onCall: Property for {} not found", objectId);
            }
        }
        catch (StatusException se) {
            throw se;
        }
        catch (Exception ex) {
            throw new StatusException(ex.getMessage(), StatusCodes.Bad_UnexpectedError);
        }

        return retval;
    }
}
