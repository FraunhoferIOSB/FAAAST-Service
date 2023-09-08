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

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.SubmodelElementCreator.addSubmodelElements;

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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import opc.i4aas.AASSubmodelElementListType;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


/**
 * Helper class to create SubmodelElementLists and integrate them into the OPC UA address space.
 */
public class SubmodelElementListCreator extends SubmodelElementCreator {

    /**
     * Adds a SubmodelElementList to the given node.
     *
     * @param node The desired UA node
     * @param aasList The corresponding SubmodelElementList to add
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The AAS reference to the parent object
     * @param ordered Specifies whether the entity should be added ordered (true) or unordered (false)
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     */
    public static void addAasSubmodelElementList(UaNode node, SubmodelElementList aasList, Submodel submodel, Reference parentRef, boolean ordered,
                                                 AasServiceNodeManager nodeManager)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException, ValueFormatException {
        if ((node != null) && (aasList != null)) {
            String name = aasList.getIdShort();
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASSubmodelElementListType.getNamespaceUri(), name)
                    .toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.getDefaultNodeId();
            AASSubmodelElementListType collNode;
            collNode = nodeManager.createInstance(AASSubmodelElementListType.class, nid, browseName, LocalizedText.english(name));

            addSubmodelElementBaseData(collNode, aasList, nodeManager);

            Reference collRef = AasUtils.toReference(parentRef, aasList);

            // add SubmodelElements 
            addSubmodelElements(collNode, aasList.getValue(), submodel, collRef, false, nodeManager);

            node.addComponent(collNode);

            nodeManager.addReferable(collRef, new ObjectData(aasList, collNode, submodel));
        }
    }
}
