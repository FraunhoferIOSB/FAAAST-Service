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
import com.prosysopc.ua.server.instantiation.NodeBuilder;
import com.prosysopc.ua.server.instantiation.NodeBuilderConfiguration;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ValueData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.AasSubmodelElementHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import opc.ua.aas.Ids;
import opc.ua.aas.variabletypes.AASPropertyType;
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
     * @param aasProperty The corresponding AAS property to add
     * @param propertyRef The AAS reference to the AAS property
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param nodeManager The corresponding Node Manager
     * @return The created node.
     */
    public static UaNode createAasProperty(Property aasProperty, Reference propertyRef, Submodel submodel, AasServiceNodeManager nodeManager) {
        UaNode retval = null;
        try {
            String name = aasProperty.getIdShort();
            if ((name == null) || name.isEmpty()) {
                name = getNameFromReference(propertyRef);
            }
            QualifiedName browseName = UaQualifiedName.from(Ids.AASPropertyType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.getDefaultNodeId();

            NodeBuilderConfiguration conf = new NodeBuilderConfiguration();
            conf.addOptional(Ids.AASPropertyType_ValueId);
            NodeBuilder<AASPropertyType> nb = nodeManager.createNodeBuilder(AASPropertyType.class, conf);
            nb.setBrowseName(browseName);
            LocalizedText displayName = LocalizedText.english(name);
            nb.setDisplayName(displayName);
            nb.setNodeId(nid);
            AASPropertyType prop = nb.build();

            LOGGER.info("createAasProperty: {}: create {}", name, nid);

            addSubmodelElementBaseData(prop, aasProperty, nodeManager);

            // ValueId
            if (prop.getValueIdNode() == null) {
                LOGGER.info("createAasProperty: ValueIdNode null");
            }
            else {
                prop.setValueId(ReferenceCreator.getAasReference(aasProperty.getValueId()));
            }

            // here Value and ValueType are set
            AasSubmodelElementHelper.setPropertyValueAndType(aasProperty, prop, new ValueData(nid, browseName, displayName, nodeManager));

            if (propertyRef != null) {
                nodeManager.addSubmodelElementOpcUA(propertyRef, prop);
            }

            nodeManager.addSubmodelElementAasMap(nid, new SubmodelElementData(aasProperty, submodel, SubmodelElementData.Type.PROPERTY_VALUE, propertyRef));

            LOGGER.atInfo().log("createAasProperty: add Property {}, Reference: {}", nid, ReferenceHelper.toString(propertyRef));

            if (propertyRef != null) {
                nodeManager.addReferable(propertyRef, new ObjectData(aasProperty, prop, submodel));
            }
            retval = prop;
        }
        catch (Exception ex) {
            LOGGER.error("addAasProperty Exception", ex);
        }
        return retval;
    }

}
