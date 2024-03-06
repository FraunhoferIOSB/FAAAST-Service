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
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.Identifiers;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.UaHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import java.util.List;
import opc.i4aas.objecttypes.AASEntityType;
import opc.i4aas.objecttypes.AASSpecificAssetIdList;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create Entities and integrate them into the
 * OPC UA address space.
 */
public class EntityCreator extends SubmodelElementCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityCreator.class);

    /**
     * Adds an AAS entity to the given node.
     *
     * @param node The desired UA node
     * @param aasEntity The AAS entity to add
     * @param entityRef The AAS reference to the AAS entity
     * @param submodel The corresponding Submodel
     * @param ordered Specifies whether the entity should be added ordered
     *            (true) or unordered (false)
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     */
    public static void addAasEntity(UaNode node, Entity aasEntity, Reference entityRef, Submodel submodel, boolean ordered, AasServiceNodeManager nodeManager)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException, ValueFormatException {
        try {
            if ((node != null) && (aasEntity != null)) {
                String name = aasEntity.getIdShort();
                if ((name == null) || name.isEmpty()) {
                    name = getNameFromReference(entityRef);
                }
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASEntityType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
                NodeId nid = nodeManager.getDefaultNodeId();
                AASEntityType entityNode = nodeManager.createInstance(AASEntityType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(entityNode, aasEntity, nodeManager);

                // EntityType
                entityNode.setEntityType(ValueConverter.getAasEntityType(aasEntity.getEntityType()));

                nodeManager.addSubmodelElementAasMap(entityNode.getEntityTypeNode().getNodeId(),
                        new SubmodelElementData(aasEntity, submodel, SubmodelElementData.Type.ENTITY_TYPE, entityRef));

                // GlobalAssetId
                if (aasEntity.getGlobalAssetId() != null) {
                    addGlobalAssetIdData(entityNode, aasEntity, nodeManager, submodel, entityRef);
                }

                // SpecificAssetIds
                List<SpecificAssetId> specificAssetId = aasEntity.getSpecificAssetIds();
                if (specificAssetId != null) {
                    setSpecificAssetIdData(entityNode, specificAssetId, nodeManager);
                }

                // Statements
                SubmodelElementCreator.addSubmodelElements(entityNode.getStatementNode(), aasEntity.getStatements(), submodel, entityRef, nodeManager);

                nodeManager.addSubmodelElementOpcUA(entityRef, entityNode);

                if (ordered) {
                    node.addReference(entityNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(entityNode);
                }

                nodeManager.addReferable(entityRef, new ObjectData(aasEntity, entityNode, submodel));
            }
        }
        catch (Exception ex) {
            LOGGER.error("addAasEntity Exception", ex);
        }
    }


    public static void setGlobalAssetIdData(AASEntityType entityNode, String value, NodeManagerUaNode nodeManager) throws StatusException, ValueFormatException {
        if (entityNode.getGlobalAssetIdNode() == null) {
            // create node
            UaHelper.addStringUaProperty(entityNode, nodeManager, AASEntityType.GLOBAL_ASSET_ID, value, opc.i4aas.ObjectTypeIds.AASEntityType.getNamespaceUri());
        }
        else {
            entityNode.setGlobalAssetId(value);
        }
    }


    private static void setSpecificAssetIdData(AASEntityType entityNode, List<SpecificAssetId> specificAssetId, AasServiceNodeManager nodeManager) throws StatusException {
        AASSpecificAssetIdList listNode = entityNode.getSpecificAssetIdNode();
        if (listNode == null) {
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASSpecificAssetIdList.getNamespaceUri(), AASEntityType.SPECIFIC_ASSET_ID)
                    .toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.createNodeId(entityNode, browseName);
            listNode = nodeManager.createInstance(AASSpecificAssetIdList.class, nid, browseName, LocalizedText.english(AASEntityType.SPECIFIC_ASSET_ID));
            entityNode.addComponent(listNode);
        }

        SpecificAssetIdCreator.addSpecificAssetIdList(listNode, specificAssetId, nodeManager);
    }


    private static void addGlobalAssetIdData(AASEntityType entityNode, Entity aasEntity, AasServiceNodeManager nodeManager, Submodel submodel, Reference entityRef)
            throws StatusException, ValueFormatException {

        setGlobalAssetIdData(entityNode, aasEntity.getGlobalAssetId(), nodeManager);
        nodeManager.addSubmodelElementAasMap(entityNode.getGlobalAssetIdNode().getNodeId(),
                new SubmodelElementData(aasEntity, submodel, SubmodelElementData.Type.ENTITY_GLOBAL_ASSET_ID, entityRef));
    }
}
