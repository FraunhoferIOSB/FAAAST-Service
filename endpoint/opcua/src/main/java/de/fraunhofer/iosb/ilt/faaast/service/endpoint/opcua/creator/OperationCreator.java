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

import com.prosysopc.ua.UaQualifiedName;
import com.prosysopc.ua.ValueRanks;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.server.NodeManager;
import com.prosysopc.ua.server.instantiation.NodeBuilder;
import com.prosysopc.ua.server.instantiation.NodeBuilderConfiguration;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.Argument;
import com.prosysopc.ua.stack.core.Identifiers;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.List;
import opc.ua.aas.MethodIds;
import opc.ua.aas.ObjectIds;
import opc.ua.aas.ObjectTypeIds;
import opc.ua.aas.ReferenceTypeIds;
import opc.ua.aas.VariableIds;
import opc.ua.aas.objecttypes.AASBlobType;
import opc.ua.aas.objecttypes.AASOperationType;
import opc.ua.aas.objecttypes.AASOperationVariableType;
import opc.ua.aas.objecttypes.AASSubmodelElementObjectType;
import opc.ua.aas.variabletypes.AASSubmodelElementVariableType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
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
            QualifiedName browseName = UaQualifiedName.from(ObjectTypeIds.AASOperationType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.getDefaultNodeId();

            NodeBuilderConfiguration conf = new NodeBuilderConfiguration();
            conf.addOptional(MethodIds.AASOperationType_Operation);
            NodeBuilder nb = nodeManager.createNodeBuilder(AASBlobType.class, conf);
            nb.setBrowseName(browseName);
            nb.setDisplayName(LocalizedText.english(name));
            nb.setNodeId(nid);
            AASOperationType oper = (AASOperationType) nb.build();
            //AASOperationType oper = nodeManager.createInstance(AASOperationType.class, nid, browseName, LocalizedText.english(name));

            addSubmodelElementBaseData(oper, aasOperation, nodeManager);

            // for operations we put the corresponding operation object into the map
            nodeManager.addSubmodelElementAasMap(nid, new SubmodelElementData(aasOperation, submodel, SubmodelElementData.Type.OPERATION, operationRef));
            LOGGER.atDebug().log("addAasOperation: NodeId {}; Property: {}; Reference: {}", nid, aasOperation.getIdShort(), ReferenceHelper.toString(operationRef));

            // TODO attach arguments to oper

            // InputArguments
            // AASOperationVariableType
            if (!aasOperation.getInputVariables().isEmpty()) {
                for (var input: aasOperation.getInputVariables()) {
                    //
                    UaNode inputElement = SubmodelElementCreator.createSubmodelElement(aasOperation, operationRef, submodel, nodeManager);
                    conf = new NodeBuilderConfiguration();
                    AASSubmodelElementObjectType inputObject = null;
                    AASSubmodelElementVariableType inputVariable = null;
                    if (inputElement instanceof AASSubmodelElementObjectType object) {
                        conf.addOptional(ObjectIds.AASOperationVariableType_ValueObject);
                        inputObject = object;
                    }
                    else if (inputElement instanceof AASSubmodelElementVariableType variable) {
                        conf.addOptional(VariableIds.AASOperationVariableType_ValueVariable);
                        inputVariable = variable;
                    }
                    nb.setBrowseName(
                            UaQualifiedName.from(ObjectTypeIds.AASOperationType.getNamespaceUri(), input.getValue().getIdShort()).toQualifiedName(nodeManager.getNamespaceTable()));
                    nb.setDisplayName(LocalizedText.english(input.getValue().getIdShort()));
                    nb.setNodeId(nodeManager.getDefaultNodeId());
                    AASOperationVariableType inputNode = (AASOperationVariableType) nb.build();
                    if (inputVariable != null) {
                        inputNode.setValueVariable(inputVariable);
                    }
                    else if (inputObject != null) {
                        inputNode.addReference(inputObject, nodeManager.getNamespaceTable().toNodeId(ReferenceTypeIds.AASHasAttribute));
                    }
                    oper.addReference(inputNode, nodeManager.getNamespaceTable().toNodeId(ReferenceTypeIds.AASHasAttribute));
                }
            }

            // add method
            //NodeId myMethodId = new NodeId(nodeManager.getNamespaceIndex(), nid.getValue().toString() + "." + name);
            //PlainMethod method = new PlainMethod(nodeManager, myMethodId, AASOperationType.OPERATION, Locale.ENGLISH);
            //Argument[] inputs = new Argument[aasOperation.getInputVariables().size()];
            //for (int i = 0; i < aasOperation.getInputVariables().size(); i++) {
            //    OperationVariable v = aasOperation.getInputVariables().get(i);
            //    inputs[i] = new Argument();
            //    setOperationArgument(inputs[i], v, nodeManager);
            //}

            //method.setInputArguments(inputs);

            //Argument[] outputs = new Argument[aasOperation.getOutputVariables().size()];
            //for (int i = 0; i < aasOperation.getOutputVariables().size(); i++) {
            //    OperationVariable v = aasOperation.getOutputVariables().get(i);
            //    outputs[i] = new Argument();
            //    setOperationArgument(outputs[i], v, nodeManager);
            //}

            //method.setOutputArguments(outputs);

            //method.setDescription(new LocalizedText("", ""));
            //oper.addComponent(method);

            //if (ordered) {
            //    node.addReference(oper, nodeManager.getNamespaceTable().toNodeId(ReferenceTypeIds.AASHasOrderedComponent), false);
            //}
            //else {
            //    node.addReference(oper, nodeManager.getNamespaceTable().toNodeId(ReferenceTypeIds.AASHasComponent), false);
            //}

            nodeManager.addReferable(operationRef, new ObjectData(aasOperation, oper, submodel));
            retval = oper;
        }
        catch (Exception ex) {
            LOGGER.error("addAasOperation Exception", ex);
        }
        return retval;
    }


    /**
     * Sets the arguments for the given Operation Variable.
     *
     * @param arg The UA argument
     * @param var The corresponding Operation Variable
     * @param nodeManager The NodeManager.
     * @throws ServiceResultException If an error occurs.
     */
    private static void setOperationArgument(Argument arg, OperationVariable operVar, NodeManager nodeManager) throws ServiceResultException {
        if (operVar.getValue() instanceof Property prop) {
            arg.setName(prop.getIdShort());
            arg.setValueRank(ValueRanks.Scalar);
            arg.setArrayDimensions(null);

            // Description
            addDescriptions(arg, prop.getDescription());

            NodeId type = ValueConverter.convertDataTypeDefToNodeId(prop.getValueType(), nodeManager);
            if (type.isNullNodeId()) {
                LOGGER.warn("setOperationArgument: Property {}: Unknown type: {}", prop.getIdShort(), prop.getValueType());

                // Default type is String. That's what we receive from the AAS Service
                arg.setDataType(Identifiers.String);
            }
            else {
                arg.setDataType(type);
            }
        }
        else {
            LOGGER.warn("setOperationArgument: unknown Argument type");
        }
    }


    private static void addDescriptions(Argument arg, List<LangStringTextType> descriptions) {
        var textList = ValueConverter.convertLangStringSet(descriptions);
        if ((textList != null) && (textList.length > 0)) {
            arg.setDescription(textList[0]);
        }
    }
}
