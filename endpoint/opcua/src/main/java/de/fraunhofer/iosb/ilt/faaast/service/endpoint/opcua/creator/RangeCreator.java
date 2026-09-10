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
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.AasSubmodelElementHelper;
import opc.ua.iosb.aas.Ids;
import opc.ua.iosb.aas.variabletypes.AASRangeType;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create Ranges and integrate them into the
 * OPC UA address space.
 */
public class RangeCreator extends SubmodelElementCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RangeCreator.class);

    /**
     * Adds an AAS range object to the given node.
     *
     * @param aasRange The corresponding AAS range object to add
     * @param rangeRef The reference to the AAS range
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param nodeManager The corresponding Node Manager
     * @return The created node.
     */
    public static UaNode createAasRange(Range aasRange, Reference rangeRef, Submodel submodel, AasServiceNodeManager nodeManager) {
        UaNode retval = null;
        try {
            //LOGGER.info("createAasRange: not yet supported (experimental)");
            //            if ((node != null) && (aasRange != null)) {
            String name = aasRange.getIdShort();
            if ((name == null) || name.isEmpty()) {
                name = getNameFromReference(rangeRef);
            }
            QualifiedName browseName = UaQualifiedName.from(Ids.AASRangeType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.getDefaultNodeId();
            AASRangeType rangeNode = nodeManager.createInstance(AASRangeType.class, nid, browseName, LocalizedText.english(name));

            LOGGER.info("createAasRange: {}: create {}", name, nid);

            addSubmodelElementBaseData(rangeNode, aasRange, nodeManager);

            AasSubmodelElementHelper.setRangeValue(aasRange, rangeNode);

            nodeManager.addSubmodelElementOpcUA(rangeRef, rangeNode);
            nodeManager.addSubmodelElementAasMap(nid, new SubmodelElementData(aasRange, submodel, SubmodelElementData.Type.RANGE_VALUE, rangeRef));
            nodeManager.addReferable(rangeRef, new ObjectData(aasRange, rangeNode, submodel));

            retval = rangeNode;
            //                addSubmodelElementBaseData(rangeNode, aasRange, nodeManager);
            //
            //                addOpcUaRange(aasRange, rangeNode, submodel, rangeRef, nodeManager);
            //
            //                if (VALUES_READ_ONLY) {
            //                    // ValueType read-only
            //                    rangeNode.getValueTypeNode().setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
            //                }
            //
            //                if (ordered) {
            //                    node.addReference(rangeNode, Identifiers.HasOrderedComponent, false);
            //                }
            //                else {
            //                    node.addComponent(rangeNode);
            //                }
            //
            //                if (rangeRef != null) {
            //                    nodeManager.addReferable(rangeRef, new ObjectData(aasRange, rangeNode, submodel));
            //                }
            //            }
        }
        catch (Exception ex) {
            LOGGER.error("createAasRange Exception", ex);
        }
        return retval;
    }

    /**
     * Adds the min and max properties to the UA range object and sets the values
     *
     * @param aasRange The AAS range object
     * @param range The corresponding UA range object
     * @param submodel The corresponding submodel
     * @param rangeRef The AAS reference to the Range
     * @param nodeManager The corresponding Node Manager
     */
    //    private static void addOpcUaRange(Range aasRange, AASRangeType range, Submodel submodel, Reference rangeRef, AasServiceNodeManager nodeManager) throws StatusException {
    //        String minValue = aasRange.getMin();
    //        String maxValue = aasRange.getMax();
    //        NodeId myPropertyIdMin = new NodeId(nodeManager.getNamespaceIndex(), range.getNodeId().getValue().toString() + "." + AASRangeType.MIN);
    //        NodeId myPropertyIdMax = new NodeId(nodeManager.getNamespaceIndex(), range.getNodeId().getValue().toString() + "." + AASRangeType.MAX);
    //        DataTypeDefXsd valueType = aasRange.getValueType();
    //        QualifiedName browseNameMin = UaQualifiedName.from(ObjectTypeIds.AASRangeType.getNamespaceUri(), AASRangeType.MIN)
    //                .toQualifiedName(nodeManager.getNamespaceTable());
    //        LocalizedText displayNameMin = LocalizedText.english(AASRangeType.MIN);
    //        QualifiedName browseNameMax = UaQualifiedName.from(ObjectTypeIds.AASRangeType.getNamespaceUri(), AASRangeType.MAX)
    //                .toQualifiedName(nodeManager.getNamespaceTable());
    //        LocalizedText displayNameMax = LocalizedText.english(AASRangeType.MAX);
    //
    //        nodeManager.addSubmodelElementAasMap(myPropertyIdMin, new SubmodelElementData(aasRange, submodel, SubmodelElementData.Type.RANGE_MIN, rangeRef));
    //        nodeManager.addSubmodelElementAasMap(myPropertyIdMax, new SubmodelElementData(aasRange, submodel, SubmodelElementData.Type.RANGE_MAX, rangeRef));
    //
    //        nodeManager.addSubmodelElementOpcUA(rangeRef, range);
    //
    //        AasSubmodelElementHelper.setRangeValueAndType(valueType, minValue, maxValue, range, new ValueData(myPropertyIdMin, browseNameMin, displayNameMin, nodeManager),
    //                new ValueData(myPropertyIdMax, browseNameMax, displayNameMax, nodeManager));
    //    }

}
