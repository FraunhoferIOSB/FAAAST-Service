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
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.common.ServiceResultException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import opc.i4aas.objecttypes.AASSubmodelElementCollectionType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create SubmodelElementCollections and integrate them into the OPC UA address space.
 */
public class SubmodelElementCollectionCreator extends SubmodelElementCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubmodelElementCollectionCreator.class);

    /**
     * Adds a SubmodelElementCollection to the given node.
     *
     * @param node The desired UA node
     * @param aasColl The corresponding SubmodelElementCollection to add
     * @param collectionRef The AAS reference to the SubmodelElementCollection
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     */
    public static void addAasSubmodelElementCollection(UaNode node, SubmodelElementCollection aasColl, Reference collectionRef, Submodel submodel,
                                                       AasServiceNodeManager nodeManager)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException, ValueFormatException {
        try {
            if ((node != null) && (aasColl != null)) {
                String name = aasColl.getIdShort();
                if ((name == null) || name.isEmpty()) {
                    name = getNameFromReference(collectionRef);
                }
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASSubmodelElementCollectionType.getNamespaceUri(), name)
                        .toQualifiedName(nodeManager.getNamespaceTable());
                NodeId nid = nodeManager.getDefaultNodeId();
                AASSubmodelElementCollectionType collNode;

                collNode = nodeManager.createInstance(AASSubmodelElementCollectionType.class, nid, browseName, LocalizedText.english(name));

                addSubmodelElementBaseData(collNode, aasColl, nodeManager);

                // SubmodelElements 
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("addAasSubmodelElementCollection ({}): add {} SubmodelElements; Ref {}", name, aasColl.getValue().size(), ReferenceHelper.toString(collectionRef));
                }
                addSubmodelElements(collNode, aasColl.getValue(), collectionRef, submodel, false, nodeManager);

                node.addComponent(collNode);

                nodeManager.addReferable(collectionRef, new ObjectData(aasColl, collNode, submodel));
            }
        }
        catch (Exception ex) {
            LOGGER.error("addAasSubmodelElementCollection Exception", ex);
        }
    }

}
