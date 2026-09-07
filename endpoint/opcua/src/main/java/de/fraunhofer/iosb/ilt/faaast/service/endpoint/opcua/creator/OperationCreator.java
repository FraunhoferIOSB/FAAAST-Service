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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator;

import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaQualifiedName;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.server.instantiation.NodeBuilder;
import com.prosysopc.ua.server.instantiation.NodeBuilderConfiguration;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.common.ServiceResultException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import opc.ua.aas.Ids;
import opc.ua.aas.objecttypes.AASOperationType;
import opc.ua.aas.objecttypes.AASOperationVariableType;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create Operations and integrate them into the
 * OPC UA address space.
 */
public class OperationCreator extends SubmodelElementCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationCreator.class);

    /**
     * Adds an AAS Operation to the given node.
     *
     * @param aasOperation The corresponding AAS operation to add
     * @param operationRef The reference to the AAS operation
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param nodeManager The corresponding Node Manager
     * @return The created node.
     */
    public static UaNode createAasOperation(Operation aasOperation, Reference operationRef, Submodel submodel, AasServiceNodeManager nodeManager) {
        UaNode retval = null;
        try {
            String name = aasOperation.getIdShort();
            if ((name == null) || name.isEmpty()) {
                name = getNameFromReference(operationRef);
            }
            QualifiedName browseName = UaQualifiedName.from(Ids.AASOperationType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.getDefaultNodeId();

            NodeBuilderConfiguration conf = new NodeBuilderConfiguration();
            conf.addOptional(Ids.AASOperationType_Operation);
            NodeBuilder<AASOperationType> nb = nodeManager.createNodeBuilder(AASOperationType.class, conf);
            nb.setBrowseName(browseName);
            nb.setDisplayName(LocalizedText.english(name));
            nb.setNodeId(nid);
            AASOperationType oper = nb.build();

            addSubmodelElementBaseData(oper, aasOperation, nodeManager);

            // for operations we put the corresponding operation object into the map
            nodeManager.addSubmodelElementAasMap(nid, new SubmodelElementData(aasOperation, submodel, SubmodelElementData.Type.OPERATION, operationRef));
            LOGGER.atDebug().log("addAasOperation: NodeId {}; Property: {}; Reference: {}", nid, aasOperation.getIdShort(), ReferenceHelper.toString(operationRef));

            // InputArguments
            // AASOperationVariableType
            if (!aasOperation.getInputVariables().isEmpty()) {
                for (var input: aasOperation.getInputVariables()) {
                    AASOperationVariableType inputNode = createOperationVariable(operationRef, input, submodel, nodeManager);
                    oper.addReference(inputNode, nodeManager.getNamespaceTable().toNodeId(Ids.AASHasAttribute));
                }
            }

            if (!aasOperation.getInoutputVariables().isEmpty()) {
                for (var inoutput: aasOperation.getInoutputVariables()) {
                    AASOperationVariableType inoutputNode = createOperationVariable(operationRef, inoutput, submodel, nodeManager);
                    oper.addReference(inoutputNode, nodeManager.getNamespaceTable().toNodeId(Ids.AASHasAttribute));
                }
            }

            if (!aasOperation.getOutputVariables().isEmpty()) {
                for (var output: aasOperation.getOutputVariables()) {
                    AASOperationVariableType outputNode = createOperationVariable(operationRef, output, submodel, nodeManager);
                    oper.addReference(outputNode, nodeManager.getNamespaceTable().toNodeId(Ids.AASHasAttribute));
                }
            }

            nodeManager.addReferable(operationRef, new ObjectData(aasOperation, oper, submodel));
            retval = oper;
        }
        catch (Exception ex) {
            LOGGER.error("createasOperation Exception", ex);
        }
        return retval;
    }


    private static AASOperationVariableType createOperationVariable(Reference operationRef, OperationVariable input, Submodel submodel, AasServiceNodeManager nodeManager)
            throws StatusException, ValueFormatException, ServiceException, AddressSpaceException, ServiceResultException {
        Reference elementRef = ReferenceBuilder.with(operationRef).element(input.getValue()).build();
        UaNode inputElement = SubmodelElementCreator.createSubmodelElement(input.getValue(), elementRef, submodel, nodeManager);

        NodeId nid = nodeManager.getDefaultNodeId();
        String name = input.getValue().getIdShort();
        QualifiedName browseName = UaQualifiedName.from(Ids.AASOperationType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
        AASOperationVariableType inputNode = nodeManager.createInstance(AASOperationVariableType.class, nid, browseName, LocalizedText.english(name));
        inputNode.addReference(inputElement, nodeManager.getNamespaceTable().toNodeId(Ids.AASHasAttribute));
        return inputNode;
    }

}
