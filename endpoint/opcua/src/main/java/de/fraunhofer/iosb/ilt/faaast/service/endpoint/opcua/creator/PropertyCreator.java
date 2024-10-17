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

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager.VALUES_READ_ONLY;

import com.prosysopc.ua.UaQualifiedName;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.Identifiers;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ValueData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.AasSubmodelElementHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import opc.i4aas.objecttypes.AASPropertyType;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create Properties and integrate them into the
 * OPC UA address space.
 */
public class PropertyCreator extends SubmodelElementCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyCreator.class);

    /**
     * Adds an AAS property the given node.
     *
     * @param node The desired node
     * @param aasProperty The corresponding AAS property to add
     * @param propertyRef The AAS reference to the AAS property
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param ordered Specifies whether the property should be added ordered
     *            (true) or unordered (false)
     * @param nodeManager The corresponding Node Manager
     */
    public static void addAasProperty(UaNode node, Property aasProperty, Reference propertyRef, Submodel submodel, boolean ordered, AasServiceNodeManager nodeManager) {
        try {
            String name = aasProperty.getIdShort();
            if ((name == null) || name.isEmpty()) {
                name = getNameFromReference(propertyRef);
            }
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASPropertyType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.getDefaultNodeId();

            AASPropertyType prop = nodeManager.createInstance(AASPropertyType.class, nid, browseName, LocalizedText.english(name));
            addSubmodelElementBaseData(prop, aasProperty, nodeManager);

            // ValueId
            Reference ref = aasProperty.getValueId();
            if (ref != null) {
                AasReferenceCreator.addAasReferenceAasNS(prop, ref, AASPropertyType.VALUE_ID, nodeManager);
            }

            // here Value and ValueType are set
            addOpcUaProperty(aasProperty, submodel, prop, propertyRef, nodeManager);

            if (propertyRef != null) {
                nodeManager.addSubmodelElementOpcUA(propertyRef, prop);
            }

            if (VALUES_READ_ONLY) {
                // ValueType read-only
                prop.getValueTypeNode().setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));

                // if the Submodel is null, we also make the value read-only
                if ((submodel == null) && (prop.getValueNode() != null)) {
                    prop.getValueNode().setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
                }
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("addAasProperty: add Property {}, Reference: {}", nid, ReferenceHelper.toString(ref));
            }

            if (ordered) {
                node.addReference(prop, Identifiers.HasOrderedComponent, false);
            }
            else {
                node.addComponent(prop);
            }

            if (propertyRef != null) {
                nodeManager.addReferable(propertyRef, new ObjectData(aasProperty, prop, submodel));
            }
        }
        catch (Exception ex) {
            LOGGER.error("addAasProperty Exception", ex);
        }
    }


    /**
     * Adds the OPC UA property itself to the given Property object and sets the value.
     *
     * @param aasProperty The AAS property
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param prop The UA Property object
     * @param propRef The AAS reference to the property
     * @param nodeManager The corresponding Node Manager
     */
    private static void addOpcUaProperty(Property aasProperty, Submodel submodel, AASPropertyType prop, Reference propRef, AasServiceNodeManager nodeManager) {
        try {
            NodeId myPropertyId = new NodeId(nodeManager.getNamespaceIndex(), prop.getNodeId().getValue().toString() + "." + AASPropertyType.VALUE);
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASPropertyType.getNamespaceUri(), AASPropertyType.VALUE)
                    .toQualifiedName(nodeManager.getNamespaceTable());
            LocalizedText displayName = LocalizedText.english(AASPropertyType.VALUE);

            nodeManager.addSubmodelElementAasMap(myPropertyId, new SubmodelElementData(aasProperty, submodel, SubmodelElementData.Type.PROPERTY_VALUE, propRef));
            LOGGER.debug("addOpcUaProperty: NodeId {}; Property: {}", myPropertyId, aasProperty);

            AasSubmodelElementHelper.setPropertyValueAndType(aasProperty, prop, new ValueData(myPropertyId, browseName, displayName, nodeManager));
        }
        catch (Exception ex) {
            LOGGER.error("addOpcUaProperty Exception", ex);
        }
    }

}
