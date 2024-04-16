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
import com.prosysopc.ua.server.nodes.PlainMethod;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.core.Argument;
import com.prosysopc.ua.stack.core.Identifiers;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.Locale;
import opc.i4aas.objecttypes.AASOperationType;
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
     * @param node The desired UA node
     * @param aasOperation The corresponding AAS operation to add
     * @param operationRef The reference to the AAS operation
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param ordered Specifies whether the operation should be added ordered
     *            (true) or unordered (false)
     * @param nodeManager The corresponding Node Manager
     */
    public static void addAasOperation(UaNode node, Operation aasOperation, Reference operationRef, Submodel submodel, boolean ordered, AasServiceNodeManager nodeManager) {
        try {
            String name = aasOperation.getIdShort();
            if ((name == null) || name.isEmpty()) {
                name = getNameFromReference(operationRef);
            }
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASOperationType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.getDefaultNodeId();
            AASOperationType oper = nodeManager.createInstance(AASOperationType.class, nid, browseName, LocalizedText.english(name));
            addSubmodelElementBaseData(oper, aasOperation, nodeManager);

            // for operations we put the corresponding operation object into the map
            nodeManager.addSubmodelElementAasMap(nid, new SubmodelElementData(aasOperation, submodel, SubmodelElementData.Type.OPERATION, operationRef));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("addAasOperation: NodeId {}; Property: {}; Reference: {}", nid, aasOperation.getIdShort(), ReferenceHelper.toString(operationRef));
            }

            // add method
            NodeId myMethodId = new NodeId(nodeManager.getNamespaceIndex(), nid.getValue().toString() + "." + name);
            PlainMethod method = new PlainMethod(nodeManager, myMethodId, name, Locale.ENGLISH);
            Argument[] inputs = new Argument[aasOperation.getInputVariables().size()];
            for (int i = 0; i < aasOperation.getInputVariables().size(); i++) {
                OperationVariable v = aasOperation.getInputVariables().get(i);
                inputs[i] = new Argument();
                setOperationArgument(inputs[i], v);
            }

            method.setInputArguments(inputs);

            Argument[] outputs = new Argument[aasOperation.getOutputVariables().size()];
            for (int i = 0; i < aasOperation.getOutputVariables().size(); i++) {
                OperationVariable v = aasOperation.getOutputVariables().get(i);
                outputs[i] = new Argument();
                setOperationArgument(outputs[i], v);
            }

            method.setOutputArguments(outputs);

            method.setDescription(new LocalizedText("", ""));
            oper.addComponent(method);

            if (ordered) {
                node.addReference(oper, Identifiers.HasOrderedComponent, false);
            }
            else {
                node.addComponent(oper);
            }

            nodeManager.addReferable(operationRef, new ObjectData(aasOperation, oper, submodel));
        }
        catch (Exception ex) {
            LOGGER.error("addAasOperation Exception", ex);
        }
    }


    /**
     * Sets the arguments for the given Operation Variable.
     *
     * @param arg The UA argument
     * @param var The corresponding Operation Variable
     */
    private static void setOperationArgument(Argument arg, OperationVariable operVar) {
        if (operVar.getValue() instanceof Property) {
            Property prop = (Property) operVar.getValue();
            arg.setName(prop.getIdShort());
            arg.setValueRank(ValueRanks.Scalar);
            arg.setArrayDimensions(null);

            // Description
            DescriptionCreator.addDescriptions(arg, prop.getDescription());

            NodeId type = ValueConverter.convertValueTypeStringToNodeId(prop.getValueType());
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

}
