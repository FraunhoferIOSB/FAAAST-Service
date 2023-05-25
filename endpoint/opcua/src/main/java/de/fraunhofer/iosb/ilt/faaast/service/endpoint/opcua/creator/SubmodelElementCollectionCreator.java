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

import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaQualifiedName;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.server.nodes.PlainProperty;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.Identifiers;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import opc.i4aas.AASOrderedSubmodelElementCollectionType;
import opc.i4aas.AASSubmodelElementCollectionType;


/**
 * Helper class to create SubmodelElementCollections and integrate them into the
 * OPC UA address space.
 */
public class SubmodelElementCollectionCreator extends SubmodelElementCreator {

    /**
     * Adds a SubmodelElementCollection to the given node.
     *
     * @param node The desired UA node
     * @param aasColl The corresponding SubmodelElementCollection to add
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The AAS reference to the parent object
     * @param ordered Specifies whether the entity should be added ordered
     *            (true) or unordered (false)
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    public static void addAasSubmodelElementCollection(UaNode node, SubmodelElementCollection aasColl, Submodel submodel, Reference parentRef, boolean ordered,
                                                       AasServiceNodeManager nodeManager)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        if ((node != null) && (aasColl != null)) {
            String name = aasColl.getIdShort();
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASSubmodelElementCollectionType.getNamespaceUri(), name)
                    .toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.getDefaultNodeId();
            AASSubmodelElementCollectionType collNode;
            if (aasColl.getOrdered()) {
                collNode = createAasOrderedSubmodelElementCollection(name, nid, nodeManager);
            }
            else {
                collNode = nodeManager.createInstance(AASSubmodelElementCollectionType.class, nid, browseName, LocalizedText.english(name));
            }

            addSubmodelElementBaseData(collNode, aasColl, nodeManager);

            // AllowDuplicates
            if (collNode.getAllowDuplicatesNode() == null) {
                NodeId myPropertyId = new NodeId(nodeManager.getNamespaceIndex(),
                        collNode.getNodeId().getValue().toString() + "." + AASSubmodelElementCollectionType.ALLOW_DUPLICATES);
                PlainProperty<Boolean> myProperty = new PlainProperty<>(nodeManager, myPropertyId,
                        UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASSubmodelElementCollectionType.getNamespaceUri(), AASSubmodelElementCollectionType.ALLOW_DUPLICATES)
                                .toQualifiedName(nodeManager.getNamespaceTable()),
                        LocalizedText.english(AASSubmodelElementCollectionType.ALLOW_DUPLICATES));
                myProperty.setDataTypeId(Identifiers.Boolean);
                myProperty.setDescription(new LocalizedText("", ""));
                if (VALUES_READ_ONLY) {
                    myProperty.setAccessLevel(AccessLevelType.CurrentRead);
                }
                collNode.addProperty(myProperty);
            }

            collNode.setAllowDuplicates(aasColl.getAllowDuplicates());

            Reference collRef = AasUtils.toReference(parentRef, aasColl);

            // SubmodelElements 
            addSubmodelElements(collNode, aasColl.getValues(), submodel, collRef, aasColl.getOrdered(), nodeManager);

            if (ordered) {
                node.addReference(collNode, Identifiers.HasOrderedComponent, false);
            }
            else {
                node.addComponent(collNode);
            }

            nodeManager.addReferable(collRef, new ObjectData(aasColl, collNode, submodel));
        }
    }


    /**
     * Creates an AAS Ordered Submodel Element Collection.
     *
     * @param name The desired name
     * @param nid The desired NodeId
     * @param nodeManager The corresponding Node Manager
     * @return The created Ordered Submodel Element Collection object
     */
    private static AASSubmodelElementCollectionType createAasOrderedSubmodelElementCollection(String name, NodeId nid, AasServiceNodeManager nodeManager) {
        AASSubmodelElementCollectionType retval = null;

        AASOrderedSubmodelElementCollectionType orderedNode = nodeManager.createInstance(AASOrderedSubmodelElementCollectionType.class, nid,
                UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASOrderedSubmodelElementCollectionType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable()),
                LocalizedText.english(name));

        retval = orderedNode;

        return retval;
    }

}
