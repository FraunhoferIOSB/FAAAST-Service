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
import io.adminshell.aas.v3.model.Operation;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for listening method calls from a UaCallable node.
 *
 * @author Tino Bischoff
 */
public class AasServiceMethodManagerListener implements CallableListener {

    private static final Logger logger = LoggerFactory.getLogger(AasServiceMethodManagerListener.class);

    private final OpcUaEndpoint endpoint;
    private final AasServiceNodeManager nodeManager;

    /**
     * Creates a new instance of AasServiceMethodManagerListener
     *
     * @param ep the associated endpoint
     * @param nodeMan the associated NodeManager
     */
    public AasServiceMethodManagerListener(OpcUaEndpoint ep, AasServiceNodeManager nodeMan) {
        endpoint = ep;
        nodeManager = nodeMan;
    }


    /**
     * Callback method when a method was called
     *
     * @param serviceContext the current service context
     * @param objectId the ID of the node whose method is being called
     * @param object the object node whose method is being called, if available
     * @param methodId the ID of the method being called
     * @param method the method node being called, if available
     * @param inputArguments input argument values
     * @param inputArgumentResults argument errors. If errors in the values are
     *            encountered.
     * @param inputArgumentDiagnosticInfos diagnostic info, in case of errors.
     * @param outputs output values. The array is pre-created, just fill in the
     *            values.
     * @return true if you handle the call, which prevents any other handler
     *         being called.
     * @throws StatusException if there are errors in the method handling. For
     *             example, if you set inputArgumentResults, you should throw a
     *             StatusException with StatusCodes.Bad_InvalidArgument
     */
    @Override
    public boolean onCall(ServiceContext serviceContext, NodeId objectId, UaNode object, NodeId methodId, UaMethod method, Variant[] inputArguments,
                          StatusCode[] inputArgumentResults, DiagnosticInfo[] inputArgumentDiagnosticInfos, Variant[] outputs)
            throws StatusException {

        boolean retval = false;

        // Handle method calls
        // Note that the outputs array is already allocated
        logger.info("onCall: method " + methodId.toString() + ": called. InputArguments: " + Arrays.toString(inputArguments));

        try {
            if (endpoint == null) {
                logger.warn("onCall: no Endpoint available");
            }
            else {
                // TODO implement method
                //throw new UnsupportedOperationException("onCall not implemented");

                SubmodelElementData data = nodeManager.getAasData(objectId);
                Operation aasOper = (Operation) data.getSubmodelElement();
                if (aasOper != null) {
                    List<OperationVariable> inputVariables = aasOper.getInputVariables();
                    if (inputArguments.length < inputVariables.size()) {
                        throw new StatusException(StatusCodes.Bad_ArgumentsMissing);
                    }
                    if (inputArguments.length > inputVariables.size()) {
                        throw new StatusException(StatusCodes.Bad_TooManyArguments);
                    }
                    else {
                        for (int i = 0; i < inputVariables.size(); i++) {
                            SubmodelElement smelem = inputVariables.get(i).getValue();
                            if (smelem instanceof Property) {
                                ((Property) smelem).setValue(inputArguments[i].toString());
                            }

                            // TODO: set values for other SubmodelElement Types
                        }

                        endpoint.callOperation(aasOper, inputVariables);
                    }

                    //                    // search the corresponding submodel
                    //                    Reference smref = nodeManager.getSubmodelReference(aasOper.getReference());
                    //                    if (smref != null) {
                    //                        Argument[] arguments = new Argument[inputArguments.length];
                    //                        List<OperationVariable> aasInputVariables = aasOper.getInputVariables();
                    //                        for (int i = 0; i < arguments.length; i++) {
                    //                            arguments[0] = Argument.of(aasInputVariables.get(i).getValue().getIdShort(), inputArguments[i].toString());
                    //                        }
                    //                        
                    //                        OpcUaResponse response = endpoint.callOperation(smref, aasOper, arguments);
                    //                        if ((response == null) || (!response.isSuccess())) {
                    //                            throw new StatusException(StatusCodes.Bad_UnexpectedError);
                    //                        }
                    //                        
                    //                        if (response.getResult() != null) {
                    //                            if (Argument[].class.isAssignableFrom(response.getResult().getClass())) {
                    //                                Argument[] outputArgs = (Argument[])response.getResult();
                    //                                for (int i = 0; i < outputArgs.length; i++) {
                    //                                    Property outprop = (Property)aasOper.getOutputVariables().get(i).getValue();
                    //
                    //                                    outputs[i] = nodeManager.getServer().getAddressSpace().getDataTypeConverter().parseVariant(outputArgs[i].getValue(), ValueConverter.convertAasDataTypeToNodeId(outprop.getValueType()));
                    //                                }
                    //                                
                    //                                retval = true;
                    //                            }
                    //                            else {
                    //                                logger.warn("onCall: result wrong type: " + response.getResult());
                    //                            }
                    //                        }
                    //                    }
                }
                else {
                    logger.info("onCall: Property for " + objectId.toString() + " not found");
                }
            }
        }
        catch (StatusException se) {
            logger.error("onCall StatusException", se);
            throw se;
        }
        catch (Throwable ex) {
            logger.error("onCall Exception", ex);
            throw new StatusException(ex.getMessage(), StatusCodes.Bad_UnexpectedError);
        }

        return retval;
    }
}
